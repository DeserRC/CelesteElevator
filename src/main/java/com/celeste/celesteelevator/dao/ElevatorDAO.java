package com.celeste.celesteelevator.dao;

import com.celeste.Storage;
import com.celeste.celesteelevator.entity.ElevatorEntity;
import com.celeste.function.SqlFunction;
import com.celeste.provider.SQLConnectionProvider;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.UUID;

public class ElevatorDAO implements Storage<ElevatorEntity> {

    private static final String CREATE_REPORTS_SQL = "CREATE TABLE IF NOT EXISTS `elevator` (" +
      "id CHAR(36) NOT NULL PRIMARY KEY, " +
      "location VARCHAR(100) NOT NULL);";
    private static final String STORE_REPORT_SQL = "INSERT INTO `elevator` VALUES (?, ?);";
    private static final String DELETE_REPORT_SQL = "DELETE FROM `elevator` WHERE id=?;";
    private static final String SELECT_FROM_REPORTS_WHERE_ID = "SELECT * FROM `elevator` WHERE id=?;";
    private static final String SELECT_ALL_REPORTS = "SELECT * FROM `elevator`;";

    private final SQLConnectionProvider connectionProvider;
    private final SqlFunction<ResultSet, ElevatorEntity> function;

    public ElevatorDAO(final SQLConnectionProvider connectionProvider) {
        this.connectionProvider = connectionProvider;

        this.function = this::read;
        createTable();
    }

    @Override
    public Boolean createTable() {
        return connectionProvider.executeUpdate(CREATE_REPORTS_SQL).join();
    }

    @Override
    public void store(final ElevatorEntity report) {
        connectionProvider.executeUpdate(
          STORE_REPORT_SQL,
          report.getId(),
          report.getSerialize()
        );
    }

    @Override
    public void delete(final UUID id) {
        connectionProvider.executeUpdate(
          DELETE_REPORT_SQL,
          id.toString()
        );
    }

    @Override
    public ElevatorEntity getByValue(final UUID id) {
        return connectionProvider.getFirstFromQuery(
          SELECT_FROM_REPORTS_WHERE_ID,
          function,
          id.toString()
        ).join();
    }

    @Override
    public List<ElevatorEntity> getAll() {
        return connectionProvider.selectAsList(SELECT_ALL_REPORTS, function).join();
    }

    public ElevatorEntity read(final ResultSet resultSet) throws SQLException {
        final UUID id = UUID.fromString(resultSet.getString("id"));
        final String location = resultSet.getString("location");

        return new ElevatorEntity(id, location);
    }

}
