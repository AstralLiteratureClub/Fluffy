package me.antritus.astral.fluffycombat;

import lombok.Getter;
import me.antritus.astral.fluffycombat.antsfactions.FactionsPlugin;
import me.antritus.astral.fluffycombat.configs.CombatConfig;
import me.antritus.astral.fluffycombat.hitdetection.*;
import me.antritus.astral.fluffycombat.listeners.*;
import me.antritus.astral.fluffycombat.manager.*;
import org.bukkit.event.Listener;
import org.jetbrains.annotations.Nullable;


@Getter
@SuppressWarnings("removal")
public class FluffyCombat extends FactionsPlugin implements Listener {
	public static boolean isPaper = false;
	public static boolean isStopping = false;
	public static boolean debug = false;
	private CombatManager combatManager;
	private UserManager userManager;
	private BlockUserManager blockUserManager;
	private HookManager hookManager;
	private CooldownManager cooldownManager;
	private CombatConfig combatConfig;
	private AnchorDetection anchorDetection;
	private BedDetection bedDetection;
	private CrystalDetection crystalDetection;
	private TNTDetection tntDetection;
	private MagicDetection magicDetection;

	@Override
	public void updateConfig(@Nullable String oldVersion, String newVersion) {
	}

	@Override
	public void enable() {
		combatConfig = new CombatConfig(this);
		debug = getConfig().getBoolean("debug");

		combatManager = new CombatManager(this);
		userManager = new UserManager(this);
		blockUserManager = new BlockUserManager(this);
		cooldownManager = new CooldownManager(this);
		combatManager.onEnable();
		userManager.onEnable();
		new CMDDebug(this).registerCommand();
		new CMDReload(this).registerCommand();
		new CMDPotions(this).registerCommand();
		registerListeners(new CombatEnterListener(this));
		registerListeners(new PlayerQuitListener(this));
		registerListeners(new PlayerJoinListener(this));
		registerListeners(this);
		registerListeners(new DeathListener(this));
		registerListeners(cooldownManager);
		registerListeners(new TridentListener(this));
		registerListeners(new ElytraListener(this));
		anchorDetection = new AnchorDetection(this);
		bedDetection = new BedDetection(this);
		crystalDetection = new CrystalDetection(this);
		tntDetection = new TNTDetection(this, crystalDetection);
		magicDetection = new MagicDetection(this);
		registerListeners(anchorDetection,
				bedDetection,
				crystalDetection,
				tntDetection,
				magicDetection);


		hookManager = new HookManager(this);
	}

	public void registerListeners(Listener... listener){
		for (Listener list : listener) {
			getServer().getPluginManager().registerEvents(list, this);
		}
	}

	@Override
	public void startDisable() {

	}

	@Override
	public void reloadConfig() {
		super.reloadConfig();
		combatConfig.reload(getConfig());
	}

	@Override
	public void disable() {
		isStopping = true;
		userManager.onDisable();
		combatManager.onDisable();
	}

}