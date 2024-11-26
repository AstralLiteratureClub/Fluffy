package bet.astral.fluffy.database.cache;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.util.Date;
import java.util.UUID;

@RequiredArgsConstructor
@Getter
public class CombatLog {
    private final UUID uniqueId;
    private final Date date;
    private final UUID killedBy;
    private final boolean killed;
}
