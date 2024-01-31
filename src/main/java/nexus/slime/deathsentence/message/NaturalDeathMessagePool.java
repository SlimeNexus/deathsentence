package nexus.slime.deathsentence.message;

import nexus.slime.deathsentence.damage.DamageSource;
import nexus.slime.deathsentence.damage.DamageType;
import org.bukkit.entity.Player;

import java.util.List;
import java.util.Map;

public record NaturalDeathMessagePool(
        Map<DamageType, List<DeathMessage>> messages
) {
    public DeathMessage findMessage(Player player, DamageSource context) {
        return DeathMessage.choose(
                messages,
                context.damageType(),
                DamageType.GENERIC,
                list -> DeathMessage.choose(list, player, context)
        );
    }
}
