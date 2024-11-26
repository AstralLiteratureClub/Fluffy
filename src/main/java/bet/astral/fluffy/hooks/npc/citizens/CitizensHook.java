package bet.astral.fluffy.hooks.npc.citizens;

import bet.astral.fluffy.FluffyCombat;
import bet.astral.fluffy.hooks.Hook;
import bet.astral.fluffy.hooks.HookState;
import bet.astral.fluffy.hooks.npc.NPCReceiver;
import bet.astral.fluffy.hooks.npc.citizens.traits.FluffyTrait;
import lombok.Getter;
import net.citizensnpcs.Citizens;
import net.citizensnpcs.api.npc.NPC;
import net.citizensnpcs.api.trait.TraitInfo;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class CitizensHook implements Hook {
    private final Citizens citizens;
    @Getter
    private final Class<?> hookClass;
    private final FluffyCombat fluffy;
    private final HookState hookState;

    public CitizensHook(@NotNull FluffyCombat fluffyCombat, @Nullable Citizens hook, @Nullable Class<?> clazz, @NotNull HookState state) {
        this.fluffy = fluffyCombat;
        this.hookState = state;
        this.citizens = hook;
        this.hookClass = clazz;
    }

    @Override
    public void onLoad() {
    }

    @Override
    public void onEnable() {
        citizens.getTraitFactory().registerTrait(TraitInfo.create(FluffyTrait.class));

        fluffy.setNpcManager(new CitizensNPCManager(this));

        fluffy.getMessenger().registerReceiverConverter((obj)->{
            if (obj instanceof NPC) {
                return NPCReceiver.INSTANCE;
            }
            return null;
        });
    }


    @Override
    public FluffyCombat main() {
        return fluffy;
    }

    @Override
    public Citizens hookPlugin() {
        return citizens;
    }

    @Override
    public Class<?> hookPluginClass() {
        return hookClass;
    }

    @Override
    public HookState state() {
        return hookState;
    }
}