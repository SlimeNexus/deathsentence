package nexus.slime.deathsentence.nms;

import org.bukkit.Bukkit;

public class NmsProvider {
    public static Nms findNms() {
        var bukkitVersion = Bukkit.getServer().getBukkitVersion();

        return switch (bukkitVersion) {
            case "1.20.4-R0.1-SNAPSHOT" -> new NmsV1_20_4();
            default -> new FallbackNms();
        };
    }
}
