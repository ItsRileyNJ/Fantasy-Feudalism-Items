package com.fantasyfeudalism.items.trophy;

import java.util.*;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.tags.*;
import net.minecraft.world.effect.*;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.ai.attributes.*;
import net.minecraft.world.entity.monster.*;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.*;
import net.neoforged.neoforge.event.entity.living.*;
import net.neoforged.neoforge.event.entity.player.*;
import net.neoforged.neoforge.event.tick.PlayerTickEvent;

public final class TrophyEffects {
    private static final Map<Player,MobEffectInstance> OWN_VISION=new WeakHashMap<>();
    private static final ResourceLocation GOLD_ARMOR = ResourceLocation.fromNamespaceAndPath("fantasy_feudalism_items", "gold_trophy_armor");
    public static TrophyKind active(Player player) { return player instanceof ServerPlayer server ? TrophyLedger.get(server.serverLevel()).active(player.getUUID()) : null; }
    public static boolean gold(ItemStack item) {
        if (item.getItem() instanceof TieredItem tool) return tool.getTier() == Tiers.GOLD;
        if (item.getItem() instanceof ArmorItem armor) return armor.getMaterial().is(net.minecraft.world.item.ArmorMaterials.GOLD);
        return false;
    }
    public static void damage(LivingIncomingDamageEvent event) {
        var source = event.getSource(); var victim = event.getEntity(); float amount = event.getAmount();
        boolean projectile = source.is(DamageTypeTags.IS_PROJECTILE);
        if (source.getEntity() instanceof Player attacker) {
            var kind = active(attacker); boolean melee = source.getDirectEntity() == attacker && source.is(net.minecraft.world.damagesource.DamageTypes.PLAYER_ATTACK);
            if (kind == TrophyKind.SKELETON && projectile) amount *= 1.15f;
            if (kind == TrophyKind.CREEPER && melee) amount *= 0.8f;
            if (kind == TrophyKind.STRAY || kind == TrophyKind.BOGGED) amount *= 1.15f;
            if (kind == TrophyKind.PIGLIN && source.getWeaponItem()!=null && gold(source.getWeaponItem())) amount *= 1.15f;
            if (kind == TrophyKind.PLAYER && victim instanceof Player && victim != attacker) amount *= 1.15f;
            if (melee && kind == TrophyKind.WITHER_SKELETON && attacker.getRandom().nextFloat() < 0.20f) victim.addEffect(new MobEffectInstance(MobEffects.WITHER, 80, 0), attacker);
            if ((melee || projectile) && kind == TrophyKind.STRAY && !(victim instanceof AbstractSkeleton) && !victim.getType().is(EntityTypeTags.SKELETONS) && victim.getType() != EntityType.POLAR_BEAR)
                victim.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, 80, 0), attacker);
            if ((melee || projectile) && kind == TrophyKind.BOGGED && !victim.getType().is(EntityTypeTags.UNDEAD))
                victim.addEffect(new MobEffectInstance(MobEffects.POISON, 80, 0), attacker);
        }
        if (victim instanceof Player player) {
            var kind = active(player);

            if (kind == TrophyKind.SKELETON) amount *= 1.15f;
            if (kind == TrophyKind.CREEPER && source.is(DamageTypeTags.IS_EXPLOSION)) amount *= 0.6f;
            if (kind == TrophyKind.STRAY && source.is(DamageTypeTags.IS_FIRE)) amount *= 1.25f;
            if (kind == TrophyKind.BOGGED && (source.getEntity() instanceof EnderMan || source.getEntity() instanceof Endermite
                || source.getEntity() instanceof net.minecraft.world.entity.boss.enderdragon.EnderDragon
                || source.getDirectEntity() instanceof net.minecraft.world.entity.boss.enderdragon.EnderDragon
                || source.getDirectEntity() instanceof net.minecraft.world.entity.boss.enderdragon.EndCrystal
                || source.is(net.minecraft.world.damagesource.DamageTypes.DRAGON_BREATH))) amount *= 1.25f;
        }
        event.setAmount(amount);
    }
    public static void tick(PlayerTickEvent.Post event) {
        if (!(event.getEntity() instanceof ServerPlayer player) || player.tickCount % 20 != 0) return;
        var kind = active(player);
        int selected=kind==null?-1:kind.ordinal();if(player.getData(com.fantasyfeudalism.items.content.ModAttachments.TROPHY)!=selected)player.setData(com.fantasyfeudalism.items.content.ModAttachments.TROPHY,selected);
        changed(player);
        var armor = player.getAttribute(Attributes.ARMOR);
        int goldPieces = 0; if (kind == TrophyKind.PIGLIN) for (var item : player.getArmorSlots()) if (gold(item)) goldPieces++;
        if (armor != null) {
            var previous = armor.getModifier(GOLD_ARMOR);
            if (previous != null && previous.amount() != goldPieces) armor.removeModifier(GOLD_ARMOR);
            if (goldPieces > 0 && armor.getModifier(GOLD_ARMOR) == null) armor.addTransientModifier(new AttributeModifier(GOLD_ARMOR, goldPieces, AttributeModifier.Operation.ADD_VALUE));
        }
        if(kind==TrophyKind.WITHER_SKELETON){int elapsed=player.getPersistentData().getInt("ff_trophy_elapsed")+20;if(elapsed>=600){player.hurt(player.damageSources().magic(),1);elapsed=0;}player.getPersistentData().putInt("ff_trophy_elapsed",elapsed);}
        if (kind == TrophyKind.ENDER_DRAGON) {
            if(player.isInWaterOrRain())player.hurt(player.damageSources().magic(),1);
            for(var mob:player.serverLevel().getEntitiesOfClass(Mob.class,player.getBoundingBox().inflate(64),m->(m instanceof EnderMan||m instanceof Endermite)&&m.getTarget()==player&&m.getLastHurtByMob()!=player))mob.setTarget(null);
            // Do not overwrite a stronger or longer external Night Vision source.
            var vision = player.getEffect(MobEffects.NIGHT_VISION);
            if(vision==null){player.addEffect(new MobEffectInstance(MobEffects.NIGHT_VISION,240,0,true,false,true));OWN_VISION.put(player,player.getEffect(MobEffects.NIGHT_VISION));}
            else if(OWN_VISION.get(player)==vision){if(vision.getDuration()<=240)player.addEffect(new MobEffectInstance(MobEffects.NIGHT_VISION,240,0,true,false,true));else OWN_VISION.remove(player);}
            player.serverLevel().sendParticles(net.minecraft.core.particles.ParticleTypes.PORTAL, player.getX(), player.getY() + 1, player.getZ(), 1, 0.2, 0.3, 0.2, 0.01);
        }
    }
    public static void changed(Player player){
        var kind=active(player);String id=kind==null?"":kind.name();var data=player.getPersistentData();
        if(!data.getString("ff_trophy_timer_kind").equals(id)){data.putString("ff_trophy_timer_kind",id);data.putInt("ff_trophy_elapsed",0);}
        if(active(player)!=TrophyKind.ENDER_DRAGON){var owned=OWN_VISION.remove(player);if(owned!=null&&player.getEffect(MobEffects.NIGHT_VISION)==owned&&owned.getDuration()<=240)player.removeEffect(MobEffects.NIGHT_VISION);}
        if(active(player)!=TrophyKind.PIGLIN&&player.getAttribute(Attributes.ARMOR)!=null)player.getAttribute(Attributes.ARMOR).removeModifier(GOLD_ARMOR);
    }
    public static void resistance(LivingDamageEvent.Pre event){if(event.getEntity() instanceof Player player&&active(player)==TrophyKind.ZOMBIE&&!event.getSource().is(DamageTypeTags.BYPASSES_RESISTANCE))event.setNewDamage(event.getNewDamage()*0.8f);}
    public static void phantoms(PlayerSpawnPhantomsEvent event){
        if(active(event.getEntity())!=TrophyKind.ENDER_DRAGON||!(event.getEntity().level() instanceof net.minecraft.server.level.ServerLevel level))return;
        var pos=event.getEntity().blockPosition();boolean sky=!level.dimensionType().hasSkyLight()||(pos.getY()>=level.getSeaLevel()&&level.canSeeSky(pos));
        boolean difficulty=level.getCurrentDifficultyAt(pos).isHarderThan(event.getEntity().getRandom().nextFloat()*3);
        event.setResult(sky&&difficulty?PlayerSpawnPhantomsEvent.Result.ALLOW:PlayerSpawnPhantomsEvent.Result.DENY);
    }
    public static void xp(PlayerXpEvent.PickupXp event) { if (active(event.getEntity()) == TrophyKind.PIGLIN) { event.getOrb().discard(); event.setCanceled(true); } }
    public static void clone(PlayerEvent.Clone event) {
        if (!(event.getEntity() instanceof ServerPlayer player)) return;
        if (event.isWasDeath()) { TrophyLedger.get(player.serverLevel()).clear(player.getUUID()); player.setData(com.fantasyfeudalism.items.content.ModAttachments.TROPHY,-1); player.getPersistentData().remove("fantasy_feudalism_trophy"); }
        else if (event.getOriginal().getPersistentData().contains("fantasy_feudalism_trophy")) player.getPersistentData().putString("fantasy_feudalism_trophy", event.getOriginal().getPersistentData().getString("fantasy_feudalism_trophy"));
    }
    public static void target(LivingChangeTargetEvent event) {
        if (!(event.getNewAboutToBeSetTarget() instanceof Player player)) return;
        var kind = active(player); var mob = event.getEntity();
        if (kind == TrophyKind.ENDER_DRAGON && (mob instanceof EnderMan || mob instanceof Endermite) && mob.getLastHurtByMob() != player) event.setCanceled(true);

    }
}
