package com.fantasyfeudalism.items.mount;
import net.minecraft.core.HolderLookup;
import net.minecraft.world.item.*;
import net.minecraft.world.item.component.DyedItemColor;
import net.minecraft.world.item.crafting.*;
import net.minecraft.world.level.Level;
public final class SaddlebagDyeRecipe extends CustomRecipe {
    public static final java.util.function.Supplier<RecipeSerializer<?>> SERIALIZER=com.fantasyfeudalism.items.trophy.TrophyRecipes.REGISTRY.register("saddlebag_dye",()->new SimpleCraftingRecipeSerializer<>(SaddlebagDyeRecipe::new));
    public SaddlebagDyeRecipe(CraftingBookCategory category){super(category);}
    public boolean matches(CraftingInput input,Level level){int bags=0,dyes=0;for(var item:input.items()){if(item.isEmpty())continue;if(item.is(MountContent.SADDLEBAG.get()))bags++;else if(item.getItem() instanceof DyeItem)dyes++;else return false;}return bags==1&&dyes>0;}
    public ItemStack assemble(CraftingInput input,HolderLookup.Provider provider){ItemStack bag=ItemStack.EMPTY;var dyes=new java.util.ArrayList<DyeItem>();for(var item:input.items()){if(item.is(MountContent.SADDLEBAG.get()))bag=item.copyWithCount(1);else if(item.getItem() instanceof DyeItem dye)dyes.add(dye);}return bag.isEmpty()?bag:DyedItemColor.applyDyes(bag,dyes);}
    public boolean canCraftInDimensions(int width,int height){return width*height>=2;}
    public RecipeSerializer<?> getSerializer(){return SERIALIZER.get();}
    public static void init(){}
}
