# Fantasy Feudalism Items

Minecraft **1.21.1**, NeoForge **21.1.262**, Java **21**. Install the Items JAR on both client and server. Replace previous Items builds rather than installing two versions. Placeholder textures and models are included. See [controls](docs/controls.md) and [validation](docs/validation.md).

## Included systems

- **Lighting:** vanilla torches burn for seven Minecraft days; lanterns consume fifteen days of fuel. Craft Kelp Oil from kelp and a glass bottle. One bottle completely refuels a lantern and returns the bottle. Only an empty lantern automatically lights after refueling. Shift-right-click toggles lanterns; flint and steel refreshes torches for seven days. Fuel advances while lights are active and loaded. Stored inventory lights pause unless held or mounted.
- **Weather:** exposed torches can extinguish in rain, thunderstorms and water; lanterns only in thunderstorms. Defaults are checks every 200 ticks, 10% rain, 25% torch storm, 100% torch water, and 5% lantern storm. Roofs shelter lights; storms do not also roll ordinary rain. Days, intervals and probabilities are configurable.
- **Soul lights:** unlimited life and immunity to water and weather. Placed soul torches protect sunlight-vulnerable mobs within six blocks; soul lanterns within twelve. Solid walls block protection. Held, dropped and mounted soul lights do not grant protection.
- **Clairvoyance:** white outlines visible only to affected players, through walls and invisibility. Configurable defaults: 24 blocks and 120 seconds. Bosses, operators and creative players are excluded. Water bottle + glowstone makes Thick Potion; add amethyst for Clairvoyance. Vanilla splash, lingering and tipped-arrow conversions apply their usual duration scaling. No upgraded variants.
- **Vial of Milk:** a milk bucket fills a cauldron and yields three bottled vials. Each restores 2.5 food points, returns its glass bottle, halves effect duration and halves effect level rounded down. Level I disappears; II becomes I, III becomes I, IV becomes II. Fractional food is retained between meals and hunger expenditure. No thirst dependency.
- **Animal Allegiance:** brew Vial of Milk → nether wart → honeycomb → rabbit's foot. Drinkable only; 60 seconds, or 180 with redstone. Passive, unowned animals within 32 blocks follow the first affected player until the effect ends, including beyond the trigger radius. Neutral animals are excluded; no teleporting or cross-dimension following.
- **Mount equipment:** horses have separate saddle, armor, horseshoe and lantern slots. Donkeys and mules have saddle, saddlebag and lantern slots. Equipment renders and drops on death. Saddlebags replace chest cargo and migrate existing cargo; one portable, dyeable bag holds 27 shared slots, shown as two side bags. Only empty containers can be nested. Right-click a held bag to open it.
- **Horseshoes:** copper, iron, gold, diamond and netherite give respectively 5/8/11/15/20% speed and 15/25/35/50/70% fall-damage reduction. Gold grants 10% greater jump height. Natural mount stats and other modifiers remain relevant; removing equipment removes bonuses. No durability. Craft seven materials in a U; upgrade diamond + netherite ingot in a smithing table.
- **Bone Dagger:** center-column flint/bone/stick; 4 attack damage, 3.2 attack speed, half a block less entity reach, 100 durability, flint repair, sword-only enchantments and 20% armor penetration. Protection and other reductions still apply. Mobs reject it; the Mercenaries weapon menu rejects it too.
- **Fishing Trap:** master Fisherman sells one for 18 emeralds; no recipe. Place in river water. Every 3 minutes of loaded game time, roll 25% fish first; on failure roll 25% extra catch, then 80% junk/20% treasure. Eight total catches; right-click collects all, and breaking drops contents. No bait or redstone.
- **Spawner Eyes:** zombie, skeleton, spider, cave spider, blaze and magma cube variants. Surround an Eye of Ender with eight corresponding drops. Finds the nearest matching ordinary spawner within 128 blocks in already-loaded chunks. No target costs nothing; successful throws use vanilla flight and 80% recovery. No chunk generation or trial-spawner search.
- **Mob Trophies:** all nine approved types, ownership and permanent placement history, exclusive attunement, particles, bonuses and drawbacks. See [the detailed rules](docs/approved-additions.md). Heads must receive **Sigil** from the level-30 enchanting-table option before crafting with a Trophy Mount. Sigil has no other effect.

All new items appear in the Fantasy Feudalism Items creative tab. Ingredient-based recipe unlocks and obtain-item advancements are included. Banners, flags and offhand head bonuses are deferred.

## Optional integrations

Tested with the supplied **LambDynamicLights 4.8.11+1.21.1** and **Fantasy Feudalism Mercenaries 1.17.1**. Neither is bundled or required. Compatibility code lives entirely in Items; their JARs are unchanged.

Version **0.1.1** fixes a lantern-registration conflict with **Supplementaries Squared 1.21-1.2.18**. Items adds its fuel state only to vanilla lanterns; other mods retain ownership of their lantern states, defaults and behavior. Verified with Supplementaries Squared 1.21-1.2.18, Supplementaries 1.21.1-4.0.2 and Moonlight 1.21.1-3.7.2: 49 server tests and 10 client smoke checks passed.

LambDynamicLights handles held, dropped and mounted light emission, respecting fuel and on/off state. Without it, placed lighting continues normally and mounted lights emit no light.

With Mercenaries, its caravan stock offers one saddlebag for 14 emeralds and four matching Spawner Eyes for 20 emeralds. Village blacksmith/leatherworker chests have a 20% saddlebag chance. **Without Mercenaries, saddlebags and Spawner Eyes are obtained only through crafting.** A saddlebag half uses eight leather around an iron block; combine half/leather/half across a row.

## Configuration and development

Server configuration files are under `<world>/serverconfig/`: `fantasy_feudalism_items-server.toml`, `fantasy_feudalism_items-weather.toml`, `fantasy_feudalism_items-fuel.toml`, and `fantasy_feudalism_items-head-drops.toml`. Head-drop defaults are 0.1%, 0.4%, 0.9%, and 1.1% for Looting 0/I/II/III; higher levels use III. Player and Dragon heads retain special acquisition.

```sh
./gradlew build                  # includes dedicated-server integration tests
./gradlew build -Pcompat         # also test supplied Mercenaries, installed in run/mods
./gradlew runClient
```

The cloud launcher `./scripts/cloud-gradle.sh` selects this workspace's JDK, proxy and cache. Test source lives in `src/gameTest` and is excluded from the release JAR. Tests require a fresh, complete XML report, so startup failures cannot silently pass. Client smoke checks use `runClient -Psmoke` and the prepared local `New World` fixture; this fixture is not distributed.

Dynamic lighting remains client-side visual illumination and does not change server block light. Clairvoyance rendering depends on normal server entity-tracking distance. Modded bosses should join `neoforge:bosses`; custom containers should expose their item contents through standard components. Recipes and models can be regenerated with `python3 scripts/generate-content-resources.py`.
