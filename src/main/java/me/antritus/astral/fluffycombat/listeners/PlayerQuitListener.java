package me.antritus.astral.fluffycombat.listeners;

import me.antritus.astral.fluffycombat.FluffyCombat;
import me.antritus.astral.fluffycombat.antsfactions.Property;
import me.antritus.astral.fluffycombat.api.CombatUser;
import me.antritus.astral.fluffycombat.api.events.CombatLogEvent;
import me.antritus.astral.fluffycombat.configs.CombatConfig;
import me.antritus.astral.fluffycombat.manager.CombatManager;
import me.antritus.astral.fluffycombat.manager.UserManager;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityResurrectEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.jetbrains.annotations.NotNull;

import java.util.LinkedList;
import java.util.List;

public class PlayerQuitListener implements Listener {
	private final MiniMessage miniMessage = MiniMessage.miniMessage();
	private final FluffyCombat fluffy;
	private final List<String> loggers = new LinkedList<>();

	public PlayerQuitListener(FluffyCombat fluffy) {
		this.fluffy = fluffy;
	}

	@EventHandler
	public void onJoin(PlayerJoinEvent event) {
		Player player = event.getPlayer();
		if (loggers.stream().anyMatch(id->id.contentEquals(player.getUniqueId().toString()))){
			loggers.remove(player.getUniqueId().toString());
			CombatConfig combatConfig = fluffy.getCombatConfig();
			if (!combatConfig.isCombatLogRejoinEnabled()){
				return;
			}
			if (combatConfig.isCombatLogRejoinBroadcast()){
				fluffy.getMessageManager().broadcast(
						"combat-log.rejoin.broadcast",
						"%player%="+event.getPlayer().getName(),
						"%uniqueId%="+event.getPlayer().getUniqueId(),
						"%displayname%="+miniMessage.serialize(event.getPlayer().displayName()));
			}
			if (combatConfig.isCombatLogRejoinPrivateMessage()){
				fluffy.getMessageManager().message(player,
						"combat-log.rejoin.broadcast",
						"%player%="+event.getPlayer().getName(),
						"%uniqueId%="+event.getPlayer().getUniqueId(),
						"%displayname%="+miniMessage.serialize(event.getPlayer().displayName()));
			}
		}
	}

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
		if (!fluffy.getCombatConfig().isCombatLog()) {
			return;
		}
		if (cM.hasTags(player)) {
			CombatUser user = uM.getUser(event.getPlayer());
			CombatLogEvent logEvent = new CombatLogEvent(fluffy, player, cM.getTags(player));
			if (logEvent.isCancelled()) {
				return;
			}
			if (fluffy.getCombatConfig().isCombatLogBroadcast()){
				fluffy.getMessageManager().broadcast("combat-log.broadcast",
						"%player%="+event.getPlayer().getName(),
						"%uniqueId%="+event.getPlayer().getUniqueId(),
						"%displayname%="+miniMessage.serialize(event.getPlayer().displayName()));
			}
			if (!fluffy.getCombatConfig().isCombatLogKill()){
				return;
			}
			assert user != null;
			user.setting("logged", true);
			user.setting("combat-log", true);
			player.setHealth(0);
		}
	}

	@SuppressWarnings("removal")
	@EventHandler(priority = EventPriority.HIGHEST)
	public void onTotemResurrect(EntityResurrectEvent event){
		if (FluffyCombat.isStopping){
			return;
		}
		if (!fluffy.getCombatConfig().isCombatLog() || !fluffy.getCombatConfig().isCombatLogKill()) {
			return;
		}
		if (event.getEntity() instanceof Player player){
			if (player.hasPermission("fluffy.bypass.combat-log")){
				return;
			}
			UserManager uM = fluffy.getUserManager();
			CombatUser user = uM.getUser(player.getUniqueId());
			assert user != null;
			Property<String, ?> property = user.get("logged");
			if (property != null){
				if (property.getValue() != null){
					if (property.getValue()==Boolean.TRUE){
						player.setHealth(0);
					}
				}
			}
		}
	}

	@SuppressWarnings("removal")
	@EventHandler(priority = EventPriority.NORMAL)
	public void onDeath(PlayerDeathEvent event){
		if (FluffyCombat.isStopping){
			return;
		}
		if (!fluffy.getCombatConfig().isCombatLog() || !fluffy.getCombatConfig().isCombatLogKill()) {
			return;
		}

		Player player = event.getEntity();
		if (player.hasPermission("fluffy.bypass.combat-log")){
			return;
		}
		UserManager uM = fluffy.getUserManager();
		CombatUser user = uM.getUser(player);
		assert user != null;
		Property<String, ?> property = user.get("logged");
		if (property != null){
			if (property.getValue() != null){
				if (property.getValue()==Boolean.TRUE){
					user.setting("logged", false);
				}
			}
		}
	}
}
