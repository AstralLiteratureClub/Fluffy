package bet.astral.fluffy;

import bet.astral.fluffy.astrolminiapi.CoreCommand;
import bet.astral.fluffy.configs.CombatConfig;
import org.bukkit.command.CommandSender;
import org.jetbrains.annotations.NotNull;

public class CMDPotions extends CoreCommand {
	private final FluffyCombat combat;
	protected CMDPotions(FluffyCombat main) {
		super(main, "fluffy-potions");
		this.combat = main;
		setPermission("fluffy.potions");
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
		CombatConfig combatConfig = combat.getCombatConfig();
		if (!combatConfig.isPotionStartCombat()){
			sender.sendRichMessage("<red>Potions do not begin combat.");
			return true;
		}
		sender.sendRichMessage("<green>Potions do begin combat.");
		combatConfig.getPotionsToBeginCombat().forEach(potion->{
			sender.sendRichMessage(" <gray>- <green>"+potion.key().namespace()+"<gray>:<green>"+ potion.key().value());
		});
		return true;
	}
}
