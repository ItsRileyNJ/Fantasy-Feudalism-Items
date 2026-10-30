package com.fantasyfeudalism.items.trophy.mixin;
import com.fantasyfeudalism.items.trophy.Trophies;
import net.minecraft.network.chat.contents.TranslatableContents;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.Enchantment;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
@Mixin(Enchantment.class)
abstract class EnchantmentSupportMixin {
    @Inject(method="isSupportedItem", at=@At("HEAD"), cancellable=true)
    private void feudalItems$headOnly(ItemStack stack, CallbackInfoReturnable<Boolean> result) {
        boolean sigil = ((Enchantment)(Object)this).description().getContents() instanceof TranslatableContents text && text.getKey().equals("enchantment.fantasy_feudalism_items.sigil");
        if (stack.is(Trophies.HEADS) || sigil) result.setReturnValue(stack.is(Trophies.HEADS) && sigil);
    }
}
