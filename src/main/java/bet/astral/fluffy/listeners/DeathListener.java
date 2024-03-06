package bet.astral.fluffy.listeners;

import bet.astral.fluffy.FluffyCombat;
import bet.astral.fluffy.api.CombatTag;
import bet.astral.fluffy.api.CombatUser;
import bet.astral.fluffy.api.StatisticUser;
import bet.astral.messenger.Message;
import bet.astral.messenger.Messenger;
import bet.astral.messenger.placeholder.PlaceholderList;
import bet.astral.messenger.utils.PlaceholderUtils;
import net.kyori.adventure.text.logger.slf4j.ComponentLogger;
import org.bukkit.Material;
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
import oshi.hardware.platform.linux.LinuxUsbDevice;

import java.lang.reflect.Field;
import java.util.LinkedList;
import java.util.List;

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
	private final Messenger<FluffyCombat> msg;

	public DeathListener(FluffyCombat fluffy) {
		this.fluffy = fluffy;
		msg = fluffy.getMessageManager();
	}

	@EventHandler
	public void onEntityDeathEvent(PlayerDeathEvent event){
		EntityDamageEvent damageEvent = event.getEntity().getLastDamageCause();
		if (damageEvent == null){
			getLogger().error("Damage event found null!");
			return;
		}
		final Player player = event.getPlayer();
		final CombatTag tag = fluffy.getCombatManager().getLatest(player);
		final CombatUser user = fluffy.getUserManager().getUser(player);
		final StatisticUser statisticUser = fluffy.getStatisticManager().get(player);
		getLogger().info("Died due to " + damageEvent.getCause().name());

		String messageKey;
		PlaceholderList placeholders = new PlaceholderList(PlaceholderUtils.createPlaceholders("victim", (LivingEntity) player));
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
