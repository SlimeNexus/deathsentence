package nexus.slime.deathsentence.damage;

import nexus.slime.deathsentence.DeathSentencePlugin;
import nexus.slime.deathsentence.UuidPersistentDataType;
import org.bukkit.NamespacedKey;
import org.bukkit.Registry;
import org.bukkit.entity.*;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.projectiles.ProjectileSource;

import java.util.HashSet;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;

public class CombatTracker {
    public static CombatTracker forPlugin(DeathSentencePlugin plugin) {
        DamageTypeRegistry registry = null;
        var damageTypes = plugin.getNms().getDamageTypes();

        if (damageTypes != null) {
            registry = DamageTypeRegistry.fromRawTypes(damageTypes);
        }

        return new CombatTracker(plugin, registry);
    }

    public static final NamespacedKey DAMAGING_ENTITY_KEY = Objects.requireNonNull(NamespacedKey.fromString("deathsentence:damaging_entity"));

    private final DeathSentencePlugin plugin;
    private final Registry<DamageType> damageTypeRegistry;

    private final Set<UUID> playersOnCooldown = new HashSet<>();

    private CombatTracker(DeathSentencePlugin plugin, Registry<DamageType> damageTypeRegistry) {
        this.plugin = plugin;
        this.damageTypeRegistry = damageTypeRegistry;
    }

    public void setDeathMessageCooldown(Player player) {
        UUID uuid = player.getUniqueId();
        playersOnCooldown.add(uuid);

        plugin.getServer().getScheduler().runTaskLater(
                plugin,
                () -> playersOnCooldown.remove(uuid),
                plugin.getSettings().getCooldownSeconds() * 20L
        );
    }

    public boolean hasDeathMessageCooldown(Player player) {
        return playersOnCooldown.contains(player.getUniqueId());
    }

    public Registry<DamageType> getDamageTypeRegistry() {
        return damageTypeRegistry;
    }

    public DamageType getDamageType(NamespacedKey key) {
        if (key == null) {
            return null;
        }

        if (damageTypeRegistry == null) {
            return new KeyedDamageType(key);
        }

        return damageTypeRegistry.get(key);
    }

    public DamageSource getLastDamageSource(Player player) {
        var damageType = new KeyedDamageType(plugin.getNms().getLastDamageType(player));
        var lastDamageEvent = player.getLastDamageCause();

        if (lastDamageEvent instanceof EntityDamageByEntityEvent entityEvent) {
            return createDamageSource(damageType, entityEvent.getDamager());
        } else {
            return createDamageSource(damageType, null);
        }
    }

    private DamageSource createDamageSource(DamageType damageType, Entity originalEntity) {
        Entity causingEntity = null;
        ItemStack specialItem = null;

        // Get causing entity
        if (originalEntity != null) {
            causingEntity = getCausingEntity(originalEntity);
        }

        // Check for special items
        if (causingEntity instanceof Mob mob) {
            var item = mob.getEquipment().getItemInMainHand();

            if (item.hasItemMeta() && item.getItemMeta().hasDisplayName()) {
                specialItem = item;
            }
        }

        // --------------------
        // The section below changes some of the behavior away from the vanilla one,
        // because the vanilla one is rather unintuitive and/or inconvenient in those cases.

        // Some explosions are listed as "player_explosion", but not actually caused by players.
        // If that is the case, we change the damage type to "explosion".
        if (damageType.equals(DamageType.PLAYER_EXPLOSION) && !(causingEntity instanceof Player)) {
            damageType = DamageType.EXPLOSION;
        }

        // The unattributed fireball damage type is unintuitive. If the player was killed by a
        // fireball, we just always use "fireball".
        if (originalEntity instanceof Fireball) {
            damageType = DamageType.FIREBALL;
        }

        // When we tested this, unowned wither skulls were always damage type "magic" and ones with a
        // corresponding wither were listed under "player_explosion". To simplify, we just always
        // "wither_skull" as a damage type if the player was killed by a wither skull.
        if (originalEntity instanceof WitherSkull) {
            damageType = DamageType.WITHER_SKULL;
        }

        // "indirect_magic" is a rather unintuitive damage type, because the split to "magic" is
        // a bit unclear. We just always use "magic" in this plugin.
        if (damageType.equals(DamageType.INDIRECT_MAGIC)) {
            damageType = DamageType.MAGIC;
        }

        // "mob_attack_no_aggro" is only used for goats ramming things, so we can just use "mob_attack"
        // instead, because that is probably what users expect.
        if (damageType.equals(DamageType.MOB_ATTACK_NO_AGGRO)) {
            damageType = DamageType.MOB_ATTACK;
        }

        // TODO: Do something special for hunger given from a husk?
        // TODO: Do something special for wither skeletons giving you wither effect?
        // TODO: Track wolves belonging to someone?
        // TODO: There is an extra case when an arrow of harming does magic damage, which is not attributed to the
        //  entity that shot the arrow, because it is (probably) not applied in the same tick

        return new DamageSource(damageType, causingEntity, specialItem);
    }

    private static Entity getCausingEntity(Entity entity) {
        var entityType = entity.getType();

        switch (entityType) {
            case FALLING_BLOCK:
            case ENDER_PEARL:
                entity = null;
        }

        // TODO: Consider what happens a player hits an end crystal with a snowball.
        //  Theoretically, we have enough information to put it to the correct player.

        if (entity instanceof Projectile projectile) {
            entity = extractProjectileSource(projectile.getShooter());
        }

        if (entity instanceof AreaEffectCloud cloud) {
            entity = extractProjectileSource(cloud.getSource());
        }

        if (entity instanceof LightningStrike lightning) {
            entity = extractSource(lightning.getCausingEntity());
        }

        if (entity instanceof TNTPrimed tnt) {
            entity = extractSource(tnt.getSource());
        }

        if (entity instanceof EvokerFangs fangs) {
            entity = extractSource(fangs.getOwner());
        }

        if (entity != null && entity.getPersistentDataContainer().has(DAMAGING_ENTITY_KEY)) {
            entity = extractSourceFromDataContainer(entity);
        }

        return entity;
    }

    private static Entity extractSourceFromDataContainer(Entity entity) {
        UUID uuid = entity.getPersistentDataContainer().get(DAMAGING_ENTITY_KEY, UuidPersistentDataType.UUID_TYPE);

        if (uuid == null) {
            return null;
        }

        return entity.getServer().getEntity(uuid);
    }

    private static Entity extractProjectileSource(ProjectileSource entity) {
        if (entity instanceof LivingEntity livingCause) {
            return livingCause;
        }

        return null;
    }

    private static Entity extractSource(Entity entity) {
        if (entity instanceof LivingEntity livingCause) {
            return livingCause;
        }

        return null;
    }
}
