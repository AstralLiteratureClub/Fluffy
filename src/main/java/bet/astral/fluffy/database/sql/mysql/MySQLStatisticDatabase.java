package bet.astral.fluffy.database.sql.mysql;

import bet.astral.fluffy.FluffyCombat;
import bet.astral.fluffy.database.StatisticDatabase;
import bet.astral.fluffy.database.sql.SQLDatabase;
import bet.astral.fluffy.statistic.Account;
import bet.astral.fluffy.statistic.Statistic;
import com.zaxxer.hikari.HikariDataSource;
import org.javatuples.Pair;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.function.Function;

public class MySQLStatisticDatabase extends StatisticDatabase implements SQLDatabase {
	private final HikariDataSource dataSource;
	private final String table;


	public MySQLStatisticDatabase(FluffyCombat fluffyCombat, Statistic[] statistics, HikariDataSource hikariDataSource, String table, Function<Pair<Account, Map<String, Integer>>, Account> function) {
		super(fluffyCombat, statistics, function);
		this.dataSource = hikariDataSource;
		this.table = table;
		createTable();
	}

	private void createTable() {
		getConnection().thenAccept((connection) -> {
			PreparedStatement statement = null;

			try {
				StringBuilder builder = new StringBuilder();
				for (String key : getAllowedKeys()) {
					if (!builder.isEmpty()) {
						builder.append(", ");
					}
					builder.append(key).append(" INT");
				}
				String query = "CREATE TABLE IF NOT EXISTS " + table + "(uniqueId VARCHAR(36), " + builder + ", PRIMARY KEY (uniqueId));";
				getFluffyCombat()
						.getComponentLogger().info("Executing query: " + query);
				statement = connection.prepareStatement(query);
				statement.execute();
				getFluffyCombat()
						.getComponentLogger().info("Executed query!");
			} catch (SQLException e) {
				getFluffyCombat()
						.getComponentLogger()
						.error("Couldn't create a table properly!", e);
			} finally {
				tryAndClose(statement);
				tryAndClose(connection);
			}
		});
	}

	@Nullable
	private PreparedStatement createUpdateQuery(Connection connection, @NotNull Account account) throws SQLException {
		if (account.getAllStatistics().isEmpty()) {
			getLogger().error("All statistics for user "+ account.getId()+ " found to be empty.");
			return null;
		}
		Map<String, Integer> stats = new HashMap<>();
		account.getAllStatistics().forEach((statistic, value) -> stats.put(statistic.getName(), value));

		StringBuilder builder = new StringBuilder();

		for (String key : getAllowedKeys()) {
			if (!builder.isEmpty()) {
				builder.append(", ");
			}
			builder.append(key).append(" = ?");
		}

		String query = "UPDATE " + table + " SET " + builder + " WHERE uniqueId = ?";
		getLogger().info("Preparing statement...");
		PreparedStatement statement = connection.prepareStatement(query);
		int i = 0;
		for (String ignored : getAllowedKeys()){
			i++;
			Integer amount = stats.get(getAllowedKeys()[i]);
			if (amount == null){
				amount = 0;
			}
			statement.setInt(i, amount);
		}
		getLogger().info("Working..!");
		i++;
		statement.setString(i, account.getId().toString());
		getLogger().info("Working..! 2");
		getLogger().info("");
		return statement;
	}

	@Nullable
	public PreparedStatement createInsertQuery(Connection connection, @NotNull Account account) throws SQLException {
		if (account.getAllStatistics().isEmpty()) {
			return null;
		}
		Map<String, Integer> stats = new HashMap<>();
		account.getAllStatistics().forEach((statistic, value) -> stats.put(statistic.getName(), value));

		StringBuilder keys = new StringBuilder();
		StringBuilder values = new StringBuilder();

		for (String key : getAllowedKeys()) {
			if (!keys.isEmpty()) {
				keys.append(", ");
				values.append(", ");
			}
			keys.append(key);
			values.append("?");
		}

		PreparedStatement statement = connection.prepareStatement("INSERT INTO " + table + "(" + "uniqueId, " + keys + ") VALUES (" + "?, " + values + ")");
		int i = 1;
		statement.setString(i, account.getId().toString());
		for (String ignored : getAllowedKeys()){
			i++;
			Integer amount = stats.get(getAllowedKeys()[i]);
			if (amount == null){
				amount = 0;
			}
			statement.setInt(i, amount);
		}
		return statement;
	}

	@Override
	public CompletableFuture<Void> save(@NotNull Account account) {
		return exists(account.getId())
				.thenAccept(exists -> {
					Connection connection = getConnectionUnsafe();
					PreparedStatement statement = null;
					try {
						if (exists) {
							statement = createUpdateQuery(connection, account);
							if (statement == null) {
								getLogger().error("Couldn't get update statement for "+ account.getId());
								return;
							}
							statement.executeUpdate();
						} else {
							statement = createInsertQuery(connection, account);
							if (statement == null) {
								getLogger().error("Couldn't get insert statement for "+ account.getId());
								return;
							}
							statement.executeUpdate();
						}
					} catch (SQLException e) {
						getFluffyCombat()
								.getComponentLogger()
								.error("Couldn't save an account properly!", e);
					} finally {
						tryAndClose(statement);
						tryAndClose(connection);
					}});
	}

	@Override
	public CompletableFuture<Void> delete(UUID account) {

		return getConnection().thenAccept(connection -> {
			PreparedStatement statement = null;
			try {

				statement = connection.prepareStatement("DELETE FROM " + table + " WHERE uniqueId = ?");
				statement.setString(1, account.toString());
				statement.executeUpdate();
			} catch (SQLException e) {
				getFluffyCombat()
						.getComponentLogger()
						.error("Couldn't delete an account properly!", e);
			} finally {
				tryAndClose(statement);
				tryAndClose(connection);
			}
		});
	}

	@Override
	public CompletableFuture<Account> get(@NotNull Account accountId) {
		getLogger().info("Account: "+ accountId);
		getLogger().info("Account Id: "+ accountId.getId().toString());
		return getConnection().thenApplyAsync((connection) -> {
			try {
				if (connection.isClosed()){
					getLogger().error("Connection is closed!");
				}
			} catch (SQLException e) {
				getLogger().error("Connection is closed!", e);
				return null;
			}
			PreparedStatement statement = null;
			ResultSet resultSet = null;
			try {
				String query = "SELECT * FROM " + table + " WHERE uniqueId = ?";
				statement = connection.prepareStatement(query);
				statement.setString(1, accountId.getId().toString());
				resultSet = statement.executeQuery();
				if (resultSet != null && resultSet.next()) {
					Map<String, Integer> stats = new HashMap<>();
					for (String key : getAllowedKeys()) {
						int val = resultSet.getInt(key);
						stats.put(key, val);
					}
					return getCreateFunction().apply(Pair.with(accountId, stats));
				}
				return null;
			} catch (SQLException e) {
				getFluffyCombat()
						.getComponentLogger()
						.error("Couldn't load an account properly!", e);
			} finally {
				tryAndClose(statement);
				tryAndClose(resultSet);
				tryAndClose(connection);
			}
			return null;
		});
	}


	@Override
	public HikariDataSource getDataSource() {
		return dataSource;
	}

	@Override
	public CompletableFuture<Boolean> exists(UUID uniqueId) {
		return getConnection()
				.thenApplyAsync((connection) -> {
					PreparedStatement statement = null;
					ResultSet resultSet = null;
					try {
						statement = connection.prepareStatement("SELECT * FROM " + table + " WHERE uniqueId = ?");
						statement.setString(1, uniqueId.toString());
						resultSet = statement.executeQuery();
						return resultSet != null && resultSet.next();
					} catch (SQLException e) {
						getFluffyCombat()
								.getComponentLogger()
								.error("Couldn't load an account properly!", e);
					} finally {
						tryAndClose(statement);
						tryAndClose(resultSet);
						tryAndClose(connection);
					}
					return false;
				});
	}
}
