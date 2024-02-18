package bet.astral.fluffy.listeners;

import bet.astral.fluffy.messenger.Placeholders;
import bet.astral.fluffy.FluffyCombat;
import bet.astral.fluffy.api.CombatUser;
import bet.astral.fluffy.api.events.CombatLogEvent;
import bet.astral.fluffy.configs.CombatConfig;
import bet.astral.fluffy.manager.CombatManager;
import bet.astral.fluffy.manager.UserManager;
import bet.astral.messenger.placeholder.Placeholder;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityResurrectEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public class PlayerExitWhileInCombatListener implements Listener {
	private final FluffyCombat fluffy;

	public PlayerExitWhileInCombatListener(FluffyCombat fluffy) {
		this.fluffy = fluffy;
	}

	@EventHandler
	public void onJoin(PlayerJoinEvent event) {
		Player player = event.getPlayer();
		boolean logged = fluffy.getCombatLogManager().hasCombatLogged(player); // Automatically deleted
		if (logged){
			CombatConfig combatConfig = fluffy.getCombatConfig();
			Placeholder[] placeholders = Placeholders.playerPlaceholders("player", player).toArray(Placeholder[]::new);
			if (combatConfig.isCombatLogRejoinBroadcast())
				fluffy.getMessageManager().broadcast("combat-log.join.broadcast", placeholders);
			if (combatConfig.isCombatLogRejoinPrivateMessage())
				fluffy.getMessageManager().message(player, "combat-log.join.broadcast", placeholders);
		}
	}

	@EventHandler
	public void onQuit(@NotNull PlayerQuitEvent event) {
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
			if (fluffy.getCombatConfig().isCombatLogBroadcast()){
				List<Placeholder> placeholders = Placeholders.playerPlaceholders("player", player);
				Placeholder[] placeholderArray = placeholders.toArray(Placeholder[]::new);
				fluffy.getMessageManager().broadcast("combat-log.quit.broadcast",
						placeholderArray);
			}
			if (fluffy.getCombatConfig().getCombatLogAction() == CombatConfig.CombatLogAction.NOTHING){
				return;
			} else if (fluffy.getCombatConfig().getCombatLogAction() == CombatConfig.CombatLogAction.SPAWN_NPC){
				//TODO
				return;
			} else if (fluffy.getCombatConfig().getCombatLogAction() == CombatConfig.CombatLogAction.KILL) {
				assert user != null;
				user.setting("logged", true);
				user.setting("combat-log", true);
				player.setHealth(0);
			}
		}
	}

	@SuppressWarnings("removal")
	@EventHandler(priority = EventPriority.HIGHEST)
	public void onTotemResurrect(EntityResurrectEvent event){
		if (FluffyCombat.isStopping){
			return;
		}
		if (!fluffy.getCombatConfig().isCombatLogKillTotemBypass()){
			event.setCancelled(true);
		}
		if (fluffy.getCombatConfig().getCombatLogAction() != CombatConfig.CombatLogAction.KILL) {
			return;
		}
		if (event.getEntity() instanceof Player player){
			if (player.hasPermission("fluffy.bypass.combat-log")){
				return;
			}
			UserManager uM = fluffy.getUserManager();
			CombatUser user = uM.getUser(player.getUniqueId());
			assert user != null;
			if (user.getTotemCounter() == 0){
				event.setCancelled(true);
			}
			if (user.getTotemCounter()>-1){
				user.setTotemCounter(user.getTotemCounter());
			}
			Object property = user.get("logged");
			if (property != null){
				if (property instanceof Boolean && (boolean) property) {
					player.setHealth(0);
				}
			}
		}
	}

	@SuppressWarnings("removal")
	@EventHandler(priority = EventPriority.NORMAL)
	public void onDeath(PlayerDeathEvent event) {
		if (FluffyCombat.isStopping) {
			return;
		}
		if (fluffy.getCombatConfig().getCombatLogAction() != CombatConfig.CombatLogAction.KILL) {
			return;
		}

		Player player = event.getEntity();
		if (player.hasPermission("fluffy.bypass.combat-log")) {
			return;
		}
		UserManager uM = fluffy.getUserManager();
		CombatUser user = uM.getUser(player);
		assert user != null;
		Object property = user.get("logged");
		if (property != null) {
			if (property instanceof Boolean && (boolean) property) {
				user.setting("logged", false);
				CombatConfig config = fluffy.getCombatConfig();
				if (!config.isCombatLogKillKeepExp())
					event.setKeepLevel(false);
				if (!config.isCombatLogKillKeepItem())
					event.setKeepInventory(false);
			}
		}
	}
}
