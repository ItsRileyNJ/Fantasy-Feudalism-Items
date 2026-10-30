package com.fantasyfeudalism.items.clairvoyance;

import com.fantasyfeudalism.items.FeudalItems;
import java.util.List;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.item.alchemy.Potion;

/** Resolve server-synced duration when used, rather than baking a config value into registry creation. */
public final class ClairvoyancePotion extends Potion {
    public ClairvoyancePotion() { super(new MobEffectInstance(FeudalItems.CLAIRVOYANCE, 2400)); }
    @Override
    public List<MobEffectInstance> getEffects() {
        return List.of(new MobEffectInstance(FeudalItems.CLAIRVOYANCE, ClairvoyanceConfig.durationTicks()));
    }
}
