package bet.astral.fluffy.manager;

import bet.astral.fluffy.FluffyCombat;
import bet.astral.fluffy.hooks.*;
import lombok.Getter;
//import me.clip.placeholderapi.PlaceholderAPIPlugin;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.LinkedHashMap;
import java.util.Map;

public class HookManager {
	private final Map<String, Hook> hookMap = new LinkedHashMap<>();
	@Getter
	private final FluffyCombat fluffyCombat;
	public HookManager(FluffyCombat fluffyCombat){
		this.fluffyCombat = fluffyCombat;
//		hookPlaceholderAPI();
	}


//	private void hookPlaceholderAPI() {
//		try {
//			Class.forName("me.clip.placeholderapi.PlaceholderAPI");
//			hook("placeholderapi", PlaceholderAPIPlugin.class, PlaceholderAPIHook.class);
//		} catch (ClassNotFoundException ignore) {}
//	}



	private <T extends JavaPlugin> void hook(@NotNull String name, @NotNull Class<T> clazz, @NotNull Class<? extends Hook> hookClass) throws ClassNotFoundException {
		JavaPlugin javaPlugin = (JavaPlugin) getFluffyCombat().getServer().getPluginManager().getPlugin(name);
		HookState state;
		if (javaPlugin==null){
			state=HookState.HOOK_NOT_FOUND;
		} else if (!javaPlugin.isEnabled()){
			state=HookState.UNKNOWN;
		} else {
			if (getFluffyCombat().getConfig().getBoolean("hooks."+name+".enabled", false)){
				state=HookState.HOOKED;
			}else{
				state=HookState.NOT_HOOKED;
			}
		}
		@SuppressWarnings("unchecked") T plugin = (T) javaPlugin;
		try {
			Constructor<? extends Hook> hookConstructor = hookClass.getConstructor(FluffyCombat.class, clazz, Class.class, HookState.class);
			Hook hook = hookConstructor.newInstance(fluffyCombat, plugin, clazz, state);
			hookMap.put(name.toLowerCase(), hook);
		} catch (NoSuchMethodException | InvocationTargetException | InstantiationException | IllegalAccessException e) {
			throw new RuntimeException(e);
		}
	}

	@Nullable
	public Hook getHook(String name){
		return hookMap.get(name);
	}

}
