package bet.astral.fluffy.listeners.combat;

import bet.astral.fluffy.FluffyCombat;
import bet.astral.fluffy.api.BlockCombatUser;
import bet.astral.fluffy.api.CombatTag;
import bet.astral.fluffy.api.CombatUser;
import bet.astral.fluffy.configs.CombatConfig;
import bet.astral.fluffy.events.CombatLogEvent;
import bet.astral.fluffy.manager.CombatManager;
import bet.astral.fluffy.manager.UserManager;
import bet.astral.fluffy.messenger.Placeholders;
import bet.astral.fluffy.messenger.Translations;
import bet.astral.messenger.v2.Messenger;
import bet.astral.messenger.v2.placeholder.Placeholder;
import org.bukkit.*;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityResurrectEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public class QuitWhileInCombatListener implements Listener {
	private final FluffyCombat fluffy;

	public QuitWhileInCombatListener(FluffyCombat fluffy) {
		this.fluffy = fluffy;
	}

	@EventHandler
	private void onJoin(PlayerJoinEvent event) {
		Player player = event.getPlayer();
		boolean logged = fluffy.getCombatLogManager().hasCombatLogged(player); // Automatically deleted
		if (logged){
			Messenger messenger = fluffy.getMessenger();
			Placeholder[] placeholders = Placeholders.playerPlaceholders("player", player).toArray(Placeholder[]::new);
			/**
			if (Objects.requireNonNull(fluffy.getCombatConfig().getCombatLogAction()) == CombatConfig.CombatLogAction.SPAWN_NPC) {
				// TODO - Make the message chosen if the NPC is alive or dead.
//					fluffy.getMessageManager().broadcast(MessageKey.COMBAT_REJOIN_NPC_REPLACEMENT_DEAD_BROADCAST, placeholders);
				messenger.message(messenger.broadcast(), Translations.COMBAT_REJOIN_NPC_REPLACEMENT_ALIVE_BROADCAST, placeholders);
//					fluffy.getMessageManager().message(player, MessageKey.COMBAT_REJOIN_NPC_REPLACEMENT_DEAD, placeholders);
				messenger.message(player, MessageKey.COMBAT_REJOIN_NPC_REPLACEMENT_ALIVE, placeholders);
			} else {
			 */
				messenger.broadcast(Translations.COMBAT_REJOINED_BROADCAST, placeholders);
				messenger.message(player, Translations.COMBAT_REJOINED_BROADCAST, placeholders);
//			}
		}
	}

	@EventHandler
	private void onQuit(@NotNull PlayerQuitEvent event) {
		if (FluffyCombat.isStopping) {
			return;
		}
		Player player = event.getPlayer();
		if (player.hasPermission("fluffy.bypass.combat-log")) {
			return;
		}
		CombatManager cM = fluffy.getCombatManager();
		UserManager uM = fluffy.getUserManager();
		if (cM.hasTags(player)) {
			Messenger messenger = fluffy.getMessenger();
			CombatUser user = uM.getUser(event.getPlayer());
			CombatLogEvent logEvent = new CombatLogEvent(fluffy, player, cM.getTags(player));
			if (logEvent.isCancelled()) {
				return;
			}
			List<Placeholder> placeholders = Placeholders.playerPlaceholders("player", player);
			Placeholder[] placeholderArray = placeholders.toArray(Placeholder[]::new);
			messenger.message(messenger.broadcast(), Translations.COMBAT_LOGGED_BROADCAST, placeholderArray);

			if (fluffy.getCombatConfig().getCombatLogAction() == CombatConfig.CombatLogAction.NOTHING){
				return;
			} else if (fluffy.getCombatConfig().getCombatLogAction() == CombatConfig.CombatLogAction.SPAWN_NPC){
				return;
			} else if (fluffy.getCombatConfig().getCombatLogAction() == CombatConfig.CombatLogAction.KILL) {
				boolean destroyed = false;
				user.setting("logged", true);
				while (true){
					if (destroyed){
						CombatTag latest = fluffy.getCombatManager().getLatest(player);
						CombatUser opposite = latest.getOpposite(player);
						if (opposite instanceof BlockCombatUser) {
							player.damage(100000D);
						} else {
							player.damage(100000D, Bukkit.getEntity(opposite.getUniqueId()));
						}
						fluffy.getCombatManager().getTags(player)
								.forEach(tag->{
									tag.setVictimTicksLeft(-1);
									tag.setAttackerTicksLeft(-1);
									if (tag.getVictim().getUniqueId().equals(player.getUniqueId())){
										tag.setDeadVictim(true);
									} else {
										tag.setDeadAttacker(true);
									}
								});
						break;
					}
					if (player.getInventory().getItemInMainHand().getType() == Material.TOTEM_OF_UNDYING){
						EntityResurrectEvent entityResurrectEvent = new EntityResurrectEvent(player , EquipmentSlot.HAND);
						if (!entityResurrectEvent.callEvent()){
							break;
						}
						player.getInventory().setItemInMainHand(null);
						continue;
					} else if (player.getInventory().getItemInOffHand().getType()==Material.TOTEM_OF_UNDYING){
						EntityResurrectEvent entityResurrectEvent = new EntityResurrectEvent(player , EquipmentSlot.OFF_HAND);
						if (!entityResurrectEvent.callEvent()){
							break;
						}
						player.getInventory().setItemInOffHand(null);
						Location location = event.getPlayer().getEyeLocation();
						World world = location.getWorld();
						world.spawnParticle(Particle.TOTEM_OF_UNDYING, location, 1);
						world.playSound(location, Sound.ITEM_TOTEM_USE, 1, 1);
						continue;
					}
					destroyed = true;
				}
			}
		}
	}


	@SuppressWarnings("removal")
	@EventHandler(priority = EventPriority.NORMAL)
	private void onDeath(PlayerDeathEvent event) {
		if (FluffyCombat.isStopping) {
			return;
		}
		if (fluffy.getCombatConfig().getCombatLogAction() != CombatConfig.CombatLogAction.KILL) {
			return;
		}

		Player player = event.getEntity();
		UserManager uM = fluffy.getUserManager();
		CombatUser user = uM.getUser(player);
		assert user != null;
		Object property = user.get("logged");
		if (property != null) {
			if (property instanceof Boolean && (boolean) property) {
				user.setting("logged", null);
				CombatConfig config = fluffy.getCombatConfig();
				event.setKeepLevel(config.isCombatLogKillKeepExp());
				event.setKeepInventory(config.isCombatLogKillKeepItem());
			}
		}
	}
}
