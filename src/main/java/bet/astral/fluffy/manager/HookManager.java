package bet.astral.fluffy.manager;

import bet.astral.fluffy.FluffyCombat;
import bet.astral.fluffy.hooks.*;
import bet.astral.fluffy.hooks.npc.citizens.CitizensHook;
import bet.astral.fluffy.hooks.npc.sentinel.SentinelHook;
import bet.astral.fluffy.hooks.placeholderapi.PlaceholderAPIHook;
import bet.astral.fluffy.hooks.worldguard.WorldGuardHook;
import com.sk89q.worldguard.bukkit.WorldGuardPlugin;
import lombok.Getter;
import me.clip.placeholderapi.PlaceholderAPIPlugin;
import net.citizensnpcs.Citizens;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.mcmonkey.sentinel.SentinelPlugin;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.Map;

public class HookManager {
	private final Map<String, Hook> hookMap = new LinkedHashMap<>();
	@Getter
	private final FluffyCombat fluffyCombat;
	public HookManager(FluffyCombat fluffyCombat){
		this.fluffyCombat = fluffyCombat;

	}

	public void onLoad(){
		hookWorldGuard();
		hookMap.values().stream().filter(hook->hook.state()==HookState.HOOKED||hook.state()==HookState.UNKNOWN_PLUGIN_NOT_ENABLED).forEach(hook->{
			if (hook.state() == HookState.UNKNOWN_PLUGIN_NOT_ENABLED){
				hook.tryFixState();
			}
			if (hook.state() == HookState.HOOKED){
				hook.onLoad();
			}
		});
	}
	public void onEnable(){
		hookPlaceholderAPI();
		hookCitizens();
		hookSentinel();
		hookMap.values().stream().filter(hook->hook.state()==HookState.HOOKED).forEach(Hook::onEnable);
	}

	private void hookPlaceholderAPI() {
		try {
			Class.forName("me.clip.placeholderapi.PlaceholderAPI");
			hook("placeholderapi", PlaceholderAPIPlugin.class, PlaceholderAPIHook.class);
		} catch (ClassNotFoundException ignore) {}
	}
	private void hookWorldGuard() {
		try {
			Class.forName("com.sk89q.worldguard.WorldGuard");
			hook("worldguard", WorldGuardPlugin.class, WorldGuardHook.class);
		} catch (ClassNotFoundException ignore) {}
	}
	private void hookSentinel() {
		try {
			Class.forName("org.mcmonkey.sentinel.SentinelPlugin");
			hook("sentinel", SentinelPlugin.class, SentinelHook.class);
		} catch (ClassNotFoundException ignore) {}
	}

	private void hookCitizens() {
		try {
			Class.forName("net.citizensnpcs.api.npc.NPC");
			hook("citizens", Citizens.class, CitizensHook.class);
		} catch (ClassNotFoundException ignore) {}
	}





	private <T extends JavaPlugin> void hook(@NotNull String name, @NotNull Class<T> clazz, @NotNull Class<? extends Hook> hookClass) throws ClassNotFoundException {
		JavaPlugin javaPlugin = (JavaPlugin) getFluffyCombat().getServer().getPluginManager().getPlugin(name);
		HookState state;
		if (javaPlugin==null){
			state=HookState.HOOK_NOT_FOUND;
		} else if (!javaPlugin.isEnabled()){
			if (!getFluffyCombat().getConfig().getBoolean("hooks."+name+".enabled", false)){
				state=HookState.NOT_HOOKED;
			} else {
				state = HookState.UNKNOWN_PLUGIN_NOT_ENABLED;
			}
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

	public Collection<Hook> getHooks() {
		return hookMap.values();
	}
}
