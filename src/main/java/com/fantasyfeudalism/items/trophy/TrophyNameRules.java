package com.fantasyfeudalism.items.trophy;

import com.fantasyfeudalism.items.content.ModAttachments;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;

/** The Player Trophy's nametag drawback cannot be reduced by crouching. */
public final class TrophyNameRules {
    private TrophyNameRules() {}
    public static boolean applies(Entity entity){
        return entity instanceof Player&&entity.getData(ModAttachments.TROPHY)==TrophyKind.PLAYER.ordinal();
    }
    public static boolean discrete(Entity entity,boolean vanillaDiscrete){return vanillaDiscrete&&!applies(entity);}
    public static float distanceMultiplier(Entity entity){return applies(entity)?1.5f:1.0f;}
}
