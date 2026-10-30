package com.fantasyfeudalism.items.mount;

import net.minecraft.world.*;
import net.minecraft.world.entity.player.*;
import net.minecraft.world.inventory.*;
import net.minecraft.world.item.*;

public final class MountMenu extends ChestMenu {
    public final net.minecraft.world.entity.animal.horse.AbstractHorse horse;
    private static final class ClientInventory extends SimpleContainer {
        final net.minecraft.world.entity.animal.horse.AbstractHorse horse;
        ClientInventory(net.minecraft.world.entity.animal.horse.AbstractHorse horse){super(36);this.horse=horse;}
        @Override public boolean canPlaceItem(int slot,ItemStack item){return horse!=null&&MountInventory.allowed(horse,slot,item,!getItem(4).isEmpty());}
    }
    private static net.minecraft.world.entity.animal.horse.AbstractHorse mount(Container container){return container instanceof MountInventory server?server.horse():container instanceof ClientInventory client?client.horse:null;}
    public static MountMenu client(int id,Inventory inventory,net.minecraft.network.RegistryFriendlyByteBuf data){var entity=inventory.player.level().getEntity(data.readVarInt());return new MountMenu(id,inventory,new ClientInventory(entity instanceof net.minecraft.world.entity.animal.horse.AbstractHorse h?h:null));}
    public boolean disabled(int index){return index<9&&(index>=5||(horse instanceof net.minecraft.world.entity.animal.horse.Horse?index==4:index==1||index==2));}
    public MountMenu(int id,Inventory inventory,Container container){super(MountContent.MOUNT_MENU.get(),id,inventory,container,mount(container) instanceof net.minecraft.world.entity.animal.horse.Horse?1:4);horse=mount(container);}
    @Override protected Slot addSlot(Slot slot){
        int number=slots.size();int storage=getRowCount()*9;
        return super.addSlot(new Slot(slot.container,slot.getSlotIndex(),slot.x,slot.y){
            @Override public boolean mayPlace(ItemStack item){return number>=storage||slot.container.canPlaceItem(slot.getSlotIndex(),item);}
            @Override public int getMaxStackSize(){return number<9?1:super.getMaxStackSize();}
        });
    }
    @Override public void clicked(int slot,int button,ClickType type,Player player){
        if(slot==3&&button==1&&!getContainer().getItem(3).isEmpty()){
            var lantern=getContainer().getItem(3);
            if(type==ClickType.QUICK_MOVE){MountEquipment.toggle(lantern);getContainer().setChanged();broadcastChanges();return;}
            if(type==ClickType.PICKUP&&getCarried().is(com.fantasyfeudalism.items.content.Content.KELP_OIL.get())&&lantern.is(Items.LANTERN)){
                MountEquipment.refill(lantern);
                if(!player.getAbilities().instabuild){getCarried().shrink(1);var bottle=new ItemStack(Items.GLASS_BOTTLE);if(getCarried().isEmpty())setCarried(bottle);else if(!player.getInventory().add(bottle))player.drop(bottle,false);}
                getContainer().setChanged();broadcastChanges();return;
            }
        }
        super.clicked(slot,button,type,player);
    }
}
