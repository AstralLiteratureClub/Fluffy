package bet.astral.fluffy.statistic;

import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

import java.lang.reflect.Field;
import java.util.Collection;
import java.util.HashSet;

public final class Statistics {
	public static final Statistic KILLS_GLOBAL = Statistic.of("kills.global");
	public static final Statistic KILLS_ANCHOR = Statistic.of("kills.anchor");
	public static final Statistic KILLS_CRYSTAL= Statistic.of("kills.crystal");
	public static final Statistic KILLS_TNT = Statistic.of("kills.tnt");
	public static final Statistic KILLS_BED = Statistic.of("kills.bed");

	public static final Statistic DEATHS_GLOBAL = Statistic.of("deaths.global");
	public static final Statistic DEATHS_ANCHOR = Statistic.of("deaths.anchor");
	public static final Statistic DEATHS_CRYSTAL= Statistic.of("deaths.crystal");
	public static final Statistic DEATHS_TNT = Statistic.of("deaths.tnt");
	public static final Statistic DEATHS_BED = Statistic.of("deaths.bed");

	public static final Statistic STREAK_KILLS = Statistic.of("streaks.kills");
	public static final Statistic STREAK_DEATHS = Statistic.of("streaks.deaths");

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
			if (statistic.getName().equals(key)){
				return statistic;
			}
		}
		return null;
	}
}