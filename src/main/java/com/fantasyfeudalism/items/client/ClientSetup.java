package com.fantasyfeudalism.items.client;
import com.fantasyfeudalism.items.FeudalItems;
import com.fantasyfeudalism.items.content.*;
import com.fantasyfeudalism.items.mount.*;
import net.minecraft.core.component.DataComponents;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.item.*;
import net.minecraft.world.item.alchemy.PotionContents;
import net.minecraft.world.item.component.DyedItemColor;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.api.distmarker.Dist;
@EventBusSubscriber(modid=FeudalItems.ID,bus=EventBusSubscriber.Bus.MOD,value=Dist.CLIENT)
public final class ClientSetup {
    @SubscribeEvent public static void screens(net.neoforged.neoforge.client.event.RegisterMenuScreensEvent event){
        event.<BagMenu,StorageScreen<BagMenu>>register(MountContent.BAG_MENU.get(),(menu,inventory,title)->new StorageScreen<>(menu,inventory,title));event.<MountMenu,StorageScreen<MountMenu>>register(MountContent.MOUNT_MENU.get(),(menu,inventory,title)->new StorageScreen<>(menu,inventory,title));
    }
    @SubscribeEvent public static void colors(net.neoforged.neoforge.client.event.RegisterColorHandlersEvent.Item event){
        event.register((stack,index)->index==0?DyedItemColor.getOrDefault(stack,0x8B5A2B):0xFFFFFF,MountContent.SADDLEBAG.get());
        event.register((stack,index)->index==0?stack.getOrDefault(DataComponents.POTION_CONTENTS,PotionContents.EMPTY).getColor():0xFFFFFF,AnimalAllegiance.AWKWARD.get(),AnimalAllegiance.INTERMEDIATE.get(),AnimalAllegiance.NORMAL.get(),AnimalAllegiance.EXTENDED.get());
    }
    @SuppressWarnings({"rawtypes","unchecked"})
    @SubscribeEvent public static void layers(net.neoforged.neoforge.client.event.EntityRenderersEvent.AddLayers event){
        for(var type:new EntityType[]{EntityType.HORSE,EntityType.DONKEY,EntityType.MULE}){
            net.minecraft.client.renderer.entity.LivingEntityRenderer renderer=(net.minecraft.client.renderer.entity.LivingEntityRenderer)event.getRenderer(type);
            if(renderer!=null)renderer.addLayer(new MountLayer(renderer));
        }
    }
    @SubscribeEvent public static void setup(net.neoforged.fml.event.lifecycle.FMLClientSetupEvent event){event.enqueueWork(()->{
        for(var item:new Item[]{Items.LANTERN,Items.SOUL_LANTERN,Items.TORCH,Items.SOUL_TORCH})net.minecraft.client.renderer.item.ItemProperties.register(item,net.minecraft.resources.ResourceLocation.fromNamespaceAndPath(FeudalItems.ID,"off"),(stack,level,entity,seed)->com.fantasyfeudalism.items.lighting.LightItemFuel.lit(stack)?0:1);
    });}
}
