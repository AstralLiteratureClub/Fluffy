package bet.astral.fluffy.database.sql.mysql;

import bet.astral.fluffy.database.sql.SQLDatabase;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.concurrent.CompletableFuture;

public class MySQLDatabase implements SQLDatabase {
    private final String table;
    private final Credentials credentials;
    private Connection connection;

    public MySQLDatabase(Credentials credentials, String table) {
        this.table = table;
        this.credentials = credentials;
    }

    @Override
    public String getTableName() {
        return table;
    }

    @Override
    public CompletableFuture<Connection> connect() {
        return CompletableFuture.supplyAsync(()->{
            try {
                connection = DriverManager.getConnection(credentials.getHost(), credentials.getUsername(), credentials.getPassword());
            } catch (SQLException e) {
                throw new RuntimeException(e);
            }
            return connection;
        });
    }

    @Override
    public CompletableFuture<Void> createTable() {
        return null;
    }

    @Override
    public CompletableFuture<Void> close() {
        return null;
    }

    @Override
    public Connection getConnection() {
        return null;
    }

    @Override
    public void updateTable(int version) throws SQLException {

    }
}
