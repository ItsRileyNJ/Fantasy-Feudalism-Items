package com.fantasyfeudalism.items.gametest;

import com.fantasyfeudalism.items.FeudalItems;
import com.fantasyfeudalism.items.lighting.WeatherLighting;
import net.minecraft.core.BlockPos;
import net.minecraft.gametest.framework.*;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.material.Fluids;
import net.neoforged.neoforge.gametest.*;

@GameTestHolder(FeudalItems.ID)
@PrefixGameTestTemplate(false)
public final class WeatherTests {
    @GameTest(template = "empty")
    public static void light_defaults_and_extinguished_emission(GameTestHelper h) {
        for (var block : new Block[]{Blocks.TORCH, Blocks.WALL_TORCH, Blocks.LANTERN, Blocks.SOUL_TORCH, Blocks.SOUL_WALL_TORCH, Blocks.SOUL_LANTERN}) {
            var lit = block.defaultBlockState();
            h.assertTrue(lit.getValue(BlockStateProperties.LIT), "Vanilla lights start lit");
            h.assertTrue(lit.getLightEmission(h.getLevel(), h.absolutePos(BlockPos.ZERO)) > 0, "Lit sources emit light");
            var unlit = lit.setValue(BlockStateProperties.LIT, false);
            h.assertTrue(unlit.getLightEmission(h.getLevel(), h.absolutePos(BlockPos.ZERO)) == 0, "Extinguished sources emit no light");
        }
        h.succeed();
    }
    @GameTest(template = "empty")
    public static void weather_rules_and_independent_probabilities(GameTestHelper h) {
        var torch = Blocks.TORCH.defaultBlockState();
        var lantern = Blocks.LANTERN.defaultBlockState();
        h.assertTrue(WeatherLighting.chance(torch, false, true) == 0, "Roof protects torch even during storm");
        h.assertTrue(WeatherLighting.chance(lantern, true, false) == 0, "Rain alone cannot extinguish lantern");
        h.assertTrue(WeatherLighting.chance(torch, true, false) == WeatherLighting.TORCH_RAIN.get(), "Normal rain has its own chance");
        h.assertTrue(WeatherLighting.chance(torch, true, true) == WeatherLighting.TORCH_STORM.get(), "Storm uses its own chance instead of double-rolling rain");
        h.assertTrue(WeatherLighting.chance(lantern, true, true) == WeatherLighting.LANTERN_STORM.get(), "Lantern storm chance is independent");
        h.assertTrue(WeatherLighting.chance(torch.setValue(BlockStateProperties.LIT, false), true, true) == 0, "Unlit sources do not roll again");
        for (var block : new Block[]{Blocks.SOUL_TORCH, Blocks.SOUL_WALL_TORCH, Blocks.SOUL_LANTERN}) {
            var state = block.defaultBlockState().setValue(BlockStateProperties.WATERLOGGED, true);
            h.assertTrue(WeatherLighting.chance(state, true, true) == 0, "Soul lights ignore rain, storm and water");
        }
        double old = WeatherLighting.TORCH_RAIN.get();
        try {
            WeatherLighting.TORCH_RAIN.set(0.0);
            h.assertTrue(WeatherLighting.chance(torch, true, false) == 0, "Zero probability disables the rain event");
        } finally { WeatherLighting.TORCH_RAIN.set(old); }
        h.succeed();
    }
    @GameTest(template = "empty")
    public static void water_extinguishes_without_destroying_torches(GameTestHelper h) {
        var pos = h.absolutePos(new BlockPos(2, 2, 2));
        h.getLevel().setBlockAndUpdate(pos.below(), Blocks.STONE.defaultBlockState());
        h.getLevel().setBlockAndUpdate(pos, Blocks.TORCH.defaultBlockState());
        var waterloggable = (SimpleWaterloggedBlock) Blocks.TORCH;
        h.assertTrue(waterloggable.placeLiquid(h.getLevel(), pos, h.getLevel().getBlockState(pos), Fluids.WATER.getSource(false)), "Flowing water can waterlog a torch");
        double old = WeatherLighting.TORCH_WATER.get();
        try {
            WeatherLighting.TORCH_WATER.set(1.0);
            WeatherLighting.check(h.getLevel(), pos, 0.0);
            var state = h.getLevel().getBlockState(pos);
            h.assertTrue(state.is(Blocks.TORCH), "Water preserves the torch block");
            h.assertFalse(state.getValue(BlockStateProperties.LIT), "Water extinguishes the torch");
            h.assertTrue(state.getFluidState().is(Fluids.WATER), "Water remains in extinguished torch");
        } finally { WeatherLighting.TORCH_WATER.set(old); }
        h.succeed();
    }
    @GameTest(template = "empty", timeoutTicks = 40)
    public static void scheduled_water_checks_run_in_loaded_world(GameTestHelper h) {
        var pos = h.absolutePos(new BlockPos(2, 2, 2));
        h.getLevel().setBlockAndUpdate(pos.below(), Blocks.STONE.defaultBlockState());
        int oldInterval = WeatherLighting.INTERVAL.get();
        try {
            WeatherLighting.INTERVAL.set(1);
            h.getLevel().setBlockAndUpdate(pos, Blocks.TORCH.defaultBlockState().setValue(BlockStateProperties.WATERLOGGED, true));
        } finally { WeatherLighting.INTERVAL.set(oldInterval); }
        h.succeedWhen(() -> h.assertFalse(h.getLevel().getBlockState(pos).getValue(BlockStateProperties.LIT), "Scheduled checks extinguish immersed lights"));
    }
    @GameTest(template = "empty")
    public static void relighting_and_lantern_controls(GameTestHelper h) {
        var pos = h.absolutePos(new BlockPos(2, 2, 2));
        h.getLevel().setBlockAndUpdate(pos.below(), Blocks.STONE.defaultBlockState());
        var player = h.makeMockPlayer(net.minecraft.world.level.GameType.CREATIVE);
        player.setItemInHand(net.minecraft.world.InteractionHand.MAIN_HAND, new net.minecraft.world.item.ItemStack(net.minecraft.world.item.Items.FLINT_AND_STEEL));
        h.getLevel().setBlockAndUpdate(pos, Blocks.TORCH.defaultBlockState().setValue(BlockStateProperties.LIT, false));
        var hit = new net.minecraft.world.phys.BlockHitResult(net.minecraft.world.phys.Vec3.atCenterOf(pos), net.minecraft.core.Direction.UP, pos, false);
        var relight = new net.neoforged.neoforge.event.entity.player.PlayerInteractEvent.RightClickBlock(player, net.minecraft.world.InteractionHand.MAIN_HAND, pos, hit);
        com.fantasyfeudalism.items.lighting.LightInteractions.interact(relight);
        h.assertTrue(h.getLevel().getBlockState(pos).getValue(BlockStateProperties.LIT), "Flint and steel relights torches");
        h.getLevel().setBlockAndUpdate(pos, Blocks.LANTERN.defaultBlockState());
        player.setShiftKeyDown(true);
        var toggle = new net.neoforged.neoforge.event.entity.player.PlayerInteractEvent.RightClickBlock(player, net.minecraft.world.InteractionHand.MAIN_HAND, pos, hit);
        com.fantasyfeudalism.items.lighting.LightInteractions.interact(toggle);
        h.assertFalse(h.getLevel().getBlockState(pos).getValue(BlockStateProperties.LIT), "Shift-right-click switches lantern off");
        var toggleOn = new net.neoforged.neoforge.event.entity.player.PlayerInteractEvent.RightClickBlock(player, net.minecraft.world.InteractionHand.MAIN_HAND, pos, hit);
        com.fantasyfeudalism.items.lighting.LightInteractions.interact(toggleOn);
        h.assertTrue(h.getLevel().getBlockState(pos).getValue(BlockStateProperties.LIT), "Shift-right-click switches lantern on");
        h.succeed();
    }

    @GameTest(template="empty")
    public static void lantern_subclasses_keep_their_own_state_definitions(GameTestHelper h){
        var plain=LanternCompatibilityBlocks.plain.defaultBlockState();
        h.assertFalse(plain.hasProperty(BlockStateProperties.LIT),"A plain subclass is not forced to add vanilla fuel state");
        for(var block:new LanternBlock[]{LanternCompatibilityBlocks.lightable,LanternCompatibilityBlocks.copper}){
            var state=block.defaultBlockState();
            h.assertTrue(state.hasProperty(BlockStateProperties.LIT),"Lightable subclasses define their lit property once");
            h.assertFalse(state.getValue(BlockStateProperties.LIT),"Subclass defaults remain under the other mod's control");
            h.assertTrue(state.setValue(BlockStateProperties.LIT,true).getValue(BlockStateProperties.LIT),"Subclass lanterns still switch on");
            h.assertFalse(WeatherLighting.supported(block),"Vanilla fuel/weather policy does not take over other mods' lanterns");
        }
        h.assertTrue(LanternCompatibilityBlocks.copper.defaultBlockState().getValue(BlockStateProperties.HORIZONTAL_AXIS)==net.minecraft.core.Direction.Axis.Z,"Copper-style subclass preserves its axis property");
        if(net.neoforged.fml.ModList.get().isLoaded("suppsquared")){
            for(String name:new String[]{"copper_lantern","brass_lantern"}){
                var id=net.minecraft.resources.ResourceLocation.fromNamespaceAndPath("suppsquared",name);
                var block=net.minecraft.core.registries.BuiltInRegistries.BLOCK.getOptional(id).orElseThrow();
                var state=block.defaultBlockState();
                h.assertTrue(block instanceof LanternBlock&&state.getValue(BlockStateProperties.LIT),"Actual "+id+" registers lit");
                h.assertTrue(state.getLightEmission(h.getLevel(),h.absolutePos(BlockPos.ZERO))>0,"Actual "+id+" emits its own light");
                h.assertTrue(state.setValue(BlockStateProperties.LIT,false).getLightEmission(h.getLevel(),h.absolutePos(BlockPos.ZERO))==0,"Actual "+id+" retains its own off state");
                h.assertFalse(WeatherLighting.supported(block),"Items does not replace "+id+" fuel/weather behavior");
            }
            var crimson=net.minecraft.core.registries.BuiltInRegistries.BLOCK.getOptional(net.minecraft.resources.ResourceLocation.fromNamespaceAndPath("suppsquared","crimson_lantern")).orElseThrow();
            h.assertFalse(crimson.defaultBlockState().hasProperty(BlockStateProperties.LIT),"Actual crimson lantern retains its native state without lit");
            h.assertTrue(crimson.defaultBlockState().hasProperty(BlockStateProperties.ATTACHED),"Actual crimson lantern retains its connection state");
            h.assertTrue(crimson.defaultBlockState().getLightEmission(h.getLevel(),h.absolutePos(BlockPos.ZERO))>0,"Actual crimson lantern retains its light emission");
        }
        h.succeed();
    }

}
