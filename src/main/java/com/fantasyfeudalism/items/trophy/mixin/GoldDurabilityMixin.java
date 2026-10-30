package com.fantasyfeudalism.items.trophy.mixin;
import com.fantasyfeudalism.items.trophy.*;
import net.minecraft.server.level.*;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.*;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.*;
@Mixin(ItemStack.class)
abstract class GoldDurabilityMixin {
    @ModifyVariable(method="hurtAndBreak(ILnet/minecraft/server/level/ServerLevel;Lnet/minecraft/world/entity/LivingEntity;Ljava/util/function/Consumer;)V", at=@At("HEAD"), argsOnly=true)
    private int feudalItems$goldWear(int amount, int original, ServerLevel level, LivingEntity user, java.util.function.Consumer<Item> broken) {
        if (user instanceof net.minecraft.world.entity.player.Player player && TrophyEffects.active(player) == TrophyKind.PIGLIN && TrophyEffects.gold((ItemStack)(Object)this)) {
            int saved = 0; for (int i = 0; i < amount; i++) if (user.getRandom().nextFloat() < 0.3f) saved++;
            return amount - saved;
        }
        return amount;
    }
}
