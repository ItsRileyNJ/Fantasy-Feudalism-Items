# Implemented trophy rules and selected defaults

The README describes the other implemented systems. These rules incorporate the user's final corrections; there is no thirst integration, offhand-head bonus, banner or flag feature.

## Ownership and acquisition

Each player can permanently place one trophy of each type, ever. Only its owner can attune, with one selection active at a time. Selection survives logout and unloaded trophy chunks, but death or destruction ends it. Breaking drops nothing; destruction does not restore the placement allowance. Placed trophies cannot be recovered by pistons.

Normal trophy-mob head drops are configurable: 0.1% without Looting, 0.4% with I, 0.9% with II, 1.1% with III or higher. Special Player/Dragon acquisition and charged-creeper drops remain. Heads have one dedicated enchantment, **Sigil**, offered only in the level-30 enchanting-table slot with normal XP/lapis costs. It only enables crafting. No vanilla head enchantments qualify.

One matching Sigil-enchanted head plus one Trophy Mount crafts a trophy. Mount recipe:

```text
Stick       Iron Ingot   Stick
Stick       Leather      Stick
Wooden Slab Wooden Slab  Wooden Slab
```

## Effects

| Trophy | Bonuses | Drawbacks | Particles |
| --- | --- | --- | --- |
| Zombie | Resistance-I-equivalent 20% reduction, stacking with other resistance | 25% faster hunger exhaustion | Red dust |
| Skeleton | 15% more ranged damage | 15% more incoming damage | White dust |
| Creeper | 40% less explosion damage, movement sounds 50% quieter, hostile detection 25% lower | 20% less melee damage | Green dust |
| Wither Skeleton | Melee hits have 20% chance of Wither I for four seconds | Half a heart every 30 active seconds | Black smoke |
| Stray | 15% more damage; attacks apply Slowness I for four seconds, except skeleton-family mobs and polar bears | 25% more fire damage taken | Snow |
| Bogged | 15% more damage; attacks apply Poison I for four seconds, except undead | 25% more damage from Endermen, Endermites, Dragon, End Crystals and dragon breath | Poison-green dust |
| Piglin | Gold weapons 15% more damage; gold equipment 30% less durability loss; each gold armor piece adds one armor point | XP orbs destroyed before XP or Mending | Gold dust |
| Ender Dragon | Endermen/Endermites neutral unless attacked; Night Vision; purple player particles | Water/rain costs half a heart per second; nightly phantoms ignore sleep history | Portal |
| Player | 15% more damage to other players | Nametag visible 50% farther away | Enchantment |

Placed trophies always emit sparse particles. The owner's currently selected trophy emits them faster and denser. Tooltips describe bonuses and drawbacks. Ordinary damage, armor, enchantments and compatible effects continue stacking. Phantom spawning retains normal sky and difficulty restrictions apart from sleep history.

## Delegated choices

Spawner Eyes use six variants: rotten flesh/zombie, bone/skeleton, string/spider, spider eye/cave spider, blaze rod/blaze and magma cream/magma cube. Their search is limited to a 128-block sphere of loaded chunks, chooses the closest matching normal spawner, and follows vanilla Eye flight/recovery on success. Recipe unlocks use ingredient possession; advancements use obtaining the new items, without additional progression rewards.

Weather defaults are configurable periodic exposure checks, not lightning-strike proximity. Lanterns roll only storm exposure; torches roll storm or ordinary rain, never both for one check. Soul protection is placed-source-only with solid-block line of sight. Decorative assets are placeholders approved for this implementation.
