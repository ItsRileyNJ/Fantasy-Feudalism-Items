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
