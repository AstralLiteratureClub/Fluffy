package me.antritus.astral.fluffycombat.listeners;

import me.antritus.astral.fluffycombat.FluffyCombat;
import me.antritus.astral.fluffycombat.antsfactions.MessageManager;
import me.antritus.astral.fluffycombat.api.CombatTag;
import me.antritus.astral.fluffycombat.api.events.CombatEnterEvent;
import me.antritus.astral.fluffycombat.manager.CombatManager;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;

/**
 * @author Antritus
 * @since 1.0-SNAPSHOT
 */
public class CombatEnterListener implements Listener {
	private final FluffyCombat combat;

	public CombatEnterListener(FluffyCombat combat) {
		this.combat = combat;
	}

	@EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
	public void onEntityDamage(EntityDamageByEntityEvent event){
		if (!(event.getEntity() instanceof Player victim)){
			return;
		}
		if (!(event.getDamager() instanceof Player attacker)){
			return;
		}
		CombatManager cM = combat.getCombatManager();
		MessageManager mm = combat.getMessageManager();
		if (!cM.hasTags(victim)){
			mm.message(victim, "combat-enter.victim", "%attacker%="+attacker.getUniqueId());
		}
		if (!cM.hasTags(attacker)){
			mm.message(attacker, "combat-enter.attacker", "%victim%="+victim.getUniqueId());
		}
		CombatTag tag = cM.getTag(victim, attacker);
		if (!combat.getCombatManager().hasTag(victim, attacker)){
			tag = combat.getCombatManager().create(victim, attacker);
		}
		assert tag != null;
		tag.resetTicks();
		CombatEnterEvent enterEvent = new CombatEnterEvent(combat, tag);
		enterEvent.callEvent();
	}
}
