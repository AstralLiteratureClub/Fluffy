package bet.astral.fluffy.statistic;

import bet.astral.fluffy.FluffyCombat;

import java.util.UUID;

public class PlaceholderAccount extends AccountImpl{
    public PlaceholderAccount(FluffyCombat fluffyCombat, UUID uniqueId) {
        super(fluffyCombat, uniqueId);
    }
    public void apply(Account account){
        for (Statistic statistic : getAllStatistics().keySet()){
            if (account instanceof AccountImpl impl){
                impl.addStatisticPlaceholder(statistic, getStatistic(statistic));
            }
            account.add(statistic, getStatistic(statistic));
        }
    }
}
