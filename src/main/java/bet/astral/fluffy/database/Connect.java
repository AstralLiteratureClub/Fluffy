package bet.astral.fluffy.database;

import bet.astral.fluffy.FluffyCombat;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.function.Function;

public class Connect {
    private final Database database;

    public Connect(Database database) {
        this.database = database;
    }
    public void onEnable(){ }
    public void onDisable(){ }

    public boolean isClosed() throws SQLException {
        return database.getConnection().isClosed();
    }

    public Connection getConnection(){
        return database.getConnection();
    }

    public FluffyCombat getFluffy(){
        return database.getFluffy();
    }

    public <T> Function<Throwable, T> exception(String name){
        return throwable -> {

            getFluffy().getComponentLogger()
                    .error(name, throwable);
            return null;
        };
    }
}
