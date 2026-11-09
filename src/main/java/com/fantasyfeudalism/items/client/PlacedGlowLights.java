package com.fantasyfeudalism.items.client;

import com.fantasyfeudalism.items.FeudalItems;
import com.fantasyfeudalism.items.lighting.GlowOil;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.level.ChunkEvent;

/** Loaded glow-fueled blocks only; never loads chunks or retains old client worlds. */
@EventBusSubscriber(modid=FeudalItems.ID,value=Dist.CLIENT)
public final class PlacedGlowLights {
    private static final Map<ClientLevel,Set<BlockPos>> WORLDS=Collections.synchronizedMap(new WeakHashMap<>());
    public static void track(ClientLevel level,BlockPos pos,BlockState state){
        if(GlowOil.fueled(state))WORLDS.computeIfAbsent(level,ignored->ConcurrentHashMap.newKeySet()).add(pos.immutable());
        else {var positions=WORLDS.get(level);if(positions!=null)positions.remove(pos);}
    }
    public static List<BlockPos> positions(ClientLevel level){var positions=WORLDS.get(level);return positions==null?List.of():List.copyOf(positions);}
    @SubscribeEvent public static void loaded(ChunkEvent.Load event){
        if(event.getLevel() instanceof ClientLevel level)event.getChunk().findBlockLightSources((pos,state)->track(level,pos,state));
    }
    @SubscribeEvent public static void unloaded(ChunkEvent.Unload event){
        if(event.getLevel() instanceof ClientLevel level){var positions=WORLDS.get(level);var chunk=event.getChunk().getPos();if(positions!=null)positions.removeIf(pos->(pos.getX()>>4)==chunk.x&&(pos.getZ()>>4)==chunk.z);}
    }
    private PlacedGlowLights() {}
}
