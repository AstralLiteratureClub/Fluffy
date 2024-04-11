package bet.astral.fluffy.statistic;

import bet.astral.messenger.placeholder.Placeholder;
import bet.astral.messenger.placeholder.PlaceholderList;
import bet.astral.messenger.placeholder.Placeholderable;
import bet.astral.messenger.utils.PlaceholderUtils;
import org.jetbrains.annotations.Range;

import java.util.Collection;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

public interface Account extends Placeholderable {
	Map<Statistic, Integer> getAllStatistics();
	int getStatistic(Statistic statistic);
	void increment(Statistic statistic);
	void add(Statistic statistic, @Range(from = 0, to = Integer.MAX_VALUE) int amount);
	void decrement(Statistic statistic);
	void remove(Statistic statistic, @Range(from = 0, to = Integer.MAX_VALUE) int amount);
	void reset(Statistic statistic);
	void set(Statistic statistic, @Range(from = 0, to = Integer.MAX_VALUE) int amount);
	CompletableFuture<Void> delete(Statistic statistic);
	CompletableFuture<Void> save();

	UUID getId();


	@Override
	default Collection<Placeholder> asPlaceholder(String s) {
		PlaceholderList placeholders = new PlaceholderList();
		for (Map.Entry<Statistic, Integer> entry : getAllStatistics().entrySet()){
			Statistic statistic = entry.getKey();
			int amount = entry.getValue() != null ? entry.getValue() : 0;
			placeholders.add(PlaceholderUtils.createPlaceholder(s, statistic.getName().replace(".", "_"), amount));
		}
		return placeholders;
	}
}
