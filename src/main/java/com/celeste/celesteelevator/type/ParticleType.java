package com.celeste.celesteelevator.type;

import lombok.Getter;
import lombok.SneakyThrows;

import java.util.Arrays;
import java.util.List;

import static com.celeste.celesteelevator.util.ReflectionUtil.getField;
import static com.celeste.celesteelevator.util.ReflectionUtil.getNMS;
import static com.google.common.collect.ImmutableList.copyOf;

@Getter
public enum ParticleType {
    BARRIER("BARRIER"),
    BLOCK_CRACK("BLOCK_CRACK", "BLOCKCRACK"),
    BLOCK_DUST("BLOCK_DUST", "BLOCKDUST"),
    CLOUD("CLOUD"),
    CRIT("CRIT"),
    CRIT_MAGIC("CRIT_MAGIC", "MAGICCRIT"),
    DRIP_LAVA("DRIP_LAVA", "DRIPLAVA"),
    DRIP_WATER("DRIP_WATER", "DRIPWATER"),
    ENCHANTMENT_TABLE("ENCHANTMENT_TABLE", "ENCHANTMENTTABLE"),
    EXPLOSION_HUGE("EXPLOSION_HUGE", "HUGEEEXPLOSION"),
    EXPLOSION_LARGE("EXPLOSION_LARGE", "LARGEEXPLODE"),
    EXPLOSION_NORMAL("EXPLOSION_NORMAL", "EXPLODE"),
    FIREWORKS_SPARK("FIREWORKS_SPARK", "FIREWORKSSPARK"),
    FLAME("FLAME"),
    FOOTSTEP("FOOTSTEP"),
    HEART("HEART"),
    ITEM_CRACK("ITEM_CRACK", "ICONCRACK"),
    ITEM_TAKE("ITEM_TAKE", "TAKE"),
    LAVA("LAVA"),
    MOB_APPEARANCE("MOB_APPEARANCE", "MOBAPPEARANCE"),
    NOTE("NOTE"),
    PORTAL("PORTAL"),
    REDSTONE("REDSTONE", "REDDUST"),
    SLIME("SLIME", "SLIME"),
    SMOKE_LARGE("SMOKE_LARGE", "LARGESMOKE"),
    SMOKE_NORMAL("SMOKE_NORMAL", "SMOKE"),
    SNOWBALL("SNOWBALL", "SNOWBALLPOOF"),
    SNOW_SHOVEL("SNOW_SHOVEL", "SNOWSHOVEL"),
    SPELL("SPELL"),
    SPELL_INSTANT("SPELL_INSTANT", "INSTANTSPELL"),
    SPELL_MOB("SPELL_MOB", "MOBSPELL"),
    SPELL_MOB_AMBIENT("SPELL_MOB_AMBIENT", "MOBSPELLAMBIENT"),
    SPELL_WITCH("SPELL_WITCH", "WITCHMAGIC"),
    SUSPENDED("SUSPENDED"),
    SUSPENDED_DEPTH("SUSPENDED_DEPTH", "DEPTHSUSPEND"),
    TOWN_AURA("TOWN_AURA", "TOWNAURA"),
    VILLAGER_ANGRY("VILLAGER_ANGRY", "ANGRYVILLAGER"),
    VILLAGER_HAPPY("VILLAGER_HAPPY", "HAPPYVILLAGER"),
    WATER_BUBBLE("WATER_BUBBLE", "BUBBLE"),
    WATER_DROP("WATER_DROP", "DROPLET"),
    WATER_SPLASH("WATER_SPLASH", "SPLASH"),
    WATER_WAKE("WATER_WAKE", "WAKE");

    private final List<String> names;
    private final Class<?> epClass;

    @SneakyThrows
    ParticleType(final String... names) {
        this.names = copyOf(names);
        this.epClass = getNMS("EnumParticle");
    }

    public static ParticleType getParticle(final String particle) {
        return Arrays.stream(values())
          .filter(type -> type.getNames().contains(particle.toUpperCase()))
          .findFirst()
          .orElse(null);
    }

    public Object getParticle() {
        try {
            return getField(epClass, name()).get(null);
        } catch (Throwable e) {
            e.printStackTrace();
        }

        return null;
    }

}
