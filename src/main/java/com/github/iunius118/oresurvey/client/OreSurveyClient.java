package com.github.iunius118.oresurvey.client;

import com.github.iunius118.oresurvey.OreSurvey;
import com.github.iunius118.oresurvey.common.OreSurveyConfig;
import com.github.iunius118.oresurvey.common.OreSurveyor;
import com.mojang.blaze3d.platform.InputConstants;
import net.minecraft.ChatFormatting;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraftforge.client.event.RegisterKeyMappingsEvent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.ForgeRegistries;

import java.io.BufferedWriter;
import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.function.Consumer;

public class OreSurveyClient {
    private static OreSurveyor oreSurveyor;

    public OreSurveyClient(IEventBus modEventBus) {
        bindKeys(modEventBus);
        modEventBus.addListener(OreSurveyConfig.Client::onLoad);
    }

    public void bindKeys(IEventBus modEventBus) {
        KeyMapping keySurvey = createKeyBinding("survey", InputConstants.KEY_LBRACKET, "main");
        KeyMapping keyResult = createKeyBinding("result", InputConstants.KEY_BACKSLASH, "main");

        // Register key bind event listener
        Consumer<RegisterKeyMappingsEvent> registerKeyMappingsListener = event -> {
            event.register(keySurvey);
            event.register(keyResult);
        };

        modEventBus.addListener(registerKeyMappingsListener);

        // Register key click event listener
        Consumer<TickEvent.ClientTickEvent> clientTickEventListener = event -> {
            Minecraft client = Minecraft.getInstance();

            while (keySurvey.consumeClick()) {
                if (oreSurveyor == null) {
                    oreSurveyor = createOreSurveyor();
                }

                surveyOres(oreSurveyor, client);
            }

            while (keyResult.consumeClick()) {
                if (oreSurveyor == null) {
                    oreSurveyor = createOreSurveyor();
                    surveyOres(oreSurveyor, client);
                }

                saveResult(oreSurveyor);
                oreSurveyor = null;
            }
        };

        MinecraftForge.EVENT_BUS.addListener(clientTickEventListener);
    }

    private KeyMapping createKeyBinding(String name, int key, String category) {
        return new KeyMapping("key." + OreSurvey.MOD_ID + "." + name, key, "key.categories." + OreSurvey.MOD_ID + "." + category);
    }

    private OreSurveyor createOreSurveyor() {
        if (OreSurveyConfig.client().useDefaultOreList()) {
            return OreSurveyor.ofDefault();
        }

        List<String> blockIds = OreSurveyConfig.client().customOreList();
        List<Block> blocks = blockIds.stream()
                .map(ResourceLocation::new)
                .map(ForgeRegistries.BLOCKS::getHolder)
                .filter(Optional::isPresent)
                .map(Optional::get)
                .map(Holder::value)
                .toList();
        return OreSurveyor.of(blocks);
    }

    private void surveyOres(OreSurveyor oreSurveyor, Minecraft client) {
        var player = client.player;
        if (player == null) {
            return;
        }

        BlockPos pos = player.getOnPos();

        try {
            surveyOres(oreSurveyor, player.level(), pos);
            client.player.displayClientMessage(createChatMessage(Component.literal("Survey ores: #" + oreSurveyor.getSurveyCount())), false);
        } catch (Exception e) {
            OreSurvey.LOGGER.warn("surveyOres:%n", e);
        }
    }

    private void surveyOres(OreSurveyor oreSurveyor, Level level, BlockPos pos) {
        oreSurveyor.surveyOres(level, pos);
    }

    private void saveResult(OreSurveyor oreSurveyor) {
        Minecraft client = Minecraft.getInstance();
        File gameDirectory = client.gameDirectory;
        String filename = getTimestamp() + ".tsv";

        var path = Paths.get(gameDirectory.getPath(), OreSurvey.MOD_ID, filename);

        try {
            // Create parent dirs
            Files.createDirectories(path.getParent());
        } catch (IOException e) {
            OreSurvey.LOGGER.warn("saveResult:%n", e);
        }

        try (BufferedWriter bufferedWriter = Files.newBufferedWriter(path, StandardCharsets.UTF_8);
             PrintWriter writer = new PrintWriter(bufferedWriter)) {
            printResult(writer, oreSurveyor);
            if (client.player != null)
                client.player.displayClientMessage(createChatMessage(Component.literal("Result saved as " + Paths.get(OreSurvey.MOD_ID, filename))), false);
        } catch (IOException e) {
            OreSurvey.LOGGER.warn("saveResult:%n", e);
        }
    }

    private String getTimestamp() {
        var timestamp = new Timestamp(System.currentTimeMillis());
        var dateFormat = new SimpleDateFormat("yyyyMMdd_HHmmss_SSS");
        return dateFormat.format(timestamp);
    }

    private void printResult(PrintWriter writer, OreSurveyor oreSurveyor) {
        OreSurveyor.SurveyResult result = oreSurveyor.getResult();

        // Write header record
        StringJoiner header = new StringJoiner("\t").add("Layer");

        for (Block block : result.ores()) {
            header.add(ForgeRegistries.BLOCKS.getKey(block).toString());
        }

        writer.println(header);

        // Write data records
        Set<Map.Entry<Integer, int[]>> entries = result.data().entrySet();

        for (Map.Entry<Integer, int[]> entry : entries) {
            StringJoiner data = new StringJoiner("\t");
            data.add(entry.getKey().toString());

            for (int value : entry.getValue()) {
                data.add(String.valueOf(value));
            }

            writer.println(data);
        }
    }

    private Component createChatMessage(MutableComponent message) {
        return Component.literal("").append(Component.literal("[OreSurvey] ").withStyle(ChatFormatting.YELLOW)).append(message.withStyle(ChatFormatting.RESET));
    }
}
