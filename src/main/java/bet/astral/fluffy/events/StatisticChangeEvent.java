package bet.astral.fluffy.events;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.HandlerList;
import org.bukkit.event.player.PlayerEvent;
import org.jetbrains.annotations.NotNull;

public class StatisticChangeEvent extends PlayerEvent {
	public StatisticChangeEvent(@NotNull Player who) {
		super(who, Bukkit.isPrimaryThread());
	}

	@Override
	public @NotNull HandlerList getHandlers() {
		return null;
	}
}
