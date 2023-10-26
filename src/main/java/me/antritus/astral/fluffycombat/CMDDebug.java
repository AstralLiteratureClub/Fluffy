package me.antritus.astral.fluffycombat;

import me.antritus.astral.fluffycombat.astrolminiapi.CoreCommand;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

public class CMDDebug extends CoreCommand {
	private final FluffyCombat combat;
	protected CMDDebug(FluffyCombat main) {
		super(main, "db-combat-me");
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
		if (!(sender instanceof Player player)){
			sender.sendMessage("This command only works with players");
			return true;
		}
		if (combat.getCombatManager().hasTags(player)){
			sender.sendMessage("You already are in combat.");
			return true;
		}
		combat.getCombatManager().create(player, player);
		sender.sendMessage("There you go!");
		return true;
	}
}
