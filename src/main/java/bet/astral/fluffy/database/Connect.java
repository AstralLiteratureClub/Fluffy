package bet.astral.fluffy.database;

import bet.astral.fluffy.FluffyCombat;
import lombok.Getter;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.function.Function;

public abstract class Connect {
    private final FluffyCombat fluffy;
    @Getter
    private Connection connection;

    public Connect(FluffyCombat fluffyCombat) {
        this.fluffy = fluffyCombat;
        this.connection = connect();
    }
    public abstract Connection connect();
    public void onEnable(){ }
    public void onDisable(){ }

    public boolean isClosed() throws SQLException {
        return getConnection().isClosed();
    }

    public FluffyCombat getFluffy(){
        return fluffy;
    }

    public <T> Function<Throwable, T> exception(String name){
        return throwable -> {
            getFluffy().getComponentLogger()
                    .error(name, throwable);
            return null;
        };
    }
}
