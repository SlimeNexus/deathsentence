package nexus.slime.deathsentence.listener;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextReplacementConfig;
import nexus.slime.deathsentence.DeathSentencePlugin;
import nexus.slime.deathsentence.Settings;
import nexus.slime.deathsentence.damage.DamageSource;
import nexus.slime.deathsentence.damage.DamageSources;
import nexus.slime.deathsentence.message.DeathMessage;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PlayerDeathEvent;

public record PlayerDeathListener(
        DeathSentencePlugin plugin
) implements Listener {
    @EventHandler
    public void onDeath(PlayerDeathEvent event) {
        // No death message - we do it ourselves
        var player = event.getEntity();

        event.deathMessage(null);

        Bukkit.getScheduler().runTask(plugin, () -> {
            if (!player.isOnline()) {
                return;
            }

            var damageSource = DamageSources.getLastDamageSource(player);

            if (damageSource == null) {
                plugin.getLogger().severe("Could not find damage source for dead player!");
                return;
            }

            var deathMessage = findDeathMessage(player, damageSource);
            var chatMessage = constructChatMessage(deathMessage, player, damageSource);

            Bukkit.getServer().sendMessage(chatMessage);
            // TODO: Expand
            plugin.getLogger().info(player.getName() + " died in " + player.getWorld().getName() + " at " + player.getLocation().getBlockX() + "/" + player.getLocation().getBlockY() + "/" + player.getLocation().getBlockZ() + " (Cause: " + damageSource.damageType() + ")");
        });
    }

    private Component constructChatMessage(DeathMessage message, Player player, DamageSource context) {
        var actualMessage = message.message();

        actualMessage = actualMessage.replaceText(TextReplacementConfig.builder()
                .match("%player%")
                .replacement(player.displayName())
                .build());

        if (context.causingEntity() != null) {
            actualMessage = actualMessage.replaceText(TextReplacementConfig.builder()
                    .match("%attacker%")
                    .replacement(context.causingEntity() instanceof Player cause
                            ? cause.displayName()
                            : context.causingEntity().name())
                    .build());
        }

        if (context.specialItem() != null) {
            actualMessage = actualMessage.replaceText(TextReplacementConfig.builder()
                    .match("%item%")
                    .replacement(context.specialItem().displayName())
                    .build());
        }

        return plugin.getSettings().getPrefix()
                .append(actualMessage);
    }

    private DeathMessage findDeathMessage(Player player, DamageSource context) {
        Settings settings = plugin.getSettings();

        if (context.specialItem() != null) {
            var specialItemMessage = settings.getSpecialItemPool().findMessage(player, context);

            if (specialItemMessage != null) {
                return specialItemMessage;
            }
        }

        if (context.causingEntity() != null) {
            var entityMessage = settings.getEntityPool().findMessage(player, context);

            if (entityMessage != null) {
                return entityMessage;
            }
        }

        var naturalMessage = settings.getNaturalPool().findMessage(player, context);

        if (naturalMessage != null) {
            return naturalMessage;
        }

        return new DeathMessage(settings.getFallbackMessage());
    }
}
