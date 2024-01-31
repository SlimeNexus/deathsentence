package nexus.slime.deathsentence.nms;

import org.bukkit.NamespacedKey;
import org.bukkit.entity.Player;

import java.util.List;

public interface Nms {
    List<NamespacedKey> getDamageTypes();

    NamespacedKey getLastDamageType(Player player);
}
