package bet.astral.fluffy.listeners.deathmessages;

import bet.astral.fluffy.FluffyCombat;
import bet.astral.fluffy.api.CombatTag;
import bet.astral.fluffy.api.CombatUser;
import bet.astral.fluffy.statistic.Account;
import bet.astral.messenger.v2.Messenger;
import bet.astral.messenger.v2.placeholder.Placeholder;
import bet.astral.messenger.v2.placeholder.PlaceholderList;
import net.kyori.adventure.text.logger.slf4j.ComponentLogger;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.lang.reflect.Field;

public class DeathListener implements Listener {
	private final Field nameField;
	{
		try {
			nameField = EntityType.class.getDeclaredField("name");
		} catch (NoSuchFieldException e) {
			throw new RuntimeException(e);
		}
	}

	private final FluffyCombat fluffy;
	private final Messenger msg;

	public DeathListener(FluffyCombat fluffy) {
		this.fluffy = fluffy;
		msg = fluffy.getMessenger();
	}

	@EventHandler
	private void onEntityDeathEvent(PlayerDeathEvent event){
		EntityDamageEvent damageEvent = event.getEntity().getLastDamageCause();
		if (damageEvent == null){
			getLogger().error("Damage event found null!");
			return;
		}
		final Player player = event.getPlayer();
		final CombatTag tag = fluffy.getCombatManager().getLatest(player);
		final CombatUser user = fluffy.getUserManager().getUser(player);
		final Account statisticUser = fluffy.getStatisticManager().get(player);
		getLogger().info("Died due to " + damageEvent.getCause().name());

		String messageKey;
//		PlaceholderList placeholders = new PlaceholderList(Placeholder.of("victim", (LivingEntity) player));
		ItemStack weapon = null;


		if (damageEvent instanceof EntityDamageByEntityEvent entityDamageEvent){
			Entity attacker = entityDamageEvent.getDamager();
			if (attacker instanceof LivingEntity lve && !(attacker instanceof Player)){
				if (lve.getEquipment() != null){
					weapon = lve.getEquipment().getItemInMainHand();
				}
			}
			if (attacker instanceof Player attackerPlayer){
				weapon = attackerPlayer.getInventory().getItemInMainHand();
				switch (damageEvent.getCause()){
					case ENTITY_SWEEP_ATTACK -> {
						messageKey = "entity_sweep_attack.player.";
					}
				}
			}

		}
	}

	private String createKeys(String key, Entity entity, @Nullable ItemStack itemStack) {
		return null;
	}

	private String asString(@Nullable ItemStack itemStack){
		return itemStack != null ? itemStack.getType().name().toLowerCase() : "default";
	}

	private String asString(@NotNull EntityType entityType){
		try {
			return (String) nameField.get(entityType);
		} catch (IllegalAccessException e) {
			throw new RuntimeException(e);
		}
	}



	private ComponentLogger getLogger(){
		return fluffy.getComponentLogger();
	}
}
