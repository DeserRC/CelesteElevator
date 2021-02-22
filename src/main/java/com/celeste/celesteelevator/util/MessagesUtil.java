package com.celeste.celesteelevator.util;

import com.celeste.celesteelevator.CelesteElevator;
import com.celeste.celesteelevator.manager.ConfigManager;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.FileConfiguration;

import java.util.AbstractMap;
import java.util.Map.Entry;

public abstract class MessagesUtil {

    protected final ConfigManager config;

    public MessagesUtil(final CelesteElevator plugin) {
        this.config = plugin.getConfigManager();
    }

    public static <T, U> Entry<String, String> build(final T key, final U value) {
        return new AbstractMap.SimpleEntry<>(key.toString(), value.toString());
    }

    @SafeVarargs
    public final void send(final CommandSender sender, final String path, final Entry<String, String>... entries) {
        send(sender, path, config.getMessage(), entries);
    }

    public abstract void send(final CommandSender sender, final String path, final FileConfiguration file, final Entry<String, String>... entries);

    @SafeVarargs
    public final void sendAll(final String path, final Entry<String, String>... entries) {
        sendAll(path, config.getMessage(), entries);
    }

    public abstract void sendAll(final String path, final FileConfiguration file, final Entry<String, String>... entries);

}