package bet.astral.fluffy.statistic;

import bet.astral.fluffy.events.player.PlayerNewHighStreakEvent;
import bet.astral.messenger.v2.placeholder.values.PlaceholderValue;
import net.kyori.adventure.text.Component;
import org.bukkit.OfflinePlayer;
import org.incendo.cloud.description.Description;
import org.jetbrains.annotations.NotNull;

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

	@NotNull
	static Statistic of(@NotNull String name){
		return new StatisticImpl(name, true);
	}
	@NotNull
	static Statistic of(@NotNull String name, boolean canOnlyIncrement){
		return new StatisticImpl(name, canOnlyIncrement);
	}
	@NotNull
	static Statistic of(@NotNull String name, @NotNull Description description){
		return new StatisticDescriptionImpl(name, true, description);
	}
	@NotNull
	static Statistic of(@NotNull String name, boolean canOnlyIncrement, @NotNull Description description){
		return new StatisticDescriptionImpl(name, canOnlyIncrement, description);
	}

	@Override
	default @NotNull Component getValue() {
		return Component.text(getName());
	}

	class StatisticImpl implements Statistic {
		@NotNull
		private final String name;
		private final boolean canBeReset;
		protected StatisticImpl(@NotNull String name, boolean canBeReset){
			this.name = name;
            this.canBeReset = canBeReset;
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

		protected StatisticDescriptionImpl(@NotNull String name, boolean canOnlyIncrement, Description description) {
			super(name, canOnlyIncrement);
			this.description = description;
		}

		@Override
		public Description getDescription() {
			return description;
		}
	}
}