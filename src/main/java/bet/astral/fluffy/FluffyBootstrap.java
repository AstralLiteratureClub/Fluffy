package bet.astral.fluffy;

import bet.astral.fluffy.messenger.FluffyMessenger;
import bet.astral.fluffy.messenger.Translations;
import bet.astral.messenger.v2.component.ComponentType;
import bet.astral.messenger.v2.source.LanguageTable;
import bet.astral.messenger.v2.source.source.FileLanguageSource;
import bet.astral.messenger.v2.source.source.LanguageSource;
import io.papermc.paper.plugin.bootstrap.BootstrapContext;
import io.papermc.paper.plugin.bootstrap.PluginBootstrap;
import io.papermc.paper.plugin.bootstrap.PluginProviderContext;
import lombok.Getter;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.List;
import java.util.Locale;

@Getter
public class FluffyBootstrap implements PluginBootstrap {
	private final FluffyMessenger messenger = new FluffyMessenger();
	private FluffyCommandRegisterer commandRegisterer;
	@Override
	public void bootstrap(@NotNull BootstrapContext context) {
		try {
			File file = new File(context.getDataDirectory().toFile(), "messages/en_us.json");
			if (!file.exists()){
				if (!file.getParentFile().exists()){
					file.getParentFile().mkdirs();
				}
				file.createNewFile();
			}
			BufferedWriter writer = new BufferedWriter(new FileWriter(file));
			writer.write(Translations.GSON.toJson(Translations.getDefaults()));
			writer.flush();
			writer.close();

			LanguageSource source = FileLanguageSource.gson(messenger, Locale.US, file, MiniMessage.miniMessage());
			LanguageTable table = LanguageTable.of(source);
			messenger.setDefaultLocale(source);
			messenger.registerLanguageTable(source.getLocale(), table);
			messenger.loadTranslations(List.copyOf(Translations.getTranslations()));
			messenger.setPrefix(messenger.parseComponent(Translations.MESSENGER_PREFIX, Locale.US, ComponentType.CHAT));

		} catch (IOException e) {
			throw new RuntimeException(e);
		}
		commandRegisterer = new FluffyCommandRegisterer(context, messenger);
	}

	@Override
	public @NotNull JavaPlugin createPlugin(@NotNull PluginProviderContext context) {
		return new FluffyCombat(commandRegisterer.getHandler(),messenger);
	}
}
