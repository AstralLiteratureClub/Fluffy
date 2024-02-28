package bet.astral.fluffy.database;

import bet.astral.fluffy.api.Statistic;
import org.apache.commons.lang3.tuple.Triple;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

public interface LeaderboardTopDatabase<T extends Statistic> {
	/**
	 * Returns the leaders for given leaderboard.
	 * <p>
	 * Triple = Position, UniqueId, Amount
	 *
	 * @param statistic  statistic to find
	 * @param maxEntries maximum number of entries
	 * @return entries
	 */
	@NotNull CompletableFuture<List<@Nullable Triple<Integer, UUID, Integer>>> getLeaderboard(T statistic, int maxEntries);
}
