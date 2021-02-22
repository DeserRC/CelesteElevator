package com.celeste.celesteelevator.task;

import com.celeste.celesteelevator.CelesteElevator;
import com.celeste.celesteelevator.entity.ElevatorEntity;
import com.celeste.celesteelevator.factory.ConnectionFactory;
import com.celeste.celesteelevator.factory.ElevatorFactory;
import lombok.AllArgsConstructor;

@AllArgsConstructor
public class ElevatorUpdateTask implements Runnable {

    private final CelesteElevator plugin;

    @Override
    public void run() {
        final ConnectionFactory connection = plugin.getConnectionFactory();
        final ElevatorFactory elevator = plugin.getElevatorFactory();

        elevator.getAddElevatorRegistry().keySet().forEach(id -> {
            final ElevatorEntity entity = elevator.getAddElevatorRegistry().get(id);
            connection.getElevatorDAO().store(entity);
        });

        elevator.getRemoveElevatorRegistry().forEach(id -> {
            connection.getElevatorDAO().delete(id);
        });

        elevator.getAddElevatorRegistry().clear();
        elevator.getRemoveElevatorRegistry().clear();
    }

}
