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

		// Sonatype
		resolver.addRepository(new RemoteRepository.Builder("sonatype", "default", "https://oss.sonatype.org/content/repositories/snapshots/").build());

		// Cloud
		String cloudFramework = "2.0.0-SNAPSHOT";
		resolver.addDependency(new Dependency(new DefaultArtifact("org.incendo:cloud-core:"+cloudFramework), null));
		resolver.addDependency(new Dependency(new DefaultArtifact("org.incendo:cloud-paper:"+cloudFramework), null));
		resolver.addDependency(new Dependency(new DefaultArtifact("org.incendo:cloud-brigadier:"+cloudFramework), null));
		resolver.addDependency(new Dependency(new DefaultArtifact("org.incendo:cloud-minecraft-extras:"+cloudFramework), null));
		pluginClasspathBuilder.addLibrary(resolver);

		resolver = new MavenLibraryResolver();




		// Maven Central
		resolver.addRepository(new RemoteRepository.Builder("central", "default", "https://repo1.maven.org/maven2/").build());

		// Classgraph for reflections
		resolver.addDependency(new Dependency(new DefaultArtifact("io.github.classgraph:classgraph:4.8.165"), null));

		// Tuples
		resolver.addDependency(new Dependency(new DefaultArtifact("org.javatuples:javatuples:1.2"), null));

		// Armor change event
		resolver.addDependency(new Dependency(new DefaultArtifact("com.jeff-media:armor-equip-event:1.0.2"), null));

		// Glowing entities
		resolver.addDependency(new Dependency(new DefaultArtifact("io.github.skytasul:glowingentities:1.3.2"), null));

		// JDA
		resolver.addDependency(new Dependency(new DefaultArtifact("net.dv8tion:JDA:5.0.0-beta.10"), null));

		// H2 Database
		resolver.addDependency(new Dependency(new DefaultArtifact("com.h2database:h2:2.2.224"), null));

		// ormlite
		resolver.addDependency(new Dependency(new DefaultArtifact("com.j256:ormlite:3.0"), null));

		// Database stuff (mysql)
		resolver.addDependency(new Dependency(new DefaultArtifact("com.zaxxer:HikariCP:5.1.0"), null));
		pluginClasspathBuilder.addLibrary(resolver);
	}
}
