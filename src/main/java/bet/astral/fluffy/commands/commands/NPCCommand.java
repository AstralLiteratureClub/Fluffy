package bet.astral.fluffy.commands.commands;

import bet.astral.cloudplusplus.annotations.Cloud;
import bet.astral.fluffy.FluffyCommandRegisterer;
import bet.astral.fluffy.commands.FluffyCommand;
import bet.astral.fluffy.manager.NPCManager;
import bet.astral.fluffy.messenger.Translations;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.incendo.cloud.context.CommandContext;
import org.incendo.cloud.paper.PaperCommandManager;
import org.incendo.cloud.permission.Permission;

@Cloud
public class NPCCommand extends FluffyCommand {
    public NPCCommand(FluffyCommandRegisterer registerer, PaperCommandManager.Bootstrapped<CommandSender> commandManager) {
        super(registerer, commandManager);
        command("spawn-npc", Translations.COMMAND_BLOCK_OWNER_DESCRIPTION,
                b -> b.permission(Permission.of("fluffy.block-owner"))
                        //.optional(LocationParser.locationComponent().name("location"))
                        //		.description(description(Translations.COMMAND_BLOCK_OWNER_LOCATION_DESCRIPTION)))
                        .handler(this::handle)).register();
    }

    private void handle(@NonNull CommandContext<? extends CommandSender> handler){
        Player player = (Player) handler.sender();
        player.sendMessage("hi");
        player.getScheduler().run(fluffy(), t->{
            player.sendMessage("hi 2");
            player.sendMessage(fluffy().getNpcManager().getClass().getName());
            NPCManager npcManager = fluffy().getNpcManager();
            npcManager.spawnNPC(player.getLocation(), player);
        }, null);
    }
}
