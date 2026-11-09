package com.fantasyfeudalism.items.lighting;

import com.fantasyfeudalism.items.content.Content;
import net.minecraft.core.component.DataComponents;
import net.minecraft.world.item.*;
import net.minecraft.world.item.component.CustomData;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BooleanProperty;

/** Persistent fuel identity; visual light strength may exceed vanilla's level-15 cap. */
public final class GlowOil {
    public static final BooleanProperty FUEL=BooleanProperty.create("glow_oil");
    public static boolean isOil(ItemStack stack){return stack.is(Content.KELP_OIL.get())||stack.is(Content.GLOW_OIL.get());}
    public static boolean fueled(ItemStack stack){return stack.is(Items.LANTERN)&&stack.getOrDefault(DataComponents.CUSTOM_DATA,CustomData.EMPTY).getUnsafe().getBoolean("ff_glow_oil");}
    public static boolean fueled(BlockState state){return state.is(Blocks.LANTERN)&&state.hasProperty(FUEL)&&state.getValue(FUEL);}
    public static void set(ItemStack stack,boolean glow){
        if(!stack.is(Items.LANTERN))return;
        var tag=stack.getOrDefault(DataComponents.CUSTOM_DATA,CustomData.EMPTY).copyTag();
        if(glow)tag.putBoolean("ff_glow_oil",true);else tag.remove("ff_glow_oil");
        if(tag.isEmpty())stack.remove(DataComponents.CUSTOM_DATA);else stack.set(DataComponents.CUSTOM_DATA,CustomData.of(tag));
    }
    public static int potential(ItemStack stack){var state=LightItemFuel.state(stack);return state!=null&&LightItemFuel.lit(stack)?state.getLightEmission()*(fueled(stack)?2:1):0;}
    public static int potential(BlockState state){return state.getLightEmission()*(fueled(state)?2:1);}
    public static int mergeLight(int packed,double light){return (packed&~0xFFFF)|Math.max(packed&0xFFFF,(int)(Math.clamp(light,0,15)*16));}
    private GlowOil() {}
}
