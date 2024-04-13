package bet.astral.fluffy.database.sql;

import com.zaxxer.hikari.HikariDataSource;
import net.kyori.adventure.text.logger.slf4j.ComponentLogger;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

public interface SQLDatabase {
	HikariDataSource getDataSource();
	default Connection getConnectionUnsafe(){
		try {
			return getDataSource().getConnection();
		} catch (SQLException e) {
			getLogger().info("Couldn't get database connection.", e);
		}
		return null;
	}
	default CompletableFuture<Connection> getConnection() {
		return CompletableFuture.supplyAsync(()-> {
			try {
				return getDataSource().getConnection();
			} catch (SQLException e) {
				getLogger().info("Couldn't get database connection.", e);
			}
			return null;
		});
	}

	CompletableFuture<Boolean> exists(UUID uniqueId);


	default void tryAndClose(ResultSet resultSet) {
		try {
			if (resultSet != null && !resultSet.isClosed()){
				resultSet.close();
			}
		} catch (SQLException e) {
			getLogger().error("Couldn't close resultSet properly!", e);
		}
	}
	default void tryAndClose(Statement statement) {
		try {
			if (statement != null && !statement.isClosed()){
				statement.close();
			}
		} catch (SQLException e) {
			getLogger().error("Couldn't close statement properly!", e);
		}
	}
	default void tryAndClose(Connection connection){
		try {
			if (connection != null && !connection.isClosed()){
				connection.close();
			}
		} catch (SQLException e) {
			getLogger().error("Couldn't close connection properly!", e);
		}
	}
	ComponentLogger getLogger();
}
