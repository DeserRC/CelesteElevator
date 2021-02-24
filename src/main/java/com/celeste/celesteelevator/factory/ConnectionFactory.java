package com.celeste.celesteelevator.factory;

import com.celeste.SQLProcessor;
import com.celeste.celesteelevator.CelesteElevator;
import com.celeste.celesteelevator.dao.ElevatorDAO;
import com.celeste.celesteelevator.manager.ConfigManager;
import lombok.Getter;

@Getter
public class ConnectionFactory {

    private final SQLProcessor sqlProcessor;

    private final ElevatorDAO elevatorDAO;

    public ConnectionFactory(final CelesteElevator plugin) {
        final ConfigManager config = plugin.getConfigManager();

        this.sqlProcessor = new SQLProcessor(plugin.getExecutor());
        this.sqlProcessor.connect(
          config.getConfig("mysql.hostname"),
          config.getConfig("mysql.port"),
          config.getConfig("mysql.database"),
          config.getConfig("mysql.username"),
          config.getConfig("mysql.password")
        );

        this.elevatorDAO = new ElevatorDAO(sqlProcessor.getSqlConnectionProvider());
    }

}
