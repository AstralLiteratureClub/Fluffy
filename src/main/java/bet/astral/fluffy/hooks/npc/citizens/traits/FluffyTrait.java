package bet.astral.fluffy.hooks.npc.citizens.traits;

import bet.astral.fluffy.api.CombatUser;
import lombok.Getter;
import lombok.Setter;
import net.citizensnpcs.api.trait.Trait;

import java.rmi.server.UID;
import java.util.UUID;

@Getter
@Setter
public class FluffyTrait extends Trait {
    boolean isCombatLogNPC = false;
    UUID combatLogUser = null;
    CombatUser combatUser = null;

    protected FluffyTrait() {
        super("flf");
    }
}
