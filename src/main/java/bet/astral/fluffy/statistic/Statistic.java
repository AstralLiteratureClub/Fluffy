package bet.astral.fluffy.statistic;

import bet.astral.messenger.placeholder.PlaceholderValue;
import org.incendo.cloud.description.Description;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;

public interface Statistic extends PlaceholderValue {
	@NotNull
	String getName();

	@NotNull
	static Statistic of(@NotNull String name){
		return new StatisticImpl(name);
	}
	@NotNull
	static Statistic of(@NotNull String name, @NotNull Description description){
		return new StatisticDescriptionImpl(name, description);
	}

	@Override
	default @NotNull String getValue() {
		return getName();
	}

	class StatisticImpl implements Statistic {
		@NotNull
		private final String name;
		protected StatisticImpl(@NotNull String name){
			this.name = name;
		}

		@Override
		public @NotNull String getName() {
			return name;
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

		protected StatisticDescriptionImpl(@NotNull String name, Description description) {
			super(name);
			this.description = description;
		}

		@Override
		public Description getDescription() {
			return description;
		}
	}
}