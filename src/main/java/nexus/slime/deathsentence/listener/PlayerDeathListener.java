package nexus.slime.deathsentence.listener;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextReplacementConfig;
import nexus.slime.deathsentence.DeathSentencePlugin;
import nexus.slime.deathsentence.Settings;
import nexus.slime.deathsentence.damage.DamageSource;
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
        final var player = event.getEntity();
        final Component previousDeathMessage = event.deathMessage();

        // No death message - we do it ourselves
        event.deathMessage(null);

        Bukkit.getScheduler().runTask(plugin, () -> {
            if (!player.isOnline()) {
                return;
            }

            var damageSource = plugin.getCombatTracker().getLastDamageSource(player);

            if (damageSource == null) {
                plugin.getLogger().severe("Could not find damage source for dead player!");
                return;
            }

            if (plugin.getSettings().isLogDeaths()) {
                plugin.getLogger().info(getLogMessage(player, damageSource));
            }

            if (plugin.getCombatTracker().hasDeathMessageCooldown(player)) {
                return;
            }

            var deathMessage = findDeathMessage(player, damageSource, previousDeathMessage);
            var chatMessage = constructChatMessage(deathMessage, player, damageSource);

            plugin.getCombatTracker().setDeathMessageCooldown(player);
            Bukkit.getServer().sendMessage(chatMessage);
        });
    }

    public String getLogMessage(Player player, DamageSource source) {
        var damageType = source.damageType();
        var entity = source.responsibleEntity();
        String entityMessage = null;

        if (entity != null) {
            if (entity instanceof Player) {
                entityMessage = "Killer: " + entity.getName();
            } else {
                entityMessage = "Killer: " + entity.getType().getKey() + ", Killer UUID: " + entity.getUniqueId();
            }
        }

        return player.getName()
                + " died in world " + player.getWorld().getName()
                + " (type: " + player.getWorld().getEnvironment().name()
                + ") at " + player.getLocation().getBlockX()
                + "/" + player.getLocation().getBlockY()
                + "/" + player.getLocation().getBlockZ()
                + " (Damage type: " + damageType
                + (entityMessage != null ? ", " + entityMessage : "")
                + ")";
    }

    private Component constructChatMessage(DeathMessage message, Player player, DamageSource context) {
        var messagePlaceholders = plugin.getPlaceholderProvider()
                .with("player", player.displayName());

        if (context.responsibleEntity() != null) {
            var name = context.responsibleEntity() instanceof Player cause
                    ? cause.displayName()
                    : context.responsibleEntity().name();

            messagePlaceholders = messagePlaceholders.with("attacker", name);
        }

        if (context.specialItem() != null) {
            messagePlaceholders = messagePlaceholders.with("item", context.specialItem().displayName());
        }

        var actualMessage = message.message().replaceText(messagePlaceholders.asReplacement(player));

        return plugin.getSettings().getMessageTemplate().replaceText(
                plugin.getPlaceholderProvider()
                        .with("message", actualMessage)
                        .asReplacement(player)
        );
    }

    private DeathMessage findDeathMessage(Player player, DamageSource context, Component fallback) {
        Settings settings = plugin.getSettings();

        if (context.specialItem() != null) {
            var specialItemMessage = settings.getSpecialItemPool().findMessage(player, context);

            if (specialItemMessage != null) {
                return specialItemMessage;
            }
        }

        if (context.responsibleEntity() != null) {
            var entityMessage = settings.getEntityPool().findMessage(player, context);

            if (entityMessage != null) {
                return entityMessage;
            }
        }

        var naturalMessage = settings.getNaturalPool().findMessage(player, context);

        if (naturalMessage != null) {
            return naturalMessage;
        }

        return new DeathMessage(fallback);
    }
}
