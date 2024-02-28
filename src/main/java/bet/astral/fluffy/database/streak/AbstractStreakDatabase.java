package bet.astral.fluffy.database.streak;

import bet.astral.fluffy.FluffyCombat;
import bet.astral.fluffy.database.AbstractDatabase;
import bet.astral.fluffy.database.LeaderboardTopDatabase;
import bet.astral.fluffy.api.Statistic;
import org.jetbrains.annotations.NotNull;

import java.sql.Connection;

public abstract class AbstractStreakDatabase extends AbstractDatabase implements LeaderboardTopDatabase<Statistic.Streak> {
	protected String CREATE_TABLE;
	protected String INSERT;
	protected String UPDATE;
	protected String GET;
	protected String LEADERBOARD;
	protected final Connection connection;
	protected final String table;
	protected AbstractStreakDatabase(FluffyCombat fluffyCombat, Connection connection, DBType databaseType) {
		super(fluffyCombat, databaseType);
		this.connection = connection;
		this.table = getTableName("streak", fluffyCombat.getConfig());
	}
	public String leaderboardQuery(Statistic.@NotNull Streak streak, int limit){
		return LEADERBOARD.replace("%statistic%", streak.getKey().value()).replace("%limit%", String.valueOf(limit));
	}
}