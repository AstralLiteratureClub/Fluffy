package bet.astral.fluffy.commands;

import bet.astral.fluffy.FluffyCombat;
import bet.astral.fluffy.commands.arguments.StatisticParser;
import bet.astral.fluffy.messenger.MessageKey;
import bet.astral.fluffy.statistic.Account;
import bet.astral.fluffy.statistic.Statistic;
import bet.astral.messenger.placeholder.Placeholder;
import io.leangen.geantyref.TypeToken;
import lombok.Getter;
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
		manager.parserRegistry().registerNamedParser("statistic", StatisticParser.statisticParser());
		manager.parserRegistry().registerNamedParser("offline-player", OfflinePlayerParser.offlinePlayerParser());

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
					fluffy.messenger().message(sender, MessageKey.EDIT_STATS_HELP);
					fluffy.messenger().message(Permission.of("fluffy.editstatistics.reset"), sender, MessageKey.EDIT_STATS_HELP_RESET);
					fluffy.messenger().message(Permission.of("fluffy.editstatistics.set"), sender, MessageKey.EDIT_STATS_HELP_SET);
					fluffy.messenger().message(Permission.of("fluffy.editstatistics.add"), sender, MessageKey.EDIT_STATS_HELP_ADD);
					fluffy.messenger().message(Permission.of("fluffy.editstatistics.remove"), sender, MessageKey.EDIT_STATS_HELP_REMOVE);
				});
		manager.command(builder);
		manager.command(
				builder
						.literal("reset")
						.permission(Permission.of("fluffy.editstatistics.reset"))
						.commandDescription(Description.of("Resets certain statistic of a player"))
						.required(
								OfflinePlayerParser.offlinePlayerComponent()
										.name("offline-player")
										.description(Description.of("Player to reset statistics for")))
						.optional(StatisticParser.statisticComponent()
								.name("statistic")
								.description(Description.of("Optional. Specify the statistic to reset."))
						)
						.handler(context -> {
							CommandSender sender = context.sender();
							OfflinePlayer player = context.get("offline-player");
							Statistic statistic = context.getOrDefault("statistic", null);

							Account account = fluffy.getStatisticManager().get(player.getUniqueId());
							if (account == null) {
								fluffy.getStatisticManager().load(player)
										.thenRunAsync(() -> {
											Account acc = fluffy.getStatisticManager().get(player.getUniqueId());
											if (statistic == null) {
												for (Statistic stat : acc.getAllStatistics().keySet()) {
													int before = acc.getStatistic(stat);
													acc.reset(stat);
													fluffy.messenger()
															.message(sender,
																	MessageKey.EDIT_STATS_RESET,
																	new Placeholder("player", player.getName()),
																	new Placeholder("old_value", before),
																	new Placeholder("new_value", 0),
																	new Placeholder("statistic", stat)
															);
												}
											} else {
												int before = acc.getStatistic(statistic);
												acc.reset(statistic);
												fluffy.messenger()
														.message(sender,
																MessageKey.EDIT_STATS_RESET,
																new Placeholder("player", player.getName()),
																new Placeholder("old_value", before),
																new Placeholder("new_value", 0),
																new Placeholder("statistic", statistic)
														);
											}
											acc.save();
										})
								;
							} else {
								if (statistic == null) {
									for (Statistic stat : account.getAllStatistics().keySet()) {
										int before = account.getStatistic(stat);
										account.reset(stat);
										fluffy.messenger()
												.message(sender,
														MessageKey.EDIT_STATS_RESET,
														new Placeholder("player", player.getName()),
														new Placeholder("old_value", before),
														new Placeholder("new_value", 0),
														new Placeholder("statistic", stat)
												);
									}
								} else {
									int before = account.getStatistic(statistic);
									account.reset(statistic);
									fluffy.messenger()
											.message(sender,
													MessageKey.EDIT_STATS_RESET,
													new Placeholder("player", player.getName()),
													new Placeholder("old_value", before),
													new Placeholder("new_value", 0),
													new Placeholder("statistic", statistic)
											);
								}
								account.save();
							}
						}));

		manager.command(handle("set", "Sets a certain statistic of a player", context -> {
			CommandSender sender = context.sender();
			Editor editor = context.get("editor");
			OfflinePlayer player = editor.getPlayer();
			Statistic statistic = editor.getStatistic();
			int amount = editor.getAmount();

			Account account = fluffy.getStatisticManager().get(player.getUniqueId());
			if (account == null) {
				fluffy.getStatisticManager().load(player)
						.thenRunAsync(() -> {
							Account acc = fluffy.getStatisticManager().get(player.getUniqueId());
							int before = account.getStatistic(statistic);
							acc.set(statistic, amount);
							acc.save();
							fluffy.messenger()
									.message(sender,
											MessageKey.EDIT_STATS_SET,
											new Placeholder("player", player.getName()),
											new Placeholder("old_value", before),
											new Placeholder("new_value", amount),
											new Placeholder("statistic", statistic)
									);
						})
				;
			} else {
				int before = account.getStatistic(statistic);
				account.set(statistic, amount);
				account.save();
				fluffy.messenger()
						.message(sender,
								MessageKey.EDIT_STATS_SET,
								new Placeholder("player", player.getName()),
								new Placeholder("old_value", before),
								new Placeholder("new_value", amount),
								new Placeholder("statistic", statistic)
						);
			}
		}));


		manager.command(handle("add", "Adds to a certain statistic of a player", context -> {
			CommandSender sender = context.sender();
			Editor editor = context.get("editor");
			OfflinePlayer player = editor.getPlayer();
			Statistic statistic = editor.getStatistic();
			int amount = editor.getAmount();

			Account account = fluffy.getStatisticManager().get(player.getUniqueId());
			if (account == null) {
				fluffy.getStatisticManager().load(player)
						.thenRunAsync(() -> {
							Account acc = fluffy.getStatisticManager().get(player.getUniqueId());
							int before = account.getStatistic(statistic);
							acc.add(statistic, amount);
							acc.save();
							fluffy.messenger()
									.message(sender,
											MessageKey.EDIT_STATS_ADD,
											new Placeholder("player", player.getName()),
											new Placeholder("old_value", before),
											new Placeholder("new_value", amount),
											new Placeholder("statistic", statistic)
									);
						})
				;
			} else {
				int before = account.getStatistic(statistic);
				account.add(statistic, amount);
				account.save();
				fluffy.messenger()
						.message(sender,
								MessageKey.EDIT_STATS_ADD,
								new Placeholder("player", player.getName()),
								new Placeholder("old_value", before),
								new Placeholder("new_value", amount),
								new Placeholder("statistic", statistic)
						);

			}
		}));


		manager.command(handle("remove", "Removes from a certain statistic of a player", context -> {
			CommandSender sender = context.sender();
			Editor editor = context.get("editor");
			OfflinePlayer player = editor.getPlayer();
			Statistic statistic = editor.getStatistic();
			int amount = editor.getAmount();

			Account account = fluffy.getStatisticManager().get(player.getUniqueId());
			if (account == null) {
				fluffy.getStatisticManager().load(player)
						.thenRunAsync(() -> {
							Account acc = fluffy.getStatisticManager().get(player.getUniqueId());
							int before = account.getStatistic(statistic);
							acc.remove(statistic, amount);
							acc.save();

							fluffy.messenger()
									.message(sender,
											MessageKey.EDIT_STATS_REMOVE,
											new Placeholder("player", player.getName()),
											new Placeholder("old_value", before),
											new Placeholder("new_value", amount),
											new Placeholder("statistic", statistic)
									);
						});
			} else {
				int before = account.getStatistic(statistic);
				account.remove(statistic, amount);
				account.save();
				fluffy.messenger()
						.message(sender,
								MessageKey.EDIT_STATS_REMOVE,
								new Placeholder("player", player.getName()),
								new Placeholder("old_value", before),
								new Placeholder("new_value", amount),
								new Placeholder("statistic", statistic)
								);
			}
		}));
	}

	private Command.Builder<CommandSender> handle(String name, String description, CommandExecutionHandler<CommandSender> context) {
		return builder
				.literal(name)
				.permission(Permission.of("fluffy.editstatistics." + name))
				.commandDescription(Description.of(description))
				.requiredArgumentTriplet(
						"editor",
						TypeToken.get(Editor.class),
						Triplet.of("offline-player", "statistic", "amount"),
						Triplet.of(OfflinePlayer.class, Statistic.class, Integer.class),
						(sender, pair)-> new Editor(pair.first(), pair.second(), pair.third()),
						Description.of("Player to change statistics for, statistic to chance, amount to change with")
				)
				.handler(context);
	}

	@Getter
	private static class Editor {
		private final OfflinePlayer player;
		private final Statistic statistic;
		private final Integer amount;
		public Editor(OfflinePlayer player, Statistic statistic, Integer amount) {
			this.player = player;
			this.statistic = statistic;
			this.amount = amount;
		}
	}
}