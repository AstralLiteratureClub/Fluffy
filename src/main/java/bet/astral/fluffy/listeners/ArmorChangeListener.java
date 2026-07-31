package bet.astral.fluffy.listeners;

import bet.astral.fluffy.FluffyCombat;
import bet.astral.fluffy.api.CombatTag;
import com.jeff_media.armorequipevent.ArmorEquipEvent;
import io.papermc.paper.datacomponent.DataComponentTypes;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class ArmorChangeListener implements Listener {
	private final FluffyCombat fluffy;

	public ArmorChangeListener(FluffyCombat fluffy) {
		this.fluffy = fluffy;
	}

	@EventHandler(ignoreCancelled = true, priority = EventPriority.HIGHEST)
	private void onArmorHotSwapEvent(PlayerInteractEvent event) {
		CombatTag tag = fluffy.getCombatManager().getLatest(event.getPlayer());
		if (tag == null) {
			return;
		}
		ItemStack item = event.getPlayer().getInventory().getItemInMainHand();
		if (item.hasData(DataComponentTypes.EQUIPPABLE)) {
			if (!fluffy.getCombatConfig().isArmorHotSwapAllowed()) {
				event.setCancelled(true);
			}
		}
	}


	@EventHandler(ignoreCancelled = true, priority = EventPriority.HIGHEST)
	private void onArmorEquipEvent(ArmorEquipEvent event) {
		CombatTag tag = fluffy.getCombatManager().getLatest(event.getPlayer());
		if (tag == null) {
			return;
		}

		Set<ArmorEquipEvent.EquipMethod> methods = new HashSet<>(List.of(
				ArmorEquipEvent.EquipMethod.SHIFT_CLICK,
				ArmorEquipEvent.EquipMethod.DRAG,
				ArmorEquipEvent.EquipMethod.PICK_DROP,
				ArmorEquipEvent.EquipMethod.HOTBAR_SWAP,
				ArmorEquipEvent.EquipMethod.DISPENSER));
		if (methods.contains(event.getMethod())) {
			event.setCancelled(!fluffy.getCombatConfig().isArmorChangeAllowed());
		} else if (event.getMethod() == ArmorEquipEvent.EquipMethod.HOTBAR) {
			if (event.getOldArmorPiece() != null) {
				if (!fluffy.getCombatConfig().isArmorHotSwapAllowed()) {
					event.setCancelled(true);
				}
			} else if (!fluffy.getCombatConfig().isArmorHotBarEquipAllowed()) {
				event.setCancelled(true);
			}
		}
	}

	/*
	@EventHandler
	public void onArmorEquip(bet.astral.fluffy.armorer.ArmorEquipEvent event) {
		event.getPlayer().sendMessage("§e============ ARMOR EVENT ============");
		event.getPlayer().sendMessage("§bMethod: §f" + event.getMethod().name());
		event.getPlayer().sendMessage("§bSlot: §f" + event.getArmorType().name());
		event.getPlayer().sendMessage("§bAction: §f" +
				(event.isEquipping() ? "EQUIPPING" :
						event.isUnequipping() ? "UNEQUIPPING" : "SWAPPING"));

		if (event.getOldArmorPiece() != null) {
			event.getPlayer().sendMessage("§cOld: §f" + event.getOldArmorPiece().getType());
		}
		if (event.getNewArmorPiece() != null) {
			event.getPlayer().sendMessage("§aNew: §f" + event.getNewArmorPiece().getType());
		}

		// Example: Cancel if player tries to equip leather armor via dispenser
		if (event.getMethod() == bet.astral.fluffy.armorer.ArmorEquipEvent.EquipMethod.DISPENSER &&
				event.getNewArmorPiece() != null &&
				event.getNewArmorPiece().getType().name().contains("LEATHER")) {
			event.setCancelled(true);
			event.getPlayer().sendMessage("§cYou cannot equip leather armor via dispenser!");
		}
	}

	 */
}
