package com.celeste.celesteelevator.factory;

import com.celeste.celesteelevator.CelesteElevator;
import com.celeste.celesteelevator.task.ElevatorGetTask;
import com.celeste.celesteelevator.task.ElevatorUpdateTask;
import lombok.Getter;

@Getter
public class TaskFactory {

    private final ElevatorGetTask elevatorGetTask;
    private final ElevatorUpdateTask elevatorUpdateTask;

    public TaskFactory(final CelesteElevator plugin) {
        this.elevatorGetTask = new ElevatorGetTask(plugin);
        this.elevatorUpdateTask = new ElevatorUpdateTask(plugin);
    }

}
