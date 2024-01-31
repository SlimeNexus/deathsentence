package nexus.slime.deathsentence.nms;

import nexus.slime.deathsentence.damage.DamageType;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.Player;

import java.util.List;

public class FallbackNms implements Nms {
    @Override
    public List<NamespacedKey> getDamageTypes() {
        return null;
    }

    @Override
    public NamespacedKey getLastDamageType(Player player) {
        var event = player.getLastDamageCause();

        if (event == null) {
            return null;
        }

        var cause = event.getCause();

        return switch (cause) {
            case BLOCK_EXPLOSION, ENTITY_EXPLOSION -> DamageType.EXPLOSION;
            case CONTACT -> DamageType.CACTUS;
            case CRAMMING -> DamageType.CRAMMING;
            case DRAGON_BREATH -> DamageType.DRAGON_BREATH;
            case DROWNING -> DamageType.DROWN;
            case DRYOUT -> DamageType.DRY_OUT;
            case ENTITY_ATTACK, ENTITY_SWEEP_ATTACK -> DamageType.MOB_ATTACK;
            case FALL -> DamageType.FALL;
            case FALLING_BLOCK -> DamageType.FALLING_BLOCK;
            case FIRE -> DamageType.IN_FIRE;
            case FIRE_TICK -> DamageType.ON_FIRE;
            case FLY_INTO_WALL -> DamageType.FLY_INTO_WALL;
            case FREEZE -> DamageType.FREEZE;
            case HOT_FLOOR -> DamageType.HOT_FLOOR;
            case KILL -> DamageType.GENERIC_KILL;
            case LAVA -> DamageType.LAVA;
            case LIGHTNING -> DamageType.LIGHTNING_BOLT;
            case MAGIC, POISON -> DamageType.MAGIC;
            case SONIC_BOOM -> DamageType.SONIC_BOOM;
            case STARVATION -> DamageType.STARVE;
            case SUFFOCATION -> DamageType.IN_WALL;
            case THORNS -> DamageType.THORNS;
            case VOID -> DamageType.OUT_OF_WORLD;
            case WITHER -> DamageType.WITHER;
            case WORLD_BORDER -> DamageType.OUTSIDE_BORDER;
            default -> DamageType.GENERIC;
        };
    }
}
