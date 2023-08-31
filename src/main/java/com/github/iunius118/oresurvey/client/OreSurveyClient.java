package com.github.iunius118.oresurvey.client;

import com.github.iunius118.oresurvey.common.OreSurveyor;
import com.mojang.blaze3d.platform.InputConstants;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.minecraft.ChatFormatting;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedWriter;
import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.Map;
import java.util.Set;
import java.util.StringJoiner;

public class OreSurveyClient implements ClientModInitializer {
    public static final String MOD_ID = "oresurvey";
    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

    private static OreSurveyor oreSurveyor;

    @Override
    public void onInitializeClient() {
        bindKeys();
    }

    private void bindKeys() {
        KeyMapping keySurvey = KeyBindingHelper.registerKeyBinding(createKeyBinding("survey", InputConstants.KEY_LBRACKET, "main"));
        KeyMapping keyResult = KeyBindingHelper.registerKeyBinding(createKeyBinding("result", InputConstants.KEY_BACKSLASH, "main"));

        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            while (keySurvey.consumeClick()) {
                if (oreSurveyor == null) {
                    oreSurveyor = OreSurveyor.ofDefault();
                }

                surveyOres(oreSurveyor, client);
            }

            while (keyResult.consumeClick()) {
                if (oreSurveyor == null) {
                    oreSurveyor = OreSurveyor.ofDefault();
                    surveyOres(oreSurveyor, client);
                }

                saveResult(oreSurveyor);
                oreSurveyor = null;
            }
        });
    }

    private KeyMapping createKeyBinding(String name, int key, String category) {
        return new KeyMapping("key." + MOD_ID + "." + name, key, "key.categories." + MOD_ID + "." + category);
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
            LOGGER.warn("surveyOres:%n", e);
        }
    }

    private void surveyOres(OreSurveyor oreSurveyor, Level level, BlockPos pos) {
        oreSurveyor.surveyOres(level, pos);
    }

    private void  saveResult(OreSurveyor oreSurveyor) {
        Minecraft client = Minecraft.getInstance();
        File gameDirectory = client.gameDirectory;
        String filename = getTimestamp() + ".tsv";

        var path = Paths.get(gameDirectory.getPath(), MOD_ID, filename);

        try {
            // Create parent dirs
            Files.createDirectories(path.getParent());
        } catch (IOException e) {
            LOGGER.warn("saveResult:%n", e);
        }

        try (BufferedWriter bufferedWriter = Files.newBufferedWriter(path, StandardCharsets.UTF_8);
             PrintWriter writer = new PrintWriter(bufferedWriter)) {
            printResult(writer, oreSurveyor);
            if (client.player != null)
                client.player.displayClientMessage(createChatMessage(Component.literal("Result saved as " + Paths.get(MOD_ID, filename))), false);
        } catch (IOException e) {
            LOGGER.warn("saveResult:%n", e);
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

        for(Block block : result.ores()) {
            header.add(BuiltInRegistries.BLOCK.getKey(block).toString());
        }

        writer.println(header);

        // Write data records
        Set<Map.Entry<Integer, int[]>> entries = result.data().entrySet();

        for(Map.Entry<Integer, int[]> entry : entries) {
            StringJoiner data = new StringJoiner("\t");
            data.add(entry.getKey().toString());

            for(int value : entry.getValue()) {
                data.add(String.valueOf(value));
            }

            writer.println(data);
        }
    }

    private Component createChatMessage(MutableComponent message) {
        return Component.literal("").append(Component.literal("[OreSurvey] ").withStyle(ChatFormatting.YELLOW)).append(message.withStyle(ChatFormatting.RESET));
    }
}
