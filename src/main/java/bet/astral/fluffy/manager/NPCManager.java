package bet.astral.fluffy.manager;

import org.bukkit.Location;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.UUID;

public class NPCManager {
    public static final NPCManager NONE = new NPCManager();

    public UUID spawnNPC(Location location, Player whoToClone){
        return null;
    }
    @Nullable
    public Object getNPC(UUID uniqueId) {
        return null;
    }

    public Location getLocation(Object npc){
        return null;
    }

    public void clearNPC(Object player){
    }

    public void handleDeath(Player player, Object attacker, ItemStack itemStack){
    }

    public boolean isNPC(Object object){
        return false;
    }

    public boolean isFluffyNPC(Object object){
        return false;
    }

    public UUID getUniqueId(Object object){
        if (object instanceof Entity player){
            return player.getUniqueId();
        }
        return null;
    }
    public OfflinePlayer getOwnerFromNPC(Object object){
        return (OfflinePlayer) object;
    }

    public boolean isCombatLogNPC(Object object){
        return false;
    }

    public Object getCombatLogNPC(@NotNull UUID uniqueId) {
        return null;
    }
}
