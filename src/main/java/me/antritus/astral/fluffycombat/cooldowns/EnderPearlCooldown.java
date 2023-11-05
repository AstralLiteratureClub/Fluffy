package me.antritus.astral.fluffycombat.cooldowns;

import me.antritus.astral.fluffycombat.FluffyCombat;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

public class EnderPearlCooldown extends Cooldown {
	public EnderPearlCooldown(FluffyCombat fluffy, double seconds, NamespacedKey sound, boolean message) {
		super(fluffy, Material.ENDER_PEARL, seconds, sound, message);
	}

	@Override
	public void handleCooldown(Player player) {
		super.handleCooldown(player);
		new BukkitRunnable() {
			@Override
			public void run() {
				player.setCooldown(Material.ENDER_PEARL, ticks());
			}
		}.runTaskLater(fluffy(), 3);
	}
}