package com.mystic.comp;

import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.config.ModConfig;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;

@Mod(Comp.MODID)
public class Comp {

    public static final String MODID = "comp";

    public Comp(FMLJavaModLoadingContext context) {
        context.registerConfig(ModConfig.Type.COMMON, Config.COMMON_SPEC, MODID + "-common.toml");
    }
}

