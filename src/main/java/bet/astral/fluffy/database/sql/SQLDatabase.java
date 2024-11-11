package bet.astral.fluffy.database.sql;

import org.jetbrains.annotations.Blocking;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.concurrent.CompletableFuture;

/**
 * Provides easy interface connection to an SQL database
 */
public interface SQLDatabase {
    /**
     * Returns the table name of the SQL table
     * @return name
     */
    String getTableName();

    /**
     * Creates a new connection to the database
     * @return table
     */
    CompletableFuture<Connection> connect();
    /**
     * Creates a new table if it does not exist. Auto adds new parameters to the database connection of needed
     * @return table
     */
    CompletableFuture<Void> createTable();

    /**
     * Closes the database connection to the database.
     * @return completed state
     */
    CompletableFuture<Void> close();

    /**
     * Returns the database connection to the database.
     * @return connection
     */
    Connection getConnection();

    /**
     * Updates the table to the latest version
     * @param version version to update to
     */
    @Blocking
    void updateTable(int version) throws SQLException;
}
