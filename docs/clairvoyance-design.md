# Clairvoyance implementation

All mobs and players are eligible, including invisible entities and targets through walls. Bosses, operators and creative players are excluded. The single white outline color is visible only to affected viewers. Radius and duration are configurable, defaulting to 24 blocks and two minutes. Vanilla splash, lingering and tipped-arrow delivery forms retain vanilla duration scaling; no stronger or extended variant is added.

Brewing: Water Bottle + Glowstone Dust → Thick Potion; Thick Potion + Amethyst Shard → Clairvoyance.

The server selects eligible targets and sends UUIDs only to affected viewers. Clients request no targets and never change shared glowing metadata. The renderer checks the viewer's effect, world identity, dimension and short packet lease. Modded bosses can join the `neoforge:bosses` entity-type tag. Normal server entity tracking limits what clients can render.

A vanilla milk bucket cures the effect normally. A Vial of Milk applies its separate level-and-duration reduction rule; Clairvoyance is level I, so the vial removes it too.
