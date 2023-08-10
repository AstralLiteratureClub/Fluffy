package me.antritus.astral.fluffycombat;

import me.antritus.astral.fluffycombat.antsfactions.FactionsPlugin;
import me.antritus.astral.fluffycombat.listeners.CombatEnterListener;
import me.antritus.astral.fluffycombat.listeners.PlayerCombatLogListener;
import me.antritus.astral.fluffycombat.listeners.PlayerJoinListener;
import me.antritus.astral.fluffycombat.manager.CombatManager;
import me.antritus.astral.fluffycombat.manager.UserManager;
import org.jetbrains.annotations.Nullable;

public class FluffyCombat extends FactionsPlugin {
	private CombatManager combatManager;
	private UserManager userManager;

	@Override
	public void updateConfig(@Nullable String oldVersion, String newVersion) {

	}

	@Override
	public void enable() {
		combatManager = new CombatManager(this);
		userManager = new UserManager(this);
		combatManager.onEnable();
		userManager.onEnable();
		getServer().getPluginManager().registerEvents(new CombatEnterListener(this), this);
		getServer().getPluginManager().registerEvents(new PlayerCombatLogListener(this), this);
		getServer().getPluginManager().registerEvents(new PlayerJoinListener(this), this);
	}

	@Override
	public void startDisable() {

	}

	@Override
	public void disable() {
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
}