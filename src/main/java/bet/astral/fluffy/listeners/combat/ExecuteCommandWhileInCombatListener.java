package bet.astral.fluffy.listeners.combat;

import bet.astral.fluffy.FluffyCombat;
import bet.astral.fluffy.manager.CombatManager;
import bet.astral.fluffy.messenger.Translations;
import bet.astral.messenger.v2.placeholder.Placeholder;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;

public class ExecuteCommandWhileInCombatListener implements Listener {
	private final FluffyCombat fluffy;
	public ExecuteCommandWhileInCombatListener(FluffyCombat fluffy){
		this.fluffy = fluffy;
	}
	@EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
	private void onCommandPrepare(PlayerCommandPreprocessEvent event){
		if (!fluffy.getCombatConfig().isCommandsDisabled()){
			return;
		}

		CombatManager cM = fluffy.getCombatManager();
		Player player = event.getPlayer();
		if (cM.hasTags(player)) {
			if (player.hasPermission("fluffy.bypass.commands")){
				return;
			}
			final String finalCommand = event.getMessage();
			if (fluffy.getCombatConfig().getCommandsToDisable().stream().
					anyMatch(
							cmd -> cmd.toLowerCase().startsWith(finalCommand)
					)
			) {
				event.setCancelled(true);
				fluffy.getMessenger().message(player, Translations.COMBAT_CANNOT_USE_COMMANDS, Placeholder.of("command", event.getMessage()));
			}
		}
	}
}
