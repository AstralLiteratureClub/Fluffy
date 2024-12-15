package bet.astral.fluffy.database;

import bet.astral.data.helper.PackedPreparedStatement;
import bet.astral.data.helper.PackedResultSet;
import bet.astral.fluffy.FluffyCombat;
import bet.astral.fluffy.statistic.Account;
import bet.astral.fluffy.statistic.AccountImpl;
import bet.astral.fluffy.statistic.Statistics;

import java.io.File;
import java.sql.*;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

public class StatisticsDatabase extends Connect{

    public StatisticsDatabase(FluffyCombat fluffyCombat) {
        super(fluffyCombat);
    }

    @Override
    public void onDisable() {
    }

    @Override
    public Connection connect() {
        File file = new File(getFluffy().getDataFolder(), "users.db");
        if (file.getParentFile().exists()) {
            file.getParentFile().mkdir();
        }

        try {
            return DriverManager.getConnection("jdbc:sqlite:" + file.getName());
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }
    @Override
    public void onEnable() {
        try {
            PreparedStatement statement = getConnection().prepareStatement("CREATE TABLE IF NOT EXISTS stats (uniqueId VARCHAR(36), " +
                    "kills INT, killsAnchor INT, killsCrystal INT, killsTNT INT, killsBed INT, deaths INT, deathsAnchor INT, deathsCrystal INT, deathsTNT INT, deathsBed INT, killstreak INT, highestKillstreak INT, deathstreak INT, highestDeathstreak INT)");
            statement.execute();
            statement.close();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }
    public CompletableFuture<Void> save(Account account){
        return CompletableFuture.runAsync(()->{
            try {
                PackedPreparedStatement getStatement = new PackedPreparedStatement(
                        getConnection().prepareStatement("SELECT * FROM stats WHERE uniqueId = ?"));
                getStatement.setUUID(1, account.getId());
                ResultSet resultSet = getStatement.executeQuery();
                if (resultSet != null && resultSet.next()){
                    PackedPreparedStatement updateStatement = new PackedPreparedStatement(
                            getConnection().prepareStatement("UPDATE stats SET kills = ?, killsAnchor = ?, killsCrystal = ?, killsTNT = ?," +
                                    "killsBed = ?, deaths = ?, deathsAnchor = ?, deathsCrystal = ?, deathsTNT = ?, deathsBed = ?, " +
                                    "killstreak = ?, highestKillstreak = ?, deathstreak = ?, highestDeathstreak = ? WHERE uniqueId = ?")
                    );
                    updateStatement.setInt(1, account.getStatistic(Statistics.KILLS_GLOBAL));
                    updateStatement.setInt(2, account.getStatistic(Statistics.KILLS_ANCHOR));
                    updateStatement.setInt(3, account.getStatistic(Statistics.KILLS_CRYSTAL));
                    updateStatement.setInt(4, account.getStatistic(Statistics.KILLS_TNT));
                    updateStatement.setInt(5, account.getStatistic(Statistics.KILLS_BED));
                    updateStatement.setInt(6, account.getStatistic(Statistics.DEATHS_GLOBAL));
                    updateStatement.setInt(7, account.getStatistic(Statistics.DEATHS_ANCHOR));
                    updateStatement.setInt(8, account.getStatistic(Statistics.DEATHS_CRYSTAL));
                    updateStatement.setInt(9, account.getStatistic(Statistics.DEATHS_TNT));
                    updateStatement.setInt(10, account.getStatistic(Statistics.DEATHS_BED));
                    updateStatement.setInt(11, account.getStatistic(Statistics.STREAK_KILLS));
                    updateStatement.setInt(12, account.getStatistic(Statistics.STREAK_KILLS_HIGHEST));
                    updateStatement.setInt(13, account.getStatistic(Statistics.STREAK_DEATHS));
                    updateStatement.setInt(14, account.getStatistic(Statistics.STREAK_DEATHS_HIGHEST));
                    updateStatement.setUUID(15, account.getId());
                    updateStatement.executeUpdate();
                    updateStatement.close();
                } else {
                    PackedPreparedStatement insertStatement = new PackedPreparedStatement(
                            getConnection().prepareStatement("INSERT INTO stats (kills, killsAnchor, killsCrystal, killsTNT, killsBed," +
                                    "deaths, deathsAnchor, deathsCrystal, deathsTNT, deathsBed, killstreak, highestKillstreak," +
                                    "deathstreak, highestDeathstreak) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)")
                    );
                    insertStatement.setUUID(1, account.getId());
                    insertStatement.setInt(2, account.getStatistic(Statistics.KILLS_GLOBAL));
                    insertStatement.setInt(3, account.getStatistic(Statistics.KILLS_ANCHOR));
                    insertStatement.setInt(4, account.getStatistic(Statistics.KILLS_CRYSTAL));
                    insertStatement.setInt(5, account.getStatistic(Statistics.KILLS_TNT));
                    insertStatement.setInt(6, account.getStatistic(Statistics.KILLS_BED));
                    insertStatement.setInt(7, account.getStatistic(Statistics.DEATHS_GLOBAL));
                    insertStatement.setInt(8, account.getStatistic(Statistics.DEATHS_ANCHOR));
                    insertStatement.setInt(9, account.getStatistic(Statistics.DEATHS_CRYSTAL));
                    insertStatement.setInt(10, account.getStatistic(Statistics.DEATHS_TNT));
                    insertStatement.setInt(11, account.getStatistic(Statistics.DEATHS_BED));
                    insertStatement.setInt(12, account.getStatistic(Statistics.STREAK_KILLS));
                    insertStatement.setInt(13, account.getStatistic(Statistics.STREAK_KILLS_HIGHEST));
                    insertStatement.setInt(14, account.getStatistic(Statistics.STREAK_DEATHS));
                    insertStatement.setInt(15, account.getStatistic(Statistics.STREAK_DEATHS_HIGHEST));
                    insertStatement.executeUpdate();
                    insertStatement.close();
                }

                if (resultSet != null && !resultSet.isClosed()){
                    resultSet.close();
                }
                getStatement.close();
            } catch (SQLException e) {
                throw new RuntimeException(e);
            }
        }).exceptionally(exception("Encountered error while trying to save user of "+ account.getId().toString()));
    }
    public CompletableFuture<Account> load(UUID uniqueId){
        CompletableFuture<Account> future = CompletableFuture.supplyAsync(()->{
            try {
                PackedPreparedStatement getStatement = new PackedPreparedStatement(getConnection()
                        .prepareStatement("SELECT * FROM stats WHERE uniqueId = ?"));
                getStatement.setUUID(1, uniqueId);
                ResultSet resultSet = getStatement.executeQuery();
                PackedResultSet packedSet = new PackedResultSet(resultSet);

                Account account = new AccountImpl(getFluffy(), uniqueId);
                if (resultSet != null && resultSet.next()){
                    account.set(Statistics.KILLS_GLOBAL, packedSet.getInt("kills"));
                    account.set(Statistics.KILLS_ANCHOR, packedSet.getInt("killsAnchor"));
                    account.set(Statistics.KILLS_CRYSTAL, packedSet.getInt("killsCrystal"));
                    account.set(Statistics.KILLS_TNT, packedSet.getInt("killsTNT"));
                    account.set(Statistics.KILLS_BED, packedSet.getInt("killsBed"));
                    account.set(Statistics.DEATHS_GLOBAL, packedSet.getInt("deaths"));
                    account.set(Statistics.DEATHS_ANCHOR, packedSet.getInt("deathsAnchor"));
                    account.set(Statistics.DEATHS_CRYSTAL, packedSet.getInt("deathsCrystal"));
                    account.set(Statistics.DEATHS_TNT, packedSet.getInt("deathsTNT"));
                    account.set(Statistics.DEATHS_BED, packedSet.getInt("deathsBed"));
                    account.set(Statistics.STREAK_KILLS, packedSet.getInt("killstreak"));
                    account.set(Statistics.STREAK_KILLS_HIGHEST, packedSet.getInt("highestKillstreak"));
                    account.set(Statistics.STREAK_DEATHS, packedSet.getInt("deathstreak"));
                    account.set(Statistics.STREAK_DEATHS_HIGHEST, packedSet.getInt("highestDeathstreak"));
                }

                if (resultSet != null && !resultSet.isClosed()){
                    resultSet.close();
                }
                getStatement.close();

                return account;
            } catch (SQLException e) {
                throw new RuntimeException(e);
            }
        });
        return future.exceptionally(exception("Encountered error while trying to get combat log of "+ uniqueId.toString()));

    }
}
