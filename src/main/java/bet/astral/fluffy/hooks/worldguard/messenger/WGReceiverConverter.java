package bet.astral.fluffy.hooks.worldguard.messenger;

import bet.astral.messenger.v2.receiver.Receiver;
import bet.astral.messenger.v2.receiver.ReceiverConverter;
import bet.astral.messenger.v3.minecraft.paper.PaperMessenger;
import com.sk89q.worldedit.entity.Player;

public class WGReceiverConverter implements ReceiverConverter {
    @Override
    public Receiver apply(Object o) {
        if (o instanceof Player player){
            return PaperMessenger.playerManager.players.get(player.getUniqueId());
        }
        return null;
    }
}
