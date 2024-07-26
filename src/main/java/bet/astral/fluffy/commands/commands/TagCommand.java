package bet.astral.fluffy.commands.commands;

import bet.astral.cloudplusplus.annotations.Cloud;
import bet.astral.fluffy.FluffyCommandRegisterer;
import bet.astral.fluffy.api.BlockCombatUser;
import bet.astral.fluffy.api.CombatTag;
import bet.astral.fluffy.api.CombatUser;
import bet.astral.fluffy.commands.FluffyCommand;
import bet.astral.fluffy.manager.CombatManager;
import bet.astral.fluffy.messenger.Translations;
import bet.astral.messenger.v2.placeholder.Placeholder;
import net.kyori.adventure.util.Ticks;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.incendo.cloud.bukkit.parser.PlayerParser;
import org.incendo.cloud.context.CommandContext;
import org.incendo.cloud.paper.PaperCommandManager;

@Cloud
public class TagCommand extends FluffyCommand {
	public TagCommand(FluffyCommandRegisterer registerer, PaperCommandManager.Bootstrapped<CommandSender> commandManager) {
		super(registerer, commandManager);
		command("tag", Translations.COMMAND_TAG_DESCRIPTION, b -> b.senderType(Player.class)
				.permission("fluffy.tag")
				.optional(PlayerParser.playerComponent().name("target").description(description(Translations.COMMAND_TAG_WHO_DESCRIPTION)))
				.handler(this::handle)).register();
	}

	public void handle(CommandContext<Player> context) {
		Player player = context.sender();
		Player target = context.getOrDefault("target", player);
		CombatManager combatManager = fluffy().getCombatManager();

		if (!combatManager.hasTags(target)) {
			if (target.equals(player)) {
				messenger.message(player, Translations.COMMAND_TAG_NO_TAGS_SELF);
			} else {
				messenger.message(player, Translations.COMMAND_TAG_NO_TAGS_OTHER, Placeholder.of("who", target.name()));
			}
		} else {
			if (target.equals(player)) {
				messenger.message(player, Translations.COMMAND_TAG_INFO_SELF);
			} else {
				messenger.message(player, Translations.COMMAND_TAG_INFO_OTHER, Placeholder.of("who", target.name()));
			}
			for (CombatTag tag : combatManager.getTags(target)) {
				if (!tag.isActive(target)) {
					continue;
				}
				int ticks = tag.getTicksLeft(tag.getUser(target));
				double seconds = Ticks.duration(ticks).getSeconds();
				CombatUser opposite = tag.getOpposite(target);
				if (opposite instanceof BlockCombatUser blockCombatUser) {
					messenger.disablePrefixForNextParse().
							message(player, Translations.COMMAND_TAG_INFO_BLOCK,
									Placeholder.of("who", blockCombatUser.getBlock().getType().getKey().toString()),
									Placeholder.of("x", blockCombatUser.getLocation().getX()),
									Placeholder.of("y", blockCombatUser.getLocation().getZ()),
									Placeholder.of("z", blockCombatUser.getLocation().getY()),
									Placeholder.of("world", blockCombatUser.getLocation().getWorld().getName()),
									Placeholder.of("ticks", ticks),
									Placeholder.of("seconds", seconds)
							);
				} else {
					messenger.disablePrefixForNextParse().
							message(player, Translations.COMMAND_TAG_INFO_PLAYER,
									Placeholder.of("who", opposite.getPlayer().getName()),
									Placeholder.of("ticks", ticks),
									Placeholder.of("seconds", seconds)
							);
				}
			}
		}
	}
}
