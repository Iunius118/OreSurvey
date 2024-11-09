package com.github.iunius118.oresurvey.common;

import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.common.ForgeConfigSpec;
import net.minecraftforge.fml.event.config.ModConfigEvent;

import java.util.List;

public class OreSurveyConfig {
    private static Client client = new Client(true, List.of());

    public static Client client() {
        return client;
    }

    public record Client(boolean useDefaultOreList, List<String> customOreList) {
        private static final ForgeConfigSpec.Builder BUILDER = new ForgeConfigSpec.Builder();

        private static final ForgeConfigSpec.BooleanValue USE_DEFAULT_ORE_LIST = BUILDER
                .define("useDefaultOreList", true);

        private static final ForgeConfigSpec.ConfigValue<List<? extends String>> CUSTOM_ORE_LIST = BUILDER
                .define("customOreList",
                        List.of(
                                "minecraft:stone",
                                "minecraft:coal_ore",
                                "minecraft:copper_ore",
                                "minecraft:lapis_ore",
                                "minecraft:iron_ore",
                                "minecraft:gold_ore",
                                "minecraft:redstone_ore",
                                "minecraft:diamond_ore",
                                "minecraft:emerald_ore",
                                "minecraft:deepslate",
                                "minecraft:deepslate_coal_ore",
                                "minecraft:deepslate_copper_ore",
                                "minecraft:deepslate_lapis_ore",
                                "minecraft:deepslate_iron_ore",
                                "minecraft:deepslate_gold_ore",
                                "minecraft:deepslate_redstone_ore",
                                "minecraft:deepslate_diamond_ore",
                                "minecraft:deepslate_emerald_ore",
                                "minecraft:budding_amethyst",
                                "minecraft:netherrack",
                                "minecraft:nether_quartz_ore",
                                "minecraft:nether_gold_ore",
                                "minecraft:ancient_debris"),
                        Client::validateOreIds);

        public static final ForgeConfigSpec SPEC = BUILDER.build();

        private static boolean validateOreIds(final Object obj) {
            if (obj instanceof List<?> list) {
                return list.stream().allMatch(o -> o instanceof final String id && ResourceLocation.isValidResourceLocation(id));
            }

            return false;
        }

        public static void onLoad(final ModConfigEvent event) {
            boolean useDefaultOreList = USE_DEFAULT_ORE_LIST.get();
            List<String> customOreList = CUSTOM_ORE_LIST.get().stream().map(String.class::cast).toList();
            OreSurveyConfig.client = new Client(useDefaultOreList, customOreList);
        }
    }
}
