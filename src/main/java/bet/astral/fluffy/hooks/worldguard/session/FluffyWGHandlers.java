package bet.astral.fluffy.hooks.worldguard.session;

import com.sk89q.worldguard.session.handler.Handler;

public class FluffyWGHandlers {
    public static final Handler.Factory<CombatEntryFlag> COMBAT_ENTRY_HANDLER_FACTORY = new CombatEntryFlag.Factory();
}
