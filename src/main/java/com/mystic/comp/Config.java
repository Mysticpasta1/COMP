package com.mystic.comp;

import net.minecraftforge.common.ForgeConfigSpec;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.config.ModConfig;
import org.apache.commons.lang3.tuple.Pair;

import java.util.List;

public class Config {
    public static final Config COMMON;
    public static final ForgeConfigSpec COMMON_SPEC;

    public final ForgeConfigSpec.ConfigValue<List<? extends String>> flowerWhitelist;

    static {
        Pair<Config, ForgeConfigSpec> specPair = new ForgeConfigSpec.Builder()
                .comment("Common configuration for Atlantis")
                .configure(Config::new);
        COMMON = specPair.getLeft();
        COMMON_SPEC = specPair.getRight();
    }

    private Config(ForgeConfigSpec.Builder builder) {
        builder.push("flowers");
        flowerWhitelist = builder
                .comment("List of flower block registry names that Overworld Mirror will accept.")
                .defineList("flowerWhitelist",
                        List.of(
                                "minecraft:dandelion"
                        ),
                        entry -> entry instanceof String
                );
        builder.pop();
    }
}

