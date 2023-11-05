package me.antritus.astral.fluffycombat.antsfactions;

import me.antritus.astral.fluffycombat.astrolminiapi.Configuration;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * @author Antritus
 * @since 1.1-SNAPSHOT
 */
@SuppressWarnings("removal")
@Deprecated(forRemoval = true)
public interface ISettings {
	@NotNull
	String name();
	@Nullable
	Property<?, ?> get(String name);
	Configuration getConfiguration();
	void save();
	default void reload(){
		try {
			getConfiguration().load();
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}
}
