package bet.astral.fluffy.listeners;

import bet.astral.fluffy.FluffyCombat;
import bet.astral.fluffy.api.CombatCause;
import bet.astral.fluffy.api.CombatTag;
import bet.astral.fluffy.manager.CombatManager;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

public class DeathListener implements Listener {
	private final FluffyCombat fluffy;

	public DeathListener(@NotNull FluffyCombat fluffy) {
		this.fluffy = fluffy;
	}

	@EventHandler(priority = EventPriority.LOWEST)
	private void onDeath(PlayerDeathEvent event) {
		EntityDamageEvent entityDamageEvent = event.getEntity().getLastDamageCause();
		if (entityDamageEvent == null){
			return;
		}
		final Player player = event.getPlayer();
		Entity attacker = (entityDamageEvent instanceof EntityDamageByEntityEvent entityDamageByEntityEven ? entityDamageByEntityEven.getDamager() : null);
		final EntityDamageEvent.DamageCause cause = entityDamageEvent.getCause();
		final CombatManager combatManager = fluffy.getCombatManager();
		final CombatTag tag = combatManager.getLatest(player);
		final ItemStack weapon;
		boolean isVictim;
		if (tag != null){
			isVictim = tag.getVictim().getUniqueId().equals(player.getUniqueId());
			weapon = isVictim ? tag.getAttackerWeapon() : tag.getVictimWeapon();
		} else{
			weapon = null;
			isVictim = true;
		}
		String messageKey;
		switch (cause){
			case FALL -> {
				if (tag != null){
					if (isVictim ? tag.isVictimFalling() : tag.isAttackerFalling()){

					} //     "entity_has_displayname=true,item_has_displayname=false": "Option 2 for item displaynames"

				}
			}
		}
	}
}
