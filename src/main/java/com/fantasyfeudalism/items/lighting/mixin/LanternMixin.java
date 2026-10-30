package com.fantasyfeudalism.items.lighting.mixin;

import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.state.*;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(LanternBlock.class)
abstract class LanternMixin extends Block {
    protected LanternMixin(BlockBehaviour.Properties properties) { super(properties); }
    @Inject(method = "createBlockStateDefinition", at = @At("TAIL"))
    private void feudalItems$properties(StateDefinition.Builder<Block, BlockState> builder, CallbackInfo ci) { builder.add(BlockStateProperties.LIT); }
    @Inject(method = "<init>", at = @At("TAIL"))
    private void feudalItems$defaults(BlockBehaviour.Properties properties, CallbackInfo ci) {
        registerDefaultState(defaultBlockState().setValue(BlockStateProperties.LIT, true));
    }
}
