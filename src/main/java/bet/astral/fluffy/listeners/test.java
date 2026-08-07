package bet.astral.fluffy.listeners;

import bet.astral.fluffy.FluffyCombat;
import bet.astral.guiman.clickable.Clickable;
import bet.astral.guiman.gui.InventoryGUI;
import bet.astral.messenger.v2.placeholder.collection.PlaceholderList;
import io.papermc.paper.datacomponent.DataComponentTypes;
import io.papermc.paper.datacomponent.item.ItemLore;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.block.data.BlockData;
import org.bukkit.entity.*;
import org.bukkit.event.EventHandler;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEntityEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.BoundingBox;
import org.bukkit.util.RayTraceResult;
import org.bukkit.util.Transformation;
import org.bukkit.util.Vector;
import org.jetbrains.annotations.ApiStatus;
import org.joml.Quaternionf;
import org.joml.Vector3f;

import java.util.*;

public class test /*implements Listener*/ {
    private final Map<UUID, Long> cooldowns = new HashMap<>();
    private final Map<UUID, Integer> colorIndices = new HashMap<>();

    // ---- Tweakable settings ----
    private static final long COOLDOWN_MS = 0;            // time between shots
    private static final double MAX_DISTANCE = 40.0;        // max ray length
    private static final float THICKNESS = 0.12f;           // beam cross-section size
    private static final double HEAD_SEPARATION = 0.22;     // distance from center to right/left lasers

    /**
     * Enhanced profile defining unique statistics, powers, status effects, knockback, audio, and visual design patterns for each laser variant.
     */
    public record LaserProfile(
            Material material,
            String name,
            double damage,
            float explosionPower,
            boolean heals,
            boolean setsFire,
            boolean strikesLightning,
            boolean isSpiral,
            boolean isDoubleHelix,
            boolean isWave,
            boolean isPulsar,
            double knockbackPower,
            PotionEffect statusEffect,
            Sound fireSound,
            float soundPitch,
            String description
    ) {}

    private record BeamTraceResult(Location origin, Vector direction, double distance, LivingEntity hitEntity, Location impactLocation) {}

    public enum LaserCategory {
        ELEMENTAL("Elemental & Atmospheric", Material.MAGMA_BLOCK, 0, 10, NamedTextColor.AQUA, "High-velocity wind, thermal flames, freezing cryo, and cascading waves."),
        COSMIC("Cosmic & Void", Material.CRYING_OBSIDIAN, 10, 20, NamedTextColor.LIGHT_PURPLE, "Gravitational singularities, pinball ricochets, stellar beams, and dark matter."),
        MAGICAL("Magical & Arcane", Material.AMETHYST_BLOCK, 20, 30, NamedTextColor.GREEN, "Restorative mana surges, soul harvesting, hex curses, and mystic runes."),
        FUSION("Fusion & Glass-Infused", Material.POINTED_DRIPSTONE, 30, 45, NamedTextColor.GOLD, "Rugged stone-glass fusions, geode resonance, and bouncy slime shockwaves."),
        CYBERNETIC("Cybernetic & Biological", Material.REDSTONE_BLOCK, 45, 55, NamedTextColor.RED, "EMP disruptors, plasma cutters, nanite swarms, and hyper-drive slugs."),
        MYTHIC("Mythic & Refractive", Material.BEACON, 55, 57, NamedTextColor.YELLOW, "Prismatic splitters and ultimate celestial singularity core beams.");

        private final String title;
        private final Material icon;
        private final int startIndex;
        private final int endIndex;
        private final NamedTextColor color;
        private final String description;

        LaserCategory(String title, Material icon, int startIndex, int endIndex, NamedTextColor color, String description) {
            this.title = title;
            this.icon = icon;
            this.startIndex = startIndex;
            this.endIndex = endIndex;
            this.color = color;
            this.description = description;
        }

        public String getTitle() { return title; }
        public Material getIcon() { return icon; }
        public int getStartIndex() { return startIndex; }
        public int getEndIndex() { return endIndex; }
        public NamedTextColor getColor() { return color; }
        public String getDescription() { return description; }
    }

    // Complete collection featuring expanded laser concepts including glass-stone fusions, hybrid designs, and mythic ricochet profiles
    private static final LaserProfile[] LASER_PROFILES = {
            // ---- ELEMENTAL & ATMOSPHERIC LASERS (1–10) ----
            new LaserProfile(
                    Material.SNOW_BLOCK, "Zephyr Gale", 3.0, 0.0f, false, false, false, false, false, true, false, 2.0,
                    new PotionEffect(PotionEffectType.SLOW_FALLING, 80, 1), Sound.BLOCK_WOOL_STEP, 1.8f,
                    "A high-velocity undulating wind beam that knocks enemies far backward."
            ),
            new LaserProfile(
                    Material.MAGMA_BLOCK, "Magma Core", 6.0, 1.5f, false, true, false, false, false, false, false, 0.5,
                    new PotionEffect(PotionEffectType.BLINDNESS, 60, 0), Sound.ITEM_FIRECHARGE_USE, 1.0f,
                    "A scorching thermal ray that sets targets ablaze and triggers minor explosions."
            ),
            new LaserProfile(
                    Material.PACKED_ICE, "Absolute Zero", 4.0, 0.0f, false, false, false, false, false, false, false, 0.3,
                    new PotionEffect(PotionEffectType.SLOWNESS, 100, 2), Sound.ENTITY_PLAYER_HURT_FREEZE, 1.2f,
                    "A freezing cryogenic beam that inflicts heavy slowness and freezing visuals."
            ),
            new LaserProfile(
                    Material.LIGHTNING_ROD, "Tesla Core Spheres", 9.0, 0.0f, false, false, true, false, false, false, true, 1.2,
                    null, Sound.ENTITY_LIGHTNING_BOLT_THUNDER, 0.8f,
                    "A high-voltage ray ending in crackling energy spheres that discharge chain lightning."
            ),
            new LaserProfile(
                    Material.PRISMARINE, "Tsunami Surge", 5.0, 0.0f, false, false, false, false, false, true, false, 1.2,
                    new PotionEffect(PotionEffectType.SLOWNESS, 80, 1), Sound.ENTITY_DROWNED_SHOOT, 1.0f,
                    "A high-pressure waving water beam that drags down enemy momentum."
            ),
            new LaserProfile(
                    Material.GLOWSTONE, "Solar Flare", 5.0, 0.0f, false, false, false, false, false, false, true, 0.4,
                    new PotionEffect(PotionEffectType.BLINDNESS, 80, 1), Sound.ENTITY_FIREWORK_ROCKET_BLAST, 1.6f,
                    "An intensely bright pulsing energy beam that blinds opponents."
            ),
            new LaserProfile(
                    Material.MUD, "Seismic Rupture", 7.0, 3.0f, false, false, false, false, false, false, false, 1.5,
                    new PotionEffect(PotionEffectType.SLOWNESS, 60, 3), Sound.ENTITY_ZOMBIE_BREAK_WOODEN_DOOR, 0.6f,
                    "A heavy ground-shaking shockwave beam that cracks the earth beneath targets."
            ),
            new LaserProfile(
                    Material.ORANGE_CONCRETE, "Plasma Arc", 6.5, 2.0f, false, true, false, false, false, true, false, 0.6,
                    null, Sound.ENTITY_GENERIC_EXPLODE, 0.9f,
                    "A superheated snake-wave energy beam with continuous thermal burn damage."
            ),
            new LaserProfile(
                    Material.WHITE_WOOL, "Cyclone Helix", 4.0, 0.0f, false, false, false, true, false, false, false, 1.4,
                    new PotionEffect(PotionEffectType.SLOW_FALLING, 80, 1), Sound.BLOCK_WOOL_STEP, 1.5f,
                    "A rotating helical air current that lifts targets into low gravity."
            ),
            new LaserProfile(
                    Material.HEAVY_WEIGHTED_PRESSURE_PLATE, "Atmospheric Crush", 10.0, 0.0f, false, false, false, false, false, false, false, 1.8,
                    new PotionEffect(PotionEffectType.WEAKNESS, 80, 1), Sound.BLOCK_ANVIL_LAND, 0.5f,
                    "A gravity-dense beam simulating crushing weight."
            ),

            // ---- COSMIC & VOID LASERS (11–20) ----
            new LaserProfile(
                    Material.CRYING_OBSIDIAN, "Quantum Orbiter", 15.0, 4.0f, false, true, false, false, false, false, true, 2.2,
                    new PotionEffect(PotionEffectType.WITHER, 60, 1), Sound.ENTITY_WITHER_SHOOT, 0.5f,
                    "Terminates in a spinning dark-matter energy sphere that pulls and implodes on targets."
            ),
            new LaserProfile(
                    Material.GOLD_BLOCK, "Pinball Ricochet", 12.0, 1.0f, false, false, false, false, false, false, true, 1.5,
                    new PotionEffect(PotionEffectType.GLOWING, 80, 0), Sound.ENTITY_EXPERIENCE_ORB_PICKUP, 1.4f,
                    "A high-speed golden ray that bounces off walls up to 5 times, leaving glowing terminal orbs."
            ),
            new LaserProfile(
                    Material.BLACK_CONCRETE, "Event Horizon", 16.0, 1.0f, false, false, false, false, false, false, true, 2.5,
                    new PotionEffect(PotionEffectType.LEVITATION, 40, 1), Sound.ENTITY_WITHER_SHOOT, 0.4f,
                    "A gravitational singularity beam pulsing with immense inward pull."
            ),
            new LaserProfile(
                    Material.END_ROD, "Starlight Beam", 9.0, 0.0f, false, false, false, false, false, false, false, 0.8,
                    new PotionEffect(PotionEffectType.GLOWING, 80, 0), Sound.ENTITY_ARROW_SHOOT, 2.0f,
                    "A piercing, long-range beam of pure stellar luminescence."
            ),
            new LaserProfile(
                    Material.NETHERITE_BLOCK, "Dark Matter Pulsar", 13.0, 2.0f, false, false, false, false, false, false, true, 1.6,
                    new PotionEffect(PotionEffectType.WEAKNESS, 100, 1), Sound.ENTITY_IRON_GOLEM_ATTACK, 0.7f,
                    "A heavy armor-piercing kinetic void slug with pulsing impact waves."
            ),
            new LaserProfile(
                    Material.BASALT, "Meteor Shower", 15.0, 5.0f, false, true, false, false, false, false, false, 2.0,
                    new PotionEffect(PotionEffectType.FIRE_RESISTANCE, 100, 0), Sound.ENTITY_ENDER_DRAGON_GROWL, 0.5f,
                    "Summons falling cosmic debris and burning impact craters."
            ),
            new LaserProfile(
                    Material.TINTED_GLASS, "Solar Eclipse", 7.5, 0.0f, false, false, false, false, false, false, false, 0.5,
                    new PotionEffect(PotionEffectType.BLINDNESS, 100, 1), Sound.ENTITY_ELDER_GUARDIAN_CURSE, 1.2f,
                    "A shadow-weaving beam that plunges targets into absolute darkness."
            ),
            new LaserProfile(
                    Material.LODESTONE, "Gravity Well", 8.0, 0.0f, false, false, false, false, false, false, false, 1.5,
                    new PotionEffect(PotionEffectType.LEVITATION, 60, 1), Sound.ENTITY_ENDERMAN_TELEPORT, 0.8f,
                    "Imbues targets with a sudden levitation and pulling force."
            ),
            new LaserProfile(
                    Material.QUARTZ_BLOCK, "Comet Tail", 6.0, 0.0f, false, false, false, false, false, true, false, 0.7,
                    new PotionEffect(PotionEffectType.SPEED, 60, 0), Sound.ENTITY_FIREWORK_ROCKET_BLAST, 1.8f,
                    "A high-speed undulating trailing ray leaving frozen stardust particles."
            ),
            new LaserProfile(
                    Material.RESPAWN_ANCHOR, "Quantum Singularity", 18.0, 3.0f, false, false, false, false, false, false, true, 2.0,
                    new PotionEffect(PotionEffectType.WITHER, 80, 1), Sound.ENTITY_ENDERMAN_TELEPORT, 0.6f,
                    "A devastating single-target disintegration energy beam with rapid light pulses."
            ),

            // ---- MAGICAL & ARCANE LASERS (21–30) ----
            new LaserProfile(
                    Material.AMETHYST_BLOCK, "Mana Surge", -8.0, 0.0f, true, false, false, false, false, false, true, 0.0,
                    new PotionEffect(PotionEffectType.REGENERATION, 80, 1), Sound.ENTITY_PLAYER_LEVELUP, 1.5f,
                    "A restorative magical beam pulsing with healing energy."
            ),
            new LaserProfile(
                    Material.SOUL_SAND, "Hex Curse", 7.0, 0.0f, false, false, false, false, false, true, false, 0.4,
                    new PotionEffect(PotionEffectType.WITHER, 80, 1), Sound.ENTITY_WITHER_AMBIENT, 0.8f,
                    "Inflicts decaying wither and lingering mystical weakness through a waving shadow beam."
            ),
            new LaserProfile(
                    Material.GOLD_BLOCK, "Divine Smite", 11.0, 0.0f, false, false, true, false, false, false, false, 1.2,
                    new PotionEffect(PotionEffectType.GLOWING, 100, 0), Sound.ENTITY_LIGHTNING_BOLT_THUNDER, 1.4f,
                    "Holy golden light that deals heavy damage to hostile mobs."
            ),
            new LaserProfile(
                    Material.PURPUR_BLOCK, "Ender Blink", 6.0, 0.0f, false, false, false, false, false, false, false, 1.0,
                    new PotionEffect(PotionEffectType.NAUSEA, 60, 0), Sound.ENTITY_ENDERMAN_TELEPORT, 1.2f,
                    "A teleportation-infused beam that disorients targets on impact."
            ),
            new LaserProfile(
                    Material.SOUL_SOIL, "Soul Harvester", -10.0, 0.0f, true, false, false, false, false, false, false, 0.0,
                    new PotionEffect(PotionEffectType.ABSORPTION, 100, 1), Sound.ENTITY_WARDEN_HEARTBEAT, 0.9f,
                    "A necromantic ray that drains life force from the target."
            ),
            new LaserProfile(
                    Material.ENCHANTING_TABLE, "Enchanted Rune", 8.5, 0.0f, false, false, false, false, true, false, false, 0.6,
                    new PotionEffect(PotionEffectType.MINING_FATIGUE, 80, 1), Sound.BLOCK_ENCHANTMENT_TABLE_USE, 1.0f,
                    "Fires random volatile magical double-helix debuffs with every strike."
            ),
            new LaserProfile(
                    Material.WHITE_STAINED_GLASS, "Phantasmal Ray", 5.5, 0.0f, false, false, false, false, false, false, false, 0.3,
                    new PotionEffect(PotionEffectType.INVISIBILITY, 80, 0), Sound.ENTITY_PHANTOM_BITE, 1.3f,
                    "A ghost-like phase-shifting beam that bypasses standard blocks."
            ),
            new LaserProfile(
                    Material.RED_NETHER_BRICKS, "Warlock's Grasp", 8.0, 0.0f, false, false, false, false, false, false, false, -1.0,
                    new PotionEffect(PotionEffectType.SLOWNESS, 80, 1), Sound.ENTITY_EVOKER_CAST_SPELL, 0.8f,
                    "A dark thread beam that pulls targets directly toward the caster."
            ),
            new LaserProfile(
                    Material.DIAMOND_BLOCK, "Prismatic Shield", -12.0, 0.0f, true, false, false, false, false, false, false, 0.0,
                    new PotionEffect(PotionEffectType.RESISTANCE, 100, 1), Sound.BLOCK_BEACON_ACTIVATE, 1.4f,
                    "A defensive support beam granting golden absorption shields."
            ),
            new LaserProfile(
                    Material.SCULK_SHRIEKER, "Banshee Wail", 9.0, 0.0f, false, false, false, false, false, true, true, 2.2,
                    new PotionEffect(PotionEffectType.DARKNESS, 60, 0), Sound.ENTITY_WARDEN_SONIC_BOOM, 1.0f,
                    "A disorienting sonic frequency wave echoing with aggressive pulses."
            ),

            // ---- FUSION & GLASS-INFUSED DESIGN IDEAS (31–45) ----
            new LaserProfile(
                    Material.TUFF, "Tuff-Fused Glass", 8.5, 1.0f, false, false, false, false, false, true, false, 1.1,
                    new PotionEffect(PotionEffectType.SLOWNESS, 80, 1), Sound.BLOCK_TUFF_BREAK, 0.9f,
                    "A rugged beam of reinforced stone and refractive glass shards."
            ),
            new LaserProfile(
                    Material.POINTED_DRIPSTONE, "Dripstone Spike", 9.0, 0.0f, false, false, false, false, true, false, false, 1.3,
                    new PotionEffect(PotionEffectType.MINING_FATIGUE, 80, 1), Sound.BLOCK_POINTED_DRIPSTONE_LAND, 1.1f,
                    "A sharp jagged beam combining hard stone and piercing stalactite shards."
            ),
            new LaserProfile(
                    Material.GILDED_BLACKSTONE, "Gilded Obsidian", 12.0, 2.0f, false, true, false, false, false, false, true, 1.8,
                    new PotionEffect(PotionEffectType.WEAKNESS, 80, 1), Sound.BLOCK_GILDED_BLACKSTONE_STEP, 0.7f,
                    "An opulent dark stone beam threaded with blazing gold veins."
            ),
            new LaserProfile(
                    Material.WEATHERED_COPPER, "Oxidized Spark", 7.0, 0.0f, false, false, true, false, false, true, false, 0.9,
                    new PotionEffect(PotionEffectType.SLOWNESS, 60, 0), Sound.ITEM_AXE_SCRAPE, 1.4f,
                    "A weathered copper-green electrical discharge beam."
            ),
            new LaserProfile(
                    Material.BUDDING_AMETHYST, "Geode Resonance", 8.0, 0.0f, false, false, false, true, false, false, true, 0.5,
                    new PotionEffect(PotionEffectType.GLOWING, 100, 0), Sound.BLOCK_AMETHYST_CLUSTER_BREAK, 1.5f,
                    "A crystalline frequency beam pulsing with raw geode energy."
            ),
            new LaserProfile(
                    Material.CALCITE, "Calcite Prism", 6.0, 0.0f, false, false, false, false, true, false, false, 0.4,
                    new PotionEffect(PotionEffectType.BLINDNESS, 60, 0), Sound.BLOCK_CALCITE_BREAK, 1.3f,
                    "A dazzling double-helix beam of polished calcite and mineral glass."
            ),
            new LaserProfile(
                    Material.DRIPSTONE_BLOCK, "Stalagmite Core", 10.0, 1.5f, false, false, false, false, false, false, true, 1.6,
                    new PotionEffect(PotionEffectType.WEAKNESS, 80, 1), Sound.BLOCK_STONE_BREAK, 0.8f,
                    "A dense stone-infused energy pulse that shatters armor upon contact."
            ),
            new LaserProfile(
                    Material.SEA_LANTERN, "Prismarine Glass", 7.0, 0.0f, false, false, false, false, false, true, true, 0.8,
                    new PotionEffect(PotionEffectType.GLOWING, 100, 0), Sound.BLOCK_GLASS_STEP, 1.2f,
                    "An oceanic translucent beam combining glowing sea lanterns and sea glass."
            ),
            new LaserProfile(
                    Material.MUD_BRICKS, "Terran Glass", 7.5, 0.5f, false, false, false, false, false, false, false, 1.0,
                    new PotionEffect(PotionEffectType.SLOWNESS, 80, 1), Sound.BLOCK_MUD_BRICKS_BREAK, 0.7f,
                    "A sturdy composite beam of baked earthen brick and hardened silicate."
            ),
            new LaserProfile(
                    Material.PACKED_MUD, "Earthen Shard", 8.0, 0.0f, false, false, false, true, false, false, false, 1.2,
                    new PotionEffect(PotionEffectType.MINING_FATIGUE, 80, 1), Sound.BLOCK_MUD_STEP, 0.8f,
                    "A spiraling coil of compacted earth and crystalline shards."
            ),
            new LaserProfile(
                    Material.CHISELED_STONE_BRICKS, "Ancient Monolith", 11.0, 2.0f, false, false, false, false, false, false, true, 1.7,
                    new PotionEffect(PotionEffectType.WEAKNESS, 100, 1), Sound.BLOCK_STONE_BREAK, 0.6f,
                    "A heavy stone-carved beam radiating ancient protective magic."
            ),
            new LaserProfile(
                    Material.SMOOTH_BASALT, "Basalt Glass", 9.5, 1.0f, false, true, false, false, false, true, false, 1.3,
                    new PotionEffect(PotionEffectType.SLOWNESS, 60, 1), Sound.BLOCK_BASALT_BREAK, 0.9f,
                    "A sleek volcanic pillar beam formed from smooth basalt and molten glass."
            ),
            new LaserProfile(
                    Material.SLIME_BLOCK, "Slime Bounce-Shot", 7.0, 0.5f, false, false, false, false, false, true, false, 2.5,
                    new PotionEffect(PotionEffectType.JUMP_BOOST, 100, 2), Sound.ENTITY_SLIME_JUMP, 1.2f,
                    "A bouncy, squishy green wave ray that bounces off walls and launches entities skyward."
            ),
            new LaserProfile(
                    Material.COBBLESTONE, "Cobble-Shard Beam", 8.0, 0.0f, false, false, false, false, false, false, false, 1.1,
                    new PotionEffect(PotionEffectType.SLOWNESS, 60, 0), Sound.BLOCK_STONE_BREAK, 1.0f,
                    "A rugged projectile beam packed with rough cobblestone fragments."
            ),
            new LaserProfile(
                    Material.MOSSY_COBBLESTONE, "Overgrown Crystal", 7.5, 0.0f, false, false, false, false, false, true, false, 0.9,
                    new PotionEffect(PotionEffectType.POISON, 80, 1), Sound.BLOCK_VINE_STEP, 1.1f,
                    "An organic vine-wrapped stone beam dripping with toxic moss spores."
            ),

            // ---- CYBERNETIC & BIOLOGICAL LASERS (46–55) ----
            new LaserProfile(
                    Material.IRON_BARS, "EMP Disruptor", 6.0, 0.0f, false, false, false, false, false, false, true, 1.1,
                    new PotionEffect(PotionEffectType.BLINDNESS, 60, 0), Sound.BLOCK_ANVIL_DESTROY, 1.5f,
                    "A technological pulse wave that scrambles enemy positioning and movement."
            ),
            new LaserProfile(
                    Material.IRON_BLOCK, "Laser Grid", 7.5, 0.0f, false, false, false, false, false, false, false, 0.8,
                    new PotionEffect(PotionEffectType.SLOWNESS, 80, 1), Sound.ENTITY_IRON_GOLEM_STEP, 1.2f,
                    "A multi-point restrictive security beam trap."
            ),
            new LaserProfile(
                    Material.REDSTONE_BLOCK, "Plasma Cutter", 9.5, 1.0f, false, true, false, false, false, false, false, 0.7,
                    new PotionEffect(PotionEffectType.WEAKNESS, 60, 0), Sound.ITEM_FIRECHARGE_USE, 1.1f,
                    "High-precision industrial cutting laser designed to melt armor."
            ),
            new LaserProfile(
                    Material.HONEYCOMB_BLOCK, "Nanite Swarm", 5.0, 0.0f, false, false, false, false, false, false, false, 0.2,
                    new PotionEffect(PotionEffectType.POISON, 80, 1), Sound.BLOCK_BEEHIVE_WORK, 1.3f,
                    "Micro-machine filaments that eat away at target vitality over time."
            ),
            new LaserProfile(
                    Material.COPPER_BLOCK, "Hyper-Drive Slug", 11.0, 0.0f, false, false, false, false, false, false, true, 1.7,
                    new PotionEffect(PotionEffectType.SPEED, 60, 1), Sound.ENTITY_ARROW_SHOOT, 1.9f,
                    "Ultra-fast mechanical kinetic projectile with blinking tachyon pulses."
            ),
            new LaserProfile(
                    Material.GLASS, "Holographic Decoy", 3.0, 0.0f, false, false, false, false, false, true, false, 0.1,
                    new PotionEffect(PotionEffectType.INVISIBILITY, 60, 0), Sound.BLOCK_GLASS_BREAK, 1.6f,
                    "A refractive waving light beam that creates deceptive visual trails."
            ),
            new LaserProfile(
                    Material.END_STONE, "Tachyon Pulse", 6.5, 0.0f, false, false, false, false, false, false, true, 0.9,
                    new PotionEffect(PotionEffectType.SPEED, 80, 1), Sound.ENTITY_ENDERMAN_TELEPORT, 1.4f,
                    "A time-distortion beam granting temporary speed boosts with light pulses."
            ),
            new LaserProfile(
                    Material.CALIBRATED_SCULK_SENSOR, "Sonar Ping", 4.0, 0.0f, false, false, false, false, false, false, true, 0.3,
                    new PotionEffect(PotionEffectType.GLOWING, 100, 0), Sound.BLOCK_SCULK_SPREAD, 1.0f,
                    "A high-frequency pulsing pulse that reveals hidden entities through walls."
            ),
            new LaserProfile(
                    Material.SLIME_BLOCK, "Reactor Meltdown", 8.0, 2.0f, false, false, false, false, false, true, false, 1.2,
                    new PotionEffect(PotionEffectType.POISON, 100, 2), Sound.ENTITY_SLIME_JUMP, 0.7f,
                    "A toxic green radioactive wave undulating with radiation sickness."
            ),
            new LaserProfile(
                    Material.QUARTZ_BLOCK, "Cybernetic Overclock", 7.0, 0.0f, false, false, false, false, false, false, false, 0.6,
                    new PotionEffect(PotionEffectType.HASTE, 100, 1), Sound.BLOCK_NOTE_BLOCK_CHIME, 1.5f,
                    "A high-frequency tuning beam optimizing combat reflexes."
            ),

            // ---- MYTHIC & REFRACTIVE LASERS (56–57) ----
            new LaserProfile(
                    Material.PRISMARINE_BRICKS, "Prismatic Splitter", 9.0, 1.0f, false, false, false, true, false, false, true, 1.4,
                    new PotionEffect(PotionEffectType.GLOWING, 100, 0), Sound.BLOCK_AMETHYST_BLOCK_RESONATE, 1.6f,
                    "A mythic refractive beam that bounces off walls and leaves terminal energy orbs."
            ),
            new LaserProfile(
                    Material.BEACON, "Celestial Singularity", 20.0, 5.0f, false, true, false, false, false, false, true, 3.0,
                    new PotionEffect(PotionEffectType.WITHER, 100, 2), Sound.ENTITY_WITHER_SPAWN, 0.4f,
                    "The ultimate celestial weapon emitting blinding light pulses and massive void blasts."
            )
    };

    private final FluffyCombat fluffy;

    public test(FluffyCombat fluffy) {
        this.fluffy = fluffy;
    }

    private BlockData getBlockData(LaserProfile profile) {
        Material mat = profile.material();
        if (mat == null || !mat.isBlock()) {
            if (mat == Material.PRISMARINE_SHARD) return Material.PRISMARINE_BRICKS.createBlockData();
            if (mat == Material.NETHER_STAR) return Material.BEACON.createBlockData();
            return Material.RED_STAINED_GLASS.createBlockData();
        }
        return mat.createBlockData();
    }

    @EventHandler
    public void onInteract(PlayerInteractEvent event) {
        if (event.getHand() == EquipmentSlot.OFF_HAND) return;

        Action action = event.getAction();
        Player player = event.getPlayer();

        if (action == Action.LEFT_CLICK_AIR || action == Action.LEFT_CLICK_BLOCK) {
            openCategoryMenu(player);
            event.setCancelled(true);
            return;
        }

        if (action == Action.RIGHT_CLICK_AIR || action == Action.RIGHT_CLICK_BLOCK) {
            UUID id = player.getUniqueId();
            long now = System.currentTimeMillis();

            Long last = cooldowns.get(id);
            if (last != null && now - last < COOLDOWN_MS) return;
            cooldowns.put(id, now);

            fireLaser(player);
        }
    }

    @EventHandler
    public void onInteractEntity(PlayerInteractEntityEvent event) {
        if (event.getHand() == EquipmentSlot.OFF_HAND) return;

        Entity clicked = event.getRightClicked();
        if (!(clicked instanceof LivingEntity target)) return;

        Player player = event.getPlayer();
        UUID id = player.getUniqueId();
        long now = System.currentTimeMillis();

        Long last = cooldowns.get(id);
        if (last != null && now - last < COOLDOWN_MS) return;
        cooldowns.put(id, now);

        fireLaserAtEntity(player, target);
        event.setCancelled(true);
    }

    @ApiStatus.Internal
    public void openCategoryMenu(Player player) {
        PlaceholderList placeholders = new PlaceholderList();
        placeholders.add("player", player.getName());

        var builder = InventoryGUI.builder(4)
                .messenger(fluffy.getMessenger())
                .placeholderGenerator(p -> placeholders)
                .title(Component.text("Laser Categories ✦ Select Arsenal", NamedTextColor.DARK_AQUA, TextDecoration.BOLD));

        // Category slot placement layout in 4-row GUI
        int[] slots = {10, 12, 14, 16, 20, 24};
        LaserCategory[] categories = LaserCategory.values();

        for (int i = 0; i < categories.length && i < slots.length; i++) {
            LaserCategory cat = categories[i];
            ItemStack item = ItemStack.of(cat.getIcon());
            item.setData(DataComponentTypes.ITEM_NAME, Component.text(cat.getTitle(), cat.getColor(), TextDecoration.BOLD));

            int profileCount = cat.getEndIndex() - cat.getStartIndex();

            item.setData(DataComponentTypes.LORE, ItemLore.lore()
                    .addLine(Component.text(cat.getDescription(), NamedTextColor.GRAY))
                    .addLine(Component.empty())
                    .addLine(Component.text("📦 Profiles Available: ", NamedTextColor.YELLOW).append(Component.text(profileCount, NamedTextColor.WHITE)))
                    .addLine(Component.empty())
                    .addLine(Component.text("▶ Click to Browse Category", NamedTextColor.GREEN, TextDecoration.ITALIC))
                    .build());

            builder.addClickable(slots[i], Clickable.general(item, context -> {
                player.playSound(player.getLocation(), Sound.BLOCK_NOTE_BLOCK_CHIME, 0.5f, 1.4f);
                openLaserMenu(player, cat, 0);
            }));
        }

        // Decorative / Information buttons
        ItemStack cyanPane = ItemStack.of(Material.CYAN_STAINED_GLASS_PANE);
        cyanPane.setData(DataComponentTypes.ITEM_NAME, Component.text(" "));
        for (int s = 0; s < 36; s++) {
            if (s != 10 && s != 12 && s != 14 && s != 16 && s != 20 && s != 24 && s != 31) {
                builder.addClickable(s, Clickable.general(cyanPane, context -> {}));
            }
        }

        ItemStack closeItem = ItemStack.of(Material.BARRIER);
        closeItem.setData(DataComponentTypes.ITEM_NAME, Component.text("Close Menu", NamedTextColor.RED, TextDecoration.BOLD));
        closeItem.setData(DataComponentTypes.LORE, ItemLore.lore().addLine(Component.text("Exit the laser configuration menu.", NamedTextColor.GRAY)).build());
        builder.addClickable(31, Clickable.general(closeItem, context -> {
            player.playSound(player.getLocation(), Sound.BLOCK_PISTON_CONTRACT, 0.5f, 0.8f);
            Bukkit.getScheduler().runTask(fluffy, () -> player.closeInventory());
        }));

        builder.build().open(player);
    }

    @ApiStatus.Internal
    public void openLaserMenu(Player player, LaserCategory category, int page) {
        PlaceholderList placeholders = new PlaceholderList();
        placeholders.add("player", player.getName());

        final int itemsPerPage = 45;
        int totalProfilesInCat = category.getEndIndex() - category.getStartIndex();
        final int maxPages = (totalProfilesInCat + itemsPerPage - 1) / itemsPerPage;
        final int currentPage = Math.max(0, Math.min(page, maxPages - 1));

        var builder = InventoryGUI.builder(6)
                .messenger(fluffy.getMessenger())
                .placeholderGenerator(p -> placeholders)
                .title(Component.text(category.getTitle() + " (" + (currentPage + 1) + "/" + maxPages + ")", category.getColor(), TextDecoration.BOLD));

        int startIndex = category.getStartIndex() + (currentPage * itemsPerPage);
        int endIndex = Math.min(startIndex + itemsPerPage, category.getEndIndex());

        for (int i = startIndex; i < endIndex; i++) {
            LaserProfile profile = LASER_PROFILES[i];
            final int index = i;
            int slotInGui = i - startIndex;

            ItemStack item = ItemStack.of(profile.material());
            item.setData(DataComponentTypes.ITEM_NAME, Component.text(profile.name(), NamedTextColor.AQUA, TextDecoration.BOLD));

            var loreBuilder = ItemLore.lore()
                    .addLine(Component.text(profile.description(), NamedTextColor.GRAY))
                    .addLine(Component.empty())
                    .addLine(Component.text("⚡ Damage / Power: ", NamedTextColor.YELLOW).append(Component.text(profile.damage(), NamedTextColor.WHITE)));

            if (profile.explosionPower() > 0) {
                loreBuilder.addLine(Component.text("💥 Explosion Radius: ", NamedTextColor.RED).append(Component.text(profile.explosionPower(), NamedTextColor.WHITE)));
            }
            if (profile.heals()) {
                loreBuilder.addLine(Component.text("✨ Effect: ", NamedTextColor.GREEN).append(Component.text("Healing & Regeneration", NamedTextColor.WHITE)));
            }
            if (profile.setsFire()) {
                loreBuilder.addLine(Component.text("🔥 Effect: ", NamedTextColor.GOLD).append(Component.text("Incendiary Burn", NamedTextColor.WHITE)));
            }
            if (profile.strikesLightning()) {
                loreBuilder.addLine(Component.text("⚡ Effect: ", NamedTextColor.YELLOW).append(Component.text("Lightning Strike Beam", NamedTextColor.WHITE)));
            }
            if (profile.isSpiral()) {
                loreBuilder.addLine(Component.text("🌀 Effect: ", NamedTextColor.LIGHT_PURPLE).append(Component.text("Dual Rotating Spiral", NamedTextColor.WHITE)));
            }
            if (profile.isDoubleHelix()) {
                loreBuilder.addLine(Component.text("🧬 Effect: ", NamedTextColor.LIGHT_PURPLE).append(Component.text("Double Helix Weave", NamedTextColor.WHITE)));
            }
            if (profile.isWave()) {
                loreBuilder.addLine(Component.text("〰️ Effect: ", NamedTextColor.AQUA).append(Component.text("Oscillating Sine Wave", NamedTextColor.WHITE)));
            }
            if (profile.isPulsar()) {
                loreBuilder.addLine(Component.text("🔆 Effect: ", NamedTextColor.YELLOW).append(Component.text("Pulsar Energy Ring Wave", NamedTextColor.WHITE)));
            }
            if (profile.name().contains("Ricochet") || profile.name().contains("Splitter") || profile.name().contains("Orbiter")) {
                loreBuilder.addLine(Component.text("🔄 Effect: ", NamedTextColor.LIGHT_PURPLE).append(Component.text("Surface Bouncing & Terminal Orbs", NamedTextColor.WHITE)));
            }
            if (profile.knockbackPower() > 0) {
                loreBuilder.addLine(Component.text("💨 Knockback Power: ", NamedTextColor.BLUE).append(Component.text(profile.knockbackPower(), NamedTextColor.WHITE)));
            }
            if (profile.statusEffect() != null) {
                String effectName = profile.statusEffect().getType().getKey().getKey().replace('_', ' ');
                loreBuilder.addLine(Component.text("🧪 Status Effect: ", NamedTextColor.DARK_PURPLE).append(Component.text(effectName, NamedTextColor.WHITE)));
            }

            loreBuilder.addLine(Component.empty())
                    .addLine(Component.text("▶ Click to Equip This Laser", NamedTextColor.GREEN, TextDecoration.ITALIC));

            item.setData(DataComponentTypes.LORE, loreBuilder.build());

            builder.addClickable(slotInGui, Clickable.general(item, context -> {
                colorIndices.put(player.getUniqueId(), index);
                player.sendMessage(ChatColor.GREEN + "Equipped Laser: " + ChatColor.YELLOW + profile.name());
                player.playSound(player.getLocation(), Sound.BLOCK_BEACON_ACTIVATE, 0.4f, 1.8f);
                Bukkit.getScheduler().runTask(fluffy, () -> player.closeInventory());
            }));
        }

        if (currentPage > 0) {
            ItemStack prevItem = ItemStack.of(Material.ARROW);
            prevItem.setData(DataComponentTypes.ITEM_NAME, Component.text("◄ Previous Page", NamedTextColor.YELLOW, TextDecoration.BOLD));
            prevItem.setData(DataComponentTypes.LORE, ItemLore.lore().addLine(Component.text("Click to view earlier lasers in category.", NamedTextColor.GRAY)).build());
            builder.addClickable(45, Clickable.general(prevItem, context -> {
                player.playSound(player.getLocation(), Sound.BLOCK_NOTE_BLOCK_CHIME, 0.5f, 1.2f);
                openLaserMenu(player, category, currentPage - 1);
            }));
        } else {
            ItemStack inactiveItem = ItemStack.of(Material.BLACK_STAINED_GLASS_PANE);
            inactiveItem.setData(DataComponentTypes.ITEM_NAME, Component.text("◄ First Page", NamedTextColor.DARK_GRAY));
            builder.addClickable(45, Clickable.general(inactiveItem, context -> {}));
        }

        ItemStack cyanPane = ItemStack.of(Material.CYAN_STAINED_GLASS_PANE);
        cyanPane.setData(DataComponentTypes.ITEM_NAME, Component.text(" "));
        builder.addClickable(46, Clickable.general(cyanPane, context -> {}));

        ItemStack backItem = ItemStack.of(Material.COMPASS);
        backItem.setData(DataComponentTypes.ITEM_NAME, Component.text("◄ Back to Categories", NamedTextColor.GOLD, TextDecoration.BOLD));
        backItem.setData(DataComponentTypes.LORE, ItemLore.lore().addLine(Component.text("Return to the main category menu.", NamedTextColor.GRAY)).build());
        builder.addClickable(47, Clickable.general(backItem, context -> {
            player.playSound(player.getLocation(), Sound.BLOCK_PISTON_CONTRACT, 0.5f, 1.2f);
            openCategoryMenu(player);
        }));

        LaserProfile currentEquipped = getPlayerProfile(player.getUniqueId());
        ItemStack currentItem = ItemStack.of(currentEquipped.material());
        currentItem.setData(DataComponentTypes.ITEM_NAME, Component.text("Current: " + currentEquipped.name(), NamedTextColor.GOLD, TextDecoration.BOLD));
        currentItem.setData(DataComponentTypes.LORE, ItemLore.lore()
                .addLine(Component.text("Your currently active laser profile.", NamedTextColor.GRAY))
                .addLine(Component.text("Left-Click air/block to open menu.", NamedTextColor.DARK_GRAY))
                .addLine(Component.text("Right-Click air/block to fire.", NamedTextColor.DARK_GRAY))
                .build());
        builder.addClickable(48, Clickable.general(currentItem, context -> {}));

        ItemStack infoItem = ItemStack.of(Material.PAPER);
        infoItem.setData(DataComponentTypes.ITEM_NAME, Component.text(category.getTitle(), NamedTextColor.AQUA, TextDecoration.BOLD));
        infoItem.setData(DataComponentTypes.LORE, ItemLore.lore()
                .addLine(Component.text(category.getDescription(), NamedTextColor.GRAY))
                .build());
        builder.addClickable(49, Clickable.general(infoItem, context -> {}));

        ItemStack closeItem = ItemStack.of(Material.BARRIER);
        closeItem.setData(DataComponentTypes.ITEM_NAME, Component.text("Close Menu", NamedTextColor.RED, TextDecoration.BOLD));
        closeItem.setData(DataComponentTypes.LORE, ItemLore.lore().addLine(Component.text("Exit the laser menu.", NamedTextColor.GRAY)).build());
        builder.addClickable(50, Clickable.general(closeItem, context -> {
            player.playSound(player.getLocation(), Sound.BLOCK_PISTON_CONTRACT, 0.5f, 0.8f);
            Bukkit.getScheduler().runTask(fluffy, () -> player.closeInventory());
        }));

        builder.addClickable(51, Clickable.general(cyanPane, context -> {}));

        ItemStack darkPane = ItemStack.of(Material.BLACK_STAINED_GLASS_PANE);
        darkPane.setData(DataComponentTypes.ITEM_NAME, Component.text(" "));
        builder.addClickable(52, Clickable.general(darkPane, context -> {}));

        if (currentPage < maxPages - 1) {
            ItemStack nextItem = ItemStack.of(Material.ARROW);
            nextItem.setData(DataComponentTypes.ITEM_NAME, Component.text("Next Page ►", NamedTextColor.YELLOW, TextDecoration.BOLD));
            nextItem.setData(DataComponentTypes.LORE, ItemLore.lore().addLine(Component.text("Click to view more lasers in category.", NamedTextColor.GRAY)).build());
            builder.addClickable(53, Clickable.general(nextItem, context -> {
                player.playSound(player.getLocation(), Sound.BLOCK_NOTE_BLOCK_CHIME, 0.5f, 1.5f);
                openLaserMenu(player, category, currentPage + 1);
            }));
        } else {
            ItemStack inactiveItem = ItemStack.of(Material.BLACK_STAINED_GLASS_PANE);
            inactiveItem.setData(DataComponentTypes.ITEM_NAME, Component.text("► Last Page", NamedTextColor.DARK_GRAY));
            builder.addClickable(53, Clickable.general(inactiveItem, context -> {}));
        }

        builder.build().open(player);
    }

    private LaserProfile getPlayerProfile(UUID uuid) {
        int index = colorIndices.getOrDefault(uuid, 0);
        if (index < 0 || index >= LASER_PROFILES.length) {
            index = 0;
        }
        return LASER_PROFILES[index];
    }

    private void applyHitEffects(LivingEntity hitEntity, Player player, LaserProfile profile) {
        Location loc = hitEntity.getLocation();

        if (profile.heals()) {
            double healAmount = Math.abs(profile.damage());
            double newHealth = Math.min(hitEntity.getMaxHealth(), hitEntity.getHealth() + healAmount);
            hitEntity.setHealth(newHealth);
        } else {
            hitEntity.damage(profile.damage(), player);
        }

        if (profile.explosionPower() > 0) {
            loc.createExplosion(player, profile.explosionPower());
        }

        if (profile.setsFire()) {
            hitEntity.setFireTicks(100);
        }

        if (profile.strikesLightning()) {
            loc.getWorld().strikeLightning(loc);
        }

        if (profile.knockbackPower() != 0) {
            Vector kb = loc.toVector().subtract(player.getLocation().toVector()).normalize();
            kb.setY(0.3);
            hitEntity.setVelocity(kb.multiply(profile.knockbackPower()));
        }

        if (profile.statusEffect() != null) {
            hitEntity.addPotionEffect(profile.statusEffect());
        }

        if (profile.name().equalsIgnoreCase("Meteor Shower")) {
            triggerMeteorEffect(loc);
        }
    }

    private void triggerMeteorEffect(Location targetLoc) {
        World world = targetLoc.getWorld();
        if (world == null) return;

        for (int x = -2; x <= 2; x++) {
            for (int y = -2; y <= 1; y++) {
                for (int z = -2; z <= 2; z++) {
                    if (x * x + y * y + z * z <= 5) {
                        Location blockLoc = targetLoc.clone().add(x, y, z);
                        Block block = blockLoc.getBlock();
                        if (block.getType() == Material.BEDROCK) continue;

                        if (y <= 0) {
                            block.setType(Material.AIR);
                        } else if (y == 1) {
                            block.setType(Math.random() < 0.6 ? Material.MAGMA_BLOCK : Material.FIRE);
                        }
                    }
                }
            }
        }

        world.spawnParticle(Particle.EXPLOSION_EMITTER, targetLoc, 1);
        world.spawnParticle(Particle.FLAME, targetLoc, 40, 1.0, 1.0, 1.0, 0.2);
        world.spawnParticle(Particle.SMOKE, targetLoc, 30, 1.0, 1.0, 1.0, 0.1);
        world.playSound(targetLoc, Sound.ENTITY_GENERIC_EXPLODE, 1.5f, 0.7f);
    }

    private void spawnTerminalOrb(World world, Location location, LaserProfile profile) {
        if (location == null) return;
        world.spawn(location, BlockDisplay.class, display -> {
            display.setBlock(getBlockData(profile));
            display.setBrightness(new Display.Brightness(15, 15));
            display.setBillboard(Display.Billboard.CENTER);
            display.setPersistent(false);
            Transformation transformation = new Transformation(
                    new Vector3f(-0.25f, -0.25f, -0.25f),
                    new Quaternionf(),
                    new Vector3f(0.5f, 0.5f, 0.5f),
                    new Quaternionf()
            );
            display.setTransformation(transformation);
            Bukkit.getScheduler().runTaskLater(fluffy, () -> {
                if (display.isValid()) display.remove();
            }, 20L);
        });
    }

    private BeamTraceResult traceBeam(Player player, boolean isRight) {
        World world = player.getWorld();
        Location eyeLoc = player.getEyeLocation();
        Vector rayOrigin = eyeLoc.toVector();
        Vector rayDir = eyeLoc.getDirection().normalize();

        double distance = MAX_DISTANCE;
        LivingEntity hitEntity = null;
        Location impactLocation = null;

        RayTraceResult blockHit = world.rayTraceBlocks(
                eyeLoc, rayDir, MAX_DISTANCE,
                FluidCollisionMode.NEVER, true
        );
        if (blockHit != null && blockHit.getHitPosition() != null) {
            distance = rayOrigin.distance(blockHit.getHitPosition());
            impactLocation = blockHit.getHitPosition().toLocation(world);
        }

        for (Entity entity : world.getEntities()) {
            if (entity.equals(player) || !(entity instanceof LivingEntity living)) continue;

            BoundingBox bb = living.getBoundingBox();
            Vector min = new Vector(bb.getMinX(), bb.getMinY(), bb.getMinZ());
            Vector max = new Vector(bb.getMaxX(), bb.getMaxY(), bb.getMaxZ());

            double t = rayAABBIntersect(rayOrigin, rayDir, min, max);
            if (t >= 0 && t < distance) {
                distance = t;
                hitEntity = living;
                impactLocation = living.getLocation();
            }
        }

        Vector right = rayDir.clone().crossProduct(new Vector(0, 1, 0));
        if (right.lengthSquared() < 1e-4) {
            right = new Vector(1, 0, 0);
        } else {
            right.normalize();
        }

        double separation = isRight ? HEAD_SEPARATION : -HEAD_SEPARATION;
        Location origin = eyeLoc.clone().add(right.clone().multiply(separation));

        return new BeamTraceResult(origin, rayDir, distance, hitEntity, impactLocation);
    }

    private List<BeamTraceResult> traceAdvancedRicochet(Player player, LaserProfile profile, int maxBounces) {
        List<BeamTraceResult> results = new ArrayList<>();
        World world = player.getWorld();
        Location eyeLoc = player.getEyeLocation();
        Vector right = eyeLoc.getDirection().normalize().crossProduct(new Vector(0, 1, 0));
        if (right.lengthSquared() < 1e-4) right = new Vector(1, 0, 0);
        else right.normalize();

        Location currentOrigin = eyeLoc.clone().add(right.clone().multiply(HEAD_SEPARATION));
        Vector currentDir = eyeLoc.getDirection().normalize();
        double remainingDistance = MAX_DISTANCE;

        for (int bounce = 0; bounce <= maxBounces && remainingDistance > 0; bounce++) {
            RayTraceResult blockHit = world.rayTraceBlocks(
                    currentOrigin, currentDir, remainingDistance,
                    FluidCollisionMode.NEVER, true
            );

            double segmentDistance = remainingDistance;
            Location impactLoc = null;
            Vector normal = null;

            if (blockHit != null && blockHit.getHitPosition() != null) {
                segmentDistance = currentOrigin.toVector().distance(blockHit.getHitPosition());
                impactLoc = blockHit.getHitPosition().toLocation(world);
                if (blockHit.getHitBlockFace() != null) {
                    normal = blockHit.getHitBlockFace().getDirection();
                }
            }

            LivingEntity hitEntity = null;
            for (Entity entity : world.getEntities()) {
                if (entity.equals(player) || !(entity instanceof LivingEntity living)) continue;
                BoundingBox bb = living.getBoundingBox();
                double t = rayAABBIntersect(currentOrigin.toVector(), currentDir,
                        new Vector(bb.getMinX(), bb.getMinY(), bb.getMinZ()),
                        new Vector(bb.getMaxX(), bb.getMaxY(), bb.getMaxZ()));
                if (t >= 0 && t < segmentDistance) {
                    segmentDistance = t;
                    hitEntity = living;
                    impactLoc = living.getLocation();
                }
            }

            results.add(new BeamTraceResult(currentOrigin, currentDir, segmentDistance, hitEntity, impactLoc));

            if (hitEntity != null || blockHit == null || normal == null) {
                if (hitEntity != null) {
                    applyHitEffects(hitEntity, player, profile);
                    spawnTerminalOrb(world, hitEntity.getLocation(), profile);
                } else if (impactLoc != null) {
                    spawnTerminalOrb(world, impactLoc, profile);
                }
                break;
            }

            spawnTerminalOrb(world, impactLoc, profile);

            double dot = currentDir.dot(normal);
            currentDir = currentDir.clone().subtract(normal.clone().multiply(2 * dot)).normalize();
            currentOrigin = impactLoc.clone().add(currentDir.clone().multiply(0.05));
            remainingDistance -= segmentDistance;
        }

        return results;
    }

    private void fireLaser(Player player) {
        World world = player.getWorld();
        Location eyeLoc = player.getEyeLocation();

        LaserProfile profile = getPlayerProfile(player.getUniqueId());

        if (profile.name().contains("Ricochet") || profile.name().contains("Splitter") || profile.name().contains("Orbiter")) {
            int maxBounces = profile.name().contains("Pinball") ? 5 : 3;
            List<BeamTraceResult> ricochetResults = traceAdvancedRicochet(player, profile, maxBounces);
            for (BeamTraceResult res : ricochetResults) {
                spawnLaserBeam(world, res.origin(), res.direction(), res.distance(), profile);
                if (res.hitEntity() != null) {
                    applyHitEffects(res.hitEntity(), player, profile);
                }
            }
        } else {
            BeamTraceResult initialTrace = traceBeam(player, true);
            if (initialTrace.hitEntity() != null) {
                applyHitEffects(initialTrace.hitEntity(), player, profile);
                spawnTerminalOrb(world, initialTrace.hitEntity().getLocation(), profile);
            } else if (initialTrace.impactLocation() != null) {
                if (profile.name().equalsIgnoreCase("Meteor Shower")) {
                    triggerMeteorEffect(initialTrace.impactLocation());
                } else {
                    spawnTerminalOrb(world, initialTrace.impactLocation(), profile);
                }
                if (profile.explosionPower() > 0) {
                    initialTrace.impactLocation().createExplosion(player, profile.explosionPower());
                }
            }

            if (profile.strikesLightning()) {
                spawnLightningBeam(player, profile, true);
                spawnLightningBeam(player, profile, false);
            } else if (profile.isSpiral()) {
                spawnSpiralBeam(player, profile, false, true);
                spawnSpiralBeam(player, profile, true, false);
            } else if (profile.isDoubleHelix()) {
                spawnDoubleHelixBeam(player, profile, true);
                spawnDoubleHelixBeam(player, profile, false);
            } else if (profile.isWave()) {
                spawnWaveBeam(player, profile, true);
                spawnWaveBeam(player, profile, false);
            } else if (profile.isPulsar()) {
                spawnPulsarBeam(player, profile, true);
                spawnPulsarBeam(player, profile, false);
            } else {
                spawnLaserBeam(world, initialTrace.origin(), initialTrace.direction(), initialTrace.distance(), profile);
                Vector rightVector = initialTrace.direction().clone().crossProduct(new Vector(0, 1, 0));
                if (rightVector.lengthSquared() < 1e-4) rightVector = new Vector(1, 0, 0);
                else rightVector.normalize();
                Location leftOrigin = eyeLoc.clone().add(rightVector.multiply(-HEAD_SEPARATION));
                spawnLaserBeam(world, leftOrigin, initialTrace.direction(), initialTrace.distance(), profile);
            }
        }

        world.playSound(eyeLoc, profile.fireSound(), 0.6f, profile.soundPitch());
    }

    private void fireLaserAtEntity(Player player, LivingEntity target) {
        World world = player.getWorld();
        Location eyeLoc = player.getEyeLocation();

        LaserProfile profile = getPlayerProfile(player.getUniqueId());
        applyHitEffects(target, player, profile);
        spawnTerminalOrb(world, target.getLocation(), profile);

        Vector rayOrigin = eyeLoc.toVector();
        Vector targetVec = target.getLocation().add(0, target.getHeight() / 2.0, 0).toVector();
        double distance = rayOrigin.distance(targetVec);
        if (distance > MAX_DISTANCE) distance = MAX_DISTANCE;
        Vector rayDir = targetVec.clone().subtract(rayOrigin).normalize();

        Vector right = rayDir.clone().crossProduct(new Vector(0, 1, 0));
        if (right.lengthSquared() < 1e-4) right = new Vector(1, 0, 0);
        else right.normalize();

        Location rightOrigin = eyeLoc.clone().add(right.clone().multiply(HEAD_SEPARATION));
        Location leftOrigin = eyeLoc.clone().add(right.clone().multiply(-HEAD_SEPARATION));

        if (profile.strikesLightning()) {
            spawnLightningBeam(player, profile, true);
            spawnLightningBeam(player, profile, false);
        } else if (profile.isSpiral()) {
            spawnSpiralBeam(player, profile, false, true);
            spawnSpiralBeam(player, profile, true, false);
        } else if (profile.isDoubleHelix()) {
            spawnDoubleHelixBeam(player, profile, true);
            spawnDoubleHelixBeam(player, profile, false);
        } else if (profile.isWave()) {
            spawnWaveBeam(player, profile, true);
            spawnWaveBeam(player, profile, false);
        } else if (profile.isPulsar()) {
            spawnPulsarBeam(player, profile, true);
            spawnPulsarBeam(player, profile, false);
        } else {
            spawnLaserBeam(world, rightOrigin, rayDir, distance, profile);
            spawnLaserBeam(world, leftOrigin, rayDir, distance, profile);
        }

        world.playSound(eyeLoc, profile.fireSound(), 0.6f, profile.soundPitch());
    }

    private double rayAABBIntersect(Vector origin, Vector dir, Vector min, Vector max) {
        double tMin = Double.NEGATIVE_INFINITY;
        double tMax = Double.POSITIVE_INFINITY;

        double[] o = {origin.getX(), origin.getY(), origin.getZ()};
        double[] d = {dir.getX(),    dir.getY(),    dir.getZ()};
        double[] bMin = {min.getX(), min.getY(), min.getZ()};
        double[] bMax = {max.getX(), max.getY(), max.getZ()};

        for (int i = 0; i < 3; i++) {
            if (Math.abs(d[i]) < 1e-8) {
                if (o[i] < bMin[i] || o[i] > bMax[i]) return -1.0;
            } else {
                double t1 = (bMin[i] - o[i]) / d[i];
                double t2 = (bMax[i] - o[i]) / d[i];

                if (t1 > t2) { double tmp = t1; t1 = t2; t2 = tmp; }

                tMin = Math.max(tMin, t1);
                tMax = Math.min(tMax, t2);

                if (tMin > tMax) return -1.0;
            }
        }

        return tMin >= 0 ? tMin : (tMax >= 0 ? tMax : -1.0);
    }

    private void spawnParticlesAlongBeam(World world, Location origin, Vector direction, double length, LaserProfile profile) {
        if (world == null) return;

        boolean hasParticleHalo = profile.heals() || profile.setsFire() || profile.strikesLightning()
                || profile.isSpiral() || profile.isDoubleHelix() || profile.isPulsar()
                || profile.name().contains("Supernova") || profile.name().contains("Void")
                || profile.name().contains("Starlight") || profile.name().contains("Mana")
                || profile.name().contains("Core") || profile.name().contains("Surge")
                || profile.name().contains("Prismatic") || profile.name().contains("Ricochet")
                || profile.name().contains("Orbiter");
        if (!hasParticleHalo) return;

        Vector dirNorm = direction.clone().normalize();
        Vector perp1 = dirNorm.clone().crossProduct(new Vector(0, 1, 0));
        if (perp1.lengthSquared() < 1e-4) {
            perp1 = new Vector(1, 0, 0);
        } else {
            perp1.normalize();
        }
        Vector perp2 = dirNorm.clone().crossProduct(perp1).normalize();

        double step = 1.8;
        double haloRadius = 0.28;

        for (double d = 0; d <= length; d += step) {
            Location centerLoc = origin.clone().add(dirNorm.clone().multiply(d));

            for (int i = 0; i < 3; i++) {
                double angle = (i * (Math.PI * 2 / 3.0)) + (d * 0.4);
                double xOff = Math.cos(angle) * haloRadius;
                double yOff = Math.sin(angle) * haloRadius;

                Location pLoc = centerLoc.clone().add(perp1.clone().multiply(xOff)).add(perp2.clone().multiply(yOff));

                if (profile.heals()) {
                    world.spawnParticle(Particle.HAPPY_VILLAGER, pLoc, 1, 0, 0, 0, 0);
                } else if (profile.setsFire()) {
                    world.spawnParticle(Particle.FLAME, pLoc, 1, 0, 0, 0, 0.01);
                } else if (profile.strikesLightning()) {
                    world.spawnParticle(Particle.ELECTRIC_SPARK, pLoc, 1, 0, 0, 0, 0.02);
                } else if (profile.isSpiral() || profile.isDoubleHelix() || profile.name().contains("Prismatic") || profile.name().contains("Ricochet")) {
                    world.spawnParticle(Particle.WITCH, pLoc, 1, 0, 0, 0, 0);
                } else if (profile.name().contains("Void") || profile.name().contains("Supernova") || profile.name().contains("Orbiter")) {
                    world.spawnParticle(Particle.PORTAL, pLoc, 1, 0, 0, 0, 0.05);
                } else {
                    world.spawnParticle(Particle.END_ROD, pLoc, 1, 0, 0, 0, 0);
                }
            }
        }
    }

    private void spawnLaserBeam(World world, Location origin, Vector direction, double length, LaserProfile profile) {
        Location spawnLoc = origin.clone();
        spawnLoc.setYaw(0f);
        spawnLoc.setPitch(0f);

        BlockData blockData = getBlockData(profile);

        BlockDisplay display = world.spawn(spawnLoc, BlockDisplay.class, d -> {
            d.setBlock(blockData);
            d.setBrightness(new Display.Brightness(15, 15));
            d.setBillboard(Display.Billboard.FIXED);
            d.setShadowRadius(0f);
            d.setShadowStrength(0f);
            d.setPersistent(false);
            d.setGlowColorOverride(org.bukkit.Color.RED);
            d.setInterpolationDuration(0);
            d.setInterpolationDelay(0);
        });

        Vector3f zAxis = new Vector3f(0, 0, 1);
        Vector3f aim = new Vector3f((float) direction.getX(), (float) direction.getY(), (float) direction.getZ());
        if (aim.lengthSquared() > 0) aim.normalize();

        Quaternionf rotation = new Quaternionf().rotationTo(zAxis, aim);
        Quaternionf rollRotation = new Quaternionf().rotateZ((float) Math.toRadians(45));

        Vector3f translation = new Vector3f(-THICKNESS / 2f, -THICKNESS / 2f, 0f);
        rotation.transform(translation);

        Transformation transformation = new Transformation(
                translation,
                rotation,
                new Vector3f(THICKNESS, THICKNESS, (float) length),
                rollRotation
        );
        display.setTransformation(transformation);

        spawnParticlesAlongBeam(world, origin, direction, length, profile);

        Bukkit.getScheduler().runTaskLater(fluffy, () -> {
            if (display.isValid()) display.remove();
        }, 16L);
    }

    private void spawnLightningBeam(Player player, LaserProfile profile, boolean isRight) {
        new BukkitRunnable() {
            private final List<BlockDisplay> currentDisplays = new ArrayList<>();
            private long stepCount = 0;
            private final long maxSteps = 5;

            @Override
            public void run() {
                if (!player.isOnline() || stepCount >= maxSteps) {
                    for (BlockDisplay d : currentDisplays) {
                        if (d.isValid()) d.remove();
                    }
                    cancel();
                    return;
                }

                for (BlockDisplay d : currentDisplays) {
                    if (d.isValid()) d.remove();
                }
                currentDisplays.clear();

                BeamTraceResult trace = traceBeam(player, isRight);
                Location start = trace.origin();
                Vector direction = trace.direction();
                double totalLength = trace.distance();
                World world = player.getWorld();

                spawnParticlesAlongBeam(world, start, direction, totalLength, profile);

                double segmentLength = 1.0;
                int segments = (int) Math.max(1, totalLength / segmentLength);

                Location currentLoc = start.clone();
                Vector dirNormalized = direction.clone().normalize();

                Vector perp1 = dirNormalized.clone().crossProduct(new Vector(0, 1, 0));
                if (perp1.lengthSquared() < 1e-4) {
                    perp1 = new Vector(1, 0, 0);
                } else {
                    perp1.normalize();
                }
                Vector perp2 = dirNormalized.clone().crossProduct(perp1).normalize();

                for (int i = 0; i < segments; i++) {
                    double currentSegLen = segmentLength;
                    if (i == segments - 1) {
                        currentSegLen = totalLength - (i * segmentLength);
                        if (currentSegLen <= 0) break;
                    }

                    Location nextLoc;
                    if (i == segments - 1) {
                        nextLoc = start.clone().add(dirNormalized.clone().multiply(totalLength));
                    } else {
                        double jitterAmount = 0.35;
                        double j1 = (Math.random() - 0.5) * jitterAmount;
                        double j2 = (Math.random() - 0.5) * jitterAmount;

                        nextLoc = currentLoc.clone().add(dirNormalized.clone().multiply(currentSegLen))
                                .add(perp1.clone().multiply(j1))
                                .add(perp2.clone().multiply(j2));
                    }

                    Vector segDir = nextLoc.clone().subtract(currentLoc.clone()).toVector();
                    double segLen = segDir.length();
                    if (segLen > 0.001) {
                        segDir.normalize();

                        Location spawnLoc = currentLoc.clone();
                        spawnLoc.setYaw(0f);
                        spawnLoc.setPitch(0f);

                        BlockData blockData = getBlockData(profile);

                        BlockDisplay display = world.spawn(spawnLoc, BlockDisplay.class, d -> {
                            d.setBlock(blockData);
                            d.setBrightness(new Display.Brightness(15, 15));
                            d.setBillboard(Display.Billboard.FIXED);
                            d.setShadowRadius(0f);
                            d.setShadowStrength(0f);
                            d.setPersistent(false);
                            d.setInterpolationDuration(0);
                            d.setInterpolationDelay(0);
                        });

                        Vector3f zAxis = new Vector3f(0, 0, 1);
                        Vector3f aim = new Vector3f((float) segDir.getX(), (float) segDir.getY(), (float) segDir.getZ());
                        if (aim.lengthSquared() > 0) aim.normalize();

                        Quaternionf rotation = new Quaternionf().rotationTo(zAxis, aim);
                        Quaternionf rollRotation = new Quaternionf().rotateZ((float) Math.toRadians(45));

                        Vector3f translation = new Vector3f(-THICKNESS / 2f, -THICKNESS / 2f, 0f);
                        rotation.transform(translation);

                        Transformation transformation = new Transformation(
                                translation,
                                rotation,
                                new Vector3f(THICKNESS, THICKNESS, (float) segLen),
                                rollRotation
                        );
                        display.setTransformation(transformation);
                        currentDisplays.add(display);
                    }
                    currentLoc = nextLoc;
                }
                stepCount++;
            }
        }.runTaskTimer(fluffy, 0L, 2L);
    }

    private void spawnSpiralBeam(Player player, LaserProfile profile, boolean reverse, boolean isRight) {
        new BukkitRunnable() {
            private final List<BlockDisplay> currentDisplays = new ArrayList<>();
            private long stepCount = 0;
            private final long maxSteps = 6;

            @Override
            public void run() {
                if (!player.isOnline() || stepCount >= maxSteps) {
                    for (BlockDisplay d : currentDisplays) {
                        if (d.isValid()) d.remove();
                    }
                    cancel();
                    return;
                }

                for (BlockDisplay d : currentDisplays) {
                    if (d.isValid()) d.remove();
                }
                currentDisplays.clear();

                BeamTraceResult trace = traceBeam(player, isRight);
                Location start = trace.origin();
                Vector direction = trace.direction();
                double totalLength = trace.distance();
                World world = player.getWorld();

                spawnParticlesAlongBeam(world, start, direction, totalLength, profile);

                double segmentLength = 0.5;
                int segments = (int) Math.max(1, totalLength / segmentLength);

                Vector dirNormalized = direction.clone().normalize();
                Vector perp1 = dirNormalized.clone().crossProduct(new Vector(0, 1, 0));
                if (perp1.lengthSquared() < 1e-4) {
                    perp1 = new Vector(1, 0, 0);
                } else {
                    perp1.normalize();
                }
                Vector perp2 = dirNormalized.clone().crossProduct(perp1).normalize();

                double radius = 0.55;
                double turnsPerBlock = 0.3;
                double turnSign = reverse ? -1.0 : 1.0;
                double phaseShift = (stepCount * 0.15) * turnSign;

                Location prevLoc = start.clone();

                for (int i = 1; i <= segments; i++) {
                    double s = i * segmentLength;
                    if (s > totalLength) s = totalLength;

                    double angle = (s * turnsPerBlock * Math.PI * 2 * turnSign) + phaseShift;
                    double xOffset = Math.cos(angle) * radius;
                    double yOffset = Math.sin(angle) * radius;

                    Location nextLoc = start.clone().add(dirNormalized.clone().multiply(s))
                            .add(perp1.clone().multiply(xOffset))
                            .add(perp2.clone().multiply(yOffset));

                    Vector segDir = nextLoc.clone().subtract(prevLoc).toVector();
                    double segLen = segDir.length();
                    if (segLen > 0.001) {
                        segDir.normalize();

                        Location spawnLoc = prevLoc.clone();
                        spawnLoc.setYaw(0f);
                        spawnLoc.setPitch(0f);

                        BlockData blockData = getBlockData(profile);

                        BlockDisplay display = world.spawn(spawnLoc, BlockDisplay.class, d -> {
                            d.setBlock(blockData);
                            d.setBrightness(new Display.Brightness(15, 15));
                            d.setBillboard(Display.Billboard.FIXED);
                            d.setShadowRadius(0f);
                            d.setShadowStrength(0f);
                            d.setPersistent(false);
                            d.setInterpolationDuration(0);
                            d.setInterpolationDelay(0);
                        });

                        Vector3f zAxis = new Vector3f(0, 0, 1);
                        Vector3f aim = new Vector3f((float) segDir.getX(), (float) segDir.getY(), (float) segDir.getZ());
                        if (aim.lengthSquared() > 0) aim.normalize();

                        Quaternionf rotation = new Quaternionf().rotationTo(zAxis, aim);
                        Quaternionf rollRotation = new Quaternionf().rotateZ((float) Math.toRadians(45));

                        Vector3f translation = new Vector3f(-THICKNESS / 2f, -THICKNESS / 2f, 0f);
                        rotation.transform(translation);

                        Transformation transformation = new Transformation(
                                translation,
                                rotation,
                                new Vector3f(THICKNESS, THICKNESS, (float) segLen),
                                rollRotation
                        );
                        display.setTransformation(transformation);
                        currentDisplays.add(display);
                    }
                    prevLoc = nextLoc;
                    if (s >= totalLength) break;
                }
                stepCount++;
            }
        }.runTaskTimer(fluffy, 0L, 2L);
    }

    private void spawnDoubleHelixBeam(Player player, LaserProfile profile, boolean isRight) {
        new BukkitRunnable() {
            private final List<BlockDisplay> currentDisplays = new ArrayList<>();
            private long stepCount = 0;
            private final long maxSteps = 6;

            @Override
            public void run() {
                if (!player.isOnline() || stepCount >= maxSteps) {
                    for (BlockDisplay d : currentDisplays) {
                        if (d.isValid()) d.remove();
                    }
                    cancel();
                    return;
                }

                for (BlockDisplay d : currentDisplays) {
                    if (d.isValid()) d.remove();
                }
                currentDisplays.clear();

                BeamTraceResult trace = traceBeam(player, isRight);
                Location start = trace.origin();
                Vector direction = trace.direction();
                double totalLength = trace.distance();
                World world = player.getWorld();

                spawnParticlesAlongBeam(world, start, direction, totalLength, profile);

                double segmentLength = 0.5;
                int segments = (int) Math.max(1, totalLength / segmentLength);

                Vector dirNormalized = direction.clone().normalize();
                Vector perp1 = dirNormalized.clone().crossProduct(new Vector(0, 1, 0));
                if (perp1.lengthSquared() < 1e-4) {
                    perp1 = new Vector(1, 0, 0);
                } else {
                    perp1.normalize();
                }
                Vector perp2 = dirNormalized.clone().crossProduct(perp1).normalize();

                double radius = 0.45;
                double turnsPerBlock = 0.25;
                double phaseShift = stepCount * 0.12;

                for (int strand = 0; strand < 2; strand++) {
                    double strandSign = strand == 0 ? 1.0 : -1.0;
                    double strandPhase = phaseShift * strandSign + (strand == 0 ? 0 : Math.PI);
                    Location prevLoc = start.clone();

                    for (int i = 1; i <= segments; i++) {
                        double s = i * segmentLength;
                        if (s > totalLength) s = totalLength;

                        double angle = (s * turnsPerBlock * Math.PI * 2 * strandSign) + strandPhase;
                        double xOffset = Math.cos(angle) * radius;
                        double yOffset = Math.sin(angle) * radius;

                        Location nextLoc = start.clone().add(dirNormalized.clone().multiply(s))
                                .add(perp1.clone().multiply(xOffset))
                                .add(perp2.clone().multiply(yOffset));

                        Vector segDir = nextLoc.clone().subtract(prevLoc).toVector();
                        double segLen = segDir.length();
                        if (segLen > 0.001) {
                            segDir.normalize();

                            Location spawnLoc = prevLoc.clone();
                            spawnLoc.setYaw(0f);
                            spawnLoc.setPitch(0f);

                            BlockData blockData = getBlockData(profile);

                            BlockDisplay display = world.spawn(spawnLoc, BlockDisplay.class, d -> {
                                d.setBlock(blockData);
                                d.setBrightness(new Display.Brightness(15, 15));
                                d.setBillboard(Display.Billboard.FIXED);
                                d.setShadowRadius(0f);
                                d.setShadowStrength(0f);
                                d.setPersistent(false);
                                d.setInterpolationDuration(0);
                                d.setInterpolationDelay(0);
                            });

                            Vector3f zAxis = new Vector3f(0, 0, 1);
                            Vector3f aim = new Vector3f((float) segDir.getX(), (float) segDir.getY(), (float) segDir.getZ());
                            if (aim.lengthSquared() > 0) aim.normalize();

                            Quaternionf rotation = new Quaternionf().rotationTo(zAxis, aim);
                            Quaternionf rollRotation = new Quaternionf().rotateZ((float) Math.toRadians(45));

                            Vector3f translation = new Vector3f(-THICKNESS / 2f, -THICKNESS / 2f, 0f);
                            rotation.transform(translation);

                            Transformation transformation = new Transformation(
                                    translation,
                                    rotation,
                                    new Vector3f(THICKNESS, THICKNESS, (float) segLen),
                                    rollRotation
                            );
                            display.setTransformation(transformation);
                            currentDisplays.add(display);
                        }
                        prevLoc = nextLoc;
                        if (s >= totalLength) break;
                    }
                }
                stepCount++;
            }
        }.runTaskTimer(fluffy, 0L, 2L);
    }

    private void spawnWaveBeam(Player player, LaserProfile profile, boolean isRight) {
        new BukkitRunnable() {
            private final List<BlockDisplay> currentDisplays = new ArrayList<>();
            private long stepCount = 0;
            private final long maxSteps = 6;

            @Override
            public void run() {
                if (!player.isOnline() || stepCount >= maxSteps) {
                    for (BlockDisplay d : currentDisplays) {
                        if (d.isValid()) d.remove();
                    }
                    cancel();
                    return;
                }

                for (BlockDisplay d : currentDisplays) {
                    if (d.isValid()) d.remove();
                }
                currentDisplays.clear();

                BeamTraceResult trace = traceBeam(player, isRight);
                Location start = trace.origin();
                Vector direction = trace.direction();
                double totalLength = trace.distance();
                World world = player.getWorld();

                spawnParticlesAlongBeam(world, start, direction, totalLength, profile);

                double segmentLength = 0.5;
                int segments = (int) Math.max(1, totalLength / segmentLength);

                Vector dirNormalized = direction.clone().normalize();
                Vector perp1 = dirNormalized.clone().crossProduct(new Vector(0, 1, 0));
                if (perp1.lengthSquared() < 1e-4) {
                    perp1 = new Vector(1, 0, 0);
                } else {
                    perp1.normalize();
                }
                Vector perp2 = dirNormalized.clone().crossProduct(perp1).normalize();

                double waveAmplitude = 0.5;
                double waveFrequency = 0.4;
                double timePhase = stepCount * 0.25;

                Location prevLoc = start.clone();

                for (int i = 1; i <= segments; i++) {
                    double s = i * segmentLength;
                    if (s > totalLength) s = totalLength;

                    double xOffset = Math.sin((s * waveFrequency) + timePhase) * waveAmplitude;
                    double yOffset = Math.cos((s * waveFrequency * 0.8) - timePhase) * waveAmplitude;

                    Location nextLoc = start.clone().add(dirNormalized.clone().multiply(s))
                            .add(perp1.clone().multiply(xOffset))
                            .add(perp2.clone().multiply(yOffset));

                    Vector segDir = nextLoc.clone().subtract(prevLoc).toVector();
                    double segLen = segDir.length();
                    if (segLen > 0.001) {
                        segDir.normalize();

                        Location spawnLoc = prevLoc.clone();
                        spawnLoc.setYaw(0f);
                        spawnLoc.setPitch(0f);

                        BlockData blockData = getBlockData(profile);

                        BlockDisplay display = world.spawn(spawnLoc, BlockDisplay.class, d -> {
                            d.setBlock(blockData);
                            d.setBrightness(new Display.Brightness(15, 15));
                            d.setBillboard(Display.Billboard.FIXED);
                            d.setShadowRadius(0f);
                            d.setShadowStrength(0f);
                            d.setPersistent(false);
                            d.setInterpolationDuration(0);
                            d.setInterpolationDelay(0);
                        });

                        Vector3f zAxis = new Vector3f(0, 0, 1);
                        Vector3f aim = new Vector3f((float) segDir.getX(), (float) segDir.getY(), (float) segDir.getZ());
                        if (aim.lengthSquared() > 0) aim.normalize();

                        Quaternionf rotation = new Quaternionf().rotationTo(zAxis, aim);
                        Quaternionf rollRotation = new Quaternionf().rotateZ((float) Math.toRadians(45));

                        Vector3f translation = new Vector3f(-THICKNESS / 2f, -THICKNESS / 2f, 0f);
                        rotation.transform(translation);

                        Transformation transformation = new Transformation(
                                translation,
                                rotation,
                                new Vector3f(THICKNESS, THICKNESS, (float) segLen),
                                rollRotation
                        );
                        display.setTransformation(transformation);
                        currentDisplays.add(display);
                    }
                    prevLoc = nextLoc;
                    if (s >= totalLength) break;
                }
                stepCount++;
            }
        }.runTaskTimer(fluffy, 0L, 2L);
    }

    private void spawnPulsarBeam(Player player, LaserProfile profile, boolean isRight) {
        new BukkitRunnable() {
            private final List<BlockDisplay> currentDisplays = new ArrayList<>();
            private long stepCount = 0;
            private final long maxSteps = 6;

            @Override
            public void run() {
                if (!player.isOnline() || stepCount >= maxSteps) {
                    for (BlockDisplay d : currentDisplays) {
                        if (d.isValid()) d.remove();
                    }
                    cancel();
                    return;
                }

                for (BlockDisplay d : currentDisplays) {
                    if (d.isValid()) d.remove();
                }
                currentDisplays.clear();

                BeamTraceResult trace = traceBeam(player, isRight);
                Location start = trace.origin();
                Vector direction = trace.direction();
                double totalLength = trace.distance();
                World world = player.getWorld();

                spawnParticlesAlongBeam(world, start, direction, totalLength, profile);

                double dashLength = 1.0;
                double gapLength = 0.6;
                double period = dashLength + gapLength;
                double travelOffset = (stepCount * 0.3) % period;

                Vector dirNormalized = direction.clone().normalize();
                double currentS = -period + travelOffset;

                while (currentS < totalLength) {
                    double startS = Math.max(0.0, currentS);
                    double endS = Math.min(totalLength, currentS + dashLength);

                    if (endS > startS) {
                        Location segStart = start.clone().add(dirNormalized.clone().multiply(startS));
                        Location segEnd = start.clone().add(dirNormalized.clone().multiply(endS));

                        Vector segDir = segEnd.clone().subtract(segStart).toVector();
                        double segLen = segDir.length();
                        if (segLen > 0.001) {
                            segDir.normalize();

                            Location spawnLoc = segStart.clone();
                            spawnLoc.setYaw(0f);
                            spawnLoc.setPitch(0f);

                            BlockData blockData = getBlockData(profile);

                            BlockDisplay display = world.spawn(spawnLoc, BlockDisplay.class, d -> {
                                d.setBlock(blockData);
                                d.setBrightness(new Display.Brightness(15, 15));
                                d.setBillboard(Display.Billboard.FIXED);
                                d.setShadowRadius(0f);
                                d.setShadowStrength(0f);
                                d.setPersistent(false);
                                d.setInterpolationDuration(0);
                                d.setInterpolationDelay(0);
                            });

                            Vector3f zAxis = new Vector3f(0, 0, 1);
                            Vector3f aim = new Vector3f((float) segDir.getX(), (float) segDir.getY(), (float) segDir.getZ());
                            if (aim.lengthSquared() > 0) aim.normalize();

                            Quaternionf rotation = new Quaternionf().rotationTo(zAxis, aim);
                            Quaternionf rollRotation = new Quaternionf().rotateZ((float) Math.toRadians(45));

                            Vector3f translation = new Vector3f(-THICKNESS / 2f, -THICKNESS / 2f, 0f);
                            rotation.transform(translation);

                            Transformation transformation = new Transformation(
                                    translation,
                                    rotation,
                                    new Vector3f(THICKNESS, THICKNESS, (float) segLen),
                                    rollRotation
                            );
                            display.setTransformation(transformation);
                            currentDisplays.add(display);
                        }
                    }
                    currentS += period;
                }
                stepCount++;
            }
        }.runTaskTimer(fluffy, 0L, 2L);
    }
}