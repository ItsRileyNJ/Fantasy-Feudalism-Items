package com.fantasyfeudalism.items.client;

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.List;
import java.util.function.BooleanSupplier;

/** Optional shader APIs are resolved once; no shader mod is required or linked. */
public final class ShaderCompatibility {
    private static final BooleanSupplier DETECTOR=discover(ShaderCompatibility.class.getClassLoader());
    private ShaderCompatibility() {}

    public static boolean shadersEnabled(){return DETECTOR.getAsBoolean();}

    static BooleanSupplier discover(ClassLoader loader){
        List<BooleanSupplier> probes=new ArrayList<>();
        addProbe(loader,probes,"net.irisshaders.iris.api.v0.IrisApi","isShaderPackInUse","getInstance");
        addProbe(loader,probes,"net.coderbot.iris.api.v0.IrisApi","isShaderPackInUse","getInstance");
        addProbe(loader,probes,"net.optifine.Config","isShaders",null);
        addProbe(loader,probes,"Config","isShaders",null);
        return ()->{for(var probe:probes)if(probe.getAsBoolean())return true;return false;};
    }
    private static void addProbe(ClassLoader loader,List<BooleanSupplier> probes,String name,String query,String factory){
        final Class<?> type;
        try{type=Class.forName(name,false,loader);}catch(ClassNotFoundException absent){
            if(name.endsWith(".api.v0.IrisApi")){
                try{
                    Class.forName(name.replace(".api.v0.IrisApi",".Iris"),false,loader);
                    probes.add(()->true);
                }catch(ClassNotFoundException noProvider){/* Shader provider is not installed. */}
                catch(LinkageError broken){probes.add(()->true);}
            }
            return;
        }
        catch(LinkageError broken){probes.add(()->true);return;}
        try{
            Method method=type.getMethod(query);
            Object instance=Modifier.isStatic(method.getModifiers())?null:type.getMethod(factory).invoke(null);
            probes.add(()->{
                try{return !Boolean.FALSE.equals(method.invoke(instance));}
                // An installed but broken shader API must never enable our lighting.
                catch(ReflectiveOperationException|RuntimeException|LinkageError failure){return true;}
            });
        }catch(ReflectiveOperationException|RuntimeException|LinkageError failure){probes.add(()->true);}
    }
}
