package nexus.slime.deathsentence;

import nexus.slime.deathsentence.listener.PlayerDeathListener;
import nexus.slime.deathsentence.nms.NMS;
import nexus.slime.deathsentence.nms.ReflectionException;
import nexus.slime.deathsentence.nms.ReflectionOperation;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.IOException;
import java.util.Objects;
import java.util.logging.Level;

public class DeathSentencePlugin extends JavaPlugin {
    private Settings settings = null;
    private ReflectionOperation<String> damageTypeAccessor;

    @Override
    public void onLoad() {
        try {
            settings = Settings.loadSettings(this);
        } catch (IOException e) {
            getLogger().log(Level.SEVERE, "Error while loading plugin configuration! The plugin will not work!", e);
            throw new RuntimeException();
        }

        try {
            damageTypeAccessor = NMS.damageType();
        } catch (ReflectionException e) {
            getLogger().log(Level.SEVERE, "Could not instantiate reflective operation!", e);
            throw new RuntimeException();
        }

        getLogger().info("Plugin loaded!");
    }

    @Override
    public void onEnable() {
        getServer().getPluginManager().registerEvents(new PlayerDeathListener(this), this);

        getLogger().info("Plugin enabled!");
    }

    @Override
    public void onDisable() {
        getLogger().info("Plugin disabled!");
    }

    public Settings getSettings() {
        return Objects.requireNonNull(settings);
    }

    public ReflectionOperation<String> getDamageTypeAccessor() {
        return Objects.requireNonNull(damageTypeAccessor);
    }
}
