package me.antritus.astral.fluffycombat.listeners;

import me.antritus.astral.fluffycombat.FluffyCombat;
import me.antritus.astral.fluffycombat.antsfactions.Property;
import me.antritus.astral.fluffycombat.api.CombatUser;
import me.antritus.astral.fluffycombat.api.events.CombatLogEvent;
import me.antritus.astral.fluffycombat.manager.CombatManager;
import me.antritus.astral.fluffycombat.manager.UserManager;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityResurrectEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.jetbrains.annotations.NotNull;

public class PlayerQuitListener implements Listener {
	private final FluffyCombat combat;

	public PlayerQuitListener(FluffyCombat combat) {
		this.combat = combat;
	}

	@EventHandler(priority = EventPriority.HIGHEST)
	public void onQuit(@NotNull PlayerQuitEvent event){
		if (FluffyCombat.isStopping){
			return;
		}
		Player player = event.getPlayer();
		CombatManager cM = combat.getCombatManager();
		UserManager uM = combat.getUserManager();
		if (cM.hasTags(player)){
			CombatUser user = uM.getUser(event.getPlayer());
			CombatLogEvent logEvent = new CombatLogEvent(combat, player, cM.getTags(player));
			if (logEvent.isCancelled()) {
				return;
			}
			assert user != null;
			user.setting("logged", true);
			user.setting("combat-log", true);
			if (combat.getConfig()
					.getBoolean("kill-on-log", false)) {
				player.setHealth(0);
			}
		}
	}

	@EventHandler(priority = EventPriority.HIGHEST)
	public void onTotemResurrect(EntityResurrectEvent event){
		if (FluffyCombat.isStopping){
			return;
		}
		if (event.getEntity() instanceof Player player){
			UserManager uM = combat.getUserManager();
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

	@EventHandler(priority = EventPriority.NORMAL)
	public void onDeath(PlayerDeathEvent event){
		if (FluffyCombat.isStopping){
			return;
		}
		Player player = event.getEntity();
		UserManager uM = combat.getUserManager();
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
