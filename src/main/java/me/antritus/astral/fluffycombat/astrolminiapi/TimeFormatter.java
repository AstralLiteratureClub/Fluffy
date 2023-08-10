package me.antritus.astral.fluffycombat.astrolminiapi;

import java.time.Duration;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @author Antritus
 * @since 1.1-SNAPSHOT
 */
public class TimeFormatter {
	public static void main(String[] args) {
		String input = "1s2m";
		Duration formattedTime = formatTime(input);
		System.out.println("Formatted time: " + formattedTime.getSeconds());
	}

	public static Duration formatTime(String input) {
		Pattern pattern = Pattern.compile("(\\d+)([a-z]+)");
		Matcher matcher = pattern.matcher(input);
		Map<String, Long> timeUnits = new HashMap<>();

		while (matcher.find()) {
			long value = Long.parseLong(matcher.group(1));
			String unit = matcher.group(2);

			timeUnits.put(unit, timeUnits.getOrDefault(unit, 0L) + value);
		}

		long years = timeUnits.getOrDefault("y", 0L);
		long months = timeUnits.getOrDefault("mo", 0L);
		long weeks = timeUnits.getOrDefault("w", 0L);
		long days = timeUnits.getOrDefault("d", 0L);
		long hours = timeUnits.getOrDefault("h", 0L);
		long minutes = timeUnits.getOrDefault("m", 0L);
		long seconds = timeUnits.getOrDefault("s", 0L);

		return Duration.ofDays(years * 365 + months * 30 + weeks * 7 + days)
				.plusHours(hours)
				.plusMinutes(minutes)
				.plusSeconds(seconds);
	}
}