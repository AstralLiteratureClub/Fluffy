package bet.astral.fluffy.manager;

import org.bukkit.entity.Player;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class CombatLogManager {
	private static Map<UUID, Boolean> combatLogged = new HashMap<>();


	public boolean hasCombatLogged(Player player) {
		boolean logged = combatLogged.containsKey(player.getUniqueId()) && combatLogged.get(player.getUniqueId());
		combatLogged.remove(player.getUniqueId());
		return (logged) ;
	}
	public void setCombatLogged(Player player){
		combatLogged.put(player.getUniqueId(), true);
	}
}
