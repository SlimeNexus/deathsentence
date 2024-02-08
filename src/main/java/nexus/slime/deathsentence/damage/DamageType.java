package nexus.slime.deathsentence.damage;

import org.bukkit.Keyed;

public interface DamageType extends Keyed {
    DamageType GENERIC = KeyedDamageType.fromKey("generic");
    DamageType PLAYER_EXPLOSION = KeyedDamageType.fromKey("player_explosion");
    DamageType EXPLOSION = KeyedDamageType.fromKey("explosion");
    DamageType CACTUS = KeyedDamageType.fromKey("cactus");
    DamageType CRAMMING = KeyedDamageType.fromKey("cramming");
    DamageType DRAGON_BREATH = KeyedDamageType.fromKey("dragon_breath");
    DamageType DROWN = KeyedDamageType.fromKey("drown");
    DamageType DRY_OUT = KeyedDamageType.fromKey("dry_out");
    DamageType MOB_ATTACK = KeyedDamageType.fromKey("mob_attack");
    DamageType FALL = KeyedDamageType.fromKey("fall");
    DamageType FALLING_BLOCK = KeyedDamageType.fromKey("falling_block");
    DamageType IN_FIRE = KeyedDamageType.fromKey("in_fire");
    DamageType ON_FIRE = KeyedDamageType.fromKey("on_fire");
    DamageType FLY_INTO_WALL = KeyedDamageType.fromKey("fly_into_wall");
    DamageType FREEZE = KeyedDamageType.fromKey("freeze");
    DamageType HOT_FLOOR = KeyedDamageType.fromKey("hot_floor");
    DamageType GENERIC_KILL = KeyedDamageType.fromKey("generic_kill");
    DamageType LAVA = KeyedDamageType.fromKey("lava");
    DamageType LIGHTNING_BOLT = KeyedDamageType.fromKey("lightning_bolt");
    DamageType MAGIC = KeyedDamageType.fromKey("magic");
    DamageType SONIC_BOOM = KeyedDamageType.fromKey("sonic_boom");
    DamageType STARVE = KeyedDamageType.fromKey("starve");
    DamageType IN_WALL = KeyedDamageType.fromKey("in_wall");
    DamageType THORNS = KeyedDamageType.fromKey("thorns");
    DamageType OUT_OF_WORLD = KeyedDamageType.fromKey("out_of_world");
    DamageType WITHER = KeyedDamageType.fromKey("wither");
    DamageType OUTSIDE_BORDER = KeyedDamageType.fromKey("outside_border");
    DamageType FIREBALL = KeyedDamageType.fromKey("fireball");
    DamageType WITHER_SKULL = KeyedDamageType.fromKey("wither_skull");
    DamageType INDIRECT_MAGIC = KeyedDamageType.fromKey("indirect_magic");
    DamageType MOB_ATTACK_NO_AGGRO = KeyedDamageType.fromKey("mob_attack_no_aggro");
}
