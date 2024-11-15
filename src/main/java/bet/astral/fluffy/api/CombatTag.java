package bet.astral.fluffy.api;

import bet.astral.fluffy.FluffyCombat;
import lombok.Getter;
import lombok.Setter;
import org.bukkit.OfflinePlayer;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;

import java.util.UUID;

/**
 * @author Antritus
 * @since 1.0-SNAPSHOT
 */
@Getter
@Setter
public class CombatTag {
	private static final int ticks;
	static {
		FluffyCombat fluffy = FluffyCombat.getPlugin(FluffyCombat.class);
		FileConfiguration configuration = fluffy.getConfig();
		ticks = configuration.getInt("time", 300);
	}
	private final FluffyCombat fluffyCombat;
	private final CombatUser victim;
	@NotNull
	private final CombatUser attacker;
	private int victimTicksLeft = ticks;
	private int attackerTicksLeft = ticks;
	private boolean isDeadVictim = false;
	private boolean isDeadAttacker = false;
	private CombatCause victimCombatCause;
	private CombatCause attackerCombatCause;
	private ItemStack victimWeapon;
	private ItemStack attackerWeapon;

	private int victimRejoinTimer;
	private int attackerRejoinTimer;
	private double victimDamageDealt;
	private double attackerDamageDealt;
	private boolean isVictimFalling = true;
	private boolean isAttackerFalling = true;

	/**
	 * Creates new instance of the class.
	 * This should not be initialized outside the combat manager.
	 * @param combat main class instance
	 * @param victim who was attacked
	 * @param attacker who attacked
	 */
	protected CombatTag(FluffyCombat combat, CombatUser victim, @NotNull CombatUser attacker) {
		this.fluffyCombat = combat;
		this.victim = victim;
		this.attacker = attacker;
	}

	/**
	 * Returns the main class
	 * @return main class
	 */
	@NotNull
	public FluffyCombat getFluffyCombat() {
		return fluffyCombat;
	}

	/**
	 * Returns the (attacker) of this combat tag.
	 * When combat tags are updated in combat, it doesn't matter who is the attacker.
	 * Attacker is just a label and doesn't get changed even that the victim might attack back.
	 * @return attacker's combat user
	 */
	@NotNull
	public CombatUser getAttacker() {
		return attacker;
	}

	/**
	 * Returns the (victim) of this combat tag.
	 * When combat tags are updated in combat, it doesn't matter who is the victim.
	 * The Victim is just a label and doesn't get changed even that the attacker might be attacked by victim.
	 * @return attacker's combat user
	 */
	@NotNull
	public CombatUser getVictim() {
		return victim;
	}


	/**
	 * Returns the ticks left for given user.
	 * @param user user
	 * @return ticks
	 */
	public int getTicksLeft(CombatUser user){
		if (attacker==user){
			return attackerTicksLeft;
		}
		return victimTicksLeft;
	}
	/**
	 * Resets the ticks to the default amount.
	 */
	public void resetTicks() {
		this.attackerTicksLeft = CombatTag.ticks;
		this.victimTicksLeft = CombatTag.ticks;
	}


	@ApiStatus.Internal
	public final boolean isDeadVictim() {
		return isDeadVictim;
	}

	@ApiStatus.Internal
	public boolean isDeadAttacker() {
		return isDeadAttacker;
	}

	@ApiStatus.Internal
	public final void setDeadVictim(boolean deadVictim) {
		isDeadVictim = deadVictim;
	}

	@ApiStatus.Internal
	public final void setDeadAttacker(boolean deadAttacker) {
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

	public CombatUser getUser(Player player) {
		return player.getUniqueId().equals(this.victim.getUniqueId()) ? this.victim : attacker;
	}

	public double getDamageDealt(OfflinePlayer player){
		return player.getUniqueId().equals(this.victim.getUniqueId()) ? this.victimDamageDealt : this.attackerDamageDealt;
	}
	public void setDamageDealt(OfflinePlayer player, double amount){
		if (player.getUniqueId().equals(this.victim.getUniqueId())){
			this.victimDamageDealt = amount;
		} else {
			this.attackerDamageDealt = amount;
		}
	}

	public CombatUser getOpposite(@NotNull Player player){
		return player.getUniqueId().equals(this.victim.getUniqueId()) ? this.attacker : victim;
	}
	public CombatUser getOpposite(@NotNull UUID uniqueId){
		return uniqueId.equals(this.victim.getUniqueId()) ? this.attacker : victim;
	}

	public void setTicksLeft(UUID uniqueId, int ticks){
		if (victim.getUniqueId()==uniqueId){
			setVictimTicksLeft(ticks);
		} else {
			setAttackerTicksLeft(ticks);
		}
	}
}
