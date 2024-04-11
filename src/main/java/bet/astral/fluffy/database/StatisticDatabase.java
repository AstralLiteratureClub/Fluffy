package bet.astral.fluffy.database;

import bet.astral.fluffy.FluffyCombat;
import bet.astral.fluffy.statistic.Account;
import bet.astral.fluffy.statistic.Statistic;
import lombok.Getter;
import org.javatuples.Pair;

import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.function.Function;

@Getter
public abstract class StatisticDatabase {
	private final FluffyCombat fluffyCombat;
	private final String[] allowedKeys;
	private final Function<Pair<Account, Map<String, Integer>>, Account> createFunction;

	public StatisticDatabase(FluffyCombat fluffyCombat, Statistic[] statistics, Function<Pair<Account, Map<String, Integer>>, Account> createFunction) {
		this.fluffyCombat = fluffyCombat;
		this.allowedKeys = new String[statistics.length];
		this.createFunction = createFunction;
		for (int i = 0; i < statistics.length; i++){
			Statistic statistic = statistics[i];
			allowedKeys[i] = statistic.getName();
		}
	}

	public abstract CompletableFuture<Void> save(Account account);
	public abstract CompletableFuture<Void> delete(UUID account);
	public abstract CompletableFuture<Account> get(Account account);

}
