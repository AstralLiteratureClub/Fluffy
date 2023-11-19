package me.antritus.astral.fluffycombat.manager;

import me.antritus.astral.fluffycombat.FluffyCombat;
import me.antritus.astral.fluffycombat.hooks.CitizensHook;
import me.antritus.astral.fluffycombat.hooks.Hook;
import me.antritus.astral.fluffycombat.hooks.HookState;
import me.antritus.astral.fluffycombat.hooks.WormholeHook;
import me.antritus.minecraft_server.wormhole.Wormhole;
import net.citizensnpcs.Citizens;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.LinkedHashMap;
import java.util.Map;

public class HookManager {
	private final Map<String, Hook> hookMap = new LinkedHashMap<>();
	private final FluffyCombat fluffyCombat;
	public HookManager(FluffyCombat fluffyCombat){
		this.fluffyCombat = fluffyCombat;
		hookWormhole();
		hookCitizens();
	}


	private void hookCitizens(){
		try {
			Class.forName("net.citizensnpcs.Citizens");
			hook("citizens", Citizens.class, CitizensHook.class);
		} catch (ClassNotFoundException ignore) {}
	}
	private void hookWormhole(){
		try {
			Class.forName("me.antritus.minecraft_server.wormhole.Wormhole");
			hook("wormhole", Wormhole.class, WormholeHook.class);
		} catch (ClassNotFoundException ignore) {}
	}

	private <T extends JavaPlugin> void hook(@NotNull String name, @NotNull Class<T> clazz, @NotNull Class<? extends Hook> hookClass) throws ClassNotFoundException {
		JavaPlugin javaPlugin = (JavaPlugin) getFluffyCombat().getServer().getPluginManager().getPlugin(name);
		HookState state;
		if (javaPlugin==null){
			state=HookState.UNKNOWN;
		} else if (!javaPlugin.isEnabled()){
			state=HookState.UNKNOWN;
		} else {
			if (getFluffyCombat().getConfig().getBoolean("hooks.wormhole.hook", false)){
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

	public FluffyCombat getFluffyCombat() {
		return fluffyCombat;
	}
}
