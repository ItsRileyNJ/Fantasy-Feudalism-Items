package com.fantasyfeudalism.items.clairvoyance;

import java.util.Set;
import java.util.UUID;
import net.minecraft.resources.ResourceLocation;

/** World identity and a short lease prevent stale outlines across reconnects and level changes. */
public final class ClairvoyanceView {
    private Set<UUID> targets = Set.of();
    private ResourceLocation dimension;
    private Object world;
    private long receivedAt;
    public void receive(ClairvoyancePayload payload, Object currentWorld, ResourceLocation currentDimension, long now) {
        if (currentWorld == null || !currentDimension.equals(payload.dimension())) return;
        world = currentWorld;
        dimension = currentDimension;
        targets = Set.copyOf(payload.targets());
        receivedAt = now;
    }
    public boolean highlights(UUID target, Object currentWorld, ResourceLocation currentDimension, long now) {
        return currentWorld != null && currentWorld == world && currentDimension.equals(dimension)
            && now >= receivedAt && now - receivedAt < 20 && targets.contains(target);
    }
}
