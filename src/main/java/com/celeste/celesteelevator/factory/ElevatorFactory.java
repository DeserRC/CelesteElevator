package com.celeste.celesteelevator.factory;

import com.celeste.celesteelevator.CelesteElevator;
import com.celeste.celesteelevator.entity.ElevatorEntity;
import com.celeste.celesteelevator.manager.ElevatorManager;
import com.celeste.registries.ConcurrentRegistry;
import io.netty.util.internal.ConcurrentSet;
import lombok.Getter;

import java.util.UUID;

@Getter
public class ElevatorFactory {

    private final ConcurrentRegistry<UUID, ElevatorEntity> addElevatorRegistry;
    private final ConcurrentSet<UUID> removeElevatorRegistry;

    private final ElevatorManager elevatorManager;

    public ElevatorFactory(final CelesteElevator plugin) {
        this.addElevatorRegistry = new ConcurrentRegistry<>();
        this.removeElevatorRegistry = new ConcurrentSet<>();
        this.elevatorManager = new ElevatorManager(plugin);
    }

}
