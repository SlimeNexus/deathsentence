package nexus.slime.deathsentence.message;

import net.kyori.adventure.text.Component;
import nexus.slime.deathsentence.damage.DamageSource;
import org.bukkit.entity.Player;

import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.function.Function;

public record DeathMessage(
        Component message,
        double weight,
        String world,
        String worldType,
        String permission
) {
    private static final Random random = new Random();

    public static DeathMessage choose(List<DeathMessage> messages, Player player, DamageSource context) {
        var list = messages.stream()
                .filter(message -> message.isApplicable(player, context))
                .toList();

        if (list.isEmpty()) {
            return null;
        }

        double totalWeight = list.stream()
                .mapToDouble(DeathMessage::weight)
                .sum();

        double remainingWeight = random.nextDouble(totalWeight);

        for (DeathMessage message : list) {
            if (message.weight >= remainingWeight) {
                return message;
            }

            remainingWeight -= message.weight;
        }

        return null;
    }

    public static <K, V> DeathMessage choose(Map<K, V> map, K specificKey, K genericKey, Function<V, DeathMessage> messageExtractor) {
        var specificItem = map.get(specificKey);

        if (specificItem != null) {
            var specificMessage = messageExtractor.apply(specificItem);

            if (specificMessage != null) {
                return specificMessage;
            }
        }

        var genericItem = map.get(genericKey);

        if (genericItem != null) {
            var genericMessage = messageExtractor.apply(genericItem);

            //noinspection RedundantIfStatement
            if (genericMessage != null) {
                return genericMessage;
            }
        }

        return null;
    }

    public DeathMessage(Component message) {
        this(message, 1, null, null, null);
    }

    public boolean isApplicable(Player player, DamageSource context) {
        if (world != null && !player.getWorld().getName().equalsIgnoreCase(world)) return false;
        if (worldType != null && !player.getWorld().getEnvironment().name().equalsIgnoreCase(worldType)) return false;
        return permission == null || player.hasPermission(permission);
    }
}
