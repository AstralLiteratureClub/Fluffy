package bet.astral.fluffy.api.events;

import lombok.Getter;
import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.HandlerList;
import org.bukkit.event.player.PlayerEvent;
import org.jetbrains.annotations.NotNull;

public class PlayerLandOnGroundEvent extends PlayerEvent {
	private static final HandlerList HANDLERS = new HandlerList();
	@Getter
	private final Block block;
	@Getter
	private final Location location;

	public PlayerLandOnGroundEvent(Player player, Block block, Location location) {
		super(player);
		this.block = block;
		this.location = location;
	}

	@NotNull
	public static HandlerList getHandlerList(){
		return HANDLERS;
	}
	@Override
	public @NotNull HandlerList getHandlers() {
		return HANDLERS;
	}
}
