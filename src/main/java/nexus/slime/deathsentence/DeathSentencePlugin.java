package nexus.slime.deathsentence;

import nexus.slime.deathsentence.damage.CombatTracker;
import nexus.slime.deathsentence.listener.EndCrystalExplodeListener;
import nexus.slime.deathsentence.listener.PlayerDeathListener;
import nexus.slime.deathsentence.nms.FallbackNms;
import nexus.slime.deathsentence.nms.Nms;
import nexus.slime.deathsentence.nms.NmsProvider;
import nexus.slime.deathsentence.placeholder.PlaceholderApiProvider;
import nexus.slime.deathsentence.placeholder.PlaceholderProvider;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.IOException;
import java.util.Objects;
import java.util.logging.Level;

public class DeathSentencePlugin extends JavaPlugin {
    private Nms nms = null;
    private CombatTracker combatTracker = null;
    private Settings settings = null;
    private PlaceholderProvider placeholderProvider = PlaceholderProvider.EMPTY;

    @Override
    public void onLoad() {
        nms = NmsProvider.findNms();

        if (nms == null) {
            getLogger().warning("Unsupported server version found! Some features of this plugin may not be working correctly on this version!");
            nms = new FallbackNms();
        }

        getLogger().info("Using NmsAdapter: " + nms.getClass().getSimpleName());

        combatTracker = CombatTracker.forPlugin(this);

        try {
            reloadSettings();
        } catch (IOException e) {
            getLogger().log(Level.SEVERE, "Error while loading plugin configuration! The plugin will not work!", e);
            throw new RuntimeException();
        }

        getLogger().info("Plugin loaded!");
    }

    @Override
    public void onEnable() {
        getServer().getPluginManager().registerEvents(new EndCrystalExplodeListener(), this);
        getServer().getPluginManager().registerEvents(new PlayerDeathListener(this), this);
        Objects.requireNonNull(getCommand("deathsentence")).setExecutor(new DeathSentenceCommand(this));

        if (getServer().getPluginManager().getPlugin("PlaceholderAPI") != null) {
            placeholderProvider = new PlaceholderApiProvider();
            getLogger().info("Using PlaceholderAPI for placeholders!");
        }

        getLogger().info("Plugin enabled!");
    }

    @Override
    public void onDisable() {
        getLogger().info("Plugin disabled!");
    }

    public CombatTracker getCombatTracker() {
        return combatTracker;
    }

    public Nms getNms() {
        return nms;
    }

    public Settings getSettings() {
        return Objects.requireNonNull(settings);
    }

    public PlaceholderProvider getPlaceholderProvider() {
        return placeholderProvider;
    }

    public void reloadSettings() throws IOException {
        settings = Settings.loadSettings(this);
    }
}
