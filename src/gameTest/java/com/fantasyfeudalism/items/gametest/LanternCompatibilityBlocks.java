package com.fantasyfeudalism.items.gametest;

import com.fantasyfeudalism.items.FeudalItems;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.state.*;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.registries.RegisterEvent;

/** Test-only fixtures matching the supplied mod's LightableLanternBlock/CopperLanternBlock hierarchy. */
@EventBusSubscriber(modid=FeudalItems.ID,bus=EventBusSubscriber.Bus.MOD)
public final class LanternCompatibilityBlocks {
    public static LanternBlock plain,lightable,copper;
    @SubscribeEvent public static void register(RegisterEvent event){
        event.register(Registries.BLOCK,helper->{
            plain=new PlainLantern();lightable=new LightableLantern();copper=new CopperLantern();
            helper.register(ResourceLocation.fromNamespaceAndPath(FeudalItems.ID,"test_plain_lantern"),plain);
            helper.register(ResourceLocation.fromNamespaceAndPath(FeudalItems.ID,"test_lightable_lantern"),lightable);
            helper.register(ResourceLocation.fromNamespaceAndPath(FeudalItems.ID,"test_copper_lantern"),copper);
        });
    }
    private static class PlainLantern extends LanternBlock {
        PlainLantern(){super(BlockBehaviour.Properties.of());}
    }
    private static class LightableLantern extends LanternBlock {
        LightableLantern(){super(BlockBehaviour.Properties.of());registerDefaultState(defaultBlockState().setValue(BlockStateProperties.LIT,false));}
        @Override protected void createBlockStateDefinition(StateDefinition.Builder<Block,BlockState> builder){super.createBlockStateDefinition(builder);builder.add(BlockStateProperties.LIT);}
    }
    private static final class CopperLantern extends LightableLantern {
        CopperLantern(){registerDefaultState(defaultBlockState().setValue(BlockStateProperties.HORIZONTAL_AXIS,net.minecraft.core.Direction.Axis.Z));}
        @Override protected void createBlockStateDefinition(StateDefinition.Builder<Block,BlockState> builder){super.createBlockStateDefinition(builder);builder.add(BlockStateProperties.HORIZONTAL_AXIS);}
    }
}
