package com.celeste.celesteelevator.entity;

import com.celeste.celesteelevator.util.LocationUtil;
import lombok.Getter;
import org.bukkit.Location;
import org.bukkit.block.Block;

import java.util.UUID;

@Getter
public class ElevatorEntity {

    private final UUID id;
    private final String serialize;
    private transient final Location location;

    public ElevatorEntity(final UUID id, final String serialize) {
        this.id = id;
        this.serialize = serialize;
        this.location = LocationUtil.deserialize(serialize);
    }

    public ElevatorEntity(final UUID id, final Location location) {
        this.id = id;
        this.serialize = LocationUtil.serialize(location);
        this.location = location;
    }

    public Block getBlock() {
        return location.getBlock();
    }

}
