package nexus.slime.deathsentence.damage;

import org.bukkit.NamespacedKey;
import org.bukkit.Registry;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

public class DamageTypeRegistry implements Registry<DamageType> {
    public static DamageTypeRegistry fromRawTypes(List<NamespacedKey> rawTypes) {
        Map<NamespacedKey, DamageType> types = new HashMap<>();

        for (NamespacedKey rawType : rawTypes) {
            types.put(rawType, new KeyedDamageType(rawType));
        }

        return new DamageTypeRegistry(types);
    }

    private final Map<NamespacedKey, DamageType> types;

    private DamageTypeRegistry(Map<NamespacedKey, DamageType> types) {
        this.types = types;
    }

    @Override
    public @Nullable DamageType get(@NotNull NamespacedKey key) {
        return types.get(key);
    }

    @Override
    public @NotNull Stream<DamageType> stream() {
        return types.values().stream();
    }

    @NotNull
    @Override
    public Iterator<DamageType> iterator() {
        return types.values().iterator();
    }
}
