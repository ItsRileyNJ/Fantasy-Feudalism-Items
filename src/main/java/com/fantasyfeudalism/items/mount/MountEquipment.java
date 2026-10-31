package com.fantasyfeudalism.items.mount;

import com.fantasyfeudalism.items.lighting.*;
import net.minecraft.core.component.DataComponents;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.*;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.ai.attributes.*;
import net.minecraft.world.entity.animal.horse.*;
import net.minecraft.world.item.*;
import net.minecraft.world.item.component.CustomData;
import net.neoforged.neoforge.event.entity.player.PlayerInteractEvent;
import net.neoforged.neoforge.event.entity.living.*;
import net.neoforged.neoforge.event.tick.EntityTickEvent;

public final class MountEquipment {
    private static final ResourceLocation SPEED=ResourceLocation.fromNamespaceAndPath("fantasy_feudalism_items","horseshoe_speed"),JUMP=ResourceLocation.fromNamespaceAndPath("fantasy_feudalism_items","horseshoe_jump");
    private static final java.util.Map<AbstractHorse,java.lang.ref.WeakReference<MountInventory>> OPEN=new java.util.WeakHashMap<>();
    public static MountInventory inventory(AbstractHorse horse){var reference=OPEN.get(horse);var inventory=reference==null?null:reference.get();if(inventory==null){inventory=new MountInventory(horse);OPEN.put(horse,new java.lang.ref.WeakReference<>(inventory));}return inventory;}
    public static boolean supported(Entity entity){return entity.getType()==EntityType.HORSE||entity.getType()==EntityType.DONKEY||entity.getType()==EntityType.MULE;}
    public static ItemStack item(AbstractHorse horse,int slot){var data=horse.getPersistentData().getCompound("ff_equipment_"+slot);return data.isEmpty()?ItemStack.EMPTY:ItemStack.parse(horse.registryAccess(),data).orElse(ItemStack.EMPTY);}
    public static void set(AbstractHorse horse,int slot,ItemStack stack){
        if(stack.isEmpty())horse.getPersistentData().remove("ff_equipment_"+slot);else horse.getPersistentData().put("ff_equipment_"+slot,stack.save(horse.registryAccess()));
        var visuals=horse.getData(com.fantasyfeudalism.items.content.ModAttachments.MOUNT).copy();
        var display=stack.copy();display.remove(DataComponents.CONTAINER);
        if(display.has(DataComponents.CUSTOM_DATA))CustomData.update(DataComponents.CUSTOM_DATA,display,tag->{tag.remove("ff_fuel");tag.remove("ff_last_burn_tick");tag.remove("ff_last_burn_dimension");});
        if(display.isEmpty())visuals.remove("Slot"+slot);else visuals.put("Slot"+slot,display.save(horse.registryAccess()));
        if(!visuals.equals(horse.getData(com.fantasyfeudalism.items.content.ModAttachments.MOUNT)))horse.setData(com.fantasyfeudalism.items.content.ModAttachments.MOUNT,visuals);
    }
    public static float jumpVelocity(Horse horse,float natural){
        ItemStack shoe;
        if(horse.level().isClientSide){var data=horse.getData(com.fantasyfeudalism.items.content.ModAttachments.MOUNT);shoe=data.contains("Slot2")?ItemStack.parse(horse.registryAccess(),data.getCompound("Slot2")).orElse(ItemStack.EMPTY):ItemStack.EMPTY;}else shoe=item(horse,2);
        if(!(shoe.getItem() instanceof Horseshoe h)||!h.gold||natural<=0)return natural;
        double low=natural,high=Math.max(2,natural*2),target=jumpHeight(natural)*1.10;
        for(int i=0;i<60;i++){double middle=(low+high)/2;if(jumpHeight(middle)<target)low=middle;else high=middle;}
        return(float)((low+high)/2);
    }
    public static double jumpHeight(double velocity){double y=0;for(int i=0;i<1000&&velocity>0;i++){y+=velocity;velocity=(velocity-0.08)*0.98;}return y;}
    public static void join(net.neoforged.neoforge.event.entity.EntityJoinLevelEvent event){
        if(!(event.getEntity() instanceof AbstractHorse horse)||event.getLevel().isClientSide||!supported(horse))return;
        if(horse instanceof AbstractChestedHorse chested&&chested.hasChest()){
            var old=((com.fantasyfeudalism.items.trophy.mixin.HorseInventoryAccessor)horse).feudalItems$inventory();
            var contents=net.minecraft.core.NonNullList.withSize(27,ItemStack.EMPTY);for(int i=1;i<old.getContainerSize()&&i<=27;i++)contents.set(i-1,old.getItem(i).copy());
            var migrated=new ItemStack(MountContent.SADDLEBAG.get());migrated.set(DataComponents.CONTAINER,net.minecraft.world.item.component.ItemContainerContents.fromItems(contents));
            if(item(horse,4).isEmpty())set(horse,4,migrated);else horse.spawnAtLocation(migrated);
            horse.getSlot(499).set(ItemStack.EMPTY);
        }
        for(int i=2;i<=4;i++)set(horse,i,item(horse,i));apply(horse);
    }
    public static void apply(AbstractHorse horse){
        var shoe=item(horse,2).getItem();var speed=horse.getAttribute(Attributes.MOVEMENT_SPEED);var jump=horse.getAttribute(Attributes.JUMP_STRENGTH);
        if(speed!=null){double desired=shoe instanceof Horseshoe h?h.speed:0;var existing=speed.getModifier(SPEED);
            if(existing!=null&&existing.amount()!=desired){speed.removeModifier(SPEED);existing=null;}
            if(desired!=0&&existing==null)speed.addTransientModifier(new AttributeModifier(SPEED,desired,AttributeModifier.Operation.ADD_MULTIPLIED_TOTAL));}
        if(jump!=null)jump.removeModifier(JUMP);

    }
    public static void interact(PlayerInteractEvent.EntityInteract event){
        if(!(event.getTarget() instanceof AbstractHorse horse)||!supported(horse))return;
        if((horse instanceof Donkey||horse instanceof Mule)&&event.getItemStack().is(Items.CHEST)){event.setCanceled(true);event.setCancellationResult(InteractionResult.FAIL);return;}
        if(!horse.isTamed()||!event.getEntity().isShiftKeyDown())return;
        if(event.getEntity() instanceof net.minecraft.server.level.ServerPlayer player)player.openMenu(new SimpleMenuProvider((id,inv,p)->new MountMenu(id,inv,inventory(horse)),net.minecraft.network.chat.Component.literal("Mount Equipment")),buffer->buffer.writeVarInt(horse.getId()));
        event.setCanceled(true);event.setCancellationResult(InteractionResult.sidedSuccess(horse.level().isClientSide));
    }
    public static void drops(LivingDropsEvent event){
        if(!(event.getEntity() instanceof AbstractHorse horse)||!supported(horse))return;
        for(int i=2;i<=4;i++){var stack=item(horse,i);if(!stack.isEmpty()){event.getDrops().add(new net.minecraft.world.entity.item.ItemEntity(horse.level(),horse.getX(),horse.getY(),horse.getZ(),stack));set(horse,i,ItemStack.EMPTY);}}
    }
    public static void damage(LivingIncomingDamageEvent event){if(event.getEntity() instanceof Horse horse&&event.getSource().is(net.minecraft.tags.DamageTypeTags.IS_FALL)&&item(horse,2).getItem() instanceof Horseshoe shoe)event.setAmount((float)(event.getAmount()*(1-shoe.fall)));}
    public static long remaining(ItemStack lantern){if(lantern.is(Items.SOUL_LANTERN))return Long.MAX_VALUE;var tag=lantern.getOrDefault(DataComponents.CUSTOM_DATA,CustomData.EMPTY).copyTag();return tag.contains("ff_fuel")?tag.getLong("ff_fuel"):LightFuel.capacity(net.minecraft.world.level.block.Blocks.LANTERN.defaultBlockState());}
    public static boolean lit(ItemStack lantern){var tag=lantern.getOrDefault(DataComponents.CUSTOM_DATA,CustomData.EMPTY).copyTag();return !tag.getBoolean("ff_off")&&remaining(lantern)>0;}
    private static void fuel(ItemStack lantern,long remaining,boolean lit){CustomData.update(DataComponents.CUSTOM_DATA,lantern,tag->{tag.putLong("ff_fuel",remaining);tag.putBoolean("ff_off",!lit);});}
    public static void toggle(ItemStack lantern){fuel(lantern,remaining(lantern),!lit(lantern));}
    public static void refill(ItemStack lantern){boolean wasEmpty=remaining(lantern)==0;fuel(lantern,LightFuel.capacity(net.minecraft.world.level.block.Blocks.LANTERN.defaultBlockState()),wasEmpty||lit(lantern));}
    public static void tick(EntityTickEvent.Post event){
        if(!(event.getEntity() instanceof AbstractHorse horse)||horse.level().isClientSide||!supported(horse)||horse.level().getGameTime()%20!=0)return;
        apply(horse);
        var reference=OPEN.get(horse);var menu=reference==null?null:reference.get();
        var lantern=menu==null?item(horse,3):menu.getItem(3);if(lantern.isEmpty()||lantern.is(Items.SOUL_LANTERN)||!lit(lantern))return;
        com.fantasyfeudalism.items.lighting.LightItemFuel.advance(lantern,horse);set(horse,3,lantern);
    }
}
