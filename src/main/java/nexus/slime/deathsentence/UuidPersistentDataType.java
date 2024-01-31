package nexus.slime.deathsentence;

import org.bukkit.persistence.PersistentDataAdapterContext;
import org.bukkit.persistence.PersistentDataType;
import org.jetbrains.annotations.NotNull;

import java.util.UUID;

public class UuidPersistentDataType implements PersistentDataType<int[], UUID> {
    public static final UuidPersistentDataType UUID_TYPE = new UuidPersistentDataType();

    @Override
    public @NotNull Class<int[]> getPrimitiveType() {
        return int[].class;
    }

    @Override
    public @NotNull Class<UUID> getComplexType() {
        return UUID.class;
    }

    @Override
    public int @NotNull [] toPrimitive(@NotNull UUID uuid, @NotNull PersistentDataAdapterContext persistentDataAdapterContext) {
        long msb = uuid.getMostSignificantBits();
        long lsb = uuid.getLeastSignificantBits();
        return new int[] { (int) (msb >> Integer.SIZE), (int) msb, (int) (lsb >> Integer.SIZE), (int) lsb };
    }

    @Override
    public @NotNull UUID fromPrimitive(int @NotNull [] array, @NotNull PersistentDataAdapterContext persistentDataAdapterContext) {
        if (array.length != 4) {
            throw new IllegalArgumentException("Could not UUID: Integer array must have exactly 4 elements!");
        }

        long msb = (long) array[0] << 32 | (long) array[1] & 0xFFFFFFFFL;
        long lsb = (long) array[2] << 32 | (long) array[3] & 0xFFFFFFFFL;
        return new UUID(msb, lsb);
    }
}
