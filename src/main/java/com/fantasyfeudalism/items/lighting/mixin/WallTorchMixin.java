package com.fantasyfeudalism.items.lighting.mixin;

import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.SimpleParticleType;
import net.minecraft.util.RandomSource;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.state.*;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.material.Fluids;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.*;

@Mixin(WallTorchBlock.class)
abstract class WallTorchMixin extends TorchBlock {
    protected WallTorchMixin(SimpleParticleType particle, BlockBehaviour.Properties properties) { super(particle, properties); }
    @Inject(method = "createBlockStateDefinition", at = @At("TAIL"))
    private void feudalItems$properties(StateDefinition.Builder<Block, BlockState> builder, CallbackInfo ci) {
        builder.add(BlockStateProperties.LIT, BlockStateProperties.WATERLOGGED);
    }
    @Inject(method = "<init>", at = @At("TAIL"))
    private void feudalItems$defaults(SimpleParticleType particle, BlockBehaviour.Properties properties, CallbackInfo ci) {
        registerDefaultState(defaultBlockState().setValue(BlockStateProperties.LIT, true).setValue(BlockStateProperties.WATERLOGGED, false));
    }
    @Inject(method = "getStateForPlacement", at = @At("RETURN"), cancellable = true)
    private void feudalItems$waterPlacement(BlockPlaceContext context, CallbackInfoReturnable<BlockState> result) {
        if (result.getReturnValue() != null) result.setReturnValue(result.getReturnValue().setValue(BlockStateProperties.WATERLOGGED,
            context.getLevel().getFluidState(context.getClickedPos()).is(Fluids.WATER)));
    }
    @Inject(method = "animateTick", at = @At("HEAD"), cancellable = true)
    private void feudalItems$noUnlitFlame(BlockState state, Level level, BlockPos pos, RandomSource random, CallbackInfo ci) {
        if (!state.getValue(BlockStateProperties.LIT)) ci.cancel();
    }
}
