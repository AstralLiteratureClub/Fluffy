package bet.astral.fluffy.manager;

import bet.astral.fluffy.cooldowns.Cooldown;
import bet.astral.fluffy.cooldowns.EnderPearlCooldown;
import bet.astral.fluffy.FluffyCombat;
import bet.astral.fluffy.events.player.PlayerCombatFullEndEvent;
import bet.astral.fluffy.messenger.Translations;
import bet.astral.messenger.v2.placeholder.Placeholder;
import bet.astral.messenger.v2.translation.TranslationKey;
import net.kyori.adventure.sound.Sound;
import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.Registry;
import org.bukkit.configuration.MemorySection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.ProjectileLaunchEvent;
import org.bukkit.event.player.PlayerItemConsumeEvent;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;

public class CooldownManager implements Listener {
	private final Map<Material, Cooldown> cooldowns = new HashMap<>();
	private final FluffyCombat fluffy;

	public CooldownManager(FluffyCombat fluffy) {
		this.fluffy = fluffy;
		loadCooldowns();
	}

	public void register(@NotNull Material material, double seconds, @Nullable NamespacedKey sound, boolean message){
		if (material==Material.ENDER_PEARL){
			cooldowns.put(material, new EnderPearlCooldown(fluffy, seconds, sound, message));
		} else {
			cooldowns.put(material, new Cooldown(fluffy, material, seconds, sound, message));
		}
	}

	public void loadCooldowns(){
		cooldowns.clear();
		if (!fluffy.getCombatConfig().isCustomCooldowns()){
			return;
		}
		@SuppressWarnings("removal") FileConfiguration configuration = fluffy.getConfig();
		List<Map<?, ?>> cooldownListMap = configuration.getMapList("cooldowns.cooldowns");
		Registry<Material> materials = Registry.MATERIAL;
		for (Map<?, ?> cooldownMap : cooldownListMap){
			Material material = materials.get(Objects.requireNonNull(NamespacedKey.fromString((String) cooldownMap.get("material"))));
			if (material==null){
				continue;
			}
			Object objTime = cooldownMap.get("cooldown");
			double seconds;
			if (objTime instanceof Integer){
				seconds = (double) ((int) objTime);
			} else {
				 seconds = (double) cooldownMap.get("cooldown");
			}
			Object soundObj = configuration.get("sounds");
			NamespacedKey soundKeyUse = null;
			if (soundObj != null){
				MemorySection section = (MemorySection) cooldownMap.get("sounds");
				String soundKeyUseString = section.getString("use");
				if (soundKeyUseString != null) {
					soundKeyUse = NamespacedKey.fromString(soundKeyUseString);
				}
			}
			boolean message = (cooldownMap.get("message") != null ? (Boolean) cooldownMap.get("message") : false);
			register(material, seconds, soundKeyUse, message);
		}
	}

	@EventHandler
	public void onCombatEnd(PlayerCombatFullEndEvent event){
		if (!fluffy.getCombatConfig().isResetCooldownsOnDeath()){
			return;
		}
		if (event.isAsynchronous()){
			Bukkit.getScheduler().runTask(fluffy, () -> cooldowns.forEach(((material, cooldown) -> {
				if (event.player() instanceof Player){
					cooldown.remove((Player) event.player());
				}
			})));
			return;
		}
		cooldowns.forEach(((material, cooldown) -> {
			if (event.player() instanceof Player){
				cooldown.remove((Player) event.player());
			}
		}));
	}

	@EventHandler
	public void onEat(PlayerItemConsumeEvent event) {
		if (!fluffy.getCombatConfig().isCustomCooldowns()){
			return;
		}
		Material material = event.getItem().getType();
		if (cooldowns.get(material) != null){
			CombatManager combatManager = fluffy.getCombatManager();
			Player player = event.getPlayer();
			if (combatManager.hasTags(player)) {
				if (player.hasPermission("fluffy.bypass.combat-cooldown."+material.name().toLowerCase())){
					return;
				}
				Cooldown cooldown = cooldowns.get(material);
				if (!cooldown.hasCooldown(player)) {
					cooldown.handleCooldown(player);
					if (cooldown.sound() != null){
						assert cooldown.sound() != null;
						Sound sound = Sound.sound(
								Objects.requireNonNull(cooldown.sound()).key(),
								Sound.Source.PLAYER,
								1,
								1
						);
						player.playSound(sound);
					}
				} else {
					event.setCancelled(true);
					if (cooldown.message()){
						TranslationKey key = TranslationKey.of("listener.cooldown."+ cooldown.material().getKey());
						if (fluffy.getMessenger().getBaseComponent(key, Locale.US)==null){
							key = Translations.LISTENER_COOLDOWN_DEFAULT;
						}
						fluffy.getMessenger()
								.message(player, key, Placeholder.of("cooldown", cooldown.seconds()), Placeholder.of("type", Component.translatable(cooldown.material().translationKey())));
					}
				}
			}
		}
	}

	@EventHandler
	public void onEnderPearl(ProjectileLaunchEvent event) {
		if (!fluffy.getCombatConfig().isCustomCooldowns()){
			return;
		}
		if (event.getEntityType()== EntityType.ENDER_PEARL
				&& event.getEntity().getShooter() != null
				&& event.getEntity().getShooter() instanceof Player player
		){
			Material material = Material.ENDER_PEARL;
			if (cooldowns.get(material) != null){
				CombatManager combatManager = fluffy.getCombatManager();
				if (combatManager.hasTags(player)) {
					Cooldown cooldown = cooldowns.get(material);
					if (!cooldown.hasCooldown(player)) {
						cooldown.handleCooldown(player);
						if (cooldown.sound() != null){
							assert cooldown.sound() != null;
							Sound sound = Sound.sound(
									Objects.requireNonNull(cooldown.sound()).key(),
									Sound.Source.PLAYER,
									1,
									1
							);
							player.playSound(sound);
						}
						if (cooldown.message()) {
							TranslationKey key = TranslationKey.of("listener.cooldown." + cooldown.material().getKey());
							if (fluffy.getMessenger().getBaseComponent(key, Locale.US) == null) {
								key = Translations.LISTENER_COOLDOWN_DEFAULT;
							}
							fluffy.getMessenger()
									.message(player, key, Placeholder.of("cooldown", cooldown.seconds()), Placeholder.of("type", Component.translatable(cooldown.material().translationKey())));
						}
					} else {
						event.setCancelled(true);
					}
				}
			}
		}
	}
}