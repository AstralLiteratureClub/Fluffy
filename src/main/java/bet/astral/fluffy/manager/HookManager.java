package bet.astral.fluffy.manager;

import bet.astral.fluffy.FluffyCombat;
import bet.astral.fluffy.hooks.Hook;
import bet.astral.fluffy.hooks.HookState;
import bet.astral.fluffy.hooks.gsit.GSitHook;
import bet.astral.fluffy.hooks.npc.citizens.CitizensHook;
import bet.astral.fluffy.hooks.npc.sentinel.SentinelHook;
import bet.astral.fluffy.hooks.placeholderapi.PlaceholderAPIHook;
import bet.astral.fluffy.hooks.worldguard.WorldGuardHook;
import com.sk89q.worldguard.bukkit.WorldGuardPlugin;
import dev.geco.gsit.GSitMain;
import lombok.Getter;
import me.clip.placeholderapi.PlaceholderAPIPlugin;
import net.citizensnpcs.Citizens;
import org.bukkit.OfflinePlayer;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.mcmonkey.sentinel.SentinelPlugin;

import java.io.File;
import java.io.IOException;
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
		fluffyCombat.getLogger().info("Loading hooks: onLoad!");
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
		fluffyCombat.getLogger().info("Loading hooks: onEnable!");
		hookPlaceholderAPI();
		hookCitizens();
		hookSentinel();
		hookGSit();
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


	private void hookGSit() {
		try {
			Class.forName("dev.geco.gsit.GSitMain");
			hook("GSit", GSitMain.class, GSitHook.class);
		} catch (ClassNotFoundException ignore) {}
	}

	private <T extends JavaPlugin> void hook(@NotNull String name, @NotNull Class<T> clazz, @NotNull Class<? extends Hook> hookClass) throws ClassNotFoundException {
		fluffyCombat.getComponentLogger().info("Starting to hook to hook named {}", name);
		JavaPlugin javaPlugin = (JavaPlugin) getFluffyCombat().getServer().getPluginManager().getPlugin(name);
		HookState state;
		if (javaPlugin==null){
			state=HookState.HOOK_NOT_FOUND;
			fluffyCombat.getComponentLogger().warn("Couldn't find the hook plugin for: {}", name);
		} else {
            if (getFluffyCombat().getConfig().get("hooks."+name+".enabled") == null) {
                getFluffyCombat().getConfig().set("hooks."+name+".enabled", false);
                try {
                    getFluffyCombat().getConfig().save(new File(getFluffyCombat().getDataFolder(), "config.yml"));
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
                fluffyCombat.getComponentLogger().warn("New fluffy hook has been installed on the new server jar! Hook: {}", name);
                fluffyCombat.getComponentLogger().warn("The hook has been set to state: false");
                state = HookState.NOT_HOOKED;
            } else if (!javaPlugin.isEnabled()){
                if (!getFluffyCombat().getConfig().getBoolean("hooks."+name+".enabled", false)){
                    state=HookState.NOT_HOOKED;
					fluffyCombat.getComponentLogger().warn("Set hook state of {} to false", name);
                } else {
                    state = HookState.UNKNOWN_PLUGIN_NOT_ENABLED;
					fluffyCombat.getComponentLogger().info("Couldn't find hook plugin for name {}", name);
                }
            } else {
                if (getFluffyCombat().getConfig().getBoolean("hooks."+name+".enabled", false)){
                    state=HookState.HOOKED;
					fluffyCombat.getComponentLogger().info("Set hook state of {} to true", name);
                }else{
                    state=HookState.NOT_HOOKED;
					fluffyCombat.getComponentLogger().warn("Set hook state of {} to false", name);
                }
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


	public void onCombatBegin(Player player, OfflinePlayer player2) {
		getHooks().forEach(hook -> hook.onCombatBegin(player, player2));
	}
	public void onCombatEnd(Player player, OfflinePlayer player2) {
		getHooks().forEach(hook -> hook.onCombatEnd(player, player2));
	}
	public void onCombatUpdate(Player player, OfflinePlayer player2) {
		getHooks().forEach(hook -> hook.onCombatUpdate(player, player2));
	}

	public void onCombatBegin(Player player, Block block) {
		getHooks().forEach(hook -> hook.onCombatBegin(player, block));
	}
	public void onCombatEnd(Player player, Block block) {
		getHooks().forEach(hook -> hook.onCombatEnd(player, block));
	}
	public void onCombatUpdate(Player player, Block block) {
		getHooks().forEach(hook -> hook.onCombatUpdate(player, block));
	}
}
