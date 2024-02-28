package bet.astral.fluffy.database;

import bet.astral.fluffy.FluffyCombat;
import bet.astral.fluffy.api.StatisticUser;
import bet.astral.fluffy.database.deaths.AbstractDeathDatabase;
import bet.astral.fluffy.database.deaths.DeathSQLiteDatabase;
import bet.astral.fluffy.database.kills.AbstractKillDatabase;
import bet.astral.fluffy.database.kills.KillSQLiteDatabase;
import bet.astral.fluffy.database.streak.AbstractStreakDatabase;
import bet.astral.fluffy.database.streak.StreakSQLiteDatabase;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.concurrent.CompletableFuture;

public class CombinedStatisticDatabase extends AbstractDatabase {
	private final AbstractDeathDatabase deathDatabase;
	private final AbstractKillDatabase killDatabase;
	private final AbstractStreakDatabase streakDatabase;

	public CombinedStatisticDatabase(FluffyCombat fluffyCombat) {
		super(fluffyCombat, fluffyCombat.getCombatConfig().getStatisticDatabaseType());
		Connection connection = connect();


		switch (databaseType){
			case SQLITE -> {
				deathDatabase = new DeathSQLiteDatabase(fluffyCombat, connection);
				killDatabase = new KillSQLiteDatabase(fluffyCombat, connection);
				streakDatabase = new StreakSQLiteDatabase(fluffyCombat, connection);
			}
			default -> {
				throw new IllegalStateException("Couldn't find database classes for "+ databaseType.name());
			}
		}
	}

	private Connection connect(){
		Connection connection;
		try {
			connection = databaseType.helpConnect(fluffy.getConfiguration(), "database.stats");
		} catch (SQLException e){
			fluffy.getServer().getPluginManager().disablePlugin(fluffy);
			throw new RuntimeException(e);
		}
		return connection;
	}

	@Override
	public void save(StatisticUser user) {
		deathDatabase.save(user);
		killDatabase.save(user);
		streakDatabase.save(user);
	}

	@Override
	public void saveASync(StatisticUser user) {
		deathDatabase.saveASync(user);
		killDatabase.saveASync(user);
		streakDatabase.saveASync(user);
	}

	@Override
	public void load(StatisticUser user) {
		deathDatabase.load(user);
		killDatabase.load(user);
		streakDatabase.load(user);
	}

	@Override
	public CompletableFuture<StatisticUser> loadASync(StatisticUser user) {
		CompletableFuture<StatisticUser> kill = killDatabase.loadASync(user);
		CompletableFuture<StatisticUser> death = deathDatabase.loadASync(user);
		CompletableFuture<StatisticUser> streak = streakDatabase.loadASync(user);

		return CompletableFuture.allOf(kill, death, streak)
				.thenApply(ignored -> user);
	}
}
