package com.fantasyfeudalism.items.trophy.mixin;

import com.fantasyfeudalism.items.content.Content;
import net.minecraft.world.damagesource.*;
import net.minecraft.world.entity.*;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.*;

@Mixin(LivingEntity.class)
abstract class DaggerArmorMixin {
    @Redirect(method="getDamageAfterArmorAbsorb", at=@At(value="INVOKE", target="Lnet/minecraft/world/damagesource/CombatRules;getDamageAfterAbsorb(Lnet/minecraft/world/entity/LivingEntity;FLnet/minecraft/world/damagesource/DamageSource;FF)F"))
    private float feudalItems$armorPenetration(LivingEntity victim, float damage, DamageSource source, float armor, float toughness) {
        if (source.getDirectEntity() instanceof net.minecraft.world.entity.player.Player player && source.getEntity() == player && source.is(DamageTypes.PLAYER_ATTACK) && player.getMainHandItem().is(Content.BONE_DAGGER.get()))
            return damage * 0.2f + CombatRules.getDamageAfterAbsorb(victim, damage * 0.8f, source, armor, toughness);
        return CombatRules.getDamageAfterAbsorb(victim, damage, source, armor, toughness);
    }
}
