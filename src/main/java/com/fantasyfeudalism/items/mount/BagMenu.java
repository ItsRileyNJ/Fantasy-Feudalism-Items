package com.fantasyfeudalism.items.mount;

import net.minecraft.core.NonNullList;
import net.minecraft.core.component.DataComponents;
import net.minecraft.world.*;
import net.minecraft.world.entity.player.*;
import net.minecraft.world.inventory.*;
import net.minecraft.world.item.*;
import net.minecraft.world.item.component.ItemContainerContents;

public final class BagMenu extends ChestMenu {
    private final ItemStack bag;
    public BagMenu(int id, Inventory inventory, ItemStack bag) { super(MountContent.BAG_MENU.get(), id, inventory, storage(bag), 3); this.bag=bag; }
    public BagMenu(int id, Inventory inventory) { this(id, inventory, ItemStack.EMPTY); }
    private static SimpleContainer storage(ItemStack bag) {
        var items=NonNullList.withSize(27, ItemStack.EMPTY);bag.getOrDefault(DataComponents.CONTAINER,ItemContainerContents.EMPTY).copyInto(items);
        return new SimpleContainer(items.toArray(ItemStack[]::new)) {
            @Override public void setChanged() { super.setChanged(); if (!bag.isEmpty()) bag.set(DataComponents.CONTAINER,ItemContainerContents.fromItems(java.util.stream.IntStream.range(0,27).mapToObj(this::getItem).toList())); }
        };
    }
    @Override protected Slot addSlot(Slot slot) {
        int number=slots.size();
        return super.addSlot(new Slot(slot.container,slot.getSlotIndex(),slot.x,slot.y) {
            @Override public boolean mayPlace(ItemStack item) { return item != bag && (number>=27 || SaddlebagItem.allowed(item)); }
            @Override public boolean mayPickup(Player player) { return getItem()!=bag; }
        });
    }
    @Override public boolean stillValid(Player player) { return player.level().isClientSide || (!bag.isEmpty() && player.getInventory().items.stream().anyMatch(item->item==bag)) || player.getOffhandItem()==bag; }
    @Override public ItemStack quickMoveStack(Player player,int index) {
        if (index<0 || index>=slots.size() || !slots.get(index).mayPickup(player)) return ItemStack.EMPTY;
        if (index>=27 && !SaddlebagItem.allowed(slots.get(index).getItem())) return ItemStack.EMPTY;
        return super.quickMoveStack(player,index);
    }
}
