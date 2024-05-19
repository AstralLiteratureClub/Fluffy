package bet.astral.fluffy;

import bet.astral.fluffy.astrolminiapi.CoreCommand;
import bet.astral.fluffy.configs.CombatConfig;
import bet.astral.shine.Shine;
import bet.astral.shine.ShineColor;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;

@ApiStatus.ScheduledForRemoval(inVersion = "1.3")
@Deprecated(forRemoval = true)
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
		ShineColor shineColor = ShineColor.RED;
		Player player = (Player) sender;
		try {
			fluffy.getShine().setGlowing(player, player, shineColor);
		} catch (ReflectiveOperationException e) {
			throw new RuntimeException(e);
		}
		sender.sendRichMessage("<green>You should now glow!");
		return true;
	}
}
