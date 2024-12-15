package bet.astral.fluffy.events.player;

import bet.astral.fluffy.statistic.Account;
import bet.astral.fluffy.statistic.Statistic;
import lombok.Getter;
import org.bukkit.OfflinePlayer;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;

@Getter
public class PlayerNewHighStreakEvent extends Event {
    private static final HandlerList HANDLER_LIST = new HandlerList();
    private final OfflinePlayer player;
    private final Account account;
    private final Statistic streak;
    private final Statistic highestStreak;
    private int value;


    public PlayerNewHighStreakEvent(OfflinePlayer player, Account account, Statistic streak, Statistic highestStreak) {
        this.player = player;
        this.account = account;
        this.streak = streak;
        this.highestStreak = highestStreak;
        this.value = account.getStatistic(highestStreak);
    }

    public static HandlerList getHandlerList(){
        return HANDLER_LIST;
    }
    @Override
    public @NotNull HandlerList getHandlers() {
        return HANDLER_LIST;
    }
}
