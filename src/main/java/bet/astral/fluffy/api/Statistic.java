package bet.astral.fluffy.api;

import net.kyori.adventure.translation.Translatable;
import org.bukkit.Keyed;
import org.bukkit.NamespacedKey;
import org.jetbrains.annotations.NotNull;

public interface Statistic extends Translatable, Keyed {
	enum Kills implements Statistic {
		ALL,
		ENDER_CRYSTAL,
		RESPAWN_ANCHOR,
		BED,
		TNT,
		VOID,
		TOTEM_OF_UNDYING;
		private final NamespacedKey key;
		private final String translationKey;

		Kills() {
			this.key = new NamespacedKey("fluffy", name().toLowerCase());
			this.translationKey = ("statistic.kill." + name().toLowerCase()).replace("_", "-");
		}

		@Override
		public @NotNull NamespacedKey getKey() {
			return key;
		}

		@Override
		public @NotNull String translationKey() {
			return translationKey;
		}
	}

	enum Deaths implements Statistic {
		ALL,
		ENDER_CRYSTAL,
		RESPAWN_ANCHOR,
		BED,
		TNT,
		VOID,
		TOTEM_OF_UNDYING;
		private final NamespacedKey key;
		private final String translationKey;

		Deaths() {
			this.key = new NamespacedKey("fluffy", name().toLowerCase());
			this.translationKey = ("statistic.streak." + name().toLowerCase()).replace("_", "-");
		}

		@Override
		public @NotNull NamespacedKey getKey() {
			return key;
		}

		@Override
		public @NotNull String translationKey() {
			return translationKey;
		}
	}

	enum Streak implements Statistic {
		KILLS,
		DEATHS,
		TOTEM_OF_UNDYING_KILLS,
		TOTEM_OF_UNDYING_DEATHS,
		;
		private final NamespacedKey key;
		private final String translationKey;
		Streak() {
			this.key = new NamespacedKey("fluffy", name().toLowerCase());
			this.translationKey = ("statistic.streak." + name().toLowerCase()).replace("_", "-");
		}

		@Override
		public @NotNull NamespacedKey getKey() {
			return key;
		}

		@Override
		public @NotNull String translationKey() {
			return translationKey;
		}
	}
}
