package bet.astral.fluffy.manager;

import bet.astral.fluffy.FluffyCombat;


import bet.astral.fluffy.api.AccountImpl;
import bet.astral.fluffy.statistic.Account;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerLoginEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

public class StatisticManager implements Listener {
	private final Map<UUID, Account> users = new HashMap<>();
	private final Set<Account> loadingUsers = new HashSet<>();
	private final Set<Account> savingUsers = new HashSet<>();
	private final Set<Account> removingUsers = new HashSet<>();
	private final FluffyCombat fluffy;


	public StatisticManager(FluffyCombat fluffyCombat) {
		this.fluffy = fluffyCombat;

		fluffy.getServer().getAsyncScheduler().runAtFixedRate(fluffyCombat,
				(task) -> {
					for (Account user : removingUsers) {
						users.remove(user.getId());
					}
					removingUsers.clear();

					for (Account user : loadingUsers) {
						users.put(user.getId(), user);
					}

					for (Account user : savingUsers) {
						fluffy.getDatabase().save(user);
					}
				},
				10, 30,
				TimeUnit.SECONDS);
	}

	public void onEnable() {
		for (Player player : Bukkit.getOnlinePlayers()) {
			load(player);
		}
	}

	public void onDisable() {
		for (Account user : savingUsers) {
			fluffy.getDatabase().save(user);
		}
	}

	@EventHandler(ignoreCancelled = true, priority = EventPriority.MONITOR)
	public void onLogin(PlayerLoginEvent event) {
		if (event.getResult() == PlayerLoginEvent.Result.ALLOWED) {
			load(event.getPlayer());
		}
	}

	@EventHandler()
	public void onQuit(PlayerQuitEvent event) {
		Account user = users.get(event.getPlayer().getUniqueId());
		if (user != null) {
			removingUsers.add(user);
		}
	}

	@Nullable
	public Account get(UUID uuid) {
		return users.get(uuid);
	}

	@NotNull
	public Account get(Player player) {
		return users.get(player.getUniqueId());
	}

	public CompletableFuture<Void> load(OfflinePlayer player) {
		return CompletableFuture
				.runAsync(() -> fluffy.getDatabase().get(new AccountImpl(fluffy, player.getUniqueId()), (user) -> {
					users.put(user.getId(), user);
				}));
	}
}