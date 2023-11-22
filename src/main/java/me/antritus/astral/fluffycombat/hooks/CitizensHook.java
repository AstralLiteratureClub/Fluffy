package me.antritus.astral.fluffycombat.hooks;

import com.destroystokyo.paper.profile.PlayerProfile;
import com.destroystokyo.paper.profile.ProfileProperty;
import me.antritus.astral.fluffycombat.FluffyCombat;
import me.antritus.astral.fluffycombat.hooks.citizens.CombatTrait;
import net.citizensnpcs.Citizens;
import net.citizensnpcs.api.CitizensAPI;
import net.citizensnpcs.api.event.DespawnReason;
import net.citizensnpcs.api.event.NPCDeathEvent;
import net.citizensnpcs.api.npc.NPC;
import net.citizensnpcs.api.npc.NPCRegistry;
import net.citizensnpcs.api.trait.Trait;
import net.citizensnpcs.api.trait.trait.Equipment;
import net.citizensnpcs.api.trait.trait.Inventory;
import net.citizensnpcs.trait.SkinTrait;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import org.bukkit.Location;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.inventory.PlayerInventory;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

public class CitizensHook implements Hook, Listener {
	private static final Map<UUID, UUID> uniqueIdTranslator = new HashMap<>();
	private static final Map<UUID, NPC> NPCs = new HashMap<>();
	private static final LegacyComponentSerializer serializer = LegacyComponentSerializer.legacySection();
	@NotNull
	private final FluffyCombat fluffyCombat;
	@Nullable
	private final Citizens citizens;
	@Nullable
	private final Class<?> clazz;
	@NotNull
	private final HookState state;


	public CitizensHook(@NotNull FluffyCombat fluffyCombat, @Nullable Citizens citizens, @Nullable Class<?> clazz, @NotNull HookState state) {
		this.fluffyCombat = fluffyCombat;
		this.citizens = citizens;
		this.clazz = clazz;
		this.state = state;
		if (state==HookState.HOOKED) {
			fluffyCombat.registerListeners(this);
		}
	}

	public void spawnNPC(Player player, Location location, Component displayname){
		NPCRegistry npcRegistry = CitizensAPI.getNPCRegistry();
		NPC npc  = npcRegistry.createNPC(EntityType.PLAYER, serializer.serialize(displayname), location);
		uniqueIdTranslator.put(player.getUniqueId(), npc.getUniqueId());
		uniqueIdTranslator.put(npc.getUniqueId(), player.getUniqueId());
		NPCs.put(npc.getUniqueId(), npc);
		// Skin
		PlayerProfile profile = player.getPlayerProfile();
		@NotNull Set<ProfileProperty> properties = profile.getProperties();
		boolean hasTextures = profile.hasTextures();
		String textures = null;
		String signatures = null;
		if (hasTextures){
			for (ProfileProperty profileProperty : properties){
				if (profileProperty==null){
					continue;
				}
				textures = profileProperty.getValue();
				signatures = profileProperty.getSignature();
			}
		}
		npc.addTrait(new CombatTrait(fluffyCombat.getUserManager().getUser(player)));

		npc.getOrAddTrait(SkinTrait.class).setTexture(textures, signatures);

		PlayerInventory playerInventory = player.getInventory();
		Equipment equipment = npc.getOrAddTrait(Equipment.class);
		equipment.set(Equipment.EquipmentSlot.OFF_HAND, playerInventory.getItemInOffHand());
		equipment.set(Equipment.EquipmentSlot.HELMET, playerInventory.getHelmet());
		equipment.set(Equipment.EquipmentSlot.CHESTPLATE, playerInventory.getChestplate());
		equipment.set(Equipment.EquipmentSlot.LEGGINGS, playerInventory.getLeggings());
		equipment.set(Equipment.EquipmentSlot.BOOTS, playerInventory.getBoots());
		Inventory inventoryTrait = npc.getOrAddTrait(Inventory.class);
		for (int i = 0; i < playerInventory.getSize(); i++){
			inventoryTrait.setItem(i, playerInventory.getItem(i));
		}
		npc.addTrait(Trait.class);
	}

	public void despawn(UUID player){
		NPC npc = getNPC(player);
		Inventory inventory = npc.getTraitNullable(Inventory.class);
		npc.despawn(DespawnReason.PLUGIN);
	}
	public void death(NPC npc){
		UUID playerId = uniqueIdTranslator.get(npc.getUniqueId());
		if (playerId != null) {
			despawn(playerId);
		}
	}
	public NPC getNPC(UUID player){
		return NPCs.get(uniqueIdTranslator.get(player));
	}

	@Override
	public FluffyCombat main() {
		return fluffyCombat;
	}

	@Override
	public Citizens hookPlugin() {
		return citizens;
	}

	@Override
	public Class<?> hookPluginClass() {
		return clazz;
	}

	@Override
	public HookState state() {
		return state;
	}




	@EventHandler
	public void onNPCDeath(NPCDeathEvent event){
		death(event.getNPC());
	}

}