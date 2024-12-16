package bet.astral.fluffy.statistic;

import bet.astral.fluffy.FluffyCombat;
import lombok.Getter;
import org.jetbrains.annotations.Range;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

@Getter
public class AccountImpl implements Account {
	private final UUID uniqueId;
	private final FluffyCombat fluffyCombat;
	private final Map<Statistic, Integer> statistics = new HashMap<>();

	public AccountImpl(FluffyCombat fluffyCombat, UUID uniqueId) {
		this.fluffyCombat = fluffyCombat;
		this.uniqueId = uniqueId;
	}

	protected void addStatisticPlaceholder(Statistic statistic, int amount) {
		setDefault(statistic, 0);
		statistics.put(statistic, statistics.get(statistic)+amount);
	}

	@Override
	public Map<Statistic, Integer> getAllStatistics() {
		return statistics;
	}

	@Override
	public int getStatistic(Statistic statistic) {
		return statistics.get(statistic) != null ? statistics.get(statistic) : 0;
	}

	private boolean has(Statistic statistic){
		return statistics.get(statistic) != null;
	}

	@Override
	public void increment(Statistic statistic) {
		if (!has(statistic)){
			setDefault(statistic, 1);
			return;
		}
		statistics.put(statistic, statistics.get(statistic)+1);
	}

	@Override
	public void add(Statistic statistic, @Range(from = 0, to = Integer.MAX_VALUE) int amount) {
		if (amount > 1){
			if (statistic.canOnlyIncrement()){
				increment(statistic);
				return;
			}
		}
		statistics.putIfAbsent(statistic, 0);
		int a = statistics.get(statistic);
		a+=amount;
		if (a<0){
			a = 0;
		}
		statistics.put(statistic, a);
	}

	@Override
	public void decrement(Statistic statistic) {
		if (statistic.canOnlyIncrement()){
			return;
		}
		if (!has(statistic)){
			setDefault(statistic, -1);
			return;
		}
		statistics.put(statistic, statistics.get(statistic)-1);
	}

	@Override
	public void remove(Statistic statistic, @Range(from = 0, to = Integer.MAX_VALUE) int amount) {
		if (statistic.canOnlyIncrement()){
			return;
		}

		statistics.putIfAbsent(statistic, 0);
		int a = statistics.get(statistic);
		a-=amount;
		if (a<0){
			a = 0;
		}
		statistics.put(statistic, a);
	}

	@Override
	public void reset(Statistic statistic) {
		if (statistic.canOnlyIncrement()){
			return;
		}
		statistics.remove(statistic);
	}

	@Override
	public void set(Statistic statistic, @Range(from = 0, to = Integer.MAX_VALUE) int amount) {
		if (statistic.canOnlyIncrement()) {
			if (amount - getStatistic(statistic) != 1) {
				return;
			}
		}
		statistics.put(statistic, amount);
	}

	@Override
	public void setDefault(Statistic statistic, @Range(from = 0, to = Integer.MAX_VALUE) int amount) {
		statistics.putIfAbsent(statistic, amount);
	}


	@Override
	public CompletableFuture<Void> delete(Statistic statistic) {
		if (statistic.canOnlyIncrement()) {
			return new CompletableFuture<>();
		}
		statistics.remove(statistic);
		return save();
	}

	@Override
	public CompletableFuture<Void> save() {
		return fluffyCombat.getStatisticsDatabase().save(this);
	}

	@Override
	public UUID getId() {
		return uniqueId;
	}
}
