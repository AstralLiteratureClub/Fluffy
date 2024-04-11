package bet.astral.fluffy.database.sql.mysql;

import bet.astral.fluffy.FluffyCombat;
import bet.astral.fluffy.database.StatisticDatabase;
import bet.astral.fluffy.database.sql.SQLDatabase;
import bet.astral.fluffy.statistic.Account;
import bet.astral.fluffy.statistic.Statistic;
import com.zaxxer.hikari.HikariDataSource;
import org.javatuples.Pair;
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
	}

	private void createTable(){
		PreparedStatement statement = null;

		try {
			StringBuilder builder = new StringBuilder();
			for (String key : getAllowedKeys()){
				if (!builder.isEmpty()){
					builder.append(", ");
				}
				builder.append(key).append(" INT");
			}
			statement = getConnection().prepareStatement("CREATE TABLE IF NOT EXISTS "+ table+ "(uniqueId VARCHAR(36)"+ builder+", PRIMARY KEY (uniqueId));");
			statement.execute();
		} catch (SQLException e) {
			throw new RuntimeException(e);
		} finally {
			tryAndClose(statement);
		}
	}

	@Nullable
	private PreparedStatement createUpdateQuery(Account account) throws SQLException {
		if (account.getAllStatistics().isEmpty()){
			return null;
		}
		Map<String, Integer> stats = new HashMap<>();
		account.getAllStatistics().forEach((statistic, value)-> stats.put(statistic.getName(), value));

		StringBuilder builder = new StringBuilder();

		for (String key : getAllowedKeys()){
			if (!builder.isEmpty()){
				builder.append(", ");
			}
			builder.append(key).append(" = ?");
		}

		Connection connection = getConnection();

		PreparedStatement statement = connection.prepareStatement("UPDATE "+table+ " SET "+ builder + " WHERE uniqueId = ?");
		int i;
		for (i = 0; i < getAllowedKeys().length; i++){
			statement.setInt(i, stats.get(getAllowedKeys()[i]));
		}
		i++;
		statement.setString(i, account.getId().toString());
		return statement;
	}

	@Nullable
	public PreparedStatement createInsertQuery(Account account) throws SQLException{
		if (account.getAllStatistics().isEmpty()){
			return null;
		}
		Map<String, Integer> stats = new HashMap<>();
		account.getAllStatistics().forEach((statistic, value)-> stats.put(statistic.getName(), value));

		StringBuilder keys = new StringBuilder();
		StringBuilder values = new StringBuilder();

		for (String key : getAllowedKeys()){
			if (!keys.isEmpty()){
				keys.append(", ");
				values.append(", ");
			}
			keys.append(key);
			values.append("?");
		}

		Connection connection = getConnection();

		PreparedStatement statement = connection.prepareStatement("INSERT INTO "+table + "("+ "uniqueId, "+ keys+") VALUES (" + "?, "+ values + ")");
		int i = 0;
		statement.setString(i, account.getId().toString());

		for (i = 1; i < getAllowedKeys().length+1; i++){
			statement.setInt(i, stats.get(getAllowedKeys()[i]));
		}
		return statement;
	}

	@Override
	public CompletableFuture<Void> save(Account account) {
		return CompletableFuture.runAsync(()->{
			PreparedStatement statement = null;
			try {
				if (exists(account.getId())) {
					statement = createUpdateQuery(account);
					if (statement == null){
						return;
					}
					statement.executeUpdate();
				} else {
					statement = createInsertQuery(account);
					if (statement == null){
						return;
					}
					statement.executeUpdate();
				}
			} catch (SQLException e) {
				throw new RuntimeException(e);
			} finally {
				tryAndClose(statement);
			}
		});
	}

	@Override
	public CompletableFuture<Void> delete(UUID account) {
		return CompletableFuture.runAsync(()->{
			PreparedStatement statement = null;
			try {

				if (exists(account)){
					statement = getConnection().prepareStatement("DELETE FROM " + table + " WHERE uniqueId = ?");
					statement.setString(1, account.toString());
					statement.executeUpdate();
				}
			} catch (SQLException e) {
				throw new RuntimeException(e);
			} finally {
				tryAndClose(statement);
			}
		});
	}

	@Override
	public CompletableFuture<Account> get(Account accountId) {
		return CompletableFuture.supplyAsync(() -> {
			PreparedStatement statement = null;
			ResultSet resultSet = null;
			try {
				Connection connection = getConnection();

				statement = connection.prepareStatement("GET * FROM " + table + " WHERE uniqueId = ?");
				statement.setString(1, accountId.getId().toString());
				resultSet = statement.executeQuery();
				if (resultSet != null && resultSet.next()){
					Map<String, Integer> stats = new HashMap<>();
					for (String key : getAllowedKeys()){
						int val = resultSet.getInt(key);
						stats.put(key, val);
					}
					return getCreateFunction().apply(Pair.with(accountId, stats));
				}
				return null;
			} catch (SQLException e) {
				throw new RuntimeException(e);
			} finally {
				tryAndClose(statement);
				tryAndClose(resultSet);
			}
		});
	}

	@Override
	public HikariDataSource getDataSource() {
		return dataSource;
	}

	@Override
	public boolean exists(UUID uniqueId) {
		PreparedStatement statement = null;
		ResultSet resultSet = null;
		try {
			Connection connection = getConnection();

			statement = connection.prepareStatement("GET * FROM "+ table + " WHERE uniqueId = ?");
			statement.setString(1, uniqueId.toString());
			resultSet = statement.executeQuery();
			return resultSet != null && resultSet.next();
		} catch (SQLException e) {
			throw new RuntimeException(e);
		} finally {
			tryAndClose(statement);
			tryAndClose(resultSet);
		}
	}
}
