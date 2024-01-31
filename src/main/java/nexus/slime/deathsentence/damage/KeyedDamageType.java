package nexus.slime.deathsentence.damage;

import org.bukkit.NamespacedKey;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;

public record KeyedDamageType(
        NamespacedKey key
) implements DamageType {
    public static DamageType fromKey(String key) {
        return new KeyedDamageType(Objects.requireNonNull(NamespacedKey.fromString(key)));
    }

    @Override
    public @NotNull NamespacedKey getKey() {
        return key;
    }

    @Override
    public String toString() {
        return key.toString();
    }
}
