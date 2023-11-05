package me.antritus.astral.fluffycombat.antsfactions;

import me.antritus.astral.fluffycombat.astrolminiapi.Configuration;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;

/**
 * @author Antritus
 * @since 1.1-SNAPSHOT
 */
@SuppressWarnings("removal")
@Deprecated(forRemoval = true)
public abstract class FactionsPlugin extends JavaPlugin {
	private CoreSettings coreSettings;
	private final MessageManager messageManager;
	private Configuration config;
	private final CoreDatabase coreDatabase;

	protected FactionsPlugin() {
		coreDatabase = new CoreDatabase(this);
		messageManager = new MessageManager(this);
	}

	@Override
	public void onEnable(){
		config = new Configuration(this, "config.yml");
		config.reload();
		config.setIfNull("database.type", "mysql");
		config.setIfNull("database.password", "_123_!_Bad_Password_!_123_");
		config.setIfNull("database.user", "root");
		config.setIfNull("database.url", "https://database.example.com/db/wormhole");
		config.setIfNull("version", "version");
		try {
			config.save();
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
		coreSettings = new CoreSettings(this);
		coreSettings.load(new SimpleProperty<>("database-type", getConfig().getString("database.type", "database.type")));
		coreSettings.load(new SimpleProperty<>("database-password", getConfig().getString("database.password", "database.password")));
		coreSettings.load(new SimpleProperty<>("database-user", getConfig().getString("database.user", "database.user")));
		coreSettings.load(new SimpleProperty<>("database-url", getConfig().getString("database.url", "database.url")));
		coreSettings.load(new SimpleProperty<>("version", getConfig().getString("version", "version")));
		SimpleProperty<?> version = (SimpleProperty<?>) getCoreSettings().getKnownNonNull("version");
		if (version.getValue().getClass().isInstance("version")){
			String ver = (String) version.getValue();
			//noinspection UnstableApiUsage
			if (!ver.equalsIgnoreCase(getPluginMeta().getVersion())){
				//noinspection UnstableApiUsage
				updateConfig(ver.equalsIgnoreCase("version") ? null : ver, getPluginMeta().getVersion());
				//noinspection DataFlowIssue,UnstableApiUsage
				coreSettings.get("version").setValueObj(getPluginMeta().getVersion());
				// noinspection UnstableApiUsage
				config.set("version", getPluginMeta().getVersion());
				try {
					config.save();
				} catch (IOException e) {
					throw new RuntimeException(e);
				}
			}
		}
		enable();
	}
	@Override
	public void onDisable(){
		startDisable();
		coreDatabase.closeConnection();
		disable();
	}

	public void enableDatabase(){
		coreDatabase.connect();
	}

	public abstract void updateConfig(@Nullable String oldVersion, String newVersion);

	public abstract void enable();
	public abstract void startDisable();
	public abstract void disable();

	public CoreSettings getCoreSettings() {
		return coreSettings;
	}

	public MessageManager getMessageManager() {
		return messageManager;
	}

	@NotNull
	@Override
	public Configuration getConfig() {
		return config;
	}

	public CoreDatabase getCoreDatabase() {
		return coreDatabase;
	}

	public void reloadConfig() {
		if (config == null) {
			config = new Configuration(this, "config.yml");
			config.reload();
			return;
		}
		config.reload();
	}

	@Override
	public void saveDefaultConfig() {
		config.reload();
	}
}
