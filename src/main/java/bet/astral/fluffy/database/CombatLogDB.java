package bet.astral.fluffy.database;

import bet.astral.fluffy.FluffyCombat;
import bet.astral.fluffy.database.cache.CombatLog;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.sql.*;
import java.time.Instant;
import java.util.Date;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

public class CombatLogDB extends Connect{
    private Connection connection;
    public CombatLogDB(FluffyCombat fluffyCombat) {
        super(fluffyCombat);
    }

    @Override
    public Connection connect() {
        File file = new File(getFluffy().getDataFolder(), "combatlog.db");
        if (file.getParentFile().exists()) {
            file.getParentFile().mkdir();
        }

        try {
            return DriverManager.getConnection("jdbc:sqlite:" + file);
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }


    @Override
    public void onDisable() {
    }

    @Override
    public void onEnable() {
        try {
            PreparedStatement statement = getConnection().prepareStatement("CREATE TABLE IF NOT EXISTS combatlog (uniqueId VARCHAR(36), date BIGINT, wasKilled BOOL DEFAULT false, killer VARCHAR(36))");
            statement.execute();
            statement.close();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public CompletableFuture<@Nullable CombatLog> getLog(UUID uniqueId){
        CompletableFuture<CombatLog> result = CompletableFuture.supplyAsync(()->{
            try {
                PreparedStatement statement = getConnection().prepareStatement("SELECT * FROM combatlog WHERE uniqueId = ?");
                statement.setString(1, uniqueId.toString());
                ResultSet resultSet = statement.executeQuery();
                if (resultSet == null || !resultSet.next()) {
                    if (resultSet != null){
                        resultSet.close();
                    }
                    statement.close();

                    delete(uniqueId);
                    return null;
                }

                long date = resultSet.getLong("date");
                String killer = resultSet.getString("killer");
                UUID killerUUID = killer != null ? UUID.fromString(killer) : null;
                boolean killed = resultSet.getBoolean("wasKilled");

                CombatLog combatLog = new CombatLog(uniqueId, Date.from(Instant.ofEpochMilli(date)), killerUUID, killed);

                resultSet.close();
                statement.close();

                delete(uniqueId);
                return combatLog;
            } catch (SQLException e) {
                throw new RuntimeException(e);
            }
        });
        return result.exceptionally(exception("Encountered error while trying to get combat log of "+ uniqueId.toString()));
    }

    void delete(UUID uniqueId) throws SQLException {
        PreparedStatement statement = getConnection().prepareStatement("DELETE FROM combatlog WHERE uniqueId = ?");
        statement.setString(1, uniqueId.toString());
        statement.executeUpdate();

        statement.close();
    }

    public void save(UUID uniqueId){
        CompletableFuture.runAsync(()->{
            try {
                delete(uniqueId);

                PreparedStatement statement = getConnection().prepareStatement("INSERT INTO combatlog (uniqueId, date) VALUES (?, ?)");
                statement.setString(1, uniqueId.toString());
                statement.setLong(2, System.currentTimeMillis());
                statement.executeUpdate();
                statement.close();
            } catch (SQLException e) {
                throw new RuntimeException(e);
            }
        }).exceptionally(exception("Encountered an exception while trying to save "+uniqueId +" combat log!"));
    }
    public void update(UUID uniqueId, UUID killer){
        CompletableFuture.runAsync(()->{
            try {
                if (killer != null) {
                    PreparedStatement statement = getConnection().prepareStatement("UPDATE combatlog SET killer = ?, wasKilled = ? WHERE uniqueId = ?");
                    statement.setString(1, killer.toString());
                    statement.setBoolean(2, true);
                    statement.setString(3, uniqueId.toString());

                    statement.executeUpdate();
                    statement.close();
                } else {
                    PreparedStatement statement = getConnection().prepareStatement("UPDATE combatlog SET wasKilled = ? WHERE uniqueId = ?");
                    statement.setBoolean(1, true);
                    statement.setString(2, uniqueId.toString());

                    statement.executeUpdate();
                    statement.close();
                }
            } catch (SQLException e) {
                throw new RuntimeException(e);
            }
        }).exceptionally(exception("Encountered an exception while trying to save "+uniqueId +" combat log killer " + killer+"!"));
    }
}
