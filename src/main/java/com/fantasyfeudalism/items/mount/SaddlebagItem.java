package com.fantasyfeudalism.items.mount;

import net.minecraft.core.component.DataComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.world.*;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.*;
import net.minecraft.world.item.component.ItemContainerContents;
import net.minecraft.world.level.Level;

public final class SaddlebagItem extends Item {
    public SaddlebagItem(Properties properties) { super(properties.stacksTo(1).component(DataComponents.CONTAINER, ItemContainerContents.EMPTY)); }
    @Override public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        var stack = player.getItemInHand(hand);
        if (player instanceof net.minecraft.server.level.ServerPlayer server)
            server.openMenu(new SimpleMenuProvider((id, inventory, p) -> new BagMenu(id, inventory, stack), Component.translatable("item.fantasy_feudalism_items.saddlebag")));
        return InteractionResultHolder.sidedSuccess(stack, level.isClientSide);
    }
    public static boolean allowed(ItemStack stack) {
        var container = stack.get(DataComponents.CONTAINER);
        if (container != null && container.nonEmptyStream().findAny().isPresent()) return false;
        var bundle = stack.get(DataComponents.BUNDLE_CONTENTS);
        if (bundle != null && !bundle.isEmpty()) return false;
        // Vanilla block-entity containers and other mods' item-handler containers must also be empty.
        var block = stack.get(DataComponents.BLOCK_ENTITY_DATA);
        if (block != null && !block.copyTag().getList("Items", net.minecraft.nbt.Tag.TAG_COMPOUND).isEmpty()) return false;
        var handler = stack.getCapability(net.neoforged.neoforge.capabilities.Capabilities.ItemHandler.ITEM);
        if (handler != null) for (int i=0;i<handler.getSlots();i++) if (!handler.getStackInSlot(i).isEmpty()) return false;
        return true;
    }
}
