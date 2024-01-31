package nexus.slime.deathsentence.message;

import nexus.slime.deathsentence.damage.DamageSource;
import nexus.slime.deathsentence.damage.DamageType;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;

import java.util.List;
import java.util.Map;

public record EntityDeathMessagePool(
        Map<EntityType, Map<DamageType, List<DeathMessage>>> messages
) {
    public static final EntityType CUSTOM_ENTITY_TYPE = EntityType.UNKNOWN;

    public DeathMessage findMessage(Player player, DamageSource context) {
        if (context.causingEntity() == null) {
            return null;
        }

        return DeathMessage.choose(
                messages,
                context.causingEntity().getType(),
                CUSTOM_ENTITY_TYPE,
                map -> DeathMessage.choose(
                        map,
                        context.damageType(),
                        DamageType.GENERIC,
                        list -> DeathMessage.choose(list, player, context)
                )
        );
    }
}
