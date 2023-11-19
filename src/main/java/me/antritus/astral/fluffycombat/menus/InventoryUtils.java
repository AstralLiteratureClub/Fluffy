package me.antritus.astral.fluffycombat.menus;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;

import java.util.LinkedHashMap;
import java.util.Map;

public class InventoryUtils {
	private InventoryUtils() {}

	public static Inventory createInventory(InventoryHolder holder, String name, InventoryType type){
		//noinspection deprecation
		return Bukkit.createInventory(holder, type, name);
	}
	public static Inventory createInventory(InventoryHolder holder, String name, int size){
		if (size>6||size<1){
			throw new IllegalArgumentException("size can only be between 1 and 6");
		}
		//noinspection deprecation
		return Bukkit.createInventory(holder, size * 9, name);
	}
	public static void setSlots(Inventory inventory, ItemStack item, int... slots){
		for (int slot : slots){
			inventory.setItem(slot, item);
		}
	}
	public static void setSlots(Inventory inventory, Material material, int... slots){
		ItemStack item = new ItemStack(material);
		for (int slot : slots){
			inventory.setItem(slot, item);
		}
	}

	public static void setBorders(Inventory inventory, ItemStack... borderItems) {
		int borderIndex = 0;
		int endSlots = inventory.getSize() - 9;

		for (int i = 0; i < inventory.getSize(); i++) {
			if (endSlots <= i) {
				if ((i > 9 && i < 17) || (i > 18 && i < 26) || (i > 27 && i < 35) || (i > 36 && i < 44)) {
					i++;
					continue;
				}
			}
			try {
				inventory.setItem(i, borderItems[borderIndex]);
				borderIndex = (borderIndex + 1) % borderItems.length;
			} catch (IndexOutOfBoundsException e) {
				borderIndex = 1;
				inventory.setItem(i, borderItems[0]);
			}
		}
	}

	public static boolean isWithinBorder(Inventory inventory, int slot) {
		int inventorySize = inventory.getSize();
		int hotbarSize = 9;
		int lastRowSize = 9;

		if (slot < hotbarSize || slot >= inventorySize - lastRowSize) {
			return false;
		}

		int[] rows = {10, 16,
				19, 25,
				28, 34,
				37, 43
		};
		int[] bottomRowStarts = { 28, 37, 46, 55 };

		for (int i = 0; i < rows.length; i+=2){
			if (slot >= rows[i] && slot <= rows[i+1] && slot < (i < bottomRowStarts.length ? bottomRowStarts[i] : inventorySize)) {
				return true;
			}
		}
		return false;
	}
	public static InventoryPattern makePattern(String... patternRows) {
		return new InventoryPattern(patternRows);
	}

	public static void setPattern(Inventory inventory, InventoryPattern pattern) {
		pattern.update(inventory);
	}

	public static class InventoryPattern {
		private final String[] patternRows;
		private final Map<Character, ItemStack> patternReplacements = new LinkedHashMap<>();

		public InventoryPattern(String... patternRows) {
			this.patternRows = patternRows;
			patternReplacements.put(' ', new ItemStack(Material.AIR));
		}

		public void setItem(char character, ItemStack itemStack) {
			patternReplacements.put(character, itemStack);
		}

		public void setItem(char character, Material material) {
			patternReplacements.put(character, new ItemStack(material));
		}

		public void setPattern(int row, String patternRow) {
			this.patternRows[row] = patternRow;
		}

		public void update(Inventory inventory) {
			for (int rowIndex = 0; rowIndex < patternRows.length; rowIndex++) {
				String patternRow = patternRows[rowIndex];
				if (patternRow == null) {
					continue;
				}

				char[] pattern = patternRow.toCharArray();
				for (int colIndex = 0; colIndex < pattern.length && colIndex < 9; colIndex++) {
					inventory.setItem(rowIndex * 9 + colIndex, patternReplacements.get(pattern[colIndex]));
				}
			}
		}
	}
}