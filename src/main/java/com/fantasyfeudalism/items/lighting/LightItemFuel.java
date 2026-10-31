package com.fantasyfeudalism.items.lighting;

import net.minecraft.core.component.DataComponents;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.*;
import net.minecraft.world.item.component.CustomData;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.neoforged.neoforge.event.level.BlockDropsEvent;
import net.neoforged.neoforge.event.tick.EntityTickEvent;

public final class LightItemFuel {
    public static BlockState state(ItemStack stack){
        if(stack.is(Items.TORCH))return Blocks.TORCH.defaultBlockState();if(stack.is(Items.SOUL_TORCH))return Blocks.SOUL_TORCH.defaultBlockState();
        if(stack.is(Items.LANTERN))return Blocks.LANTERN.defaultBlockState();if(stack.is(Items.SOUL_LANTERN))return Blocks.SOUL_LANTERN.defaultBlockState();return null;
    }
    public static long remaining(ItemStack stack){var state=state(stack);if(state==null)return 0;if(WeatherLighting.soul(state))return Long.MAX_VALUE;var data=stack.getOrDefault(DataComponents.CUSTOM_DATA,CustomData.EMPTY).getUnsafe();return data.contains("ff_fuel")?Math.max(0,data.getLong("ff_fuel")):LightFuel.capacity(state);}
    /** Hot rendering path: read only, without copying NBT or accessing server configs. */
    public static boolean lit(ItemStack stack){
        if(state(stack)==null)return false;
        var tag=stack.getOrDefault(DataComponents.CUSTOM_DATA,CustomData.EMPTY).getUnsafe();
        return !tag.getBoolean("ff_off")&&(!tag.contains("ff_fuel")||tag.getLong("ff_fuel")>0||stack.is(Items.SOUL_TORCH)||stack.is(Items.SOUL_LANTERN));
    }
    public static boolean sameHeldLight(ItemStack before,ItemStack after){
        if(state(before)==null||!before.is(after.getItem()))return false;
        var old=before.copy();var next=after.copy();
        for(var stack:new ItemStack[]{old,next})CustomData.update(DataComponents.CUSTOM_DATA,stack,tag->{tag.remove("ff_fuel");tag.remove("ff_last_burn_tick");tag.remove("ff_last_burn_dimension");tag.remove("ff_off");});
        return ItemStack.isSameItemSameComponents(old,next);
    }
    public static void set(ItemStack stack,long remaining,boolean on){CustomData.update(DataComponents.CUSTOM_DATA,stack,data->{data.putLong("ff_fuel",Math.max(0,remaining));data.putBoolean("ff_off",!on);});}
    public static void advance(ItemStack stack,LivingEntity holder){advance(stack,(Entity)holder);}
    public static void advance(ItemStack stack,Entity holder){
        var state=state(stack);if(state==null||holder.level().isClientSide||holder.level().getGameTime()%20!=0||WeatherLighting.soul(state)||!lit(stack))return;
        var previous=stack.getOrDefault(DataComponents.CUSTOM_DATA,CustomData.EMPTY).getUnsafe();
        long now=holder.level().getGameTime();String dimension=holder.level().dimension().location().toString();
        if(previous.contains("ff_last_burn_tick")&&previous.getLong("ff_last_burn_tick")==now&&previous.getString("ff_last_burn_dimension").equals(dimension))return;
        long fuel=Math.max(0,remaining(stack)-20);boolean on=fuel>0;
        if(holder.level().getGameTime()%WeatherLighting.interval()==0){
            state=state.setValue(BlockStateProperties.LIT,true).setValue(BlockStateProperties.WATERLOGGED,holder.isInWater());
            if(holder.getRandom().nextDouble()<WeatherLighting.chance(state,holder.level().isRainingAt(holder.blockPosition()),holder.level().isThundering()))on=false;
        }
        set(stack,fuel,on);
        CustomData.update(DataComponents.CUSTOM_DATA,stack,data->{data.putLong("ff_last_burn_tick",now);data.putString("ff_last_burn_dimension",dimension);});
    }
    public static void itemTick(EntityTickEvent.Post event){if(event.getEntity() instanceof ItemEntity item)advance(item.getItem(),item);}
    public static void drops(BlockDropsEvent event){
        if(!WeatherLighting.supported(event.getState().getBlock()))return;
        long fuel=LightFuel.remaining(event.getLevel(),event.getPos());boolean on=event.getState().getValue(BlockStateProperties.LIT);
        for(var drop:event.getDrops())if(state(drop.getItem())!=null)set(drop.getItem(),fuel,on);
    }
}
