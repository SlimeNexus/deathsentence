package nexus.slime.deathsentence.placeholder;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextReplacementConfig;
import org.bukkit.entity.Player;

import java.util.regex.Pattern;

@FunctionalInterface
public interface PlaceholderProvider {
    Component resolvePlaceholder(Player player, String placeholder);

    default PlaceholderProvider with(String placeholder, Component value) {
        return (player, resolvingPlaceholder) -> {
            if (resolvingPlaceholder.equalsIgnoreCase(placeholder)) {
                return value;
            }

            return resolvePlaceholder(player, resolvingPlaceholder);
        };
    }

    default TextReplacementConfig asReplacement(Player player) {
        return TextReplacementConfig.builder()
                .match(PLACEHOLDER_PATTERN)
                .replacement((match, builder) -> {
                    Component result = resolvePlaceholder(player, match.group(1));
                    if (result == null) return Component.text(match.group());
                    else return result;
                })
                .build();
    }

    PlaceholderProvider EMPTY = (player, placeholder) -> null;

    Pattern PLACEHOLDER_PATTERN = Pattern.compile("%([^%]+)%");
}
