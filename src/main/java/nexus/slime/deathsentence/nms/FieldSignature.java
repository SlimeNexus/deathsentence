package nexus.slime.deathsentence.nms;

import java.lang.reflect.Field;

public class FieldSignature {
    public static FieldSignature ofPublic(Class<?> type) {
        return new FieldSignature(type, true);
    }

    public static FieldSignature ofPrivate(Class<?> type) {
        return new FieldSignature(type, false);
    }

    private final Class<?> type;
    private final boolean isPublic;

    private FieldSignature(Class<?> type, boolean isPublic) {
        this.type = type;
        this.isPublic = isPublic;
    }

    public boolean isPublic() {
        return isPublic;
    }

    public boolean matchesField(Field field) {
        return field.getType().equals(type);
    }

    @Override
    public String toString() {
        return super.toString();
    }
}
