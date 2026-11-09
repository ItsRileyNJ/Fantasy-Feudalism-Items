package com.fantasyfeudalism.items.lighting;

import net.minecraft.core.component.DataComponents;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.*;
import net.minecraft.world.item.component.CustomData;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.neoforged.neoforge.event.level.BlockDropsEvent;
import net.neoforged.neoforge.event.tick.EntityTickEvent;

/** Items carry only a shared fuel stage; partial countdowns live on their holder. */
public final class LightItemFuel {
    public static BlockState state(ItemStack stack){
        if(stack.is(Items.TORCH))return Blocks.TORCH.defaultBlockState();if(stack.is(Items.SOUL_TORCH))return Blocks.SOUL_TORCH.defaultBlockState();
        if(stack.is(Items.LANTERN))return Blocks.LANTERN.defaultBlockState();if(stack.is(Items.SOUL_LANTERN))return Blocks.SOUL_LANTERN.defaultBlockState();return null;
    }
    public static int stageFor(long remaining,long capacity){return remaining<=0?0:(int)Math.min(3,1+(Math.min(remaining,capacity)-1)*3/capacity);}
    public static long boundary(long capacity,int stage){return stage<=0?0:(capacity*Math.min(3,stage)+2)/3;}
    public static int stage(ItemStack stack){
        var state=state(stack);if(state==null)return 0;if(WeatherLighting.soul(state))return 3;
        var tag=stack.getOrDefault(DataComponents.CUSTOM_DATA,CustomData.EMPTY).getUnsafe();
        return tag.contains("ff_fuel_stage")?Math.clamp(tag.getInt("ff_fuel_stage"),0,3):tag.contains("ff_fuel")?stageFor(tag.getLong("ff_fuel"),LightFuel.capacity(state)):3;
    }
    public static long remaining(ItemStack stack){var state=state(stack);return state==null?0:WeatherLighting.soul(state)?Long.MAX_VALUE:boundary(LightFuel.capacity(state),stage(stack));}
    /** Rendering reads do not copy NBT or read server configuration. */
    public static boolean lit(ItemStack stack){
        if(state(stack)==null)return false;
        var tag=stack.getOrDefault(DataComponents.CUSTOM_DATA,CustomData.EMPTY).getUnsafe();
        return !tag.getBoolean("ff_off")&&(WeatherLighting.soul(state(stack))||(!tag.contains("ff_fuel_stage")||tag.getInt("ff_fuel_stage")>0)&&(!tag.contains("ff_fuel")||tag.getLong("ff_fuel")>0));
    }
    public static void setStage(ItemStack stack,int stage,boolean on){
        var state=state(stack);if(state==null)return;
        stage=WeatherLighting.soul(state)?3:Math.clamp(stage,0,3);
        var old=stack.getOrDefault(DataComponents.CUSTOM_DATA,CustomData.EMPTY).getUnsafe();var tag=old.copy();
        tag.remove("ff_fuel");tag.remove("ff_last_burn_tick");tag.remove("ff_last_burn_dimension");
        tag.remove("ff_fuel_stage");tag.remove("ff_off");
        if(stage<3)tag.putInt("ff_fuel_stage",stage);
        if(!on||stage==0)tag.putBoolean("ff_off",true);
        if(!tag.equals(old)){
            if(tag.isEmpty())stack.remove(DataComponents.CUSTOM_DATA);else stack.set(DataComponents.CUSTOM_DATA,CustomData.of(tag));
        }else if(tag.isEmpty()&&stack.has(DataComponents.CUSTOM_DATA))stack.remove(DataComponents.CUSTOM_DATA);
    }
    public static void normalize(ItemStack stack){if(state(stack)!=null&&stack.has(DataComponents.CUSTOM_DATA))setStage(stack,stage(stack),lit(stack));}
    public static void set(ItemStack stack,long remaining,boolean on){var state=state(stack);if(state!=null)setStage(stack,stageFor(remaining,LightFuel.capacity(state)),on);}
    public static boolean sameHeldLight(ItemStack before,ItemStack after){
        if(state(before)==null||!before.is(after.getItem()))return false;
        var old=before.copy();var next=after.copy();
        for(var stack:new ItemStack[]{old,next}){
            if(stack.has(DataComponents.CUSTOM_DATA)){
                var tag=stack.get(DataComponents.CUSTOM_DATA).copyTag();
                for(var key:new String[]{"ff_fuel","ff_fuel_stage","ff_last_burn_tick","ff_last_burn_dimension","ff_off"})tag.remove(key);
                if(tag.isEmpty())stack.remove(DataComponents.CUSTOM_DATA);else stack.set(DataComponents.CUSTOM_DATA,CustomData.of(tag));
            }
        }
        return ItemStack.isSameItemSameComponents(old,next);
    }
    private static String channel(ItemStack stack,Entity holder){
        if(holder instanceof net.minecraft.world.entity.player.Player player){
            for(int i=0;i<player.getInventory().getContainerSize();i++)if(player.getInventory().getItem(i)==stack)return "slot_"+i;
        }
        return holder instanceof ItemEntity?"dropped":"mounted";
    }
    public static void resetProgress(Entity holder){holder.getPersistentData().remove("ff_light_progress");}
    public static void resetProgress(Entity holder,ItemStack stack){
        var root=holder.getPersistentData();var progress=root.getCompound("ff_light_progress");progress.remove(channel(stack,holder));
        if(progress.isEmpty())root.remove("ff_light_progress");else root.put("ff_light_progress",progress);
    }
    public static void resetProgressForItem(Entity holder,Item item){
        var root=holder.getPersistentData();var progress=root.getCompound("ff_light_progress");var id=net.minecraft.core.registries.BuiltInRegistries.ITEM.getKey(item).toString();
        for(var key:java.util.List.copyOf(progress.getAllKeys()))if(progress.getCompound(key).getString("Item").equals(id))progress.remove(key);
        if(progress.isEmpty())root.remove("ff_light_progress");else root.put("ff_light_progress",progress);
    }
    public static void refresh(ItemStack stack,Entity holder){setStage(stack,3,true);resetProgress(holder,stack);}
    public static void playerTick(net.neoforged.neoforge.event.tick.PlayerTickEvent.Post event){
        var player=event.getEntity();if(player.level().isClientSide||player.level().getGameTime()%20!=0)return;
        for(int i=0;i<player.getInventory().getContainerSize();i++)normalize(player.getInventory().getItem(i));
        var root=player.getPersistentData();var progress=root.getCompound("ff_light_progress");
        for(var key:java.util.List.copyOf(progress.getAllKeys()))if(key.startsWith("slot_")){
            int slot=Integer.parseInt(key.substring(5));var stack=player.getInventory().getItem(slot);var entry=progress.getCompound(key);
            if(state(stack)==null||stage(stack)!=entry.getInt("Stage")||!net.minecraft.core.registries.BuiltInRegistries.ITEM.getKey(stack.getItem()).toString().equals(entry.getString("Item")))progress.remove(key);
        }
        if(progress.isEmpty())root.remove("ff_light_progress");else root.put("ff_light_progress",progress);
    }
    public static void advance(ItemStack stack,LivingEntity holder){advance(stack,(Entity)holder);}
    public static void advance(ItemStack stack,Entity holder){
        if(holder.level().isClientSide||holder.level().getGameTime()%20!=0)return;
        normalize(stack);var state=state(stack);
        if(state==null||WeatherLighting.soul(state)||!lit(stack))return;
        var root=holder.getPersistentData();var progress=root.getCompound("ff_light_progress");var key=channel(stack,holder);var entry=progress.getCompound(key);
        int stage=stage(stack);String item=net.minecraft.core.registries.BuiltInRegistries.ITEM.getKey(stack.getItem()).toString();
        if(!item.equals(entry.getString("Item"))||entry.getInt("Stage")!=stage){entry=new CompoundTag();entry.putString("Item",item);entry.putInt("Stage",stage);}
        long now=holder.level().getGameTime();String dimension=holder.level().dimension().location().toString();
        if(entry.contains("Last")&&entry.getLong("Last")==now&&entry.getString("Dimension").equals(dimension))return;
        long capacity=LightFuel.capacity(state);
        long fuel=Math.max(0,boundary(capacity,stage)-entry.getLong("Elapsed")-20);
        int next=stageFor(fuel,capacity);boolean on=fuel>0;
        if(now%WeatherLighting.interval()==0){
            var weatherState=state.setValue(BlockStateProperties.LIT,true).setValue(BlockStateProperties.WATERLOGGED,holder.isInWater());
            if(holder.getRandom().nextDouble()<WeatherLighting.chance(weatherState,holder.level().isRainingAt(holder.blockPosition()),holder.level().isThundering()))on=false;
        }
        if(next!=stage||on!=lit(stack))setStage(stack,next,on);
        entry.putInt("Stage",next);entry.putLong("Elapsed",boundary(capacity,next)-fuel);entry.putLong("Last",now);entry.putString("Dimension",dimension);
        progress.put(key,entry);root.put("ff_light_progress",progress);
    }
    public static void containerOpened(net.neoforged.neoforge.event.entity.player.PlayerContainerEvent.Open event){
        if(event.getEntity().level().isClientSide)return;
        for(var slot:event.getContainer().slots)if(state(slot.getItem())!=null){normalize(slot.getItem());slot.setChanged();}
        event.getContainer().broadcastChanges();
    }
    public static void itemTick(EntityTickEvent.Post event){if(event.getEntity() instanceof ItemEntity item)advance(item.getItem(),item);}
    public static void drops(BlockDropsEvent event){
        if(!WeatherLighting.supported(event.getState().getBlock()))return;
        long fuel=LightFuel.remaining(event.getLevel(),event.getPos());boolean on=event.getState().getValue(BlockStateProperties.LIT);
        for(var drop:event.getDrops())if(state(drop.getItem())!=null){set(drop.getItem(),fuel,on);GlowOil.set(drop.getItem(),GlowOil.fueled(event.getState()));}
    }
}
