package bet.astral.fluffy.manager;

import bet.astral.fluffy.FluffyCombat;

import bet.astral.fluffy.events.AccountLoadEvent;
import bet.astral.fluffy.statistic.Account;
import bet.astral.fluffy.statistic.Statistics;
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

public class StatisticManager implements Listener {
	private final Map<UUID, Account> users = new HashMap<>();
	private final FluffyCombat fluffy;


	public StatisticManager(FluffyCombat fluffyCombat) {
		this.fluffy = fluffyCombat;
	}

	public void onEnable() {
		for (Player player : Bukkit.getOnlinePlayers()) {
			load(player);
		}
	}

	public void onDisable() {
		for (Account account : users.values()){
			account.save();
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
		System.out.println(user.getStatistic(Statistics.DEATHS_TOTEM));
		user.save().thenRun(() -> users.remove(user.getId())).thenRun(()->fluffy.getComponentLogger().info("Saved user "+ event.getPlayer().getName()));
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
		if (users.get(player.getUniqueId()) != null){
			return CompletableFuture.completedFuture(null);
		}
		return fluffy.getStatisticsDatabase()
				.load(player.getUniqueId()).thenAccept(account->{
					users.put(account.getId(), account);
					AccountLoadEvent event = new AccountLoadEvent(true, account);
					event.callEvent();
				});
	}
}