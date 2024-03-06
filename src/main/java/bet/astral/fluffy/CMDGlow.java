package bet.astral.fluffy;

import bet.astral.fluffy.astrolminiapi.CoreCommand;
import bet.astral.fluffy.configs.CombatConfig;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;

public class CMDGlow extends CoreCommand {
	private final FluffyCombat fluffy;
	protected CMDGlow(FluffyCombat main) {
		super(main, "fluffy-glowing");
		this.fluffy = main;
		setPermission("fluffy.glowing");
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
		CombatConfig combatConfig = fluffy.getCombatConfig();
		if (!combatConfig.isPotionStartCombat()){
			sender.sendRichMessage("<red>Potions do not begin combat.");
			return true;
		}
		ChatColor color = ChatColor.AQUA;
		Player player = (Player) sender;
		try {
			fluffy.getGlowingEntities().setGlowing(player, player, color);
		} catch (ReflectiveOperationException e) {
			throw new RuntimeException(e);
		}
		sender.sendRichMessage("<green>You should now glow!");
		return true;
	}
}
