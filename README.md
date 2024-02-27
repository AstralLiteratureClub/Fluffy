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
Fluffy tracks few explosion specific kills and totems (and ofcourse kills, deaths, killstreak, deathstreak)

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
Fluffy allows players to glow when they are tagged. Players by default will glow red, if the player is latest in combat tag list. Other players will glow orange (which have combat tag with the player).
### TAB (Plugin)
When a server is using TAB plugin, it's advised to turn off the glowing feature.
TAB plugin makes the glowing colors not work correctly and only displays them as white.
TAB is using custom teams for each player and doesn't like the current glowing system.

# Expansions? Addons?
Fluffy will not custom expansions/addons to the plugin.
You can create your own expansions by creating a new plugin and just using the API inside the plugin.

Other combat management plugins have 10+ expansions/addons.
Having 1 single plugin is the reason why fluffy was created.
Having 10 different expansions/addons which should have been built inside the plugin is not ideal for anyone,
as you need to download multiple jar files for them.
In fluffy you can just disable features in the config.