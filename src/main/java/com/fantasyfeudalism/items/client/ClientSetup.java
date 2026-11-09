package com.fantasyfeudalism.items.client;
import com.fantasyfeudalism.items.FeudalItems;
import com.fantasyfeudalism.items.content.*;
import com.fantasyfeudalism.items.mount.*;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.item.*;
import net.minecraft.world.item.component.DyedItemColor;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.api.distmarker.Dist;
@EventBusSubscriber(modid=FeudalItems.ID,bus=EventBusSubscriber.Bus.MOD,value=Dist.CLIENT)
public final class ClientSetup {
    @SubscribeEvent public static void particles(net.neoforged.neoforge.client.event.RegisterParticleProvidersEvent event){
        event.registerSpriteSet(com.fantasyfeudalism.items.trophy.TrophyParticles.IDLE.get(),sprites->TrophyAuraParticle.provider(sprites,false));
        event.registerSpriteSet(com.fantasyfeudalism.items.trophy.TrophyParticles.ACTIVE.get(),sprites->TrophyAuraParticle.provider(sprites,true));
    }
    @SubscribeEvent public static void effectIcons(net.neoforged.neoforge.client.extensions.common.RegisterClientExtensionsEvent event){
        for(var kind:com.fantasyfeudalism.items.trophy.TrophyKind.values()){
            var item=new ItemStack(com.fantasyfeudalism.items.trophy.Trophies.BLOCKS.get(kind).get());
            event.registerMobEffect(new TrophyEffectIcons(item),com.fantasyfeudalism.items.trophy.TrophyAttunement.EFFECTS.get(kind));
        }
    }
    @SubscribeEvent public static void screens(net.neoforged.neoforge.client.event.RegisterMenuScreensEvent event){
        event.<BagMenu,StorageScreen<BagMenu>>register(MountContent.BAG_MENU.get(),(menu,inventory,title)->new StorageScreen<>(menu,inventory,title));event.<MountMenu,StorageScreen<MountMenu>>register(MountContent.MOUNT_MENU.get(),(menu,inventory,title)->new StorageScreen<>(menu,inventory,title));
    }
    @SubscribeEvent public static void colors(net.neoforged.neoforge.client.event.RegisterColorHandlersEvent.Item event){
        event.register((stack,index)->index==0?DyedItemColor.getOrDefault(stack,0x8B5A2B):0xFFFFFF,MountContent.SADDLEBAG.get());
        event.register((stack,index)->index==0?0xFFDCEFAD:-1,Content.GLOW_OIL.get());
        event.register(Horseshoe::itemColor,MountContent.COPPER.get(),MountContent.IRON.get(),MountContent.GOLD.get(),MountContent.DIAMOND.get(),MountContent.NETHERITE.get());
        event.register(AnimalAllegiance::itemColor,AnimalAllegiance.AWKWARD.get(),AnimalAllegiance.INTERMEDIATE.get(),AnimalAllegiance.NORMAL.get(),AnimalAllegiance.EXTENDED.get());
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
