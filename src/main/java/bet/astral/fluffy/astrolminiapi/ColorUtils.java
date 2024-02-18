package bet.astral.fluffy.astrolminiapi;


import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.TextDecoration;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer;
import net.md_5.bungee.api.ChatColor;

/**
 * @since 1.0.0-snapshot
 * @author antritus
 */
@SuppressWarnings("ALL")
public class ColorUtils {
	public static String translate(String str){
		return ChatColor.translateAlternateColorCodes('&', str);
	}
	public static Component translateComp(String msg){
		return MiniMessage.miniMessage().deserialize(msg).decoration(TextDecoration.ITALIC, false);
	}
	public static Component translateCompLegacy(String msg){
		return LegacyComponentSerializer.legacy('\u00a7').deserialize(msg);
	}
	public static String deserialize(Component component){
		return PlainTextComponentSerializer.plainText().serialize(component);
	}
}
