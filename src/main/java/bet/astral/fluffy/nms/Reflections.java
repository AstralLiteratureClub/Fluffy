package bet.astral.fluffy.nms;

import org.bukkit.Bukkit;

public class Reflections {
	protected static final String OBC = Bukkit.getServer().getClass().getPackage().getName();
	public static Class<?> getOBC(String obc){
		try {
			return Class.forName((OBC+"."+obc));
		} catch (ClassNotFoundException e) {
			throw new RuntimeException(e);
		}
	}
}
