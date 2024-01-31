package nexus.slime.deathsentence;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.util.logging.Level;

public record DeathSentenceCommand(
        DeathSentencePlugin plugin
) implements CommandExecutor {
    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        if (args.length != 1) {
            return false;
        }

        if (args[0].equalsIgnoreCase("generate_debug_config")) {
            try {
                Settings.generateDebugSettings(plugin.getDataFolder().toPath().resolve("debug_config.yml"));
            } catch (IOException e) {
                sender.sendMessage(Component.text("Could not generate debug config! See console for details!", NamedTextColor.RED));
                plugin.getLogger().log(Level.SEVERE, "Could not generate debug config!", e);
                return true;
            }

            sender.sendMessage(Component.text("Debug config successfully generated!", NamedTextColor.GREEN));
        }

        if (args[0].equalsIgnoreCase("reload")) {
            try {
                plugin.reloadSettings();
            } catch (IOException e) {
                sender.sendMessage(Component.text("Could not reload config! See console for details!", NamedTextColor.RED));
                plugin.getLogger().log(Level.SEVERE, "Could not reload config!", e);
                return true;
            }

            sender.sendMessage(Component.text("Config reloaded successfully!", NamedTextColor.GREEN));
        }

        return true;
    }
}
