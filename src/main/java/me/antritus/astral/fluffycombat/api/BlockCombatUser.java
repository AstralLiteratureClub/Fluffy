package me.antritus.astral.fluffycombat.api;

import lombok.Getter;
import lombok.Setter;
import me.antritus.astral.fluffycombat.FluffyCombat;
import org.bukkit.Location;
import org.bukkit.OfflinePlayer;
import org.bukkit.block.Block;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.UUID;

public class BlockCombatUser extends CombatUser {
	@Getter
	private final Block block;
	@Getter
	private final Location location;
	@Getter
	@Setter
	private boolean isAlive = true;

	/**
	 * Generates new user lol
	 *
	 * @param combat main instance
	 * @param block  block
	 */
	protected BlockCombatUser(@NotNull FluffyCombat combat, @NotNull Block block) {
		super(combat, null);
		this.block = block;
		this.location = block.getLocation();
	}


	/**
	 * Returns null as blocks don't have player forms
	 * @return null
	 */
	@Override
	public OfflinePlayer getPlayer() {
		return null;
	}

	/**
	 * Returns null as block's don't have their own ids.
	 * @return null
	 */
	@Override
	@Nullable
	public UUID getUniqueId() {
		return null;
	}
}
