package bet.astral.fluffy;

import lombok.Getter;
import net.kyori.adventure.text.format.NamedTextColor;

@Getter
public enum NamedTextColorUtils {
	BLACK(NamedTextColor.BLACK, '0'),
	DARK_BLUE(NamedTextColor.DARK_BLUE, '1'),
	DARK_GREEN(NamedTextColor.DARK_GREEN, '2'),
	DARK_AQUA(NamedTextColor.DARK_AQUA, '3'),
	DARK_RED(NamedTextColor.DARK_RED, '4'),
	DARK_PURPLE(NamedTextColor.DARK_PURPLE, '5'),
	GOLD(NamedTextColor.GOLD, '6'),
	GRAY(NamedTextColor.GRAY, '7'),
	DARK_GRAY(NamedTextColor.DARK_GRAY, '8'),
	BLUE(NamedTextColor.BLUE, '9'),
	GREEN(NamedTextColor.GREEN, 'a'),
	AQUA(NamedTextColor.AQUA, 'b'),
	RED(NamedTextColor.RED, 'c'),
	LIGHT_PURPLE(NamedTextColor.LIGHT_PURPLE, 'd'),
	YELLOW(NamedTextColor.YELLOW, 'e'),
	WHITE(NamedTextColor.WHITE, 'f');

	private final NamedTextColor color;
	private final char bukkitChar;

	NamedTextColorUtils(NamedTextColor color, char bukkitChar) {
		this.color = color;
		this.bukkitChar = bukkitChar;
	}

	public static NamedTextColorUtils getByChar(char bukkitChar) {
		for (NamedTextColorUtils entry : values()) {
			if (entry.getBukkitChar() == bukkitChar) {
				return entry;
			}
		}
		return null;
	}

	public static NamedTextColorUtils getByName(String name) {
		for (NamedTextColorUtils entry : values()) {
			if (entry.getColor().toString().equals(name.toUpperCase())) {
				return entry;
			}
		}
		return null;
	}

	public static NamedTextColorUtils getByChar(char bukkitChar, NamedTextColorUtils defaultValue) {
		for (NamedTextColorUtils entry : values()) {
			if (entry.getBukkitChar() == bukkitChar) {
				return entry;
			}
		}
		return defaultValue;
	}

	public static NamedTextColorUtils getByName(String name, NamedTextColorUtils defaultValue) {
		for (NamedTextColorUtils entry : values()) {
			if (entry.getColor().toString().equals(name.toUpperCase())) {
				return entry;
			}
		}
		return defaultValue;
	}
}