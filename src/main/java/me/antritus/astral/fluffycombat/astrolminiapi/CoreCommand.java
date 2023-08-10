package me.antritus.astral.fluffycombat.astrolminiapi;


import me.antritus.astral.fluffycombat.antsfactions.FactionsPlugin;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandMap;
import org.bukkit.command.defaults.BukkitCommand;
import org.jetbrains.annotations.NotNull;

import java.lang.reflect.Field;

/**
 * @since 1.0-snapshot
 * @author antritus
 * @version 3-CosmicCapital
 */
public abstract class CoreCommand extends BukkitCommand {
	public void registerCommand() {
		try {
			Field commandMapField = Bukkit.getServer().getClass().getDeclaredField("commandMap");
			commandMapField.setAccessible(true);
			CommandMap commandMap = (CommandMap) commandMapField.get(Bukkit.getServer());
			commandMap.register(getName(), main.getName(), this);
		} catch (NoSuchFieldException | IllegalAccessException e) {
			e.printStackTrace();
		}
	}
	protected final FactionsPlugin main;

	protected CoreCommand(FactionsPlugin main, @NotNull String name) {
		super(name);
		this.main = main;
	}

	protected String buildStringFromArgs(int pos, String[] args){
		StringBuilder builder = new StringBuilder();
		for (int i = pos; i < args.length; i++){
			if (builder.length() > 0)
				builder.append(" ");
			builder.append(args[i]);
		}
		return builder.toString();
	}

	protected boolean isInteger(String s) {
		try {
			Integer.parseInt(s);
			return true;
		} catch (NumberFormatException e) {
			return false;
		}
	}

	@SuppressWarnings("BooleanMethodIsAlwaysInverted")
	protected boolean isDouble(String s) {
		try {
			double x = Double.parseDouble(s);
			if (Double.isNaN(x)){
				return false;
			}
			if (Double.isInfinite(x)) {
				return false;
			}
			return !s.contains("E") && !s.contains("e");
		} catch (NumberFormatException e) {
			return false;
		}
	}
}
