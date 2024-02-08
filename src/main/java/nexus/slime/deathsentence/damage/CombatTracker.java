package nexus.slime.deathsentence.damage;

import nexus.slime.deathsentence.DeathSentencePlugin;
import nexus.slime.deathsentence.UuidPersistentDataType;
import org.bukkit.NamespacedKey;
import org.bukkit.Registry;
import org.bukkit.entity.*;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
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
            var entity = getCausingEntity(entityEvent.getDamager());

            if (entity instanceof Mob mob) {
                var item = mob.getEquipment().getItemInMainHand();

                if (item.hasItemMeta() && item.getItemMeta().hasDisplayName()) {
                    return fixDamageSource(new DamageSource(damageType, entity, item));
                }
            }

            return fixDamageSource(new DamageSource(damageType, entity, null));
        } else {
            return fixDamageSource(new DamageSource(damageType, null, null));
        }
    }

    private DamageSource fixDamageSource(DamageSource damageSource) {
        // Some explosions are listed as "player_explosion", but not actually caused by players.
        // If that is the case, we change the damage type to "explosion".
        if (damageSource.damageType().equals(DamageType.PLAYER_EXPLOSION) && !(damageSource.causingEntity() instanceof Player)) {
            damageSource = new DamageSource(DamageType.EXPLOSION, damageSource.causingEntity(), damageSource.specialItem());
        }

        // TODO: The "unattributed_fireball" damage type is kindof goofy, we might wanna replace it with
        //  just being a regular "fireball", just without a causing entity.
        //  Same with indirect_magic, maybe?
        //  Same with mob_attack_no_aggro, maybe?
        //  Do something special for hunger given from a husk?
        //  Do something special for wither skeletons giving you wither effect?
        //  Wolves belonging to someone?

        // TODO: If the player is killed by a wither skull, the damage type should be wither skull all the time
        //  Note: This requires rewriting this method, so that we know the original entity and not the causing one

        // TODO: There is an extra case when an arrow of harming does magic damage, which is not attributed to the
        //  entity that shot the arrow, because it is (probably) not applied in the same tick

        return damageSource;
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
