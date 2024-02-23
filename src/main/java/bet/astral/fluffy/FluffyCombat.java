package bet.astral.fluffy;

import bet.astral.fluffy.configs.CombatConfig;
import bet.astral.fluffy.database.StatisticDatabase;
import bet.astral.fluffy.listeners.hitdetection.*;
import bet.astral.fluffy.listeners.*;
import bet.astral.fluffy.manager.*;
import bet.astral.fluffy.messenger.MessageKey;
import bet.astral.messenger.Messenger;
import bet.astral.messenger.placeholder.Placeholder;
import fr.skytasul.glowingentities.GlowingBlocks;
import fr.skytasul.glowingentities.GlowingEntities;
import lombok.AccessLevel;
import lombok.Getter;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.TextDecoration;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.*;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeModifier;
import org.bukkit.block.Block;
import org.bukkit.configuration.Configuration;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.world.ChunkLoadEvent;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.io.IOException;
import java.util.*;

import static bet.astral.fluffy.utils.Resource.loadResourceAsTemp;
import static bet.astral.fluffy.utils.Resource.loadResourceToFile;


@Getter
public class FluffyCombat extends JavaPlugin implements Listener {
	@Getter(AccessLevel.NONE)
	private static final MiniMessage miniMessage = MiniMessage.miniMessage();
	public static ItemStack elytraReplacer = new ItemStack(Material.LEATHER_CHESTPLATE);
	@Getter(AccessLevel.NONE)
	private static Map<Chunk, Map<Location, Pair<UUID, Material>>> blockOwners = new HashMap<>();
	public static Pair<UUID, Material> getBlockData(Block block){
		return blockOwners.get(block.getChunk()) != null ? ((blockOwners.get(block.getChunk()).get(block.getLocation())) != null ? blockOwners.get(block.getChunk()).get(block.getLocation()) : null): null;
	}
	public static UUID getBlockOwner(Block block){
		return blockOwners.get(block.getChunk()) != null ? ((blockOwners.get(block.getChunk()).get(block.getLocation())) != null ? blockOwners.get(block.getChunk()).get(block.getLocation()).value() : null): null;
	}
	public static void clearBlockData(Chunk chunk, Location location){
		if (blockOwners.get(chunk) != null) {
			blockOwners.get(chunk).remove(location);
		}
	}
	public static void clearIncorrectBlockData(Chunk chunk){
		if (blockOwners.get(chunk) != null) {
			for (Location location : blockOwners.get(chunk).keySet()) {
				if (blockOwners.get(chunk).get(location).value2()!=location.getBlock().getType()){
					if (blockOwners.get(chunk).get(location).value2()==Material.FIRE && location.getBlock().getType() == Material.SOUL_FIRE){
						continue;
					}
					if (blockOwners.get(chunk).get(location).value2()==Material.SOUL_FIRE && location.getBlock().getType() == Material.FIRE){
						continue;
					}
					blockOwners.get(chunk).remove(location);
				}
			}
		}
	}
	public static void setBlockOwner(OfflinePlayer player, Block block){
		blockOwners.putIfAbsent(block.getChunk(), new HashMap<>());
		blockOwners.get(block.getChunk()).put(block.getLocation(), new Pair<>(player.getUniqueId(), block.getType()));
	}


	/**
	 *  Returns the elytra replacer with item meta of given elytra
	 *  Returns null if not elytra
	 * @param original elytra
	 * @return replacer, else if not elytra null
	 */
	@Nullable
	public static ItemStack convertElytraWithReplacer(ItemStack original) {
		if (original.getType()!=Material.ELYTRA){
			return null;
		}
		ItemStack clone = elytraReplacer.clone();
		clone.setItemMeta(original.getItemMeta());
		ItemMeta meta = original.getItemMeta();
		Component displayname = meta.displayName();
		if (displayname == null)
			displayname = miniMessage.deserialize("<Yellow>Elytra Placeholder").decoration(TextDecoration.ITALIC, false);
		meta.displayName(displayname);
		@Nullable List<Component> lore = meta.lore();
		if (lore == null) {
			lore = new LinkedList<>();
		}
		lore.add(Component.text().build());
		lore.add(miniMessage.deserialize("<dark_gray> | <gray>This item will disappear soon").decoration(TextDecoration.ITALIC, false));
		lore.add(miniMessage.deserialize("<dark_gray> |  <gray>and give your elytra back!").decoration(TextDecoration.ITALIC, false));
		lore.add(miniMessage.deserialize("<dark_gray> | <gray>Bugged? <yellow>@antritus <dark_aqua>(DISCORD) <gray>report this").decoration(TextDecoration.ITALIC, false));
		lore.add(miniMessage.deserialize("<dark_gray> |  <gray>bug so it may be fixed.").decoration(TextDecoration.ITALIC, false));
		meta.lore(lore);

		meta.removeAttributeModifier(Attribute.GENERIC_ARMOR);
		meta.addAttributeModifier(Attribute.GENERIC_ARMOR, new AttributeModifier("FluffyElytraReplacer", -3, AttributeModifier.Operation.ADD_NUMBER));
		meta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES);
		if (!meta.hasEnchants()){
			meta.addItemFlags(ItemFlag.HIDE_ENCHANTS);
		}
		clone.setItemMeta(meta);
		clone.addUnsafeEnchantment(Enchantment.BINDING_CURSE, 5);
		clone.addUnsafeEnchantment(Enchantment.VANISHING_CURSE, 5);
		return clone;
	}
	public static boolean isPaper = false;
	public static boolean isStopping = false;
	public static boolean debug = false;
	private Messenger<FluffyCombat> messageManager;
	private CombatManager combatManager;
	private UserManager userManager;
	private BlockUserManager blockUserManager;
	private HookManager hookManager;
	private CooldownManager cooldownManager;
	private CombatLogManager combatLogManager;

	private CombatConfig combatConfig;

	private AnchorDetection anchorDetection;
	private BedDetection bedDetection;
	private CrystalDetection crystalDetection;
	private TNTDetection tntDetection;
	private MagicDetection magicDetection;
	private LavaDetection lavaDetection;
	private LavaCauldronDetection lavaCauldronDetection;
	private FireDetection fireDetection;

	private FileConfiguration configuration;
	private StatisticDatabase statisticDatabase;

	private GlowingEntities glowingEntities;
	private GlowingBlocks glowingBlocks;

	@Override
	public void onEnable() {
		uploadUploads();
		configuration = YamlConfiguration.loadConfiguration(new File(getDataFolder(), "config.yml"));
		combatConfig = new CombatConfig(this);
		reloadConfig();
		debug = getConfig().getBoolean("debug");
		FileConfiguration configuration = YamlConfiguration.loadConfiguration(new File(getDataFolder(), "messages.yml"));
		messageManager = new Messenger<>(this, configuration, new HashMap<>());
		getLogger().info("Loading placeholders for messages...");
		Map<String, Placeholder> placeholderMap = messageManager.loadPlaceholders("placeholders");
		getLogger().info("Loaded placeholders for messages...");
		// Debug
		if (placeholderMap == null){
			placeholderMap = new HashMap<>();
		} else {
			placeholderMap = new HashMap<>(placeholderMap);
		}
		getLogger().info("Overriding message placeholders...");
		messageManager.overrideDefaultPlaceholders(placeholderMap);
		getLogger().info("Overrode message placeholders...");
		// Just loading the plugin-specific messages.
		MessageKey.loadMessages(messageManager);

		statisticDatabase = new StatisticDatabase(this);

		glowingEntities = new GlowingEntities(this); // required in combat manager
		glowingBlocks = new GlowingBlocks(this); // required in combat manager
		combatManager = new CombatManager(this);
		userManager = new UserManager(this);
		blockUserManager = new BlockUserManager(this);
		cooldownManager = new CooldownManager(this);
		combatLogManager = new CombatLogManager();
		combatManager.onEnable();
		userManager.onEnable();
		new CMDDebug(this).registerCommand();
		new CMDReload(this).registerCommand();
		new CMDPotions(this).registerCommand();
		new CMDGlow(this).registerCommand();
		registerListeners(new PlayerBeginCombatListener(this));
		registerListeners(new PlayerGlowDisableListener(this));
		registerListeners(new PlayerExitWhileInCombatListener(this));
		registerListeners(new PlayerJoinListener(this));
		registerListeners(this);
		registerListeners(new DeathWhileInCombatListener(this));
		registerListeners(cooldownManager);
		registerListeners(new TridentWhileInCombatListener(this));
		registerListeners(new ElytraWhileInCombatListener(this));
		if (Compatibility.RESPAWN_ANCHOR.isCompatible())
			anchorDetection = new AnchorDetection(this);
		if (Compatibility.ENDER_CRYSTAL.isCompatible())
			crystalDetection = new CrystalDetection(this);
		if (Compatibility.BED.isCompatible()) //
			bedDetection = new BedDetection(this);
		tntDetection = new TNTDetection(this, crystalDetection);
		magicDetection = new MagicDetection(this);
		fireDetection = new FireDetection(this);
		lavaDetection = new LavaDetection(this);
		lavaCauldronDetection = new LavaCauldronDetection(this);
		registerListeners(anchorDetection,
				bedDetection,
				crystalDetection,
				tntDetection,
				magicDetection,
				fireDetection,
				lavaDetection,
				lavaCauldronDetection
				);


		hookManager = new HookManager(this);

		//statisticDatabase = new StatisticDatabase(this);

		registerListeners(this);

		getLogger().info("Fluffy Combat Management plugin has loaded!");
	}
	@EventHandler
	public void onChunkLoad(ChunkLoadEvent event) {
		clearIncorrectBlockData(event.getChunk());
	}

	@Override
	public void onDisable() {
		isStopping = true;
		userManager.onDisable();
		combatManager.onDisable();
		statisticDatabase.disable();
		glowingEntities.disable();
		glowingBlocks.disable();
	}


	public void registerListeners(Listener... listener){
		for (Listener list : listener) {
			if (list == null){
				continue;
			}
			getServer().getPluginManager().registerEvents(list, this);
		}
	}

	@Override
	public @NotNull FileConfiguration getConfig(){
		return configuration;
	}

	@Override
	public void reloadConfig() {
		super.reloadConfig();
		combatConfig.reload(getConfig());
	}

	private void uploadUploads(){
		String[] files = new String[]{
				"config|yml",
				"deaths|yml",
				"deaths-npc|yml",
				"messages|yml",
		};
		for (String name : files){
			name = name.replace("dm/", "discord-messages/");

			String[] split = name.split("\\|");
			String fileName = split[0];
			String ending = split[1];
			File fileTemp = loadResourceAsTemp("/upload/"+fileName, ending);
			File file = loadResourceToFile("/upload/"+fileName, ending, new File(getDataFolder(), fileName+"."+ending), true);
			if (ending.matches("(?i)yml") || ending.matches("(?i)yaml")){
				loadConfig(getConfig(fileTemp), getConfig(file), file);
			}
		}
	}

	private void loadConfig(FileConfiguration tempConfig, FileConfiguration config, File file){
		Set<String> keys = tempConfig.getKeys(false);
		for (String key : keys){
			addDefaults(key, tempConfig, config);
		}
		try {
			config.save(file);
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}

	private void addDefaults(String key, Configuration tempConfig, Configuration config) {
		List<String> comment = tempConfig.getComments(key);
		if (!comment.isEmpty() && config.getInlineComments(key).isEmpty()) {
			config.setComments(key, comment);
		}
		comment = tempConfig.getInlineComments(key);
		if (!comment.isEmpty() && config.getInlineComments(key).isEmpty()) {
			config.setInlineComments(key, comment);
		}
		Object value = tempConfig.get(key); // Retrieve the value from the tempConfig
		if (value instanceof ConfigurationSection section) {
			for (String k : section.getKeys(false)) {
				addDefaults(key + "." + k, tempConfig, config); // Append current key
			}
		}
	}

	private FileConfiguration getConfig(File file){
		return YamlConfiguration.loadConfiguration(file);
	}


}