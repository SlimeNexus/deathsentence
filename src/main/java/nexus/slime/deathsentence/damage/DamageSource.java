package nexus.slime.deathsentence.damage;

import org.bukkit.entity.Entity;

public record DamageSource(
        String damageType,
        Entity causingEntity
) {
}
