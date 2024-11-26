package bet.astral.fluffy.messenger;

import bet.astral.messenger.v2.component.ComponentType;
import bet.astral.messenger.v2.translation.TranslationKey;
import bet.astral.messenger.v2.translation.serializer.gson.TranslationGsonHelper;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.title.Title;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import static bet.astral.messenger.v2.translation.Translation.text;

public class Translations {
	public static final Gson GSON = new GsonBuilder().setPrettyPrinting().disableHtmlEscaping().create();
	private static final Map<String, bet.astral.messenger.v2.translation.Translation> translations = new HashMap<>();
	public static final Translation MESSENGER_PREFIX = new Translation("messenger.settings.prefix").add(ComponentType.CHAT, text("<!italic><light_purple><bold>Fluffy<!bold> "));
	public static final Translation COMMAND_BLOCK_OWNER_DESCRIPTION = new Translation("commands.block-owner.description").add(ComponentType.CHAT, text("Allows administrators to see who have placed blocks. Only specific blocks can have owners."));
	public static final Translation COMMAND_BLOCK_OWNER_LOCATION_DESCRIPTION = new Translation("commands.block-owner.location-description").add(ComponentType.CHAT, text("Specific location to see information from"));
	public static final Translation COMMAND_BLOCK_OWNER_TARGET_BLOCk = new Translation("commands.block-owner.unknown-block-target").add(ComponentType.CHAT, text("<red>Couldn't find a block being targeted."));
	public static final Translation COMMAND_BLOCK_OWNER_NO_DATA = new Translation("commands.block-owner.no-data").add(ComponentType.CHAT, text("<red>No data found for block <white>%x%<gray>, <white>%y%<gray>, <white>%z%"));
	public static final TranslationKey COMMAND_BLOCK_OWNER_INFO = new Translation("commands.block-owner.info").add(ComponentType.CHAT,
			text("<yellow>Block Owner: <white>%owner% <gray>(%owner_id%)").appendNewline(),
			text("<yellow>Type: <white>%type%").appendSpace(),
			text("<yellow>X: <white>%x%<gray>, <yellow>Y: <white>%y%<gray>, <yellow>Z: <white>%z%")
	);
	public static final Translation COMMAND_TAG_DESCRIPTION = new Translation("commands.tag.description").add(ComponentType.CHAT, text("Allows administrators to see combat tags of players."));
	public static final Translation COMMAND_TAG_WHO_DESCRIPTION = new Translation("commands.tag.description").add(ComponentType.CHAT, text("Who to see combat tags of."));
	public static final Translation COMMAND_TAG_NO_TAGS_SELF = new Translation("commands.tag.no-tags-self").add(ComponentType.CHAT, text("<red>You do not have any combat tags!"));
	public static final Translation COMMAND_TAG_NO_TAGS_OTHER = new Translation("commands.tag.no-tags-other").add(ComponentType.CHAT, text("<red>%who% does not have any combat tags!"));
	public static final Translation COMMAND_TAG_INFO_SELF = new Translation("commands.tag.info-self").add(ComponentType.CHAT, text("<yellow>Your combat tags (Highest = Newest)"));
	public static final Translation COMMAND_TAG_INFO_OTHER = new Translation("commands.tag.info-other").add(ComponentType.CHAT, text("<yellow>%who%'s combat tags (Highest = Newest)"));
	public static final Translation COMMAND_TAG_INFO_BLOCK = new Translation("commands.tag.tag-block").add(ComponentType.CHAT, text("<yellow>- <white>(Block) <yellow>%who% <white>x%x%<gray>, <white>y%y%<gray>, <white>z%z%<gray>, <white>%world% <gray>(<yellow>%ticks% <gray>ticks)"));
	public static final Translation COMMAND_TAG_INFO_PLAYER = new Translation("commands.tag.tag-player").add(ComponentType.CHAT, text("<yellow>- <yellow>%who% <gray>(<yellow>%ticks% <gray>ticks)"));
	public static final Translation LISTENER_COOLDOWN_DEFAULT = new Translation("listener.cooldown.default").add(ComponentType.CHAT, text("<yellow>You're on cooldown with <white>%type%<yellow> for <white>%time% seconds<yellow>."));
	public static final Translation LISTENER_COOLDOWN_ENDER_PEARL = new Translation("listener.cooldown.minecraft:enderpearl").add(ComponentType.CHAT, text("<yellow>You're on cooldown with <light_purple>Ender Pearl<yellow> for <white>%time% seconds<yellow>."));
	public static final Translation LISTENER_COOLDOWN_ENCHANTED_GOLDEN_APPLE = new Translation("listener.cooldown.minecraft:enchanted_golden_apple").add(ComponentType.CHAT, text("<yellow>You're on cooldown with <light_purple>Enchanted Golden Apple<yellow> for <white>%time% seconds<yellow>."));
	public static final Translation COMBAT_END = new Translation("combat.end").add(ComponentType.CHAT, text("<red>You're no longer in combat!")).add(ComponentType.SUBTITLE, text("<gray>You're no longer in combat!"));
	public static final Translation COMBAT_LOGGED_BROADCAST = new Translation("combat.rejoin.player").add(ComponentType.CHAT, text("\n<white>%player% <dark_red>has logged out while in combat!\n"));
	public static final Translation COMBAT_REJOINED_BROADCAST = new Translation("combat.rejoin.broadcast").add(ComponentType.CHAT, text("\n<white>%player% <yellow>quit while in combat. They are back now! <gray>x<white>%x%<gray>, y<white>%y%<gray>, y<white>%y%<gray>, <white>%world%\n"));
	public static final Translation COMBAT_REJOINED_PLAYER = new Translation("combat.rejoin.player").add(ComponentType.CHAT, text("<dark_red>You last time quit while in combat!"));
	public static final Translation COMBAT_REJOINED_PLAYER_KILLED = new Translation("combat.rejoin.player.killed").add(ComponentType.CHAT, text("<dark_red>You last time quit while in combat!"));
	public static final Translation COMBAT_REJOINED_PLAYER_NPC_ALIVE = new Translation("combat.rejoin.player.npc-alive").add(ComponentType.CHAT, text("<dark_red>You last time quit while in combat!"));
	public static final Translation COMBAT_CANNOT_USE_COMMANDS = new Translation("combat.command.canceled").add(ComponentType.CHAT, text("<red>You cannot use this command in combat!"));
	public static final Translation COMBAT_CANNOT_USE_TRIDENT_RIPTIDE = new Translation("combat.item.trident-riptide-canceled").add(ComponentType.CHAT, text("<red>You cannot use riptide tridents while in combat!"));
	public static final Translation COMBAT_CANNOT_USE_ELYTRA = new Translation("combat.item.elytra-canceled").add(ComponentType.CHAT, text("<red>You cannot use elytra while in combat!"));
	public static final Translation COMBAT_CANNOT_USE_ELYTRA_BOOST = new Translation("combat.item.elytra-boost-canceled").add(ComponentType.CHAT, text("<red>You cannot use rockets to boost your elytra while in combat!"));
	public static final Translation COMBAT_ENTER_VICTIM = new Translation("combat.enter.victim").add(ComponentType.CHAT, text("<red>You're now in combat with <white>%attacker%<red>!"));
	public static final Translation COMBAT_ENTER_ATTACKER = new Translation("combat.enter.attacker").add(ComponentType.CHAT, text("<red>You're now in combat with <white>%victim%<red>!"));

	public static final Translation REGION_ENTER_IN_COMBAT = new Translation("combat.region.cannot-enter").add(ComponentType.CHAT, text("<red>You are not permitted to enter this region while in combat!"));
	public static final Translation REGION_ENTER_IN_COMBAT_COMBAT_EXTENDED = new Translation("combat.region.cannot-enter-extended-tag").add(ComponentType.CHAT, text("<red>Your combat tag was extended for trying to attempt to escape!"));

	public static Collection<bet.astral.messenger.v2.translation.Translation> getTranslations(){
		return translations.values();
	}

	public static JsonObject getDefaults(){
		return TranslationGsonHelper.getDefaults(Translations.class, MiniMessage.miniMessage(), GSON);
	}

	public static final class Translation extends bet.astral.messenger.v2.translation.Translation {
		public Translation(String key) {
			super(key);
			translations.put(key, this);
		}

		@Override
		public Translation add(ComponentType componentType, Component component) {
			return (Translation) super.add(componentType, component);
		}

		@Override
		public Translation add(ComponentType componentType, Component component, Title.Times times) {
			return (Translation) super.add(componentType, component, times);
		}
	}
}
