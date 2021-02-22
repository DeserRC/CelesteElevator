package com.celeste.celesteelevator.util;

import org.bukkit.Bukkit;
import org.bukkit.Location;

import java.util.StringJoiner;

public class LocationUtil {

    public static String serialize(final Location location, final boolean yawAndPitch) {
        final StringJoiner joiner = new StringJoiner(":");

        joiner.add(location.getWorld().getName());
        joiner.add(String.valueOf(location.getX()));
        joiner.add(String.valueOf(location.getY()));
        joiner.add(String.valueOf(location.getZ()));

        if (yawAndPitch) {
            joiner.add(String.valueOf(location.getYaw()));
            joiner.add(String.valueOf(location.getPitch()));
        }

        return joiner.toString().substring(0, joiner.length() - 1);
    }

    public static Location deserialize(final String serializeLocation) {
        final String[] split = serializeLocation.split(":");

        if (split.length == 6) {
            return new Location(
              Bukkit.getWorld(split[0]),
              Double.parseDouble(split[1]),
              Double.parseDouble(split[2]),
              Double.parseDouble(split[3]),
              Float.parseFloat(split[4]),
              Float.parseFloat(split[5])
            );
        }

        if (split.length == 4) {
            return new Location(
              Bukkit.getWorld(split[0]),
              Double.parseDouble(split[1]),
              Double.parseDouble(split[2]),
              Double.parseDouble(split[3]));
        }

        return null;
    }

}
