package nexus.slime.deathsentence.listener;

import nexus.slime.deathsentence.DeathSentencePlugin;
import nexus.slime.deathsentence.nms.ReflectionException;
import org.bukkit.Bukkit;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
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
            try {
                System.out.println(plugin.getDamageTypeAccessor().access(player));
                System.out.println(((EntityDamageByEntityEvent)event.getPlayer().getLastDamageCause()).getDamager());
            } catch (ReflectionException e) {
                throw new RuntimeException(e);
            }
        });
    }
}
