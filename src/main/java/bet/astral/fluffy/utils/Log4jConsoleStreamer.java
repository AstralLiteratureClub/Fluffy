package bet.astral.fluffy.utils;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.core.Appender;
import org.apache.logging.log4j.core.LogEvent;
import org.apache.logging.log4j.core.LoggerContext;
import org.apache.logging.log4j.core.appender.AbstractAppender;
import org.apache.logging.log4j.core.config.Configuration;
import org.apache.logging.log4j.core.config.LoggerConfig;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.HashSet;
import java.util.UUID;

public class Log4jConsoleStreamer extends AbstractAppender {

    private final JavaPlugin plugin;
    private final HashSet<UUID> watchers = new HashSet<>();

    public Log4jConsoleStreamer(JavaPlugin plugin) {
        super("Log4jConsoleStreamer", null, null, true, null);
        this.plugin = plugin;
        this.start();
        
        // Attach directly to Log4j root logger context
        LoggerContext context = (LoggerContext) LogManager.getContext(false);
        Configuration configuration = context.getConfiguration();
        configuration.addAppender(this);
        
        // Update loggers to include this appender
        updateLoggers(configuration, this);
    }

    private void updateLoggers(Configuration configuration, Appender appender) {
        for (LoggerConfig loggerConfig : configuration.getLoggers().values()) {
            loggerConfig.addAppender(appender, null, null);
        }
        configuration.getRootLogger().addAppender(appender, null, null);
    }

    @Override
    public void append(LogEvent event) {
        if (watchers.isEmpty()) return;

        // Extract message safely
        String message = event.getMessage().getFormattedMessage();
        String level = event.getLevel().name();
        Component logComponent = Component.text("[" + level + "] " + message, NamedTextColor.GRAY);

        // Push back to the main thread for safe player chat dispatching
        Bukkit.getScheduler().runTask(plugin, () -> {
            for (UUID uuid : watchers) {
                Player player = Bukkit.getPlayer(uuid);
                if (player != null && player.isOnline()) {
                    player.sendMessage(logComponent);
                }
            }
        });
    }

    public void toggleWatcher(Player player) {
        UUID uuid = player.getUniqueId();
        if (watchers.remove(uuid)) {
            player.sendMessage(Component.text("Log stream disabled.", NamedTextColor.RED));
        } else {
            watchers.add(uuid);
            player.sendMessage(Component.text("Log stream enabled.", NamedTextColor.GREEN));
        }
    }

    public void unregister() {
        this.stop();
        LoggerContext context = (LoggerContext) LogManager.getContext(false);
        Configuration configuration = context.getConfiguration();
        configuration.getRootLogger().removeAppender(getName());
        context.updateLoggers();
        watchers.clear();
    }
}