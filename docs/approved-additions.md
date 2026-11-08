# Implemented trophy rules and selected defaults

The README describes the other implemented systems. These rules incorporate the user's final corrections; there is no thirst integration, offhand-head bonus, banner or flag feature.

## Ownership and acquisition

Each player can have one placed trophy of each type at a time. Only its owner can attune, with one selection active at a time. Right-clicking another owned trophy replaces the active selection, including while holding an item; the old trophy's bonuses and drawbacks stop. Selection survives logout and unloaded trophy chunks, but death or destruction ends it. Breaking drops nothing and frees the owner's placement slot for that type, so a newly crafted replacement can be placed. Death and chunk unloading do not free a slot while its trophy remains standing. Placed trophies cannot be recovered by pistons.

Normal trophy-mob head drops are configurable: 0.1% without Looting, 0.4% with I, 0.9% with II, 1.1% with III or higher. Player-head acquisition and charged-creeper drops remain unchanged. Enderman Heads use the normal rates; Dragon Heads are no longer trophy ingredients. Heads have one dedicated enchantment, **Sigil**, offered only in the level-30 enchanting-table slot with normal XP/lapis costs. It only enables crafting. No vanilla head enchantments qualify.

One matching Sigil-enchanted head plus one Trophy Mount crafts a trophy. Mount recipe:

```text
Stick       Iron Ingot   Stick
Stick       Leather      Stick
Wooden Slab Wooden Slab  Wooden Slab
```

## Effects

| Trophy | Bonuses | Drawbacks | Particles |
| --- | --- | --- | --- |
| Zombie | Resistance-I-equivalent 20% reduction, stacking with other resistance | 25% faster hunger exhaustion | Red rising motes |
| Skeleton | 15% more ranged damage | 15% more incoming damage | White rising motes |
| Creeper | 40% less explosion damage, movement sounds 50% quieter, hostile detection 25% lower | 20% less melee damage | Green rising motes |
| Wither Skeleton | Melee hits have 20% chance of Wither I for four seconds | Half a heart every 30 active seconds | Charcoal rising motes |
| Stray | 15% more damage; attacks apply Slowness I for four seconds, except skeleton-family mobs and polar bears | 25% more fire damage taken | Ice-blue rising motes |
| Bogged | 15% more damage; attacks apply Poison I for four seconds, except undead | 25% more damage from Endermen, Endermites, Dragon, End Crystals and dragon breath | Poison-green rising motes |
| Piglin | Gold weapons 15% more damage; gold equipment 30% less durability loss; each gold armor piece adds one armor point | XP orbs destroyed before XP or Mending | Gold rising motes |
| Enderman | Endermen/Endermites neutral unless attacked; Night Vision; purple player particles | Water/rain costs half a heart per second; nightly phantoms ignore sleep history | Purple rising motes |
| Player | 15% more damage to other players | Nametag visible 50% farther away, with full range and visibility even while crouching | Pale-blue rising motes |

Placed trophies keep the orientation chosen at placement, with their fronts toward the placing player. All trophies emit sparse, slow-rising motes; the owner's currently selected trophy emits faster, larger, denser motes with a swirling motion. Hovering its inventory status icon shows every bonus and drawback in both compact and expanded effect layouts. Ordinary damage, armor, enchantments and compatible effects continue stacking. Phantom spawning retains normal sky and difficulty restrictions apart from sleep history.

## Delegated choices

Spawner Eyes use six variants: rotten flesh/zombie, bone/skeleton, string/spider, spider eye/cave spider, blaze rod/blaze and magma cream/magma cube. Their search is limited to a 128-block sphere of loaded chunks, chooses the closest matching normal spawner, and follows vanilla Eye flight/recovery on success. Recipe unlocks use ingredient possession; advancements use obtaining the new items, without additional progression rewards.

Weather defaults are configurable periodic exposure checks, not lightning-strike proximity. Lanterns roll only storm exposure; torches roll storm or ordinary rain, never both for one check. Soul protection is placed-source-only with solid-block line of sight. Trophies use the supplied Blockbench model with stripped spruce and vanilla mob-head textures; other decorative assets may still use placeholders.

Endermen drop Enderman Heads using the shared head-drop configuration: 0.1%, 0.4%, 0.9%, and 1.1% by default at Looting 0/I/II/III (higher levels use III). Apply Sigil to an Enderman Head, then combine it with a Trophy Mount to craft the Enderman Trophy. Dragon heads do not satisfy this recipe.
