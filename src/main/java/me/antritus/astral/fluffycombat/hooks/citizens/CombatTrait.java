package me.antritus.astral.fluffycombat.hooks.citizens;

import me.antritus.astral.fluffycombat.api.CombatUser;
import net.citizensnpcs.api.trait.Trait;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;

import java.util.UUID;

public class CombatTrait extends Trait {
	private final CombatUser user;
	public CombatTrait(CombatUser user) {
		super("fluffy-npc-core");
		this.user = user;
	}
	public CombatUser getUser(){
		return user;
	}
	public UUID getOwner(){
		return user.getUniqueId();
	}
	public OfflinePlayer getOwnerPlayer(){
		return Bukkit.getOfflinePlayer(getOwner());
	}
}
