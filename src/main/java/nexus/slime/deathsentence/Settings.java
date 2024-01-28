package nexus.slime.deathsentence;

import net.kyori.adventure.text.minimessage.MiniMessage;
import nexus.slime.deathsentence.message.DeathMessage;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Settings {
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

    private static DeathMessage readDeathMessage(Map<?, ?> map) throws IOException {
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

    private static List<DeathMessage> readDeathMessageSet(List<?> list) throws IOException {
        if (list == null) {
            throw new IOException("List cannot be null!");
        }

        List<DeathMessage> messages = new ArrayList<>();

        for (Object object : list) {
            if (object instanceof Map<?, ?> map) {
                messages.add(readDeathMessage(map));
            } else {
                var message = MiniMessage.miniMessage().deserialize(object.toString());
                messages.add(new DeathMessage(message));
            }
        }

        return messages;
    }

    private final Map<String, List<DeathMessage>> naturalDeathMessages;
    private final Map<String, Map<String, List<DeathMessage>>> entityDeathMessages;

    private Settings(FileConfiguration config) throws IOException {
        // Natural death messages
        naturalDeathMessages = new HashMap<>();
        var naturalSection = config.getConfigurationSection("natural_death");

        if (naturalSection == null) {
            throw new IOException("Could not find the natural_death section!");
        }

        for (String damageTypeId : naturalSection.getKeys(false)) {
            var messages = readDeathMessageSet(naturalSection.getList(damageTypeId));
            naturalDeathMessages.put(damageTypeId, messages);
        }

        // Entity death messages
        entityDeathMessages = new HashMap<>();
        var entitySection = config.getConfigurationSection("entity_death");

        if (entitySection == null) {
            throw new IOException("Could not find the entity_death section!");
        }

        for (String entityTypeId : entitySection.getKeys(false)) {
            var damageSection = entitySection.getConfigurationSection(entityTypeId);
            Map<String, List<DeathMessage>> damageMap = new HashMap<>();

            if (damageSection == null) {
                throw new IOException("Could not find a section for entity_death." + entityTypeId);
            }

            for (String damageTypeId : damageSection.getKeys(false)) {
                var messages = readDeathMessageSet(damageSection.getList(damageTypeId));
                damageMap.put(damageTypeId, messages);
            }

            entityDeathMessages.put(entityTypeId, damageMap);
        }
    }

    public List<DeathMessage> getNaturalDeathMessage(String damageTypeId) {
        return naturalDeathMessages.get(damageTypeId);
    }

    public List<DeathMessage> getEntityDeathMessage(String entityTypeId, String damageTypeId) {
        var map = entityDeathMessages.get(entityTypeId);
        if (map == null) return null;
        return map.get(damageTypeId);
    }
}
