package com.celeste.celesteelevator.manager;

import com.celeste.celesteelevator.CelesteElevator;
import com.celeste.celesteelevator.entity.ElevatorEntity;
import com.celeste.celesteelevator.factory.ElevatorFactory;
import com.celeste.celesteelevator.type.DirectionType;
import com.celeste.celesteelevator.util.ItemBuilder;
import com.celeste.celesteelevator.util.LocationUtil;
import lombok.SneakyThrows;
import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.metadata.FixedMetadataValue;
import org.bukkit.util.Vector;

import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.util.List;
import java.util.UUID;

import static com.celeste.celesteelevator.util.ReflectionUtil.*;

public class ElevatorManager {
    
    private final CelesteElevator plugin;

    private final Constructor<?> doCon;

    private final Method asNMSCopy;
    private final Method getTag;
    private final Method hasKey;

    @SneakyThrows
    public ElevatorManager(final CelesteElevator plugin) {
        this.plugin = plugin;

        final Class<?> craftItemStackClazz = getOBC("inventory.CraftItemStack");
        final Class<?> itemStackClazz = getNMS("ItemStack");
        final Class<?> compoundClazz = getNMS("NBTTagCompound");

        this.doCon = Particle.REDSTONE.getDataType().getConstructor(Color.class, float.class);

        this.asNMSCopy = getMethod(craftItemStackClazz, "asNMSCopy", ItemStack.class);
        this.getTag = getMethod(itemStackClazz, "getTag");
        this.hasKey = getMethod(compoundClazz, "hasKey", String.class);
    }

    public void createElevator(final Block block) {
        final ElevatorFactory elevator = plugin.getElevatorFactory();
        final Location location = block.getLocation();

        final UUID id = UUID.randomUUID();
        final String serialize = LocationUtil.serialize(location);

        final ElevatorEntity entity = new ElevatorEntity(id, serialize);
        elevator.getAddElevatorRegistry().put(id, entity);

        registryMetadata(entity);
    }

    public void deleteElevator(final UUID id) {
        final ElevatorFactory elevator = plugin.getElevatorFactory();
        elevator.getRemoveElevatorRegistry().add(id);
    }

    public void registryMetadata(final ElevatorEntity entity) {
        final Block block = entity.getBlock();
        block.setMetadata("elevator", new FixedMetadataValue(plugin, entity.getId().toString()));
    }

    public boolean containsMetaData(final Block block) {
        if (block.isEmpty() || block.isLiquid()) return false;
        return block.hasMetadata("elevator");
    }

    public String getMetaData(final Block block) {
        return block.getMetadata("elevator").get(0).asString();
    }

    public ItemStack getElevator(final Material material, final int data, final int amount) {
        final ConfigManager config = plugin.getConfigManager();

        final String name = config.getConfig("elevator.name");
        final boolean glow = config.getConfig("elevator.glow");
        final List<String> lore = config.getConfig("elevator.lore");
        final List<String> enchantment = config.getConfig("elevator.enchantment");

        return new ItemBuilder(material, amount, data)
          .name(name)
          .glow(glow)
          .lore(lore)
          .enchantment(enchantment)
          .nbtTag("elevator", "")
          .build();
    }

    @SneakyThrows
    public boolean isElevator(final ItemStack item) {
        final Object nmsItem = invokeStatic(asNMSCopy, item);
        if (nmsItem == null) return false;

        final Object compound = invoke(getTag, nmsItem);
        if (compound == null) return false;

        return (boolean) invoke(hasKey, compound, "elevator");
    }

    public void sendSound(final Player player, final String path) {
        final ConfigManager config = plugin.getConfigManager();

        final boolean useSound = config.getConfig(path + "use");
        if (!useSound) return;

        final Sound sound = Sound.valueOf(config.getConfig(path + "type"));
        final double volume = config.getConfig(path + "volume");
        final double pitch = config.getConfig(path + "pitch");

        player.playSound(player.getLocation(), sound, (float) volume, (float) pitch);
    }

    public void sendParticle(final Player player, final DirectionType direction) {
        final ConfigManager config = plugin.getConfigManager();
        final Location location = player.getLocation();

        final String path = "particle." + direction.name().toLowerCase() + ".";

        final boolean useParticle = config.getConfig(path + "use");
        if (!useParticle) return;

        final double size = config.getConfig( path + "size");
        final int[] color = {
          config.getConfig(path + "color.red"),
          config.getConfig(path + "color.green"),
          config.getConfig(path + "color.blue")
        };

        for (double i = 0; i < size * 10; i++) {
            final Location particle = location.clone().add(0.5, size - i / 10, 0.5);
            sendParticle(particle, color);
        }

        final Location[] inclined = getDirection(location, size);
        if (inclined == null) return;

        for (int i = 0; i < 2; i++) {

            final Location start = location.clone();
            final Vector end;

            if (direction.equals(DirectionType.UP)) {

                start.add(0.5, size, 0.5);
                end = i == 0 ? inclined[2].toVector() : inclined[3].toVector();

            } else {

                start.add(0.5, 0, 0.5);
                end = i == 0 ? inclined[0].toVector() : inclined[1].toVector();

            }

            start.setDirection(end.subtract(start.toVector()));

            final Vector increase = start.getDirection().multiply(0.1D);
            final double distance = direction == DirectionType.UP ? start.distance(inclined[2]) : start.distance(inclined[0]);

            for (double j = 0; j < distance / 2 * size; j += 0.1) {
                final Location loc = start.add(increase);
                sendParticle(loc, color);
            }
        }
    }

    @SneakyThrows
    private void sendParticle(final Location location, final int[] color) {
        final Color rgb = Color.fromRGB(color[0], color[1], color[2]);
        final Object dustOptions = instance(doCon, rgb, 1);

        location.getWorld().getNearbyEntities(location, 30, 30, 30)
          .stream().filter(player -> player instanceof Player)
          .forEach(player -> ((Player) player).spawnParticle(Particle.REDSTONE, location, 1, 0.0D, 0.0D, 0.0D, 0.0D, dustOptions));
    }

    private Location[] getDirection(final Location location, final double size) {
        float yaw = (location.getYaw() - 90) % 360;

        if (yaw < 0) yaw = yaw + 360;

        if ((yaw >= 45.0F && yaw <= 135.0F) || (yaw >= 225.0F && yaw <= 315.0F)) {
            return new Location[] {
              new Location(location.getWorld(), location.getBlockX() + 0.5 + 0.7, location.getBlockY() + 1, location.getBlockZ() + 0.5),
              new Location(location.getWorld(), location.getBlockX() + 0.5 - 0.7, location.getBlockY() + 1, location.getBlockZ() + 0.5),
              new Location(location.getWorld(), location.getBlockX() + 0.5 + 0.7, location.getBlockY() + size - 1, location.getBlockZ() + 0.5),
              new Location(location.getWorld(), location.getBlockX() + 0.5 - 0.7, location.getBlockY() + size - 1, location.getBlockZ() + 0.5)
            };
        }

        if ((yaw >= 135 && yaw <= 225) || yaw >= 315 || yaw <= 45) {
            return new Location[] {
              new Location(location.getWorld(), location.getBlockX() + 0.5, location.getBlockY() + 1, location.getBlockZ() + 0.5 + 0.7),
              new Location(location.getWorld(), location.getBlockX() + 0.5, location.getBlockY() + 1, location.getBlockZ() + 0.5 - 0.7),
              new Location(location.getWorld(), location.getBlockX() + 0.5, location.getBlockY() + size - 1, location.getBlockZ() + 0.5 + 0.7),
              new Location(location.getWorld(), location.getBlockX() + 0.5, location.getBlockY() + size - 1, location.getBlockZ() + 0.5 - 0.7)
            };
        }

        return null;
    }

}
