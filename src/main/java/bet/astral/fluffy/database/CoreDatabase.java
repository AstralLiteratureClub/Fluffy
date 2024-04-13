package bet.astral.fluffy.database;

import bet.astral.fluffy.FluffyCombat;
import bet.astral.fluffy.statistic.Account;
import lombok.Getter;

import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.function.Consumer;

@Getter
public class CoreDatabase {
	private final FluffyCombat fluffy;
	private final Set<StatisticDatabase> databases = new HashSet<>();

	public CoreDatabase(FluffyCombat fluffyCombat) {
		this.fluffy = fluffyCombat;
	}

	public void addDatabase(StatisticDatabase database) {
		databases.add(database);
	}

	public void removeDatabase(StatisticDatabase database) {
		databases.remove(database);
	}

	public CompletableFuture<Void> save(Account account) {
		Collection<CompletableFuture<Void>> futures = new HashSet<>();
		databases.forEach(database -> {
			futures.add(database.save(account));
		});
		return CompletableFuture.allOf(futures.toArray(CompletableFuture[]::new));
	}

	public CompletableFuture<Void> delete(UUID account) {
		Collection<CompletableFuture<Void>> futures = new HashSet<>();
		databases.forEach(database -> {
			futures.add(database.delete(account));
		});
		return CompletableFuture.allOf(futures.toArray(CompletableFuture[]::new));
	}

	public CompletableFuture<Void> get(Account account, Consumer<Account> lastTask) {
		Collection<CompletableFuture<Account>> futures = new HashSet<>();
		databases.forEach(database -> {
			futures.add(database.get(account));
		});

		return CompletableFuture.allOf(futures.toArray(CompletableFuture[]::new))
				.thenRunAsync(() -> lastTask.accept(account)).thenRun(() -> System.out.println("Returning...!"));
	}
}