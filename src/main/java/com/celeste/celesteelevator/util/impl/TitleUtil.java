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

import static com.celeste.celesteelevator.util.ReflectionUtil.getNMS;
import static com.celeste.celesteelevator.util.ReflectionUtil.sendPacket;

public class TitleUtil extends MessagesUtil {

    private final Constructor<?> ppotTimeCon;
    private final Constructor<?> ppotTextCon;

    private final Method method;

    private final Object time;
    private final Object typeTitle;
    private final Object typeSubTitle;

    @SneakyThrows
    public TitleUtil(final CelesteElevator plugin) {
        super(plugin);

        final Class<?> ppotClass = getNMS("PacketPlayOutTitle");
        final Class<?> icbcClass = getNMS("IChatBaseComponent");
        final Class<?> etaClass;

        if (ppotClass.getDeclaredClasses().length > 0) {
            etaClass = ppotClass.getDeclaredClasses()[0];
        } else etaClass = getNMS("EnumTitleAction");

        if (icbcClass.getDeclaredClasses().length > 0) {
            method = icbcClass.getDeclaredClasses()[0].getMethod("a", String.class);
        } else method = getNMS("ChatSerializer").getMethod("a", String.class);

        ppotTimeCon = ppotClass.getConstructor(etaClass, icbcClass, int.class, int.class, int.class);
        ppotTextCon = ppotClass.getConstructor(etaClass, icbcClass);

        time = etaClass.getField("TIMES").get(null);
        typeTitle = etaClass.getField("TITLE").get(null);
        typeSubTitle = etaClass.getField("SUBTITLE").get(null);
    }

    @Override @SneakyThrows @SafeVarargs
    public final void send(final CommandSender sender, final String path, final FileConfiguration file, final Entry<String, String>... entries) {
        if (!(sender instanceof Player)) return;

        final Player player = (Player) sender;
        final String[] titles = getMessage(path, file, entries);

        if (titles == null) return;

        final String title = titles[0];
        final String subtitle = titles[1];

        final Object timePacket = ppotTimeCon.newInstance(time, null, 10, 10, 10);
        sendPacket(player, timePacket);

        if (title != null && title != "") {
            final Object chatBase = method.invoke(null, "{\"text\":\"" + title + "\"}");
            final Object packet = ppotTextCon.newInstance(typeTitle, chatBase);
            sendPacket(player, packet);
        }

        if (subtitle != null && subtitle != "") {
            final Object chatBase = method.invoke(null,"{\"text\":\"" + subtitle + "\"}");
            final Object packet = ppotTextCon.newInstance(typeSubTitle, chatBase);
            sendPacket(player, packet);
        }

    }

    @Override @SneakyThrows @SafeVarargs
    public final void sendAll(final String path, final FileConfiguration file, final Entry<String, String>... entries) {
        final String[] titles = getMessage(path, file, entries);

        if (titles == null) return;

        final String title = titles[0];
        final String subtitle = titles[1];

        final Object timePacket = ppotTimeCon.newInstance(time, null, 10, 10, 10);
        Bukkit.getOnlinePlayers().forEach(player -> sendPacket(player, timePacket));

        if (title != null) {
            final Object chatBase = method.invoke(null, "{\"text\":\"" + title + "\"}");
            final Object packet = ppotTextCon.newInstance(typeTitle, chatBase);
            Bukkit.getOnlinePlayers().forEach(player -> sendPacket(player, packet));
        }

        if (subtitle != null) {
            final Object chatBase = method.invoke(null,"{\"text\":\"" + subtitle + "\"}");
            final Object packet = ppotTextCon.newInstance(typeSubTitle, chatBase);
            Bukkit.getOnlinePlayers().forEach(player -> sendPacket(player, packet));
        }
    }

    @SafeVarargs
    private final String[] getMessage(final String path, final FileConfiguration file, final Entry<String, String>... entries) {
        String title = path;
        String subtitle = path;

        if (file.contains(path)) {
            final boolean useTitle = config.get(path + ".use", file);

            if (!useTitle) return null;

            title = config.get(path + ".title", file);
            subtitle = config.get(path + ".subTitle", file);
        }

        for (final Entry<String, String> entry : entries) {
            final String key = entry.getKey();
            final String value = entry.getValue();

            title = title.replace(key, value);
            subtitle = subtitle.replace(key, value);
        }

        return new String[] { title, subtitle };
    }

}