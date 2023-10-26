package me.antritus.astral.fluffycombat.listeners;

import me.antritus.astral.fluffycombat.FluffyCombat;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.jetbrains.annotations.NotNull;

public class PlayerJoinListener implements Listener {
	private final FluffyCombat combat;

	public PlayerJoinListener(FluffyCombat combat) {
		this.combat = combat;
	}

	@EventHandler(priority = EventPriority.NORMAL)
	public void onJoin(@NotNull PlayerJoinEvent event){
		combat.getUserManager().onJoin(event.getPlayer());
		combat.getCombatManager().onJoin(event.getPlayer());
	}
}
