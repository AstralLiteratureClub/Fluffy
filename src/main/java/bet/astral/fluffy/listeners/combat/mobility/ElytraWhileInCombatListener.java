package bet.astral.fluffy.listeners.combat.mobility;

import bet.astral.fluffy.configs.CombatConfig;
import bet.astral.fluffy.hooks.npc.citizens.CitizensNPCManager;
import bet.astral.fluffy.messenger.Translations;
import bet.astral.fluffy.FluffyCombat;
import bet.astral.fluffy.manager.CombatManager;

import com.destroystokyo.paper.event.player.PlayerElytraBoostEvent;
import com.jeff_media.armorequipevent.ArmorEquipEvent;
import io.papermc.paper.datacomponent.DataComponentTypes;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityToggleGlideEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.player.PlayerAttemptPickupItemEvent;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;

import java.util.ArrayList;
import java.util.List;

public class ElytraWhileInCombatListener implements Listener {
//	public static ItemStack elytraReplacer = new ItemStack(Material.LEATHER_CHESTPLATE);
	private final FluffyCombat fluffy;
	public static final NamespacedKey ELYTRA_KEY = new NamespacedKey("fluffy", "elytra");


	public ElytraWhileInCombatListener(FluffyCombat fluffy) {
		this.fluffy = fluffy;
	}

	public static <P, C> void setValue(ItemStack itemStack, NamespacedKey key, PersistentDataType<P, C> type, C value){
		ItemMeta meta = itemStack.getItemMeta();
		meta.getPersistentDataContainer().set(key, type, value);
		itemStack.setItemMeta(meta);
	}
	public static <P, C> void resetValue(ItemStack itemStack, NamespacedKey key) {
		ItemMeta meta = itemStack.getItemMeta();
		meta.getPersistentDataContainer().remove(key);
		itemStack.setItemMeta(meta);
	}


	public static void convertElytraBack(ItemStack itemStack){
		if (!itemStack.getPersistentDataContainer().has(ELYTRA_KEY)){
			return;
		}
		/*
		if (itemStack.getType()== Material.ELYTRA){
			itemStack.unsetData(DataComponentTypes.GLIDER);
		} else{
			itemStack.setData(DataComponentTypes.GLIDER);
		}
		 */
		itemStack.setData(DataComponentTypes.GLIDER);
		resetValue(itemStack, ELYTRA_KEY);
	}
	/**
	 * Returns the elytra replacer with item meta of given elytra
	 * Returns null if not elytra
	 *
	 * @param original   elytra
	 * @param elytraMode
	 * @return replacer, else if not elytra null
	 */
	public static ItemStack convertElytraWithReplacer(ItemStack original, CombatConfig.ElytraMode elytraMode) {
		if (!original.hasData(DataComponentTypes.GLIDER)){
			return original;
		}

		ItemStack clone = original.clone();
		clone.unsetData(DataComponentTypes.GLIDER);
		setValue(clone, ELYTRA_KEY, PersistentDataType.BOOLEAN, true);

		return clone;

		/*
		ItemStack clone = elytraMode== CombatConfig.ElytraMode.DENY_CHESTPLATE ? elytraReplacer.clone() : original.clone();
		clone.setItemMeta(original.getItemMeta());

		ItemMeta meta = original.getItemMeta();
		if (elytraMode == CombatConfig.ElytraMode.DENY_CHESTPLATE) {
			Component displayname = meta.displayName();
			if (displayname == null)
				displayname = miniMessage.deserialize("<Yellow>Elytra Placeholder").decoration(TextDecoration.ITALIC, false);
			meta.displayName(displayname);
		}
		@Nullable List<Component> lore = meta.lore();
		if (lore == null) {
			lore = new LinkedList<>();
		} else {
			lore = new ArrayList<>(lore);
		}

		lore.add(Component.text().build());
		lore.add(miniMessage.deserialize("<dark_gray> | <gray>This item will disappear soon").decoration(TextDecoration.ITALIC, false));
		lore.add(miniMessage.deserialize("<dark_gray> |  <gray>and give your elytra back!").decoration(TextDecoration.ITALIC, false));
		lore.add(miniMessage.deserialize("<dark_gray> | <gray>Bugged? <yellow>@antritus <dark_aqua>(DISCORD) <gray>report this").decoration(TextDecoration.ITALIC, false));
		lore.add(miniMessage.deserialize("<dark_gray> |  <gray>bug so it may be fixed.").decoration(TextDecoration.ITALIC, false));
		meta.lore(lore);

		if (elytraMode== CombatConfig.ElytraMode.DENY_CHESTPLATE) {
			meta.removeAttributeModifier(Attribute.ARMOR);
			meta.addAttributeModifier(Attribute.ARMOR, new AttributeModifier(new NamespacedKey("fluffy", "elytra_replacer"), -3, AttributeModifier.Operation.ADD_NUMBER));
			meta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES);
			if (!meta.hasEnchants()) {
				meta.addItemFlags(ItemFlag.HIDE_ENCHANTS);
			}
			clone.setItemMeta(meta);
		}

		meta.getPersistentDataContainer().set(ELYTRA_KEY, PersistentDataType.BOOLEAN, true);
		clone.setItemMeta(meta);

		return clone;
		 */
	}

	@EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
	private void onDeath(PlayerDeathEvent event) {
		// Citizens 2 break this completely with their own NPCs
		if (fluffy.getNpcManager() instanceof CitizensNPCManager
				&& fluffy.getNpcManager().isNPC(event.getPlayer())){
			return;
		}
		fixItemsList(event.getDrops());
		fixItemsList(event.getItemsToKeep());
	}

	public void fixItemsList(List<ItemStack> items) {
		List<ItemStack> newDrops = new ArrayList<>();
		for (ItemStack itemStack : items) {
			if (itemStack.getPersistentDataContainer().has(ELYTRA_KEY)) {
				convertElytraBack(itemStack);
				newDrops.add(itemStack);
			} else {
				newDrops.add(itemStack);
			}
		}
		items.clear();
		items.addAll(newDrops);
	}

	@EventHandler(priority = EventPriority.HIGHEST)
	private void onQuit(PlayerQuitEvent event){
		for (ItemStack itemStack : event.getPlayer().getInventory().getContents()){
			if (itemStack == null || itemStack.isEmpty()){
				continue;
			}
			convertElytraBack(itemStack);
		}
	}

	@EventHandler
	private void onItemDrop(PlayerDropItemEvent event){
		if (event.getItemDrop().getPersistentDataContainer().has(ELYTRA_KEY)){
			convertElytraBack(event.getItemDrop().getItemStack());
		}
	}
	@EventHandler
	private void onItemPickup(PlayerAttemptPickupItemEvent event){
		if (event.getItem().getPersistentDataContainer().has(ELYTRA_KEY)){
			convertElytraBack(event.getItem().getItemStack());
		}
	}

	@EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
	private void onItemInventoryPickup(InventoryClickEvent event){
		if (event.getCurrentItem() != null && event.getCurrentItem().getPersistentDataContainer().has(ELYTRA_KEY)){
			convertElytraBack(event.getCurrentItem());
		}
	}

	@EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
	private void onSwap(ArmorEquipEvent event){
		if (event.getNewArmorPiece() != null && event.getNewArmorPiece().getPersistentDataContainer().has(ELYTRA_KEY)){
			convertElytraBack(event.getNewArmorPiece());
		}
		if (event.getOldArmorPiece() != null && event.getOldArmorPiece().getPersistentDataContainer().has(ELYTRA_KEY)){
			convertElytraBack(event.getOldArmorPiece());
		}
	}


	@EventHandler
	private void onEntityToggleGlide(EntityToggleGlideEvent e) {
		if (!(e.getEntity() instanceof Player player)) return;
		ItemStack itemStack = player.getInventory().getChestplate();
		if (itemStack == null || !itemStack.hasData(DataComponentTypes.GLIDER)) {
			return;
		}
		if (fluffy.getCombatConfig().getElytraMode() == CombatConfig.ElytraMode.TRUE) {
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
		player.getInventory().setChestplate(convertElytraWithReplacer(itemStack, fluffy.getCombatConfig().getElytraMode()));

		fluffy.getServer().getScheduler().runTaskLater(fluffy, () -> {
			for (ItemStack item : player.getInventory().getContents()){
				if (item == null || item.isEmpty() || !item.getPersistentDataContainer().has(ELYTRA_KEY)){
					continue;
				}
				convertElytraBack(item);
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
