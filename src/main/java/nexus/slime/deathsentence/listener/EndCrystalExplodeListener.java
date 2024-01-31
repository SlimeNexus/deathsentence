package nexus.slime.deathsentence.listener;

import nexus.slime.deathsentence.UuidPersistentDataType;
import nexus.slime.deathsentence.damage.CombatTracker;
import org.bukkit.entity.EnderCrystal;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;

public class EndCrystalExplodeListener implements Listener {
    @EventHandler
    public void onEntityDamage(EntityDamageByEntityEvent event) {
        var damagedEntity = event.getEntity();
        var damagingEntity = event.getDamager();

        if (!(damagingEntity instanceof Player player)) return;
        if (!(damagedEntity instanceof EnderCrystal crystal)) return;

        crystal.getPersistentDataContainer()
                .set(CombatTracker.DAMAGING_ENTITY_KEY, UuidPersistentDataType.UUID_TYPE, player.getUniqueId());
    }
}
