package bet.astral.fluffy.listeners.combat;

import bet.astral.fluffy.messenger.MessageKey;
import bet.astral.fluffy.messenger.Placeholders;
import bet.astral.fluffy.FluffyCombat;
import bet.astral.fluffy.api.CombatUser;
import bet.astral.fluffy.events.CombatLogEvent;
import bet.astral.fluffy.configs.CombatConfig;
import bet.astral.fluffy.manager.CombatManager;
import bet.astral.fluffy.manager.UserManager;
import bet.astral.messenger.placeholder.Placeholder;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.Objects;

public class QuitWhileInCombatListener implements Listener {
	private final FluffyCombat fluffy;

	public QuitWhileInCombatListener(FluffyCombat fluffy) {
		this.fluffy = fluffy;
	}

	@EventHandler
	private void onJoin(PlayerJoinEvent event) {
		Player player = event.getPlayer();
		boolean logged = fluffy.getCombatLogManager().hasCombatLogged(player); // Automatically deleted
		if (logged){
			Placeholder[] placeholders = Placeholders.playerPlaceholders("player", player).toArray(Placeholder[]::new);
			if (Objects.requireNonNull(fluffy.getCombatConfig().getCombatLogAction()) == CombatConfig.CombatLogAction.SPAWN_NPC) {
				// TODO - Make the message chosen if the NPC is alive or dead.
//					fluffy.getMessageManager().broadcast(MessageKey.COMBAT_REJOIN_NPC_REPLACEMENT_DEAD_BROADCAST, placeholders);
				fluffy.getMessageManager().broadcast(MessageKey.COMBAT_REJOIN_NPC_REPLACEMENT_ALIVE_BROADCAST, placeholders);
//					fluffy.getMessageManager().message(player, MessageKey.COMBAT_REJOIN_NPC_REPLACEMENT_DEAD, placeholders);
				fluffy.getMessageManager().message(player, MessageKey.COMBAT_REJOIN_NPC_REPLACEMENT_ALIVE, placeholders);
			} else {
				fluffy.getMessageManager().broadcast(MessageKey.COMBAT_REJOIN_BROADCAST, placeholders);
				fluffy.getMessageManager().message(player, MessageKey.COMBAT_REJOIN_PLAYER, placeholders);
			}
		}
	}

	@EventHandler
	private void onQuit(@NotNull PlayerQuitEvent event) {
		if (FluffyCombat.isStopping) {
			return;
		}
		Player player = event.getPlayer();
		if (player.hasPermission("fluffy.bypass.combat-log")) {
			return;
		}
		CombatManager cM = fluffy.getCombatManager();
		UserManager uM = fluffy.getUserManager();
		if (cM.hasTags(player)) {
			CombatUser user = uM.getUser(event.getPlayer());
			CombatLogEvent logEvent = new CombatLogEvent(fluffy, player, cM.getTags(player));
			if (logEvent.isCancelled()) {
				return;
			}
			List<Placeholder> placeholders = Placeholders.playerPlaceholders("player", player);
			Placeholder[] placeholderArray = placeholders.toArray(Placeholder[]::new);
			fluffy.getMessageManager().broadcast(MessageKey.COMBAT_LOG_BROADCAST, placeholderArray);

			if (fluffy.getCombatConfig().getCombatLogAction() == CombatConfig.CombatLogAction.NOTHING){
				return;
			} else if (fluffy.getCombatConfig().getCombatLogAction() == CombatConfig.CombatLogAction.SPAWN_NPC){
				fluffy.getMessageManager().broadcast(MessageKey.COMBAT_LOG_NPC_SPAWN_BROADCAST, placeholderArray);
				//TODO - Make the NPC spawning possible using hooks for the plugin
				return;
			} else if (fluffy.getCombatConfig().getCombatLogAction() == CombatConfig.CombatLogAction.KILL) {
				boolean diedOnce = false;
				assert user != null;
				while (!player.isDead()){
					if (diedOnce && fluffy.getCombatConfig().isCombatLogKillTotemBypass()){
						return;
					} else if (diedOnce){
						if (user.getTotemCounter() == 0){
							return;
						}
						if (user.getTotemCounter()>-1) {
							user.setTotemCounter(user.getTotemCounter()-1);
						}
					}
					player.setHealth(0);
					if (player.isDead()){
						user.setting("logged", true);
					}
					diedOnce = true;
				}
			}
		}
	}


	@SuppressWarnings("removal")
	@EventHandler(priority = EventPriority.NORMAL)
	private void onDeath(PlayerDeathEvent event) {
		if (FluffyCombat.isStopping) {
			return;
		}
		if (fluffy.getCombatConfig().getCombatLogAction() != CombatConfig.CombatLogAction.KILL) {
			return;
		}

		Player player = event.getEntity();
		UserManager uM = fluffy.getUserManager();
		CombatUser user = uM.getUser(player);
		assert user != null;
		Object property = user.get("logged");
		if (property != null) {
			if (property instanceof Boolean && (boolean) property) {
				user.setting("logged", false);
				CombatConfig config = fluffy.getCombatConfig();
				event.setKeepLevel(config.isCombatLogKillKeepExp());
				event.setKeepInventory(config.isCombatLogKillKeepItem());
			}
		}
	}
}
