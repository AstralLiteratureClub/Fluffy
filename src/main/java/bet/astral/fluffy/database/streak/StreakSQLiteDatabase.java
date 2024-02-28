package bet.astral.fluffy.database.streak;

import bet.astral.fluffy.FluffyCombat;
import bet.astral.fluffy.api.StatisticUser;
import bet.astral.fluffy.api.Statistic;
import org.apache.commons.lang3.tuple.Triple;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

public class StreakSQLiteDatabase extends AbstractStreakDatabase {

	public StreakSQLiteDatabase(FluffyCombat fluffyCombat, Connection connection) {
		super(fluffyCombat, connection, DBType.SQLITE);
		CREATE_TABLE = "CREATE TABLE IF NOT EXISTS "+ table + " ("
				+ "uniqueId VARCHAR(36) PRIMARY KEY, "
				+ Statistic.Streak.KILLS.key().value() + " INTEGER DEFAULT 0, "
				+ Statistic.Streak.DEATHS.key().value() + " INTEGER DEFAULT 0, "
				+ Statistic.Streak.TOTEM_OF_UNDYING_KILLS.key().value() + " INTEGER DEFAULT 0, "
				+ Statistic.Streak.TOTEM_OF_UNDYING_DEATHS.key().value() + " INTEGER DEFAULT 0"
				+ ");";
		INSERT = "INSERT INTO "+ table + "("
				+ "uniqueId, "
				+  Statistic.Streak.KILLS.key().value() + ", "
				+  Statistic.Streak.DEATHS.key().value() + ", "
				+  Statistic.Streak.TOTEM_OF_UNDYING_KILLS.key().value() + ", "
				+  Statistic.Streak.TOTEM_OF_UNDYING_DEATHS.key().value() + " VALUES (?, ?, ?, ?, ?)";
		UPDATE = "UPDATE "+ table + " SET "
				+  Statistic.Streak.KILLS.key().value() + " = ?,"
				+  Statistic.Streak.DEATHS.key().value() + " = ?,"
				+  Statistic.Streak.TOTEM_OF_UNDYING_KILLS.key().value() + " = ?,"
				+  Statistic.Streak.TOTEM_OF_UNDYING_DEATHS.key().value() + " = ?,"
				+ "uniqueId = ?";
		GET = "SELECT * FROM "+ table + " WHERE uniqueId = ?";
		LEADERBOARD = "SELECT uniqueId, %statistic% FROM "+ table + " ORDER BY %statistic% DESC LIMIT %limit%";
		createTable();
	}

	public void createTable() {
		try {
			PreparedStatement statement = connection.prepareStatement(CREATE_TABLE);
			statement.execute();
			statement.close();
		} catch (SQLException e) {
			throw new RuntimeException(e);
		}
	}

	@Override
	public void save(StatisticUser user) {
		PreparedStatement statement = null;
		if (user.isNewStreak) {
			try {
				statement = connection.prepareStatement(UPDATE);
				statement.setInt(1, user.get(Statistic.Streak.KILLS));
				statement.setInt(2, user.get(Statistic.Streak.DEATHS));
				statement.setInt(3, user.get(Statistic.Streak.TOTEM_OF_UNDYING_KILLS));
				statement.setInt(4, user.get(Statistic.Streak.TOTEM_OF_UNDYING_DEATHS));
				statement.setString(5, user.getUniqueId().toString());
				statement.executeUpdate();
				user.isNewStreak = false;
			} catch (SQLException e) {
				throw new RuntimeException(e);
			} finally {
				if (statement != null){
					try {
						statement.close();
					} catch (SQLException e) {
						throw new RuntimeException(e);
					}
				}
			}
		} else {
			try {
				statement = connection.prepareStatement(INSERT);
				statement.setString(1, user.getUniqueId().toString());
				statement.setInt(2, user.get(Statistic.Streak.KILLS));
				statement.setInt(3, user.get(Statistic.Streak.DEATHS));
				statement.setInt(4, user.get(Statistic.Streak.TOTEM_OF_UNDYING_KILLS));
				statement.setInt(5, user.get(Statistic.Streak.TOTEM_OF_UNDYING_DEATHS));
				statement.executeUpdate();
				user.isNewStreak = false;
			} catch (SQLException e) {
				throw new RuntimeException(e);
			} finally {
				if (statement != null){
					try {
						statement.close();
					} catch (SQLException e) {
						throw new RuntimeException(e);
					}
				}
			}
		}
	}

	@Override
	public void saveASync(StatisticUser user) {
		fluffy.getServer().getAsyncScheduler()
				.runNow(fluffy,
						task -> {
							PreparedStatement statement = null;
							if (user.isNewStreak) {
								try {
									statement = connection.prepareStatement(UPDATE);
									statement.setInt(1, user.get(Statistic.Streak.KILLS));
									statement.setInt(2, user.get(Statistic.Streak.DEATHS));
									statement.setInt(3, user.get(Statistic.Streak.TOTEM_OF_UNDYING_KILLS));
									statement.setInt(4, user.get(Statistic.Streak.TOTEM_OF_UNDYING_DEATHS));
									statement.setString(5, user.getUniqueId().toString());
									statement.executeUpdate();
									user.isNewStreak = false;
								} catch (SQLException e) {
									throw new RuntimeException(e);
								} finally {
									if (statement != null){
										try {
											statement.close();
										} catch (SQLException e) {
											throw new RuntimeException(e);
										}
									}
								}
							} else {
								try {
									statement = connection.prepareStatement(INSERT);
									statement.setString(1, user.getUniqueId().toString());
									statement.setInt(2, user.get(Statistic.Streak.KILLS));
									statement.setInt(3, user.get(Statistic.Streak.DEATHS));
									statement.setInt(4, user.get(Statistic.Streak.TOTEM_OF_UNDYING_KILLS));
									statement.setInt(5, user.get(Statistic.Streak.TOTEM_OF_UNDYING_DEATHS));
									statement.executeUpdate();
									user.isNewStreak = false;
								} catch (SQLException e) {
									throw new RuntimeException(e);
								} finally {
									if (statement != null){
										try {
											statement.close();
										} catch (SQLException e) {
											throw new RuntimeException(e);
										}
									}
								}
							}
						});
	}

	@Override
	public void load(StatisticUser user) {
		try {
			PreparedStatement statement = connection.prepareStatement(GET);
			statement.setString(1, user.getUniqueId().toString());
			ResultSet resultSet = statement.executeQuery();

			if (resultSet != null && resultSet.next()) {
				user.set(Statistic.Streak.KILLS, resultSet.getInt(Statistic.Streak.KILLS.key().value()));
				user.set(Statistic.Streak.DEATHS, resultSet.getInt(Statistic.Streak.DEATHS.key().value()));
				user.set(Statistic.Streak.TOTEM_OF_UNDYING_KILLS, resultSet.getInt(Statistic.Streak.TOTEM_OF_UNDYING_KILLS.key().value()));
				user.set(Statistic.Streak.TOTEM_OF_UNDYING_DEATHS, resultSet.getInt(Statistic.Streak.TOTEM_OF_UNDYING_DEATHS.key().value()));
				resultSet.close();
				user.isNewStreak = false;
			} else {
				user.isNewStreak = true;
			}
			statement.close();
		} catch (SQLException e) {
			throw new RuntimeException(e);
		}
	}

	@Override
	public CompletableFuture<StatisticUser> loadASync(StatisticUser user) {
		return CompletableFuture.supplyAsync(()->{
			try {
				PreparedStatement statement = connection.prepareStatement(GET);
				statement.setString(1, user.getUniqueId().toString());
				ResultSet resultSet = statement.executeQuery();

				if (resultSet != null && resultSet.next()) {
					user.set(Statistic.Streak.KILLS, resultSet.getInt(Statistic.Streak.KILLS.key().value()));
					user.set(Statistic.Streak.DEATHS, resultSet.getInt(Statistic.Streak.DEATHS.key().value()));
					user.set(Statistic.Streak.TOTEM_OF_UNDYING_KILLS, resultSet.getInt(Statistic.Streak.TOTEM_OF_UNDYING_KILLS.key().value()));
					user.set(Statistic.Streak.TOTEM_OF_UNDYING_DEATHS, resultSet.getInt(Statistic.Streak.TOTEM_OF_UNDYING_DEATHS.key().value()));
					resultSet.close();
					user.isNewStreak = false;
				} else {
					user.isNewStreak = true;
				}
				statement.close();
			} catch (SQLException e) {
				throw new RuntimeException(e);
			}
			return user;
		});
	}

	@Override
	public @NotNull CompletableFuture<List<@Nullable Triple<Integer, UUID, Integer>>> getLeaderboard(Statistic.Streak statistic, int maxEntries) {
		return CompletableFuture.supplyAsync(()->{
			List<@Nullable Triple<Integer, UUID, Integer>> data = new ArrayList<>();
			PreparedStatement statement = null;
			try {
				statement = connection.prepareStatement(leaderboardQuery(statistic, maxEntries));


				ResultSet resultSet = statement.executeQuery();
				if (resultSet != null){
					int i = 0;
					while (resultSet.next()){
						Triple<Integer, UUID, Integer> triple = Triple.of(
							i,
							UUID.fromString(resultSet.getString(resultSet.getString("uniqueId"))),
							resultSet.getInt(statistic.key().value()));
						data.add(triple);
						i++;
					}
					resultSet.close();
				}

			} catch (SQLException e) {
				throw new RuntimeException(e);
			} finally {
				if (statement != null){
					try {
						statement.close();
					} catch (SQLException e) {
						throw new RuntimeException(e);
					}
				}
			}

			return data;
		});
	}
}