package com.fantasyfeudalism.items.lighting.mixin;

import com.fantasyfeudalism.items.lighting.*;
import com.fantasyfeudalism.items.content.Content;
import net.minecraft.world.*;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.*;
import net.minecraft.world.item.*;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.*;

@Mixin(Item.class)
abstract class LightItemMixin {
    @Inject(method="inventoryTick",at=@At("HEAD"))
    private void feudalItems$heldFuel(ItemStack stack,Level level,Entity entity,int slot,boolean selected,CallbackInfo ci){
        if(entity instanceof Player player&&(stack==player.getMainHandItem()||stack==player.getOffhandItem()))LightItemFuel.advance(stack,player);
    }
    @Inject(method="use",at=@At("HEAD"),cancellable=true)
    private void feudalItems$toggle(Level level,Player player,InteractionHand hand,CallbackInfoReturnable<InteractionResultHolder<ItemStack>> result){
        var item=player.getItemInHand(hand);if(!player.isShiftKeyDown()||(!item.is(Items.LANTERN)&&!item.is(Items.SOUL_LANTERN)))return;
        if(!level.isClientSide)LightItemFuel.setStage(item,LightItemFuel.stage(item),!LightItemFuel.lit(item));
        result.setReturnValue(InteractionResultHolder.sidedSuccess(item,level.isClientSide));
    }
    @Inject(method="overrideOtherStackedOnMe",at=@At("HEAD"),cancellable=true)
    private void feudalItems$service(ItemStack light,ItemStack carried,Slot slot,ClickAction action,Player player,SlotAccess cursor,CallbackInfoReturnable<Boolean> result){
        if(!player.level().isClientSide){LightItemFuel.normalize(light);LightItemFuel.normalize(carried);}
        var state=LightItemFuel.state(light);if(state==null||action!=ClickAction.SECONDARY||!slot.allowModification(player))return;
        boolean oil=light.is(Items.LANTERN)&&GlowOil.isOil(carried);
        boolean flint=(WeatherLighting.torch(state)||light.is(Items.SOUL_TORCH))&&carried.is(Items.FLINT_AND_STEEL);
        if(!oil&&!flint)return;
        if(flint&&!player.getAbilities().instabuild&&carried.isDamageableItem()&&carried.getMaxDamage()-carried.getDamageValue()<light.getCount()){
            player.displayClientMessage(net.minecraft.network.chat.Component.translatable("message.fantasy_feudalism_items.flint_too_worn"),true);
            result.setReturnValue(true);return;
        }
        if(!player.level().isClientSide){
            boolean empty=LightItemFuel.remaining(light)==0;
            LightItemFuel.setStage(light,3,flint||empty||LightItemFuel.lit(light));
            LightItemFuel.resetProgress(player,light);
            if(oil)GlowOil.set(light,carried.is(Content.GLOW_OIL.get()));
            if(!player.getAbilities().instabuild){
                if(oil){carried.shrink(1);var bottle=new ItemStack(Items.GLASS_BOTTLE);if(carried.isEmpty())cursor.set(bottle);else if(!player.getInventory().add(bottle))player.drop(bottle,false);}
                else carried.hurtAndBreak(light.getCount(),player,LivingEntity.getSlotForHand(net.minecraft.world.InteractionHand.MAIN_HAND));
            }
            slot.setChanged();
        }
        result.setReturnValue(true);
    }
}
