package bet.astral.fluffy.manager;

import bet.astral.fluffy.FluffyCombat;
import bet.astral.fluffy.api.StatisticUser;
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
	private final Map<UUID, StatisticUser> users = new HashMap<>();
	private final Set<StatisticUser> loadingUsers = new HashSet<>();
	private final Set<StatisticUser> savingUsers = new HashSet<>();
	private final Set<StatisticUser> removingUsers = new HashSet<>();
	private final FluffyCombat fluffy;


	public StatisticManager(FluffyCombat fluffyCombat){
		this.fluffy = fluffyCombat;

		fluffy.getServer().getAsyncScheduler().runAtFixedRate(fluffyCombat,
				(task)->{
					for (StatisticUser user : removingUsers){
						users.remove(user.getUniqueId());
					}
					removingUsers.clear();

					for (StatisticUser user : loadingUsers){
						users.put(user.getUniqueId(), user);
					}

					for (StatisticUser user : savingUsers){
						fluffy.getStatisticDatabase().save(user);
					}
				},
				10, 30,
				TimeUnit.SECONDS);
	}

	public void onEnable(){
		for (Player player : Bukkit.getOnlinePlayers()){
			load(player).thenAcceptAsync(loadingUsers::add);
		}
	}
	public void onDisable(){
		for (StatisticUser user : savingUsers){
			fluffy.getStatisticDatabase().save(user);
		}
	}

	@EventHandler(ignoreCancelled = true, priority = EventPriority.MONITOR)
	public void onLogin(PlayerLoginEvent event) {
		if (event.getResult() == PlayerLoginEvent.Result.ALLOWED) {
			StatisticUser u = users.get(event.getPlayer().getUniqueId());
			if (u != null) {
				if (u.getRejoinTimer() != null) {
					u.getRejoinTimer().cancel();
					removingUsers.remove(u);
					return;
				}
			}
			load(event.getPlayer()).thenAcceptAsync(user -> {
				loadingUsers.removeIf(us -> us.getUniqueId().equals(user.getUniqueId()));
				loadingUsers.add(user);
			});
		}
	}

	@EventHandler()
	public void onQuit(PlayerQuitEvent event){
		StatisticUser user = users.get(event.getPlayer().getUniqueId());
		if (user.getRejoinTimer() != null){
			user.getRejoinTimer().cancel();
		}
		removingUsers.add(user);
	}

	@Nullable
	public StatisticUser get(UUID uuid){
		return users.get(uuid);
	}

	@NotNull
	public StatisticUser get(Player player){
		return users.get(player.getUniqueId());
	}

	public CompletableFuture<StatisticUser> load(OfflinePlayer player){
		if (true) {
			return CompletableFuture.supplyAsync(()->{
				return new StatisticUser(player.getUniqueId());
			});
		}
		return fluffy.getStatisticDatabase().loadASync(new StatisticUser(player.getUniqueId()));
	}
}