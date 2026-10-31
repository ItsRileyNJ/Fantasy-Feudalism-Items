package com.fantasyfeudalism.items.mount;

import com.fantasyfeudalism.items.content.ModAttachments;
import java.util.WeakHashMap;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.item.ItemStack;

/** Decode synchronized equipment once per attachment replacement, rather than per frame. */
public final class MountVisuals {
    private record Entry(CompoundTag data,ItemStack lantern,ItemStack bag,ItemStack shoe) {}
    private static final WeakHashMap<Entity,Entry> CACHE=new WeakHashMap<>();
    private static synchronized Entry get(Entity entity){
        var data=entity.getData(ModAttachments.MOUNT);var cached=CACHE.get(entity);
        if(cached==null||cached.data()!=data){cached=new Entry(data,parse(entity,data,3),parse(entity,data,4),parse(entity,data,2));CACHE.put(entity,cached);}
        return cached;
    }
    private static ItemStack parse(Entity entity,CompoundTag data,int slot){return data.contains("Slot"+slot)?ItemStack.parse(entity.registryAccess(),data.getCompound("Slot"+slot)).orElse(ItemStack.EMPTY):ItemStack.EMPTY;}
    public static ItemStack lantern(Entity entity){return get(entity).lantern();}
    public static ItemStack bag(Entity entity){return get(entity).bag();}
    public static ItemStack shoe(Entity entity){return get(entity).shoe();}
}
