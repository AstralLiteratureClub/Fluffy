package me.antritus.astral.fluffycombat.menus;

import lombok.Getter;
import me.antritus.astral.fluffycombat.FluffyCombat;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.HumanEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.scheduler.BukkitRunnable;
import org.jetbrains.annotations.NotNull;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ColorMenu implements Listener {
	private final FluffyCombat fluffy;
	private final Map<HumanEntity, ColorHolder> inventoryMap = new HashMap<>();

	public ColorMenu(FluffyCombat fluffy) {
		this.fluffy = fluffy;
	}

	public void createMenu(Player player, Inventory reOpen, ButtonType type, String name, Object instance, Field field){
		// 000000000
		// 00ROYLG00
		// 00LBPPB00
		// 000GGW000
		// 000000000
		ColorHolder glowMenu = new ColorHolder(fluffy, type, name, 5, reOpen, instance, field);
		inventoryMap.put(player, glowMenu);
		player.closeInventory(InventoryCloseEvent.Reason.CANT_USE);
		new BukkitRunnable() {
			@Override
			public void run() {
				player.openInventory(glowMenu.getInventory());
			}
		}.runTaskLater(fluffy, 1);
	}

	@EventHandler
	public void onInventoryClick(InventoryClickEvent event){
		if (event.getInventory() instanceof ColorHolder menu) {
			boolean correct = menu.onItemClick(event.getCurrentItem());
			if (correct){
				inventoryMap.remove(event.getWhoClicked());
			}
		}
	}

	@EventHandler
	public void onInventoryClose(InventoryCloseEvent event){
		if (event.getInventory() instanceof ColorHolder menu){
			if (inventoryMap.get(event.getPlayer()) != null){
				if (event.getReason()== InventoryCloseEvent.Reason.DISCONNECT){
					return;
				}
				if (event.getReason()== InventoryCloseEvent.Reason.TELEPORT){
					return;
				}
				if (event.getReason()== InventoryCloseEvent.Reason.PLUGIN){
					return;
				}
				if (event.getReason()== InventoryCloseEvent.Reason.DEATH){
					return;
				}
				event.getPlayer().openInventory(menu.getInventory());
			}
		}
	}


	public enum ButtonType {
		WOOL("%color%_WOOL"),
		GLASS("%color%_STAINED_GLASS"),
		GLASS_PANE("%color%_STAINED_GLASS_PANE"),
		CONCRETE("%color%_CONCRETE"),
		CONCRETE_POWDER("%color%_CONCRETE_POWDER"),
		DYE("%color%_DYE"),
		TERRACOTTA("%color%_TERRACOTTA")

		;
		final String materialIdFormat;

		ButtonType(String materialIdFormat) {
			this.materialIdFormat = materialIdFormat;
		}
		public Material get(String color) throws IllegalArgumentException{
			return Material.valueOf(color);
		}
	}
	@SuppressWarnings("deprecation")
	public enum Colors {
		RED(ChatColor.RED),
		ORANGE(ChatColor.GOLD),
		YELLOW(ChatColor.YELLOW),
		LIME(ChatColor.GREEN),
		GREEN(ChatColor.DARK_GREEN),
		LIGHT_BLUE(ChatColor.BLUE),
		BLUE(ChatColor.DARK_BLUE),
		PINK(ChatColor.LIGHT_PURPLE),
		PURPLE(ChatColor.DARK_PURPLE),
		WHITE(ChatColor.WHITE),
		LIGHT_GRAY(ChatColor.GRAY),
		GRAY(ChatColor.DARK_GRAY),
		BLACK(ChatColor.BLACK),
		;
		@Getter
		final ChatColor color;

		Colors(ChatColor color) {
			this.color = color;
		}

		public static ArrayList<Colors> ordered(){
			ArrayList<Colors> colors = new ArrayList<>();
			colors.add(RED);
			colors.add(ORANGE);
			colors.add(YELLOW);
			colors.add(LIME);
			colors.add(GREEN);
			colors.add(LIGHT_BLUE);
			colors.add(BLUE);
			colors.add(PURPLE);
			colors.add(PINK);
			colors.add(WHITE);
			colors.add(LIGHT_GRAY);
			colors.add(GRAY);
			colors.add(BLACK);
			return colors;
		}
	}
	@SuppressWarnings("deprecation")
	private static class ColorHolder implements InventoryHolder {
		private final FluffyCombat fluffy;
		private final Inventory reOpen;
		private final Inventory inventory;
		private final Field field;
		private final Object instance;
		private final ButtonType type;

		public ColorHolder(FluffyCombat fluffy, ButtonType type, String name, int rows, Inventory reOpen, Object instance, Field field) {
			this.type = type;
			this.fluffy = fluffy;
			this.reOpen = reOpen;
			this.inventory = fluffy.getServer().createInventory(this, rows*9, Component.text(name));
			this.field = field;
			field.setAccessible(true);
			this.instance = instance;
		}
		private void formatInventory(){
			InventoryUtils.InventoryPattern pattern = InventoryUtils.makePattern(
					"xxxxxxxxx",
					"xx01234xx",
					"xx56789xx",
					"xxxabcxxx",
					"xxxxxxxxx");
			char[] patterns = new char[]{'0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'a', 'b', 'c'};
			LegacyComponentSerializer serializer = LegacyComponentSerializer.legacySection();
			List<Colors> colors = Colors.ordered();
			for (int i = 0; i < colors.size(); i++){
				Colors color = colors.get(i);
				ItemStack itemStack = new ItemStack(type.get(color.name()));
				ItemMeta meta = itemStack.getItemMeta();
				meta.displayName(serializer.deserialize(color.color + color.name()));
				itemStack.setItemMeta(meta);
				char character = patterns[i];
				pattern.setItem(character, itemStack);
			}
			pattern.setItem('x', Material.AIR);
			pattern.update(inventory);
		}
		protected boolean onItemClick(ItemStack itemStack){
			if (itemStack == null || itemStack.getType().isAir()){
				return false;
			}
			ChatColor color = null;
			String name = itemStack.getType().name();
			if (name.contains("RED")){
				color = ChatColor.RED;
			} else if (name.contains("ORANGE")){
				color = ChatColor.GOLD;
			} else if (name.contains("YELLOW")){
				color = ChatColor.YELLOW;
			} else if (name.contains("LIME")){
				color = ChatColor.GREEN;
			} else if (name.contains("GREEN")){
				color = ChatColor.DARK_GREEN;
			} else if (name.contains("LIGHT_BLUE")){
				color = ChatColor.AQUA;
			} else if (name.contains("BLUE")) {
				color = ChatColor.DARK_BLUE;
			} else if (name.contains("PINK")){
				color = ChatColor.LIGHT_PURPLE;
			} else if (name.contains("PURPLE")){
				color = ChatColor.DARK_PURPLE;
			} else if (name.contains("LGIHT_GRAY")){
				color = ChatColor.GRAY;
			} else if (name.contains("GRAY")){
				color = ChatColor.DARK_GRAY;
			} else if (name.contains("BLACK")){
				color = ChatColor.BLACK;
			} else if (name.contains("WHITE")){
				color = ChatColor.WHITE;
			}

			if (color == null){
				return false;
			}
			try {
				field.set(instance, color);
			} catch (IllegalAccessException e) {
				throw new RuntimeException(e);
			}
			HumanEntity player = inventory.getViewers().get(0);
			inventory.close();
			new BukkitRunnable() {
				@Override
				public void run() {
					player.openInventory(reOpen);
				}
			}.runTaskLater(fluffy, 1);
			return true;
		}

		@Override
		public @NotNull Inventory getInventory() {
			return inventory;
		}
	}
}
