package com.fantasyfeudalism.items.mount;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;
import net.neoforged.neoforge.common.loot.*;
import net.neoforged.neoforge.registries.*;
public final class SaddlebagLoot extends LootModifier {
    public static final MapCodec<SaddlebagLoot> CODEC=RecordCodecBuilder.mapCodec(instance->codecStart(instance).apply(instance,SaddlebagLoot::new));
    public static final DeferredRegister<MapCodec<? extends IGlobalLootModifier>> REGISTRY=DeferredRegister.create(NeoForgeRegistries.Keys.GLOBAL_LOOT_MODIFIER_SERIALIZERS,"fantasy_feudalism_items");
    static{REGISTRY.register("saddlebags",()->CODEC);}
    public SaddlebagLoot(LootItemCondition[] conditions){super(conditions);}
    protected ObjectArrayList<ItemStack> doApply(ObjectArrayList<ItemStack> loot,LootContext context){if(net.neoforged.fml.ModList.get().isLoaded("villagerscontract"))loot.add(new ItemStack(MountContent.SADDLEBAG.get()));return loot;}
    public MapCodec<? extends IGlobalLootModifier> codec(){return CODEC;}
}
