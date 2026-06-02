package bet.astral.fluffy.statistic;

import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

import java.lang.reflect.Field;
import java.util.Collection;
import java.util.HashSet;

public final class Statistics {
	public static final Statistic KILLS_GLOBAL = Statistic.of("kills_global", StatisticType.KILLS, true, true);
	public static final Statistic KILLS_ANCHOR = Statistic.of("kills_anchor", StatisticType.KILLS, true, true);
	public static final Statistic KILLS_CRYSTAL= Statistic.of("kills_crystal", StatisticType.KILLS, true, true);
	public static final Statistic KILLS_TNT = Statistic.of("kills_tnt", StatisticType.KILLS, true, true);
	public static final Statistic KILLS_BED = Statistic.of("kills_bed", StatisticType.KILLS, true, true);
	public static final Statistic KILLS_TOTEM = Statistic.of("kills_totem", StatisticType.KILLS, true, true);

	public static final Statistic DEATHS_GLOBAL = Statistic.of("deaths_global", StatisticType.DEATHS, true, true);
	public static final Statistic DEATHS_ANCHOR = Statistic.of("deaths_anchor", StatisticType.DEATHS, true, true);
	public static final Statistic DEATHS_CRYSTAL= Statistic.of("deaths_crystal", StatisticType.DEATHS, true, true);
	public static final Statistic DEATHS_TNT = Statistic.of("deaths_tnt", StatisticType.DEATHS, true, true);
	public static final Statistic DEATHS_BED = Statistic.of("deaths_bed", StatisticType.DEATHS, true, true);
	public static final Statistic DEATHS_TOTEM = Statistic.of("deaths_totem", StatisticType.DEATHS, true, true);

	public static final Statistic STREAK_KILLS = Statistic.of("streak_kills", StatisticType.KILL_STREAKS, true, true);
	public static final Statistic STREAK_KILLS_HIGHEST = Statistic.of("streak_kills_highest", StatisticType.KILL_STREAKS, false, true);
	public static final Statistic STREAK_DEATHS = Statistic.of("streak_deaths", StatisticType.DEATH_STREAKS, true, true);
	public static final Statistic STREAK_DEATHS_HIGHEST = Statistic.of("streak_deaths_highest", StatisticType.DEATH_STREAKS, false, true);
	public static final Statistic STREAK_KILLS_TOTEM = Statistic.of("streak_kills_totem", StatisticType.KILL_STREAKS, true, true);
	public static final Statistic STREAK_DEATHS_TOTEM = Statistic.of("streak_deaths_totem", StatisticType.DEATHS, true, true);
	public static final Statistic STREAK_KILLS_TOTEM_HIGHEST = Statistic.of("streak_kills_totem_highest", StatisticType.KILL_STREAKS, false, true);
	public static final Statistic STREAK_DEATHS_TOTEM_HIGHEST = Statistic.of("streak_deaths_totem_highest", StatisticType.DEATH_STREAKS, false, true);

	public static final Statistic STREAK_DEATHS_GLOBAL = Statistic.of("streak_deaths_global", true, true);
	public static final Statistic STREAK_KILLS_GLOBAL = Statistic.of("streak_kills_global", true, true);
	public static final Statistic STREAK_DEATHS_ANCHOR = Statistic.of("streak_deaths_anchor", true, true);
	public static final Statistic STREAK_KILLS_ANCHOR = Statistic.of("streak_kills_anchor", true, true);
	public static final Statistic STREAK_DEATHS_CRYSTAL= Statistic.of("streak_deaths_crystal", true, true);
	public static final Statistic STREAK_KILLS_CRYSTAL= Statistic.of("streak_kills_crystal", true, true);
	public static final Statistic STREAK_DEATHS_TNT = Statistic.of("streak_deaths_tnt", true, true);
	public static final Statistic STREAK_KILLS_TNT = Statistic.of("streak_kills_tnt", true, true);
	public static final Statistic STREAK_DEATHS_BED = Statistic.of("streak_deaths_bed", true, true);
	public static final Statistic STREAK_KILLS_BED = Statistic.of("streak_kills_bed", true, true);

	public static final Statistic COMBAT_LOGS = Statistic.of("combat_logs", true, true);
	public static final Statistic STREAK_COMBAT_LOGS = Statistic.of("streak_combat_logs");


	private static final Statistic[] statistics;
	static {
		Collection<Statistic> stats = new HashSet<>();
		for (Field field : Statistics.class.getFields()){
			try {
				Object object = field.get(null);
				stats.add((Statistic) object);
			} catch (IllegalAccessException e) {
				throw new RuntimeException(e);
			}
		}
		statistics = stats.toArray(Statistic[]::new);
	}

	@Contract(pure = true)
	@NotNull
	public static Statistic[] values(){
		return statistics;
	}

	public static Statistic valueOf(String key) {
		for (Statistic statistic : statistics){
			if (statistic.getName().equalsIgnoreCase(key)){
				return statistic;
			}
		}
		return null;
	}
}