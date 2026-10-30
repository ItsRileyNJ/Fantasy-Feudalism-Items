package com.fantasyfeudalism.items.lighting.mixin;

import com.fantasyfeudalism.items.lighting.WeatherLighting;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.*;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.material.*;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.*;

@Mixin(BlockBehaviour.BlockStateBase.class)
abstract class LightStateMixin {
    @Inject(method = "getLightEmission()I", at = @At("HEAD"), cancellable = true)
    private void feudalItems$unlit(CallbackInfoReturnable<Integer> result) {
        BlockState state = (BlockState) (Object) this;
        if (WeatherLighting.supported(state.getBlock()) && !state.getValue(BlockStateProperties.LIT)) result.setReturnValue(0);
    }
    @Inject(method = "onPlace", at = @At("TAIL"))
    private void feudalItems$schedule(Level level, BlockPos pos, BlockState oldState, boolean moving, CallbackInfo ci) {
        if (level instanceof ServerLevel server) {
            com.fantasyfeudalism.items.lighting.LightFuel.placed(server, pos, (BlockState)(Object)this, oldState);
            WeatherLighting.schedule(server, pos, (BlockState)(Object)this);
        }
    }
    @Inject(method = "tick", at = @At("TAIL"))
    private void feudalItems$weather(ServerLevel level, BlockPos pos, net.minecraft.util.RandomSource random, CallbackInfo ci) {
        BlockState state = (BlockState) (Object) this;
        if (WeatherLighting.supported(state.getBlock())) {
            WeatherLighting.check(level, pos, random.nextDouble());
            WeatherLighting.schedule(level, pos, level.getBlockState(pos));
        }
    }
}
