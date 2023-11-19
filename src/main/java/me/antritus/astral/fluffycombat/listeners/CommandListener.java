package me.antritus.astral.fluffycombat.listeners;

import bet.astral.messagemanager.placeholder.LegacyPlaceholder;
import me.antritus.astral.fluffycombat.FluffyCombat;
import me.antritus.astral.fluffycombat.manager.CombatManager;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;

public class CommandListener implements Listener {
	private final FluffyCombat fluffy;
	public CommandListener(FluffyCombat fluffy){
		this.fluffy = fluffy;
	}
	@EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
	public void onCommandPrepare(PlayerCommandPreprocessEvent event){
		if (!fluffy.getCombatConfig().isCommandsDisabled()){
			return;
		}

		CombatManager cM = fluffy.getCombatManager();
		Player player = event.getPlayer();
		if (cM.hasTags(player)) {
			if (player.hasPermission("fluffy.bypass.commands")){
				return;
			}
			String command = event.getMessage();
			if (command.contains(" ")) {
				command = command.split(" ")[0];
			}
			command = command.replace("/", "");
			final String finalCommand = command;
			if (fluffy.getCombatConfig().getCommandsToDisable().stream().
					anyMatch(
							cmd -> cmd.equalsIgnoreCase(finalCommand)
					)
			) {
				event.setCancelled(true);
				fluffy.getMessageManager().message(player, "combat-command", new LegacyPlaceholder("command", event.getMessage()));
			}
		}
	}
}
