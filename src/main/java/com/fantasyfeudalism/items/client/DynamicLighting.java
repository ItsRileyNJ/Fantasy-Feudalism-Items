package com.fantasyfeudalism.items.client;

import com.fantasyfeudalism.items.FeudalItems;
import com.fantasyfeudalism.items.lighting.LightItemFuel;
import com.fantasyfeudalism.items.mount.MountEquipment;
import com.fantasyfeudalism.items.mount.MountVisuals;
import java.util.*;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.ItemStack;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.ClientTickEvent;

/** Client-only light snapshots. Chunk build workers never read entities or mutable stacks. */
@EventBusSubscriber(modid=FeudalItems.ID,value=Dist.CLIENT)
public final class DynamicLighting {
    public record Source(double x,double y,double z,int brightness) {}
    private static volatile Map<Long,List<Source>> sources=Map.of();
    private static Map<Object,Source> previous=Map.of();
    private static ClientLevel world;
    private static final int UPDATE_INTERVAL_TICKS=3;
    private static int ticks;
    private static volatile boolean enabled;

    public static int itemLight(ItemStack stack){
        return com.fantasyfeudalism.items.lighting.GlowOil.potential(stack);
    }
    public static int entityLight(Entity entity){
        int light=0;
        if(entity instanceof ItemEntity item)light=itemLight(item.getItem());
        if(entity instanceof LivingEntity living)light=Math.max(itemLight(living.getMainHandItem()),itemLight(living.getOffhandItem()));
        if(MountEquipment.supported(entity))light=Math.max(light,itemLight(MountVisuals.lantern(entity)));
        return light;
    }
    public static int lightAt(List<Source> lights,double x,double y,double z,int packed){
        if((packed&0xFFFF)>=240||lights.isEmpty())return packed;
        double light=0;
        for(var source:lights){
            double dx=x-source.x(),dy=y-source.y(),dz=z-source.z();
            double distanceSquared=dx*dx+dy*dy+dz*dz;
            if(distanceSquared<source.brightness()*source.brightness())light=Math.max(light,source.brightness()-Math.sqrt(distanceSquared));
        }
        // Packed block light occupies bits 4..7, with fractional precision in the low bits.
        return com.fantasyfeudalism.items.lighting.GlowOil.mergeLight(packed,light);
    }
    public static int lightAt(BlockPos pos,int packed){if(!enabled)return packed;return lightAt(sources.getOrDefault(net.minecraft.core.SectionPos.asLong(pos.getX()>>4,pos.getY()>>4,pos.getZ()>>4),List.of()),pos.getX(),pos.getY(),pos.getZ(),packed);}

    @SubscribeEvent public static void frame(net.neoforged.neoforge.client.event.RenderFrameEvent.Pre event){
        updateShaderState(Minecraft.getInstance());
    }
    private static void updateShaderState(Minecraft client){
        if(client.level!=world){enabled=false;world=client.level;previous=Map.of();sources=Map.of();ticks=0;}
        boolean active=!ShaderCompatibility.shadersEnabled();
        if(active==enabled)return;
        enabled=active;
        if(!active){
            // Publish the disabled gate before clearing snapshots or scheduling rebuilds.
            // Rebuild old terrain too, so baked illumination cannot survive a shader toggle.
            var affected=sources.keySet();
            sources=Map.of();previous=Map.of();
            if(world!=null)for(long section:affected)client.levelRenderer.setSectionDirty(net.minecraft.core.SectionPos.x(section),net.minecraft.core.SectionPos.y(section),net.minecraft.core.SectionPos.z(section));
        }
        ticks=UPDATE_INTERVAL_TICKS-1;
    }
    @SubscribeEvent public static void tick(ClientTickEvent.Post event){
        var client=Minecraft.getInstance();
        updateShaderState(client);
        if(!enabled||world==null||client.isPaused()||++ticks%UPDATE_INTERVAL_TICKS!=0)return;
        Map<Object,Source> next=new HashMap<>();
        for(var entity:world.entitiesForRendering()){
            int light=entityLight(entity);
            if(light>0&&!entity.isRemoved())next.put(entity.getId(),new Source(entity.getX(),entity.getY()+entity.getBbHeight()*0.5,entity.getZ(),light));
        }
        for(var pos:PlacedGlowLights.positions(world)){
            if(!world.hasChunkAt(pos))continue;
            var state=world.getBlockState(pos);
            if(!com.fantasyfeudalism.items.lighting.GlowOil.fueled(state)){PlacedGlowLights.track(world,pos,state);continue;}
            int light=com.fantasyfeudalism.items.lighting.GlowOil.potential(state);
            if(light>0)next.put(pos,new Source(pos.getX()+.5,pos.getY()+.5,pos.getZ()+.5,light));
        }
        Map<Long,List<Source>> buckets=new HashMap<>();
        for(var source:next.values()){
            Set<Long> sections=new HashSet<>();dirtySections(source,sections);
            for(long section:sections)buckets.computeIfAbsent(section,key->new ArrayList<>()).add(source);
        }
        buckets.replaceAll((section,lights)->List.copyOf(lights));
        sources=Map.copyOf(buckets);
        Set<Long> dirty=new HashSet<>();
        previous.forEach((id,source)->{if(!source.equals(next.get(id)))dirtySections(source,dirty);});
        next.forEach((id,source)->{if(!source.equals(previous.get(id)))dirtySections(source,dirty);});
        for(long section:dirty)client.levelRenderer.setSectionDirty(net.minecraft.core.SectionPos.x(section),net.minecraft.core.SectionPos.y(section),net.minecraft.core.SectionPos.z(section));
        previous=next;
    }
    private static void dirtySections(Source source,Set<Long> dirty){
        int radius=source.brightness();
        for(int x=(int)Math.floor(source.x()-radius)>>4;x<=((int)Math.floor(source.x()+radius)>>4);x++)
            for(int y=(int)Math.floor(source.y()-radius)>>4;y<=((int)Math.floor(source.y()+radius)>>4);y++)
                for(int z=(int)Math.floor(source.z()-radius)>>4;z<=((int)Math.floor(source.z()+radius)>>4);z++)
                    dirty.add(net.minecraft.core.SectionPos.asLong(x,y,z));
    }
}
