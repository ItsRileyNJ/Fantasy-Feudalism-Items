package com.fantasyfeudalism.items.gametest;

import com.fantasyfeudalism.items.FeudalItems;
import com.fantasyfeudalism.items.clairvoyance.*;
import com.mojang.authlib.GameProfile;
import io.netty.buffer.Unpooled;
import java.util.*;
import net.minecraft.core.BlockPos;
import net.minecraft.core.component.DataComponents;
import net.minecraft.gametest.framework.*;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.*;
import net.minecraft.server.players.ServerOpListEntry;
import net.minecraft.world.effect.*;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.projectile.*;
import net.minecraft.world.item.*;
import net.minecraft.world.item.alchemy.*;
import net.minecraft.world.item.crafting.*;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.phys.*;
import net.neoforged.neoforge.gametest.*;

@GameTestHolder(FeudalItems.ID)
@PrefixGameTestTemplate(false)
public final class ClairvoyanceTests {
    private static class TestPlayer extends ServerPlayer {
        boolean creative;
        TestPlayer(ServerLevel level) {
            super(level.getServer(), level, new GameProfile(UUID.randomUUID(), "clair-test"), ClientInformation.createDefault());
            var connection = new net.minecraft.network.Connection(net.minecraft.network.protocol.PacketFlow.SERVERBOUND);
            new io.netty.channel.embedded.EmbeddedChannel(connection);
            new net.minecraft.server.network.ServerGamePacketListenerImpl(level.getServer(), connection, this,
                new net.minecraft.server.network.CommonListenerCookie(getGameProfile(),0,ClientInformation.createDefault(),false,net.neoforged.neoforge.network.connection.ConnectionType.NEOFORGE));
            net.neoforged.neoforge.network.registration.NetworkRegistry.configureMockConnection(connection);
        }
        @Override public boolean isCreative() { return creative; }
        @Override public boolean isSpectator() { return false; }
    }
    private static TestPlayer viewer(GameTestHelper h) {
        var p = new TestPlayer(h.getLevel());
        p.setPos(Vec3.atCenterOf(h.absolutePos(new BlockPos(1, 2, 1))));
        return p;
    }
    private static void assertPotion(GameTestHelper h, ItemStack stack, Item type) {
        h.assertTrue(stack.is(type), "Expected potion container " + type);
        h.assertTrue(stack.getOrDefault(DataComponents.POTION_CONTENTS, PotionContents.EMPTY).is(FeudalItems.CLAIRVOYANCE_POTION), "Expected Clairvoyance contents");
    }

    @GameTest(template = "empty")
    public static void brewing_and_tipped_arrow_recipe(GameTestHelper h) {
        var brewing = h.getLevel().getServer().potionBrewing();
        var water = PotionContents.createItemStack(Items.POTION, Potions.WATER);
        h.assertTrue(brewing.hasMix(water, new ItemStack(Items.GLOWSTONE_DUST)), "Glowstone must brew the first stage from water");
        var thick = brewing.mix(new ItemStack(Items.GLOWSTONE_DUST), water);
        h.assertTrue(thick.getOrDefault(DataComponents.POTION_CONTENTS, PotionContents.EMPTY).is(Potions.THICK), "First stage is a vanilla Thick Potion");
        h.assertTrue(brewing.hasMix(thick, new ItemStack(Items.AMETHYST_SHARD)), "Amethyst must complete the Thick Potion");
        h.assertFalse(brewing.hasMix(water, new ItemStack(Items.AMETHYST_SHARD)), "Amethyst cannot skip the glowstone stage");
        var awkward = PotionContents.createItemStack(Items.POTION, Potions.AWKWARD);
        h.assertFalse(brewing.hasMix(awkward, new ItemStack(Items.AMETHYST_SHARD)), "Old Awkward Potion recipe is removed");
        var drink = brewing.mix(new ItemStack(Items.AMETHYST_SHARD), thick);
        assertPotion(h, drink, Items.POTION);
        var splash = brewing.mix(new ItemStack(Items.GUNPOWDER), drink);
        assertPotion(h, splash, Items.SPLASH_POTION);
        var lingering = brewing.mix(new ItemStack(Items.DRAGON_BREATH), splash);
        assertPotion(h, lingering, Items.LINGERING_POTION);
        h.assertFalse(brewing.hasMix(drink, new ItemStack(Items.REDSTONE)), "No extended variant");
        h.assertFalse(brewing.hasMix(drink, new ItemStack(Items.GLOWSTONE_DUST)), "No stronger variant");
        var ingredients = new ArrayList<ItemStack>();
        for (int i = 0; i < 9; i++) ingredients.add(i == 4 ? lingering : new ItemStack(Items.ARROW));
        var input = CraftingInput.of(3, 3, ingredients);
        var recipe = new TippedArrowRecipe(CraftingBookCategory.MISC);
        h.assertTrue(recipe.matches(input, h.getLevel()), "Vanilla tipped-arrow recipe accepts Clairvoyance");
        var arrows = recipe.assemble(input, h.getLevel().registryAccess());
        assertPotion(h, arrows, Items.TIPPED_ARROW);
        h.assertTrue(arrows.getCount() == 8, "Recipe produces eight arrows");
        h.succeed();
    }

    @GameTest(template = "empty")
    public static void drinking_duration_and_milk(GameTestHelper h) {
        var player = viewer(h);
        int old = ClairvoyanceConfig.DURATION_SECONDS.get();
        try {
            ClairvoyanceConfig.DURATION_SECONDS.set(77);
            var potion = FeudalItems.potionStack(Items.POTION);
            Items.POTION.finishUsingItem(potion, h.getLevel(), player);
            var effect = player.getEffect(FeudalItems.CLAIRVOYANCE);
            h.assertTrue(effect != null && effect.getDuration() == 1540 && effect.getAmplifier() == 0, "Drinking honors configurable duration and single strength");
            h.assertFalse(player.hasEffect(MobEffects.GLOWING), "Clairvoyance never applies shared Glowing");
            Items.MILK_BUCKET.finishUsingItem(new ItemStack(Items.MILK_BUCKET), h.getLevel(), player);
            h.assertFalse(player.hasEffect(FeudalItems.CLAIRVOYANCE), "Milk removes Clairvoyance");
            h.assertTrue(ClairvoyanceTracker.targets(player).isEmpty(), "No highlights without effect");
        } finally { ClairvoyanceConfig.DURATION_SECONDS.set(old); }
        h.succeed();
    }

    @GameTest(template = "empty")
    public static void range_walls_invisibility_and_viewer_privacy(GameTestHelper h) {
        var player = viewer(h);
        var observer = viewer(h);
        var sheep = h.spawnWithNoFreeWill(EntityType.SHEEP, 3, 2, 1);
        var zombie = h.spawnWithNoFreeWill(EntityType.ZOMBIE, 4, 2, 1);
        zombie.setInvisible(true);
        zombie.setNoAi(true);
        h.setBlock(new BlockPos(2, 2, 1), Blocks.STONE);
        h.setBlock(new BlockPos(2, 3, 1), Blocks.STONE);
        player.addEffect(new MobEffectInstance(FeudalItems.CLAIRVOYANCE, 2400));
        var targets = ClairvoyanceTracker.targets(player);
        h.assertTrue(targets.contains(sheep.getUUID()), "Passive mobs are revealed through walls");
        h.assertTrue(targets.contains(zombie.getUUID()), "Invisible hostile mobs are revealed");
        h.assertTrue(ClairvoyanceTracker.targets(observer).isEmpty(), "An unaffected observer receives no targets");
        h.assertFalse(sheep.isCurrentlyGlowing() || zombie.isCurrentlyGlowing(), "Target metadata stays private");
        sheep.setPos(player.getX() + 24, player.getY(), player.getZ());
        h.assertTrue(ClairvoyanceTracker.eligible(player, sheep, 24), "Radius boundary included");
        sheep.setPos(player.getX() + 24.01, player.getY(), player.getZ());
        h.assertFalse(ClairvoyanceTracker.eligible(player, sheep, 24), "Outside radius excluded");
        sheep.setPos(player.getX() + 6, player.getY(), player.getZ());
        int old = ClairvoyanceConfig.RADIUS.get();
        try {
            ClairvoyanceConfig.RADIUS.set(5);
            h.assertFalse(ClairvoyanceTracker.targets(player).contains(sheep.getUUID()), "Configured radius is used");
        } finally { ClairvoyanceConfig.RADIUS.set(old); }
        h.succeed();
    }

    @GameTest(template = "empty")
    public static void boss_creative_and_operator_exclusions(GameTestHelper h) {
        var player = viewer(h);
        var target = viewer(h);
        h.assertTrue(ClairvoyanceTracker.eligible(player, target, 24), "Survival players can be detected");
        h.assertFalse(ClairvoyanceTracker.eligible(player, player, 24), "Viewer does not outline themselves");
        target.creative = true;
        h.assertFalse(ClairvoyanceTracker.eligible(player, target, 24), "Creative players excluded");
        target.creative = false;
        var ops = h.getLevel().getServer().getPlayerList().getOps();
        try {
            ops.add(new ServerOpListEntry(target.getGameProfile(), 4, false));
            h.assertFalse(ClairvoyanceTracker.eligible(player, target, 24), "Operators excluded");
        } finally { ops.remove(target.getGameProfile()); }
        var wither = EntityType.WITHER.create(h.getLevel());
        var dragon = EntityType.ENDER_DRAGON.create(h.getLevel());
        wither.setPos(player.position()); dragon.setPos(player.position());
        h.assertFalse(ClairvoyanceTracker.eligible(player, wither, 24), "Wither excluded");
        h.assertFalse(ClairvoyanceTracker.eligible(player, dragon, 24), "Dragon excluded");
        var stand = EntityType.ARMOR_STAND.create(h.getLevel());
        stand.setPos(player.position());
        h.assertFalse(ClairvoyanceTracker.eligible(player, stand, 24), "Decorative living entities are not mobs");
        h.succeed();
    }

    private static class TestSplash extends ThrownPotion {
        TestSplash(ServerLevel level) { super(level, 0, 0, 0); }
        void impact(Entity target) { onHit(new EntityHitResult(target)); }
    }
    @GameTest(template = "empty")
    public static void splash_applies_effect(GameTestHelper h) {
        var sheep = h.spawnWithNoFreeWill(EntityType.SHEEP, 3, 2, 3);
        var splash = new TestSplash(h.getLevel());
        splash.setPos(sheep.position());
        splash.setItem(FeudalItems.potionStack(Items.SPLASH_POTION));
        splash.impact(sheep);
        var effect = sheep.getEffect(FeudalItems.CLAIRVOYANCE);
        h.assertTrue(effect != null && effect.getDuration() == ClairvoyanceConfig.durationTicks(), "Direct splash grants configured base duration");
        h.succeed();
    }

    @GameTest(template = "empty", timeoutTicks = 40)
    public static void lingering_cloud_applies_effect(GameTestHelper h) {
        var sheep = h.spawnWithNoFreeWill(EntityType.SHEEP, 3, 2, 3);
        var cloud = new AreaEffectCloud(h.getLevel(), sheep.getX(), sheep.getY(), sheep.getZ());
        cloud.setPotionContents(new PotionContents(FeudalItems.CLAIRVOYANCE_POTION));
        cloud.setWaitTime(0);
        cloud.setRadius(3);
        h.getLevel().addFreshEntity(cloud);
        h.succeedWhen(() -> {
            var effect = sheep.getEffect(FeudalItems.CLAIRVOYANCE);
            h.assertTrue(effect != null && effect.getDuration() > 0 && effect.getDuration() <= ClairvoyanceConfig.durationTicks() / 4,
                "Lingering cloud grants vanilla quarter-duration effect");
        });
    }

    private static class TestArrow extends Arrow {
        TestArrow(ServerLevel level) { super(level, 0, 0, 0, FeudalItems.potionStack(Items.TIPPED_ARROW), null); }
        void apply(LivingEntity target) { doPostHurtEffects(target); }
    }
    @GameTest(template = "empty")
    public static void tipped_arrow_applies_effect(GameTestHelper h) {
        var sheep = h.spawnWithNoFreeWill(EntityType.SHEEP, 3, 2, 3);
        new TestArrow(h.getLevel()).apply(sheep);
        var effect = sheep.getEffect(FeudalItems.CLAIRVOYANCE);
        h.assertTrue(effect != null && effect.getDuration() == ClairvoyanceConfig.durationTicks() / 8, "Tipped arrows grant vanilla eighth-duration effect");
        h.succeed();
    }

    @GameTest(template = "empty")
    public static void private_payload_roundtrip_and_cache_cleanup(GameTestHelper h) {
        var dimension = ResourceLocation.withDefaultNamespace("overworld");
        var otherDimension = ResourceLocation.withDefaultNamespace("the_nether");
        var id = UUID.randomUUID();
        var payload = new ClairvoyancePayload(dimension, List.of(id));
        var buffer = new RegistryFriendlyByteBuf(Unpooled.buffer(), h.getLevel().registryAccess());
        try {
            ClairvoyancePayload.CODEC.encode(buffer, payload);
            h.assertTrue(payload.equals(ClairvoyancePayload.CODEC.decode(buffer)), "Payload roundtrips without changing targets");
        } finally { buffer.release(); }
        var view = new ClairvoyanceView();
        Object world = new Object();
        view.receive(payload, world, dimension, 100);
        h.assertTrue(view.highlights(id, world, dimension, 100), "Affected viewer sees target");
        h.assertFalse(view.highlights(UUID.randomUUID(), world, dimension, 100), "Unlisted entities remain unhighlighted");
        h.assertFalse(view.highlights(id, new Object(), dimension, 100), "Reconnect clears old-world targets");
        h.assertFalse(view.highlights(id, world, otherDimension, 100), "Dimension changes invalidate targets");
        h.assertFalse(view.highlights(id, world, dimension, 120), "Stale packets expire");
        view.receive(new ClairvoyancePayload(dimension, List.of()), world, dimension, 101);
        h.assertFalse(view.highlights(id, world, dimension, 101), "Empty update clears highlights");
        view.receive(new ClairvoyancePayload(otherDimension, List.of(id)), world, dimension, 102);
        h.assertFalse(view.highlights(id, world, dimension, 102), "Packets from old dimensions are ignored");
        h.succeed();
    }
}
