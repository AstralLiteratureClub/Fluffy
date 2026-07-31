package bet.astral.fluffy.armorer;

import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

/**
 * Custom event fired when a player equips or unequips armor
 */
public class ArmorEquipEvent extends Event {
    private static final HandlerList HANDLERS = new HandlerList();

    private final Player player;
    private final EquipMethod method;
    private final ArmorType armorType;
    private final ItemStack oldArmorPiece;
    private final ItemStack newArmorPiece;
    private boolean cancelled = false;

    public ArmorEquipEvent(Player player, EquipMethod method, ArmorType armorType,
                           ItemStack oldArmorPiece, ItemStack newArmorPiece) {
        this.player = player;
        this.method = method;
        this.armorType = armorType;
        this.oldArmorPiece = oldArmorPiece;
        this.newArmorPiece = newArmorPiece;
    }

    /**
     * Enum representing how the armor was equipped
     */
    public enum EquipMethod {
        /**
         * Armor equipped by dragging in inventory (original method)
         */
        INVENTORY_DRAG,

        /**
         * Armor equipped via shift-click (1.3+)
         */
        SHIFT_CLICK,

        /**
         * Armor equipped by right-clicking with armor in hand (1.9+)
         */
        RIGHT_CLICK,

        /**
         * Armor swapped by right-clicking with different armor (1.19.4+)
         */
        RIGHT_CLICK_SWAP,

        /**
         * Armor equipped by a dispenser (1.4.2+)
         */
        DISPENSER,

        /**
         * Armor equipped via command (/item replace, /replaceitem)
         */
        COMMAND,

        /**
         * Armor equipped by death (keeping inventory on)
         */
        DEATH,

        /**
         * Armor equipped by picking up armor as an item entity
         */
        PICKUP,

        /**
         * Armor broken/destroyed
         */
        BROKE,

        /**
         * Unknown or other method
         */
        UNKNOWN
    }

    /**
     * Enum representing armor slot types
     */
    public enum ArmorType {
        HELMET(39),
        CHESTPLATE(38),
        LEGGINGS(37),
        BOOTS(36);

        private final int slot;

        ArmorType(int slot) {
            this.slot = slot;
        }

        public int getSlot() {
            return slot;
        }

        /**
         * Get ArmorType from material, supports 1.20+ custom items
         */
        public static ArmorType getByMaterial(Material material) {
            if (material == null) return null;

            String name = material.name();

            if (name.endsWith("_HELMET") || name.equals("TURTLE_HELMET") ||
                    name.equals("CARVED_PUMPKIN") || name.equals("PLAYER_HEAD") ||
                    name.equals("SKELETON_SKULL") || name.equals("WITHER_SKELETON_SKULL") ||
                    name.equals("ZOMBIE_HEAD") || name.equals("CREEPER_HEAD") ||
                    name.equals("DRAGON_HEAD") || name.equals("PIGLIN_HEAD")) {
                return HELMET;
            } else if (name.endsWith("_CHESTPLATE") || name.equals("ELYTRA")) {
                return CHESTPLATE;
            } else if (name.endsWith("_LEGGINGS")) {
                return LEGGINGS;
            } else if (name.endsWith("_BOOTS")) {
                return BOOTS;
            }

            return null;
        }

        /**
         * Get ArmorType from ItemStack, supports 1.20+ custom equippable items
         */
        public static ArmorType getByItemStack(ItemStack item) {
            if (item == null || item.getType() == Material.AIR) {
                return null;
            }

            // First check traditional armor materials
            ArmorType type = getByMaterial(item.getType());
            if (type != null) {
                return type;
            }

            // Check for 1.20+ custom equippable items via ItemMeta
            if (item.hasItemMeta()) {
                ItemMeta meta = item.getItemMeta();

                // Try to get equippable component (1.20.5+)
                try {
                    // Use reflection to support both older and newer versions
                    Object equippable = meta.getClass().getMethod("getEquippable").invoke(meta);
                    if (equippable != null) {
                        Object slot = equippable.getClass().getMethod("getSlot").invoke(equippable);
                        String slotName = slot.toString();

                        switch (slotName.toUpperCase()) {
                            case "HEAD":
                                return HELMET;
                            case "CHEST":
                                return CHESTPLATE;
                            case "LEGS":
                                return LEGGINGS;
                            case "FEET":
                                return BOOTS;
                        }
                    }
                } catch (Exception e) {
                    // Method doesn't exist in this version, continue
                }

                // Check custom model data or display name for custom armor
                if (meta.hasCustomModelData() || meta.hasDisplayName()) {
                    // Could implement custom logic here for server-specific custom armor
                }
            }

            return null;
        }
    }

    public Player getPlayer() {
        return player;
    }

    public EquipMethod getMethod() {
        return method;
    }

    public ArmorType getArmorType() {
        return armorType;
    }

    public ItemStack getOldArmorPiece() {
        return oldArmorPiece;
    }

    public ItemStack getNewArmorPiece() {
        return newArmorPiece;
    }

    public boolean isCancelled() {
        return cancelled;
    }

    public void setCancelled(boolean cancelled) {
        this.cancelled = cancelled;
    }

    /**
     * Check if this is an equip action (putting armor on)
     */
    public boolean isEquipping() {
        return (oldArmorPiece == null || oldArmorPiece.getType() == Material.AIR) &&
                (newArmorPiece != null && newArmorPiece.getType() != Material.AIR);
    }

    /**
     * Check if this is an unequip action (taking armor off)
     */
    public boolean isUnequipping() {
        return (oldArmorPiece != null && oldArmorPiece.getType() != Material.AIR) &&
                (newArmorPiece == null || newArmorPiece.getType() == Material.AIR);
    }

    /**
     * Check if this is a swap action (changing armor)
     */
    public boolean isSwapping() {
        return (oldArmorPiece != null && oldArmorPiece.getType() != Material.AIR) &&
                (newArmorPiece != null && newArmorPiece.getType() != Material.AIR);
    }

    @Override
    public HandlerList getHandlers() {
        return HANDLERS;
    }

    public static HandlerList getHandlerList() {
        return HANDLERS;
    }
}