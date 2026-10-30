package com.fantasyfeudalism.items.trophy;

import com.mojang.serialization.MapCodec;
import net.minecraft.core.*;
import net.minecraft.world.*;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.*;
import net.minecraft.world.level.*;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.entity.*;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;

public final class TrophyBlock extends BaseEntityBlock {
    public static final MapCodec<TrophyBlock> CODEC = simpleCodec(properties -> new TrophyBlock(TrophyKind.ZOMBIE, properties));
    public final TrophyKind kind;
    public TrophyBlock(TrophyKind kind, Properties properties) { super(properties); this.kind = kind; }
    @Override protected MapCodec<? extends BaseEntityBlock> codec() { return CODEC; }
    @Override protected RenderShape getRenderShape(BlockState state) { return RenderShape.MODEL; }
    @Override protected net.minecraft.world.phys.shapes.VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, net.minecraft.world.phys.shapes.CollisionContext context) {
        return net.minecraft.world.phys.shapes.Shapes.or(Block.box(2, 0, 2, 14, 4, 14), Block.box(4, 4, 4, 12, 12, 12));
    }
    @Override public BlockEntity newBlockEntity(BlockPos pos, BlockState state) { return new TrophyEntity(pos, state); }
    @Override public void setPlacedBy(Level level, BlockPos pos, BlockState state, LivingEntity placer, ItemStack stack) {
        if (!level.isClientSide && placer instanceof Player player && level.getBlockEntity(pos) instanceof TrophyEntity trophy) {
            trophy.owner = player.getUUID(); trophy.setChanged();
            TrophyLedger.get((net.minecraft.server.level.ServerLevel) level).recordPlacement(player.getUUID(), kind);
        }
    }
    @Override protected InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos, Player player, BlockHitResult hit) {
        if (level.getBlockEntity(pos) instanceof TrophyEntity trophy && player.getUUID().equals(trophy.owner)) {
            if (!level.isClientSide) {
                TrophyLedger.get((net.minecraft.server.level.ServerLevel) level).attune(player.getUUID(), kind);
                TrophyEffects.changed(player);
                player.setData(com.fantasyfeudalism.items.content.ModAttachments.TROPHY,kind.ordinal());
                player.getPersistentData().putString("fantasy_feudalism_trophy", kind.name());
                player.displayClientMessage(net.minecraft.network.chat.Component.translatable("message.fantasy_feudalism_items.attuned", net.minecraft.network.chat.Component.translatable("block.fantasy_feudalism_items." + kind.id() + "_trophy")), true);
            }
            return InteractionResult.sidedSuccess(level.isClientSide);
        }
        return InteractionResult.PASS;
    }
    @Override protected void onRemove(BlockState state,Level level,BlockPos pos,BlockState next,boolean moving){
        if(!state.is(next.getBlock())&&level instanceof net.minecraft.server.level.ServerLevel server&&level.getBlockEntity(pos) instanceof TrophyEntity trophy&&trophy.owner!=null){
            var ledger=TrophyLedger.get(server);
            if(ledger.active(trophy.owner)==kind){ledger.clear(trophy.owner);var owner=server.getServer().getPlayerList().getPlayer(trophy.owner);if(owner!=null){TrophyEffects.changed(owner);owner.setData(com.fantasyfeudalism.items.content.ModAttachments.TROPHY,-1);owner.getPersistentData().remove("fantasy_feudalism_trophy");}}
        }
        super.onRemove(state,level,pos,next,moving);
    }
    @Override public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state, BlockEntityType<T> type) {
        return level.isClientSide ? null : createTickerHelper(type, Trophies.BLOCK_ENTITY.get(), TrophyEntity::tick);
    }
    public static final class TrophyItem extends BlockItem {
        final TrophyKind kind;
        public TrophyItem(TrophyBlock block, Properties properties) { super(block, properties); kind = block.kind; }
        @Override public InteractionResult place(net.minecraft.world.item.context.BlockPlaceContext context) {
            if (!context.getLevel().isClientSide && context.getPlayer() != null
                && TrophyLedger.get((net.minecraft.server.level.ServerLevel) context.getLevel()).hasPlaced(context.getPlayer().getUUID(), kind)) {
                context.getPlayer().displayClientMessage(net.minecraft.network.chat.Component.literal("You have already permanently placed this trophy type."), true);
                return InteractionResult.FAIL;
            }
            return super.place(context);
        }
        @Override public void appendHoverText(ItemStack stack, TooltipContext context, java.util.List<net.minecraft.network.chat.Component> tooltip, net.minecraft.world.item.TooltipFlag flag) {
            tooltip.add(net.minecraft.network.chat.Component.literal(kind.tooltip));
        }
    }
}
