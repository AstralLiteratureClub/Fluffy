package bet.astral.fluffy.hooks.npc;

import bet.astral.messenger.v2.DefaultScheduler;
import bet.astral.messenger.v2.permission.Permission;
import bet.astral.messenger.v2.receiver.Receiver;
import bet.astral.messenger.v2.task.IScheduler;
import net.kyori.adventure.audience.Audience;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.Locale;

public class NPCReceiver implements Receiver {
    public static final Receiver INSTANCE = new NPCReceiver();

    @Override
    public @NotNull IScheduler getScheduler() {
        return DefaultScheduler.ASYNC_SCHEDULER;
    }

    @Override
    public @NotNull Locale getLocale() {
        return Locale.US;
    }

    @Override
    public boolean hasPermission(@NotNull Permission permission) {
        return false;
    }

    @Override
    public boolean hasPermission(@NotNull String s) {
        return false;
    }

    @Override
    public @NotNull Iterable<? extends Audience> audiences() {
        return List.of();
    }
}
