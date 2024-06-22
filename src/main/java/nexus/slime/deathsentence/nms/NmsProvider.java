package nexus.slime.deathsentence.nms;

import nexus.slime.deathsentence.DeathSentencePlugin;
import org.bukkit.Bukkit;

import java.lang.reflect.InvocationTargetException;
import java.util.logging.Level;

public class NmsProvider {
    private static boolean isExperimentalApiAvailable() {
        try {
            Class.forName("org.bukkit.damage.DamageSource");
            return true;
        } catch (ClassNotFoundException e) {
            return false;
        }
    }

    public static Nms findNms(DeathSentencePlugin plugin) {
        if (!plugin.getSettings().isDisableExperimentalApi() && isExperimentalApiAvailable()) {
            plugin.getLogger().info("Experimental DamageSource Api seems to be present! Attempting to use it...");

            try {
                return new NmsExperimentalApi();
            } catch (Exception e) {
                plugin.getLogger().log(Level.SEVERE, "Failed to load experimental damage source api!", e);
            }
        }

        var bukkitVersion = Bukkit.getServer().getBukkitVersion();

        var nmsClassName = switch (bukkitVersion) {
            case "1.21-R0.1-SNAPSHOT" -> "NmsV1_21";
            case "1.20.6-R0.1-SNAPSHOT" -> "NmsV1_20_6";
            case "1.20.4-R0.1-SNAPSHOT" -> "NmsV1_20_4";
            default -> null;
        };

        if (nmsClassName == null) {
            plugin.getLogger().warning("This server version is unsupported. The plugin will attempt to work around this, but there will be some noticeable issues!");
            return new FallbackNms();
        }

        try {
            return (Nms) Class.forName("nexus.slime.deathsentence.nms." + nmsClassName)
                    .getConstructors()[0]
                    .newInstance();
        } catch (ClassNotFoundException
                 | InstantiationException
                 | IllegalAccessException
                 | InvocationTargetException e) {
            plugin.getLogger().log(Level.SEVERE, "Failed to load version specific NMS provider!", e);
            return new FallbackNms();
        }
    }
}
