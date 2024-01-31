package nexus.slime.deathsentence.damage;

import org.bukkit.entity.*;
import org.bukkit.projectiles.ProjectileSource;

import java.util.List;

public class DamageSources {
    // Prevent instantiation
    private DamageSources() {}

    public static DamageSource getLastDamageSource(Player player) {
        return null;
//        if (!(player instanceof CraftPlayer craftPlayer)) {
//            throw new RuntimeException("Player is not a CraftPlayer?!");
//        }
//
//        var lastDamageSource = craftPlayer.getHandle().getLastDamageSource();
//
//        if (lastDamageSource == null) {
//            return null;
//        }
//
//        var damageTypeRegistry = MinecraftServer.getServer().registryAccess().registry(Registries.DAMAGE_TYPE);
//
//        if (damageTypeRegistry.isEmpty()) {
//            throw new RuntimeException("Could not find damage type registry");
//        }
//
//        var damageTypeKey = damageTypeRegistry.get().getKey(lastDamageSource.type());
//
//        if (damageTypeKey == null) {
//            throw new RuntimeException("Could not find damage type for " + lastDamageSource);
//        }
//
//        var entityTypeRegistry = MinecraftServer.getServer().registryAccess().registry(Registries.ENTITY_TYPE).get();
//        var sourceEntity = lastDamageSource.getEntity();
//        var directEntity = lastDamageSource.getDirectEntity();
//        player.getServer().sendMessage(Component.text("Entity: " + (sourceEntity == null ? "null" : entityTypeRegistry.getKey(sourceEntity.getType()).toString() + sourceEntity), NamedTextColor.GOLD));
//        player.getServer().sendMessage(Component.text("Direct Entity: " + (directEntity == null ? "null" : entityTypeRegistry.getKey(directEntity.getType()).toString() + directEntity), NamedTextColor.GOLD));
//
//        var damageType = nexus.slime.deathsentence.damage.DamageType.fromKey(damageTypeKey.toString());
//        var lastDamageEvent = player.getLastDamageCause();
//
//        if (lastDamageEvent instanceof EntityDamageByEntityEvent entityEvent) {
//            var entity = getCausingEntity(entityEvent.getDamager());
//
//            if (entity instanceof Mob mob) {
//                var item = mob.getEquipment().getItemInMainHand();
//
//                if (item.hasItemMeta() && item.getItemMeta().hasDisplayName()) {
//                    return fixDamageSource(new DamageSource(damageType, entity, item));
//                }
//            }
//
//            return fixDamageSource(new DamageSource(damageType, entity, null));
//        } else {
//            return fixDamageSource(new DamageSource(damageType, null, null));
//        }
    }

    private static DamageSource fixDamageSource(DamageSource damageSource) {
        // Some explosions are listed as "player_explosion", but not actually caused by players.
        // If that is the case, we change the damage type as "explosion".
        if (damageSource.damageType().equals(DamageType.PLAYER_EXPLOSION) && !(damageSource.causingEntity() instanceof Player)) {
            damageSource = new DamageSource(DamageType.EXPLOSION, damageSource.causingEntity(), damageSource.specialItem());
        }

        return damageSource;
    }

    private static Entity getCausingEntity(Entity entity) {
        var entityType = entity.getType();

        switch (entityType) {
            case FALLING_BLOCK:
            case ENDER_PEARL:
            case ENDER_CRYSTAL: // TODO: Handle end crystal properly
                entity = null;
        }

        System.out.println("boop");
        if (entity instanceof Projectile projectile) {
            System.out.println("hi there");
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

        return entity;
    }

    private static Entity extractProjectileSource(ProjectileSource entity) {
        if (entity instanceof LivingEntity livingCause) {
            System.out.println("uhhh....");
            System.out.println(livingCause);
            return getCausingEntity(livingCause);
        }

        return null;
    }

    private static Entity extractSource(Entity entity) {
        if (entity instanceof LivingEntity livingCause) {
            return getCausingEntity(livingCause);
        }

        return null;
    }

    public static List<DamageType> listDamageTypes() {
        return null;
//        return MinecraftServer.getServer()
//                .registryAccess()
//                .registry(Registries.DAMAGE_TYPE)
//                .orElseThrow(() -> new RuntimeException("Could not find damage type registry!"))
//                .entrySet()
//                .stream()
//                .map(entry -> nexus.slime.deathsentence.damage.DamageType.fromKey(entry.getKey().location().toString()))
//                .toList();
    }
}
