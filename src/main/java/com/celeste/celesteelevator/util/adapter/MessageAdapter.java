package com.celeste.celesteelevator.util.adapter;

import com.celeste.celesteelevator.CelesteElevator;
import com.celeste.celesteelevator.factory.MessageFactory;
import com.celeste.celesteelevator.manager.ConfigManager;
import com.celeste.celesteelevator.util.impl.BarUtil;
import com.celeste.celesteelevator.util.impl.ChatUtil;
import com.celeste.celesteelevator.util.impl.TitleUtil;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.FileConfiguration;

import java.util.Map.Entry;

public class MessageAdapter {

    private final ConfigManager config;
    private final ChatUtil chat;
    private final BarUtil bar;
    private final TitleUtil title;

    public MessageAdapter(final CelesteElevator plugin, final MessageFactory message) {
        this.config = plugin.getConfigManager();
        this.chat = message.getChatUtil();
        this.bar = message.getBarUtil();
        this.title = message.getTitleUtil();
    }

    @SafeVarargs
    public final void adaptAndSendToSender(final CommandSender sender, final String path, final Entry<String, String>... entries) {
        adaptAndSendToSender(sender, path, config.getMessage(), entries);
    }

    @SafeVarargs
    public final void adaptAndSendToSender(final CommandSender sender, final String path, final FileConfiguration file, final Entry<String, String>... entries) {
        chat.send(sender, path, file, entries);
        bar.send(sender, path + "_bar", file, entries);
        title.send(sender, path + "_title", file, entries);
    }

    @SafeVarargs
    public final void adaptAndSendAll(final String path, final Entry<String, String>... entries) {
        adaptAndSendAll(path, config.getMessage(), entries);
    }

    @SafeVarargs
    public final void adaptAndSendAll(final String path, final FileConfiguration file, final Entry<String, String>... entries) {
        chat.sendAll(path, file, entries);
        bar.sendAll(path + "_bar", file, entries);
        title.sendAll(path + "_title", file, entries);
    }

}
