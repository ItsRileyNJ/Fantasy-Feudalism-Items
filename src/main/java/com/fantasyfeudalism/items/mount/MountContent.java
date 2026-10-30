package com.fantasyfeudalism.items.mount;

import com.fantasyfeudalism.items.content.Content;
import com.fantasyfeudalism.items.FeudalItems;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.item.*;
import net.neoforged.neoforge.registries.*;

public final class MountContent {
    public static final DeferredItem<SaddlebagItem> SADDLEBAG=Content.ITEMS.register("saddlebag",()->new SaddlebagItem(new Item.Properties()));
    public static final DeferredItem<Item> HALF=Content.ITEMS.registerSimpleItem("saddlebag_half");
    public static final DeferredItem<Horseshoe> COPPER=Content.ITEMS.register("copper_horseshoe",()->new Horseshoe(.05,.15,false,new Item.Properties()));
    public static final DeferredItem<Horseshoe> IRON=Content.ITEMS.register("iron_horseshoe",()->new Horseshoe(.08,.25,false,new Item.Properties()));
    public static final DeferredItem<Horseshoe> GOLD=Content.ITEMS.register("gold_horseshoe",()->new Horseshoe(.11,.35,true,new Item.Properties()));
    public static final DeferredItem<Horseshoe> DIAMOND=Content.ITEMS.register("diamond_horseshoe",()->new Horseshoe(.15,.5,false,new Item.Properties()));
    public static final DeferredItem<Horseshoe> NETHERITE=Content.ITEMS.register("netherite_horseshoe",()->new Horseshoe(.2,.7,false,new Item.Properties()));
    public static final DeferredRegister<MenuType<?>> MENUS=DeferredRegister.create(Registries.MENU,FeudalItems.ID);
    public static final DeferredHolder<MenuType<?>,MenuType<BagMenu>> BAG_MENU=MENUS.register("saddlebag",()->new MenuType<>(BagMenu::new,net.minecraft.world.flag.FeatureFlags.DEFAULT_FLAGS));
    public static final DeferredHolder<MenuType<?>,MenuType<MountMenu>> MOUNT_MENU=MENUS.register("mount_equipment",()->net.neoforged.neoforge.common.extensions.IMenuTypeExtension.create(MountMenu::client));
    public static void init() {}
}
