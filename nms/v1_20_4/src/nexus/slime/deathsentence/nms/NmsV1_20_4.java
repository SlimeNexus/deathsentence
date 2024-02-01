package nexus.slime.deathsentence.nms;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.Registries;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.damagesource.DamageType;
import org.bukkit.NamespacedKey;
import org.bukkit.craftbukkit.v1_20_R3.entity.CraftPlayer;
import org.bukkit.entity.Player;

import java.util.List;
import java.util.Objects;

public class NmsV1_20_4 implements Nms {
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

        // DEBUG
        var entityTypeRegistry = MinecraftServer.getServer().registryAccess().registry(Registries.ENTITY_TYPE).get();
        var sourceEntity = damageSource.getEntity();
        var directEntity = damageSource.getDirectEntity();
        player.getServer().sendMessage(Component.text("Entity: " + (sourceEntity == null ? "null" : entityTypeRegistry.getKey(sourceEntity.getType()).toString() + sourceEntity), NamedTextColor.GOLD));
        player.getServer().sendMessage(Component.text("Direct Entity: " + (directEntity == null ? "null" : entityTypeRegistry.getKey(directEntity.getType()).toString() + directEntity), NamedTextColor.GOLD));
        // DEBUG

        var damageTypeKey = getDamageTypeRegistry().getKey(damageSource.type());
        return NamespacedKey.fromString(Objects.requireNonNull(damageTypeKey).toString());
    }
}