package com.celeste.celesteelevator.manager;

import com.celeste.celesteelevator.CelesteElevator;
import lombok.Getter;
import lombok.SneakyThrows;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

@Getter
public class ConfigManager {

    private final CelesteElevator plugin;

    private final FileConfiguration config;
    private final FileConfiguration message;

    public ConfigManager(final CelesteElevator plugin) {
        this.plugin = plugin;

        this.config = new YamlConfiguration();
        this.message = new YamlConfiguration();

        load();
    }

    public <T> T getConfig(final String path) {
        return get(path, config);
    }

    public <T> T getMessage(final String path) {
        return get(path, message);
    }

    @SuppressWarnings("unchecked")
    public <T> T get(final String path, final FileConfiguration file) {
        final T result = (T) file.get(path, "§cThere was an error loading the message: §e" + path);

        if (result instanceof String)
            return (T) result.toString().replace("&", "\u00A7");

        else if (result instanceof List && !((List<?>) result).isEmpty() && ((List<?>) result).get(0) instanceof String) {
            return (T) ((List<String>) result).stream()
                .map(line -> line.replace("&", "\u00A7"))
                .collect(Collectors.toList());
        }

        return result;
    }

    public Set<String> getKeysConfig(final String path) {
        return getKeys(path, config);
    }

    public Set<String> getKeysMessage(final String path) {
        return getKeys(path, message);
    }

    public Set<String> getKeys(final String path, final FileConfiguration file) {
        final ConfigurationSection config = file.getConfigurationSection(path);
        if (config == null) return new HashSet<>();

        final Optional<Set<String>> optional = Optional.of(config.getKeys(false));
        return optional.orElse(new HashSet<>());
    }

    @SneakyThrows
    public void load() {
        // config.yml
        final File fileConfig = new File(plugin.getDataFolder(), "config.yml");

        if (!fileConfig.exists()) plugin.saveResource("config.yml", false);

        config.load(fileConfig);

        // message.yml
        final File fileMessage = new File(plugin.getDataFolder(), "message.yml");

        if (!fileMessage.exists()) plugin.saveResource("message.yml", false);

        message.load(fileMessage);
    }

}
