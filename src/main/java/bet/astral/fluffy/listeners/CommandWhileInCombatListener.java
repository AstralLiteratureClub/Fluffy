package bet.astral.fluffy.listeners;

import bet.astral.fluffy.FluffyCombat;
import bet.astral.fluffy.manager.CombatManager;
import bet.astral.messenger.placeholder.LegacyPlaceholder;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;

public class CommandWhileInCombatListener implements Listener {
	private final FluffyCombat fluffy;
	public CommandWhileInCombatListener(FluffyCombat fluffy){
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
