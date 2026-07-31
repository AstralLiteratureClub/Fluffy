package bet.astral.fluffy.cosmetics;

import bet.astral.fluffy.FluffyCombat;
import bet.astral.fluffy.messenger.FluffyMessenger;
import org.bukkit.*;
import org.bukkit.block.data.BlockData;
import org.bukkit.entity.Entity;
import org.bukkit.inventory.ItemStack;

import static bet.astral.fluffy.cosmetics.parser.EffectParser.parseHex;

public class ParticleEffect extends Effect {
    private final Particle particle;
    private final int amount;
    private final double x;
    private final double y;
    private final double z;
    private final int extra;
    private final String data;
    private final Type type;

    public ParticleEffect(FluffyCombat plugin, String name, Particle particle, int amount, double x, double y, double z, int extra, String data) {
        super(plugin, name);
        this.particle = particle;
        this.amount = amount;
        this.x = x;
        this.y = y;
        this.z = z;
        this.extra = extra;
        this.data = data;
        type = Type.findTypeByParticle(particle);
    }

    @Override
    public void run(OfflinePlayer player, Entity entity, Location location, EffectData effectData) {
        Object data = type.createData(this.data);
        location.getWorld().spawnParticle(particle, location, amount, x, y, z, extra, data);
    }

    @Override
    public void loadTranslations(FluffyMessenger messenger) {

    }

    private enum Type {
        NORMAL(null),
        BLOCK(BlockData.class) {
            public Object createData(String data) {
                String type = data.split(",")[0];
                Material material = Material.matchMaterial(type);
                if (material == null) {
                    return null;
                } else {
                    return material.createBlockData();
                }
            }
        },
        ITEM(ItemStack.class) {
            public Object createData(String data) {
                String type = data.split(",")[0];
                Material material = Material.matchMaterial(type);
                if (material == null) {
                    return null;
                }
                return ItemStack.of(material);
            }
        },
        COLOR(Color.class) {
            public Object createData(String data) {
                String colorStr = data.split(",")[0];
                int color = Integer.parseInt(colorStr);
                return Color.fromRGB(color);
            }
        },
        DUST(Particle.DustOptions.class) {
            public Object createData(String data) {
                String[] dataSplit = data.split(",");

                // Color
                Color colorBk = parseHex(dataSplit[0]);

                // Size
                int size = Integer.parseInt(dataSplit[1]);

                return new Particle.DustOptions(colorBk, size);
            }
        },
        DUST_TRANSITION(Particle.DustTransition.class) {
            public Object createData(String data) {
                String[] dataSplit = data.split(",");
                // Color & Color to transition to
                Color colorBk = parseHex(dataSplit[0]);
                Color colorBk2 = parseHex(dataSplit[1]);

                int size = Integer.parseInt(dataSplit[2]);

                return new Particle.DustTransition(colorBk, colorBk2, size);
            }
        },
        VIBRATION(Vibration.class) {
            public Object createData(String data) {
                throw new IllegalStateException("Vibration particles are not supported in hit effects!");
            }
        },
        FLOAT(float.class) {
            public Object createData(String data) {
                return Float.parseFloat(data);
            }
        },
        INT(int.class) {
            public Object createData(String data) {
                return Integer.parseInt(data);
            }
        },
        ;

        private Class clazz;
        Type(Class<?> clazz) {
            this.clazz = clazz;
        }

        public static Type findTypeByParticle(Particle particle) {
            for (Type type : Type.values()) {
                if (type.clazz == particle.getDataType()) {
                    return type;
                }
            }
            return NORMAL;
        }

        public Object createData(String extra) {
            return null;
        }
    }
}
