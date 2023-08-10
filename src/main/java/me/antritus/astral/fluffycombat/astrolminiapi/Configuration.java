package me.antritus.astral.fluffycombat.astrolminiapi;

import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.*;
import java.util.Objects;

/**
 * @since 1.0.0-snapshot
 * @author antritus
 */
public class Configuration extends YamlConfiguration {

	private final String filename;
	private final File file;
	private final JavaPlugin javaPlugin;
	/**
	 * Creates a new config with the given filename.
	 */
	public Configuration(JavaPlugin pl, String file) {
		this.javaPlugin = pl;
		this.filename = file;
		this.file = new File(pl.getDataFolder(), file);
		loadDefaults();
		reload();
	}

	private void loadDefaults() {
		final YamlConfiguration defaultConfig = new YamlConfiguration();
		try (final InputStream inputStream = javaPlugin.getResource(filename)) {
			if (inputStream != null) {
				try (final Reader reader = new InputStreamReader(Objects.requireNonNull(inputStream))) {
					System.out.println("Reader");
					defaultConfig.load(reader);
				} catch (InvalidConfigurationException e) {
					throw new RuntimeException(e);
				}
			}
		} catch (final IOException exception) {
			throw new IllegalArgumentException("Could not load included config file " + filename, exception);
		}

		setDefaults(defaultConfig);
	}

	/**
	 * Reloads the configuration
	 */
	public void reload() {
		saveDefaultConfig();
		try {
			load(file);
		} catch (final IOException exception) {
			new IllegalArgumentException("Could not find or load file " + filename, exception).printStackTrace();
		} catch (final InvalidConfigurationException exception) {
			javaPlugin.getLogger().severe("Your config file " + filename + " is invalid, using default values now. Please fix the below mentioned errors and try again:");
			exception.printStackTrace();
		}
	}

	private void saveDefaultConfig() {
		if (!file.exists()) {
			File parent = file.getParentFile();
			if (parent != null && !parent.exists() && !parent.mkdirs()) {
				throw new UncheckedIOException(new IOException("Could not create directory " + parent.getAbsolutePath()));

			}
			javaPlugin.saveResource(filename, false);
		}
	}

	/**
	 * Saves the configuration under its original file name
	 *
	 * @throws IOException if the underlying YamlConfiguration throws it
	 */
	public void save() throws IOException {
		this.save(file);
	}

	public void load() throws IOException, InvalidConfigurationException {
		reload();
	}

	public void setIfNull(String key, Object obj){
		if (get(key) == null){
			set(key, obj);
		}
	}

}