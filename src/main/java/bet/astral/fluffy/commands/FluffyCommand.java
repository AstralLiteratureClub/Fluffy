package bet.astral.fluffy.commands;

import bet.astral.cloudplusplus.minecraft.paper.bootstrap.commands.CPPBootstrapCommand;
import bet.astral.fluffy.FluffyCombat;
import bet.astral.fluffy.FluffyCommandRegisterer;
import org.bukkit.command.CommandSender;
import org.incendo.cloud.paper.PaperCommandManager;

public class FluffyCommand extends CPPBootstrapCommand<CommandSender> {
	private FluffyCombat fluffy;
	public FluffyCommand(FluffyCommandRegisterer registerer, PaperCommandManager.Bootstrapped<CommandSender> commandManager) {
		super(registerer, commandManager);
	}

	public FluffyCombat fluffy(){
		if (fluffy==null){
			fluffy = FluffyCombat.getPlugin(FluffyCombat.class);
		}

		return fluffy;
	}
}
