package nexus.slime.deathsentence.listener;

import nexus.slime.deathsentence.UuidPersistentDataType;
import nexus.slime.deathsentence.damage.CombatTracker;
import org.bukkit.entity.EnderCrystal;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;

public class EndCrystalExplodeListener implements Listener {
    @EventHandler(priority = EventPriority.MONITOR)
    public void onEntityDamage(EntityDamageByEntityEvent event) {
        var damagedEntity = event.getEntity();

        if (!(damagedEntity instanceof EnderCrystal crystal)) return;

        var damagingEntity = CombatTracker.trackResponsibleEntity(event.getDamager());

        crystal.getPersistentDataContainer().set(
                CombatTracker.DAMAGING_ENTITY_KEY,
                UuidPersistentDataType.UUID_TYPE,
                damagingEntity.getUniqueId()
        );
    }
}
