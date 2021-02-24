package com.celeste.celesteelevator.util.impl;

import com.celeste.celesteelevator.CelesteElevator;
import com.celeste.celesteelevator.util.MessagesUtil;
import lombok.SneakyThrows;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;

import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.util.Map.Entry;
import java.util.UUID;

import static com.celeste.celesteelevator.util.ReflectionUtil.*;

public class BarUtil extends MessagesUtil {

    private final Constructor<?> ppocCon;

    private final Method method;

    private final Object type;

    @SneakyThrows
    public BarUtil(final CelesteElevator plugin) {
        super(plugin);

        final Class<?> cmtClass;
        final Class<?> icbcClass = getNMS("IChatBaseComponent");
        final Class<?> ppocClass = getNMS("PacketPlayOutChat");

        if (isEqualsOrMoreRecent(12)) {
            cmtClass = getNMS("ChatMessageType");
            this.type = cmtClass.getEnumConstants() [2];
        } else {
            cmtClass = byte.class;
            this.type = (byte) 2;
        }

        if (icbcClass.getDeclaredClasses().length > 0) {
            this.method = icbcClass.getDeclaredClasses() [0].getMethod("a", String.class);
        } else this.method = getNMS("ChatSerializer").getMethod("a", String.class);

        if (isEqualsOrMoreRecent(16)) {
            this.ppocCon = ppocClass.getConstructor(icbcClass, cmtClass, UUID.class);
        } else this.ppocCon = ppocClass.getConstructor(icbcClass, cmtClass);
    }

    @Override @SneakyThrows @SafeVarargs
    public final void send(final CommandSender sender, final String path, final FileConfiguration file, final Entry<String, String>... entries) {
        if (!(sender instanceof Player)) return;

        final Player player = (Player) sender;
        final String message = getMessage(path, file, entries);

        if (message == null) return;

        if (isEqualsOrMoreRecent(16)) {
            final Object chatBase = method.invoke(null, "{\"text\":\"" + message + "\"}");
            final Object packet = ppocCon.newInstance(chatBase, type, player.getUniqueId());
            sendPacket(player, packet);
            return;
        }

        final Object chatBase = method.invoke(null, "{\"text\":\"" + message + "\"}");
        final Object packet = ppocCon.newInstance(chatBase, type);
        sendPacket(player, packet);
    }

    @Override @SneakyThrows @SafeVarargs
    public final void sendAll(final String path, final FileConfiguration file, final Entry<String, String>... entries) {
        final String message = getMessage(path, file, entries);

        if (message == null) return;

        if (isEqualsOrMoreRecent(16)) {
            final Object chatBase = method.invoke(null, "{\"text\":\"" + message + "\"}");

            for (final Player player : Bukkit.getOnlinePlayers()) {
                final Object packet = ppocCon.newInstance(chatBase, type, player.getUniqueId());
                sendPacket(player, packet);
            }

            return;
        }

        final Object chatBase = method.invoke(null, "{\"text\":\"" + message + "\"}");
        final Object packet = ppocCon.newInstance(chatBase, type);
        Bukkit.getOnlinePlayers().forEach(player -> sendPacket(player, packet));
    }

    @SafeVarargs
    private final String getMessage(final String path, final FileConfiguration file, final Entry<String, String>... entries) {
        String message = path;

        if (file.contains(path)) {

            final boolean containsuse = file.contains(path + ".use");
            final boolean useChat = containsuse ? config.get(path + ".use", file) : false;

            if (!containsuse) message = config.get(path, file);
            else if (useChat) message = config.get(path + ".message", file);

            else return null;
        }

        for (final Entry<String, String> entry : entries) {
            final String key = entry.getKey();
            final String value = entry.getValue();

            message = message.replace(key, value);
        }

        return message;
    }

}
