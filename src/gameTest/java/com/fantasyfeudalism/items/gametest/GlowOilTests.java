package com.fantasyfeudalism.items.gametest;

import com.fantasyfeudalism.items.FeudalItems;
import com.fantasyfeudalism.items.content.Content;
import com.fantasyfeudalism.items.lighting.*;
import com.fantasyfeudalism.items.mount.MountEquipment;
import java.util.List;
import net.minecraft.core.*;
import net.minecraft.gametest.framework.*;
import net.minecraft.world.*;
import net.minecraft.world.entity.SlotAccess;
import net.minecraft.world.inventory.*;
import net.minecraft.world.item.*;
import net.minecraft.world.item.crafting.*;
import net.minecraft.world.level.*;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.phys.*;
import net.neoforged.neoforge.event.entity.player.PlayerInteractEvent;
import net.neoforged.neoforge.gametest.*;

@GameTestHolder(FeudalItems.ID)
@PrefixGameTestTemplate(false)
public final class GlowOilTests {
    @GameTest(template="empty")
    public static void glow_oil_recipe_requires_ink_kelp_and_bottle(GameTestHelper h){
        var input=CraftingInput.of(3,1,List.of(new ItemStack(Items.GLOW_INK_SAC),new ItemStack(Items.KELP),new ItemStack(Items.GLASS_BOTTLE)));
        var recipe=h.getLevel().getRecipeManager().getRecipeFor(RecipeType.CRAFTING,input,h.getLevel());
        h.assertTrue(recipe.isPresent()&&recipe.get().value().assemble(input,h.getLevel().registryAccess()).is(Content.GLOW_OIL.get()),"Glow ink, kelp and a bottle craft Glow Oil");
        var missing=CraftingInput.of(2,1,List.of(new ItemStack(Items.GLOW_INK_SAC),new ItemStack(Items.GLASS_BOTTLE)));
        h.assertFalse(h.getLevel().getRecipeManager().getRecipeFor(RecipeType.CRAFTING,missing,h.getLevel()).isPresent(),"Kelp is required");h.succeed();
    }
    @GameTest(template="empty")
    public static void inventory_oil_refills_and_replaces_fuel_type(GameTestHelper h){
        var player=h.makeMockPlayer(GameType.SURVIVAL);var lamp=new ItemStack(Items.LANTERN);LightItemFuel.setStage(lamp,0,false);
        var slot=new Slot(new SimpleContainer(lamp),0,0,0);var oil=new ItemStack(Content.GLOW_OIL.get(),2);
        h.assertTrue(lamp.overrideOtherStackedOnMe(oil,slot,ClickAction.SECONDARY,player,SlotAccess.NULL),"Glow Oil services inventory lanterns");
        h.assertTrue(GlowOil.fueled(lamp)&&LightItemFuel.stage(lamp)==3&&GlowOil.potential(lamp)==30&&oil.getCount()==1,"One oil fills and lights an empty lantern with doubled light potential");
        h.assertTrue(player.getInventory().countItem(Items.GLASS_BOTTLE)==1,"Refueling returns the bottle");
        LightItemFuel.setStage(lamp,2,true);var restored=ItemStack.parseOptional(h.getLevel().registryAccess(),(net.minecraft.nbt.CompoundTag)lamp.save(h.getLevel().registryAccess()));
        h.assertTrue(GlowOil.fueled(restored)&&LightItemFuel.stage(restored)==2,"Fuel identity survives saved item data and stage changes");
        lamp.overrideOtherStackedOnMe(new ItemStack(Content.KELP_OIL.get(),2),slot,ClickAction.SECONDARY,player,SlotAccess.NULL);
        h.assertTrue(GlowOil.potential(lamp)==15&&ItemStack.isSameItemSameComponents(lamp,new ItemStack(Items.LANTERN)),"Normal oil replaces Glow Oil and restores fresh-lantern stacking");h.succeed();
    }
    @GameTest(template="empty")
    public static void placed_glow_oil_respects_switch_and_normal_refills(GameTestHelper h){
        var pos=h.absolutePos(new BlockPos(2,2,2));var level=h.getLevel();level.setBlockAndUpdate(pos.below(),Blocks.STONE.defaultBlockState());level.setBlockAndUpdate(pos,Blocks.LANTERN.defaultBlockState());
        LightFuel.setRemaining(level,pos,0);level.setBlockAndUpdate(pos,level.getBlockState(pos).setValue(BlockStateProperties.LIT,false));
        var player=h.makeMockPlayer(GameType.SURVIVAL);player.setItemInHand(InteractionHand.MAIN_HAND,new ItemStack(Content.GLOW_OIL.get()));
        var event=new PlayerInteractEvent.RightClickBlock(player,InteractionHand.MAIN_HAND,pos,new BlockHitResult(Vec3.atCenterOf(pos),Direction.UP,pos,false));LightInteractions.interact(event);
        h.assertTrue(GlowOil.fueled(level.getBlockState(pos))&&GlowOil.potential(level.getBlockState(pos))==30,"Placed empty lantern lights with Glow Oil");
        h.assertTrue(player.getMainHandItem().is(Items.GLASS_BOTTLE)&&LightFuel.remaining(level,pos)==LightFuel.capacity(level.getBlockState(pos)),"Placed refueling returns a bottle and a full fuel budget");
        level.setBlockAndUpdate(pos,level.getBlockState(pos).setValue(BlockStateProperties.LIT,false));
        h.assertTrue(GlowOil.potential(level.getBlockState(pos))==0,"A switched-off glow lantern emits no extra light");
        player.setItemInHand(InteractionHand.MAIN_HAND,new ItemStack(Content.KELP_OIL.get()));LightInteractions.interact(new PlayerInteractEvent.RightClickBlock(player,InteractionHand.MAIN_HAND,pos,new BlockHitResult(Vec3.atCenterOf(pos),Direction.UP,pos,false)));
        h.assertFalse(GlowOil.fueled(level.getBlockState(pos)),"Normal oil replaces the placed fuel type");
        h.assertFalse(level.getBlockState(pos).getValue(BlockStateProperties.LIT),"Refilling a nonempty switched-off lantern leaves it switched off");h.succeed();
    }
    @GameTest(template="empty")
    public static void glow_fuel_survives_placement_and_block_drops(GameTestHelper h){
        var level=h.getLevel();var base=h.absolutePos(new BlockPos(2,1,2));level.setBlockAndUpdate(base,Blocks.STONE.defaultBlockState());
        var lamp=new ItemStack(Items.LANTERN);GlowOil.set(lamp,true);LightItemFuel.setStage(lamp,2,true);
        var player=h.makeMockPlayer(GameType.SURVIVAL);player.setItemInHand(InteractionHand.MAIN_HAND,lamp);
        var hit=new BlockHitResult(Vec3.atCenterOf(base).add(0,.5,0),Direction.UP,base,false);
        var placed=((BlockItem)Items.LANTERN).place(new net.minecraft.world.item.context.BlockPlaceContext(player,InteractionHand.MAIN_HAND,lamp,hit));
        var pos=base.above();h.assertTrue(placed.consumesAction()&&GlowOil.fueled(level.getBlockState(pos)),"Placement transfers Glow Oil to the block state");
        h.assertTrue(LightFuel.remaining(level,pos)==LightItemFuel.boundary(LightFuel.capacity(level.getBlockState(pos)),2),"Placement preserves the fuel stage");
        level.destroyBlock(pos,true);
        var drops=level.getEntitiesOfClass(net.minecraft.world.entity.item.ItemEntity.class,new AABB(pos).inflate(1));
        h.assertTrue(drops.stream().anyMatch(drop->drop.getItem().is(Items.LANTERN)&&GlowOil.fueled(drop.getItem())&&LightItemFuel.stage(drop.getItem())==2),"Breaking the lantern preserves glow fuel and stage on its dropped item");h.succeed();
    }
    @GameTest(template="empty")
    public static void mounted_glow_oil_and_packed_light_remain_valid(GameTestHelper h){
        var lamp=new ItemStack(Items.LANTERN);MountEquipment.refill(lamp,true);
        h.assertTrue(GlowOil.potential(lamp)==30,"Mounted lantern refills use doubled light potential");
        LightItemFuel.setStage(lamp,0,false);h.assertTrue(GlowOil.potential(lamp)==0,"Empty glow lanterns emit no light");
        MountEquipment.refill(lamp);h.assertTrue(!GlowOil.fueled(lamp)&&GlowOil.potential(lamp)==15,"Normal mount refills replace glow fuel");
        int sky=11<<20;h.assertTrue(GlowOil.mergeLight(sky,30)==(sky|240),"Enhanced light clamps to vanilla level 15 and preserves sky light");
        h.assertTrue(GlowOil.mergeLight(sky|224,10)==(sky|224),"Additional light never darkens an already brighter block");h.succeed();
    }
}
