package nexus.slime.deathsentence;

import nexus.slime.deathsentence.damage.DamageTypeRegistry;
import nexus.slime.deathsentence.listener.PlayerDeathListener;
import nexus.slime.deathsentence.nms.Nms;
import nexus.slime.deathsentence.nms.NmsV1_20_4;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.IOException;
import java.util.Objects;
import java.util.logging.Level;

public class DeathSentencePlugin extends JavaPlugin {
    private Settings settings = null;
    private DamageTypeRegistry registry = null;
    private Nms nms = null;

    @Override
    public void onLoad() {
        try {
            reloadSettings();
        } catch (IOException e) {
            getLogger().log(Level.SEVERE, "Error while loading plugin configuration! The plugin will not work!", e);
            throw new RuntimeException();
        }

        nms = new NmsV1_20_4();

        getLogger().info("Plugin loaded!");
    }

    @Override
    public void onEnable() {
        getServer().getPluginManager().registerEvents(new PlayerDeathListener(this), this);
        Objects.requireNonNull(getCommand("deathsentence")).setExecutor(new DeathSentenceCommand(this));

        getLogger().info("Plugin enabled!");
    }

    @Override
    public void onDisable() {
        getLogger().info("Plugin disabled!");
    }

    public Settings getSettings() {
        return Objects.requireNonNull(settings);
    }

    public void reloadSettings() throws IOException {
        settings = Settings.loadSettings(this);
    }
}
