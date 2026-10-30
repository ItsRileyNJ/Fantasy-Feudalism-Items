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
    private void feudalItems$properties(StateDefinition.Builder<Block, BlockState> builder, CallbackInfo ci) {
        // Subclasses such as Supplementaries Squared's copper lantern define LIT
        // themselves after calling super. Adding it here duplicates that property.
        if (((Object)this).getClass() == LanternBlock.class) builder.add(BlockStateProperties.LIT);
    }
    @Inject(method = "<init>", at = @At("TAIL"))
    private void feudalItems$defaults(BlockBehaviour.Properties properties, CallbackInfo ci) {
        if (((Object)this).getClass() == LanternBlock.class)
            registerDefaultState(defaultBlockState().setValue(BlockStateProperties.LIT, true));
    }
}
