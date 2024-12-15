package bet.astral.fluffy.commands.commands;

import bet.astral.cloudplusplus.annotations.Cloud;
import bet.astral.fluffy.FluffyCommandRegisterer;
import bet.astral.fluffy.commands.FluffyCommand;
import bet.astral.fluffy.hooks.Hook;
import bet.astral.fluffy.messenger.Translations;
import org.bukkit.command.CommandSender;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.incendo.cloud.context.CommandContext;
import org.incendo.cloud.paper.PaperCommandManager;
import org.incendo.cloud.permission.Permission;

@Cloud
public class PluginHooksCommand extends FluffyCommand {
    public PluginHooksCommand(FluffyCommandRegisterer registerer, PaperCommandManager.Bootstrapped<CommandSender> commandManager) {
        super(registerer, commandManager);
        command("plugin-hooks", Translations.COMMAND_BLOCK_OWNER_DESCRIPTION,
                b -> b.permission(Permission.of("fluffy.plugin-hooks"))
                        .handler(this::handle)).register();
    }

    private void handle(@NonNull CommandContext<? extends CommandSender> handler){
        CommandSender sender = handler.sender();
        for (Hook hook : fluffy().getHookManager().getHooks()){
            sender.sendMessage("Name: "+ hook.getClass().getTypeName() + " state: "+ hook.state().name());
        }
    }
}
