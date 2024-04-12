package bet.astral.fluffy;

import bet.astral.fluffy.api.CombatUser;
import bet.astral.fluffy.commands.EditStatisticsCommand;
import bet.astral.fluffy.commands.StatisticCommand;
import bet.astral.fluffy.configs.CombatConfig;
import bet.astral.fluffy.database.CoreDatabase;
import bet.astral.fluffy.database.sql.mysql.MySQLStatisticDatabase;
import bet.astral.fluffy.listeners.ConnectionListener;
import bet.astral.fluffy.listeners.ArmorChangeListener;
import bet.astral.fluffy.listeners.block.LiquidOwnerListener;
import bet.astral.fluffy.listeners.combat.BreakCombatTaggedBlockListener;
import bet.astral.fluffy.listeners.combat.end.CombatEndListener;
import bet.astral.fluffy.listeners.combat.ExecuteCommandWhileInCombatListener;
import bet.astral.fluffy.listeners.combat.QuitWhileInCombatListener;
import bet.astral.fluffy.listeners.combat.begin.BeginCombatListener;
import bet.astral.fluffy.listeners.combat.mobility.ElytraWhileInCombatListener;
import bet.astral.fluffy.listeners.combat.mobility.FlightWhileInCombatListener;
import bet.astral.fluffy.listeners.combat.mobility.TridentWhileInCombatListener;
import bet.astral.fluffy.listeners.hitdetection.*;
import bet.astral.fluffy.manager.*;
import bet.astral.fluffy.messenger.MessageKey;
import bet.astral.fluffy.statistic.Account;
import bet.astral.fluffy.statistic.Statistic;
import bet.astral.fluffy.statistic.Statistics;
import bet.astral.fluffy.utils.Pair;
import bet.astral.messenger.Messenger;
import bet.astral.messenger.adventure.AdventurePlaceholderManager;
import bet.astral.messenger.message.adventure.serializer.ComponentTypeSerializer;
import bet.astral.messenger.placeholder.PlaceholderManager;
import com.jeff_media.armorequipevent.ArmorEquipEvent;
import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import fr.skytasul.glowingentities.GlowingBlocks;
import fr.skytasul.glowingentities.GlowingEntities;
import lombok.AccessLevel;
import lombok.Getter;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.TextDecoration;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.apache.commons.lang.Validate;
import org.bukkit.*;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeModifier;
import org.bukkit.block.Block;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.Configuration;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockFadeEvent;
import org.bukkit.event.player.PlayerBucketFillEvent;
import org.bukkit.event.world.ChunkLoadEvent;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.plugin.java.JavaPlugin;
import org.incendo.cloud.SenderMapper;
import org.incendo.cloud.execution.ExecutionCoordinator;
import org.incendo.cloud.paper.PaperCommandManager;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.io.IOException;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

import static bet.astral.fluffy.utils.Resource.loadResourceAsTemp;
import static bet.astral.fluffy.utils.Resource.loadResourceToFile;


@Getter
public class FluffyCombat extends JavaPlugin implements Listener {
	public static final NamespacedKey PROJECTILE_ITEM_KEY = new NamespacedKey("fluffy", "shooter_tool");
	public static final NamespacedKey ELYTRA_KEY = new NamespacedKey("fluffy", "elytra");
	@Getter(AccessLevel.NONE)
	private static final MiniMessage miniMessage = MiniMessage.miniMessage();
	public static ItemStack elytraReplacer = new ItemStack(Material.LEATHER_CHESTPLATE);
	@Getter(AccessLevel.NONE)
	private static Map<Chunk, Map<Location, Pair<UUID, Material>>> blockOwners = new HashMap<>();
	public static Pair<UUID, Material> getBlockData(Block block){
		if (blockOwners.get(block.getChunk()) == null){
			return null;
		}

		Pair<UUID, Material> data = blockOwners.get(block.getChunk()).get(block.getLocation());
		if (data == null){
			return null;
		}
		if (data.value2() != block.getType()){
			return null;
		}
		return data;
	}
	public static boolean isOwned(Block block){
		return getBlockData(block) != null;
	}
	public static UUID getBlockOwner(Block block){
		Pair<UUID, Material> blockData = getBlockData(block);
		if (blockData == null){
			return null;
		}
		if (blockData.value2() != block.getType()){
			return null;
		}
		return blockData.value();
	}
	public static @Nullable Block findNearestOwnedBlock(Player player, Material... allowedMaterials) {
		Validate.notNull(player, "Player cannot be null.");

		Location playerLocation = player.getLocation();
		List<Block> surroundingBlocks = getSurroundingBlocks(playerLocation, allowedMaterials);

		return getNearestOwnedBlock(player, surroundingBlocks);
	}

	private static List<Block> getSurroundingBlocks(Location playerLocation, Material... materials) {
		Set<Material> set = Arrays.stream(materials).collect(Collectors.toSet());;
		List<Block> surroundingBlocks = new ArrayList<>();
		int radius = 1; // Adjust as needed
		for (int x = -radius; x <= radius; x++) {
			for (int y = -radius; y <= radius; y++) {
				for (int z = -radius; z <= radius; z++) {
					Block block = playerLocation.clone().add(x, y, z).getBlock();
					if (set.contains(block.getType())) {
						surroundingBlocks.add(block);
					}
				}
			}
		}
		return surroundingBlocks;
	}

	public static @Nullable Block getNearestOwnedBlock(Player player, List<Block> blocks) {
		Block nearestBlock = null;
		double shortestDistance = Double.MAX_VALUE;
		for (Block block : blocks) {
			if (isOwned(block)) {
				double distance = block.getLocation().distanceSquared(player.getLocation());
				if (distance < shortestDistance) {
					shortestDistance = distance;
					nearestBlock = block;
				}
			}
		}
		return nearestBlock;
	}
	public static void clearBlockData(Chunk chunk, Location location){
		if (blockOwners.get(chunk) != null) {
			blockOwners.get(chunk).remove(location);
		}
	}
	public static void clearIncorrectBlockData(Chunk chunk){
		List<Block> remove = new ArrayList<>();
		if (blockOwners.get(chunk) != null) {
			for (Location location : blockOwners.get(chunk).keySet()) {
				if (blockOwners.get(chunk).get(location).value2()!=location.getBlock().getType()){
					if (blockOwners.get(chunk).get(location).value2()==Material.FIRE && location.getBlock().getType() == Material.SOUL_FIRE){
						continue;
					}
					if (blockOwners.get(chunk).get(location).value2()==Material.SOUL_FIRE && location.getBlock().getType() == Material.FIRE){
						continue;
					}
					remove.add(location.getBlock());
				}
			}
		}
		for (Block block : remove){
			blockOwners.get(block.getChunk()).remove(block.getLocation());
		}
	}
	public static void setBlockOwner(OfflinePlayer player, Block block){
		blockOwners.putIfAbsent(block.getChunk(), new HashMap<>());
		blockOwners.get(block.getChunk()).put(block.getLocation(), new Pair<>(player.getUniqueId(), block.getType()));
	}
	public static void setBlockOwner(OfflinePlayer player, Block block, Material material) {
		blockOwners.putIfAbsent(block.getChunk(), new HashMap<>());
		blockOwners.get(block.getChunk()).put(block.getLocation(), new Pair<>(player.getUniqueId(), material));
	}



	/**
	 * Returns the elytra replacer with item meta of given elytra
	 * Returns null if not elytra
	 *
	 * @param original   elytra
	 * @param elytraMode
	 * @return replacer, else if not elytra null
	 */
	@Nullable
	public static ItemStack convertElytraWithReplacer(ItemStack original, CombatConfig.ElytraMode elytraMode) {
		if (original.getType()!=Material.ELYTRA){
			return null;
		}
		ItemStack clone = elytraMode== CombatConfig.ElytraMode.DENY_CHESTPLATE ? elytraReplacer.clone() : original.clone();
		clone.setItemMeta(original.getItemMeta());

		ItemMeta meta = original.getItemMeta();
		if (elytraMode== CombatConfig.ElytraMode.DENY_CHESTPLATE) {
			Component displayname = meta.displayName();
			if (displayname == null)
				displayname = miniMessage.deserialize("<Yellow>Elytra Placeholder").decoration(TextDecoration.ITALIC, false);
			meta.displayName(displayname);
		}
		@Nullable List<Component> lore = meta.lore();
		if (lore == null) {
			lore = new LinkedList<>();
		} else {
			lore = new ArrayList<>(lore);
		}

		lore.add(Component.text().build());
		lore.add(miniMessage.deserialize("<dark_gray> | <gray>This item will disappear soon").decoration(TextDecoration.ITALIC, false));
		lore.add(miniMessage.deserialize("<dark_gray> |  <gray>and give your elytra back!").decoration(TextDecoration.ITALIC, false));
		lore.add(miniMessage.deserialize("<dark_gray> | <gray>Bugged? <yellow>@antritus <dark_aqua>(DISCORD) <gray>report this").decoration(TextDecoration.ITALIC, false));
		lore.add(miniMessage.deserialize("<dark_gray> |  <gray>bug so it may be fixed.").decoration(TextDecoration.ITALIC, false));
		meta.lore(lore);

		if (elytraMode== CombatConfig.ElytraMode.DENY_CHESTPLATE) {
			meta.removeAttributeModifier(Attribute.GENERIC_ARMOR);
			meta.addAttributeModifier(Attribute.GENERIC_ARMOR, new AttributeModifier("FluffyElytraReplacer", -3, AttributeModifier.Operation.ADD_NUMBER));
			meta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES);
			if (!meta.hasEnchants()) {
				meta.addItemFlags(ItemFlag.HIDE_ENCHANTS);
			}
			clone.setItemMeta(meta);
		}

		meta.getPersistentDataContainer().set(ELYTRA_KEY, PersistentDataType.BOOLEAN, true);
		clone.setItemMeta(meta);

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
	private StatisticManager statisticManager;
	private PaperCommandManager<CommandSender> commandManager;

	private CombatConfig combatConfig;

	private AnchorDetection anchorDetection;
	private BedDetection bedDetection;
	private CrystalDetection crystalDetection;
	private TNTDetection tntDetection;
	private MagicDetection magicDetection;
	private LiquidOwnerListener lavaDetection;
	private FireDetection fireDetection;

	private FileConfiguration configuration;

	@Getter
	private CoreDatabase database;

	private GlowingEntities glowingEntities;
	private GlowingBlocks glowingBlocks;

	@Override
	public void onEnable() {
		uploadUploads();
		configuration = YamlConfiguration.loadConfiguration(new File(getDataFolder(), "config.yml"));
		combatConfig = new CombatConfig(this);
		reloadConfig();
		debug = getConfig().getBoolean("debug");

		commandManager = new PaperCommandManager<>(
				this,
				ExecutionCoordinator.asyncCoordinator(),
				SenderMapper.identity());
		commandManager.registerBrigadier();
		commandManager.registerAsynchronousCompletions();

		new StatisticCommand(this, commandManager);
		new EditStatisticsCommand(this, commandManager);

		FileConfiguration configuration = YamlConfiguration.loadConfiguration(new File(getDataFolder(), "messages.yml"));
		messageManager = new Messenger<>(this, commandManager, new HashMap<>(), new ComponentTypeSerializer(), configuration);
		PlaceholderManager placeholderManager = new AdventurePlaceholderManager();
		placeholderManager.setDefaults(placeholderManager.loadPlaceholders("placeholders", configuration));
		messageManager.setPlaceholderManager(placeholderManager);

		// Just loading the plugin-specific messages.
		MessageKey.loadMessages(messageManager);

		database = new CoreDatabase(this);

		final HikariConfig config = new HikariConfig();
		config.setMaximumPoolSize(10);
		config.setIdleTimeout(2000L);
		config.setJdbcUrl(getConfig().getString("database.url"));
		config.setPassword(getConfig().getString("database.password"));
		config.setUsername(getConfig().getString("database.username"));
		final HikariDataSource hikariDataSource = new HikariDataSource(config);
		Function<org.javatuples.Pair<Account, Map<String, Integer>>, Account> function = (pair)-> {
			Account account = pair.getValue0();
			Map<String, Integer> stats = pair.getValue1();
			for (Map.Entry<String, Integer> entry : stats.entrySet()) {
				Statistic statistic = Statistics.valueOf(entry.getKey());
				if (statistic == null){
					continue;
				}
				account.set(statistic, entry.getValue());
			}
			return account;
		};
		database.addDatabase(
				new MySQLStatisticDatabase(this,
						new Statistic[]{
								Statistics.KILLS_GLOBAL,
								Statistics.KILLS_CRYSTAL,
								Statistics.KILLS_ANCHOR,
								Statistics.KILLS_BED,
								Statistics.KILLS_TNT
						},
						hikariDataSource,
						"fluffy_kills",
						function
				)
		);
		database.addDatabase(
				new MySQLStatisticDatabase(this,
						new Statistic[]{
								Statistics.DEATHS_GLOBAL,
								Statistics.DEATHS_CRYSTAL,
								Statistics.DEATHS_ANCHOR,
								Statistics.DEATHS_BED,
								Statistics.DEATHS_TNT
						},
						hikariDataSource,
						"fluffy_deaths",
						function
				)
		);
		database.addDatabase(
				new MySQLStatisticDatabase(this,
						new Statistic[]{
								Statistics.STREAK_KILLS,
								Statistics.STREAK_DEATHS,
						},
						hikariDataSource,
						"fluffy_streak",
						function
				)
		);



		statisticManager = new StatisticManager(this);
		statisticManager.onEnable();

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
		new CMDBlockOwner(this).registerCommand();
		registerListeners(this);
		registerListeners(cooldownManager);
		registerListeners(statisticManager);
		registerListeners(new LiquidOwnerListener(this));
		registerListeners(new BeginCombatListener(this));
		registerListeners(new ElytraWhileInCombatListener(this));
		registerListeners(new FlightWhileInCombatListener(this));
		registerListeners(new TridentWhileInCombatListener(this));
		registerListeners(new BreakCombatTaggedBlockListener(this));
		registerListeners(new CombatEndListener(this));
		registerListeners(new ExecuteCommandWhileInCombatListener(this));
		registerListeners(new QuitWhileInCombatListener(this));
		registerListeners(new LiquidOwnerListener(this));
		registerListeners(new ConnectionListener(this));
		registerListeners(new ArmorChangeListener(this));

		ArmorEquipEvent.registerListener(this);

		anchorDetection = new AnchorDetection(this);
		crystalDetection = new CrystalDetection(this);
		bedDetection = new BedDetection(this);
		tntDetection = new TNTDetection(this, crystalDetection);
		magicDetection = new MagicDetection(this);
		fireDetection = new FireDetection(this);
		lavaDetection = new LiquidOwnerListener(this);
		registerListeners(
				anchorDetection,
				bedDetection,
				crystalDetection,
				fireDetection,
				tntDetection,
				magicDetection,
				lavaDetection
		);


		hookManager = new HookManager(this);

		//statisticDatabase = new CombinedStatisticDatabase(this);

		registerListeners(this);

		getServer().getScheduler().runTaskTimerAsynchronously(this, () -> {
			for (Player player : Bukkit.getOnlinePlayers()) {
				CombatUser user = userManager.getUser(player.getUniqueId());
				if (user.getLastFireDamage() != null && player.getFireTicks() == 0) {
					user.setLastFireDamage(null);
				}
			}
		}, 1, 1);

		getLogger().info("Fluffy Combat Management plugin has loaded!");
	}
	@EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
	public void onChunkLoad(ChunkLoadEvent event) {
		clearIncorrectBlockData(event.getChunk());
	}

	@EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
	private void blockBreak(BlockBreakEvent event){
		FluffyCombat.clearBlockData(event.getBlock().getChunk(), event.getPlayer().getLocation());
	}

	@EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
	public void bucketFill(PlayerBucketFillEvent event){
		clearBlockData(event.getBlock().getChunk(), event.getBlock().getLocation());
	}

	@EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
	public void blockFade(BlockFadeEvent event){
		clearBlockData(event.getBlock().getChunk(), event.getBlock().getLocation());
	}

	@Override
	public void onDisable() {
		isStopping = true;
		userManager.onDisable();
		combatManager.onDisable();
		statisticManager.onDisable();
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