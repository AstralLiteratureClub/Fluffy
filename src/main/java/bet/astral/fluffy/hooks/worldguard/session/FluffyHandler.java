package bet.astral.fluffy.hooks.worldguard.session;

import bet.astral.fluffy.FluffyCombat;
import bet.astral.fluffy.manager.CombatManager;
import bet.astral.fluffy.manager.UserManager;
import bet.astral.fluffy.messenger.FluffyMessenger;
import com.sk89q.worldguard.session.Session;
import com.sk89q.worldguard.session.handler.Handler;
import lombok.Getter;

public abstract class FluffyHandler extends Handler {
    @Getter
    private final FluffyCombat fluffyCombat;
    /**
     * Create a new handler.
     *
     * @param session The session
     */
    protected FluffyHandler(Session session, FluffyCombat fluffyCombat) {
        super(session);
        this.fluffyCombat = fluffyCombat;
    }

    public FluffyMessenger getMessenger(){
        return fluffyCombat.getMessenger();
    }

    public CombatManager getCombatManager() {
        return fluffyCombat.getCombatManager();
    }

    public UserManager getUserManager(){
        return fluffyCombat.getUserManager();
    }
}
