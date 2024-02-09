package nexus.slime.deathsentence;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import nexus.slime.deathsentence.damage.DamageType;
import nexus.slime.deathsentence.message.DeathMessage;
import nexus.slime.deathsentence.message.EntityDeathMessagePool;
import nexus.slime.deathsentence.message.NaturalDeathMessagePool;
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
    public static void generateDebugSettings(Registry<DamageType> damageTypeRegistry, Path path) throws IOException {
        var config = YamlConfiguration.loadConfiguration(path.toFile());

        // General settings
        config.set("message_template", "%message%");
        config.set("cooldown_seconds", 0);
        config.set("log_deaths", true);

        // Natural deaths
        for (DamageType damageType : damageTypeRegistry) {
            config.set("natural_death." + damageType.key().asMinimalString(), List.of("[NATURAL_DEATH] PLAYER: '%player%' - DAMAGE_TYPE: '" + damageType.key().asString() + "'"));
        }

        // Entity death & special item death
        for (EntityType entityType : Registry.ENTITY_TYPE) {
            var entityTypeKey = Objects.requireNonNull(Registry.ENTITY_TYPE.getKey(entityType));

            for (DamageType damageType : damageTypeRegistry) {
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

        return new Settings(plugin, config);
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

    private static Map<DamageType, List<DeathMessage>> readDamageMessageMap(DeathSentencePlugin plugin, ConfigurationSection section) throws IOException {
        Map<DamageType, List<DeathMessage>> byDamage = new HashMap<>();

        for (String key : section.getKeys(false)) {
            var innerList = section.getList(key);

            if (innerList == null) {
                continue;
            }

            var messageList = readMessageList(innerList);
            var type = plugin.getCombatTracker().getDamageType(NamespacedKey.fromString(key.toLowerCase()));

            if (type == null) {
                plugin.getLogger().warning("Unknown damage type " + key + " found in config! Ignoring...");
                continue;
            }

            byDamage.put(type, messageList);
        }

        return byDamage;
    }

    private static Map<EntityType, Map<DamageType, List<DeathMessage>>> readEntityDamageMessageMap(DeathSentencePlugin plugin, ConfigurationSection section) throws IOException {
        Map<EntityType, Map<DamageType, List<DeathMessage>>> byEntity = new HashMap<>();

        for (String key : section.getKeys(false)) {
            var innerSection = section.getConfigurationSection(key);

            if (innerSection == null) {
                continue;
            }

            EntityType type = key.equalsIgnoreCase("default")
                    ? EntityDeathMessagePool.CUSTOM_ENTITY_TYPE
                    : Registry.ENTITY_TYPE.get(Objects.requireNonNull(NamespacedKey.fromString(key.toLowerCase())));

            var byDamage = readDamageMessageMap(plugin, innerSection);

            byEntity.put(type, byDamage);
        }

        return byEntity;
    }

    private final Component messageTemplate;
    private final int cooldownSeconds;
    private final boolean logDeaths;

    private final NaturalDeathMessagePool naturalPool;
    private final EntityDeathMessagePool entityPool;
    private final EntityDeathMessagePool specialItemPool;

    private Settings(DeathSentencePlugin plugin, FileConfiguration config) throws IOException {
        // Prefix
        var messageTemplateString = config.getString("message_template");

        if (messageTemplateString == null) {
            throw new IOException("'message_template' is required in config!");
        }

        messageTemplate = MiniMessage.miniMessage().deserialize(messageTemplateString);

        // Cooldown
        cooldownSeconds = config.getInt("cooldown_seconds");

        // Log deaths
        logDeaths = config.getBoolean("log_deaths");

        // Natural death messages
        var naturalSection = config.getConfigurationSection("natural_death");

        if (naturalSection == null) {
            throw new IOException("Could not find the natural_death section!");
        }

        naturalPool = new NaturalDeathMessagePool(readDamageMessageMap(plugin, naturalSection));

        // Entity death messages
        var entitySection = config.getConfigurationSection("entity_death");

        if (entitySection == null) {
            throw new IOException("Could not find the entity_death section!");
        }

        entityPool = new EntityDeathMessagePool(readEntityDamageMessageMap(plugin, entitySection));

        // Special item messages
        var specialItemSection = config.getConfigurationSection("special_item_death");

        if (specialItemSection == null) {
            throw new IOException("Could not find the special_item_death section!");
        }

        specialItemPool = new EntityDeathMessagePool(readEntityDamageMessageMap(plugin, specialItemSection));
    }

    public Component getMessageTemplate() {
        return messageTemplate;
    }

    public int getCooldownSeconds() {
        return cooldownSeconds;
    }

    public boolean isLogDeaths() {
        return logDeaths;
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
