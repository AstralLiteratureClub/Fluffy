package bet.astral.fluffy;

import bet.astral.cloudplusplus.minecraft.paper.bootstrap.BootstrapHandler;
import bet.astral.fluffy.api.CombatUser;
import bet.astral.fluffy.configs.CombatConfig;
import bet.astral.fluffy.database.CombatLogDB;
import bet.astral.fluffy.database.StatisticsDatabase;
import bet.astral.fluffy.listeners.ArmorChangeListener;
import bet.astral.fluffy.listeners.ConnectionListener;
import bet.astral.fluffy.listeners.DeathListener;
import bet.astral.fluffy.listeners.block.LiquidOwnerListener;
import bet.astral.fluffy.listeners.combat.BreakCombatTaggedBlockListener;
import bet.astral.fluffy.listeners.combat.ExecuteCommandWhileInCombatListener;
import bet.astral.fluffy.listeners.combat.QuitWhileInCombatListener;
import bet.astral.fluffy.listeners.combat.begin.BeginCombatListener;
import bet.astral.fluffy.listeners.combat.end.CombatEndListener;
import bet.astral.fluffy.listeners.combat.mobility.ElytraWhileInCombatListener;
import bet.astral.fluffy.listeners.combat.mobility.FlightWhileInCombatListener;
import bet.astral.fluffy.listeners.combat.mobility.TridentWhileInCombatListener;
import bet.astral.fluffy.listeners.hitdetection.*;
import bet.astral.fluffy.listeners.region.RegionWallListener;
import bet.astral.fluffy.manager.*;
import bet.astral.fluffy.messenger.FluffyMessenger;
import bet.astral.messenger.v3.minecraft.paper.PaperMessenger;
import bet.astral.shine.Shine;
import com.jeff_media.armorequipevent.ArmorEquipEvent;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.Bukkit;
import org.bukkit.NamespacedKey;
import org.bukkit.configuration.Configuration;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.Listener;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Set;

import static bet.astral.fluffy.utils.Resource.loadResourceAsTemp;
import static bet.astral.fluffy.utils.Resource.loadResourceToFile;


@Getter
public class FluffyCombat extends JavaPlugin implements Listener {
	public static boolean emergencyStop = false;
	public static final NamespacedKey PROJECTILE_ITEM_KEY = new NamespacedKey("fluffy", "shooter_tool");
	@Getter(AccessLevel.NONE)
	private static final MiniMessage miniMessage = MiniMessage.miniMessage();

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
	private Shine shine;
	private final FluffyMessenger messenger;
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
	private final BootstrapHandler handler;
	private StatisticsDatabase statisticsDatabase;
	private CombatLogDB combatLogDB;

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
		handler.init();
		uploadUploads();
		reloadConfig();
		debug = getConfig().getBoolean("debug");
		combatLogDB = new CombatLogDB(this);
		statisticsDatabase = new StatisticsDatabase(this);
		combatLogDB.onEnable();
		statisticsDatabase.onEnable();

		statisticManager = new StatisticManager(this);
		statisticManager.onEnable();

		shine = new Shine(this);
		combatManager = new CombatManager(this);
		userManager = new UserManager(this);
		blockUserManager = new BlockUserManager(this);
		cooldownManager = new CooldownManager(this);
		combatLogManager = new CombatLogManager();
		combatManager.onEnable();
		userManager.onEnable();
		hookManager.onEnable();

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
		registerListeners(new RegionWallListener(this));
		registerListeners(new DeathListener(this));
		// Register detection helper to improve better block registration and chunk loading
		registerListeners(new DetectionHelper());
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

	@Override
	public void onDisable() {
		isStopping = true;
		userManager.onDisable();
		combatManager.onDisable();
		statisticsDatabase.onDisable();
		combatLogDB.onDisable();
		statisticManager.onDisable();
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