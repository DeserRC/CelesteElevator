package com.celeste.celesteelevator.task;

import com.celeste.celesteelevator.CelesteElevator;
import com.celeste.celesteelevator.factory.ConnectionFactory;
import com.celeste.celesteelevator.manager.ElevatorManager;
import lombok.AllArgsConstructor;

@AllArgsConstructor
public class ElevatorGetTask implements Runnable {

    private final CelesteElevator plugin;

    @Override
    public void run() {
        final ConnectionFactory connection = plugin.getConnectionFactory();
        final ElevatorManager elevator = plugin.getElevatorFactory().getElevatorManager();

        connection.getElevatorDAO().getAll().forEach(elevator::registryMetadata);
    }

}
