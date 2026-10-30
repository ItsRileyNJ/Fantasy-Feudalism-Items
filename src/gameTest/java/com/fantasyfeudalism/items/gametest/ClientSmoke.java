package com.fantasyfeudalism.items.gametest;

import com.fantasyfeudalism.items.FeudalItems;
import com.fantasyfeudalism.items.content.ModAttachments;
import com.fantasyfeudalism.items.lighting.LightItemFuel;
import com.fantasyfeudalism.items.mount.*;
import java.util.*;
import java.nio.file.*;
import net.minecraft.client.Minecraft;
import net.minecraft.core.component.DataComponents;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.animal.horse.*;
import net.minecraft.world.item.*;
import net.minecraft.world.item.component.*;
import net.minecraft.world.*;
import net.minecraft.network.chat.Component;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.client.event.ClientTickEvent;

/** Development-only checks against the supplied binary mods, never packaged in the release. */
@EventBusSubscriber(modid=FeudalItems.ID,value=Dist.CLIENT)
public final class ClientSmoke {
    private static int phase,ticks;
    private static volatile UUID horseId,donkeyId;
    private static final List<String> PASSED=new ArrayList<>();
    private static Object itemManager,entityManager;
    private static void check(boolean condition,String message){if(!condition)throw new AssertionError(message);PASSED.add(message);}
    private static Entity entity(Minecraft client,UUID id){for(var entity:client.level.entitiesForRendering())if(entity.getUUID().equals(id))return entity;return null;}
    private static int itemLight(ItemStack stack)throws Exception{return (int)Class.forName("dev.lambdaurora.lambdynlights.api.item.ItemLightSourceManager").getMethod("getLuminance",ItemStack.class,boolean.class).invoke(itemManager,stack,false);}
    private static int entityLight(Entity entity)throws Exception{return (int)Class.forName("dev.lambdaurora.lambdynlights.api.entity.EntityLightSourceManager").getMethod("getLuminance",Entity.class).invoke(entityManager,entity);}
    private static void report(boolean complete,Throwable error){
        try{var json=new com.google.gson.JsonObject();json.addProperty("complete",complete);json.add("passed",new com.google.gson.Gson().toJsonTree(PASSED));if(error!=null)json.addProperty("error",error.toString());var path=Path.of(System.getProperty("fantasyfeudalism.smokeReport"));Files.createDirectories(path.getParent());Files.writeString(path,new com.google.gson.GsonBuilder().setPrettyPrinting().create().toJson(json));}catch(Exception exception){exception.printStackTrace();}
    }
    @SubscribeEvent public static void tick(ClientTickEvent.Post event){
        if(!Boolean.getBoolean("fantasyfeudalism.clientSmoke")||phase<0)return;
        var client=Minecraft.getInstance();if(client.level==null||client.player==null||client.getSingleplayerServer()==null)return;
        try{
            if(++ticks>1200)throw new AssertionError("Client smoke phase timed out: "+phase);
            if(phase==0){
                var type=Class.forName("dev.lambdaurora.lambdynlights.LambDynLights");var lights=type.getMethod("get").invoke(null);
                itemManager=type.getMethod("itemLightSourceManager").invoke(lights);entityManager=type.getMethod("entityLightSourceManager").invoke(lights);
                check(itemLight(new ItemStack(Items.TORCH))==14,"LambDynamicLights uses vanilla torch brightness");
                check(itemLight(new ItemStack(Items.LANTERN))==15,"LambDynamicLights uses vanilla lantern brightness");
                var off=new ItemStack(Items.LANTERN);LightItemFuel.set(off,100,false);check(itemLight(off)==0,"Switched-off lantern produces no dynamic light");
                var empty=new ItemStack(Items.TORCH);LightItemFuel.set(empty,0,false);check(itemLight(empty)==0,"Burned-out torch produces no dynamic light");
                check(itemLight(new ItemStack(Items.SOUL_LANTERN))==10,"Soul lantern retains vanilla brightness");
                client.getSingleplayerServer().execute(()->{
                    var server=client.getSingleplayerServer();var player=server.getPlayerList().getPlayer(client.player.getUUID());var level=player.serverLevel();
                    player.teleportTo(0.5,150,0.5);level.setBlockAndUpdate(new net.minecraft.core.BlockPos(2,149,2),net.minecraft.world.level.block.Blocks.STONE.defaultBlockState());level.setBlockAndUpdate(new net.minecraft.core.BlockPos(-2,149,2),net.minecraft.world.level.block.Blocks.STONE.defaultBlockState());
                    var horse=EntityType.HORSE.create(level);horse.setPos(2.5,150,2.5);horse.setTamed(true);horse.setOwnerUUID(player.getUUID());horse.setNoAi(true);MountEquipment.set(horse,3,new ItemStack(Items.LANTERN));level.addFreshEntity(horse);horseId=horse.getUUID();
                    var donkey=EntityType.DONKEY.create(level);donkey.setPos(-1.5,150,2.5);donkey.setTamed(true);donkey.setNoAi(true);var bag=DyedItemColor.applyDyes(new ItemStack(MountContent.SADDLEBAG.get()),List.of((DyeItem)Items.RED_DYE));MountEquipment.set(donkey,4,bag);level.addFreshEntity(donkey);donkeyId=donkey.getUUID();
                });phase=1;ticks=0;return;
            }
            var horse=horseId==null?null:entity(client,horseId);
            if(horse==null)return;
            if(phase==1){
                var data=horse.getData(ModAttachments.MOUNT);if(!data.contains("Slot3"))return;
                check(entityLight(horse)==15,"Mounted lantern contributes light through LambDynamicLights");
                var donkey=donkeyId==null?null:entity(client,donkeyId);if(donkey==null||!donkey.getData(ModAttachments.MOUNT).contains("Slot4"))return;
                var bag=ItemStack.parse(client.level.registryAccess(),donkey.getData(ModAttachments.MOUNT).getCompound("Slot4")).orElseThrow();check(bag.has(DataComponents.DYED_COLOR),"Equipped saddlebag dye is synchronized to rendering client");
                client.getSingleplayerServer().execute(()->{var player=client.getSingleplayerServer().getPlayerList().getPlayer(client.player.getUUID());var mount=(AbstractHorse)player.serverLevel().getEntity(horseId);var lantern=MountEquipment.item(mount,3);LightItemFuel.set(lantern,100,false);MountEquipment.set(mount,3,lantern);});phase=2;ticks=0;return;
            }
            if(phase==2){
                var lamp=ItemStack.parse(client.level.registryAccess(),horse.getData(ModAttachments.MOUNT).getCompound("Slot3")).orElse(ItemStack.EMPTY);if(LightItemFuel.lit(lamp))return;
                check(entityLight(horse)==0,"Switching mounted lantern off removes dynamic light");
                client.getSingleplayerServer().execute(()->{var player=client.getSingleplayerServer().getPlayerList().getPlayer(client.player.getUUID());var bag=new ItemStack(MountContent.SADDLEBAG.get());bag.set(DataComponents.CONTAINER,ItemContainerContents.fromItems(List.of(new ItemStack(Items.EMERALD,3))));player.setItemSlot(EquipmentSlot.MAINHAND,bag);player.openMenu(new SimpleMenuProvider((id,inventory,p)->new BagMenu(id,inventory,bag),Component.literal("Saddlebag smoke test")));});phase=3;ticks=0;return;
            }
            if(phase==3){
                if(!(client.player.containerMenu instanceof BagMenu bag)||client.screen==null)return;
                if(bag.getContainer().getItem(0).isEmpty())return;
                check(bag.getContainer().getContainerSize()==27&&bag.getContainer().getItem(0).getCount()==3,"Saddlebag screen opens and synchronizes twenty-seven-slot cargo");
                client.getSingleplayerServer().execute(()->{var player=client.getSingleplayerServer().getPlayerList().getPlayer(client.player.getUUID());var mount=(AbstractHorse)player.serverLevel().getEntity(horseId);player.openMenu(new SimpleMenuProvider((id,inventory,p)->new MountMenu(id,inventory,MountEquipment.inventory(mount)),Component.literal("Mount equipment")),buffer->buffer.writeVarInt(mount.getId()));});phase=4;ticks=0;return;
            }
            if(phase==4){
                if(!(client.player.containerMenu instanceof MountMenu menu)||client.screen==null||menu.getContainer().getItem(3).isEmpty())return;
                check(menu.getContainer().getContainerSize()==36&&menu.getContainer().getItem(3).is(Items.LANTERN),"Mount equipment screen opens and synchronizes dedicated lantern slot");
                report(true,null);phase=-1;
            }
        }catch(Throwable error){error.printStackTrace();report(false,error);phase=-1;}
    }
}
