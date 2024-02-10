package nexus.slime.deathsentence.damage;

import org.bukkit.entity.Entity;
import org.bukkit.inventory.ItemStack;

public record DamageSource(
        DamageType damageType,
        Entity responsibleEntity,
        ItemStack specialItem
) {
}
