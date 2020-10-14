package crt.cs.log.service;

import crt.cs.log.domain.Event;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.sql.*;
import java.util.List;

/**
 * DatabaseManager
 * support interactions with hsql database
 */
@Slf4j
@NoArgsConstructor
public class DatabaseManager {

    private static final String URL = "jdbc:hsqldb:file:db/eventdb";
    private static final String CREATE_TABLE_SQL = "CREATE TABLE IF NOT EXISTS event (id VARCHAR(50) NOT NULL, "
        + "duration INT NOT NULL, type VARCHAR(25), host VARCHAR(25), alert BOOLEAN NOT NULL);";
    private static final String INSERT_TABLE_SQL = "INSERT INTO event (id, duration, type, host, alert) " +
            "VALUES(?,?,?,?,?);";

    private Connection connection;

    public void init() throws SQLException, ClassNotFoundException {
        Class.forName("org.hsqldb.jdbcDriver");
        this.connection = DriverManager.getConnection(URL, "SA", "");
    }

    public void create() throws SQLException, ClassNotFoundException {
        this.init();
        Statement statement = connection.createStatement();
        int result = statement.executeUpdate(CREATE_TABLE_SQL);
        log.debug("Created events table");
    }

    public void countEvents() {
        ResultSet resultSet;
        try {
            Statement statement = connection.createStatement();
            resultSet = statement.executeQuery("SELECT COUNT(*) as countcol from event");
            while (resultSet.next()) {
                String count = resultSet.getString("countcol");
                log.info("Total event count in database = {}", count);
            }
        } catch (SQLException ex) {
            log.error("Could not execute count on events table due to: ", ex);
            return;
        }
    }

    public void insert(final List<Event> events) throws SQLException {
        PreparedStatement preparedStatement = this.connection.prepareStatement(INSERT_TABLE_SQL);
        for (Event event : events) {
            preparedStatement.setString(1, event.getId());
            preparedStatement.setLong(2, event.getDuration());
            preparedStatement.setString(3, event.getType());
            preparedStatement.setString(4, event.getHost());
            preparedStatement.setBoolean(5, event.isAlert());
            preparedStatement.addBatch();
        }
        preparedStatement.executeBatch();
        connection.commit();
    }

    public void close() {
        try {
            Thread.sleep(1000);
            //I noticed that for small input sizes some items are not being written in /db, (hacky solution warning)
            this.connection.close();
        } catch (SQLException | InterruptedException ex) {
            log.error("Cannot close connection due to: {}", ex);
        }
    }
}