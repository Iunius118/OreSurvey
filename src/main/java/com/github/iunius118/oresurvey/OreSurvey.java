package com.github.iunius118.oresurvey;

import com.github.iunius118.oresurvey.client.OreSurveyClient;
import com.github.iunius118.oresurvey.common.OreSurveyConfig;
import com.mojang.logging.LogUtils;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.config.ModConfig;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import org.slf4j.Logger;

@Mod(OreSurvey.MOD_ID)
public class OreSurvey {
    public static final String MOD_ID = "oresurvey";
    public static final Logger LOGGER = LogUtils.getLogger();
    public final OreSurveyClient client;


    public OreSurvey() {
        IEventBus modEventBus = FMLJavaModLoadingContext.get().getModEventBus();

        // Register config
        registerConfig();

        // Register event listeners
        client = new OreSurveyClient(modEventBus);
    }

    private void registerConfig() {
        ModLoadingContext.get().registerConfig(ModConfig.Type.CLIENT, OreSurveyConfig.Client.SPEC);
    }
}
