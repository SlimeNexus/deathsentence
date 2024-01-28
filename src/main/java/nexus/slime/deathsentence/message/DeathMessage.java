package nexus.slime.deathsentence.message;

import net.kyori.adventure.text.Component;

public record DeathMessage(
        Component message,
        double weight,
        String world,
        String worldType,
        String permission
) {
    public DeathMessage(Component message) {
        this(message, 1, null, null, null);
    }
}
