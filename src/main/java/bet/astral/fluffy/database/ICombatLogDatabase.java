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

public interface ICombatLogDatabase{
    void onDisable();
    void onEnable();

    CompletableFuture<@Nullable CombatLog> getLog(UUID uniqueId);
    void delete(UUID uniqueId) throws Exception;

    void save(UUID uniqueId);
    void update(UUID uniqueId, UUID killer);
}
