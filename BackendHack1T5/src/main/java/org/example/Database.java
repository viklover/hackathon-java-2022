package org.example;

import java.sql.*;

public class Database {

    private static Connection connection;

    private static final String UPDATE_REQUEST = "UPDATE %s SET %s WHERE %s;";

    public static void init() throws SQLException {
        Logger.print("Database", "Connecting to MySQL..");
        connection = DriverManager.getConnection(Main.MYSQL_URL, Main.MYSQL_USERNAME, Main.MYSQL_PASSWORD);
    }

    public static Connection getConnection() {
        return connection;
    }

    public static void executeUpdate(String query) {
        try(Statement statement = connection.createStatement()) {
            statement.executeUpdate(query);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public static ResultSet executeQuery(String query) throws SQLException {
        return connection.createStatement().executeQuery(query);
    }

    public static Statement executeStatement(String query) throws SQLException {
        Statement statement = connection.createStatement();
        statement.execute(query);
        return statement;
    }
}
