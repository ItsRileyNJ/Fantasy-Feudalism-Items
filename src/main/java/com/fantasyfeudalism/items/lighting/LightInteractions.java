package com.fantasyfeudalism.items.lighting;

import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.neoforged.neoforge.event.entity.player.PlayerInteractEvent;

public final class LightInteractions {
    public static void interact(PlayerInteractEvent.RightClickBlock event) {
        var level = event.getLevel();
        var pos = event.getPos();
        var state = level.getBlockState(pos);
        var player = event.getEntity();
        if (!WeatherLighting.supported(state.getBlock()) || !player.mayUseItemAt(pos, event.getFace(), event.getItemStack())) return;
        boolean lantern = state.is(Blocks.LANTERN) || state.is(Blocks.SOUL_LANTERN);
        boolean oil = !player.isShiftKeyDown() && state.is(Blocks.LANTERN) && event.getItemStack().is(com.fantasyfeudalism.items.content.Content.KELP_OIL.get());
        boolean relight = WeatherLighting.torch(state) && !state.getValue(BlockStateProperties.LIT) && event.getItemStack().is(Items.FLINT_AND_STEEL);
        if (oil || (lantern && player.isShiftKeyDown()) || relight) {
            if (!level.isClientSide) {
                var server = (net.minecraft.server.level.ServerLevel) level;
                if (oil) {
                    boolean empty = LightFuel.remaining(server, pos) == 0;
                    LightFuel.refill(server, pos);
                    if (empty) level.setBlockAndUpdate(pos, state.setValue(BlockStateProperties.LIT, true));
                    player.setItemInHand(event.getHand(), net.minecraft.world.item.ItemUtils.createFilledResult(event.getItemStack(), player, new net.minecraft.world.item.ItemStack(Items.GLASS_BOTTLE)));
                } else if (relight) {
                    LightFuel.refill(server, pos);
                    level.setBlockAndUpdate(pos, state.setValue(BlockStateProperties.LIT, true));
                } else if (WeatherLighting.soul(state) || LightFuel.remaining(server, pos) > 0) level.setBlockAndUpdate(pos, state.cycle(BlockStateProperties.LIT));
                if (relight && !player.getAbilities().instabuild)
                    event.getItemStack().hurtAndBreak(1, player, LivingEntity.getSlotForHand(event.getHand()));
            }
            event.setCanceled(true);
            event.setCancellationResult(InteractionResult.sidedSuccess(level.isClientSide));
        }
    }
    private LightInteractions() {}
}
