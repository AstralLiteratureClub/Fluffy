package me.antritus.astral.fluffycombat.listeners;

import me.antritus.astral.fluffycombat.FluffyCombat;
import org.bukkit.event.Listener;

public class DeathListener implements Listener {
	private final FluffyCombat combat;
	public DeathListener(FluffyCombat combat){
		this.combat = combat;
	}

	public FluffyCombat combat() {
		return combat;
	}


}
