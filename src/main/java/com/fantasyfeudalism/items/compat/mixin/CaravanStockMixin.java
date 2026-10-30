package com.fantasyfeudalism.items.compat.mixin;
import com.fantasyfeudalism.items.compat.CaravanTrades;
import net.minecraft.core.HolderLookup;
import net.minecraft.world.item.trading.MerchantOffers;
import org.spongepowered.asm.mixin.*;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
@Pseudo
@Mixin(targets="dev.villagerscontract.CaravanStock",remap=false)
abstract class CaravanStockMixin {
    @Inject(method="offers",at=@At("RETURN"),remap=false)
    private static void feudalItems$caravanStock(boolean guarded,HolderLookup.Provider registry,CallbackInfoReturnable<MerchantOffers> result){CaravanTrades.add(result.getReturnValue());}
}
