package nexus.slime.deathsentence.nms;

import org.bukkit.Bukkit;

public class NmsProvider {
    public static Nms findNms() {
        var bukkitVersion = Bukkit.getServer().getBukkitVersion();

        return switch (bukkitVersion) {
            case "1.20.4-R0.1-SNAPSHOT" -> new NmsV1_20_4();
            case "1.20.2-R0.1-SNAPSHOT" -> new NmsV1_20_2();
            case "1.20.1-R0.1-SNAPSHOT" -> new NmsV1_20_1();
            case "1.20-R0.1-SNAPSHOT" -> new NmsV1_20();
            case "1.19.4-R0.1-SNAPSHOT" -> new NmsV1_19_4();
            default -> null;
        };
    }
}
