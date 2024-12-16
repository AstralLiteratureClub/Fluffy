package bet.astral.fluffy.statistic;

import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

import java.lang.reflect.Field;
import java.util.Collection;
import java.util.HashSet;

public final class Statistics {
	public static final Statistic KILLS_GLOBAL = Statistic.of("kills_global", true);
	public static final Statistic KILLS_ANCHOR = Statistic.of("kills_anchor", true);
	public static final Statistic KILLS_CRYSTAL= Statistic.of("kills_crystal", true);
	public static final Statistic KILLS_TNT = Statistic.of("kills_tnt", true);
	public static final Statistic KILLS_BED = Statistic.of("kills_bed", true);
	public static final Statistic KILLS_TOTEM = Statistic.of("kills_totem", true);

	public static final Statistic DEATHS_GLOBAL = Statistic.of("deaths_global", true);
	public static final Statistic DEATHS_ANCHOR = Statistic.of("deaths_anchor", true);
	public static final Statistic DEATHS_CRYSTAL= Statistic.of("deaths_crystal", true);
	public static final Statistic DEATHS_TNT = Statistic.of("deaths_tnt", true);
	public static final Statistic DEATHS_BED = Statistic.of("deaths_bed", true);
	public static final Statistic DEATHS_TOTEM = Statistic.of("deaths_totem", true);

	public static final Statistic STREAK_KILLS = Statistic.of("streak_kills");
	public static final Statistic STREAK_KILLS_HIGHEST = Statistic.of("streak_kills_highest", true);
	public static final Statistic STREAK_DEATHS = Statistic.of("streak_deaths");
	public static final Statistic STREAK_DEATHS_HIGHEST = Statistic.of("streak_deaths_highest", true);
	public static final Statistic STREAK_KILLS_TOTEM = Statistic.of("streak_kills_totem");
	public static final Statistic STREAK_DEATHS_TOTEM = Statistic.of("streak_deaths_totem");
	public static final Statistic STREAK_KILLS_TOTEM_HIGHEST = Statistic.of("streak_kills_totem_highest", true);
	public static final Statistic STREAK_DEATHS_TOTEM_HIGHEST = Statistic.of("streak_deaths_totem_highest", true);

	public static final Statistic COMBAT_LOGS = Statistic.of("combat_logs", true);
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