package com.celeste.celesteelevator.manager;

import com.celeste.celesteelevator.CelesteElevator;
import lombok.Getter;
import lombok.SneakyThrows;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.util.List;
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

        if (result instanceof String) {
            return (T) ((String) result).replace("&", "\u00A7");
        }

        if (result instanceof List) {
            return (T) ((List<String>) result).stream()
              .map(line -> line.replace("&", "\u00A7"))
              .collect(Collectors.toList());
        }

        return result;
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
