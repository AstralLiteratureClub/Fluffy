package bet.astral.fluffy.commands;

import bet.astral.fluffy.FluffyCombat;
import bet.astral.fluffy.commands.arguments.StatisticParser;
import bet.astral.fluffy.statistic.Account;
import bet.astral.fluffy.statistic.Statistic;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.CommandSender;
import org.incendo.cloud.Command;
import org.incendo.cloud.bukkit.parser.OfflinePlayerParser;
import org.incendo.cloud.description.Description;
import org.incendo.cloud.execution.CommandExecutionHandler;
import org.incendo.cloud.paper.PaperCommandManager;
import org.incendo.cloud.permission.Permission;
import org.incendo.cloud.type.tuple.Triplet;

public class EditStatisticsCommand {
	private final FluffyCombat fluffy;
	private final Command.Builder<CommandSender> builder;
	public EditStatisticsCommand(FluffyCombat fluffy, PaperCommandManager<CommandSender> manager) {
		this.fluffy = fluffy;
		manager.parserRegistry().registerParser(StatisticParser.statisticParser());

		builder = manager.commandBuilder("editstats",
						"editstatistics"
				)
				.commandDescription(Description.of("Allows admins to edit the statistics of players."))
				.permission(Permission.of("fluffy.editstatistics.set")
						.or(Permission.of("fluffy.editstatistics.reset"))
						.or(Permission.of("fluffy.editstatistics.add"))
						.or(Permission.of("fluffy.editstatistics.remove")))
				.handler(context -> {
					CommandSender sender = context.sender();
				});
		manager.command(builder);
		manager.command(
				builder
						.literal("reset")
						.permission(Permission.of("fluffy.editstatistics.reset"))
						.commandDescription(Description.of("Resets certain statistic of a player"))
						.required(
								OfflinePlayerParser.offlinePlayerComponent()
										.name("player")
										.description(Description.of("Player to reset statistics for")))
						.optional(StatisticParser.statisticComponent()
								.name("statistic")
								.description(Description.of("Optional. Specify the statistic to reset."))
						)
						.handler(context -> {
							CommandSender sender = context.sender();
							OfflinePlayer player = context.get("player");
							Statistic statistic = context.getOrDefault("statistic", null);

							Account account = fluffy.getStatisticManager().get(player.getUniqueId());
							if (account == null) {
								fluffy.getStatisticManager().load(player)
										.thenRunAsync(() -> {
											Account acc = fluffy.getStatisticManager().get(player.getUniqueId());
											if (statistic == null) {
												for (Statistic stat : acc.getAllStatistics().keySet()) {
													acc.reset(stat);
												}
											} else {
												acc.reset(statistic);
											}
											acc.save();
										})
								;
							} else {
								if (statistic == null) {
									for (Statistic stat : account.getAllStatistics().keySet()) {
										account.reset(stat);
									}
								} else {
									account.reset(statistic);
								}
								account.save();
							}
						}));

		manager.command(handle("set", "Sets a certain statistic of a player", context -> {
			CommandSender sender = context.sender();
			OfflinePlayer player = context.get("player");
			Statistic statistic = context.get("statistic");
			int amount = context.get("amount");
			Account account = fluffy.getStatisticManager().get(player.getUniqueId());
			if (account == null) {
				fluffy.getStatisticManager().load(player)
						.thenRunAsync(() -> {
							Account acc = fluffy.getStatisticManager().get(player.getUniqueId());
							acc.set(statistic, amount);
							acc.save();
						})
				;
			} else {
				account.set(statistic, amount);
				account.save();
			}
		}));


		manager.command(handle("add", "Adds to a certain statistic of a player", context -> {
			CommandSender sender = context.sender();
			OfflinePlayer player = context.get("player");
			Statistic statistic = context.get("statistic");
			int amount = context.get("amount");
			Account account = fluffy.getStatisticManager().get(player.getUniqueId());
			if (account == null) {
				fluffy.getStatisticManager().load(player)
						.thenRunAsync(() -> {
							Account acc = fluffy.getStatisticManager().get(player.getUniqueId());
							acc.add(statistic, amount);
							acc.save();
						})
				;
			} else {
				account.add(statistic, amount);
				account.save();
			}
		}));


		manager.command(handle("remove", "Removes from a certain statistic of a player", context -> {
			CommandSender sender = context.sender();
			OfflinePlayer player = context.get("player");
			Statistic statistic = context.get("statistic");
			int amount = context.get("amount");
			Account account = fluffy.getStatisticManager().get(player.getUniqueId());
			if (account == null) {
				fluffy.getStatisticManager().load(player)
						.thenRunAsync(() -> {
							Account acc = fluffy.getStatisticManager().get(player.getUniqueId());
							acc.remove(statistic, amount);
							acc.save();
						})
				;
			} else {
				account.set(statistic, amount);
				account.save();
			}
		}));
	}
	private Command.Builder<CommandSender> handle(String name, String description, CommandExecutionHandler<CommandSender> context){
		return builder
				.literal(name)
				.permission(Permission.of("fluffy.editstatistics."+name))
				.commandDescription(Description.of(description))
				.requiredArgumentTriplet("arguments",
						Triplet.of("player", "statistic", "amount"),
						Triplet.of(OfflinePlayer.class, Statistic.class, Integer.class),
						Description.of("Player to change statistics for, statistic to chance, amount to change with")
				)
				.handler(context);
	}

}