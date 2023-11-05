package me.antritus.astral.fluffycombat.api;

import me.antritus.astral.fluffycombat.FluffyCombat;
import me.antritus.astral.fluffycombat.astrolminiapi.Configuration;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.ApiStatus;

import java.util.UUID;

/**
 * @author Antritus
 * @since 1.0-SNAPSHOT
 */
public class CombatTag {
	private static final int ticks;
	static {
		FluffyCombat fluffy = FluffyCombat.getPlugin(FluffyCombat.class);
		Configuration configuration = fluffy.getConfig();
		ticks = configuration.getInt("time", 300);
	}
	private final FluffyCombat fluffyCombat;
	private final CombatUser victim;
	private final CombatUser attacker;
	private int ticksLeft = ticks;
	private boolean isDeadVictim = false;
	private boolean isDeadAttacker = false;

	/**
	 * Creates new instance of the class.
	 * This should not be initialized outside the combat manager.
	 * @param combat main class instance
	 * @param victim who was attacked
	 * @param attacker who attacked
	 */
	protected CombatTag(FluffyCombat combat, CombatUser victim, CombatUser attacker) {
		this.fluffyCombat = combat;
		this.victim = victim;
		this.attacker = attacker;
	}

	/**
	 * Returns the main class
	 * @return main class
	 */
	public FluffyCombat getFluffyCombat() {
		return fluffyCombat;
	}

	/**
	 * Returns the (attacker) of this combat tag.
	 * When combat tags are updated in combat, it doesn't matter who is the attacker.
	 * Attacker is just a label and doesn't get changed even that the victim might attack back.
	 * @return attacker's combat user
	 */
	public CombatUser getAttacker() {
		return attacker;
	}

	/**
	 * Returns the (victim) of this combat tag.
	 * When combat tags are updated in combat, it doesn't matter who is the victim.
	 * The Victim is just a label and doesn't get changed even that the attacker might be attacked by victim.
	 * @return attacker's combat user
	 */
	public CombatUser getVictim() {
		return victim;
	}

	/**
	 * Returns how many ticks they have left of this combat tag.
	 * When hits negative, it deletes this CombatTag from the managers.
	 * @return tag
	 */
	public int getTicksLeft() {
		return ticksLeft;
	}

	/**
	 * Updates the combat tags ticks if ticksLeft < 0 it deletes it in the next server tick.
	 * @param ticksLeft ticks
	 */
	public void setTicksLeft(int ticksLeft) {
		this.ticksLeft = ticksLeft;
	}

	/**
	 * Resets the ticks to the default amount.
	 */
	public void resetTicks() {
		this.ticksLeft = CombatTag.ticks;
	}


	@ApiStatus.Internal
	@ApiStatus.NonExtendable
	public boolean isDeadVictim() {
		return isDeadVictim;
	}

	@ApiStatus.Internal
	@ApiStatus.NonExtendable
	public boolean isDeadAttacker() {
		return isDeadAttacker;
	}

	@ApiStatus.Internal
	@ApiStatus.NonExtendable
	public void setDeadVictim(boolean deadVictim) {
		isDeadVictim = deadVictim;
	}

	@ApiStatus.Internal
	@ApiStatus.NonExtendable
	public void setDeadAttacker(boolean deadAttacker) {
		isDeadAttacker = deadAttacker;
	}

	/**
	 * Returns true if given player is in combat and not dead
	 * @param playerId player
	 * @return true if dead after tag
	 */
	public boolean isActive(UUID playerId) {
		if (victim.getUniqueId().equals(playerId)){
			return !isDeadVictim;
		} else if (attacker.getUniqueId().equals(playerId)){
			return !isDeadAttacker;
		}
		return false;
	}
	/**
	 * Returns true if given player is in combat and not dead
	 * @param player player
	 * @return true if dead after tag
	 */
	public boolean isActive(Player player) {
		return isActive(player.getUniqueId());
	}
}
