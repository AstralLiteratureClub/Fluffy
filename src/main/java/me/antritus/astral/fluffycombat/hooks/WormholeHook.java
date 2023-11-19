package me.antritus.astral.fluffycombat.hooks;

import bet.astral.messagemanager.MessageManager;
import bet.astral.messagemanager.placeholder.LegacyPlaceholder;
import me.antritus.astral.fluffycombat.FluffyCombat;
import me.antritus.astral.fluffycombat.manager.CombatManager;
import me.antritus.minecraft_server.wormhole.Wormhole;
import me.antritus.minecraft_server.wormhole.events.request.TpRequestAcceptEvent;
import me.antritus.minecraft_server.wormhole.events.request.TpRequestSendEvent;
import org.bukkit.configuration.MemorySection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class WormholeHook implements Hook{
	@NotNull
	private final FluffyCombat fluffyCombat;
	@Nullable
	private final Wormhole wormhole;
	@Nullable
	private final Class<?> clazz;

	@NotNull
	private final HookState state;

	private boolean settingDisableTpa = false;
	private boolean settingDisableTpaHere = false;
	private boolean settingDisableTpacceptHere = false;
	private boolean settingDisableTpacceptTo = false;


	public WormholeHook(@NotNull FluffyCombat fluffyCombat, @Nullable Wormhole wormhole, @Nullable Class<?> clazz, @NotNull HookState state) {
		this.fluffyCombat = fluffyCombat;
		this.wormhole = wormhole;
		this.clazz = clazz;
		this.state = state;
		if (state==HookState.HOOKED) {
			FileConfiguration fileConfiguration = fluffyCombat.getConfig();
			MemorySection memorySection = (MemorySection) fileConfiguration.get("hooks.wormhole.disabled-effects");
			settingDisableTpa = memorySection.getBoolean("tpa", false);
			settingDisableTpaHere = memorySection.getBoolean("tpahere", false);
			settingDisableTpacceptTo = memorySection.getBoolean("tpaccept.to", false);
			settingDisableTpacceptHere = memorySection.getBoolean("tpa.here", false);


			fluffyCombat.getServer().getPluginManager().registerEvents(new WormholeListener(fluffyCombat, this), fluffyCombat);
		}
	}

	public boolean settingDisableTpa() {
		return settingDisableTpa;
	}

	public boolean settingDisableTpaHere() {
		return settingDisableTpaHere;
	}

	public boolean settingDisableTpacceptHere() {
		return settingDisableTpacceptHere;
	}

	public boolean settingDisableTpacceptTo() {
		return settingDisableTpacceptTo;
	}

	@Override
	public FluffyCombat main() {
		return fluffyCombat;
	}

	@Override
	public JavaPlugin hookPlugin() {
		return wormhole;
	}

	@Override
	public Class<?> hookPluginClass() {
		return clazz;
	}

	@Override
	public HookState state() {
		return state;
	}

	public static class WormholeListener implements Listener {
		private final FluffyCombat fluffyCombat;
		private final WormholeHook wormholeHook;

		public WormholeListener(FluffyCombat fluffyCombat, WormholeHook wormholeHook) {
			this.fluffyCombat = fluffyCombat;
			this.wormholeHook = wormholeHook;
		}

		@EventHandler
		public void onTeleportRequest(TpRequestSendEvent event){
			CombatManager cM = fluffyCombat.getCombatManager();
			if (!cM.hasTags(event.getPlayer())){
				return;
			}
			if (!wormholeHook.settingDisableTpa){
				return;
			}
			event.setCancelled(true);
			MessageManager<?, ?, ?> mM = fluffyCombat.getMessageManager();
			mM.message(event.getPlayer(), "wormhole.request",
					new LegacyPlaceholder("who", event.getRequested().getName()));
		}

		@EventHandler
		public void onTeleportAccept(TpRequestAcceptEvent event){
			CombatManager cM = fluffyCombat.getCombatManager();
			if (!cM.hasTags(event.getPlayer())){
				return;
			}
			if (!wormholeHook.settingDisableTpacceptTo){
				return;
			}
			event.setCancelled(true);
			MessageManager<?, ?, ?> mM = fluffyCombat.getMessageManager();
			mM.message(event.getPlayer(), "wormhole.accept",
					new LegacyPlaceholder("who", event.getRequested().getName()));
		}
	}
}
