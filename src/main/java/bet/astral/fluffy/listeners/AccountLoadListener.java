package bet.astral.fluffy.listeners;

import bet.astral.fluffy.FluffyCombat;
import bet.astral.fluffy.events.AccountLoadEvent;
import bet.astral.fluffy.statistic.Statistic;
import bet.astral.fluffy.statistic.Statistics;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

public class AccountLoadListener implements Listener {
	private final FluffyCombat fluffy;

	public AccountLoadListener(FluffyCombat fluffy) {
		this.fluffy = fluffy;
	}

	@EventHandler
	public void onLoad(AccountLoadEvent event){
		for (Statistic statistic : Statistics.values()){
			event.getAccount().setDefault(statistic, 0);
		}

		for (Statistic statistic : Statistics.values()){
			OfflinePlayer player = Bukkit.getOfflinePlayer(event.getAccount().getId());
			if (player instanceof Player p){
				p.sendMessage(statistic.getName() + " "+event.getAccount().getStatistic(statistic));
			}
		}
	}
}
