package nexus.slime.deathsentence.nms;

import org.bukkit.Bukkit;

public class NMS {
    public static ReflectionOperation<String> damageType() throws ReflectionException {
        return ReflectionOperation.from(new ReflectionOperation.Builder<>() {
            @Override
            public ReflectionOperation<String> build() throws ClassNotFoundException, ReflectionException, NoSuchMethodException {
                var craftPlayerType = Class.forName(Bukkit.getServer().getClass().getPackage().getName() + ".entity.CraftLivingEntity");
                var entityLivingType = Class.forName("net.minecraft.world.entity.EntityLiving");
                var damageSourceType = Class.forName("net.minecraft.world.damagesource.DamageSource");
                var damageTypeType = Class.forName("net.minecraft.world.damagesource.DamageType");
                var builtInRegistriesType = Class.forName("net.minecraft.core.registries.BuiltInRegistries");
                var iRegistryWritableType = Class.forName("net.minecraft.core.IRegistryWritable");

                var getHandle = findMethod(craftPlayerType, MethodSignature.ofPublic(entityLivingType));
                var damageSourceField = findField(entityLivingType, FieldSignature.ofPrivate(damageSourceType));
                var damageTypeMethod = findMethod(damageSourceType, MethodSignature.ofPublic(damageTypeType));

                var rootRegistryField = findField(builtInRegistriesType, FieldSignature.ofPrivate(builtInRegistriesType));

                var nameMethod = damageTypeType.getMethod("a");

                return entity -> {
                    var livingEntity = getHandle.invoke(entity);
                    var damageSource = damageSourceField.get(livingEntity);
                    var damageType = damageTypeMethod.invoke(damageSource);
                    return nameMethod.invoke(damageType);
                };
            }
        });
    }
}
