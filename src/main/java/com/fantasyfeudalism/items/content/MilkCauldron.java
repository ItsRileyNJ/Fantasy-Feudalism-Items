package com.fantasyfeudalism.items.content;

import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPos;
import net.minecraft.world.*;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.*;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.state.*;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.phys.BlockHitResult;
import net.neoforged.neoforge.event.entity.player.PlayerInteractEvent;

public final class MilkCauldron extends Block {
    public static final MapCodec<MilkCauldron> CODEC = simpleCodec(MilkCauldron::new);
    public MilkCauldron(Properties properties) { super(properties); registerDefaultState(stateDefinition.any().setValue(BlockStateProperties.LEVEL_CAULDRON, 3)); }
    @Override protected MapCodec<? extends Block> codec() { return CODEC; }
    @Override protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) { builder.add(BlockStateProperties.LEVEL_CAULDRON); }
    @Override protected net.minecraft.world.phys.shapes.VoxelShape getShape(BlockState state, net.minecraft.world.level.BlockGetter world, BlockPos pos, net.minecraft.world.phys.shapes.CollisionContext context) {
        return Blocks.CAULDRON.defaultBlockState().getShape(world, pos, context);
    }
    public static void interact(PlayerInteractEvent.RightClickBlock event) {
        var level = event.getLevel(); var pos = event.getPos(); var state = level.getBlockState(pos); var player = event.getEntity(); var held = event.getItemStack();
        if (!player.mayUseItemAt(pos, event.getFace(), held)) return;
        boolean fill = state.is(Blocks.CAULDRON) && held.is(Items.MILK_BUCKET);
        boolean extract = state.is(Content.MILK_CAULDRON.get()) && held.is(Items.GLASS_BOTTLE);
        if (!fill && !extract) return;
        if (!level.isClientSide) {
            if (fill) {
                level.setBlockAndUpdate(pos, Content.MILK_CAULDRON.get().defaultBlockState());
                player.setItemInHand(event.getHand(), ItemUtils.createFilledResult(held, player, new ItemStack(Items.BUCKET)));
            } else {
                int remaining = state.getValue(BlockStateProperties.LEVEL_CAULDRON) - 1;
                level.setBlockAndUpdate(pos, remaining == 0 ? Blocks.CAULDRON.defaultBlockState() : state.setValue(BlockStateProperties.LEVEL_CAULDRON, remaining));
                player.setItemInHand(event.getHand(), ItemUtils.createFilledResult(held, player, new ItemStack(Content.MILK.get())));
            }
        }
        event.setCanceled(true); event.setCancellationResult(InteractionResult.sidedSuccess(level.isClientSide));
    }
}
