# Validation — 2026-10-30

Target: Minecraft 1.21.1, NeoForge 21.1.262, Java 21.

| Run | Result |
| --- | --- |
| Dedicated GameTest server with Mercenaries 1.17.1 | 48 passed, zero failures |
| Dedicated GameTest server with neither optional mod | 46 passed, zero failures |
| Graphical client with Mercenaries 1.17.1 and LambDynamicLights 4.8.11+1.21.1 | 10 smoke checks passed |
| Release JAR inspection | JSON parsed; no GameTest classes or bundled third-party JARs |

The server suites cover brewing and delivery, private Clairvoyance targeting and exclusions, fuel and weather, soul protection and walls, milk/cauldrons, animal following, saddlebag persistence and restrictions, mounted equipment, horseshoe stats, smithing serialization, dagger penetration and enchantments, head enchanting and drops, trophy ownership and effects, fishing rolls, Spawner Eye targeting, loot gating, and compatibility trades/weapon slots. A regression test verifies mounted fuel consumption is independent of mount spawn age.

The client checks query LambDynamicLights itself for torch, lantern, soul-lantern, off/burned-out and mounted brightness; verify synced saddlebag dye and lantern switching; and open/synchronize portable storage and the mount equipment screen. The earlier Clairvoyance visual check verified an invisible sheep outlined through a stone wall and disappearance after effect removal.

These are development integration checks, not a long-running multiplayer playtest. Placeholder visuals are intentional. The cloud client had no audio device or access to online account-service endpoints; it successfully joined the local world. The supplied lighting mod also logs a nonfatal missing Pride access-transformer warning. No external mod JAR was modified.

Machine-readable reports and a mount-equipment screenshot accompany the packaged artifacts under `validation/`.

## 0.1.1 lantern compatibility fix

The supplied Supplementaries Squared 1.21-1.2.18 JAR was inspected: `LightableLanternBlock` calls the vanilla state builder and then adds its own `lit` property; `CopperLanternBlock` adds an axis afterward. Items previously added `lit` to every subclass through the vanilla builder, causing duplicate-property registration failure.

A test-only matching hierarchy reproduced the exact `has duplicate property: lit` startup failure before the patch. Items now adds its property and default only for exact vanilla `LanternBlock` instances, including vanilla soul lanterns. The patched build passed **49 dedicated-server tests with Mercenaries**, including registration and state checks for plain, lightable and copper-style subclasses. Existing vanilla fuel, toggling and weather tests also pass. Fixture blocks and test classes are excluded from the release JAR.

The complete supplied combination was subsequently verified: **Items 0.1.1 + Supplementaries Squared 1.21-1.2.18 + Moonlight 1.21.1-3.7.2 + Supplementaries 1.21.1-4.0.2**, alongside Mercenaries 1.17.1, passed **all 49 server tests**. Tests additionally check the actual copper and brass lanterns' registration, native lit defaults and light emission when on/off, and the actual crimson lantern's native state and emission. Crimson uses `attached`, not a `lit` state; Items preserves that distinction. The test-player harness negotiates mod channels before login to support Moonlight's data-sync packets.

The graphical client loaded the same combination plus LambDynamicLights 4.8.11+1.21.1, joined the local test world, and passed **all 10 client smoke checks**. It was then stopped deliberately. No additional production-code fix was needed after supplying the dependencies: the 0.1.1 JAR is byte-for-byte identical to the previously provided patch. No third-party binaries were modified or bundled. Items does not add its fuel or weather mechanics to other mods' lanterns.

## 0.1.2 rendering, synchronization and tooltip review

- Full supplied mod set, including Macaw's Lights 1.1.5: **51 server tests passed** and **14 client smoke checks passed**.
- Without optional mods: **49 server tests passed**.
- New checks verify main/offhand re-equip behavior, real hotbar changes, unrelated item changes, fuel persistence, timer-only attachment stability, cached-stack reuse and invalidation when a light switches off.
- Reviewed light predicates, dynamic-light adapters, item fuel, mount rendering and synchronization, horseshoe modifiers, soul-source lookup, trophy persistence/effects/particles, container validation, animal-following updates, fishing timers, Clairvoyance targeting and Spawner Eye search. This is a code review and regression sweep, not a guarantee that every modpack interaction is bug-free.

Rendering reads use the immutable CustomData backing tag without modifying it. State updates still use copy-on-write. Brightness checks no longer read server config or allocate copies of custom NBT. Mounted render stacks are decoded once per synchronized attachment replacement; weak entity keys avoid retaining unloaded worlds. Only appearance changes synchronize equipment visuals; the server continues persisting exact fuel. Horseshoe attribute modifiers are changed only when their value changes.

Trophies now have separate translated benefit/drawback lines and consistent instructions for owner attunement and permanent placement. Their gameplay effects are unchanged.

The graphical client was exercised with lantern search results in the creative menu, using the complete supplied mod set, and CPU profiles were captured. The severe reported frame drop was not reproduced in this cloud setup; no before/after FPS improvement is claimed. A hardware-rendered client and the user's complete modpack may behave differently. The fixes address confirmed allocation, synchronization and hand-animation issues; the user's in-game retest remains necessary to confirm the original frame-rate symptom is resolved.

## Full uploaded modpack follow-up

Ten ZIPs supplied 195 top-level mod JARs, with no missing declared required dependencies. The pack contained Items **0.1.1**; the isolated run replaced that copy with **0.1.2**. Galosphere 1.5.5 was intentionally omitted. The complete available set entered the fixture world and passed all **14 client smoke checks**. Complementary Unbound r5.9.3 was then enabled through Iris, compiled and rendered; vanilla-lantern holding and creative-menu lantern search/hover were exercised. Diagnostic JFR files and a detailed report are saved with the artifacts.

This test used generated configs and the fixture world because user configs/options/saves were not supplied. The user reports 8 GB allocation and launching through CurseForge/vanilla; the test heap was approximately 8 GB. Software rendering limits FPS comparison. The reported severe lag has not been confirmed resolved; update the uploaded 0.1.1 to 0.1.2 and compare shader-on versus shader-off behavior on the user's hardware. No further production change resulted from this run.

## 0.1.3 built-in dynamic lighting

Replaces the LambDynamicLights adapters with a client-only vanilla-renderer hook.
Sources include either hand of living entities, dropped light items, and mounted
lanterns. Fuel, toggles, and soul-light brightness use the existing item-state
logic. Immutable per-section source snapshots are safe for chunk build workers;
changed and removed sources invalidate both their previous and current areas.
Updates run every three unpaused client ticks, and world changes clear snapshots.
Packed-light merging preserves sky light and brighter existing block light.

Client smoke checks now call the built-in provider and cover distance falloff,
range limits, packed-light preservation, dropped items, both hands, mounted
lanterns, fuel exhaustion, and switching off. Historical third-party-renderer and
shader results above apply to earlier versions; they do not validate this new
rendering hook. Built-in light travels through walls and is visual only.

Validation for this change: Java 21 compilation (including client smoke sources)
and the full `build` lifecycle passed with **49/49 dedicated-server tests**, with
no optional mods installed. The updated graphical client smoke suite has not
been executed in this workspace; terrain illumination, movement/removal visuals,
and shader/alternative renderer compatibility remain unverified in-game.

### Shader guard

The render-frame and client-tick handlers query Iris (including the legacy
Oculus API) and OptiFine shader state. While a shader pack is active, the
packed-light hook returns vanilla lighting immediately, source collection stops,
and all cached sources are discarded. Sections previously affected by built-in
lighting are scheduled for rebuilding. Detection runs before pause/tick
throttling, so shader toggles while paused also clear illumination. Disabling
shaders allows fresh source collection to resume. Missing shader providers leave
lighting available; a detected provider with a broken or incompatible API fails
closed and keeps built-in lighting disabled.

`shaderCompatibilityTest` exercises isolated current/legacy Iris and OptiFine
API fixtures, toggling packs on and off, absent providers, broken calls, missing
methods and an installed Iris with no expected API. This is included in `check`;
fixtures are excluded from the release JAR. Actual shader-pack rendering and
terrain rebuilds have not been checked in a graphical client in this workspace.

Shader-guard validation: the full build passed, including all shader detection
fixture checks and all 49 dedicated-server integration tests.

### Simpler Vial of Milk cleansing

Each vial now removes exactly one randomly selected active effect in Minecraft's
`HARMFUL` category, including its full strength, duration and any hidden weaker
instance. Other harmful effects, beneficial effects (including Clairvoyance),
and neutral effects remain unchanged. With no harmful effect, drinking still
returns the bottle and restores the existing 2.5 food points without changing
any effects. Cauldron filling, brewing and creative consumption are unchanged.

Regression checks cover multiple harmful effects, repeated drinks, strong and
infinite effects, hidden weaker instances, beneficial/neutral preservation,
no active effects, stacked bottle returns and fractional food restoration.

### Emissive lights, faster updates and trophy switching

Lit normal/soul torch models and lantern light chambers use NeoForge emissive
model data for held items and placed blocks. Wall torches and hanging lanterns
are covered. Unlit models inherit ordinary vanilla geometry without emissive
data; lantern caps and chains retain normal shading. Torch items keep their
sprite silhouette using NeoForge's item-layer loader. Dynamic source snapshots
now refresh every three unpaused ticks (150 ms at 20 TPS), with shader shutdown
unchanged. Model emission is separate from dynamic illumination of surroundings.

Owned trophies explicitly handle item-bearing interactions in either hand and
replace the current attunement. Server regression tests verify empty-hand and
held-item selection, switching back, synchronized and persistent selection,
old drawback removal, previous Night Vision cleanup, ownership checks and
preservation of the active selection when an inactive trophy is destroyed.

Validation: full build, shader API checks and **53/53 dedicated-server tests**
passed. An inheritance-aware resource check passed **36 lit/unlit held/placed
model checks**, including ordinary shading on lantern caps and chains.
Graphical launch was attempted but failed at GLFW initialization: no active X11
display. The selected environment
`ccarenv_b64_Y2NhcmVudl8zN2NjYmRhNjZjMTA4MTkxOTBmYjMyZTIwMzZhMjA1Yg`
reports desktop support with state unknown; no desktop JavaScript control tool
was offered. In-game appearance remains unverified in this workspace.

### Trophy tooltip grammar, crouched nametags and stable Clairvoyance

All trophy descriptions now use consistent, grammatical wording and explicit
units. Shared instructions explain ownership, switching, exclusive attunement
and permanent placement. Breaking an inactive trophy is no longer described as
ending the active attunement. The generator and English translations agree.

The Player Trophy ignores crouching only for nametag range and name rendering.
It keeps the standing 50% range extension (96 blocks with vanilla defaults) and
standing-style visibility while crouched. Normal invisibility, camera and team
visibility checks remain in place; other trophies and non-player entities retain
vanilla behavior. A regression checks standing/crouched selection, switching away
and non-player behavior.

Clairvoyance no longer expires a target snapshot after one second or compares
client world clocks. The server replaces snapshots every five ticks; empty
snapshots clear them. Effect loss/death clears the client cache, and highlights
still require a living, affected viewer and matching world/dimension. The render
loop includes highlighted targets even when entity culling or an uncompiled
terrain section would otherwise skip them. Boss/creative/operator exclusions and
server entity-tracking limits are unchanged. Cache tests cover snapshot
replacement, explicit clearing, world changes and wrong-dimension updates.

Validation: full build and shader API checks passed, with **54/54 server tests**.
All 29 per-trophy description translations match their source text. Rendering
hooks compile and their invocation targets were checked against Minecraft's
patched sources/bytecode. The graphical client remains blocked by the missing
display described above; these results do not establish compatibility with every
shader or entity-culling mod.

### Active trophy status indicator

Each of the nine trophy types has an informational status effect named after the
trophy. The HUD and inventory effect list render its actual trophy item model as
the icon. It has no attribute modifiers or ticking gameplay effects, no added
particles, and an infinite duration while attuned. The ledger remains the source
of trophy benefits and drawbacks; applying a marker alone grants no attunement.

Selecting a trophy immediately replaces its marker. Destruction of the active
trophy and death/respawn clear it; the regular server update reconciles missing
or stale indicators, including after loading. Milk does not change attunement,
so neither milk buckets nor Vials of Milk remove its indicator. Unchanged markers
are retained rather than reapplied or synchronized every second.

Validation: full build, shader checks and **56/56 server tests** passed. Regression
coverage includes all nine selections, single-marker exclusivity, stable instance
reuse, switching, active/inactive trophy destruction, death cleanup, milk and
manual-effect removal/reconciliation. All nine localized effect names match the
trophy names. Client icon hooks compile; graphical layout remains unverified
because the environment has no active display, as documented above.

### Supplied Blockbench trophy model

`models/Trophy.bbmodel` preserves the uploaded source. All nine trophy block
models now use its four cubes unchanged: 10x2x10 base, 4x1x4 collar, 2x3x2 post,
and 8x8x8 head from [4,6,4] to [12,14,12]. The three wooden pieces preserve the
source face UVs and use stripped spruce log sides/end grain. Head faces use the
vanilla head/skull texture regions, normalized to each sheet's dimensions. Piglin
and Ender Dragon use their own head UV layouts; the generic Player Trophy uses
Steve. The supplied cube geometry is retained rather than replacing it with
vanilla skull meshes or adding ears, snouts or other mob geometry.

The block atlas adds the vanilla entity textures via single-sprite references,
without copying textures, so resource-pack replacements remain usable. Trophy
inventory models and the attunement status icons inherit these updated models.
The block interaction shape matches all four supplied cubes.

`python3 scripts/trophy-models.py` regenerates the models directly. The normal
content generator invokes the same exporter instead of writing placeholder
cubes. Validation checked all nine models against every source cube, stand UVs,
vanilla texture-file existence/dimensions, normalized head UVs, atlas references,
and inventory inheritance. Both direct and full content regeneration preserve
these assets. Full build, shader checks and **57/57 server tests** passed,
including all trophy interaction shapes. The new artwork has not been visually
verified in a running client; the earlier graphical-display limitation remains.

### Vanilla potion appearance and dedicated item sprites

The Animal Allegiance item-color handler now matches vanilla 1.21's opaque ARGB
potion tint: the liquid receives an opaque PotionContents color, and the glass
and stopper layer receives opaque white (-1). The former RGB-only values had
zero alpha, causing tinted quads to disappear. All four brewing stages retain
the vanilla potion model's liquid and bottle textures. A regression covers both
layers of all stages and normal/extended liquid-color consistency.

Trophy Mount now uses a dedicated 16x16 transparent wooden pedestal sprite rather
than the armor-stand item. Bone Dagger uses a 32x32 transparent import of the
user's cream bone blade and brown handle design, with vanilla handheld item
transforms. Source artwork is retained under `art/item-sprites/`; release textures
are under the mod's `textures/item/` directory. Images were imported using binary
alpha and nearest-neighbor downsampling with transparent margins. The original
generated files remain intact in the workspace. The content generator references
these sprites and no longer restores the armor-stand/stone-sword placeholders.

Validation: full build, shader checks and **58/58 server tests** passed. Both
imported sprites were visually inspected at inventory scale, have transparent
backgrounds, and keep all artwork within their canvases. Isolated full resource
regeneration preserves both models and textures. Actual in-game presentation
remains unverified because the graphical display limitation above persists.

### Layered Bogged and Stray heads

The Bogged skin stores important facial details on `bogged_overlay`, which was
missing from the trophy. Both skeleton variants now use their base and outer
head textures, with cutout rendering so transparent texels do not become opaque
patches. Outer cubes use the vanilla expansions (Bogged 0.2, Stray 0.25 model
units); the supplied stand and base head cubes remain unchanged. No new mushroom
or other extra decorative geometry is introduced.

The mod's Bogged and Stray head drops now use 3D models with the same skin layers
and head UVs as the trophies. The old flat placeholder PNGs are removed. Their
existing item registrations, drops and Sigil recipes are unchanged. Both base
and overlay sheets are referenced through the block atlas, retaining resource
pack compatibility. The content exporter regenerates both standalone head models
and their trophy layers, and no longer writes placeholder head sprites.

Validation: full build, shader checks and **58/58 server tests** passed. Asset
checks confirm 64x32 source sheets, correct head-face UVs, expanded overlay bounds,
cutout rendering, exact preservation of the four supplied base cubes, removal of
placeholder references and stable full regeneration. The composed Bogged face
was visually inspected and contains its missing facial detail. Full in-game 3D
rendering remains unverified under the existing display limitation.

### One standing trophy per owner and type

The placement ledger now releases a type's reservation when its trophy block is
removed. A player can place a newly crafted trophy of that type afterward, but
cannot place another while the replacement is standing. Removal frees the
original owner's slot even if someone else breaks the trophy. Other trophy types
and owners keep their own slots. Destroying the active trophy still clears its
attunement/status indicator; destroying an inactive trophy preserves the current
selection. Broken trophies still drop nothing.

Death, logout and chunk unloading do not release slots for standing trophies.
The saved-data format is retained, so existing placed trophies remain counted
and release their slots when broken under this update. This change does not
blindly reset legacy flags, which also protect standing trophies in unloaded
chunks; historical flags for trophies destroyed before this update are not
automatically distinguishable from those standing trophies.

Current tooltips, placement-denial messages, controls and rules now describe the
one-at-a-time limit instead of lifetime placement. The resource generator keeps
the replacement wording. Validation: full build, shader checks and **60/60 server
tests** passed, including duplicate rejection, replacement at a new position,
another player breaking the trophy, independent owners/types, inactive trophy
attunement, and persistence of occupied and freed slots across save/reload.

### Horseshoe material sprites

All five tiers now use the user's two-overlapping-horseshoes design instead of
material ingot/gem placeholders. A shared 32x32 neutral metal sprite preserves
identical shape, nail holes, shading and transparency across tiers. Client item
colors provide opaque copper, iron, gold, diamond and netherite palettes. This
uses vanilla item-layer tinting; no per-frame texture generation or copies are
needed. The gold jump bonus and all speed/fall modifiers and recipes are retained.
The existing Horseshoe constructor remains available for compatibility.

Source artwork is retained at `art/item-sprites/horseshoe.png`; the release sprite
is `textures/item/horseshoe.png`. The content generator references it for every
horseshoe tier. Validation: full build, shader checks and **61/61 server tests**
passed, including distinct opaque material tints, layer isolation and unchanged
bonuses. The inventory-scale sprite was visually inspected; all five generated
models use the same sprite and survive full regeneration. Full in-game color
presentation remains unverified under the existing graphical-display limitation.

### Placement orientation, upward aura and inventory hover details

Trophy blocks now store horizontal facing, chosen opposite the placing player's
heading so their fronts face that player. The direction stays fixed after
placement and supports structure rotation/mirroring. All nine exported blockstate
files include the four horizontal rotations. Older states without a facing
property use the default north orientation; no camera tracking is added.

All heads now emit colored, gravity-free rising motes. Idle trophies emit one
small, slow-rising mote per second. Active trophies emit six larger motes every
two ticks (60/second), with faster ascent, more spread and a swirling motion.
Client particle physics ensures positive upward velocity, fades particles over
their lifetime, and retains each trophy's identifying color. Particle providers
respect normal client particle limits and reuse vanilla dust sprites.

The attunement status icon's inventory hover now shows the trophy name, all
benefits and all drawbacks using the same translated descriptions as its item.
Compact layouts use NeoForge's effect-tooltip event; expanded layouts add hover
details over the effect icon after its labels render, using the actual rendered
layout positions. This does not force compact mode or change ordinary effects.
Placement instructions stay on trophy items rather than the status tooltip.

Validation: full build, shader checks and **64/64 server tests** passed. New tests
cover all nine trophies placed in all four player orientations, fixed direction
after player movement, structure rotation, every status tooltip's complete effect
summary, normal-effect isolation, rising velocities, stronger active profiles and
particle color/type packet roundtrips. Asset checks verify all 36 facing variants,
vanilla sprite references, retained layered head geometry and regeneration. The
expanded-tooltip invocation target was checked against patched Minecraft
bytecode. Full in-game GUI/particle presentation remains unverified due to the
previously documented lack of an active graphical display.

### Full Piglin skull and Enderman trophy head

The Piglin trophy now reproduces the vanilla PiglinHeadModel/PiglinModel head:
10x8x8 head, 4x4x1 snout, both 1x2x1 tusks, and both 1x5x4 ears. Each part uses
its native piglin texture region. NeoForge composite child transforms retain the
real +/-30-degree ear angles instead of approximating them with vanilla element
rotation increments. The three supplied wooden stand pieces are unchanged.

The Ender Dragon trophy's appearance now uses the 8x8 Enderman head layout and
vanilla Enderman texture. Its separate purple eye texture is added as a cutout,
emissive layer. Existing trophy IDs, names, ingredients, effects and placement
orientation are unchanged. The updated inventory and attunement icons inherit
the same models. Both changes are retained by the model/content exporter.

Validation: full build, shader checks and **64/64 server tests** passed. Asset
checks verify piglin part bounds, source UVs, ear pivots/angles, unchanged stand,
Enderman face UVs, emissive eye data, vanilla texture paths/dimensions and stable
full regeneration. The NeoForge composite loader and root-transform structure
were checked against its implementation. Full in-game appearance remains
unverified due to the existing graphical-display limitation.
