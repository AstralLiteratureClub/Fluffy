package bet.astral.fluffy;

import io.papermc.paper.plugin.loader.PluginClasspathBuilder;
import io.papermc.paper.plugin.loader.PluginLoader;
import io.papermc.paper.plugin.loader.library.impl.MavenLibraryResolver;
import org.eclipse.aether.artifact.DefaultArtifact;
import org.eclipse.aether.graph.Dependency;
import org.eclipse.aether.repository.RemoteRepository;
import org.jetbrains.annotations.NotNull;

public class FluffyLoader implements PluginLoader {
	@Override
	public void classloader(@NotNull PluginClasspathBuilder pluginClasspathBuilder) {
		MavenLibraryResolver resolver = new MavenLibraryResolver();


		// Maven Central
		resolver.addRepository(new RemoteRepository.Builder("central", "default", "https://repo1.maven.org/maven2/").build());


		// Cloud
		String cloudFramework = "2.0.0-beta.8";
		resolver.addDependency(new Dependency(new DefaultArtifact("org.incendo:cloud-paper:"+cloudFramework), null));
		resolver.addDependency(new Dependency(new DefaultArtifact("org.incendo:cloud-brigadier:"+cloudFramework), null));
		resolver.addDependency(new Dependency(new DefaultArtifact("org.incendo:cloud-minecraft-extras:"+cloudFramework), null));

		pluginClasspathBuilder.addLibrary(resolver);
		// Classgraph for reflections
		resolver.addDependency(new Dependency(new DefaultArtifact("io.github.classgraph:classgraph:4.8.165"), null));
		// Armor change event
		resolver.addDependency(new Dependency(new DefaultArtifact("com.jeff-media:armor-equip-event:1.0.2"), null));
		pluginClasspathBuilder.addLibrary(resolver);
	}
}
