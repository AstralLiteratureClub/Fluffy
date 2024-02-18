package bet.astral.fluffy.database;

import bet.astral.fluffy.FluffyCombat;
import bet.astral.fluffy.api.CombatUser;
import org.bukkit.configuration.file.FileConfiguration;

import java.lang.reflect.Field;
import java.sql.*;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

public class StatisticDatabase {
	private final FluffyCombat fluffyCombat;
	private final Connection connection;
	private final String table;

	public StatisticDatabase(FluffyCombat fluffyCombat) {
		this.fluffyCombat = fluffyCombat;
		FileConfiguration config = fluffyCombat.getConfig();
		this.table = config.getString("database.stats.table", "stats.table");
		fluffyCombat.getLogger().severe("DATABASE: " + config.getString("database.stats.table"));
		try {
			String url = config.getString("database.stats.url", "stats.url");
			String user = config.getString("database.stats.user", "stats.user");
			String password = config.getString("database.stats.password", "stats.password");
			this.connection = DriverManager.getConnection(url, user, password);
		} catch (SQLException e) {
			throw new RuntimeException("Failed to initialize the database connection", e);
		}
	}

	public void updateDatabase(CombatUser obj) {
		try (Connection connection = this.connection) {
			createTableIfNotExists(connection, obj.getClass());

			for (Field field : obj.getClass().getDeclaredFields()) {
				if (field.isAnnotationPresent(DatabaseField.class)) {
					field.setAccessible(true);
					Object value = field.get(obj);

					String sql = "UPDATE " + table + " SET " + field.getName() + " = ? WHERE unique_id = ?";
					try (PreparedStatement statement = connection.prepareStatement(sql)) {
						statement.setObject(1, value);
						statement.setObject(2, obj.getUniqueId());
						statement.executeUpdate();
					} catch (SQLException e) {
						e.printStackTrace(); // Handle the exception appropriately
					}
				}
			}
		} catch (SQLException | IllegalAccessException e) {
			e.printStackTrace(); // Handle the exception appropriately
		}
	}

	private void createTableIfNotExists(Connection connection, Class<?> clazz) throws SQLException {
		if (!tableExists(connection, clazz.getSimpleName())) {
			StringBuilder createTableSql = new StringBuilder("CREATE TABLE " + table + " (");
			for (Field field : clazz.getDeclaredFields()) {
				if (field.isAnnotationPresent(DatabaseField.class)) {
					createTableSql.append(field.getName()).append(" ").append(getSqlType(field)).append(", ");
				}
			}
			createTableSql.append("PRIMARY KEY (unique_id));");

			try (Statement statement = connection.createStatement()) {
				statement.executeUpdate(createTableSql.toString());
			}
		}
	}

	public void updateTableStructure(Class<?> clazz) {
		try (Connection connection = this.connection) {
			createTableIfNotExists(connection, clazz);
			alterTable(connection, clazz);
		} catch (SQLException e) {
			e.printStackTrace(); // Handle the exception appropriately
		}
	}

	private void alterTable(Connection connection, Class<?> clazz) throws SQLException {
		// Get existing columns from the table
		Set<String> existingColumns = getExistingColumns(connection);

		// Get declared fields from the class
		Set<String> declaredFields = Arrays.stream(clazz.getDeclaredFields())
				.filter(field -> field.isAnnotationPresent(DatabaseField.class))
				.map(Field::getName)
				.collect(Collectors.toSet());

		// Find new fields to add
		Set<String> newFields = new HashSet<>(declaredFields);
		newFields.removeAll(existingColumns);

		// Add new fields to the table
		if (!newFields.isEmpty()) {
			StringBuilder alterTableSql = new StringBuilder("ALTER TABLE " + table + " ");
			for (String newField : newFields) {
				Field field = Arrays.stream(clazz.getDeclaredFields())
						.filter(f -> f.getName().equals(newField))
						.findFirst()
						.orElseThrow(IllegalStateException::new);

				alterTableSql.append("ADD COLUMN ").append(newField).append(" ").append(getSqlType(field)).append(", ");
			}
			alterTableSql.deleteCharAt(alterTableSql.length() - 2); // Remove trailing comma and space

			try (Statement statement = connection.createStatement()) {
				statement.executeUpdate(alterTableSql.toString());
			}
		}
	}

	private Set<String> getExistingColumns(Connection connection) throws SQLException {
		Set<String> existingColumns = new HashSet<>();
		DatabaseMetaData metaData = connection.getMetaData();
		try (ResultSet resultSet = metaData.getColumns(null, null, table, null)) {
			while (resultSet.next()) {
				existingColumns.add(resultSet.getString("COLUMN_NAME"));
			}
		}
		return existingColumns;
	}

	private boolean tableExists(Connection connection, String tableName) throws SQLException {
		try (ResultSet resultSet = connection.getMetaData().getTables(null, null, tableName, null)) {
			return resultSet.next();
		}
	}

	public CombatUser loadFromDatabase(UUID uniqueId) {
		CombatUser user = null;
		try (Connection connection = this.connection) {
			String sql = "SELECT * FROM " + table + " WHERE unique_id = ?";
			try (PreparedStatement statement = connection.prepareStatement(sql)) {
				statement.setObject(1, uniqueId);
				try (ResultSet resultSet = statement.executeQuery()) {
					if (resultSet.next()) {
						user = new CombatUser(fluffyCombat, uniqueId);
						for (Field field : CombatUser.class.getDeclaredFields()) {
							if (field.isAnnotationPresent(DatabaseField.class)) {
								DatabaseField databaseField = field.getAnnotation(DatabaseField.class);
								field.setAccessible(true);
								Object value = resultSet.getObject(field.getName());
								if (value == null) {
									field.set(user, databaseField.defaultValue());
								}
								field.set(user, value);
							}
						}
					}
				}
			}
		} catch (SQLException | IllegalAccessException e) {
			e.printStackTrace();
		}
		return user;
	}

	private String getSqlType(Field field) {
		if (field.getType() == int.class || field.getType() == Integer.class) {
			return "INT";
		} else if (field.getType() == String.class) {
			return "VARCHAR(255)";
		} else {
			return "UNKNOWN";
		}
	}

	public void disable() {
		try {
			if (connection != null && connection.isClosed()){
				connection.close();
			}
		} catch (SQLException e) {
			throw new RuntimeException(e);
		}
	}
}