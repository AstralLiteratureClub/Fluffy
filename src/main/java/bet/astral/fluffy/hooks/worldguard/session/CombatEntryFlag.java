package bet.astral.fluffy.hooks.worldguard.session;

import bet.astral.fluffy.FluffyCombat;
import bet.astral.fluffy.hooks.worldguard.FluffyWGFlags;
import bet.astral.fluffy.messenger.Translations;
import bet.astral.messenger.v2.info.MessageInfo;
import bet.astral.messenger.v2.info.MessageInfoBuilder;
import com.sk89q.worldedit.util.Location;
import com.sk89q.worldguard.LocalPlayer;
import com.sk89q.worldguard.protection.ApplicableRegionSet;
import com.sk89q.worldguard.protection.regions.ProtectedRegion;
import com.sk89q.worldguard.session.MoveType;
import com.sk89q.worldguard.session.Session;
import com.sk89q.worldguard.session.handler.Handler;

import java.util.Set;

public class CombatEntryFlag extends FluffyHandler {
    private long lastMessage;
    private long lastExtended;

    public CombatEntryFlag(Session session, FluffyCombat combat) {
        super(session, combat);
    }

    public boolean onCrossBoundary(LocalPlayer player, Location from, Location to, ApplicableRegionSet toSet, Set<ProtectedRegion> entered, Set<ProtectedRegion> exited, MoveType moveType) {
        boolean allowed = toSet.testState(player, FluffyWGFlags.ENTER_ZONE_IN_COMBAT);
        boolean extend = toSet.testState(player, FluffyWGFlags.EXTEND_COMBAT_IF_ENTER);

        boolean inCombat = getCombatManager().hasTags(player.getUniqueId());

        if (    !allowed  // Cannot enter the region (by flag)
                && inCombat  // Combat is true
                && moveType.isCancellable()) {

            long now = System.currentTimeMillis();
            if (now- this.lastMessage > 1000L){
                MessageInfo messageInfo = new MessageInfoBuilder(Translations.REGION_ENTER_IN_COMBAT)
                        .withReceiver(player)
                        .build();

                getMessenger().send(messageInfo);
                this.lastMessage = now;
            }

            if (extend && now-this.lastExtended > 1000){
                MessageInfo lastExtended = new MessageInfoBuilder(Translations.REGION_ENTER_IN_COMBAT_COMBAT_EXTENDED)
                        .withReceiver(player)
                        .build();

                getMessenger().send(lastExtended);


                getCombatManager().resetTagTicks(player.getUniqueId());
                this.lastExtended = now;
            }
            return false;
        } else {
            return true;
        }
    }

    public static class Factory extends Handler.Factory<CombatEntryFlag> {
        public Factory() {
        }

        public CombatEntryFlag create(Session session) {
            return new CombatEntryFlag(session, FluffyCombat.getPlugin(FluffyCombat.class));
        }
    }
}
