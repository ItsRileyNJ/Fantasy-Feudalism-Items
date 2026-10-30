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
