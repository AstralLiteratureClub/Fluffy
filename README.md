# Fluffy Combat Manager
Fluffy is a combat manager designed to make combat kills and tags easier to detect. Fluffy is designed to detect from ender crystals, respawn anchors to even fire damage from player placed lava or fire blocks.

Different in fluffy? Fluffy tracks all tags and contains multiple tags per player.

## Detections
Fluffy is designed to detect multiple types of damages here are the currently (hopefully) working combat detections.

- Player melee, ranged
- Player respawn anchor explosion
- Player ender crystal explosion
- Player TNT explosion
- Player bed explosion
- TNT ignition from (any tracked) player's explosion
- Player splash potions (all types listed in the config)
- Player lingering potions (all types listed in the config)
- Dispenser lingering and splash potions

## Statistics counted
Fluffy tracks few explosion-specific kills and totems (and of course kills, deaths, killstreak, deathstreak)

### Explosions
- Respawn Anchor kills
- Ender Crystal kills
- Bed kills
- TNT kills

### Global
- Total kills
- Total deaths
- Total killstreak
- Total deathstreak
- Totem kills (Activated another player's totem of undying)
- Totem deaths (Another player-activated player's own totem of undying)

# Glowing
Fluffy works with [Apollo (Lunar Client)](https://lunarclient.dev/) to attackers glow.
Lunar client supports different glowing colors compared to vanilla minecraft.
Using Apollo Fluffy can send any combat tag color to the lunar client player.

Fluffy also has the ability
to use [Glowing Entities](https://www.spigotmc.org/threads/glowing-entities-and-blocks-1-17-1-20.558927/)
to make players glow,
but using glowing entities has less support for colors
and using [TAB (plugin)](https://www.spigotmc.org/resources/tab-1-5-1-20-6.57806/) will break the glowing colors
and will only send white glowing colors.
There is no way to use TAB and Fluffy with glowing entities.
Using Lunar Client is much easier to provide glowing colors for own combat-tagged players. 

# Expansions? Addons?
Fluffy will not custom expansions/addons to the plugin.
You can create your own expansions by creating a new plugin and just using the API inside the plugin.

Other combat management plugins have 10+ expansions/addons.
Having 1 single plugin is the reason why fluffy was created.
Having 10 different expansions/addons which should have been built inside the plugin is not ideal for anyone,
as you need to download multiple jar files for them.
In fluffy you can just disable features in the config.

### Dependencies
[Messenger](https://github.com/AstralLiteratureClub/MessageManager) to parse and send messages to players.
[Aura](https://github.com/Antritus/Aura) to show glowing effect to players and entities.

# Project setup
When setting up the project, you need to download the hooks which require local jars.

### Option 2: Using the Command Line
If you are building from a terminal, run the setup task manually to download the required local assets before compiling:

```bash
# On Mac/Linux:
./gradlew downloadHooks

# On Windows:
gradlew downloadHooks
```