package com.github.iunius118.oresurvey.common;

import com.google.common.collect.ImmutableList;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.phys.AABB;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class OreSurveyor {
    private final static List<Block> DEFAULT_TARGETS = new ImmutableList.Builder<Block>().add(
            Blocks.STONE,
            Blocks.COAL_ORE,
            Blocks.COPPER_ORE,
            Blocks.LAPIS_ORE,
            Blocks.IRON_ORE,
            Blocks.GOLD_ORE,
            Blocks.REDSTONE_ORE,
            Blocks.DIAMOND_ORE,
            Blocks.EMERALD_ORE,
            Blocks.DEEPSLATE,
            Blocks.DEEPSLATE_COAL_ORE,
            Blocks.DEEPSLATE_COPPER_ORE,
            Blocks.DEEPSLATE_LAPIS_ORE,
            Blocks.DEEPSLATE_IRON_ORE,
            Blocks.DEEPSLATE_GOLD_ORE,
            Blocks.DEEPSLATE_REDSTONE_ORE,
            Blocks.DEEPSLATE_DIAMOND_ORE,
            Blocks.DEEPSLATE_EMERALD_ORE,
            Blocks.BUDDING_AMETHYST,
            Blocks.NETHERRACK,
            Blocks.NETHER_QUARTZ_ORE,
            Blocks.NETHER_GOLD_ORE,
            Blocks.ANCIENT_DEBRIS
    ).build();

    private final List<Block> targets;
    private int sizeX = 128;
    private int sizeZ = 128;
    private int altitudeLow = -63;
    private int altitudeHigh = 240;
    private Map<Integer, int[]> results = new LinkedHashMap<>();
    private int countSurvey = 0;

    public static OreSurveyor of (List<Block> targetList, int x, int z, int y1, int y2){
        var oreSurveyor = new OreSurveyor(targetList);
        oreSurveyor.sizeX = x;
        oreSurveyor.sizeZ = z;
        oreSurveyor.altitudeLow = Math.min(y1, y2);
        oreSurveyor.altitudeHigh = Math.max(y1, y2);
        oreSurveyor.initResultMap();
        return oreSurveyor;
    }

    public static OreSurveyor ofDefault (){
        var oreSurveyor = new OreSurveyor(DEFAULT_TARGETS);
        oreSurveyor.initResultMap();
        return oreSurveyor;
    }

    private OreSurveyor(List<Block> targetList) {
        targets = targetList;
    }

    private void initResultMap() {
        for(int h = altitudeLow; h <= altitudeHigh; h++) {
            results.put(h, new int[targets.size()]);
        }
    }

    public OreSurveyor surveyOres(Level level, BlockPos pos) {
        for(int h = altitudeLow; h <= altitudeHigh; h++) {
            int[] oreCounts = results.get(h);
            var aabb = new AABB(
                    pos.getX() - (sizeX >> 1), h, pos.getZ() - (sizeZ >> 1),
                    pos.getX() + sizeX - (sizeX >> 1) - 1, h, pos.getZ() + sizeZ - (sizeZ >> 1) - 1);
            level.getBlockStates(aabb).filter(s -> targets.contains(s.getBlock())).forEachOrdered(s -> {
                var block = s.getBlock();
                int i = targets.indexOf(block);

                if (i >= 0) {
                    oreCounts[i] += 1;
                }
            });
        }

        countSurvey++;
        return this;
    }

    public int getSurveyCount() {
        return countSurvey;
    }

    public SurveyResult getResult() {
        return new SurveyResult(targets.toArray(new Block[0]), results);
    }

    public record SurveyResult(Block[] ores, Map<Integer, int[]> data){ }
}
