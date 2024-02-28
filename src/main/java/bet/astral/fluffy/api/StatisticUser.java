package bet.astral.fluffy.api;

import lombok.Getter;
import lombok.Setter;
import org.bukkit.scheduler.BukkitTask;
import org.jetbrains.annotations.NotNull;

import java.util.UUID;

public class StatisticUser {
	public boolean isNewStreak;
	public boolean isNewKills;
	public boolean isNewDeaths;
	@Getter
	@Setter
	private BukkitTask rejoinTimer;
	@Getter
	@NotNull
	private final UUID uniqueId;
	private int kills = 0;
	private int killsCrystal = 0;
	private int killsAnchor = 0;
	private int killsBed = 0;
	private int killsTNT = 0;
	private int killsVoid = 0;
	private int killsTotem = 0;
	private int deaths = 0;
	private int deathsCrystal = 0;
	private int deathsAnchor = 0;
	private int deathsBed = 0;
	private int deathsTNT = 0;
	private int deathsVoid = 0;
	private int deathsTotem = 0;
	private int killstreak = 0;
	private int totemKillstreak = 0;
	private int deathstreak = 0;
	private int totemDeathstreak = 0;

	public StatisticUser(@NotNull UUID uniqueId) {
		this.uniqueId = uniqueId;
	}



	public int get(Statistic statistic){
		if (statistic instanceof Statistic.Kills kills){
			switch (kills){
				case ALL -> {
					return this.kills;
				}
				case BED -> {
					return this.killsBed;
				}
				case TNT -> {
					return this.killsTNT;
				}
				case VOID -> {
					return this.killsVoid;
				}
				case ENDER_CRYSTAL -> {
					return this.killsCrystal;
				}
				case RESPAWN_ANCHOR -> {
					return this.killsAnchor;
				}
				case TOTEM_OF_UNDYING -> {
					return this.killsTotem;
				}
			}
			return 0;
		} else if (statistic instanceof Statistic.Deaths deaths){
			switch (deaths){
				case ALL -> {
					return this.deaths;
				}
				case BED -> {
					return this.deathsBed;
				}
				case TNT -> {
					return this.deathsTNT;
				}
				case VOID -> {
					return this.deathsVoid;
				}
				case ENDER_CRYSTAL -> {
					return this.deathsCrystal;
				}
				case RESPAWN_ANCHOR -> {
					return this.deathsAnchor;
				}
				case TOTEM_OF_UNDYING -> {
					return this.deathsTotem;
				}
			}
			return 0;
		} else if (statistic instanceof Statistic.Streak streak){
			switch (streak){
				case KILLS -> {
					return killstreak;
				}
				case DEATHS -> {
					return deathstreak;
				}
				case TOTEM_OF_UNDYING_KILLS -> {
					return totemKillstreak;
				}
				case TOTEM_OF_UNDYING_DEATHS -> {
					return totemDeathstreak;
				}
			}
			return 0;
		}
		return 0;
	}

	public void set(Statistic statistic, int amount){
		if (statistic instanceof Statistic.Kills kills){
			switch (kills){
				case ALL -> {
					this.kills = amount;
				}
				case BED -> {
					this.killsBed = amount;
				}
				case TNT -> {
					this.killsTNT = amount;
				}
				case VOID -> {
					this.killsVoid = amount;
				}
				case ENDER_CRYSTAL -> {
					this.killsCrystal = amount;
				}
				case RESPAWN_ANCHOR -> {
					this.killsAnchor = amount;
				}
				case TOTEM_OF_UNDYING -> {
					this.killsTotem = amount;
				}
			}
		} else if (statistic instanceof Statistic.Deaths deaths){
			switch (deaths){
				case ALL -> {
					this.deaths = amount;
				}
				case BED -> {
					this.deathsBed = amount;
				}
				case TNT -> {
					this.deathsTNT = amount;
				}
				case VOID -> {
					this.deathsVoid = amount;
				}
				case ENDER_CRYSTAL -> {
					this.deathsCrystal = amount;
				}
				case RESPAWN_ANCHOR -> {
					this.deathsAnchor = amount;
				}
				case TOTEM_OF_UNDYING -> {
					this.deathsTotem = amount;
				}
			}
		} else if (statistic instanceof Statistic.Streak streak){
			switch (streak){
				case KILLS -> {
					killstreak = amount;
				}
				case DEATHS -> {
					deathstreak = amount;
				}
				case TOTEM_OF_UNDYING_KILLS -> {
					totemKillstreak = amount;
				}
				case TOTEM_OF_UNDYING_DEATHS -> {
					totemDeathstreak = amount;
				}
			}
		}
	}

	public void add(Statistic statistic, int amount){
		if (statistic instanceof Statistic.Kills kills){
			switch (kills){
				case ALL -> {
					this.kills+=amount;
				}
				case BED -> {
					this.killsBed+=amount;
				}
				case TNT -> {
					this.killsTNT+=amount;
				}
				case VOID -> {
					this.killsVoid+=amount;
				}
				case ENDER_CRYSTAL -> {
					this.killsCrystal+=amount;
				}
				case RESPAWN_ANCHOR -> {
					this.killsAnchor+=amount;
				}
				case TOTEM_OF_UNDYING -> {
					this.killsTotem+=amount;
				}
			}
		} else if (statistic instanceof Statistic.Deaths deaths){
			switch (deaths){
				case ALL -> {
					this.deaths+=amount;
				}
				case BED -> {
					this.deathsBed+=amount;
				}
				case TNT -> {
					this.deathsTNT+=amount;
				}
				case VOID -> {
					this.deathsVoid+=amount;
				}
				case ENDER_CRYSTAL -> {
					this.deathsCrystal+=amount;
				}
				case RESPAWN_ANCHOR -> {
					this.deathsAnchor+=amount;
				}
				case TOTEM_OF_UNDYING -> {
					this.deathsTotem+=amount;
				}
			}
		} else if (statistic instanceof Statistic.Streak streak){
			switch (streak){
				case KILLS -> {
					killstreak+=amount;
				}
				case DEATHS -> {
					deathstreak+=amount;
				}
				case TOTEM_OF_UNDYING_KILLS -> {
					totemKillstreak+=amount;
				}
				case TOTEM_OF_UNDYING_DEATHS -> {
					totemDeathstreak+=amount;
				}
			}
		}
	}
}