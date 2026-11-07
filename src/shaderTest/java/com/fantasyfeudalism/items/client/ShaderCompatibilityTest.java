package com.fantasyfeudalism.items.client;

import java.net.URLClassLoader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Comparator;
import javax.tools.ToolProvider;

/** Exercises the optional reflection boundary with isolated shader API fixtures. */
public final class ShaderCompatibilityTest {
    private static void check(boolean condition,String message){if(!condition)throw new AssertionError(message);}
    public static void main(String[] args)throws Exception{
        try(var absent=new URLClassLoader(new java.net.URL[0],ClassLoader.getPlatformClassLoader())){
            check(!ShaderCompatibility.discover(absent).getAsBoolean(),"No shader mod must leave lighting enabled");
        }
        test("net.irisshaders.iris.api.v0.IrisApi",false,false);
        test("net.coderbot.iris.api.v0.IrisApi",false,false);
        test("net.optifine.Config",true,false);
        test("Config",true,false);
        test("net.irisshaders.iris.api.v0.IrisApi",false,true);
        testMissingMethod();
        Path root=Files.createTempDirectory("shader-api-test");
        try(var loader=compile(root,"net.irisshaders.iris.Iris","")){
            check(ShaderCompatibility.discover(loader).getAsBoolean(),"Installed Iris without its expected API must fail closed");
        }finally{delete(root);}
        System.out.println("Shader detection tests passed (absent, inactive, active, live toggles, broken and incompatible APIs).");
    }
    private static URLClassLoader compile(Path root,String name,String body)throws Exception{
        int split=name.lastIndexOf('.');String pkg=split<0?"":name.substring(0,split);String simple=name.substring(split+1);
        Path source=root.resolve(name.replace('.','/')+".java");Files.createDirectories(source.getParent());
        Files.writeString(source,(pkg.isEmpty()?"":"package "+pkg+";\n")+"public class "+simple+" {"+body+"}");
        check(ToolProvider.getSystemJavaCompiler().run(null,null,null,"-d",root.toString(),source.toString())==0,"Fixture compilation failed");
        return new URLClassLoader(new java.net.URL[]{root.toUri().toURL()},ClassLoader.getPlatformClassLoader());
    }
    private static void test(String name,boolean isStatic,boolean broken)throws Exception{
        Path root=Files.createTempDirectory("shader-api-test");
        String simple=name.substring(name.lastIndexOf('.')+1);
        String factory=isStatic?"":"public static "+simple+" getInstance(){return new "+simple+"();}";
        String method=isStatic?"isShaders":"isShaderPackInUse";
        try(var loader=compile(root,name,"public static boolean active;"+factory+"public "+(isStatic?"static ":"")+"boolean "+method+"(){"+(broken?"throw new IllegalStateException();":"return active;")+"}")){
            var detector=ShaderCompatibility.discover(loader);var active=loader.loadClass(name).getField("active");
            check(detector.getAsBoolean()==broken,"Inactive pack or broken API result");
            active.setBoolean(null,true);check(detector.getAsBoolean(),"Enabling shaders must block dynamic light");
            active.setBoolean(null,false);check(detector.getAsBoolean()==broken,"Disabling shaders must restore lighting unless API is broken");
        }finally{delete(root);}
    }
    private static void testMissingMethod()throws Exception{
        Path root=Files.createTempDirectory("shader-api-test");
        try(var loader=compile(root,"net.irisshaders.iris.api.v0.IrisApi","")){
            check(ShaderCompatibility.discover(loader).getAsBoolean(),"Incompatible installed API must fail closed");
        }finally{delete(root);}
    }
    private static void delete(Path root)throws Exception{
        try(var paths=Files.walk(root)){for(var path:paths.sorted(Comparator.reverseOrder()).toList())Files.delete(path);}
    }
}
