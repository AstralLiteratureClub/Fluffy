package me.antritus.astral.fluffycombat.listeners;

import com.destroystokyo.paper.event.player.PlayerElytraBoostEvent;
import me.antritus.astral.fluffycombat.FluffyCombat;
import me.antritus.astral.fluffycombat.manager.CombatManager;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityToggleGlideEvent;
import org.bukkit.inventory.ItemStack;

public class ElytraListener implements Listener {
	private final FluffyCombat fluffy;

	public ElytraListener(FluffyCombat fluffy) {
		this.fluffy = fluffy;
		/*
		fluffy.getServer().getScheduler().runTaskTimer(fluffy, () -> {
			CombatManager combatManager = fluffy.getCombatManager();
			for (Player player : Bukkit.getOnlinePlayers()){
				if (combatManager.hasTags(player)) {
					if (fluffy.combatConfig().isElytraAllowed()){
						return;
					}
					if (player.isGliding()) {
						player.setVelocity(player.getVelocity().multiply(0));
						player.setGliding(false);
						if (fluffy.combatConfig().isElytraMessage()) {
							fluffy.getMessageManager().message(player, "elytra.glide");
						}
					}
				}
			}
		}, 20, 1);

		 */
	}

	@EventHandler
	public void onEntityToggleGlide(EntityToggleGlideEvent e) {
		if (fluffy.getCombatConfig().isElytraAllowed()){
			return;
		}
		if (!(e.getEntity() instanceof Player player)) return;
		CombatManager combatManager = fluffy.getCombatManager();
		if (!combatManager.hasTags(player)) {
			return;
		}
		if (!player.isGliding() &&
				fluffy.getCombatConfig().isElytraMessage()) {
			fluffy.getMessageManager().message(player, "elytra.glide");
		}
		e.setCancelled(true);
		player.setGliding(false);

		ItemStack itemStack = player.getInventory().getChestplate();
		player.getInventory().setChestplate(null);
		fluffy.getServer().getScheduler().runTaskLater(fluffy, () -> player.getInventory().setChestplate(itemStack), 20);
	}

	@EventHandler
	public void onElytraBoost(PlayerElytraBoostEvent event){
		if (fluffy.getCombatConfig().isElytraBoostAllowed()){
			return;
		}
		CombatManager combatManager = fluffy.getCombatManager();
		Player player = event.getPlayer();
		if (combatManager.hasTags(player)){
			event.setCancelled(true);
			event.setShouldConsume(false);
			if (fluffy.getCombatConfig().isElytraBoostMessage()){
				fluffy.getMessageManager()
						.message(player, "elytra.rocket-boost");
			}
		}
	}
}
