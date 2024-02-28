package bet.astral.fluffy.listeners;

import bet.astral.fluffy.FluffyCombat;
import com.destroystokyo.paper.event.player.PlayerArmorChangeEvent;
import com.jeff_media.armorequipevent.ArmorEquipEvent;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerInteractEvent;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class ArmorChangeListener implements Listener {
	private final FluffyCombat fluffy;

	public ArmorChangeListener(FluffyCombat fluffy) {
		this.fluffy = fluffy;
	}

	@EventHandler(ignoreCancelled = true, priority = EventPriority.HIGHEST)
	public void onArmorHotSwapEvent(PlayerInteractEvent event) {
		if (PlayerArmorChangeEvent.SlotType.isEquipable(event.getMaterial())) {
			if (!fluffy.getCombatConfig().isArmorHotSwapAllowed()) {
				event.setCancelled(true);
			}
		}
	}


	@EventHandler(ignoreCancelled = true, priority = EventPriority.HIGHEST)
	public void onArmorEquipEvent(ArmorEquipEvent event) {
		Set<ArmorEquipEvent.EquipMethod> methods = new HashSet<>(List.of(
				ArmorEquipEvent.EquipMethod.SHIFT_CLICK,
				ArmorEquipEvent.EquipMethod.DRAG,
				ArmorEquipEvent.EquipMethod.PICK_DROP,
				ArmorEquipEvent.EquipMethod.HOTBAR_SWAP,
				ArmorEquipEvent.EquipMethod.DISPENSER));
		if (methods.contains(event.getMethod())){
			event.setCancelled(!fluffy.getCombatConfig().isArmorChangeAllowed());
		} else if (event.getMethod() == ArmorEquipEvent.EquipMethod.HOTBAR){
			if (event.getOldArmorPiece() != null){
				if (!fluffy.getCombatConfig().isArmorHotSwapAllowed()) {
					event.setCancelled(true);
				}
			} else if (!fluffy.getCombatConfig().isArmorHotBarEquipAllowed()){
				event.setCancelled(true);
			}
		}
	}
}
