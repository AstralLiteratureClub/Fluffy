package bet.astral.fluffy.messenger.random;

import bet.astral.messenger.Message;
import bet.astral.messenger.Messenger;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.Map;

/**
 * Overrides the message manager to allow usage of random messages.
 * This should only be used in certain instances and not in every message as it might need
 * to load new messages it has never used in the runtime of the server.
 * @param <P> Plugin
 */
// TODO
public class RandomizedMessenger<P extends JavaPlugin> extends Messenger<P> {
	public RandomizedMessenger(P plugin, FileConfiguration config, Map<String, Message> messageMap) {
		super(plugin, config, messageMap);
	}


}
