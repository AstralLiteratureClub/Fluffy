package me.antritus.astral.fluffycombat.astrolminiapi;

import org.bukkit.command.defaults.BukkitCommand;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.Collections;
/**
 * @author Antritus
 * @since 1.0-SNAPSHOT (CosmicCapital)
 */
public class CommandConfiguration extends Configuration{
	/**
	 * Creates a new config with the given filename.
	 *
	 * @param pl plugin
	 */
	public CommandConfiguration(JavaPlugin pl) {
		super(pl, "commands.yml");
	}


	public BukkitCommand load(BukkitCommand command){
		if (get(command.getName().toLowerCase()) == null){
			return command;
		}
		setIfNull(command.getName()+".aliases", null);
		command.setAliases((isList(command.getName().toLowerCase()+".aliases") ? getStringList(command.getName().toLowerCase()+".aliases") : Collections.singletonList(getString(command.getName().toLowerCase() + ".aliases"))));
		command.permissionMessage(ColorUtils.translateComp(getString(command.getName()+".permission-message")));
		command.setDescription(ColorUtils.translate(getString(command.getName()+".description")));
		command.setUsage(ColorUtils.translate(getString(command.getName()+".usage")));
		return command;
	}
}
