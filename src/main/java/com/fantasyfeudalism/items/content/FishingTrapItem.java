package com.fantasyfeudalism.items.content;
import net.minecraft.world.*;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.*;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.ClipContext;
public final class FishingTrapItem extends BlockItem {
    public FishingTrapItem(Block block,Properties properties){super(block,properties);}
    @Override public InteractionResultHolder<ItemStack> use(Level level,Player player,InteractionHand hand){
        var stack=player.getItemInHand(hand);var hit=getPlayerPOVHitResult(level,player,ClipContext.Fluid.ANY);
        if(hit.getType()!=net.minecraft.world.phys.HitResult.Type.BLOCK)return InteractionResultHolder.pass(stack);
        var result=place(new BlockPlaceContext(player,hand,stack,hit));return new InteractionResultHolder<>(result,stack);
    }
}
