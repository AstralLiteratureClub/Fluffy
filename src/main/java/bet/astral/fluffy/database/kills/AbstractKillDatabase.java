package bet.astral.fluffy.database.kills;

import bet.astral.fluffy.FluffyCombat;
import bet.astral.fluffy.database.AbstractDatabase;
import bet.astral.fluffy.api.Statistic;
import org.jetbrains.annotations.NotNull;

import java.sql.Connection;

public abstract class AbstractKillDatabase extends AbstractDatabase {
	protected String CREATE_TABLE;
	protected String INSERT;
	protected String UPDATE;
	protected String GET;
	protected String LEADERBOARD;
	protected final Connection connection;
	protected final String table;
	protected AbstractKillDatabase(FluffyCombat fluffyCombat, Connection connection, DBType databaseType) {
		super(fluffyCombat, databaseType);
		this.connection = connection;
		this.table = getTableName("streak", fluffyCombat.getConfig());
	}
	public String leaderboardQuery(Statistic.@NotNull Kills kills, int limit){
		return LEADERBOARD.replace("%statistic%", kills.getKey().value()).replace("%limit%", String.valueOf(limit));
	}
}
