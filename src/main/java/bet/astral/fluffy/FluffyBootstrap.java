package bet.astral.fluffy;

import bet.astral.fluffy.messenger.FluffyMessenger;
import bet.astral.fluffy.messenger.Translations;
import bet.astral.messenger.v2.component.ComponentType;
import bet.astral.messenger.v2.source.LanguageTable;
import bet.astral.messenger.v2.source.source.FileLanguageSource;
import bet.astral.messenger.v2.source.source.LanguageSource;
import com.google.gson.JsonObject;
import com.google.gson.stream.JsonReader;
import io.papermc.paper.plugin.bootstrap.BootstrapContext;
import io.papermc.paper.plugin.bootstrap.PluginBootstrap;
import io.papermc.paper.plugin.bootstrap.PluginProviderContext;
import lombok.Getter;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;

import java.io.*;
import java.util.List;
import java.util.Locale;

import static bet.astral.fluffy.utils.Resource.loadResourceToFile;

@Getter
public class FluffyBootstrap implements PluginBootstrap {
	private final FluffyMessenger messenger = new FluffyMessenger();
	private FluffyCommandRegisterer commandRegisterer;
	@Override
	public void bootstrap(@NotNull BootstrapContext context) {
		try {
			File folder = context.getDataDirectory().toFile();
			uploadFileRenameFile(folder, "upload/statistics.json", "statistics.json");


			File file = new File(folder, "messages/en_us.json");
			if (!file.exists()){
				if (!file.getParentFile().exists()){
					file.getParentFile().mkdirs();
				}
				file.createNewFile();
			}
			JsonObject serverTranslations = Translations.GSON.fromJson(new JsonReader(new FileReader(file)), JsonObject.class);
			JsonObject newTranslations = Translations.getDefaults();
			JsonObject updatedTranslations = updateJsonWithNewKeys(serverTranslations, newTranslations);
			File en_us = uploadFile(folder, "messages/en_us.json", Translations.GSON.toJson(updatedTranslations));

			LanguageSource source = FileLanguageSource.gson(messenger, Locale.US, en_us, MiniMessage.miniMessage());
			LanguageTable table = LanguageTable.of(source);
			messenger.setDefaultLocale(source);
			messenger.registerLanguageTable(source.getLocale(), table);
			messenger.loadTranslations(List.copyOf(Translations.getTranslations()));
			messenger.setPrefix(messenger.parseComponent(Translations.MESSENGER_PREFIX, Locale.US, ComponentType.CHAT));
			messenger.setSendTranslationKey(true);
			messenger.setSendASync(true);

		} catch (IOException e) {
			throw new RuntimeException(e);
		}
		commandRegisterer = new FluffyCommandRegisterer(context, messenger);
	}

	public JsonObject updateJsonWithNewKeys(JsonObject originalObject, JsonObject newObject) {
		for (String key : newObject.keySet()) {
			if (originalObject.get(key) != null) {
				continue;
			}
			originalObject.add(key, newObject.get(key));
		}
		return originalObject;
	}

	public File uploadFile(File folder, String fileName, String data) throws IOException {
		File file = new File(folder, fileName);
		if (!file.exists()){
			if (!file.getParentFile().exists()){
				file.getParentFile().mkdirs();
			}
			file.createNewFile();
		}
		BufferedWriter writer = new BufferedWriter(new FileWriter(file));
		writer.write(data);
		writer.flush();
		writer.close();
		return file;
	}

	public void uploadFileRenameFile(File folder, String fileName, String newName) throws IOException {
		File file = new File(folder, fileName);
		if (!file.exists()){
			if (!file.getParentFile().exists()){
				file.getParentFile().mkdirs();
			}
			file.createNewFile();
		}
		File newFile = loadResourceToFile(fileName, file, true);
	}

	@Override
	public @NotNull JavaPlugin createPlugin(@NotNull PluginProviderContext context) {
		return new FluffyCombat(commandRegisterer.getHandler(),messenger);
	}
}
