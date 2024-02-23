package bet.astral.fluffy;

import bet.astral.fluffy.api.BlockCombatTag;
import bet.astral.fluffy.api.CombatTag;
import bet.astral.fluffy.api.CombatUser;
import bet.astral.fluffy.astrolminiapi.CoreCommand;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.Comparator;
import java.util.List;
import java.util.Objects;

@Deprecated
public class CMDDebug extends CoreCommand {
	private final FluffyCombat combat;
	protected CMDDebug(FluffyCombat main) {
		super(main, "fluffy-tag");
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
			CombatUser combatUser = combat.getUserManager().getUser(player);
			List<CombatTag> tags = combat.getCombatManager().getTags(player).stream().sorted(Comparator.comparingInt(o -> o.getTicksLeft(combatUser))).toList();;

			Component component = null;
			for (CombatTag tag : tags){
				if (tag==null){
					continue;
				}
				int ticks = tag.getTicksLeft(combatUser);
				double seconds = (double) ticks /20;
				long millis = (long) (seconds* 1000L);
				if (component != null){
					component = component.appendNewline();
				} else {
					component = Component.text().build();
				}
				MiniMessage mm = MiniMessage.miniMessage();
				component = component.append(mm.deserialize("<gray> - <white>"));
				if (tag instanceof BlockCombatTag combatTag){
					component = component
							.append(
									mm.deserialize(
											combatTag.getAttacker().getBlock().getType().name().toUpperCase()))
							.append(									mm.deserialize(
									("<gray> (<red>BLOCK</red>) | <green>Millis: <white>"+millis+" <green>Ticks: <white>"+ticks+" <green>Seconds: <white>"+seconds)));
				} else {
					component = component
							.append(
									mm.deserialize(
													(
															Objects.requireNonNull(tag.getAttacker().getPlayer().getName())))
							.append(									mm.deserialize(
									("<gray> | <green>Millis: <white>"+millis+" <green>Ticks: <white>"+ticks+" <green>Seconds: <white>"+seconds))));
				}
			}
			if (component != null){
				player.sendMessage(component);
			}
		} else {
			player.sendRichMessage("<green>Does not have tags!");
		}
		return true;
	}
}
