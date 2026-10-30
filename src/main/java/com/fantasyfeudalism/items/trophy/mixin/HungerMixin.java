package com.fantasyfeudalism.items.trophy.mixin;
import com.fantasyfeudalism.items.trophy.*;
import net.minecraft.world.entity.player.Player;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.*;
@Mixin(Player.class)
abstract class HungerMixin {
    @ModifyVariable(method="causeFoodExhaustion", at=@At("HEAD"), argsOnly=true)
    private float feudalItems$zombieHunger(float value) { var player=(Player)(Object)this;float amount=TrophyEffects.active(player)==TrophyKind.ZOMBIE?value*1.25f:value;
        double fraction=player.getPersistentData().getDouble("ff_milk_food_fraction");
        if(amount>0&&fraction>0&&player.getFoodData().getSaturationLevel()<=0){double used=Math.min(fraction,amount/4.0);player.getPersistentData().putDouble("ff_milk_food_fraction",fraction-used);amount-=(float)(used*4);}
        return amount; }
}
