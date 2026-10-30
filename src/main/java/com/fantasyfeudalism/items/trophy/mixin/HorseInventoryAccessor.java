package com.fantasyfeudalism.items.trophy.mixin;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.animal.horse.AbstractHorse;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;
@Mixin(AbstractHorse.class)
public interface HorseInventoryAccessor { @Accessor("inventory") SimpleContainer feudalItems$inventory(); }
