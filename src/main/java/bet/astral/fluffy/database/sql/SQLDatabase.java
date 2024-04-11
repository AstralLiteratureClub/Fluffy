package bet.astral.fluffy.database.sql;

import com.zaxxer.hikari.HikariDataSource;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.UUID;

public interface SQLDatabase {
	HikariDataSource getDataSource();
	default Connection getConnection() throws SQLException {
		return getDataSource().getConnection();
	}

	boolean exists(UUID uniqueId);


	default void tryAndClose(ResultSet resultSet) {
		try {
			if (resultSet != null && !resultSet.isClosed()){
				resultSet.close();
			}
		} catch (SQLException e) {
			throw new RuntimeException(e);
		}
	}
	default void tryAndClose(Statement statement) {
		try {
			if (statement != null && !statement.isClosed()){
				statement.close();
			}
		} catch (SQLException e) {
			throw new RuntimeException(e);
		}
	}
}
