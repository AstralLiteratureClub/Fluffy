package me.antritus.astral.fluffycombat.manager;

import me.antritus.astral.fluffycombat.FluffyCombat;
import me.antritus.astral.fluffycombat.hooks.Hook;
import me.antritus.astral.fluffycombat.hooks.HookState;
import me.antritus.astral.fluffycombat.hooks.WormholeHook;
import me.antritus.minecraft_server.wormhole.Wormhole;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.Nullable;

import java.util.LinkedHashMap;
import java.util.Map;

public class HookManager {
	private final Map<String, Hook> hookMap = new LinkedHashMap<>();
	private final FluffyCombat fluffyCombat;
	public HookManager(FluffyCombat fluffyCombat){
		this.fluffyCombat = fluffyCombat;
		hookWormhole();
	}

	private void hookWormhole(){
		try {
			Class<?> clazzWormhole = Class.forName("me.antritus.minecraft_server.wormhole.Wormhole");
			JavaPlugin javaPlugin = (JavaPlugin) getFluffyCombat().getServer().getPluginManager().getPlugin("wormhole");
			HookState state;
			if (javaPlugin==null){
				state=HookState.UNKNOWN;
			} else if (!javaPlugin.isEnabled()){
				state=HookState.UNKNOWN;
			} else {
				if (getFluffyCombat().getConfig().getBoolean("hooks.wormhole", false)){
					state=HookState.HOOKED;
				}else{
					state=HookState.NOT_HOOKED;
				}
			}
			WormholeHook hook = new WormholeHook(fluffyCombat, JavaPlugin.getPlugin(Wormhole.class), clazzWormhole, state);
			hookMap.put("wormhole", hook);
		} catch (ClassNotFoundException e) {
			Hook hook = new WormholeHook(fluffyCombat, null, null, HookState.HOOK_NOT_FOUND);
			hookMap.put("wormhole", hook);
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
