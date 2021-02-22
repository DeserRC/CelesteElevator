package com.celeste.celesteelevator.util.impl;

import com.celeste.celesteelevator.CelesteElevator;
import com.celeste.celesteelevator.util.MessagesUtil;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.FileConfiguration;

import java.util.ArrayList;
import java.util.List;
import java.util.Map.Entry;
import java.util.stream.Collectors;

public class ChatUtil extends MessagesUtil {

    public ChatUtil(final CelesteElevator plugin) {
        super(plugin);
    }

    @Override @SafeVarargs
    public final void send(final CommandSender sender, final String path, final FileConfiguration file, final Entry<String, String>... entries) {
        final Object message = getMessage(path, file, entries);
        
        if (message == null) return;

        if (message instanceof List) {
            @SuppressWarnings("unchecked")
            final List<String> finalMessage = (List<String>) message;
            finalMessage.forEach(sender::sendMessage);
            return;
        }

        sender.sendMessage(message.toString());
    }

    @Override @SafeVarargs
    public final void sendAll(final String path, final FileConfiguration file, final Entry<String, String>... entries) {
        final Object message = getMessage(path, file, entries);

        if (message == null) return;

        if (message instanceof List) {
            @SuppressWarnings("unchecked")
            final List<String> finalMessage = (List<String>) message;
            finalMessage.forEach(Bukkit::broadcastMessage);
            return;
        }

        Bukkit.broadcastMessage(message.toString());
    }

    @SafeVarargs
    private final Object getMessage(final String path, final FileConfiguration file, final Entry<String, String>... entries) {
        Object message = path;

        if (file.contains(path)) {

            final boolean containsUse = file.contains(path + ".use");
            final boolean useChat = containsUse ? config.get(path + ".use", file) : false;

            if (!containsUse) message = config.get(path, file);
            else if (useChat) message = config.get(path + ".message", file);

            else return null;
        }

        if (message instanceof List) {
            @SuppressWarnings("unchecked")
            List<String> finalMessage = (List<String>) message;

            for (final Entry<String, String> entry : entries) {
                final String key = entry.getKey();
                final String value = entry.getValue();

                finalMessage = finalMessage.stream()
                  .map(msg -> msg.replace(key, value))
                  .collect(Collectors.toList());
            }
        }

        String finalMessage = message.toString();

        for (final Entry<String, String> entry : entries) {
            final String key = entry.getKey();
            final String value = entry.getValue();

            finalMessage = finalMessage.replace(key, value);
        }

        return finalMessage;
    }

}
