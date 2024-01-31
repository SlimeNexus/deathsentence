package nexus.slime.deathsentence;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import nexus.slime.deathsentence.damage.DamageSources;
import nexus.slime.deathsentence.damage.DamageType;
import nexus.slime.deathsentence.message.DeathMessage;
import nexus.slime.deathsentence.message.EntityDeathMessagePool;
import nexus.slime.deathsentence.message.NaturalDeathMessagePool;
import nexus.slime.deathsentence.nms.Nms;
import nexus.slime.deathsentence.nms.NmsV1_20_4;
import org.bukkit.NamespacedKey;
import org.bukkit.Registry;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.EntityType;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;

public class Settings {
    public static void generateDebugSettings(Path path) throws IOException {
        var config = YamlConfiguration.loadConfiguration(path.toFile());

        // General settings
        config.set("prefix", "");
        config.set("cooldown_seconds", 0);
        config.set("fallback_message", "");

        // Natural deaths
        for (DamageType damageType : DamageSources.listDamageTypes()) {
            config.set("natural_death." + damageType.key().asMinimalString(), List.of("[NATURAL_DEATH] PLAYER: '%player%' - DAMAGE_TYPE: '" + damageType.key().asString() + "'"));
        }

        // Entity death & special item death
        for (EntityType entityType : Registry.ENTITY_TYPE) {
            var entityTypeKey = Objects.requireNonNull(Registry.ENTITY_TYPE.getKey(entityType));

            for (DamageType damageType : DamageSources.listDamageTypes()) {
                config.set("entity_death." + entityTypeKey.asMinimalString() + "." + damageType.key().asMinimalString(), List.of("[ENTITY_DEATH] PLAYER: '%player%' - ENTITY: '%attacker%' - ENTITY_TYPE: '" + entityTypeKey.asString() + "' - DAMAGE_TYPE: '" + damageType.key().asString() + "'"));
                config.set("special_item_death." + entityTypeKey.asMinimalString() + "." + damageType.key().asMinimalString(), List.of("[SPECIAL_ITEM_DEATH] PLAYER: '%player%' - ENTITY: '%attacker%' - ITEM: '%item%' - ENTITY_TYPE: '" + entityTypeKey.asString() + "' - DAMAGE_TYPE: '" + damageType.key().asString() + "'"));
            }
        }

        config.save(path.toFile());
    }

    public static Settings loadSettings(DeathSentencePlugin plugin) throws IOException {
        var dataFolder = plugin.getDataFolder().toPath();
        Path configFile = dataFolder.resolve("config.yml");

        if (!Files.isRegularFile(configFile)) {
            try (InputStream config = plugin.getResource("config.yml")) {
                if (config == null) {
                    throw new IOException("Default configuration was not found in jar!");
                }

                Files.createDirectories(dataFolder);
                Files.copy(config, configFile);
            }
        }

        FileConfiguration config = YamlConfiguration.loadConfiguration(configFile.toFile());

        return new Settings(config);
    }

    private static DeathMessage readMessage(Map<?, ?> map) throws IOException {
        // message
        var messageObject = map.get("message");

        if (messageObject == null) {
            throw new IOException("Death message object must at least contain one message!");
        }

        var message = MiniMessage.miniMessage().deserialize(messageObject.toString());

        // Weight
        double weight = 1;
        var weightObject = map.get("weight");

        if (weightObject instanceof Number) {
            weight = ((Number) weightObject).doubleValue();
        }

        // World
        String world = null;
        var worldObject = map.get("world");

        if (worldObject != null) {
            world = worldObject.toString();
        }

        // World Type
        String worldType = null;
        var worldTypeObject = map.get("world_type");

        if (worldTypeObject != null) {
            worldType = worldTypeObject.toString();
        }

        // Permission
        String permission = null;
        var permissionObject = map.get("permission");

        if (permissionObject != null) {
            permission = permissionObject.toString();
        }

        return new DeathMessage(message, weight, world, worldType, permission);
    }

    private static List<DeathMessage> readMessageList(List<?> list) throws IOException {
        if (list == null) {
            throw new IOException("List cannot be null!");
        }

        List<DeathMessage> messages = new ArrayList<>();

        for (Object object : list) {
            if (object instanceof Map<?, ?> map) {
                messages.add(readMessage(map));
            } else {
                var message = MiniMessage.miniMessage().deserialize(object.toString());
                messages.add(new DeathMessage(message));
            }
        }

        return messages;
    }

    private static Map<DamageType, List<DeathMessage>> readDamageMessageMap(ConfigurationSection section) throws IOException {
        Map<DamageType, List<DeathMessage>> byDamage = new HashMap<>();

        for (String key : section.getKeys(false)) {
            var innerList = section.getList(key);

            if (innerList == null) {
                continue;
            }

            DamageType type = DamageType.fromKey(key.toLowerCase());
            var messageList = readMessageList(innerList);

            byDamage.put(type, messageList);
        }

        return byDamage;
    }

    private static Map<EntityType, Map<DamageType, List<DeathMessage>>> readEntityDamageMessageMap(ConfigurationSection section) throws IOException {
        Map<EntityType, Map<DamageType, List<DeathMessage>>> byEntity = new HashMap<>();

        for (String key : section.getKeys(false)) {
            var innerSection = section.getConfigurationSection(key);

            if (innerSection == null) {
                continue;
            }

            EntityType type = key.equalsIgnoreCase("default")
                    ? EntityDeathMessagePool.CUSTOM_ENTITY_TYPE
                    : Registry.ENTITY_TYPE.get(Objects.requireNonNull(NamespacedKey.fromString(key.toLowerCase())));

            var byDamage = readDamageMessageMap(innerSection);

            byEntity.put(type, byDamage);
        }

        return byEntity;
    }

    private final Component prefix;
    private final int cooldownSeconds;
    private final Component fallbackMessage;

    private final NaturalDeathMessagePool naturalPool;
    private final EntityDeathMessagePool entityPool;
    private final EntityDeathMessagePool specialItemPool;

    private Settings(FileConfiguration config) throws IOException {
        // Prefix
        var prefixString = config.getString("prefix");

        if (prefixString == null) {
            throw new IOException("'prefix' is required in config!");
        }

        prefix = MiniMessage.miniMessage().deserialize(prefixString);

        // Cooldown
        cooldownSeconds = config.getInt("cooldown_seconds");

        // Fallback message
        var fallbackMessageString = config.getString("fallback_message");

        if (fallbackMessageString == null) {
            throw new IOException("'fallback_message' is required in config!");
        }

        fallbackMessage = MiniMessage.miniMessage().deserialize(fallbackMessageString);

        // Natural death messages
        var naturalSection = config.getConfigurationSection("natural_death");

        if (naturalSection == null) {
            throw new IOException("Could not find the natural_death section!");
        }

        naturalPool = new NaturalDeathMessagePool(readDamageMessageMap(naturalSection));

        // Entity death messages
        var entitySection = config.getConfigurationSection("entity_death");

        if (entitySection == null) {
            throw new IOException("Could not find the entity_death section!");
        }

        entityPool = new EntityDeathMessagePool(readEntityDamageMessageMap(entitySection));

        // Special item messages
        var specialItemSection = config.getConfigurationSection("special_item_death");

        if (specialItemSection == null) {
            throw new IOException("Could not find the special_item_death section!");
        }

        specialItemPool = new EntityDeathMessagePool(readEntityDamageMessageMap(specialItemSection));
    }

    public Component getPrefix() {
        return prefix;
    }

    public int getCooldownSeconds() {
        Nms nms = new NmsV1_20_4();
        return cooldownSeconds;
    }

    public Component getFallbackMessage() {
        return fallbackMessage;
    }

    public NaturalDeathMessagePool getNaturalPool() {
        return naturalPool;
    }

    public EntityDeathMessagePool getEntityPool() {
        return entityPool;
    }

    public EntityDeathMessagePool getSpecialItemPool() {
        return specialItemPool;
    }

}
