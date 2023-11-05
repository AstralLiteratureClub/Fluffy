package me.antritus.astral.fluffycombat.antsfactions;

import me.antritus.astral.fluffycombat.astrolminiapi.Configuration;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;

/**
 * @author Antritus
 * @since 1.1-SNAPSHOT
 */

@SuppressWarnings("removal")
@Deprecated(forRemoval = true)
public class CoreSettings implements ISettings {
	private final HashMap<String, SimpleProperty<?>> properties = new HashMap<>();
	private final Configuration config;
	private final FactionsPlugin main;

	public CoreSettings(FactionsPlugin main) {
		this.main = main;
		this.config = main.getConfig();
	}

	public FactionsPlugin getMain() {
		return main;
	}

	@Override
	public @NotNull String name() {
		return "settings";
	}

	@Override
	@Nullable
	public Property<String, ?> get(String name) {
		return properties.get(name);
	}

	@Override
	public Configuration getConfiguration() {
		return config;
	}

	@Override
	public void save() {
		properties.forEach((key,property)->{
			config.set(key, property.getValue());
		});
	}
	public void load(SimpleProperty<?> property){
		properties.put(property.getName(), property);
	}
	@NotNull
	public Property<String, ?> getKnownNonNull(String s) {
		return properties.get(s);
	}
}
