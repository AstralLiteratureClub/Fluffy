package bet.astral.fluffy.cosmetics;

import bet.astral.fluffy.FluffyCombat;
import net.kyori.adventure.text.Component;
import org.bukkit.Color;
import org.bukkit.Location;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Display;
import org.bukkit.entity.Entity;
import org.bukkit.entity.TextDisplay;
import org.bukkit.util.Transformation;

public abstract class AbstractTextEffect extends Effect {
    private final boolean seeThrough;
    private final boolean shadowed;
    private final Display.Billboard billboard;
    private final Color backgroundColor;
    private final Animation animation;
    public AbstractTextEffect(FluffyCombat plugin, String name, boolean seeThrough, boolean shadowed, Display.Billboard billboard, Color backgroundColor, Animation animation) {
        super(plugin, name);
        this.seeThrough = seeThrough;
        this.shadowed = shadowed;
        this.billboard = billboard;
        this.backgroundColor = backgroundColor;
        this.animation = animation;
    }

    public abstract Component getDisplay(OfflinePlayer player, Entity entity, EffectData effectData);

    @Override
    public void run(OfflinePlayer player, Entity entity, Location location, EffectData effectData) {
        Component display = getDisplay(player, entity, effectData);
        if (display == null) return;

        // 1. Spawn the TextDisplay at full size (scale 1.0)
        TextDisplay textDisplay = location.getWorld().spawn(location, TextDisplay.class, t -> {
            t.text(display);
            t.setBillboard(billboard);
            t.setSeeThrough(seeThrough);
            if (shadowed) {
                t.setShadowed(shadowed);
            }
            t.setPersistent(false);
            if (backgroundColor != null) {
                t.setBackgroundColor(backgroundColor);
            }

            Transformation transformation = animation.getAnimationTransformation();
            Transformation inititialTransformation = animation.getInitialSize();

            if (inititialTransformation != null) {
                t.setTransformation(inititialTransformation);
            }

            // Add shrink/expand animation
            if (transformation != null) {
                t.getScheduler().runDelayed(getPlugin(), task->{
                    if (!t.isValid()) {
                        return;
                    }
                    int shrinkDuration = 19;
                    t.setInterpolationDelay(0);
                    t.setInterpolationDuration(shrinkDuration);

                    t.setTransformation(transformation);
                }, null, 3);
            }


            // Delete text display
            t.getScheduler().runDelayed(getPlugin(), task -> {
                if (t.isValid()) {
                    t.remove();
                }
            }, null, 20L);
        });
    }

    public enum Animation {
        EXPAND {
            @Override
            public Transformation getAnimationTransformation() {
                return new org.bukkit.util.Transformation(
                        new org.joml.Vector3f(0f, 0f, 0f),       // Translation
                        new org.joml.AxisAngle4f(),              // Left rotation
                        new org.joml.Vector3f(1.0f, 1.0f, 1.0f), // Target scale (normal/full size)
                        new org.joml.AxisAngle4f()               // Right rotation
                );
            }

            @Override
            public Transformation getInitialSize() {
                return new Transformation(new org.joml.Vector3f(0f, 0f, 0f),       // Translation
                new org.joml.AxisAngle4f(),              // Left rotation
                new org.joml.Vector3f(0.01f, 0.01f, 0.01f), // Initial scale (near-zero)
                new org.joml.AxisAngle4f());               // Right rotation
            }
        },
        SHRINK {
            @Override
            public Transformation getAnimationTransformation() {
                return new org.bukkit.util.Transformation(
                        new org.joml.Vector3f(0f, 0f, 0f),       // Translation
                        new org.joml.AxisAngle4f(),              // Left rotation
                        new org.joml.Vector3f(0.01f, 0.01f, 0.01f), // Target scale (near-zero)
                        new org.joml.AxisAngle4f()               // Right rotation
                );
            }
        },
        STAY

        ;

        public Transformation getAnimationTransformation() {
            return null;
        }
        public Transformation getInitialSize() {
            return null;
        }
    }
}
