package bet.astral.fluffy.commands;

import bet.astral.cloudplusplus.minecraft.paper.bootstrap.commands.CPPBootstrapCommand;
import bet.astral.fluffy.FluffyCombat;
import bet.astral.fluffy.FluffyCommandRegisterer;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.command.CommandSender;
import org.incendo.cloud.description.Description;
import org.incendo.cloud.paper.PaperCommandManager;

public class FluffyCommand extends CPPBootstrapCommand<CommandSender> {
	private static boolean registered = false;
	private FluffyCombat fluffy;
	protected static RegistrableCommand<? extends CommandSender> root;
	protected static RegistrableCommand<? extends CommandSender> debug;
	public FluffyCommand(FluffyCommandRegisterer registerer, PaperCommandManager.Bootstrapped<CommandSender> commandManager) {
		super(registerer, commandManager);
		if (!registered) {
			registered = true;
			root = command("fluffy", Description.EMPTY, b->b
					.handler(context->{
						CommandSender sender = context.sender();

						MiniMessage miniMessage = MiniMessage.miniMessage();
						sender.sendMessage(miniMessage.deserialize("<red><bold>Fluffy <gray>| <white>v"+fluffy().getPluginMeta().getVersion()));
						sender.sendMessage("- Authors: ", String.join(", ", fluffy().getPluginMeta().getAuthors()));
					}));
			root.register();

			debug = command(root, "debug", Description.EMPTY, b->b);
			debug.register();
		}
	}

	public FluffyCombat fluffy(){
		if (fluffy==null){
			fluffy = FluffyCombat.getPlugin(FluffyCombat.class);
		}

		return fluffy;
	}


}
