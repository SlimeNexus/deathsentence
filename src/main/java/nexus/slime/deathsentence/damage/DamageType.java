package nexus.slime.deathsentence.damage;

import org.bukkit.Keyed;
import org.bukkit.NamespacedKey;

public interface DamageType extends Keyed {
    NamespacedKey GENERIC = NamespacedKey.minecraft("generic");
    NamespacedKey PLAYER_EXPLOSION = NamespacedKey.minecraft("player_explosion");
    NamespacedKey EXPLOSION = NamespacedKey.minecraft("explosion");
    NamespacedKey CACTUS = NamespacedKey.minecraft("cactus");
    NamespacedKey CRAMMING = NamespacedKey.minecraft("cramming");
    NamespacedKey DRAGON_BREATH = NamespacedKey.minecraft("dragon_breath");
    NamespacedKey DROWN = NamespacedKey.minecraft("drown");
    NamespacedKey DRY_OUT = NamespacedKey.minecraft("dry_out");
    NamespacedKey MOB_ATTACK = NamespacedKey.minecraft("mob_attack");
    NamespacedKey FALL = NamespacedKey.minecraft("fall");
    NamespacedKey FALLING_BLOCK = NamespacedKey.minecraft("falling_block");
    NamespacedKey IN_FIRE = NamespacedKey.minecraft("in_fire");
    NamespacedKey ON_FIRE = NamespacedKey.minecraft("on_fire");
    NamespacedKey FLY_INTO_WALL = NamespacedKey.minecraft("fly_into_wall");
    NamespacedKey FREEZE = NamespacedKey.minecraft("freeze");
    NamespacedKey HOT_FLOOR = NamespacedKey.minecraft("hot_floor");
    NamespacedKey GENERIC_KILL = NamespacedKey.minecraft("generic_kill");
    NamespacedKey LAVA = NamespacedKey.minecraft("lava");
    NamespacedKey LIGHTNING_BOLT = NamespacedKey.minecraft("lightning_bolt");
    NamespacedKey MAGIC = NamespacedKey.minecraft("magic");
    NamespacedKey SONIC_BOOM = NamespacedKey.minecraft("sonic_boom");
    NamespacedKey STARVE = NamespacedKey.minecraft("starve");
    NamespacedKey IN_WALL = NamespacedKey.minecraft("in_wall");
    NamespacedKey THORNS = NamespacedKey.minecraft("thorns");
    NamespacedKey OUT_OF_WORLD = NamespacedKey.minecraft("out_of_world");
    NamespacedKey WITHER = NamespacedKey.minecraft("wither");
    NamespacedKey OUTSIDE_BORDER = NamespacedKey.minecraft("outside_border");
}
