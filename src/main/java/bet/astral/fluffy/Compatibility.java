package bet.astral.fluffy;

import org.bukkit.Bukkit;

public enum Compatibility {
	ENDER_CRYSTAL( "1.19", "1.20"),
	RESPAWN_ANCHOR("1.20.2"),
	BED("1.20.2"),
	DAMAGER_BLOCK_STATE("1.20.2")
	;
	private final String[] versions;
	private boolean isCompatible;
	Compatibility(String... versions) {
		this.versions = versions;
		isCompatible = false;
		for (String ver : versions){
			if (Bukkit.getServer().getVersion().contains(ver)){
				isCompatible = true;
				break;
			}
		}
	}

	public String[] versions() {
		return versions;
	}

	public boolean isCompatible(){
		return isCompatible;
	}
}
