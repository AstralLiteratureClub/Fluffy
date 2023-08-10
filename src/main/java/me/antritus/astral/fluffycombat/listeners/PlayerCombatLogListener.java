package me.antritus.astral.fluffycombat.listeners;

import me.antritus.astral.fluffycombat.FluffyCombat;
import me.antritus.astral.fluffycombat.antsfactions.Property;
import me.antritus.astral.fluffycombat.api.CombatUser;
import me.antritus.astral.fluffycombat.manager.CombatManager;
import me.antritus.astral.fluffycombat.manager.UserManager;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityResurrectEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.jetbrains.annotations.NotNull;

public class PlayerCombatLogListener implements Listener {
	private final FluffyCombat combat;

	public PlayerCombatLogListener(FluffyCombat combat) {
		this.combat = combat;
	}

	@EventHandler
	public void onQuit(@NotNull PlayerQuitEvent event){
		Player player = event.getPlayer();
		CombatManager cM = combat.getCombatManager();
		UserManager uM = combat.getUserManager();
		if (cM.hasTags(player)){
			CombatUser user = uM.getUser(event.getPlayer());
			assert user != null;
			user.setting("logged", true);
			player.setHealth(0);
		}
	}

	@EventHandler
	public void onTotemResurrect(EntityResurrectEvent event){
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

	@EventHandler
	public void onDeath(PlayerDeathEvent event){
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
