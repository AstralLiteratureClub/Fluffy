package me.antritus.astral.fluffycombat.api;

public enum CombatCause {
	/**
	 * TNTPrimed
	 */
	TNT,
	/**
	 *  EnderCrystal
	 */
	ENDER_CRYSTAL,
	/**
	 * RespawnAnchor
	 */
	RESPAWN_ANCHOR,
	/**
	 * Bed
	 */
	BED,
	/**
	 * Projectile
	 */
	PROJECTILE,
	/**
	 * Player melee
	 */
	MELEE,
	/**
	 * SplashPotion
	 */
	SPLASH_POTION,
	/**
	 * LingeringPotion
	 */
	LINGERING_POTION,
	/**
	 * STATUS_EFFECT FROM POTION
	 */
	EFFECT_STATUS,

	/**
	 * Fire placed by players, fire spread & ignited from player's lava
	 */
	FIRE,
	/**
	 * Lava placed by players & spread
	 */
	LAVA
}
