package bet.astral.fluffy.database;

import bet.astral.fluffy.FluffyCombat;
import bet.astral.fluffy.api.StatisticUser;
import org.bukkit.configuration.Configuration;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.CompletableFuture;

public abstract class AbstractDatabase {
	protected final FluffyCombat fluffy;
	protected final DBType databaseType;


	public String getTableName(String type, Configuration configuration) throws IllegalStateException{
		String table = configuration.getString(type);
		if (table == null){
			throw new IllegalStateException("Could not find database type for " + type);
		}
		return table;
	}

	protected AbstractDatabase(FluffyCombat fluffyCombat, DBType databaseType) {
		this.fluffy = fluffyCombat;
		this.databaseType = databaseType;
	}

	public abstract void save(StatisticUser user);
	public abstract void saveASync(StatisticUser user);

	public abstract void load(StatisticUser user);
	public abstract CompletableFuture<StatisticUser> loadASync(StatisticUser user);

	public boolean isNew = false;


	public enum DBType {
		SQLITE("url") {
			@Override
			public Map<String, String> getRequired(Configuration configuration, String prefix) throws IllegalStateException {
				Map<String, String> required = super.getRequired(configuration, prefix);
				String url = required.get("url");
				required.put("url", url.replace("./", FluffyCombat.getPlugin(FluffyCombat.class).getDataFolder().toString()));

				return required;
			}
		},

		;

		private final String[] requiredStrings;

		DBType(String... requiredStrings) {
			this.requiredStrings = requiredStrings;
		}

		public Map<String, String> getRequired(Configuration configuration, String prefix) throws IllegalStateException{
			Map<String, String> map = new HashMap<>();
			for (String req : requiredStrings){
				if (prefix!=null && !prefix.contentEquals("")){
					req = prefix+"."+req;
				}
				String object = configuration.getString(req);
				if (object == null){
					throw new RuntimeException("Could not find required field " + req+" in the database configuration.");
				}
				map.put(req, object);
			}
			return map;
		}

		public Connection helpConnect(Configuration configuration, String prefix) throws SQLException {
			Properties properties = new Properties();
			Map<String, String> values = getRequired(configuration, prefix);
			for (String field : requiredStrings){
				properties.setProperty(field.toLowerCase(), values.get(field));
			}

			return DriverManager.getConnection(values.get("url"), properties);
		}
	}

}
