package com.fantasyfeudalism.items.lighting.mixin;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.SimpleParticleType;
import net.minecraft.util.RandomSource;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.*;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.state.*;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.material.*;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(TorchBlock.class)
abstract class TorchMixin extends BaseTorchBlock implements SimpleWaterloggedBlock {
    protected TorchMixin(BlockBehaviour.Properties properties) { super(properties); }
    @Override protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(BlockStateProperties.LIT, BlockStateProperties.WATERLOGGED);
    }
    @Inject(method = "<init>", at = @At("TAIL"))
    private void feudalItems$defaults(SimpleParticleType particle, BlockBehaviour.Properties properties, CallbackInfo ci) {
        registerDefaultState(defaultBlockState().setValue(BlockStateProperties.LIT, true).setValue(BlockStateProperties.WATERLOGGED, false));
    }
    @Override public BlockState getStateForPlacement(BlockPlaceContext context) {
        return defaultBlockState().setValue(BlockStateProperties.WATERLOGGED, context.getLevel().getFluidState(context.getClickedPos()).is(Fluids.WATER));
    }
    @Override protected FluidState getFluidState(BlockState state) {
        return state.getValue(BlockStateProperties.WATERLOGGED) ? Fluids.WATER.getSource(false) : super.getFluidState(state);
    }
    @Inject(method = "animateTick", at = @At("HEAD"), cancellable = true)
    private void feudalItems$noUnlitFlame(BlockState state, Level level, BlockPos pos, RandomSource random, CallbackInfo ci) {
        if (!state.getValue(BlockStateProperties.LIT)) ci.cancel();
    }
}
