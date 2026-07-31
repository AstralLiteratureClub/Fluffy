package bet.astral.fluffy.cosmetics.parser;

import bet.astral.fluffy.FluffyCombat;
import bet.astral.fluffy.cosmetics.*;
import bet.astral.fluffy.cosmetics.Effect;
import bet.astral.fluffy.cosmetics.manager.EffectManager;
import bet.astral.messenger.v2.translation.TranslationKey;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.stream.JsonReader;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.*;
import org.bukkit.entity.Display;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;

public class EffectParser {
    private final FluffyCombat fluffy;
    private final EffectManager effectManager;

    public EffectParser(FluffyCombat fluffy, EffectManager effectManager) {
        this.fluffy = fluffy;
        this.effectManager = effectManager;
    }

    public void parse(File file) {
        try {
            Gson gson = new GsonBuilder().setPrettyPrinting().create();
            JsonReader reader = gson.newJsonReader(new FileReader(file));
            JsonElement element = gson.fromJson(reader, JsonElement.class);

            if (element == null) {
                return;
            }

            element = element.getAsJsonObject().get("effects");

            if (element.isJsonArray()) {
                for (JsonElement jsonElement : element.getAsJsonArray()) {
                    if (jsonElement.isJsonObject()) {
                        ConfigurableEffect effect = parse(jsonElement.getAsJsonObject());
                        if (effect == null){
                            continue;
                        }
                        effectManager.registerEffect(effect);
                    }
                }
            }
        } catch (FileNotFoundException e) {
            throw new RuntimeException(e);
        }

    }

    public ConfigurableEffect parse(@NotNull JsonObject obj) {
        String name = obj.get("name").getAsString();
        try {
            String materialName =  obj.get("material").getAsString();
            Material material = Registry.MATERIAL.get(NamespacedKey.fromString(materialName));
            List<Effect> effects = new ArrayList<>();

            for (JsonElement element : obj.get("action").getAsJsonArray()) {
                JsonObject action = element.getAsJsonObject();

                try {
                    Type type = Type.valueOf(action.get("type").getAsString().toUpperCase());
                    Effect effect = switch (type) {
                        case PARTICLE -> parseParticleEffect(name, action);
                        case TEXT -> parseTextEffect(name, action);
                        case TRANSLATION -> parseTranslationEffect(name, action);
                    };

                    if (effect == null) {
                        throw new IllegalStateException("While parsing hit effect action received null.");
                    }

                    effects.add(effect);
                } catch (IllegalArgumentException e) {
                    fluffy.getLogger().warning("Invalid action type: " + action.get("type").getAsString().toUpperCase()+". Skipping entire hit effect: "+ name);
                }
            }

            return new ConfigurableEffect(effects,
                    fluffy,
                    name,
                    TranslationKey.of(effectManager.getName()+"."+name.toLowerCase()+".name"),
                    TranslationKey.of(effectManager.getName()+"."+name.toLowerCase()+".description"),
                    ItemStack.of(material)
                    );
        } catch (NullPointerException | IllegalStateException e) {
            fluffy.getComponentLogger().warn("Couldn't load hit effect {} skipping it.", name, e);
        }
        return null;
    }

    @Contract("_, _ -> new")
    private @NotNull ParticleEffect parseParticleEffect(String name, JsonObject obj) {
        Particle particle = Registry.PARTICLE_TYPE.get(NamespacedKey.fromString(obj.get("particle").getAsString()));
        if (particle == null) {
            throw new IllegalStateException("Unknown particle type: " + name);
        }
        int amount = getOrElse(obj.get("amount"), element->element.getAsInt(), 1);
        double spread = getOrElse(obj.get("spread"), element->element.getAsDouble(), 1D);
        double x = getOrElse(obj.get("x"), element->element.getAsDouble(), 1D);
        double y = getOrElse(obj.get("y"), element->element.getAsDouble(), 1D);
        double z = getOrElse(obj.get("z"), element->element.getAsDouble(), 1D);
        int extra = getOrElse(obj.get("extra"), element->element.getAsInt(), 1);
        String data = getOrElse(obj.get("data"), element->element.getAsString(), null);

        return new ParticleEffect(
                fluffy,
                name+"-particle-effect",
                particle,
                amount,
                x,
                y,
                z,
                extra,
                data
        );
    }

    private @Nullable TextEffect parseTextEffect(String name, JsonObject obj) {
        String value = obj.get("text").getAsString();
        if (value == null){
            return null;
        }
        Display.Billboard billboard = getOrElse(obj.get("billboard"), element-> Display.Billboard.valueOf(element.getAsString().toUpperCase()), Display.Billboard.CENTER); 
        boolean seeThrough = getOrElse(obj.get("see-through"), element-> element.getAsBoolean(), false);
        boolean shadowed = getOrElse(obj.get("shadowed"), element-> element.getAsBoolean(), true);
        Color background = parseHex(
                getOrElse(obj.get("color"), element -> element.getAsString(), null)
        );
        AbstractTextEffect.Animation animation = getOrElse(obj.get("animation"), element -> AbstractTextEffect.Animation.valueOf(element.getAsString().toUpperCase()), AbstractTextEffect.Animation.STAY);

        return new TextEffect(fluffy, name+"-text-effect", MiniMessage.miniMessage().deserialize(value), seeThrough, shadowed, billboard, background, animation);
    }

    private TextTranslationEffect parseTranslationEffect(String name, JsonObject obj) {
        String value = obj.get("translation").getAsString();
        if (value == null){
            return null;
        }
        Display.Billboard billboard = getOrElse(obj.get("billboard"), element-> Display.Billboard.valueOf(element.getAsString().toUpperCase()), Display.Billboard.CENTER);
        boolean seeThrough = getOrElse(obj.get("see-through"), element-> element.getAsBoolean(), false);
        boolean shadowed = getOrElse(obj.get("shadowed"), element-> element.getAsBoolean(), true);
        Color background = parseHex(
                getOrElse(obj.get("color"), element -> element.getAsString(), null)
        );
        AbstractTextEffect.Animation animation = getOrElse(obj.get("animation"), element -> AbstractTextEffect.Animation.valueOf(element.getAsString().toUpperCase()), AbstractTextEffect.Animation.STAY);

        return new TextTranslationEffect(fluffy, name+"-text-effect", TranslationKey.of(value), seeThrough, shadowed, billboard, background, animation);
    }

    private <T> T getOrElse(JsonElement element, Function<JsonElement, T> elementToType, T orElse) {
        if (element == null) {
            return orElse;
        }
        return elementToType.apply(element);
    }

    public static Color parseHex(String color) {
        if (color == null) {
            return null;
        }
        try {
            int red = Integer.parseInt(color.substring(1, 3), 16);
            int green = Integer.parseInt(color.substring(3, 5), 16);
            int blue = Integer.parseInt(color.substring(5, 7), 16);
            return Color.fromRGB(red, green, blue);
        } catch (NumberFormatException e) {
            FluffyCombat.getPlugin(FluffyCombat.class).getComponentLogger().warn("Couldn't parse hex color string.", new IllegalArgumentException("Couldn't parse color from hex code: "+color, e));
        }
        return null;
    }

    private JsonElement tryOrNull(JsonObject obj, String name) {
        if (obj.has(name)) {
            return obj.get(name);
        }
        return null;
    }
    enum Type {
        TRANSLATION,
        TEXT,
        PARTICLE
    }
}