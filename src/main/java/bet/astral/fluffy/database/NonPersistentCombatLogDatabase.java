package bet.astral.fluffy.database;

import bet.astral.fluffy.database.cache.CombatLog;
import org.jetbrains.annotations.Nullable;

import java.time.Instant;
import java.util.Date;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;

public class NonPersistentCombatLogDatabase implements ICombatLogDatabase {
    private final Map<UUID, CombatLog> combatLogs = new ConcurrentHashMap<>();
    @Override
    public void onDisable() {

    }

    @Override
    public void onEnable() {

    }

    @Override
    public CompletableFuture<@Nullable CombatLog> getLog(UUID uniqueId) {
        return CompletableFuture.supplyAsync(()->combatLogs.get(uniqueId));
    }

    @Override
    public void delete(UUID uniqueId) throws Exception {
        combatLogs.remove(uniqueId);
    }

    @Override
    public void save(UUID uniqueId) {

    }

    @Override
    public void update(UUID uniqueId, UUID killer) {
        combatLogs.put(uniqueId, new CombatLog(
                uniqueId,
                Date.from(Instant.now()),
                killer,
                true
        ));
    }
}
