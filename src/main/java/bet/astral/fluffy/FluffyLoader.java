package bet.astral.fluffy;

import io.papermc.paper.plugin.loader.PluginClasspathBuilder;
import io.papermc.paper.plugin.loader.PluginLoader;
import io.papermc.paper.plugin.loader.library.impl.MavenLibraryResolver;
import org.eclipse.aether.artifact.DefaultArtifact;
import org.eclipse.aether.graph.Dependency;
import org.eclipse.aether.repository.RemoteRepository;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

public class FluffyLoader implements PluginLoader {

	public static final RemoteRepository MAVEN_CENTRAL_REPOSITORY = new RemoteRepository.Builder("central", "default", "https://repo1.maven.org/maven2/").build();
	public static final RemoteRepository JITPACK_REPOSITORY = new RemoteRepository.Builder("jitpack", "default", "https://jitpack.io").build();

	public static final String CLOUD_MINECRAFT_VERSION = "2.0.0-beta.10";
	public static final String CLASSGRAPH_VERSION = "4.8.184";
	public static final String MORE_FOR_JAVA_VERSION = "1.0.2";
	public static final String MESSAGE_MANAGER_VERSION = "2.4.1";
	public static final String GUIMAN_VERSION = "1.3.1-3";
//	public static final String CHAT_GAME_CORE_VERSION = "1.0.1";
	public static final String CLOUD_PLUS_PLUS_VERSION = "1.3.0";
	public static final String AURA_VERSION = "-SNAPSHOT";
	public static final String MORE_PERSISTENT_DATA_TYPES = "2.4.0";

	@Contract(pure = true)
	public static @NotNull String cloudMinecraftDependency(@NotNull String dependency) {
		return "org.incendo:"+dependency+":"+CLOUD_MINECRAFT_VERSION;
	}

	@Contract(pure = true)
	public static @NotNull String astralLiteratureClubDependency(@NotNull String dependency, @NotNull String version) {
		return "com.github.AstralLiteratureClub:"+dependency+":"+version;
	}

	@Contract(pure = true)
	public static @NotNull String antritusDependency(@NotNull String dependency, @NotNull String version) {
		return "com.github.Antritus:"+dependency+":"+version;
	}

	public static final String[] DEPENDENCIES = {
			cloudMinecraftDependency("cloud-minecraft"),
			cloudMinecraftDependency("cloud-brigadier"),
			cloudMinecraftDependency("cloud-minecraft-extras"),
			"io.github.classgraph:classgraph:"+CLASSGRAPH_VERSION,
			"com.jeff-media.armor-equip-event:1.0.2",
			astralLiteratureClubDependency("MoreForJava",    MORE_FOR_JAVA_VERSION),
			astralLiteratureClubDependency("MessageManager", MESSAGE_MANAGER_VERSION),
			astralLiteratureClubDependency("GUIMan",         GUIMAN_VERSION),
//			astralLiteratureClubDependency("ChatGameCore",   CHAT_GAME_CORE_VERSION),
			astralLiteratureClubDependency("CloudPlusPlus",  CLOUD_PLUS_PLUS_VERSION),
			antritusDependency("Aura", AURA_VERSION),
			"com.jeff-media:MorePersistentDataTypes:"+MORE_PERSISTENT_DATA_TYPES,
	};


	@Override
	public void classloader(@NotNull PluginClasspathBuilder pluginClasspathBuilder) {
		MavenLibraryResolver resolver = new MavenLibraryResolver();

		// Repositories
		resolver.addRepository(MAVEN_CENTRAL_REPOSITORY);
		resolver.addRepository(JITPACK_REPOSITORY);

		for (String dependency : DEPENDENCIES) {
			resolver.addDependency(new Dependency(new DefaultArtifact(dependency), null));
		}
		pluginClasspathBuilder.addLibrary(resolver);
	}
}
