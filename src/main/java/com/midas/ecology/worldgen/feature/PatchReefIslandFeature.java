package com.midas.ecology.worldgen.feature;

import com.midas.ecology.worldgen.seafloor.HardCoralStructures;
import com.midas.ecology.worldgen.seafloor.SeafloorHelpers;
import com.mojang.serialization.Codec;
import net.minecraft.core.BlockPos;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.DoubleBlockHalf;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.minecraft.world.level.levelgen.feature.FeaturePlaceContext;

/**
 * Isolated patch-reef island: sand halo + custom hard-coral heads, a few fans, sparse seagrass.
 */
public class PatchReefIslandFeature extends Feature<PatchReefIslandConfiguration> {
	private static final HardCoralStructures.Profile PROFILE = HardCoralStructures.Profile.PATCH;

	public PatchReefIslandFeature(Codec<PatchReefIslandConfiguration> codec) {
		super(codec);
	}

	@Override
	public boolean place(FeaturePlaceContext<PatchReefIslandConfiguration> context) {
		WorldGenLevel level = context.level();
		RandomSource random = context.random();
		PatchReefIslandConfiguration config = context.config();
		BlockPos.MutableBlockPos floor = context.origin().mutable();

		if (!SeafloorHelpers.snapToSoftFloor(level, floor) || !SeafloorHelpers.hasWaterAbove(level, floor)) {
			return false;
		}

		int radius = config.radius().sample(random);
		HardCoralStructures.paintSandHalo(level, floor, radius + 3, random, PROFILE);

		int coralCount = config.coralCount().sample(random);
		boolean placed = false;

		for (int i = 0; i < coralCount; i++) {
			int dx = (int) Math.round(random.nextGaussian() * radius * 0.4);
			int dz = (int) Math.round(random.nextGaussian() * radius * 0.4);
			dx = Math.max(-radius, Math.min(radius, dx));
			dz = Math.max(-radius, Math.min(radius, dz));

			int localFloorY = HardCoralStructures.findLocalFloorY(
				level, floor.getX() + dx, floor.getY(), floor.getZ() + dz, PROFILE
			);
			if (localFloorY == Integer.MIN_VALUE) {
				continue;
			}

			BlockPos headFloor = new BlockPos(floor.getX() + dx, localFloorY, floor.getZ() + dz);
			if (HardCoralStructures.placeRandomHead(level, headFloor, random, PROFILE)) {
				placed = true;
			}
		}

		if (placed) {
			HardCoralStructures.placeFansOnCoral(level, floor, radius, random, PROFILE);
			placeSparseSeagrass(level, floor, radius + 2, random);
		}

		return placed;
	}

	private static void placeSparseSeagrass(WorldGenLevel level, BlockPos floor, int radius, RandomSource random) {
		int blades = 2 + random.nextInt(3); // 2–4, scattered on the sand apron
		for (int i = 0; i < blades; i++) {
			int dx = random.nextInt(radius * 2 + 1) - radius;
			int dz = random.nextInt(radius * 2 + 1) - radius;
			// Prefer the outer sand ring so it doesn't bury the coral core.
			if (dx * dx + dz * dz < (radius * radius) / 4) {
				continue;
			}
			int localFloorY = HardCoralStructures.findLocalFloorY(
				level, floor.getX() + dx, floor.getY(), floor.getZ() + dz, PROFILE
			);
			if (localFloorY == Integer.MIN_VALUE) {
				continue;
			}
			BlockPos above = new BlockPos(floor.getX() + dx, localFloorY + 1, floor.getZ() + dz);
			if (!SeafloorHelpers.canPlaceInWater(level, above)) {
				continue;
			}

			// Rare tall seagrass; otherwise short.
			if (random.nextFloat() < 0.15f && SeafloorHelpers.canPlaceInWater(level, above.above())) {
				level.setBlock(above, Blocks.TALL_SEAGRASS.defaultBlockState()
					.setValue(BlockStateProperties.DOUBLE_BLOCK_HALF, DoubleBlockHalf.LOWER), 3);
				level.setBlock(above.above(), Blocks.TALL_SEAGRASS.defaultBlockState()
					.setValue(BlockStateProperties.DOUBLE_BLOCK_HALF, DoubleBlockHalf.UPPER), 3);
			} else {
				level.setBlock(above, Blocks.SEAGRASS.defaultBlockState(), 3);
			}
		}
	}
}
