package bet.astral.fluffy.statistic;

import bet.astral.messenger.v2.placeholder.Placeholder;
import bet.astral.messenger.v2.placeholder.collection.PlaceholderList;
import bet.astral.messenger.v2.placeholder.Placeholderable;
import net.kyori.adventure.text.Component;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Range;

import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

public interface Account extends Placeholderable {
	Map<Statistic, Integer> getAllStatistics();
	int getStatistic(Statistic statistic);
	void increment(Statistic statistic);
	void add(Statistic statistic, @Range(from = -1, to = Integer.MAX_VALUE) int amount);
	void decrement(Statistic statistic);
	void remove(Statistic statistic, @Range(from = -1, to = Integer.MAX_VALUE) int amount);
	void reset(Statistic statistic);
	void set(Statistic statistic, @Range(from = -1, to = Integer.MAX_VALUE) int amount);
	void setDefault(Statistic statistic, @Range(from = -1, to = Integer.MAX_VALUE) int amount);
	CompletableFuture<Void> delete(Statistic statistic);
	CompletableFuture<Void> save();

	UUID getId();


	@Override
	default @NotNull List<Placeholder> toPlaceholders(String s) {
		PlaceholderList placeholders = new PlaceholderList();
		for (Map.Entry<Statistic, Integer> entry : getAllStatistics().entrySet()){
			Statistic statistic = entry.getKey();
			int amount = entry.getValue() != null ? entry.getValue() : 0;
			placeholders.add(Placeholder.of(s, statistic.getName(), Component.text(amount)));
		}
		return placeholders;
	}
}
