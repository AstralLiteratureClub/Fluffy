package bet.astral.fluffy.listeners.combat;

import bet.astral.fluffy.FluffyCombat;
import bet.astral.fluffy.api.BlockCombatUser;
import bet.astral.fluffy.api.CombatTag;
import bet.astral.fluffy.api.CombatUser;
import bet.astral.fluffy.configs.CombatConfig;
import bet.astral.fluffy.database.CombatLogDB;
import bet.astral.fluffy.events.CombatLogEvent;
import bet.astral.fluffy.manager.CombatManager;
import bet.astral.fluffy.manager.NPCManager;
import bet.astral.fluffy.manager.UserManager;
import bet.astral.fluffy.messenger.Placeholders;
import bet.astral.fluffy.messenger.Translations;
import bet.astral.messenger.v2.Messenger;
import bet.astral.messenger.v2.delay.Delay;
import bet.astral.messenger.v2.placeholder.collection.PlaceholderList;
import bet.astral.messenger.v2.placeholder.collection.PlaceholderMap;
import bet.astral.messenger.v2.translation.TranslationKey;
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
import java.util.UUID;

public class QuitWhileInCombatListener implements Listener {
	private final FluffyCombat fluffy;

	public QuitWhileInCombatListener(FluffyCombat fluffy) {
		this.fluffy = fluffy;
	}

	@EventHandler
	private void onJoin(PlayerJoinEvent event) {
		Player player = event.getPlayer();
		boolean logged = fluffy.getCombatLogManager().hasCombatLogged(player); // Automatically deleted
		fluffy.getCombatLogDB().getLog(player.getUniqueId())
				.thenAccept(log -> {
					if (log != null) {
						Messenger messenger = fluffy.getMessenger();
						TranslationKey playerMessage = Translations.COMBAT_REJOINED_PLAYER;
						TranslationKey broadCastMessage = Translations.COMBAT_REJOINED_PLAYER;

						PlaceholderMap placeholders = new PlaceholderMap();
						placeholders.addAll(Placeholders.playerPlaceholders("player", player));

						NPCManager npcManager = fluffy.getNpcManager();
						if (log.isKilled()) {
							playerMessage = Translations.COMBAT_REJOINED_PLAYER_KILLED;
							UUID killer = log.getKilledBy();
							if (killer != null) {
								placeholders.add("killer", Bukkit.getOfflinePlayer(killer).getName());
							}

							if (fluffy.getCombatConfig().getCombatLogAction() == CombatConfig.CombatLogAction.SPAWN_NPC) {
								player.getInventory().clear();
								player.setHealth(0);
							}
						} else {
							Object npc = fluffy.getNpcManager().getCombatLogNPC(player.getUniqueId());
							if (npc != null) {
								playerMessage = Translations.COMBAT_REJOINED_PLAYER_NPC_ALIVE;

								Location location = npcManager.getLocation(npc);
								if (location == null){
									return;
								}

								player.teleportAsync(location);
								npcManager.clearNPC(npc);
							}

						}

						messenger.broadcast(broadCastMessage, placeholders);
						messenger.message(player, Delay.ofTicks(10), playerMessage, placeholders);
					}
				});
	}

	@EventHandler
	private void onQuit(@NotNull PlayerQuitEvent event) {
		if (fluffy.getCombatConfig().getCombatLogAction()== CombatConfig.CombatLogAction.SPAWN_NPC){

		} else if (fluffy.getCombatConfig().getCombatLogAction()== CombatConfig.CombatLogAction.KILL){
		} else {
		}

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
			PlaceholderList placeholders = new PlaceholderList(Placeholders.playerPlaceholders("player", player));

			messenger.broadcast(Translations.COMBAT_LOGGED_BROADCAST, placeholders);

			CombatLogDB combatLogDB = fluffy.getCombatLogDB();
			combatLogDB.save(player.getUniqueId());

			if (fluffy.getCombatConfig().getCombatLogAction() == CombatConfig.CombatLogAction.NOTHING){
				return;
			} else if (fluffy.getCombatConfig().getCombatLogAction() == CombatConfig.CombatLogAction.SPAWN_NPC){
				NPCManager npcManager = fluffy.getNpcManager();
				npcManager.spawnNPC(player.getLocation(), player);
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

		Player player = event.getEntity();
		if (fluffy.getNpcManager().isNPC(player)){
			return;
		}

		if (fluffy.getCombatConfig().getCombatLogAction() != CombatConfig.CombatLogAction.KILL) {
			return;
		}
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
