package bet.astral.fluffy.cooldowns;

import bet.astral.fluffy.FluffyCombat;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.Player;
import org.bukkit.event.Listener;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class Cooldown implements Listener {
	private Map<UUID, Long> cooldowns = new HashMap<>();
	private final FluffyCombat fluffy;
	private final Material material;
	private final double seconds;
	private final int ticks;
	private final long millis;
	private final NamespacedKey sound;
	private final boolean message;

	public Cooldown(FluffyCombat fluffy, Material material, double seconds, NamespacedKey sound, boolean message) {
		this.fluffy = fluffy;
		this.material = material;
		this.seconds = seconds;
		ticks = (int) (seconds * 20);
		millis = (long) (seconds*1000);
		this.sound = sound;
		this.message = message;
	}
	public Cooldown(FluffyCombat fluffy, Material material, double seconds, boolean message) {
		this.fluffy = fluffy;
		this.material = material;
		this.seconds = seconds;
		ticks = (int) (seconds * 20);
		millis = (long) (seconds*1000);
		this.message = message;
		this.sound = null;
	}


	public void handleCooldown(Player player){
		player.setCooldown(material, ticks);
		cooldowns.put(player.getUniqueId(), millis);
	}

	public boolean hasCooldown(Player player){
		return cooldowns.get(player.getUniqueId()) != null && cooldowns.get(player.getUniqueId()) > System.currentTimeMillis();
	}

	public void remove(Player player) {
		cooldowns.remove(player.getUniqueId());
		player.setCooldown(material, 0);
	}




	public Material material() {
		return material;
	}

	public double seconds() {
		return seconds;
	}

	public int ticks() {
		return ticks;
	}

	public long millis() {
		return millis;
	}

	public FluffyCombat fluffy() {
		return fluffy;
	}

	@Nullable
	public NamespacedKey sound() {
		return sound;
	}

	public boolean message() {
		return message;
	}
}
