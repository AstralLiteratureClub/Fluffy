package bet.astral.fluffy.hooks.npc.sentinel;

import bet.astral.fluffy.hooks.npc.citizens.CitizensHook;
import bet.astral.fluffy.hooks.npc.citizens.CitizensNPCManager;
import net.citizensnpcs.api.npc.NPC;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.Listener;
import org.mcmonkey.sentinel.SentinelTrait;

import java.util.UUID;

public class SentinelNPCManager extends CitizensNPCManager implements Listener {
    public SentinelNPCManager(SentinelHook sentinelHook) {
        super((CitizensHook) sentinelHook.main().getHookManager().getHook("citizens"));
    }

    @Override
    public UUID spawnNPC(Location location, Player whoToClone) {
        UUID npcId = super.spawnNPC(location, whoToClone);
        NPC npc = getNPC(npcId);
        if (npc == null){
            whoToClone.sendMessage("Nope");
            return null;
        }
        SentinelTrait sentinelTrait = npc.getOrAddTrait(SentinelTrait.class);
        sentinelTrait.setHealth(whoToClone.getHealth());
        sentinelTrait.addTarget("entity:all");
        sentinelTrait.setInvincible(false);
//        sentinelTrait.addTarget("fluffy:ignore");
//        sentinelTrait.addAvoid("uuid:"+whoToClone.getName());

        return npcId;
    }
}
