package nexus.slime.deathsentence.nms;

import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.stream.Collectors;

public class MethodSignature {
    public static MethodSignature ofPublic(Class<?> returnType, Class<?>... parameterTypes) {
        return new MethodSignature(returnType, parameterTypes, true);
    }

    public static MethodSignature ofPrivate(Class<?> returnType, Class<?>... parameterTypes) {
        return new MethodSignature(returnType, parameterTypes, false);
    }

    private final Class<?> returnType;
    private final Class<?>[] parameterTypes;
    private final boolean isPublic;

    private MethodSignature(Class<?> returnType, Class<?>[] parameterTypes, boolean isPublic) {
        this.returnType = returnType;
        this.parameterTypes = parameterTypes;
        this.isPublic = isPublic;
    }

    public boolean matchesMethod(Method method) {
        var methodParameterTypes = method.getParameterTypes();

        if (!method.getReturnType().equals(returnType)) return false;
        if (methodParameterTypes.length != parameterTypes.length) return false;

        for (int i = 0; i < methodParameterTypes.length; i++) {
            if (!methodParameterTypes[i].equals(parameterTypes[i])) {
                return false;
            }
        }

        return true;
    }

    public boolean isPublic() {
        return isPublic;
    }

    @Override
    public String toString() {
        String visibility = (isPublic ? "public" : "private");

        String parameterList = Arrays.stream(parameterTypes)
                .map(Class::getCanonicalName)
                .collect(Collectors.joining(", "));

        return visibility + " " + returnType.getCanonicalName() + " (" + parameterList + ")";
    }
}
