package bet.astral.fluffy.hooks.npc.citizens;

import bet.astral.fluffy.api.CombatUser;
import bet.astral.fluffy.hooks.npc.citizens.traits.FluffyTrait;
import bet.astral.fluffy.manager.NPCManager;
import bet.astral.fluffy.manager.UserManager;
import net.citizensnpcs.Citizens;
import net.citizensnpcs.api.ai.EntityTarget;
import net.citizensnpcs.api.event.DespawnReason;
import net.citizensnpcs.api.event.NPCDeathEvent;
import net.citizensnpcs.api.npc.NPC;
import net.citizensnpcs.api.npc.NPCRegistry;
import net.citizensnpcs.api.trait.trait.Equipment;
import net.citizensnpcs.api.trait.trait.Inventory;
import net.citizensnpcs.npc.ai.NPCHolder;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.OfflinePlayer;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.*;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;

public class CitizensNPCManager extends NPCManager implements Listener {
    private final CitizensHook citizensHook;
    private final Map<UUID, UUID> combatLogNPCs = new HashMap<>();

    public CitizensNPCManager(CitizensHook citizensHook) {
        this.citizensHook = citizensHook;
    }

    @Override
    public Location getLocation(Object npc) {
        if (npc instanceof NPC npcReal){
            return npcReal.getStoredLocation();
        }
        return null;
    }

    @Override
    public UUID spawnNPC(Location location, Player whoToClone) {
        whoToClone.sendMessage("Spawn");
        Citizens citizens = citizensHook.hookPlugin();
        NPCRegistry registry = citizens.getNPCRegistry();
        NPC npc = registry.createNPC(EntityType.PLAYER, whoToClone.getName());
        npc.getNavigator().getLocalParameters().attackRange(3.0)
                .speedModifier(50)
                .avoidWater(true);
        npc.setFlyable(false);

        Inventory inventory = npc.getOrAddTrait(Inventory.class);

        PlayerInventory playerInventory = whoToClone.getInventory();
        for (int i = 0; i < 36; i++){
            inventory.setItem(i, playerInventory.getItem(i));
        }

        int handSlot = playerInventory.getHeldItemSlot();
        inventory.setItem(handSlot, null);

        Equipment equipment = npc.getOrAddTrait(Equipment.class);
        equipment.set(Equipment.EquipmentSlot.HELMET, playerInventory.getItem(EquipmentSlot.HEAD));
        equipment.set(Equipment.EquipmentSlot.CHESTPLATE, playerInventory.getItem(EquipmentSlot.CHEST));
        equipment.set(Equipment.EquipmentSlot.LEGGINGS, playerInventory.getItem(EquipmentSlot.LEGS));
        equipment.set(Equipment.EquipmentSlot.BOOTS, playerInventory.getItem(EquipmentSlot.FEET));
        equipment.set(Equipment.EquipmentSlot.HAND, playerInventory.getItem(EquipmentSlot.HAND));
        equipment.set(Equipment.EquipmentSlot.OFF_HAND, playerInventory.getItem(EquipmentSlot.OFF_HAND));

        npc.setProtected(false);
        npc.setProtected(false);
        npc.setAlwaysUseNameHologram(true);

        if (citizensHook.main().getHookManager().getHook("sentinel") == null){
            npc.addRunnable(()-> {
                Location loc = npc.getStoredLocation();
                EntityTarget target = npc.getNavigator().getEntityTarget();
                if (target == null || target.getTarget().isDead() || target.getTarget().getLocation().distanceSquared(loc)>20){
                    List<LivingEntity> nearestEntities = new ArrayList<>(loc.getNearbyLivingEntities(10));
                    nearestEntities.sort(Comparator.comparingDouble(entity -> loc.distanceSquared(entity.getLocation())));
                }
            });
        }
        FluffyTrait fluffyTrait = npc.getOrAddTrait(FluffyTrait.class);
        fluffyTrait.setCombatLogNPC(true);
        fluffyTrait.setCombatLogUser(whoToClone.getUniqueId());

        UserManager userManager = citizensHook.main().getUserManager();
        CombatUser combatUser = userManager.getUser(whoToClone);
        fluffyTrait.setCombatUser(combatUser);

        userManager.reassignCombatUser(whoToClone, npc.getUniqueId());

        npc.spawn(location);
        Entity entity = npc.getEntity();
        LivingEntity livingEntity = (LivingEntity) entity;
        livingEntity.setInvulnerable(false);

        combatLogNPCs.put(whoToClone.getUniqueId(), npc.getUniqueId());
        return npc.getUniqueId();
    }


    @Override
    public @Nullable NPC getNPC(UUID uniqueId) {
        if (uniqueId == null){
                return null;
        }
        return citizensHook.hookPlugin().getNPCRegistry().getByUniqueId(uniqueId);
    }

    @Override
    public boolean isNPC(Object object) {
        return object instanceof NPC || object instanceof NPCHolder;
    }

    @Override
    public boolean isFluffyNPC(Object object) {
        return isNPC(object) && object instanceof NPC npc && npc.hasTrait(FluffyTrait.class) ||
                isNPC(object) && object instanceof NPCHolder npcHolder && npcHolder.getNPC().hasTrait(FluffyTrait.class);
    }

    @EventHandler(ignoreCancelled = true)
    public void onNPCDeath(NPCDeathEvent event) {
        Bukkit.broadcastMessage("hello2)");
        if (isFluffyNPC(event.getNPC())){
            NPC npc = event.getNPC();
            Inventory inventory = npc.getOrAddTrait(Inventory.class);
            List<ItemStack> itemStacks = new LinkedList<>();

            if (inventory.getContents()!=null) {
                itemStacks.addAll(Arrays.asList(inventory.getContents()));
            }

            Equipment equipment = npc.getOrAddTrait(Equipment.class);
            if (equipment.getEquipment() != null) {
                itemStacks.addAll(Arrays.asList(equipment.getEquipment()));
                itemStacks.remove(equipment.get(Equipment.EquipmentSlot.HAND));
            }

            if (!itemStacks.isEmpty()){
                itemStacks.removeAll(
                        event.getDrops().stream().filter(
                                item->item == null || item.containsEnchantment(Enchantment.BINDING_CURSE)
                        ).toList()
                );
            }

            event.getDrops().clear();
            event.getDrops().addAll(itemStacks);

            event.getNPC().despawn();
            event.getNPC().destroy();

        }
    }

    @Override
    public Object getCombatLogNPC(@NotNull UUID uniqueId) {
        return getNPC(combatLogNPCs.get(uniqueId));
    }

    @Override
    public UUID getUniqueId(Object object) {
        return object instanceof Entity entity ? entity.getUniqueId() : null;
    }

    @Override
    public boolean isCombatLogNPC(Object object) {
        UUID id = getUniqueId(object);
        if (id == null){
            return false;
        }
        return combatLogNPCs.containsValue(getUniqueId(object));
    }

    @Override
    public void clearNPC(Object obj) {
        if (isNPC(obj)){
            NPC player = (NPC) obj;
            NPC npc = getNPC(player.getUniqueId());
            npc.despawn(DespawnReason.REMOVAL);
            npc.destroy();
            return;
        }

        if (obj instanceof Player player) {
            UUID npcID = combatLogNPCs.get(player.getUniqueId());
            if (npcID == null){
                return;
            }

            NPC npc = getNPC(npcID);
            if (npc==null){
                return;
            }
            npc.despawn(DespawnReason.REMOVAL);
            npc.destroy();
        }
    }

    @Override
    @Nullable
    public OfflinePlayer getOwnerFromNPC(Object object) {
        if (isFluffyNPC(object)){
            FluffyTrait trait = ((NPC)object).getTraitNullable(FluffyTrait.class);
            if (trait == null){
                return null;
            }

            return Bukkit.getOfflinePlayer(trait.getCombatLogUser());
        }
        return null;
    }
}
