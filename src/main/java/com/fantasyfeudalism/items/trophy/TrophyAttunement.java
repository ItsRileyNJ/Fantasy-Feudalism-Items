package com.fantasyfeudalism.items.trophy;

import com.fantasyfeudalism.items.FeudalItems;
import java.util.EnumMap;
import java.util.Map;
import java.util.Set;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.effect.*;
import net.minecraft.world.entity.player.Player;
import net.neoforged.neoforge.common.EffectCure;
import net.neoforged.neoforge.registries.*;

/** Display-only status effects; the ledger remains the source of all trophy bonuses. */
public final class TrophyAttunement {
    public static final DeferredRegister<MobEffect> REGISTRY=DeferredRegister.create(Registries.MOB_EFFECT,FeudalItems.ID);
    public static final Map<TrophyKind,DeferredHolder<MobEffect,MobEffect>> EFFECTS=new EnumMap<>(TrophyKind.class);
    static {
        for(var kind:TrophyKind.values())EFFECTS.put(kind,REGISTRY.register("attuned_"+kind.id(),Indicator::new));
    }
    private static final class Indicator extends MobEffect {
        private Indicator(){super(MobEffectCategory.BENEFICIAL,0xD4AF37);}
        @Override public void fillEffectCures(Set<EffectCure> cures,MobEffectInstance instance){
            // Drinking milk does not change attunement, so it must not hide its indicator.
        }
    }
    public static java.util.List<net.minecraft.network.chat.Component> tooltip(MobEffectInstance instance){
        for(var entry:EFFECTS.entrySet())if(entry.getValue().value()==instance.getEffect().value()){
            var lines=new java.util.ArrayList<net.minecraft.network.chat.Component>();
            lines.add(instance.getEffect().value().getDisplayName().copy().withStyle(net.minecraft.ChatFormatting.GOLD));
            entry.getKey().appendEffectTooltip(lines);
            return lines;
        }
        return java.util.List.of();
    }
    public static void sync(Player player,TrophyKind active){
        if(player.level().isClientSide)return;
        for(var entry:EFFECTS.entrySet()){
            var effect=entry.getValue();
            if(entry.getKey()!=active)player.removeEffect(effect);
            else {
                var current=player.getEffect(effect);
                if(current==null||!current.isInfiniteDuration()||current.getAmplifier()!=0||current.isVisible()||!current.showIcon()){
                    player.removeEffect(effect);
                    player.addEffect(new MobEffectInstance(effect,-1,0,true,false,true));
                }
            }
        }
    }
    private TrophyAttunement() {}
}
