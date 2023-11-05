package me.antritus.astral.fluffycombat;

import me.antritus.astral.fluffycombat.api.CombatTag;
import me.antritus.astral.fluffycombat.astrolminiapi.CoreCommand;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

public class CMDDebug extends CoreCommand {
	private final FluffyCombat combat;
	protected CMDDebug(FluffyCombat main) {
		super(main, "fluffy-debug");
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
			sender.sendRichMessage("This command only works with players");
			return true;
		}
		if (combat.getCombatManager().hasTags(player)){
			player.sendRichMessage("<red>You are currently in combat!");
			CombatTag tag = combat.getCombatManager().getLatest(player);
			assert tag != null;
			int ticks = tag.getTicksLeft();
			double seconds = (double) ticks /20;
			long millis = (long) (seconds* 1000L);
			player.sendRichMessage("<red>Ticks: <white>"+ticks);
			player.sendRichMessage("<red>Seconds: <white>"+seconds);
			player.sendRichMessage("<red>Millis: <white>"+millis);
		} else {
			player.sendRichMessage("<green>Does not have tags!");
		}
		return true;
	}
}
