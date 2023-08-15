package com.github.iunius118.oresurvey.client;

import com.github.iunius118.oresurvey.common.OreSurveyor;
import com.mojang.blaze3d.platform.InputConstants;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;

import java.io.IOException;
import java.util.Map;
import java.util.Set;
import java.util.StringJoiner;

public class OreSurveyClient implements ClientModInitializer {
    public static final String MOD_ID = "oresurvey";

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
                System.out.println("Survey ores: #" + oreSurveyor.getSurveyCount());
            }

            while (keyResult.consumeClick()) {
                if (oreSurveyor != null) {
                    printResult(oreSurveyor);
                }

                oreSurveyor = OreSurveyor.ofDefault();
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

        try (var level = player.level()) {
            surveyOres(oreSurveyor, level, pos);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private void surveyOres(OreSurveyor oreSurveyor, Level level, BlockPos pos) {
        oreSurveyor.surveyOres(level, pos);
    }

    private void printResult(OreSurveyor oreSurveyor) {
        OreSurveyor.SurveyResult result = oreSurveyor.getResult();

        StringJoiner oreIDs = new StringJoiner("\t", "\nlevel\t", "\n");

        for(Block block : result.ores()) {
            oreIDs.add(BuiltInRegistries.BLOCK.getKey(block).toString());
        }

        System.out.print(oreIDs);

        Set<Map.Entry<Integer, int[]>> entries = result.data().entrySet();

        for(Map.Entry<Integer, int[]> entry : entries) {
            StringJoiner data = new StringJoiner("\t", "", "\n");
            data.add(entry.getKey().toString());

            for(int value : entry.getValue()) {
                data.add(String.valueOf(value));
            }

            System.out.print(data);
        }
    }
}
