package com.fantasyfeudalism.items.clairvoyance;

import java.util.Set;
import java.util.UUID;
import net.minecraft.resources.ResourceLocation;

/** Private server snapshot, scoped to a single client world and dimension. */
public final class ClairvoyanceView {
    private Set<UUID> targets = Set.of();
    private ResourceLocation dimension;
    private Object world;
    public void clear(){targets=Set.of();dimension=null;world=null;}
    public void receive(ClairvoyancePayload payload, Object currentWorld, ResourceLocation currentDimension) {
        if (currentWorld == null || !currentDimension.equals(payload.dimension())) return;
        world = currentWorld;
        dimension = currentDimension;
        targets = Set.copyOf(payload.targets());
    }
    public boolean highlights(UUID target, Object currentWorld, ResourceLocation currentDimension) {
        return currentWorld != null && currentWorld == world && currentDimension.equals(dimension) && targets.contains(target);
    }
}
