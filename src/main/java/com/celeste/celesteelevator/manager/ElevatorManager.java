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

        final Block block = player.getLocation().getBlock();
        final Location blockLocation = block.getLocation().add(0.5, 0, 0.5);

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
            final Location location = blockLocation.clone().add(0, size - i / 10, 0);
            sendParticle(player, location, color);
        }

        final Location[] locations = getDirection(player, blockLocation, size);
        if (locations == null) return;

        for (int i = 0; i < 2; i++) {

            final Location location = blockLocation.clone();
            final Vector vector;

            if (direction.equals(DirectionType.UP)) {
                location.add(0, size, 0);
                vector = i == 0 ? locations[2].toVector() : locations[3].toVector();
            } else vector = i == 0 ? locations[0].toVector() : locations[1].toVector();

            location.setDirection(vector.subtract(location.toVector()));

            final Vector increase = location.getDirection().multiply(0.1);
            final double distance = direction == DirectionType.UP ? location.distance(locations[2]) : location.distance(locations[0]);

            for (double j = 0; j < distance / 2 * size; j += 0.1) {
                location.add(increase);
                sendParticle(player, location, color);
            }
        }
    }

    @SneakyThrows
    private void sendParticle(final Player player, final Location location, final int[] color) {
        final Color rgb = Color.fromRGB(color[0], color[1], color[2]);
        final Object dustOptions = instance(doCon, rgb, 1);

        location.getWorld().getNearbyEntities(location, 30, 30, 30).stream()
          .filter(target -> target instanceof Player)
          .forEach(target -> ((Player) target).spawnParticle(Particle.REDSTONE, location, 1, 0.0D, 0.0D, 0.0D, 0.0D, dustOptions));
    }

    private Location[] getDirection(final Player player, final Location location, final double size) {
        float yaw = (player.getLocation().getYaw() - 90) % 360;
        if (yaw < 0) yaw += 360;

        if (yaw > 44 && yaw < 136 || yaw > 224 && yaw < 316) {
            return new Location[] {
              location.clone().add(0.7, 1, 0),
              location.clone().add(- 0.7, 1, 0),
              location.clone().add(0.7, size - 1, 0),
              location.clone().add(- 0.7, size - 1, 0)
            };
        }

        if (yaw > 134 && yaw < 226 || yaw < 46 || yaw > 314) {
            return new Location[] {
              location.clone().add(0, 1, 0.7),
              location.clone().add(0, 1, - 0.7),
              location.clone().add(0, size - 1, 0.7),
              location.clone().add(0, size - 1, - 0.7)
            };
        }

        return null;
    }

}
