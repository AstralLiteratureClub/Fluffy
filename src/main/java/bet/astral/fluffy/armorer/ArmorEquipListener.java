package bet.astral.fluffy.armorer;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockDispenseArmorEvent;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;
import org.bukkit.event.player.PlayerItemBreakEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.entity.EntityPickupItemEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.bukkit.plugin.Plugin;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class ArmorEquipListener implements Listener {

    private final Plugin plugin;
    private final Map<UUID, ItemStack[]> lastArmor = new HashMap<>();

    public ArmorEquipListener(Plugin plugin) {
        this.plugin = plugin;

        // Start armor checking task for methods that don't have direct events
        startArmorCheckTask();
    }

    // ====================================================================
    // METHOD 1 & 2: Manual Inventory Equipping & Shift-Click Equipping
    // ====================================================================

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onInventoryArmorEquip(InventoryClickEvent event) {
        if (!(event.getWhoClicked() instanceof Player)) {
            return;
        }

        Player player = (Player) event.getWhoClicked();

        // Check if clicking in player inventory
        if (event.getInventory().getType() != InventoryType.CRAFTING &&
                event.getInventory().getType() != InventoryType.PLAYER) {
            return;
        }

        int rawSlot = event.getRawSlot();

        // Armor slots in inventory view: 5=helmet, 6=chestplate, 7=leggings, 8=boots
        boolean isArmorSlot = rawSlot >= 5 && rawSlot <= 8;
        boolean isShiftClick = event.isShiftClick();
        ItemStack clickedItem = event.getCurrentItem();
        ItemStack cursorItem = event.getCursor();

        // Detect shift-click from main inventory to armor slot
        if (isShiftClick && clickedItem != null) {
            ArmorEquipEvent.ArmorType armorType = ArmorEquipEvent.ArmorType.getByItemStack(clickedItem);
            if (armorType != null) {
                // Schedule to check after the event completes
                new BukkitRunnable() {
                    @Override
                    public void run() {
                        PlayerInventory inv = player.getInventory();
                        ItemStack newArmor = inv.getItem(armorType.getSlot());
                        ItemStack oldArmor = lastArmor.containsKey(player.getUniqueId()) ?
                                lastArmor.get(player.getUniqueId())[getArmorIndex(armorType)] : null;

                        if (!isSameItem(oldArmor, newArmor)) {
                            fireArmorEquipEvent(player, ArmorEquipEvent.EquipMethod.SHIFT_CLICK,
                                    armorType, oldArmor, newArmor);
                        }
                        updateLastArmor(player);
                    }
                }.runTaskLater(plugin, 1L);
                return;
            }
        }

        // Detect manual drag to armor slot
        if (isArmorSlot) {
            ArmorEquipEvent.ArmorType armorType = getArmorTypeFromSlot(rawSlot);
            if (armorType != null) {
                new BukkitRunnable() {
                    @Override
                    public void run() {
                        PlayerInventory inv = player.getInventory();
                        ItemStack newArmor = inv.getItem(armorType.getSlot());
                        ItemStack oldArmor = lastArmor.containsKey(player.getUniqueId()) ?
                                lastArmor.get(player.getUniqueId())[getArmorIndex(armorType)] : null;

                        if (!isSameItem(oldArmor, newArmor)) {
                            fireArmorEquipEvent(player, ArmorEquipEvent.EquipMethod.INVENTORY_DRAG,
                                    armorType, oldArmor, newArmor);
                        }
                        updateLastArmor(player);
                    }
                }.runTaskLater(plugin, 1L);
            }
        }
    }

    // ====================================================================
    // METHOD 3 & 4: Right-Click to Equip/Swap
    // ====================================================================

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onRightClickArmorEquip(PlayerInteractEvent event) {
        Player player = event.getPlayer();
        ItemStack item = event.getItem();

        if (!event.getAction().name().contains("RIGHT_CLICK")) {
            return;
        }

        if (item == null) {
            return;
        }

        ArmorEquipEvent.ArmorType armorType = ArmorEquipEvent.ArmorType.getByItemStack(item);
        if (armorType == null) {
            return;
        }

        PlayerInventory inv = player.getInventory();
        ItemStack currentArmor = inv.getItem(armorType.getSlot());

        // Determine if it's equip or swap
        boolean isSwap = currentArmor != null && currentArmor.getType() != Material.AIR;
        ArmorEquipEvent.EquipMethod method = isSwap ?
                ArmorEquipEvent.EquipMethod.RIGHT_CLICK_SWAP :
                ArmorEquipEvent.EquipMethod.RIGHT_CLICK;

        // Schedule check after event completes
        new BukkitRunnable() {
            @Override
            public void run() {
                ItemStack newArmor = inv.getItem(armorType.getSlot());

                if (!isSameItem(currentArmor, newArmor)) {
                    ArmorEquipEvent armorEvent = fireArmorEquipEvent(player, method,
                            armorType, currentArmor, newArmor);

                    // If event was cancelled, revert the change
                    if (armorEvent != null && armorEvent.isCancelled()) {
                        inv.setItem(armorType.getSlot(), currentArmor);
                        player.getInventory().addItem(newArmor);
                    }
                }
                updateLastArmor(player);
            }
        }.runTaskLater(plugin, 1L);
    }

    // ====================================================================
    // METHOD 5: Dispenser Auto-Equipping
    // ====================================================================

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onDispenserArmorEquip(BlockDispenseArmorEvent event) {
        if (!(event.getTargetEntity() instanceof Player)) {
            return;
        }

        Player player = (Player) event.getTargetEntity();
        ItemStack armor = event.getItem();

        ArmorEquipEvent.ArmorType armorType = ArmorEquipEvent.ArmorType.getByItemStack(armor);
        if (armorType == null) {
            return;
        }

        PlayerInventory inv = player.getInventory();
        ItemStack oldArmor = inv.getItem(armorType.getSlot());

        ArmorEquipEvent armorEvent = fireArmorEquipEvent(player, ArmorEquipEvent.EquipMethod.DISPENSER,
                armorType, oldArmor, armor);

        // If our custom event was cancelled, cancel the Bukkit event
        if (armorEvent != null && armorEvent.isCancelled()) {
            event.setCancelled(true);
        } else {
            updateLastArmor(player);
        }
    }

    // ====================================================================
    // METHOD 6: Command Equipping
    // ====================================================================

    @EventHandler(priority = EventPriority.MONITOR)
    public void onCommandArmorEquip(PlayerCommandPreprocessEvent event) {
        String command = event.getMessage().toLowerCase();

        if (!command.contains("item replace") && !command.contains("replaceitem")) {
            return;
        }

        if (!command.contains("armor.head") &&
                !command.contains("armor.chest") &&
                !command.contains("armor.legs") &&
                !command.contains("armor.feet")) {
            return;
        }

        Player player = event.getPlayer();
        ItemStack[] currentArmor = player.getInventory().getArmorContents().clone();

        new BukkitRunnable() {
            @Override
            public void run() {
                ItemStack[] newArmor = player.getInventory().getArmorContents();

                for (int i = 0; i < 4; i++) {
                    if (!isSameItem(currentArmor[i], newArmor[i])) {
                        ArmorEquipEvent.ArmorType armorType = ArmorEquipEvent.ArmorType.values()[3 - i];
                        fireArmorEquipEvent(player, ArmorEquipEvent.EquipMethod.COMMAND,
                                armorType, currentArmor[i], newArmor[i]);
                    }
                }
                updateLastArmor(player);
            }
        }.runTaskLater(plugin, 1L);
    }

    // ====================================================================
    // ADDITIONAL: Item Break Detection
    // ====================================================================

    @EventHandler(priority = EventPriority.MONITOR)
    public void onArmorBreak(PlayerItemBreakEvent event) {
        Player player = event.getPlayer();
        ItemStack broken = event.getBrokenItem();

        ArmorEquipEvent.ArmorType armorType = ArmorEquipEvent.ArmorType.getByItemStack(broken);
        if (armorType != null) {
            fireArmorEquipEvent(player, ArmorEquipEvent.EquipMethod.BROKE,
                    armorType, broken, null);
            updateLastArmor(player);
        }
    }

    // ====================================================================
    // ADDITIONAL: Death/Respawn Detection
    // ====================================================================

    @EventHandler(priority = EventPriority.MONITOR)
    public void onPlayerDeath(PlayerDeathEvent event) {
        Player player = event.getEntity();

        if (!event.getKeepInventory()) {
            // Player loses armor
            new BukkitRunnable() {
                @Override
                public void run() {
                    updateLastArmor(player);
                }
            }.runTaskLater(plugin, 1L);
        }
    }

    // ====================================================================
    // ADDITIONAL: Pickup Detection (for auto-equip scenarios)
    // ====================================================================

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onArmorPickup(EntityPickupItemEvent event) {
        if (!(event.getEntity() instanceof Player)) {
            return;
        }

        Player player = (Player) event.getEntity();
        ItemStack item = event.getItem().getItemStack();

        ArmorEquipEvent.ArmorType armorType = ArmorEquipEvent.ArmorType.getByItemStack(item);
        if (armorType == null) {
            return;
        }

        // Check if player's armor slot is empty and item might auto-equip
        PlayerInventory inv = player.getInventory();
        ItemStack currentArmor = inv.getItem(armorType.getSlot());

        if (currentArmor == null || currentArmor.getType() == Material.AIR) {
            // Might auto-equip, check after pickup
            new BukkitRunnable() {
                @Override
                public void run() {
                    ItemStack newArmor = inv.getItem(armorType.getSlot());

                    if (isSameItem(item, newArmor)) {
                        fireArmorEquipEvent(player, ArmorEquipEvent.EquipMethod.PICKUP,
                                armorType, null, newArmor);
                        updateLastArmor(player);
                    }
                }
            }.runTaskLater(plugin, 1L);
        }
    }

    // ====================================================================
    // PERIODIC CHECK TASK (Catches any missed events)
    // ====================================================================

    private void startArmorCheckTask() {
        new BukkitRunnable() {
            @Override
            public void run() {
                for (Player player : Bukkit.getOnlinePlayers()) {
                    checkArmorChange(player);
                }
            }
        }.runTaskTimer(plugin, 20L, 20L); // Check every second
    }

    private void checkArmorChange(Player player) {
        UUID uuid = player.getUniqueId();
        ItemStack[] currentArmor = player.getInventory().getArmorContents();
        ItemStack[] previousArmor = lastArmor.get(uuid);

        if (previousArmor == null) {
            lastArmor.put(uuid, currentArmor.clone());
            return;
        }

        for (int i = 0; i < 4; i++) {
            if (!isSameItem(previousArmor[i], currentArmor[i])) {
                ArmorEquipEvent.ArmorType armorType = ArmorEquipEvent.ArmorType.values()[3 - i];
                fireArmorEquipEvent(player, ArmorEquipEvent.EquipMethod.UNKNOWN,
                        armorType, previousArmor[i], currentArmor[i]);
            }
        }

        lastArmor.put(uuid, currentArmor.clone());
    }

    // ====================================================================
    // HELPER METHODS
    // ====================================================================

    private ArmorEquipEvent fireArmorEquipEvent(Player player, ArmorEquipEvent.EquipMethod method,
                                                ArmorEquipEvent.ArmorType armorType,
                                                ItemStack oldArmor, ItemStack newArmor) {
        ArmorEquipEvent event = new ArmorEquipEvent(player, method, armorType, oldArmor, newArmor);
        Bukkit.getPluginManager().callEvent(event);

        // Send debug message
        String action = event.isEquipping() ? "equipped" : event.isUnequipping() ? "unequipped" : "swapped";
        player.sendMessage(String.format("§6[ArmorEquip] §a%s %s via %s",
                armorType.name(), action, method.name()));

        return event;
    }

    private void updateLastArmor(Player player) {
        lastArmor.put(player.getUniqueId(), player.getInventory().getArmorContents().clone());
    }

    private boolean isSameItem(ItemStack item1, ItemStack item2) {
        if (item1 == null && item2 == null) return true;
        if (item1 == null || item2 == null) return false;
        if (item1.getType() == Material.AIR && item2.getType() == Material.AIR) return true;
        return item1.isSimilar(item2) && item1.getAmount() == item2.getAmount();
    }

    private ArmorEquipEvent.ArmorType getArmorTypeFromSlot(int rawSlot) {
        switch (rawSlot) {
            case 5: return ArmorEquipEvent.ArmorType.HELMET;
            case 6: return ArmorEquipEvent.ArmorType.CHESTPLATE;
            case 7: return ArmorEquipEvent.ArmorType.LEGGINGS;
            case 8: return ArmorEquipEvent.ArmorType.BOOTS;
            default: return null;
        }
    }

    private int getArmorIndex(ArmorEquipEvent.ArmorType armorType) {
        return 3 - armorType.ordinal();
    }

    /**
     * Clean up player data when they leave
     */
    @EventHandler
    public void onPlayerQuit(org.bukkit.event.player.PlayerQuitEvent event) {
        lastArmor.remove(event.getPlayer().getUniqueId());
    }
}