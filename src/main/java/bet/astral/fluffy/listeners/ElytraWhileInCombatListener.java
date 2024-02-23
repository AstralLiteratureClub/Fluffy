package bet.astral.fluffy.listeners;

import bet.astral.fluffy.messenger.MessageKey;
import bet.astral.fluffy.messenger.Placeholders;
import com.destroystokyo.paper.event.player.PlayerElytraBoostEvent;
import bet.astral.fluffy.FluffyCombat;
import bet.astral.fluffy.manager.CombatManager;
import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityToggleGlideEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.inventory.ItemStack;

import java.util.HashMap;
import java.util.Map;

public class ElytraWhileInCombatListener implements Listener {
	private final Map<Player, ItemStack> elytras = new HashMap<>();
	private final FluffyCombat fluffy;

	public ElytraWhileInCombatListener(FluffyCombat fluffy) {
		this.fluffy = fluffy;
	}

	@EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
	public void onDeath(PlayerDeathEvent event){
		if (elytras.get(event.getEntity()) != null){
			ItemStack itemStack = elytras.get(event.getEntity());
			ItemStack replacementElytra = FluffyCombat.convertElytraWithReplacer(itemStack);
			if (replacementElytra == null){
				return;
			}
			event.getDrops().removeIf(item->item.getAmount()==replacementElytra.getAmount() && replacementElytra.isSimilar(replacementElytra));

			if (itemStack.getEnchantmentLevel(Enchantment.VANISHING_CURSE)>0){
				elytras.remove(event.getEntity());
				return;
			}
			if (event.getKeepInventory()){
				return;
			}
			event.getDrops().add(itemStack);
			elytras.remove(event.getEntity());
		}
	}

	@EventHandler(priority = EventPriority.LOWEST)
	public void onQuit(PlayerQuitEvent event){
		if (elytras.get(event.getPlayer()) != null){
			ItemStack itemStack = elytras.get(event.getPlayer());
			event.getPlayer().getInventory().setChestplate(itemStack);
		}
	}

	@EventHandler
	public void onEntityToggleGlide(EntityToggleGlideEvent e) {
		if (!(e.getEntity() instanceof Player player)) return;
		ItemStack itemStack = player.getInventory().getChestplate();
		if (itemStack == null || itemStack.getType() != Material.ELYTRA){
			return;
		}
		if (fluffy.getCombatConfig().isElytraAllowed()){
			return;
		}
		CombatManager combatManager = fluffy.getCombatManager();
		if (!combatManager.hasTags(player)) {
			return;
		}
		if (!player.isGliding() &&
				fluffy.getCombatConfig().isElytraMessage()) {
			fluffy.getMessageManager().message(player, MessageKey.COMBAT_USE_ITEM_ELYTRA_GLIDE, Placeholders.playerPlaceholders("player", player));
		}

		e.setCancelled(true);
		player.setGliding(false);
		fluffy.getServer().getScheduler().runTaskLater(fluffy,
				() -> player.setGliding(false), 2);
		elytras.put(player, itemStack);
		player.getInventory().setChestplate(FluffyCombat.convertElytraWithReplacer(itemStack));

		fluffy.getServer().getScheduler().runTaskLater(fluffy, () -> {
			if (!elytras.containsKey(player)){
				return;
			}
			player.getInventory().setChestplate(itemStack);
			elytras.remove(player);
		}, 25);
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
			}
			fluffy.getMessageManager()
					.message(player, MessageKey.COMBAT_USE_ITEM_ELYTRA_ROCKET_BOOST, Placeholders.playerPlaceholders("player", player));
		}
	}
}
