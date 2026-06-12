package bet.astral.fluffy.actions;

import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Consumer;

public abstract class Action {
    private static final Map<String, Action> actions = new HashMap<>();

    public static final Action KILL_PLAYER = register("kill_player", p-> p.setHealth(0));
    public static final Action DAMAGE_PLAYER_1 = register("damage_player_1", p-> p.damage(1));
    public static final Action DAMAGE_PLAYER_2 = register("damage_player_2", p-> p.damage(2));
    public static final Action DAMAGE_PLAYER_3 = register("damage_player_3", p-> p.damage(3));
    public static final Action DAMAGE_PLAYER_4 = register("damage_player_4", p-> p.damage(4));
    public static final Action DAMAGE_PLAYER_5 = register("damage_player_5", p-> p.damage(5));
    public static final Action DAMAGE_PLAYER_6 = register("damage_player_6", p-> p.damage(6));
    public static final Action DAMAGE_PLAYER_7 = register("damage_player_7", p-> p.damage(7));
    public static final Action DAMAGE_PLAYER_8 = register("damage_player_8", p-> p.damage(8));
    public static final Action DAMAGE_PLAYER_9 = register("damage_player_9", p-> p.damage(9));
    public static final Action DAMAGE_PLAYER_10 = register("damage_player_10", p-> p.damage(10));
    public static final Action DAMAGE_PLAYER_11 = register("damage_player_11", p-> p.damage(11));
    public static final Action DAMAGE_PLAYER_12 = register("damage_player_12", p-> p.damage(12));
    public static final Action DAMAGE_PLAYER_13 = register("damage_player_13", p-> p.damage(13));
    public static final Action DAMAGE_PLAYER_14 = register("damage_player_14", p-> p.damage(14));
    public static final Action DAMAGE_PLAYER_15 = register("damage_player_15", p-> p.damage(15));
    public static final Action DAMAGE_PLAYER_16 = register("damage_player_16", p-> p.damage(16));
    public static final Action DAMAGE_PLAYER_17 = register("damage_player_17", p-> p.damage(17));
    public static final Action DAMAGE_PLAYER_18 = register("damage_player_18", p-> p.damage(18));
    public static final Action DAMAGE_PLAYER_19 = register("damage_player_19", p-> p.damage(19));
    public static final Action DAMAGE_PLAYER_20 = register("damage_player_20", p-> p.damage(20));

    public static Action get(@NotNull String name) {
        return actions.get(name.toLowerCase());
    }

    public static Action register(@NotNull Action action) {
        actions.put(action.getName().toLowerCase(), action);
        return action;
    }
    public static Action register(@NotNull String name, @NotNull Consumer<Player> actionConsumer) {
        Action action = new Action(name) {
            @Override
            public void run(Player player) {
                actionConsumer.accept(player);
            }
        };
        actions.put(action.getName().toLowerCase(), action);

        return action;
    }

    private final String name;

    protected Action(@NotNull String name) {
        this.name = name;
    }

    public abstract void run(Player player);

    @NotNull
    public String getName() {
        return name;
    }
}
