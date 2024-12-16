package bet.astral.fluffy.commands.commands;

import bet.astral.cloudplusplus.annotations.Cloud;
import bet.astral.fluffy.FluffyCommandRegisterer;
import bet.astral.fluffy.commands.FluffyCommand;
import bet.astral.fluffy.messenger.Translations;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.event.HandlerList;
import org.bukkit.plugin.Plugin;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.incendo.cloud.context.CommandContext;
import org.incendo.cloud.paper.PaperCommandManager;
import org.incendo.cloud.permission.Permission;

import static bet.astral.fluffy.FluffyCombat.emergencyStop;

@Cloud
public class EmergencyStopCommand extends FluffyCommand {
    public EmergencyStopCommand(FluffyCommandRegisterer registerer, PaperCommandManager.Bootstrapped<CommandSender> commandManager) {
        super(registerer, commandManager);
        command("fluffy-emergency-stop", Translations.COMMAND_BLOCK_OWNER_DESCRIPTION,
                b -> b.permission(Permission.of("fluffy.emergency-stop"))
                        .senderType(ConsoleCommandSender.class)
                        .handler(this::handle)).register();
    }

    private void handle(@NonNull CommandContext<? extends CommandSender> handler){
        if (handler.sender() instanceof ConsoleCommandSender){
            emergencyStop = true;
            fluffy().getStatisticManager().onDisable(); // Ensure statistics are saved
            HandlerList.unregisterAll((Plugin) fluffy());
            Bukkit.broadcast(
                    MiniMessage.miniMessage()
                            .deserialize("""
									<dark_red><bold>FLUFFY EMERGENCY STOP<reset>
									<red>Fluffy is no no longer tagging players! It is safe to leave the server without getting killed!
									<yellow>Server restart required to disable this!""")
            );
        }
    }
}
