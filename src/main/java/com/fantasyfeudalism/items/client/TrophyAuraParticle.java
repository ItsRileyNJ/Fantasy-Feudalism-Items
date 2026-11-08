package com.fantasyfeudalism.items.client;

import com.fantasyfeudalism.items.trophy.TrophyParticles;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.*;
import net.minecraft.core.particles.ColorParticleOption;

public final class TrophyAuraParticle extends TextureSheetParticle {
    private final SpriteSet sprites;
    private final boolean active;
    private final double phase;
    private TrophyAuraParticle(ColorParticleOption options,ClientLevel level,double x,double y,double z,double vx,double vy,double vz,SpriteSet sprites,boolean active){
        super(level,x,y,z);
        this.sprites=sprites;this.active=active;this.phase=random.nextDouble()*Math.PI*2;
        var profile=active?TrophyParticles.ACTIVE_PROFILE:TrophyParticles.IDLE_PROFILE;
        xd=vx;yd=TrophyParticles.upwardVelocity(profile,vy);zd=vz;
        gravity=0;friction=.99f;hasPhysics=false;
        lifetime=profile.lifetime()+random.nextInt(10);quadSize=profile.size();
        setColor(options.getRed(),options.getGreen(),options.getBlue());
        setAlpha(active?.9f:.65f);setSpriteFromAge(sprites);
    }
    @Override public void tick(){
        super.tick();
        if(active){xd+=Math.sin(phase+age*.35)*.0015;zd+=Math.cos(phase+age*.35)*.0015;}
        alpha=(active?.9f:.65f)*(1f-(float)age/lifetime);
        setSpriteFromAge(sprites);
    }
    @Override public ParticleRenderType getRenderType(){return ParticleRenderType.PARTICLE_SHEET_TRANSLUCENT;}
    @Override protected int getLightColor(float partialTick){return 0xF000F0;}
    public static ParticleProvider<ColorParticleOption> provider(SpriteSet sprites,boolean active){
        return (options,level,x,y,z,vx,vy,vz)->new TrophyAuraParticle(options,level,x,y,z,vx,vy,vz,sprites,active);
    }
}
