package nexus.slime.deathsentence.nms;

import org.bukkit.NamespacedKey;
import org.bukkit.Registry;
import org.bukkit.damage.DamageType;
import org.bukkit.entity.Player;

import java.util.List;

@SuppressWarnings("UnstableApiUsage")
public class NmsExperimentalApi implements Nms {

    // This is here so that we fail immediately when the experimental damagesource
    // api is not available on the server.
    public NmsExperimentalApi() {
    }

    @Override
    public List<NamespacedKey> getDamageTypes() {
        // Wait for response on paper
        return Registry.DAMAGE_TYPE
                .stream()
                .map(DamageType::getKey)
                .toList();
    }

    @Override
    public NamespacedKey getLastDamageType(Player player) {
        var damageCause = player.getLastDamageCause();

        if (damageCause == null) {
            return null;
        }

        return damageCause.getDamageSource().getDamageType().getKey();
    }
}
