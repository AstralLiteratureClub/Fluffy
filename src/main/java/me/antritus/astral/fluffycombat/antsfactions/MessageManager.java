package me.antritus.astral.fluffycombat.antsfactions;

import com.google.common.collect.ImmutableMap;
import me.antritus.astral.fluffycombat.astrolminiapi.Configuration;
import net.kyori.adventure.audience.Audience;
import net.kyori.adventure.platform.bukkit.BukkitAudiences;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextReplacementConfig;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.*;

/**
 * This is a message manager which loads messages from plugins/plugin/messages.yml
 * It is designed to work in 1.8 as for my factions were first made for 1.8.
 * Removing some of the methods that allow 1.8 use it is possible, and could
 * speed up the progress.
 * But removing the support for 1.8 is not supported by @antritus.
 * @author Antritus
 * @since 1.1-SNAPSHOT
 */
public class MessageManager {
	private final MiniMessage miniMessage = MiniMessage.miniMessage();
	private final FactionsPlugin main;
	public final Configuration messageConfig;
	private final BukkitAudiences bukkitAudiences;
	private final ImmutableMap<String, Component> placeholders;
	private final Map<String, Component> messages = new LinkedHashMap<>();
	private final ImmutableMap<String, String> warningActions;
	private final Map<String, Boolean> unused = new LinkedHashMap<>();
	public MessageManager(FactionsPlugin main){
		BukkitAudiences bukkitAudiences1;
		this.main = main;
		try {
			Player.class.getMethod("sendMessage", Component.class);
			bukkitAudiences1 = null;
		} catch (NoSuchMethodException e) {
			bukkitAudiences1 = BukkitAudiences.create(main);
		}
		bukkitAudiences = bukkitAudiences1;

		try {
			messageConfig = new Configuration(main, "messages.yml");
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
		placeholders = loadPlaceholders();
		warningActions = loadWarningActions();
	}

	public Collection<String> messageKeys(){
		return messages.keySet();
	}
	private ImmutableMap<String, Component> loadPlaceholders() {
		List<Map<?, ?>> placeholderMap = messageConfig.getMapList("placeholders");
		ImmutableMap.Builder<String, Component> builder = ImmutableMap.builder();
		for (Map<?, ?> map : placeholderMap) {
			String name = (String) map.get("name");
			String txt = (String) map.get("text");
			builder.put(name, miniMessage.deserialize(txt));
		}
		return builder.build();
	}

	private ImmutableMap<String, String> loadWarningActions() {
		List<Map<?, ?>> actionMap = messageConfig.getMapList("warnings.actions");
		ImmutableMap.Builder<String, String> builder = ImmutableMap.builder();
		for (Map<?, ?> map : actionMap) {
			String name = (String) map.get("name");
			String txt = (String) map.get("text");
			builder.put(name, txt);
		}
		return builder.build();
	}
	private Component checkPlaceholders(Component msg){
		if (placeholders.size() == 0){
			return msg;
		}
		for (String placeholder : placeholders.keySet()) {
			if (placeholder == null){
				continue;
			}
			placeholder = placeholder.replace("%", "");
			String finalPlaceholder = placeholder;
			msg = msg.replaceText((TextReplacementConfig.Builder builder) -> builder.match("%" + finalPlaceholder + "%").replacement(placeholders.get(finalPlaceholder)));
		}
		return msg;
	}
	private Component checkLocalPlaceholders(Component msg, String... placeholders){
		if (placeholders.length == 0){
			return msg;
		}
		MiniMessage miniMessage = MiniMessage.miniMessage();
		for (String placeholder : placeholders) {
			if (placeholder == null){
				continue;
			}
			String[] split = placeholder.split("=", 2);
			if (split.length == 1){
				continue;
			}
			placeholder = placeholder.replace("%", "");
			split = placeholder.split("=", 2);
			String[] finalSplit = split;
			msg = msg.replaceText((TextReplacementConfig.Builder builder) -> builder.match("%" + finalSplit[0] + "%").replacement(miniMessage.deserialize(finalSplit[1])));
		}
		return msg;
	}
	private void load(String key){
		if (messages.get(key) == null){

			if (messageConfig.isList(key)){
				ArrayList<String> msg = new ArrayList<>(messageConfig.getStringList(key));
				ArrayList<Component> components = new ArrayList<>();
				for (String value : msg) {
					try {
						Component s = miniMessage.deserialize(value);
						s = checkPlaceholders(s);
						components.add(s);
					} catch (IndexOutOfBoundsException ignore) {
					}
				}
				Component combinedComponent = null;
				for (Component comp : components) {
					if (combinedComponent == null) {
						combinedComponent = comp;
					} else {
						combinedComponent = combinedComponent.appendNewline().append(comp);
					}
				}
				messages.put(key, combinedComponent);
			} else {
				String msg = messageConfig.getString(key);
				if (msg == null){
					msg = key;
				}
				if (msg.equals("UNUSED")){
					unused.put(key, true);
				}
				Component component;
				component = miniMessage.deserialize(msg);
				component = checkPlaceholders(component);
				messages.put(key, component);
			}
		}
	}
	private Component parse(boolean reparse, String key, String... placeholders){
		MiniMessage miniMessage = MiniMessage.miniMessage();
		load(key);
		Component msg = ((messages.get(key) != null ? messages.get(key) : Component.text(key)).appendSpace());
		msg = checkLocalPlaceholders(msg, placeholders);
		if (reparse){
			String deserialiazed = miniMessage.serialize(msg);
			for (String placeholder : placeholders) {
				try {
					deserialiazed = deserialiazed.replace(placeholder.split("=")[0], placeholder.split("=")[1]);
				} catch (ArrayIndexOutOfBoundsException ignore){
				}
			}
			return miniMessage.deserialize(deserialiazed);
		}
		return msg;
	}
	// Switch to async messages to reduce parse time from the server.
	public void message(CommandSender player, String key, String... placeholders) {
		if (unused.get(key) != null && unused.get(key)){
			return;
		}
		new BukkitRunnable() {
			@Override
			public void run() {
				send(player, parse(true, key, placeholders));
			}
		}.runTaskAsynchronously(main);
	}
	// Switch to async messages to reduce parse time from the server.
	public void message(boolean reparse, CommandSender player, String key, String... placeholders) {
		if (unused.get(key) != null && unused.get(key)){
			return;
		}
		new BukkitRunnable() {
			@Override
			public void run() {
				send(player, parse(reparse, key, placeholders));
			}
		}.runTaskAsynchronously(main);		send(player, parse(reparse, key, placeholders));
	}
	private void send(CommandSender sender, Component component){
		if (bukkitAudiences != null) {
			if (sender instanceof Player) {
				bukkitAudiences.player((Player) sender).sendMessage(component);
			} else {
				bukkitAudiences.sender(sender).sendMessage(component);
			}
		} else {
			((Audience) sender).sendMessage(component);
		}
	}
	public void broadcast(String key, String... placeholders){
		if (bukkitAudiences == null) {
			broadcast(parse(false, key, placeholders));
			return;
		}
		bukkitAudiences.all().sendMessage(parse(false, key, placeholders));
	}
	public void warning(Player player, String action, String... placeholders){
		String[] pls = new String[placeholders.length+1];
		System.arraycopy(placeholders, 0, pls, 0, placeholders.length);
		pls[placeholders.length] = "%player%="+player.getName();
		Component component = parse(false, "warnings.warning",pls);
		component= component.replaceText(TextReplacementConfig.builder().match("%action%").replacement(Objects.requireNonNull(warningActions.get(action))).build());
		Component finalComponent = component;
		Bukkit.getOnlinePlayers().stream().filter(
				p->
						// Switch to right plugin name
						p.hasPermission(main.getName().toLowerCase()+".receive-warnings")
		).forEach(
				p->
						send(p, finalComponent)
		);
		ConsoleCommandSender sender = Bukkit.getConsoleSender();
		send(sender, component);
	}

	private final Method broadcast;
	{
		//noinspection RedundantSuppression
		try {
			//noinspection JavaReflectionMemberAccess
			broadcast = Bukkit.class.getMethod("broadcast", Component.class);
			broadcast.setAccessible(true);
		} catch (NoSuchMethodException e) {
			throw new RuntimeException(e);
		}
	}
	private void broadcast(Component components){
		try {
			broadcast.invoke(null, components);
		} catch (IllegalAccessException | InvocationTargetException e) {
			throw new RuntimeException(e);
		}
	}
}