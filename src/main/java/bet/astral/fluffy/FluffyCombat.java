package bet.astral.fluffy;

import bet.astral.cloudplusplus.minecraft.paper.bootstrap.BootstrapHandler;
import bet.astral.fluffy.api.CombatUser;
import bet.astral.fluffy.configs.CombatConfig;
import bet.astral.fluffy.cosmetics.manager.EffectManager;
import bet.astral.fluffy.cosmetics.parser.EffectParser;
import bet.astral.fluffy.database.ICombatLogDatabase;
import bet.astral.fluffy.database.SQLiteCombatLogDatabase;
import bet.astral.fluffy.database.StatisticsDatabase;
import bet.astral.fluffy.listeners.block.LiquidOwnerListener;
import bet.astral.fluffy.listeners.hitdetection.*;
import bet.astral.fluffy.manager.*;
import bet.astral.fluffy.messenger.DeathTranslations;
import bet.astral.fluffy.messenger.FluffyMessenger;
import bet.astral.fluffy.messenger.Translations;
import bet.astral.guiman.GUIMan;
import bet.astral.messenger.v2.source.LanguageTable;
import bet.astral.messenger.v2.source.LanguageTableImpl;
import bet.astral.messenger.v2.source.source.LanguageSource;
import bet.astral.messenger.v2.source.source.gson.GsonLanguageSource;
import bet.astral.messenger.v2.translation.TranslationKeyRegistry;
import bet.astral.messenger.v3.minecraft.paper.PaperMessenger;
import bet.astral.more4j.tuples.Pair;
import com.jeff_media.armorequipevent.ArmorEquipEvent;
import io.github.classgraph.ClassGraph;
import io.github.classgraph.ClassInfoList;
import io.github.classgraph.ScanResult;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.apache.commons.lang3.Validate;
import org.bukkit.*;
import org.bukkit.block.Block;
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
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.*;
import java.util.stream.Collectors;

import static bet.astral.fluffy.utils.Resource.loadResourceAsTemp;
import static bet.astral.fluffy.utils.Resource.loadResourceToFile;


@Getter
public class FluffyCombat extends JavaPlugin implements Listener {
	public static boolean emergencyStop = false;
	public static final NamespacedKey PROJECTILE_ITEM_KEY = new NamespacedKey("fluffy", "shooter_tool");
	@Getter(AccessLevel.NONE)
	private static final MiniMessage miniMessage = MiniMessage.miniMessage();
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
		if (data.getSecond() != block.getType()){
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
		if (blockData.getSecond() != block.getType()){
			return null;
		}
		return blockData.getFirst();
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
				if (blockOwners.get(chunk).get(location).getSecond()!=location.getBlock().getType()){
					if (blockOwners.get(chunk).get(location).getSecond()==Material.FIRE && location.getBlock().getType() == Material.SOUL_FIRE){
						continue;
					}
					if (blockOwners.get(chunk).get(location).getSecond()==Material.SOUL_FIRE && location.getBlock().getType() == Material.FIRE){
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
		blockOwners.get(block.getChunk()).put(block.getLocation(), Pair.immutable(player.getUniqueId(), block.getType()));
	}
	public static void setBlockOwner(OfflinePlayer player, Block block, Material material) {
		blockOwners.putIfAbsent(block.getChunk(), new HashMap<>());
		blockOwners.get(block.getChunk()).put(block.getLocation(), Pair.immutable(player.getUniqueId(), material));
	}

	public static boolean isPaper;

    static {
        try {
            Class.forName("io.papermc.paper.plugin.bootstrap.PluginBootstrap");
            isPaper = true;
        } catch (ClassNotFoundException e) {
            throw new RuntimeException(e);
        }
    }

    public static boolean isStopping = false;
	public static boolean debug = false;
	private FluffyMessenger messenger;
	private CombatManager combatManager;
	private UserManager userManager;
	private BlockUserManager blockUserManager;
	private final HookManager hookManager = new HookManager(this);
	private CooldownManager cooldownManager;
	private CombatLogManager combatLogManager;
	private StatisticManager statisticManager;
	@Setter
	private RegionManager regionManager = RegionManager.NONE;
	@Setter
	private NPCManager npcManager = NPCManager.NONE;
	private CombatConfig combatConfig;
	private AnchorDetection anchorDetection;
	private BedDetection bedDetection;
	private CrystalDetection crystalDetection;
	private TNTDetection tntDetection;
	private MagicDetection magicDetection;
	private LiquidOwnerListener lavaDetection;
	private FireDetection fireDetection;
	private FileConfiguration configuration;
	private BootstrapHandler handler;
	private StatisticsDatabase statisticsDatabase;
	private ICombatLogDatabase combatLogDatabase;
	private EffectManager deathEffectManager = new EffectManager(this, "death-effects.json");
	private EffectManager hitEffectManager = new EffectManager(this, "hit-effects.json");


	public FluffyCombat(@NotNull BootstrapHandler handler, FluffyMessenger messenger) {
		this.handler = handler;
		this.messenger = messenger;
	}

	@Override
	public void onLoad() {
		configuration = YamlConfiguration.loadConfiguration(new File(getDataFolder(), "config.yml"));
		combatConfig = new CombatConfig(this);

		hookManager.onLoad();
	}

	@Override
	public void onEnable() {
		PaperMessenger.init(this);
		GUIMan.init(this);
		handler.init();
		reloadConfig();
		debug = getConfig().getBoolean("debug");
		combatLogDatabase = new SQLiteCombatLogDatabase(this);
		statisticsDatabase = new StatisticsDatabase(this);
		combatLogDatabase.onEnable();
		statisticsDatabase.onEnable();

		statisticManager = new StatisticManager(this);
		statisticManager.onEnable();

		combatManager = new CombatManager(this);
		userManager = new UserManager(this);
		blockUserManager = new BlockUserManager(this);
		cooldownManager = new CooldownManager(this);
		combatLogManager = new CombatLogManager();
		combatManager.onEnable();
		userManager.onEnable();
		getComponentLogger().info("Starting hook manager...");
		hookManager.onEnable();
		getComponentLogger().info("Hook manager loaded!");

		registerDefaultListeners();
		if (!getServer().getPluginManager().isPluginEnabled(this)) {
			FluffyCombat.emergencyStop = true;
			return;
		}
		registerListeners(this);
		registerListeners(cooldownManager);
		registerListeners(statisticManager);
		/*
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
		registerListeners(new RegionWallListener(this));
		registerListeners(new DeathListener(this));
		 */
		if (npcManager instanceof Listener listener) {
			registerListeners(listener);
		}

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

		//statisticDatabase = new CombinedStatisticDatabase(this);

		registerListeners(this);

		getServer().getScheduler().runTaskTimerAsynchronously(this, () -> {
			for (Player player : Bukkit.getOnlinePlayers()) {
				CombatUser user = userManager.getUser(player.getUniqueId());
				if (user==null) {
					continue;
				}
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
		emergencyStop = true;
		isStopping = true;

		disableManagers();
	}

	private void disableManagers() {
		Class<?> clazz = this.getClass();

		for (Field field : clazz.getDeclaredFields()) {
			field.setAccessible(true);
            try {
                if (!field.getDeclaringClass().isAssignableFrom(Manager.class)) {
					return;
                }

				Manager obj = (Manager) field.get(this);
				disableIfNotNull(obj);
            } catch (IllegalAccessException e) {
                throw new RuntimeException(e);
            }
        }
	}

	void disableIfNotNull(Object obj) {
		if (obj == null) {
			return;
		}

		Class<?> clazz = obj.getClass();
        try {
            Method method = clazz.getDeclaredMethod("onDisable");
			method.setAccessible(true);
			method.invoke(clazz);
        } catch (NoSuchMethodException e) {
            getComponentLogger().error("Couldn't find onDisable() on class {}", clazz.getName(), e);
        } catch (InvocationTargetException e) {
            getComponentLogger().error("Couldn't invoke onDisable() on class {}", clazz.getName(), e);
        } catch (IllegalAccessException e) {
            getComponentLogger().error("Couldn't access onDisable() on class {}", clazz.getName(), e);
        }
    }

	void registerDefaultListeners() {
		ClassGraph classGraph = new ClassGraph().acceptPackages("bet.astral.fluffy.listeners").enableAllInfo();
		ScanResult result = classGraph.scan();
		ClassInfoList classInfo = result.getClassesImplementing(Listener.class.getName());

		try {
			List<String> classes = classInfo.getNames();
			for (String clazzName : classes) {
				Class<?> clazz = null;
				try {
					clazz = Class.forName(clazzName);
					if (clazz.isInterface()) {
						continue;
					}
					Listener listener = null;
					if (isFluffyConstructorListener(clazz)) {
						Constructor<?> constructor = clazz.getConstructor(this.getClass());
						constructor.setAccessible(true);
						listener = (Listener) constructor.newInstance(this);
					} else if (isEmptyConstructorListener(clazz)) {
						Constructor<?> constructor = clazz.getConstructor();
						constructor.setAccessible(true);
						listener = (Listener) constructor.newInstance();
					} else {
						continue;
					}

					registerListeners(listener);

				} catch (ClassNotFoundException e) {
					throw new RuntimeException("Couldn't find class " + clazzName + " when trying to register listeners", e);
				} catch (InvocationTargetException e) {
					throw new RuntimeException("Couldn't invoke constructor of " + clazzName + " when trying to register listeners", e);
				} catch (InstantiationException e) {
					throw new RuntimeException("Couldn't couldn't create a new instance of class " + clazzName + " when trying to register listeners", e);
				} catch (IllegalAccessException e) {
					throw new RuntimeException("Couldn't access constructor in class " + clazzName + " when trying to register listeners", e);
				} catch (NoSuchMethodException e) {
					throw new RuntimeException("Couldn't find constructor in class " + clazzName + " when trying to register listeners", e);
				}
			}
		} catch (RuntimeException e) {
			getComponentLogger().error("Couldn't register listeners when trying to initialize fluffy! Shutting down the plugin.", e);
			getServer().getPluginManager().disablePlugin(this);
		}
	}

	boolean isFluffyConstructorListener(Class<?> clazz) {
        try {
            Constructor<?> constructor = clazz.getDeclaredConstructor(FluffyCombat.class);
			return true;
        } catch (NoSuchMethodException e) {
			return false;
        }
    }

	boolean isEmptyConstructorListener(Class<?> clazz) {
		try {
			Constructor<?> constructor = clazz.getDeclaredConstructor();
			return true;
		} catch (NoSuchMethodException e) {
			return false;
		}
	}

	public void registerListeners(Listener @NotNull ... listener){
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
		uploadUploads();

		super.reloadConfig();
		// Reload the plugin config
		combatConfig.reload(getConfig());
		// Clear registry to allow new registry of effects
		hitEffectManager.clearRegistry();
		deathEffectManager.clearRegistry();
		// Parse hit effects
		new EffectParser(this, hitEffectManager).parse(new File(getDataFolder(), "hit-effects.json"));
		new EffectParser(this, deathEffectManager).parse(new File(getDataFolder(), "death-effects.json"));

		// Clear all languages loaded.
		messenger.getLanguages().clear();

		// Register new US language source

		registerGsonLanguageSource("/messages/en_us.json");

		// Load all core translations
		messenger.loadTranslations(Translations.class);
		messenger.loadTranslations(DeathTranslations.class);

		// Load translations from hit effects
		registerEffectTranslations(hitEffectManager);
		registerEffectTranslations(deathEffectManager);
	}

	void registerGsonLanguageSource(String path) {
		TranslationKeyRegistry registry = TranslationKeyRegistry.create();
		LanguageSource languageSource = new GsonLanguageSource(messenger, Locale.US, new File(getDataFolder(), path), MiniMessage.miniMessage());
		LanguageTable languageTable = new LanguageTableImpl(registry, languageSource, languageSource.getLocale());
		messenger.registerLanguageTable(languageTable.getLocale(), languageTable);
	}

	void registerEffectTranslations(EffectManager effectManager) {
		registerGsonLanguageSource(effectManager.getFileName());

		effectManager.loadTranslations();
	}

	private void uploadUploads(){
		String[] files = new String[]{
				"config|yml",
				"deaths|yml",
				"deaths-npc|yml",
				"statistics|json",
				"hit-effects|json",
				"death-effects|json",
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