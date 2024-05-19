package bet.astral.fluffy;

import bet.astral.fluffy.astrolminiapi.CoreCommand;
import org.bukkit.command.CommandSender;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;

@ApiStatus.ScheduledForRemoval(inVersion = "1.3")
@Deprecated(forRemoval = true)
public class CMDReload  extends CoreCommand {
	private final FluffyCombat combat;
	protected CMDReload(FluffyCombat main) {
		super(main, "fluffy-reload");
		setPermission("fluffy.reload");
		this.combat = main;
	}

	/**
	 * Executes the command, returning its success
	 *
	 * @param sender       Source object which is executing this command
	 * @param commandLabel The alias of the command used
	 * @param args         All arguments passed to the command, split via ' '
	 * @return true if the command was successful, otherwise false
	 */
	@Override
	public boolean execute(@NotNull CommandSender sender, @NotNull String commandLabel, @NotNull String[] args) {
		sender.sendRichMessage("<green>Reloading...");
		main.reloadConfig();
		sender.sendRichMessage("<green>Reloaded!!");
		return true;
	}
}
