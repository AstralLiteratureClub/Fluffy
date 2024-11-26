package bet.astral.fluffy.hooks;

import bet.astral.fluffy.FluffyCombat;
import org.bukkit.plugin.java.JavaPlugin;

public interface Hook {
	FluffyCombat main();
	JavaPlugin hookPlugin();
	Class<?> hookPluginClass();
	HookState state();

	void onLoad();
	void onEnable();
}
