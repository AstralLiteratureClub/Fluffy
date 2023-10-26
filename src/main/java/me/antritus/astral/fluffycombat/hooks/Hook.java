package me.antritus.astral.fluffycombat.hooks;

import me.antritus.astral.fluffycombat.FluffyCombat;
import org.bukkit.plugin.java.JavaPlugin;

public interface Hook {
	FluffyCombat main();
	JavaPlugin hookPlugin();
	Class<?> hookPluginClass();
	HookState state();
}
