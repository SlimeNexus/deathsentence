package nexus.slime.deathsentence.nms;

import java.lang.reflect.Field;
import java.lang.reflect.InaccessibleObjectException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.stream.Collectors;

@FunctionalInterface
public interface ReflectionOperation<R> {
    Object rawAccess(Object object) throws IllegalArgumentException, InvocationTargetException, IllegalAccessException;

    default R access(Object object) throws ReflectionException {
        try {
            //noinspection unchecked
            return (R) rawAccess(object);
        } catch (InvocationTargetException | IllegalAccessException e) {
            throw new ReflectionException("Could not perform reflective operation", e);
        }
    }

    static <R> ReflectionOperation<R> from(Builder<R> builder) throws ReflectionException {
        try {
            return builder.build();
        } catch (ClassNotFoundException e) {
            throw new ReflectionException("Could not find a class while preparing reflective operation", e);
        } catch (NoSuchMethodException e) {
            throw new ReflectionException("Could not find a method while preparing reflective operation", e);
        } catch (NoSuchFieldException e) {
            throw new ReflectionException("Could not find a field while preparing reflective operation", e);
        }
    }

    @FunctionalInterface
    interface Builder<R> {
        ReflectionOperation<R> build() throws ClassNotFoundException, NoSuchMethodException, NoSuchFieldException, ReflectionException;

        default Field findField(Class<?> clazz, FieldSignature signature) throws ReflectionException {
            var matchingFields = Arrays.stream(signature.isPublic() ? clazz.getFields() : clazz.getDeclaredFields())
                    .filter(signature::matchesField)
                    .toList();

            if (matchingFields.isEmpty()) {
                throw new ReflectionException("Failed to find field on " + clazz.getCanonicalName() + " by signature " + signature);
            }

            if (matchingFields.size() > 1) {
                String names = matchingFields.stream()
                        .map(Field::getName)
                        .collect(Collectors.joining(", "));

                throw new ReflectionException("Found ambiguous fields on " + clazz.getCanonicalName() + " for signature " + signature + " - matching methods: " + names);
            }

            var field = matchingFields.get(0);

            if (!signature.isPublic()) {
                try {
                    field.setAccessible(true);
                } catch (InaccessibleObjectException | SecurityException e) {
                    throw new ReflectionException("Could not make private field " + field + " accessible", e);
                }
            }

            return field;
        }

        default Method findMethod(Class<?> clazz, MethodSignature signature) throws ReflectionException {
            var matchingMethods = Arrays.stream(signature.isPublic() ? clazz.getMethods() : clazz.getDeclaredMethods())
                    .filter(signature::matchesMethod)
                    .toList();

            if (matchingMethods.isEmpty()) {
                throw new ReflectionException("Failed to find public method on " + clazz.getCanonicalName() + " by signature " + signature);
            }

            if (matchingMethods.size() > 1) {
                String names = matchingMethods.stream()
                        .map(Method::getName)
                        .collect(Collectors.joining(", "));

                throw new ReflectionException("Found ambiguous public methods on " + clazz.getCanonicalName() + " for signature " + signature + " - matching methods: " + names);
            }

            var method = matchingMethods.get(0);

            if (!signature.isPublic()) {
                try {
                    method.setAccessible(true);
                } catch (InaccessibleObjectException | SecurityException e) {
                    throw new ReflectionException("Could not make private method " + method + " accessible", e);
                }
            }

            return method;
        }
    }
}
