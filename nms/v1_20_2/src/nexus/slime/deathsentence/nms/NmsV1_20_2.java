package nexus.slime.deathsentence.nms;

import net.minecraft.core.Registry;
import net.minecraft.core.registries.Registries;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.damagesource.DamageType;
import org.bukkit.NamespacedKey;
import org.bukkit.craftbukkit.v1_20_R2.entity.CraftPlayer;
import org.bukkit.entity.Player;

import java.util.List;
import java.util.Objects;

public class NmsV1_20_2 implements Nms {
    private Registry<DamageType> getDamageTypeRegistry() {
        return MinecraftServer.getServer()
                .registryAccess()
                .registry(Registries.DAMAGE_TYPE)
                .orElseThrow(() -> new RuntimeException("Could not find damage type registry!"));
    }

    @Override
    public List<NamespacedKey> getDamageTypes() {
        return getDamageTypeRegistry()
                .keySet()
                .stream()
                .map(damageType -> NamespacedKey.fromString(damageType.toString()))
                .toList();
    }

    @Override
    public NamespacedKey getLastDamageType(Player player) {
        var damageSource = ((CraftPlayer) player).getHandle().getLastDamageSource();

        if (damageSource == null) {
            return null;
        }

        var damageTypeKey = getDamageTypeRegistry().getKey(damageSource.type());
        return NamespacedKey.fromString(Objects.requireNonNull(damageTypeKey).toString());
    }
}
