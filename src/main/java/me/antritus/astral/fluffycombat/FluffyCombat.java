package me.antritus.astral.fluffycombat;

import me.antritus.astral.fluffycombat.antsfactions.FactionsPlugin;
import me.antritus.astral.fluffycombat.listeners.CombatEnterListener;
import me.antritus.astral.fluffycombat.listeners.PlayerQuitListener;
import me.antritus.astral.fluffycombat.listeners.PlayerJoinListener;
import me.antritus.astral.fluffycombat.manager.CombatManager;
import me.antritus.astral.fluffycombat.manager.HookManager;
import me.antritus.astral.fluffycombat.manager.UserManager;
import org.bukkit.event.Listener;
import org.jetbrains.annotations.Nullable;


public class FluffyCombat extends FactionsPlugin implements Listener {
	public static boolean isStopping = false;
	private CombatManager combatManager;
	private UserManager userManager;
	private HookManager hookManager;

	@Override
	public void updateConfig(@Nullable String oldVersion, String newVersion) {

	}

	@Override
	public void enable() {
		combatManager = new CombatManager(this);
		userManager = new UserManager(this);
		combatManager.onEnable();
		userManager.onEnable();
		new CMDDebug(this).registerCommand();
		getServer().getPluginManager().registerEvents(new CombatEnterListener(this), this);
		getServer().getPluginManager().registerEvents(new PlayerQuitListener(this), this);
		getServer().getPluginManager().registerEvents(new PlayerJoinListener(this), this);
		getServer().getPluginManager().registerEvents(this, this);

		hookManager = new HookManager(this);
	}

	@Override
	public void startDisable() {

	}

	@Override
	public void disable() {
		isStopping = true;
		userManager.onDisable();
		combatManager.onDisable();
	}

	/**
	 * Returns the combat manager. Which controls everything about combat tags.
	 * @return combat manager
	 */
	public CombatManager getCombatManager() {
		return combatManager;
	}

	/**
	 * Returns the user manager. No users are saved.
	 * @return user manager
	 */
	public UserManager getUserManager() {
		return userManager;
	}

	/**
	 * Returns the hook manager
	 * @return hook manager
	 */
	public HookManager getHookManager() {
		return hookManager;
	}

}