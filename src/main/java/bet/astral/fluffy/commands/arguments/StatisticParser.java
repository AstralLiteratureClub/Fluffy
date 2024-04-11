package bet.astral.fluffy.commands.arguments;

import bet.astral.fluffy.commands.suggestions.TooltipSuggestion;
import bet.astral.fluffy.statistic.Statistic;
import bet.astral.fluffy.statistic.StatisticDescription;
import bet.astral.fluffy.statistic.Statistics;
import org.apiguardian.api.API;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.incendo.cloud.component.CommandComponent;
import org.incendo.cloud.context.CommandContext;
import org.incendo.cloud.context.CommandInput;
import org.incendo.cloud.description.Description;
import org.incendo.cloud.minecraft.extras.RichDescription;
import org.incendo.cloud.minecraft.extras.suggestion.ComponentTooltipSuggestion;
import org.incendo.cloud.parser.ArgumentParseResult;
import org.incendo.cloud.parser.ArgumentParser;
import org.incendo.cloud.parser.ParserDescriptor;
import org.incendo.cloud.suggestion.Suggestion;
import org.incendo.cloud.suggestion.SuggestionProvider;

import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

public class StatisticParser<C> implements ArgumentParser<C, Statistic>, SuggestionProvider<C> {
	private static final Set<Statistic> statistics = new HashSet<>();

	static {
		statistics.addAll(List.of(Statistics.values()));
	}

	public static void addStatistic(Statistic statistic) {
		statistics.add(statistic);
	}


	@API(
			status = API.Status.STABLE,
			since = "2.0.0"
	)
	public static <C> @NonNull ParserDescriptor<C, Statistic> statisticParser() {
		return ParserDescriptor.of(new StatisticParser<>(), Statistic.class);
	}

	@API(
			status = API.Status.STABLE,
			since = "2.0.0"
	)
	public static <C> CommandComponent.@NonNull Builder<C, Statistic> statisticComponent() {
		return CommandComponent.<C, Statistic>builder().parser(statisticParser());
	}

	@Override
	public @NonNull ArgumentParseResult<@NonNull Statistic> parse(@NonNull CommandContext<@NonNull C> commandContext, @NonNull CommandInput commandInput) {
		String input = commandInput.readInput();
		if (statistics.stream().noneMatch(stat -> stat.getName().equalsIgnoreCase(input))) {
			return ArgumentParseResult.failure(new IllegalArgumentException("Couldn't find statistic for name " + input));
		}
		return ArgumentParseResult.success(statistics.stream().filter(statistic -> statistic.getName().equalsIgnoreCase(input)).findFirst().get());
	}

	@Override
	public @NonNull CompletableFuture<? extends @NonNull Iterable<? extends @NonNull Suggestion>> suggestionsFuture(@NonNull CommandContext<C> context, @NonNull CommandInput input) {
		return CompletableFuture.supplyAsync(() -> statistics.stream()
				.map(val -> {
					if (val instanceof StatisticDescription statisticDescription) {
						Description description = statisticDescription.getDescription();
						if (description instanceof RichDescription richDescription) {
							return ComponentTooltipSuggestion.suggestion(val.getName(), richDescription.contents());
						}
						return new TooltipSuggestion(val.getName(), description.textDescription());
					} else {
						return Suggestion.suggestion(val.getName());
					}
				}).collect(Collectors.toList()));
	}
}
