package bet.astral.fluffy.database;

import bet.astral.fluffy.FluffyCombat;
import lombok.Getter;

import java.io.File;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

@Getter
public class Database {
    private FluffyCombat fluffy;
    private Connection connection;

    public Database(FluffyCombat fluffyCombat) {
        this.fluffy = fluffyCombat;
        this.connection = connect();
    }

    Connection connect() {
        File file = new File(fluffy.getDataFolder(), "combatlog.db");
        if (file.getParentFile().exists()) {
            file.getParentFile().mkdir();
        }

        try {
            return DriverManager.getConnection("jdbc:sqlite:" + file.getName());
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }
}

