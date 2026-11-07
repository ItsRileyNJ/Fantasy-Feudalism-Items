# Clairvoyance implementation

All mobs and players are eligible, including invisible entities and targets through walls. Bosses, operators and creative players are excluded. The single white outline color is visible only to affected viewers. Radius and duration are configurable, defaulting to 24 blocks and two minutes. Vanilla splash, lingering and tipped-arrow delivery forms retain vanilla duration scaling; no stronger or extended variant is added.

Brewing: Water Bottle + Glowstone Dust → Thick Potion; Thick Potion + Amethyst Shard → Clairvoyance.

The server selects eligible targets and sends UUIDs only to affected viewers. Clients request no targets and never change shared glowing metadata. The renderer checks the viewer's effect, world identity, dimension and latest server target snapshot. Modded bosses can join the `neoforge:bosses` entity-type tag. Normal server entity tracking limits what clients can render.

A vanilla milk bucket cures the effect normally. A Vial of Milk removes one randomly chosen harmful effect entirely. Clairvoyance is beneficial, so the vial preserves it.

Target snapshots remain valid until replaced; there is no one-second expiry or dependence on world time. Client effect loss clears the cache. Highlighted targets bypass vanilla entity culling and the compiled-section gate, so walls and hidden terrain sections do not suppress their outlines. World and dimension checks still reject stale targets. Client entity tracking and third-party rendering pipelines remain relevant.
