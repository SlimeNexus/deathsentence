package nexus.slime.deathsentence.placeholder;

import me.clip.placeholderapi.PlaceholderAPI;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import org.bukkit.entity.Player;

public class PlaceholderApiProvider implements PlaceholderProvider {
    private static final LegacyComponentSerializer SERIALIZER = LegacyComponentSerializer.legacySection();

    @Override
    public Component resolvePlaceholder(Player player, String placeholder) {
        var text = "%" + placeholder + "%";
        var result = PlaceholderAPI.setPlaceholders(player, text);

        if (result.equals(text)) {
            return null;
        }

        return SERIALIZER.deserialize(result);
    }
}
