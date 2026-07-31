package bet.astral.fluffy.commands.commands.debug;

import bet.astral.cloudplusplus.annotations.Cloud;
import bet.astral.fluffy.FluffyCommandRegisterer;
import bet.astral.fluffy.commands.FluffyCommand;
import bet.astral.fluffy.configs.CombatConfig;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import org.bukkit.command.CommandSender;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.incendo.cloud.context.CommandContext;
import org.incendo.cloud.description.Description;
import org.incendo.cloud.paper.PaperCommandManager;
import org.incendo.cloud.permission.Permission;

import java.lang.reflect.Field;

@Cloud
public class ConfigCommand extends FluffyCommand {
    public ConfigCommand(FluffyCommandRegisterer registerer, PaperCommandManager.Bootstrapped<CommandSender> commandManager) {
        super(registerer, commandManager);
        command("flf-reload-config", Description.EMPTY,
                b -> b.permission(Permission.of("fluffy.debug.reload-config"))
                        .handler(this::handle)
        )
                .register();

        command("flf-see-config", Description.EMPTY,
                b -> b.permission(Permission.of("fluffy.debug.see-config"))
                        .handler(context->{
                            CommandSender sender = context.sender();
                            CombatConfig config = fluffy().getCombatConfig();

                            Class<CombatConfig> configClass = (Class<CombatConfig>) config.getClass();
                            for (Field field : configClass.getDeclaredFields()) {
                                field.setAccessible(true);
                                try {
                                    Object obj = field.get(config);
                                    if (obj == null){
                                        sender.sendMessage("Name: §e"+ field.getName() + "§f Value: §d"+ null);
                                    } else {
                                        sender.sendMessage("Name: §e" + field.getName() + "§f Value: §d" + obj.toString());
                                    }
                                } catch (IllegalAccessException e) {
                                    throw new RuntimeException(e);
                                }
                            }
                        })
        )
                .register();
        /*
        command("flf-chest-slots", Description.EMPTY,
                b -> b.permission(Permission.of("fluffy.debug.slots"))
                        .senderType(Player.class)
                        .handler(this::handle))
                .register();
         */
    }

    private void handle(@NonNull CommandContext<? extends CommandSender> handler){
        CommandSender sender = handler.sender();
        sender.sendMessage("Reloading fluffy config...");

        try {
            fluffy().reloadConfig();
        } catch (Exception e) {
            sender.sendMessage(Component.text("Caught an error while trying to reload the config!", NamedTextColor.RED));
            sender.sendMessage(Component.text(e.getMessage()));
        }

            sender.sendMessage(exceptionToComponent(new Exception("Test!")));

        sender.sendMessage("Reloaded configuration!");
    }

    public static Component exceptionToComponent(Throwable throwable) {
        if (throwable == null) {
            return Component.empty();
        }

        Component builder = Component.text("");

        // 1. Format the exception class name and message
        String message = throwable.getMessage();
        String exceptionName = throwable.getClass().getName();
        String headerText = exceptionName + (message != null ? ": " + message : "");

        builder.append(Component.text(headerText, NamedTextColor.RED, TextDecoration.BOLD));
        builder.append(Component.newline());

        // 2. Append the stack trace elements
        for (StackTraceElement element : throwable.getStackTrace()) {
            builder.append(Component.text("    at ", NamedTextColor.GRAY));
            builder.append(Component.text(element.toString(), NamedTextColor.DARK_GRAY));
            builder.append(Component.newline());
        }

        // 3. Recursively append the cause if it exists
        Throwable cause = throwable.getCause();
        if (cause != null && cause != throwable) {
            builder.append(Component.text("Caused by: ", NamedTextColor.GOLD, TextDecoration.BOLD));
            builder.append(exceptionToComponent(cause));
        }

        return builder;
    }
}
