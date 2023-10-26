package me.antritus.astral.fluffycombat.listeners;

import me.antritus.astral.fluffycombat.FluffyCombat;
import me.antritus.astral.fluffycombat.manager.CombatManager;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;

public class CommandListener implements Listener {
	private final FluffyCombat combat;
	public CommandListener(FluffyCombat combat){
		this.combat = combat;
	}
	@EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
	public void onCommandPrepare(PlayerCommandPreprocessEvent event){
		CombatManager cM = combat.getCombatManager();
		Player player = event.getPlayer();
		if (cM.hasTags(player)) {
			String command = event.getMessage();
			if (command.contains(" ")) {
				command = command.split(" ")[0];
			}
			command = command.replace("/", "");
			final String finalCommand = command;
			if (combat.getConfig().getStringList("blocked-commands").stream().
					anyMatch(
							cmd -> cmd.equalsIgnoreCase(finalCommand)
					)
			) {
				event.setCancelled(true);
				combat.getMessageManager().message(player, "combat-command", "%command%="+event.getMessage());
			}
		}
	}
}
