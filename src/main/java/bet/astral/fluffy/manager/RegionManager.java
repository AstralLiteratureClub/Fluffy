package bet.astral.fluffy.manager;

import org.bukkit.Location;
import org.bukkit.entity.Player;

public abstract class RegionManager {
    public static final RegionManager NONE = new RegionManager() {
        @Override
        public boolean canEnterCombat(Player victim, Location location) {
            return true;
        }
    };

    public abstract boolean canEnterCombat(Player victim, Location location);
}
