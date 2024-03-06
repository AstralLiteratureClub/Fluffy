package bet.astral.fluffy;

import bet.astral.fluffy.astrolminiapi.CoreCommand;
import bet.astral.fluffy.utils.Pair;
import com.destroystokyo.paper.block.TargetBlockInfo;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;

import java.util.UUID;

public class CMDBlockOwner extends CoreCommand {
	protected CMDBlockOwner(JavaPlugin main) {
		super(main, "fluffy-block");
		setPermission("fluffy.block");
	}

	@Override
	public boolean execute(@NotNull CommandSender commandSender, @NotNull String s, @NotNull String[] strings) {
		Player player = (Player) commandSender;
		Block block = player.getTargetBlock(25, TargetBlockInfo.FluidMode.ALWAYS);
		if (block == null){
			player.sendRichMessage("Didn't find targeted block.");
			return true;
		}
		Pair<UUID, Material> blockOwner = FluffyCombat.getBlockData(block);
		if (blockOwner == null){
			player.sendRichMessage("Block does not have an owner.");
			return true;
		}
		player.sendRichMessage("Block is owned by " + Bukkit.getPlayer(blockOwner.value()).getName());
		return true;
	}
}
