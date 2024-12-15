package bet.astral.fluffy.listeners.combat.mobility;

import bet.astral.fluffy.configs.CombatConfig;
import bet.astral.fluffy.messenger.Translations;
import bet.astral.fluffy.utils.ItemStackUtils;
import com.destroystokyo.paper.event.player.PlayerElytraBoostEvent;
import bet.astral.fluffy.FluffyCombat;
import bet.astral.fluffy.manager.CombatManager;
import io.papermc.paper.datacomponent.DataComponentTypes;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityToggleGlideEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.PlayerAttemptPickupItemEvent;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.inventory.ItemStack;

public class ElytraWhileInCombatListener implements Listener {
	private final FluffyCombat fluffy;

	public ElytraWhileInCombatListener(FluffyCombat fluffy) {
		this.fluffy = fluffy;
	}

	@EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
	private void onDeath(PlayerDeathEvent event){
		event.getDrops().forEach(item->{
			if (item.getPersistentDataContainer().has(FluffyCombat.ELYTRA_KEY)) {
				ItemStackUtils.resetValue(item, FluffyCombat.ELYTRA_KEY);
			}
		});
	}

	@EventHandler(priority = EventPriority.HIGHEST)
	private void onQuit(PlayerQuitEvent event){
		for (ItemStack itemStack : event.getPlayer().getInventory().getContents()){
			if (itemStack == null || itemStack.isEmpty()){
				continue;
			}
			ItemStackUtils.resetValue(itemStack, FluffyCombat.ELYTRA_KEY);
		}
	}

	@EventHandler
	private void onItemDrop(PlayerDropItemEvent event){
		if (event.getItemDrop().getPersistentDataContainer().has(FluffyCombat.ELYTRA_KEY)){
			ItemStackUtils.resetValue(event.getItemDrop().getItemStack(), FluffyCombat.ELYTRA_KEY);
		}
	}
	@EventHandler
	private void onItemPickup(PlayerAttemptPickupItemEvent event){
		if (event.getItem().getPersistentDataContainer().has(FluffyCombat.ELYTRA_KEY)){
			ItemStackUtils.resetValue(event.getItem().getItemStack(), FluffyCombat.ELYTRA_KEY);
		}
	}


	@EventHandler
	private void onEntityToggleGlide(EntityToggleGlideEvent e) {
		if (!(e.getEntity() instanceof Player player)) return;
		ItemStack itemStack = player.getInventory().getChestplate();
		if (itemStack == null || !itemStack.hasData(DataComponentTypes.GLIDER)) {
			return;
		}
		if (fluffy.getCombatConfig().getElytraMode() != CombatConfig.ElytraMode.ALLOW) {
			return;
		}
		CombatManager combatManager = fluffy.getCombatManager();
		if (!combatManager.hasTags(player)) {
			return;
		}
		if (!player.isGliding()) {
			fluffy.getMessenger().message(player, Translations.COMBAT_CANNOT_USE_ELYTRA);
		}

		e.setCancelled(true);
		player.setGliding(false);
		fluffy.getServer().getScheduler().runTaskLater(fluffy,
				() -> player.setGliding(false), 2);
		player.getInventory().setChestplate(FluffyCombat.convertElytraWithReplacer(itemStack, fluffy.getCombatConfig().getElytraMode()));

		fluffy.getServer().getScheduler().runTaskLater(fluffy, () -> {
			for (ItemStack item : player.getInventory().getContents()){
				if (item == null || item.isEmpty() || !item.getPersistentDataContainer().has(FluffyCombat.ELYTRA_KEY)){
					continue;
				}
				ItemStackUtils.resetValue(item, FluffyCombat.ELYTRA_KEY);
			}
		}, 25);
	}

	@EventHandler
	private void onElytraBoost(PlayerElytraBoostEvent event){
		if (fluffy.getCombatConfig().isElytraBoostAllowed()){
			return;
		}
		CombatManager combatManager = fluffy.getCombatManager();
		Player player = event.getPlayer();
		if (combatManager.hasTags(player)){
			event.setCancelled(true);
			event.setShouldConsume(false);
			fluffy.getMessenger()
					.message(player, Translations.COMBAT_CANNOT_USE_ELYTRA_BOOST);
		}
	}
}
