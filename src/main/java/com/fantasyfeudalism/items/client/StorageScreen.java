package com.fantasyfeudalism.items.client;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.ChestMenu;
public final class StorageScreen<T extends ChestMenu> extends AbstractContainerScreen<T> {
    private static final ResourceLocation TEXTURE=ResourceLocation.withDefaultNamespace("textures/gui/container/generic_54.png");
    public StorageScreen(T menu,Inventory inventory,Component title){super(menu,inventory,title);imageHeight=114+menu.getRowCount()*18;inventoryLabelY=imageHeight-94;}
    @Override protected void renderBg(GuiGraphics gui,float partial,int mouseX,int mouseY){int x=(width-imageWidth)/2,y=(height-imageHeight)/2;gui.blit(TEXTURE,x,y,0,0,imageWidth,menu.getRowCount()*18+17);gui.blit(TEXTURE,x,y+menu.getRowCount()*18+17,0,126,imageWidth,96);
        if(menu instanceof com.fantasyfeudalism.items.mount.MountMenu mount)for(int slot=0;slot<9;slot++)if(mount.disabled(slot))gui.fill(x+8+slot*18,y+18,x+24+slot*18,y+34,0xFF404040);}
    @Override public void render(GuiGraphics gui,int x,int y,float partial){super.render(gui,x,y,partial);renderTooltip(gui,x,y);if(menu instanceof com.fantasyfeudalism.items.mount.MountMenu&&hoveredSlot!=null&&hoveredSlot.index<9&&hoveredSlot.getItem().isEmpty()){
        String[] names={"Saddle","Horse Armor","Horseshoe","Lantern","Saddlebag","Unavailable","Unavailable","Unavailable","Unavailable"};gui.renderTooltip(font,Component.literal(((com.fantasyfeudalism.items.mount.MountMenu)menu).disabled(hoveredSlot.index)?"Unavailable":names[hoveredSlot.index]),x,y);
    }}
}
