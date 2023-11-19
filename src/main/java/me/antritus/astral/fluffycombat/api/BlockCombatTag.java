package me.antritus.astral.fluffycombat.api;

import me.antritus.astral.fluffycombat.FluffyCombat;
import org.jetbrains.annotations.NotNull;

public class BlockCombatTag extends CombatTag{

	/**
	 * Creates new instance of the class.
	 * This should not be initialized outside the combat manager.
	 *
	 * @param combat   main class instance
	 * @param victim   who was attacked
	 * @param attacker who attacked
	 */
	protected BlockCombatTag(FluffyCombat combat, CombatUser victim, BlockCombatUser attacker) {
		super(combat, victim, attacker);
	}


	@Override
	@NotNull
	public BlockCombatUser getAttacker() {
		return (BlockCombatUser) super.getAttacker();
	}


	public boolean isDeadAttacker(){
		return !getAttacker().isAlive();
	}

	public boolean isActive(BlockCombatUser block) {
		return !super.isDeadAttacker();
	}
}
