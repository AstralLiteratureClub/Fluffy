package me.antritus.astral.fluffycombat.antsfactions;

/**
 * @author Antritus
 * @since 1.1-SNAPSHOT
 */
@SuppressWarnings("removal")
@Deprecated(forRemoval = true)
public class SimpleProperty<T> extends Property<String, T>{
	public SimpleProperty(String key, T value) {
		super(key, value);
	}
}
