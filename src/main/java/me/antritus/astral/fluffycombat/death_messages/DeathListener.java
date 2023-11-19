package me.antritus.astral.fluffycombat.death_messages;

import bet.astral.messagemanager.fluffy.Placeholders;
import bet.astral.messagemanager.placeholder.LegacyPlaceholder;
import bet.astral.messagemanager.placeholder.Placeholder;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.bukkit.event.Listener;
import org.bukkit.inventory.ItemStack;

import java.text.DecimalFormat;
import java.util.List;

public interface DeathListener extends Listener {
	DecimalFormat decimalFormat = new DecimalFormat(".00");
	default String formatHealth(double health){
		return decimalFormat.format(health);
	}


	default List<Placeholder> defaults(String key, Player player){
		return Placeholders.playerPlaceholders(key, player);
	}

	default List<Placeholder> defaults(Player victim, OfflinePlayer attacker){
		List<Placeholder> placeholders = defaults("victim", victim);
		if (attacker != null) {
			if (attacker instanceof Player player) {
				placeholders.addAll(defaults("attacker", player));
			} else {
				if (attacker.getName() != null) {
					placeholders.add(new LegacyPlaceholder("attacker", attacker.getName()));
					placeholders.add(new LegacyPlaceholder("attacker_name", attacker.getName()));
				}
				placeholders.add(new LegacyPlaceholder("attacker_id", attacker.getUniqueId().toString()));
			}
		}
		return placeholders;
	}

	default List<Placeholder> defaults(Player victim, OfflinePlayer attacker, ItemStack itemStack) {
		List<Placeholder> placeholders = defaults(victim, attacker);
		placeholders.addAll(Placeholders.itemPlaceholders("attacker_tool", itemStack));
		return placeholders;
	}
}