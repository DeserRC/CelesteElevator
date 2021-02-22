package com.celeste.celesteelevator.util;

import lombok.SneakyThrows;
import org.bukkit.Color;
import org.bukkit.DyeColor;
import org.bukkit.Material;
import org.bukkit.block.BlockState;
import org.bukkit.block.CreatureSpawner;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.EntityType;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.*;
import org.bukkit.potion.Potion;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.*;
import java.util.Map.Entry;
import java.util.regex.Pattern;

import static com.celeste.celesteelevator.util.ReflectionUtil.*;
import static com.celeste.celesteelevator.type.EnchantType.getEnchantment;
import static org.bukkit.potion.PotionEffectType.getById;
import static org.bukkit.potion.PotionEffectType.getByName;

public class ItemBuilder {

    private final ItemStack itemStack;
    private ItemMeta meta;

    public ItemBuilder(final Material material) {
        this.itemStack = new ItemStack(material);
        this.meta = itemStack.getItemMeta();
    }

    public ItemBuilder(final Material material, final int amount) {
        this.itemStack = new ItemStack(material, amount);
        this.meta = itemStack.getItemMeta();
    }

    public ItemBuilder(final Material material, final int amount, final int data) {
        this.itemStack = new ItemStack(material, amount, (short) data);
        this.meta = itemStack.getItemMeta();
    }

    public ItemBuilder(final ItemStack itemStack) {
        this.itemStack = itemStack;
        this.meta = itemStack.getItemMeta();
    }

    public ItemBuilder material(final Material material) {
        itemStack.setType(material);
        return this;
    }

    public ItemBuilder data(final int data) {
        itemStack.setDurability((short) data);
        return this;
    }

    public ItemBuilder amount(final int amount) {
        itemStack.setAmount(amount);
        return this;
    }

    public ItemBuilder name(final String name) {
        if (isValid(name)) meta.setDisplayName(name);
        return this;
    }

    public ItemBuilder lore(final String... lore) {
        if (isValid(lore)) meta.setLore(Arrays.asList(lore));
        return this;
    }

    public ItemBuilder lore(final List<String> lore) {
        if (isValid(lore)) meta.setLore(lore);
        return this;
    }

    public ItemBuilder addLore(final String... lore) {
        if (!isValid(lore)) return this;

        final List<String> newLore = new ArrayList<>();
        newLore.addAll(meta.getLore());
        newLore.addAll(Arrays.asList(lore));

        meta.setLore(newLore);
        return this;
    }

    public ItemBuilder addLore(final List<String> lore) {
        if (!isValid(lore)) return this;

        final List<String> newLore = new ArrayList<>();
        newLore.addAll(meta.getLore());
        newLore.addAll(lore);

        meta.setLore(newLore);
        return this;
    }

    public ItemBuilder removeLore(final String... lore) {
        if (!isValid(lore)) return this;

        final List<String> newLore = meta.getLore();

        if (!isValid(newLore)) return this;

        newLore.removeAll(Arrays.asList(lore));
        meta.setLore(newLore);
        return this;
    }

    public ItemBuilder removeLore(final List<String> lore) {
        if (!isValid(lore)) return this;

        final List<String> newLore = meta.getLore();

        if (!isValid(newLore)) return this;

        newLore.removeAll(lore);
        meta.setLore(newLore);
        return this;
    }

    public ItemBuilder removeLoreLine(final int index) {
        if (index < 0) return this;

        final List<String> newLore = meta.getLore();

        if (!isValid(newLore)) return this;

        newLore.remove(index);
        meta.setLore(newLore);
        return this;
    }

    public ItemBuilder replaceLore(final String lore, final int index) {
        if (!isValid(lore) || index < 0) return this;

        final List<String> newLore = new ArrayList<>();
        newLore.addAll(meta.getLore());

        if (index >= newLore.size()) {
            for (int i = newLore.size(); i <= index; i++) {
                newLore.add("§c");
            }
        }

        newLore.set(index, lore);
        meta.setLore(newLore);
        return this;
    }

    public ItemBuilder enchantment(final String... enchantment) {
        if (!isValid(enchantment)) return this;

        Arrays.stream(enchantment).forEach(en -> {
            final String[] split = en.split(":");

            if (split.length != 2) return;

            final Enchantment enchant = getEnchantment(split[0]);
            final int level = Integer.parseInt(split[1]);

            if (isValid(enchant)) itemStack.addUnsafeEnchantment(enchant, level);
        });

        return this;
    }

    public ItemBuilder enchantment(final List<String> enchantment) {
        if (!isValid(enchantment)) return this;

        enchantment.forEach(en -> {
            final String[] split = en.split(":");

            if (split.length != 2) return;

            final Enchantment enchant = getEnchantment(split[0]);
            final int level = Integer.parseInt(split[1]);

            if (isValid(enchant)) itemStack.addUnsafeEnchantment(enchant, level);
        });

        return this;
    }

    public ItemBuilder enchantment(final String enchantment, final int level) {
        if (!isValid(enchantment)) return this;

        final Enchantment enchant = getEnchantment(enchantment);

        if (isValid(enchant)) itemStack.addUnsafeEnchantment(enchant, level);

        return this;
    }

    @SafeVarargs
    public final ItemBuilder enchantment(final Entry<String, Integer>... enchantment) {
        if (!isValid(enchantment)) return this;

        Arrays.stream(enchantment).forEach(en -> {
            final Enchantment enchant = getEnchantment(en.getKey());
            final int level = en.getValue();

            if (isValid(enchant)) itemStack.addUnsafeEnchantment(enchant, level);
        });

        return this;
    }

    public ItemBuilder enchantment(final Map<String, Integer> enchantment) {
        if (!isValid(enchantment)) return this;

        enchantment.keySet().forEach(en -> {
            final Enchantment enchant = getEnchantment(en);
            final int level = enchantment.get(en);

            if (isValid(enchant)) itemStack.addUnsafeEnchantment(enchant, level);
        });

        return this;
    }

    public ItemBuilder removeEnchantment(final String... enchantments) {
        Arrays.stream(enchantments).forEach(en -> {
            final Enchantment enchant = getEnchantment(en);

            if (isValid(enchant)) itemStack.removeEnchantment(enchant);
        });

        return this;
    }

    public ItemBuilder durability(final short durability) {
        itemStack.setDurability(durability);
        return this;
    }

    public ItemBuilder addDurability(final short durability) {
        itemStack.setDurability((short) (itemStack.getDurability() + durability));
        return this;
    }

    public ItemBuilder infinity() {
        itemStack.setDurability(Short.MAX_VALUE);
        meta.addItemFlags(ItemFlag.HIDE_DESTROYS);
        return this;
    }

    public ItemBuilder glow(final boolean glow) {
        if (glow) {
            itemStack.addUnsafeEnchantment(Enchantment.DURABILITY, 1);
            meta.addItemFlags(ItemFlag.HIDE_ENCHANTS);
        }

        if (!itemStack.containsEnchantment(Enchantment.DURABILITY)) return this;

        itemStack.removeEnchantment(Enchantment.DURABILITY);
        meta.removeItemFlags(ItemFlag.HIDE_ENCHANTS);
        return this;
    }

    @SneakyThrows
    public ItemBuilder skull(String texture, final UUID uuid) {
        if (itemStack.getType() != Material.PLAYER_HEAD) return this;

        texture = "http://textures.minecraft.net/texture/" + texture;
        final SkullMeta skullMeta = (SkullMeta) meta;
        final Class<?> profileClass = getClazz("com.mojang.authlib.", "GameProfile");
        final Class<?> propertyClass = getClazz("com.mojang.authlib.properties.", "Property");

        final Constructor<?> profileCon = getCon(profileClass, UUID.class, String.class);
        final Constructor<?> propertyCon = getCon(propertyClass, String.class, String.class);
        final Field propertiesField = getDcField(profileClass, "properties");

        final String encoded = Base64.getEncoder().encodeToString(String.format("{textures:{SKIN:{url:\"%s\"}}}", new Object[] { texture }).getBytes());
        final Object profile = instance(profileCon, uuid, null);
        final Object property = instance(propertyCon, "textures", encoded);

        final Class<?> propertiesClass = getType(propertiesField);

        final Method put = getMethod(propertiesClass,"put", Object.class, Object.class);
        invoke(put, propertiesField.get(profile),"textures", property);

        final Field profileField = getDcField(meta.getClass(), "profile");
        profileField.set(skullMeta, profile);
        return this;
    }

    public ItemBuilder skullOwner(final String owner) {
        final SkullMeta skullMeta = (SkullMeta) meta;
        skullMeta.setOwner(owner);
        return this;
    }

    public ItemBuilder mob(final EntityType type) {
        if (itemStack.getType() != Material.SPAWNER) return this;

        final BlockState state = ((BlockStateMeta) meta).getBlockState();
        ((CreatureSpawner) state).setSpawnedType(type);
        ((BlockStateMeta) meta).setBlockState(state);

        return this;
    }

    @Deprecated
    public ItemBuilder dye(final DyeColor color) {
        itemStack.setDurability(color.getDyeData());
        return this;
    }

    @Deprecated
    public ItemBuilder wool(final DyeColor color) {
        if (itemStack.getType().equals(Material.BLACK_WOOL)) itemStack.setDurability(color.getWoolData());
        return this;
    }

    public ItemBuilder armor(final Color color) {
        final LeatherArmorMeta armorMeta = (LeatherArmorMeta) meta;
        armorMeta.setColor(color);
        return this;
    }

    public ItemBuilder potion(final List<String> potions) {
        if (itemStack.getType() != Material.POTION) return this;

        potions.forEach(pt -> {
            final String[] split = pt.split(":");

            if (split.length < 3) return;

            final String potionName = split[0];
            final int duration = Integer.parseInt(split[1]);
            final int amplifier = Integer.parseInt(split[2]);

            final PotionMeta potionMeta = (PotionMeta) meta;
            PotionEffectType type = getByName(potionName);

            if (!isValid(type) && Pattern.matches("[0-9]+", potionName)) {
                type = getById(Integer.parseInt(potionName));
            }

            if (type == null) return;

            final PotionEffect effect = type.createEffect(duration * 20, amplifier);
            potionMeta.addCustomEffect(effect, true);

            final Potion potion = Potion.fromItemStack(itemStack);
            potion.setSplash(potion.isSplash());
            potion.apply(itemStack);
        });

        return this;
    }

    public ItemBuilder potion(final String potionName, final int duration, final int amplifier) {
        if (itemStack.getType() != Material.POTION) return this;

        final PotionMeta potionMeta = (PotionMeta) meta;
        PotionEffectType type = getByName(potionName);

        if (!isValid(type) && Pattern.matches("[0-9]+", potionName)) {
            type = getById(Integer.parseInt(potionName));
        }

        if (type == null) return this;

        final PotionEffect effect = type.createEffect(duration * 20, amplifier);
        potionMeta.addCustomEffect(effect, true);

        final Potion potion = Potion.fromItemStack(itemStack);
        potion.setSplash(potion.isSplash());
        potion.apply(itemStack);

        return this;
    }

    public ItemBuilder removePotion(final String potionName) {
        if (itemStack.getType() != Material.POTION) return this;

        final PotionMeta potionMeta = (PotionMeta) meta;
        PotionEffectType type = getByName(potionName);

        if (!isValid(type) && Pattern.matches("[0-9]+", potionName)) {
            type = getById(Integer.parseInt(potionName));
        }

        if (type == null) return this;

        potionMeta.removeCustomEffect(type);

        final Potion potion = Potion.fromItemStack(itemStack);
        potion.setSplash(potion.isSplash());
        potion.apply(itemStack);

        return this;
    }

    public ItemBuilder clearPotion() {
        if (itemStack.getType() != Material.POTION) return this;

        final PotionMeta potionMeta = (PotionMeta) meta;
        potionMeta.clearCustomEffects();

        final Potion potion = Potion.fromItemStack(itemStack);
        potion.setSplash(potion.isSplash());
        potion.apply(itemStack);

        return this;
    }

    @SneakyThrows
    public <T> ItemBuilder nbtTag(final T key, final T value) {
        final Class<?> craftItemStackClazz = getOBC("inventory.CraftItemStack");
        final Class<?> itemStackClazz = getNMS("ItemStack");
        final Class<?> compoundClazz = getNMS("NBTTagCompound");

        final Method asNMSCopy = getMethod(craftItemStackClazz, "asNMSCopy", ItemStack.class);
        final Method hasTag = getMethod(itemStackClazz, "hasTag");
        final Method getTag = getMethod(itemStackClazz, "getTag");

        final Object nmsItem = invokeStatic(asNMSCopy, itemStack);
        final boolean isExist = (Boolean) invoke(hasTag, nmsItem);
        final Object compound = isExist ? invoke(getTag, nmsItem) : compoundClazz.newInstance();

        final Class<?> tagClazz = getNMS("NBTTagString");
        final Class<?> baseClazz = getNMS("NBTBase");

        final Constructor<?> tagCon = getCon(tagClazz, String.class);

        final Method set = getMethod(compoundClazz, "set", String.class, baseClazz);
        final Method setTag = getMethod(itemStackClazz, "setTag", compoundClazz);
        final Method getItemMeta = getMethod(craftItemStackClazz, "getItemMeta", itemStackClazz);

        final Object tag = instance(tagCon, value.toString());
        invoke(set, compound, key.toString(), tag);
        invoke(setTag, nmsItem, compound);

        meta = (ItemMeta) invokeStatic(getItemMeta, nmsItem);

        return this;
    }

    public ItemBuilder itemFlag(final ItemFlag... flag) {
        meta.addItemFlags(flag);
        return this;
    }

    public ItemBuilder removeItemFlag(final ItemFlag... flag) {
        meta.removeItemFlags(flag);
        return this;
    }

    public ItemStack build() {
        itemStack.setItemMeta(meta);
        return itemStack;
    }

    private boolean isValid(final Object argument) {
        if (argument instanceof Object[]) return ((Object[]) argument).length != 0;

        if (argument instanceof List) return ((List<?>) argument).size() != 0;

        return argument != null && !argument.equals("");
    }

}