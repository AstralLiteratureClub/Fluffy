package bet.astral.fluffy.hooks;

import bet.astral.fluffy.FluffyCombat;
import org.bukkit.OfflinePlayer;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

public interface Hook {
	FluffyCombat main();
	JavaPlugin hookPlugin();
	Class<?> hookPluginClass();
	HookState state();

	void onLoad();
	void onEnable();

	void onCombatBegin(Player player, OfflinePlayer player2);
	void onCombatEnd(Player player, OfflinePlayer player2);
	void onCombatUpdate(Player player, OfflinePlayer player2);
	void onCombatBegin(Player player, Block player2);
	void onCombatEnd(Player player, Block player2);
	void onCombatUpdate(Player player, Block player2);
	default boolean hasCombat(OfflinePlayer player) {
		return main().getCombatManager().hasTags(player);
	}

    default void tryFixState(){}
}
