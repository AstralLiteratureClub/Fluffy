package bet.astral.fluffy.commands.commands.cosmetics;

import bet.astral.cloudplusplus.annotations.Cloud;
import bet.astral.fluffy.FluffyCommandRegisterer;
import bet.astral.fluffy.commands.FluffyCommand;
import bet.astral.fluffy.messenger.Translations;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.incendo.cloud.context.CommandContext;
import org.incendo.cloud.paper.PaperCommandManager;
import org.incendo.cloud.permission.Permission;

@Cloud
public class DeathEffectsCommand extends FluffyCommand {
    public DeathEffectsCommand(FluffyCommandRegisterer registerer, PaperCommandManager.Bootstrapped<CommandSender> commandManager) {
        super(registerer, commandManager);
        command(root, "death-effects", Translations.COMMAND_BLOCK_OWNER_DESCRIPTION,
                b -> b.permission(Permission.of("fluffy.death-effects"))
                        .senderType(Player.class)
                        .handler(this::handle)).register();
    }

    private void handle(@NonNull CommandContext<? extends Player> handler){
        Player player = handler.sender();
        fluffy().getDeathEffectManager().getMenu().open(player);
    }
}
