package bet.astral.fluffy.database.deaths;

import bet.astral.fluffy.FluffyCombat;
import bet.astral.fluffy.api.StatisticUser;
import bet.astral.fluffy.database.LeaderboardTopDatabase;
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

public class DeathSQLiteDatabase extends AbstractDeathDatabase implements LeaderboardTopDatabase<Statistic.Deaths> {
	public DeathSQLiteDatabase(FluffyCombat fluffyCombat, Connection connection) {
		super(fluffyCombat, connection, DBType.SQLITE);
		CREATE_TABLE = "CREATE TABLE IF NOT EXISTS " + table + " ("
				+ "uniqueId VARCHAR(36) PRIMARY KEY"
				+ Statistic.Deaths.ALL.key().value() + " INTEGER DEFAULT 0,"
				+ Statistic.Deaths.RESPAWN_ANCHOR.key().value() + " INTEGER DEFAULT 0,"
				+ Statistic.Deaths.ENDER_CRYSTAL.key().value() + " INTEGER DEFAULT 0,"
				+ Statistic.Deaths.TNT.key().value() + " INTEGER DEFAULT 0,"
				+ Statistic.Deaths.BED.key().value() + " INTEGER DEFAULT 0,"
				+ Statistic.Deaths.VOID.key().value() + " INTEGER DEFAULT 0,"
				+ Statistic.Deaths.TOTEM_OF_UNDYING.key().value() + " INTEGER DEFAULT 0"
				+ ");";
		INSERT = "INSERT INTO " + table + "("
				+ "uniqueId, "
				+ Statistic.Deaths.ALL.key().value() + ", "
				+ Statistic.Deaths.RESPAWN_ANCHOR.key().value() + ", "
				+ Statistic.Deaths.ENDER_CRYSTAL.key().value() + ", "
				+ Statistic.Deaths.TNT.key().value() + ", "
				+ Statistic.Deaths.BED.key().value() + ", "
				+ Statistic.Deaths.VOID.key().value() + ", "
				+ Statistic.Deaths.TOTEM_OF_UNDYING.key().value() + " "
				+ " VALUES (?, ?, ?, ?, ?, ?, ?, ?)";
		UPDATE = "UPDATE " + table + " SET "
				+ Statistic.Deaths.ALL.key().value() + " = ?,"
				+ Statistic.Deaths.RESPAWN_ANCHOR.key().value() + " = ?,"
				+ Statistic.Deaths.ENDER_CRYSTAL.key().value() + " = ?,"
				+ Statistic.Deaths.TNT.key().value() + " = ?,"
				+ Statistic.Deaths.BED.key().value() + " = ?,"
				+ Statistic.Deaths.VOID.key().value() + " = ?,"
				+ Statistic.Deaths.TOTEM_OF_UNDYING.key().value() + " = ?,"
				+ "uniqueId = ?";
		GET = "SELECT * FROM " + table + " WHERE uniqueId = ?";
		LEADERBOARD = "SELECT uniqueId, %statistic% FROM " + table + " ORDER BY %statistic% DESC LIMIT %limit%";
		createTable();
	}

	private void createTable() {
		try {
			PreparedStatement statement = connection.prepareStatement(CREATE_TABLE);
			statement.execute();
		} catch (SQLException e) {
			throw new RuntimeException(e);
		}
	}


	@Override
	public void save(StatisticUser user) {
		if (user.isNewKills) {
			PreparedStatement statement = null;
			try {
				statement = connection.prepareStatement(INSERT);
				statement.setString(1, user.getUniqueId().toString());
				statement.setInt(2, user.get(Statistic.Deaths.ALL));
				statement.setInt(3, user.get(Statistic.Deaths.RESPAWN_ANCHOR));
				statement.setInt(4, user.get(Statistic.Deaths.ENDER_CRYSTAL));
				statement.setInt(5, user.get(Statistic.Deaths.TNT));
				statement.setInt(6, user.get(Statistic.Deaths.BED));
				statement.setInt(7, user.get(Statistic.Deaths.VOID));
				statement.setInt(8, user.get(Statistic.Deaths.TOTEM_OF_UNDYING));
				statement.executeUpdate();
				user.isNewKills = false;
			} catch (SQLException e) {
				throw new RuntimeException(e);
			} finally {
				if (statement != null) {
					try {
						statement.close();
					} catch (SQLException e) {
						throw new RuntimeException(e);
					}
				}
			}
		} else {
			PreparedStatement statement = null;
			try {
				statement = connection.prepareStatement(UPDATE);
				statement.setInt(1, user.get(Statistic.Deaths.ALL));
				statement.setInt(2, user.get(Statistic.Deaths.RESPAWN_ANCHOR));
				statement.setInt(3, user.get(Statistic.Deaths.ENDER_CRYSTAL));
				statement.setInt(4, user.get(Statistic.Deaths.TNT));
				statement.setInt(5, user.get(Statistic.Deaths.BED));
				statement.setInt(6, user.get(Statistic.Deaths.VOID));
				statement.setInt(7, user.get(Statistic.Deaths.TOTEM_OF_UNDYING));
				statement.setString(8, user.getUniqueId().toString());
				statement.executeUpdate();
				user.isNewKills = false;
			} catch (SQLException e) {
				throw new RuntimeException(e);
			} finally {
				if (statement != null) {
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
		fluffy.getServer().getAsyncScheduler().runNow(fluffy, (task)->{
			if (user.isNewKills) {
				PreparedStatement statement = null;
				try {
					statement = connection.prepareStatement(INSERT);
					statement.setString(1, user.getUniqueId().toString());
					statement.setInt(2, user.get(Statistic.Deaths.ALL));
					statement.setInt(3, user.get(Statistic.Deaths.RESPAWN_ANCHOR));
					statement.setInt(4, user.get(Statistic.Deaths.ENDER_CRYSTAL));
					statement.setInt(5, user.get(Statistic.Deaths.TNT));
					statement.setInt(6, user.get(Statistic.Deaths.BED));
					statement.setInt(7, user.get(Statistic.Deaths.VOID));
					statement.setInt(8, user.get(Statistic.Deaths.TOTEM_OF_UNDYING));
					statement.executeUpdate();
					user.isNewKills = false;
				} catch (SQLException e) {
					throw new RuntimeException(e);
				} finally {
					if (statement != null) {
						try {
							statement.close();
						} catch (SQLException e) {
							throw new RuntimeException(e);
						}
					}
				}
			} else {
				PreparedStatement statement = null;
				try {
					statement = connection.prepareStatement(UPDATE);
					statement.setInt(1, user.get(Statistic.Deaths.ALL));
					statement.setInt(2, user.get(Statistic.Deaths.RESPAWN_ANCHOR));
					statement.setInt(3, user.get(Statistic.Deaths.ENDER_CRYSTAL));
					statement.setInt(4, user.get(Statistic.Deaths.TNT));
					statement.setInt(5, user.get(Statistic.Deaths.BED));
					statement.setInt(6, user.get(Statistic.Deaths.VOID));
					statement.setInt(7, user.get(Statistic.Deaths.TOTEM_OF_UNDYING));
					statement.setString(8, user.getUniqueId().toString());
					statement.executeUpdate();
					user.isNewKills = false;
				} catch (SQLException e) {
					throw new RuntimeException(e);
				} finally {
					if (statement != null) {
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
				user.set(Statistic.Deaths.ALL, resultSet.getInt(Statistic.Deaths.ALL.key().value()));
				user.set(Statistic.Deaths.RESPAWN_ANCHOR, resultSet.getInt(Statistic.Deaths.RESPAWN_ANCHOR.key().value()));
				user.set(Statistic.Deaths.ENDER_CRYSTAL, resultSet.getInt(Statistic.Deaths.ENDER_CRYSTAL.key().value()));
				user.set(Statistic.Deaths.TNT, resultSet.getInt(Statistic.Deaths.TNT.key().value()));
				user.set(Statistic.Deaths.BED, resultSet.getInt(Statistic.Deaths.BED.key().value()));
				user.set(Statistic.Deaths.VOID, resultSet.getInt(Statistic.Deaths.VOID.key().value()));
				user.set(Statistic.Deaths.TOTEM_OF_UNDYING, resultSet.getInt(Statistic.Deaths.TOTEM_OF_UNDYING.key().value()));
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
		return CompletableFuture.supplyAsync(() -> {
			try {
				PreparedStatement statement = connection.prepareStatement(GET);
				statement.setString(1, user.getUniqueId().toString());
				ResultSet resultSet = statement.executeQuery();

				if (resultSet != null && resultSet.next()) {
					user.set(Statistic.Deaths.ALL, resultSet.getInt(Statistic.Deaths.ALL.key().value()));
					user.set(Statistic.Deaths.RESPAWN_ANCHOR, resultSet.getInt(Statistic.Deaths.RESPAWN_ANCHOR.key().value()));
					user.set(Statistic.Deaths.ENDER_CRYSTAL, resultSet.getInt(Statistic.Deaths.ENDER_CRYSTAL.key().value()));
					user.set(Statistic.Deaths.TNT, resultSet.getInt(Statistic.Deaths.TNT.key().value()));
					user.set(Statistic.Deaths.BED, resultSet.getInt(Statistic.Deaths.BED.key().value()));
					user.set(Statistic.Deaths.VOID, resultSet.getInt(Statistic.Deaths.VOID.key().value()));
					user.set(Statistic.Deaths.TOTEM_OF_UNDYING, resultSet.getInt(Statistic.Deaths.TOTEM_OF_UNDYING.key().value()));
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
	public @NotNull CompletableFuture<List<@Nullable Triple<Integer, UUID, Integer>>> getLeaderboard(Statistic.Deaths statistic, int maxEntries) {
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
