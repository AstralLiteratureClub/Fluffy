package bet.astral.fluffy.commands.commands;

import bet.astral.cloudplusplus.annotations.Cloud;
import bet.astral.fluffy.FluffyCombat;
import bet.astral.fluffy.FluffyCommandRegisterer;
import bet.astral.fluffy.commands.FluffyCommand;
import bet.astral.fluffy.messenger.Translations;
import bet.astral.messenger.v2.placeholder.PlaceholderList;
import bet.astral.tuples.Pair;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.incendo.cloud.bukkit.parser.location.LocationParser;
import org.incendo.cloud.context.CommandContext;
import org.incendo.cloud.paper.PaperCommandManager;
import org.incendo.cloud.permission.Permission;

import java.util.Optional;
import java.util.UUID;

@Cloud
public class BlockOwnerCommand extends FluffyCommand {
	public BlockOwnerCommand(FluffyCommandRegisterer registerer, PaperCommandManager.Bootstrapped<CommandSender> commandManager) {
		super(registerer, commandManager);
		command("block-owner", Translations.COMMAND_BLOCK_OWNER_DESCRIPTION,
				b -> b.permission(Permission.of("fluffy.block-owner"))
						.senderType(Player.class)
						.optional(LocationParser.locationComponent().name("location")
								.description(description(Translations.COMMAND_BLOCK_OWNER_LOCATION_DESCRIPTION)))
						.handler(this::handle)).register();
	}

	private void handle(@NonNull CommandContext<? extends CommandSender> handler){
		Player player = (Player) handler.sender();
		player.getScheduler().run(fluffy(), t->{
			Optional<Location> locationOptional = handler.optional("location");
			if (locationOptional.isEmpty()){
				if (player.getTargetBlock(35) == null){
					messenger.message(player, Translations.COMMAND_BLOCK_OWNER_TARGET_BLOCk);
					return;
				}
				locationOptional = Optional.of(player.getTargetBlock(35).getLocation());

			}
			Location location = locationOptional.get();
			Block block = location.getBlock();
			Pair<UUID, Material> blockInfo = FluffyCombat.getBlockData(block);
			PlaceholderList placeholders = new PlaceholderList();
			placeholders.add("x", location.getX());
			placeholders.add("y", location.getY());
			placeholders.add("z", location.getX());
			placeholders.add("world", location.getWorld().getName());
			if (blockInfo == null){
				messenger.message(player, Translations.COMMAND_BLOCK_OWNER_NO_DATA, placeholders);
				return;
			}
			UUID owner = blockInfo.getFirst();
			Material type = blockInfo.getSecond();
			placeholders.add("owner_id", owner.toString());
			placeholders.add("owner", Bukkit.getOfflinePlayer(owner).getName());
			placeholders.add("type", type.getKey().toString());

			messenger.message(player, Translations.COMMAND_BLOCK_OWNER_INFO, placeholders);
		}, null);
	}
}
