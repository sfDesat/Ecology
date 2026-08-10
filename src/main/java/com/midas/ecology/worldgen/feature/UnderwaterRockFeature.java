package com.midas.ecology.worldgen.feature;

import com.midas.ecology.worldgen.seafloor.RockMoundPlacer;
import com.midas.ecology.worldgen.seafloor.SeafloorHelpers;
import com.mojang.serialization.Codec;
import net.minecraft.core.BlockPos;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.minecraft.world.level.levelgen.feature.FeaturePlaceContext;

/**
 * Places a tapered 3D rock mound on the ocean floor, replacing water above the substrate.
 * Height is clamped so the stack stays fully underwater.
 */
public class UnderwaterRockFeature extends Feature<UnderwaterRockConfiguration> {
	public UnderwaterRockFeature(Codec<UnderwaterRockConfiguration> codec) {
		super(codec);
	}

	@Override
	public boolean place(FeaturePlaceContext<UnderwaterRockConfiguration> context) {
		WorldGenLevel level = context.level();
		RandomSource random = context.random();
		UnderwaterRockConfiguration config = context.config();
		BlockPos.MutableBlockPos floor = context.origin().mutable();

		if (!SeafloorHelpers.snapToSoftFloor(level, floor) || !SeafloorHelpers.hasWaterAbove(level, floor)) {
			return false;
		}

		int waterDepth = SeafloorHelpers.countWaterAbove(level, floor);
		int maxHeight = waterDepth - RockMoundPlacer.SURFACE_CLEARANCE;
		if (maxHeight < 1) {
			return false;
		}

		int radius = config.radius().sample(random);
		int height = Math.min(config.height().sample(random), maxHeight);
		BlockState rock = config.state();
		return RockMoundPlacer.place(level, floor, radius, height, rock, random);
	}
}
