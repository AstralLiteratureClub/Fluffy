package me.antritus.astral.fluffycombat.antsfactions;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

/**
 * @author Antritus
 * @since 1.1-SNAPSHOT
 */

public class CoreDatabase {
	private final FactionsPlugin main;

	private Connection connection;

	public CoreDatabase(FactionsPlugin main) {
		this.main = main;

	}
	public void connect(){
		try {
			if (connection != null && !connection.isClosed()){
				try {
					connection.close();
				} catch (SQLException e) {
					throw new RuntimeException(e);
				}
			}
		} catch (SQLException e) {
			throw new RuntimeException(e);
		}
		try {
			// todo other db types.
			this.connection = DriverManager.getConnection((String) main.getCoreSettings().getKnownNonNull("database-url").getValue(), (String) main.getCoreSettings().getKnownNonNull("database-user").getValue(), (String) main.getCoreSettings().getKnownNonNull("database-password").getValue());
		} catch (SQLException e) {
			e.printStackTrace();
		}
		try {
			connection.setAutoCommit(true);
		} catch (SQLException e) {
			throw new RuntimeException(e);
		}
	}

	public Connection getConnection() {
		return connection;
	}

	public void closeConnection() {
		if (connection != null) {
			try {
				connection.close();
			} catch (SQLException e) {
				e.printStackTrace();
			}
		}
	}
}