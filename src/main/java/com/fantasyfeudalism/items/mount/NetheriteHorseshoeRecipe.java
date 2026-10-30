package com.fantasyfeudalism.items.mount;
import com.mojang.serialization.MapCodec;
import net.minecraft.core.*;
import net.minecraft.network.*;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.item.*;
import net.minecraft.world.item.crafting.*;
import net.minecraft.world.level.Level;
public final class NetheriteHorseshoeRecipe implements SmithingRecipe {
    public boolean matches(SmithingRecipeInput input,Level world){return input.template().isEmpty()&&isBaseIngredient(input.base())&&isAdditionIngredient(input.addition());}
    public ItemStack assemble(SmithingRecipeInput input,HolderLookup.Provider provider){return input.base().transmuteCopy(MountContent.NETHERITE.get(),1);}
    public ItemStack getResultItem(HolderLookup.Provider provider){return new ItemStack(MountContent.NETHERITE.get());}
    public boolean isTemplateIngredient(ItemStack stack){return stack.isEmpty();}
    public boolean isBaseIngredient(ItemStack stack){return stack.is(MountContent.DIAMOND.get());}
    public boolean isAdditionIngredient(ItemStack stack){return stack.is(Items.NETHERITE_INGOT);}
    public RecipeSerializer<?> getSerializer(){return MountRecipes.SMITHING.get();}
    public static final class Serializer implements RecipeSerializer<NetheriteHorseshoeRecipe>{
        private static final MapCodec<NetheriteHorseshoeRecipe> CODEC=MapCodec.unit(NetheriteHorseshoeRecipe::new);
        public MapCodec<NetheriteHorseshoeRecipe> codec(){return CODEC;}
        public StreamCodec<RegistryFriendlyByteBuf,NetheriteHorseshoeRecipe> streamCodec(){return StreamCodec.of((buffer,recipe)->{},buffer->new NetheriteHorseshoeRecipe());}
    }
}
