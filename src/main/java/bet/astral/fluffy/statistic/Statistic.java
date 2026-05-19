package bet.astral.fluffy.statistic;

import bet.astral.fluffy.events.player.PlayerNewHighStreakEvent;
import bet.astral.messenger.v2.placeholder.values.PlaceholderValue;
import net.kyori.adventure.text.Component;
import org.bukkit.OfflinePlayer;
import org.incendo.cloud.description.Description;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Objects;

public interface Statistic extends PlaceholderValue {
	static void incrementStreak(OfflinePlayer player, Account account, Statistic streak, Statistic highestStreak){
		account.increment(streak);
		if (account.getStatistic(streak) > account.getStatistic(highestStreak)) {
			account.increment(highestStreak);
			PlayerNewHighStreakEvent event = new PlayerNewHighStreakEvent(player, account, streak, highestStreak);
			event.callEvent();
		}
	}
	@NotNull
	String getName();
	boolean canOnlyIncrement();
	@Nullable
	StatisticType getType();

	@NotNull
	static Statistic of(@NotNull String name){
		return new StatisticImpl(name, true, null);
	}
	@NotNull
	static Statistic of(@NotNull String name, StatisticType type){
		return new StatisticImpl(name, true, type);
	}
	@NotNull
	static Statistic of(@NotNull String name, boolean canOnlyIncrement){
		return new StatisticImpl(name, canOnlyIncrement, null);
	}
	@NotNull
	static Statistic of(@NotNull String name, StatisticType type, boolean canOnlyIncrement){
		return new StatisticImpl(name, canOnlyIncrement, type);
	}
	@NotNull
	static Statistic of(@NotNull String name, @NotNull Description description){
		return new StatisticDescriptionImpl(name, true, null, description);
	}
	@NotNull
	static Statistic of(@NotNull String name, StatisticType statisticType, @NotNull Description description){
		return new StatisticDescriptionImpl(name, true, statisticType, description);
	}
	@NotNull
	static Statistic of(@NotNull String name, boolean canOnlyIncrement, @NotNull Description description){
		return new StatisticDescriptionImpl(name, canOnlyIncrement, null, description);
	}
	@NotNull
	static Statistic of(@NotNull String name, boolean canOnlyIncrement, StatisticType type, @NotNull Description description){
		return new StatisticDescriptionImpl(name, canOnlyIncrement, type, description);
	}

	@Override
	default @NotNull Component getValue() {
		return Component.text(getName());
	}

	class StatisticImpl implements Statistic {
		@NotNull
		private final String name;
		private final boolean canBeReset;
		private final StatisticType statisticType;
		protected StatisticImpl(@NotNull String name, boolean canBeReset, StatisticType statisticType){
			this.name = name;
            this.canBeReset = canBeReset;
            this.statisticType = statisticType;
        }

		@Override
		public @NotNull String getName() {
			return name;
		}

		@Override
		public boolean canOnlyIncrement() {
			return false;
		}

		@Override
		public int hashCode() {
			return Objects.hashCode(name) * 11;
		}

		@Override
		public boolean equals(Object obj) {
			return (obj instanceof Statistic && ((Statistic) obj).getName().contentEquals(getName()));
		}
	}
	class StatisticDescriptionImpl extends StatisticImpl implements StatisticDescription {
		private final Description description;

		protected StatisticDescriptionImpl(@NotNull String name, boolean canOnlyIncrement, StatisticType statisticType, Description description) {
			super(name, canOnlyIncrement, statisticType);
			this.description = description;
		}

		@Override
		public Description getDescription() {
			return description;
		}
	}
}