package com.fantasyfeudalism.items.mount;

import net.minecraft.core.NonNullList;
import net.minecraft.core.component.DataComponents;
import net.minecraft.world.*;
import net.minecraft.world.entity.animal.horse.*;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.*;
import net.minecraft.world.item.component.ItemContainerContents;

/** Top row: saddle, armor, horseshoe, lantern, saddlebag. Remaining rows are the bag's cargo. */
public final class MountInventory extends SimpleContainer {
    private final AbstractHorse horse;
    private boolean ready, writing;
    private ItemStack loadedBag=ItemStack.EMPTY;
    public MountInventory(AbstractHorse horse) {
        super(36);this.horse=horse;writing=true;
        super.setItem(0,horse.getSlot(400).get());super.setItem(1,horse.getBodyArmorItem());
        for(int i=2;i<=4;i++)super.setItem(i,MountEquipment.item(horse,i));
        loadBag();writing=false;ready=true;
    }
    private void loadBag() {
        var bag=getItem(4);var items=NonNullList.withSize(27,ItemStack.EMPTY);
        if(!bag.isEmpty())bag.getOrDefault(DataComponents.CONTAINER,ItemContainerContents.EMPTY).copyInto(items);
        for(int i=0;i<27;i++)super.setItem(9+i,items.get(i));loadedBag=bag;
    }
    public AbstractHorse horse(){return horse;}
    @Override public boolean canPlaceItem(int slot,ItemStack item) {return allowed(horse,slot,item,!getItem(4).isEmpty());}
    public static boolean allowed(AbstractHorse horse,int slot,ItemStack item,boolean bagPresent){
        return switch(slot) {
            case 0->item.is(Items.SADDLE);
            case 1->horse instanceof Horse && item.getItem() instanceof AnimalArmorItem armor && armor.getBodyType()==AnimalArmorItem.BodyType.EQUESTRIAN;
            case 2->horse instanceof Horse && item.getItem() instanceof Horseshoe;
            case 3->item.is(Items.LANTERN)||item.is(Items.SOUL_LANTERN);
            case 4->(horse instanceof Donkey||horse instanceof Mule)&&item.is(MountContent.SADDLEBAG.get());
            default->slot>=9&&bagPresent&&SaddlebagItem.allowed(item);
        };
    }
    @Override public void setChanged() {
        super.setChanged();if(!ready||writing)return;writing=true;
        if(getItem(4)!=loadedBag||getItem(4).isEmpty())loadBag();
        if(!getItem(4).isEmpty())getItem(4).set(DataComponents.CONTAINER,ItemContainerContents.fromItems(java.util.stream.IntStream.range(9,36).mapToObj(this::getItem).toList()));
        horse.getSlot(400).set(getItem(0));horse.setBodyArmorItem(getItem(1));
        for(int i=2;i<=4;i++)MountEquipment.set(horse,i,getItem(i));
        MountEquipment.apply(horse);writing=false;
    }
    @Override public boolean stillValid(Player player) {return horse.isAlive()&&horse.distanceToSqr(player)<64;}
}
