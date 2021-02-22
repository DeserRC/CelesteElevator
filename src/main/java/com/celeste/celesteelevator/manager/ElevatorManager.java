package com.celeste.celesteelevator.manager;

import com.celeste.celesteelevator.CelesteElevator;
import com.celeste.celesteelevator.entity.ElevatorEntity;
import com.celeste.celesteelevator.factory.ElevatorFactory;
import com.celeste.celesteelevator.util.ItemBuilder;
import com.celeste.celesteelevator.util.LocationUtil;
import lombok.SneakyThrows;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.ShapedRecipe;
import org.bukkit.metadata.FixedMetadataValue;

import java.lang.reflect.Method;
import java.util.List;
import java.util.UUID;

import static com.celeste.celesteelevator.util.ReflectionUtil.*;

public class ElevatorManager {
    
    private final CelesteElevator plugin;

    private final Method asNMSCopy;
    private final Method getTag;
    private final Method hasKey;

    @SneakyThrows
    public ElevatorManager(final CelesteElevator plugin) {
        this.plugin = plugin;

        final Class<?> craftItemStackClazz = getOBC("inventory.CraftItemStack");
        final Class<?> itemStackClazz = getNMS("ItemStack");
        final Class<?> compoundClazz = getNMS("NBTTagCompound");

        this.asNMSCopy = getMethod(craftItemStackClazz, "asNMSCopy", ItemStack.class);
        this.getTag = getMethod(itemStackClazz, "getTag");
        this.hasKey = getMethod(compoundClazz, "hasKey", String.class);
    }

    public void createElevator(final Block block) {
        final ElevatorFactory elevator = plugin.getElevatorFactory();
        final Location location = block.getLocation();

        final UUID id = UUID.randomUUID();
        final String serialize = LocationUtil.serialize(location, false);

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

    public ItemStack getElevator(final Material material, final int amount) {
        final ConfigManager config = plugin.getConfigManager();

        final String name = config.getConfig("elevator.name");
        final boolean glow = config.getConfig("elevator.glow");
        final List<String> lore = config.getConfig("elevator.lore");
        final List<String> enchantment = config.getConfig("elevator.enchantment");

        return new ItemBuilder(material, amount)
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

}
