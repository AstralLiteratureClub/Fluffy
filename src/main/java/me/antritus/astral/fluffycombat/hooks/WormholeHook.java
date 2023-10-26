package me.antritus.astral.fluffycombat.hooks;

import me.antritus.astral.fluffycombat.FluffyCombat;
import me.antritus.astral.fluffycombat.antsfactions.MessageManager;
import me.antritus.astral.fluffycombat.manager.CombatManager;
import me.antritus.minecraft_server.wormhole.Wormhole;
import me.antritus.minecraft_server.wormhole.events.request.TpRequestAcceptEvent;
import me.antritus.minecraft_server.wormhole.events.request.TpRequestSendEvent;
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

	public WormholeHook(@NotNull FluffyCombat fluffyCombat, @Nullable Wormhole wormhole, @Nullable Class<?> clazz, @NotNull HookState state) {
		this.fluffyCombat = fluffyCombat;
		this.wormhole = wormhole;
		this.clazz = clazz;
		this.state = state;
		if (state==HookState.HOOKED) {
			fluffyCombat.getServer().getPluginManager().registerEvents(new WormholeListener(fluffyCombat), fluffyCombat);
		}
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

		public WormholeListener(FluffyCombat fluffyCombat) {
			this.fluffyCombat = fluffyCombat;
		}

		@EventHandler
		public void onTeleportRequest(TpRequestSendEvent event){
			CombatManager cM = fluffyCombat.getCombatManager();
			if (!cM.hasTags(event.getPlayer())){
				return;
			}
			event.setCancelled(true);
			MessageManager mM = fluffyCombat.getMessageManager();
			mM.message(event.getPlayer(), "wormhole.request", "%who%="+event.getRequested().getName());
		}

		@EventHandler
		public void onTeleportAccept(TpRequestAcceptEvent event){
			CombatManager cM = fluffyCombat.getCombatManager();
			if (!cM.hasTags(event.getPlayer())){
				return;
			}
			event.setCancelled(true);
			MessageManager mM = fluffyCombat.getMessageManager();
			mM.message(event.getPlayer(), "wormhole.accept", "%who%="+event.getRequested().getName());
		}
	}
}
