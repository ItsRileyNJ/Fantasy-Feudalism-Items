package com.fantasyfeudalism.items.gametest;
import com.fantasyfeudalism.items.FeudalItems;
import net.minecraft.gametest.framework.*;
import net.neoforged.neoforge.gametest.GameTestHolder;
@GameTestHolder(FeudalItems.ID)
public final class TestReporting {
    @GameTestGenerator
    public static java.util.Collection<TestFunction> initialize(){
        String path=System.getProperty("fantasyfeudalism.testReport");
        if(path!=null)try{GlobalTestReporter.replaceWith(new JUnitLikeTestReporter(new java.io.File(path)));}catch(javax.xml.parsers.ParserConfigurationException exception){throw new IllegalStateException(exception);}
        return java.util.List.of();
    }
}
