package bet.astral.fluffy.hooks.placeholderapi;

import bet.astral.fluffy.FluffyCombat;
import bet.astral.fluffy.api.BlockCombatUser;
import bet.astral.fluffy.api.CombatTag;
import bet.astral.fluffy.api.CombatUser;
import bet.astral.fluffy.hooks.Hook;
import bet.astral.fluffy.hooks.HookState;
import bet.astral.fluffy.manager.CombatManager;
import bet.astral.fluffy.manager.UserManager;
import bet.astral.fluffy.statistic.Account;
import bet.astral.fluffy.statistic.Statistics;
import me.clip.placeholderapi.PlaceholderAPI;
import me.clip.placeholderapi.PlaceholderAPIPlugin;
import me.clip.placeholderapi.expansion.PlaceholderExpansion;
import org.bukkit.OfflinePlayer;
import org.bukkit.attribute.Attribute;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffectType;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.text.DecimalFormat;
import java.util.Arrays;
import java.util.logging.Level;

public class PlaceholderAPIHook extends PlaceholderExpansion implements Hook {
	private final DecimalFormat decimalFormat = new DecimalFormat(".00");
	private final PlaceholderAPIPlugin hookPlugin;
	private final Class<?> hookClass;
	private final FluffyCombat fluffy;
	private final HookState hookState;

	public PlaceholderAPIHook(@NotNull FluffyCombat fluffyCombat, @Nullable PlaceholderAPIPlugin papi, @Nullable Class<?> clazz, @NotNull HookState state) {
		this.fluffy = fluffyCombat;
		this.hookState = state;
		this.hookPlugin = papi;
		this.hookClass = clazz;
	}

	@Override
	public FluffyCombat main() {
		return fluffy;
	}

	@Override
	public PlaceholderAPIPlugin hookPlugin() {
		return hookPlugin;
	}

	@Override
	public Class<?> hookPluginClass() {
		return hookClass;
	}

	@Override
	public HookState state() {
		return hookState;
	}

	@Override
	public void onLoad() {
	}

	@Override
	public void onEnable() {
		if (hookPlugin != null){
			//noinspection UnstableApiUsage
			hookPlugin.getLocalExpansionManager().register(this);
		}
	}

	@Override
	public @NotNull String getIdentifier() {
		return "fluffy";
	}

	@Override
	public @NotNull String getAuthor() {
		return "Antritus";
	}

	@Override
	public @NotNull String getVersion() {
		return "1.0";
	}

	@Override
	public @Nullable String onRequest(OfflinePlayer player, @NotNull String params) {
		String[] args = params.split(";")[0].split("_");
		String[] flags = params.split(";");
		CombatManager combatManager = fluffy.getCombatManager();
		boolean requireValueElseEmpty = Arrays.stream(flags).anyMatch(flag->flag.equalsIgnoreCase("value_else_empty"));
		String timeFormat = "MM:ss";
		if (args.length>0) {
			if (player != null && player.isOnline()) {
				switch (args[0].toLowerCase()){
					case "opponent"->{
						boolean requirePlayerOpponent = Arrays.stream(flags).anyMatch(flag->flag.equalsIgnoreCase("require_player"));
						CombatTag tag = combatManager.getLatest(player);
						if (tag == null){
							return requireValueElseEmpty ? "" : null;
						}
						boolean isBlock = tag.getAttacker() instanceof BlockCombatUser;
						if (isBlock && requirePlayerOpponent) {
							return requireValueElseEmpty ? "" : null;
						}
						CombatUser opponent = tag.getAttacker().getUniqueId()==player.getUniqueId()?tag.getAttacker() : tag.getVictim();
						CombatUser self = tag.getAttacker().getUniqueId()==player.getUniqueId()?tag.getAttacker() : tag.getVictim();
						if (args.length>1){
							switch (args[1]) {
								case "placeholder"->{
									// Blocks can't have placeholders
									if (isBlock){
										return requireValueElseEmpty ? "" : null;
									}
									StringBuilder builder = new StringBuilder();
									for (int i = 2; i<args.length; i++){
										try {
											if (!builder.isEmpty()){
												builder.append("_");
											}
											builder.append(args[i]);
										} catch (IndexOutOfBoundsException ignore){
											break;
										}
									}
									for (String flag : flags){
										builder.append(";").append(flag);
									}
									if (args[2].equalsIgnoreCase("fluffy")){
										builder.append(";internal_opponent");
									}
									String value = PlaceholderAPI.setPlaceholders(player, builder.toString());
									if (!value.equalsIgnoreCase(builder.toString())){
										return obfuscate(opponent, value, params);
									}
									return null;
								}
								case "time_left"->{
									int ticks = tag.getTicksLeft(self);
									return ticks+"";
								}
								case "health"->{
									OfflinePlayer oPlayer = opponent instanceof  BlockCombatUser ? null : opponent.getPlayer();
									return decimalFormat.format(oPlayer != null ? (oPlayer instanceof Player oP ? oP.getHealth() : 00.00) : 1);
								}
								case "max_health" ->{
									OfflinePlayer oPlayer = opponent instanceof  BlockCombatUser ? null : opponent.getPlayer();
									return decimalFormat.format(oPlayer != null ? (oPlayer instanceof Player oP ? oP.getAttribute(Attribute.MAX_HEALTH) : 00.00) : 1);
								}
							}
						}
					}
					case "kills"->{
						return obfuscate(player, (""+handleStat(player, "kills", args.length>1 ? args[1].toLowerCase() : "total")), params);
					}
					case "deaths"->{
						return obfuscate(player, (""+handleStat(player, "deaths", args.length>1 ? args[1].toLowerCase() : "total")), params);
					}
					case "killstreak"->{
						return obfuscate(player, (""+handleStat(player, "killstreak", args.length>1 ? args[1].toLowerCase() : "total")), params);
					}
					case "deathstreak"->{
						return obfuscate(player, (""+handleStat(player, "deathstreak", args.length>1 ? args[1].toLowerCase() : "total")), params);
					}
					case "kdr"->{
						String type = args.length>1 ? args[1].toLowerCase() : "total";
						int kills = (handleStat(player, "kills", type));
						int deaths = (handleStat(player, "deaths", type));
						if (kills==0.0&&deaths==0.0){
							return obfuscate(player, "∞", params);
						}
						return obfuscate(player, ""+kills/deaths, params);
					}
				}
				return onPlaceholderRequest((Player) player, params);
			}
		}
		return null;
	}
	private int handleStat(OfflinePlayer player, String stat, String type) {
		UserManager userManager = fluffy.getUserManager();
		Account account = fluffy.getStatisticManager().get(player.getUniqueId());

		if (account == null){
			return 0;
		}
		switch (stat.toLowerCase()) {
			case "kills"-> {
				switch (type.toLowerCase()) {
					case "total" -> {
						return account.getStatistic(Statistics.KILLS_GLOBAL);
					}
					case "anchor"-> {
						return account.getStatistic(Statistics.KILLS_ANCHOR);
					}
					case "tnt"->{
						return account.getStatistic(Statistics.KILLS_TNT);
					}
					case "crystal"->{
						return account.getStatistic(Statistics.KILLS_CRYSTAL);
					}
					case "bed"->{
						return account.getStatistic(Statistics.KILLS_BED);
					}
					default -> {
						return 0;
					}
				}
			}
			case "deaths"->{
				switch (type.toLowerCase()){
					case "total" -> {
						return account.getStatistic(Statistics.DEATHS_GLOBAL);
					}
					case "anchor"-> {
						return account.getStatistic(Statistics.DEATHS_ANCHOR);
					}
					case "tnt"->{
						return account.getStatistic(Statistics.DEATHS_TNT);
					}
					case "crystal"->{
						return account.getStatistic(Statistics.DEATHS_CRYSTAL);
					}
					case "bed"->{
						return account.getStatistic(Statistics.DEATHS_BED);
					}
					default -> {
						return 0;
					}
				}
			}
			case "killstreak"->{
				return account.getStatistic(Statistics.STREAK_KILLS);
			}
			case "deathstreak"->{
				return account.getStatistic(Statistics.STREAK_DEATHS);
			}
			case "totem_activated", "totem_resurrections" ->{
				return 0;
			}
		}
 		return 0;
	}
	private @NotNull String obfuscate(CombatUser user, String value, String params){
		if (params.toLowerCase().contains(";hide_if_invis")){
			if (!(user instanceof BlockCombatUser)) {
				OfflinePlayer playerOther = user.getPlayer();
				if (playerOther instanceof Player oPlayer) {
					if (oPlayer.hasPotionEffect(PotionEffectType.INVISIBILITY)) {
						return "<OBFUSCATED>" + value;
					}
				}
			}
		}
		return value;
	}
	private @NotNull String obfuscate(OfflinePlayer user, String value, String params){
		if (user instanceof Player player) {
			if (params.toLowerCase().contains(";hide_if_invis") && params.toLowerCase().contains(";internal_opponent")) {
				if (player.hasPotionEffect(PotionEffectType.INVISIBILITY)) {
					return "<OBFUSCATED>" + value;
				}
			}
		}
		return value;
	}
	@Override
	public @Nullable String onPlaceholderRequest(Player player, @NotNull String params) {
		return super.onPlaceholderRequest(player, params);
	}


	@Override
	public void log(Level level, String msg) {
		fluffy.getLogger().log(level, msg);
	}

	@Override
	public void log(Level level, String msg, Throwable throwable) {
		fluffy.getLogger().log(level, msg, throwable);
	}

	@Override
	public void warning(String msg) {
		fluffy.getLogger().warning(msg);
	}

	@Override
	public void severe(String msg) {
		fluffy.getLogger().severe(msg);
	}

	@Override
	public void severe(String msg, Throwable throwable) {
		fluffy.getLogger().log(Level.SEVERE, msg, throwable);
	}

	@Override
	public @Nullable String getRequiredPlugin() {
		return "Fluffy";
	}

	@Override
	public boolean canRegister() {
		return true;
	}

	@Override
	public boolean persist(){
		return true;
	}
}