package me.antritus.astral.fluffycombat.menus;

import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.jetbrains.annotations.NotNull;

public class SettingsMenu {

	private class Menu implements InventoryHolder {
		@Override
		public @NotNull Inventory getInventory() {
			return null;
		}
	}
}
