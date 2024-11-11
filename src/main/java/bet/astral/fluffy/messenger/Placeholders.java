package bet.astral.fluffy.messenger;

import bet.astral.fluffy.api.CombatCause;
import bet.astral.messenger.v2.placeholder.Placeholder;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.event.HoverEvent;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.attribute.Attribute;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.Damageable;
import org.bukkit.inventory.meta.ItemMeta;

import java.text.DecimalFormat;
import java.util.LinkedList;
import java.util.List;
import java.util.Objects;
import java.util.function.UnaryOperator;

public final class Placeholders {
	private static final DecimalFormat decimalFormat = new DecimalFormat(".00");
	private Placeholders(){
		throw new RuntimeException(getClass().getName()+" should not be initialized!");
	}

	public static List<Placeholder> combatPlaceholders(Player victim, OfflinePlayer attacker, CombatCause cause, ItemStack itemStack){
		List<Placeholder> placeholders = new LinkedList<>(playerPlaceholders("victim", victim));
		if (attacker != null) {
			if (attacker instanceof Player oAttacker) {
				placeholders.addAll(playerPlaceholders("attacker", oAttacker));
			} else {
				if (attacker.getName() != null) {
					placeholders.add(Placeholder.plain("attacker", attacker.getName()));
					placeholders.add(Placeholder.plain("attacker_name", attacker.getName()));
				}
				placeholders.add(Placeholder.plain("attacker_id", attacker.getUniqueId().toString()));
				placeholders.add(Placeholder.plain("attacker_is_online", String.valueOf(attacker.isOnline())));
			}
		}
		if (itemStack != null){
			placeholders.addAll(itemPlaceholders("attacker_tool", itemStack));
		}
		placeholders.add(Placeholder.plain("cause", cause.name()));
		placeholders.add(Placeholder.plain("cause_lower", cause.name().toLowerCase()));
		return placeholders;
	}
	public static List<Placeholder> playerPlaceholders(String key, Player player){
		// TODO add kills & shit
		List<Placeholder> placeholders = entityPlaceholders(key, player);
		// Not sure if it really matters, but whatever
		placeholders.removeIf(placeholder -> placeholder.getKey().equalsIgnoreCase(key+"_displayname"));
		placeholders.add(Placeholder.of(key+"_displayname", player.customName() != null ? Objects.requireNonNull(player.customName()) : player.displayName()));
		return entityPlaceholders(key, player);
	}
	public static List<Placeholder> entityPlaceholders(String key, LivingEntity entity){
		List<Placeholder> placeholders = new LinkedList<>();
		placeholders.add(Placeholder.plain(key+"_id", entity.getUniqueId().toString()));
		placeholders.add(Placeholder.plain(key+"_name", entity.getName()));
		placeholders.add(Placeholder.plain(key, entity.getName()));
		placeholders.add(Placeholder.of(key+"_displayname", entity.customName() != null ? Objects.requireNonNull(entity.customName()) : entity.name()));
		placeholders.add(Placeholder.plain(key+"_health", decimalFormat.format(entity.getHealth())));
		// It's so ludicrous you need to use attributes now
		placeholders.add(Placeholder.plain(key+"_max_health", decimalFormat.format(entity.getAttribute(Attribute.MAX_HEALTH).getValue())));
		placeholders.add(Placeholder.plain(key+"_x", decimalFormat.format(entity.getX())));
		placeholders.add(Placeholder.plain(key+"_y", decimalFormat.format(entity.getY())));
		placeholders.add(Placeholder.plain(key+"_z", decimalFormat.format(entity.getZ())));
		placeholders.add(Placeholder.plain(key+"_yaw", decimalFormat.format(entity.getYaw())));
		placeholders.add(Placeholder.plain(key+"_pitch", decimalFormat.format(entity.getPitch())));
		placeholders.add(Placeholder.plain(key+"_world", entity.getWorld().getName()));
		return placeholders;
	}
	public static List<Placeholder> itemPlaceholders(String key, ItemStack itemStack){
		List<Placeholder> placeholders = new LinkedList<>();
		HoverEvent<HoverEvent.ShowItem> hoverEvent = Bukkit.getItemFactory().asHoverEvent(itemStack, UnaryOperator.identity());
		Component itemDisplayHover = itemStack.displayName().hoverEvent(hoverEvent);
		Component itemNameHover = Component.translatable(itemStack.translationKey(), itemStack.displayName()).hoverEvent(hoverEvent);
		Component itemDisplay = itemStack.displayName();
		Component itemName = Component.translatable(itemStack.translationKey(), itemStack.displayName());
		placeholders.add(Placeholder.of(key+"_hover_displayname", itemDisplayHover));
		placeholders.add(Placeholder.of(key+"_hover_name", itemNameHover));
		placeholders.add(Placeholder.of(key+"_displayname", itemDisplay));
		placeholders.add(Placeholder.of(key+"_name", itemName));
		ItemMeta meta = itemStack.getItemMeta();
		if (meta instanceof Damageable damageable){
			placeholders.add(Placeholder.plain(key+"_damage", String.valueOf(damageable.getDamage())));
			placeholders.add(Placeholder.plain(key+"_durability", String.valueOf(itemStack.getType().getMaxDurability()-damageable.getDamage())));
			placeholders.add(Placeholder.plain(key+"_max_durability", String.valueOf(itemStack.getType().getMaxDurability())));
		} else {
			placeholders.add(Placeholder.plain(key+"_damage", String.valueOf(0)));
			placeholders.add(Placeholder.plain(key+"_durability", String.valueOf(0)));
			placeholders.add(Placeholder.plain(key+"_max_durability", String.valueOf(0)));
		}
		return placeholders;
	}
}
