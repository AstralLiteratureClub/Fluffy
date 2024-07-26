package bet.astral.fluffy;

import bet.astral.cloudplusplus.paper.bootstrap.BootstrapCommandRegisterer;
import bet.astral.cloudplusplus.paper.bootstrap.BootstrapHandler;
import bet.astral.cloudplusplus.paper.mapper.CommandSourceStackToCommandSenderMapper;
import bet.astral.fluffy.messenger.FluffyMessenger;
import bet.astral.messenger.v2.Messenger;
import bet.astral.messenger.v2.receiver.Receiver;
import io.papermc.paper.plugin.bootstrap.BootstrapContext;
import org.bukkit.command.CommandSender;
import org.incendo.cloud.execution.ExecutionCoordinator;
import org.incendo.cloud.paper.PaperCommandManager;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class FluffyCommandRegisterer implements BootstrapCommandRegisterer<CommandSender> {
	private final Logger logger = LoggerFactory.getLogger("FluffyCommandRegisterer");
	private PaperCommandManager.Bootstrapped<CommandSender> commandManager;
	private final BootstrapHandler handler = new BootstrapHandler();
	private FluffyMessenger messenger;
	public FluffyCommandRegisterer(BootstrapContext context, FluffyMessenger messenger){
		commandManager = PaperCommandManager
				.builder(new CommandSourceStackToCommandSenderMapper())
				.executionCoordinator(ExecutionCoordinator.asyncCoordinator())
				.buildBootstrapped(context);
		this.messenger = messenger;
		try {
			registerCommands("bet.astral.fluffy.commands.commands");
		} catch (Exception e){
			logger.error("e: ", e);
		}
	}

	@Override
	public Logger getSlf4jLogger() {
		return logger;
	}

	@Override
	public Messenger getMessenger() {
		return messenger;
	}

	@Override
	public Receiver convertToReceiver(@NotNull CommandSender commandSender) {
		return messenger.convertReceiver(commandSender);
	}

	@Override
	public boolean isDebug() {
		return false;
	}

	@Override
	public PaperCommandManager.Bootstrapped<CommandSender> getCommandManager() {
		return commandManager;
	}

	@Override
	public @NotNull BootstrapHandler getHandler() {
		return handler;
	}
}
