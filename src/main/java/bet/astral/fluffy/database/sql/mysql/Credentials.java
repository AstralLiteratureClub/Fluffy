package bet.astral.fluffy.database.sql.mysql;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class Credentials {
    private final String host;
    private final String username;
    private final String password;
}
