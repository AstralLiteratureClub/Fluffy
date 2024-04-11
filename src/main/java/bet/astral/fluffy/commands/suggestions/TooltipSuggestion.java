package bet.astral.fluffy.commands.suggestions;

import com.mojang.brigadier.Message;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;

public class TooltipSuggestion implements org.incendo.cloud.brigadier.suggestion.TooltipSuggestion {
	private final String suggestion;
	private final Tooltip tooltip;

	public TooltipSuggestion(String suggestion, String tooltip) {
		this.suggestion = suggestion;
		this.tooltip = new Tooltip(tooltip);
	}

	@Override
	public @NonNull String suggestion() {
		return suggestion;
	}

	@Override
	public @Nullable Message tooltip() {
		return tooltip;
	}

	@Override
	public org.incendo.cloud.brigadier.suggestion.@NonNull TooltipSuggestion withSuggestion(@NonNull String suggestion) {
		return new TooltipSuggestion(suggestion, null);
	}

	private record Tooltip(String message) implements Message {
		@Override
			public String getString() {
				return message;
			}
		}
}
