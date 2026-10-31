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
