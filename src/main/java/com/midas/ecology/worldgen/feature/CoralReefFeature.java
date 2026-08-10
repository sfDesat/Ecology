package com.midas.ecology.worldgen.feature;

import com.midas.ecology.worldgen.seafloor.HardCoralStructures;
import com.midas.ecology.worldgen.seafloor.SeafloorHelpers;
import com.mojang.serialization.Codec;
import net.minecraft.core.BlockPos;
import net.minecraft.tags.BlockTags;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.minecraft.world.level.levelgen.feature.FeaturePlaceContext;
import net.minecraft.world.level.material.Fluids;

/**
 * Continuous tropical hard-coral reef — pavement, packed heads, fans, and pickles.
 * Denser and more connected than {@link PatchReefIslandFeature}.
 */
public class CoralReefFeature extends Feature<CoralReefConfiguration> {
	private static final HardCoralStructures.Profile PROFILE = HardCoralStructures.Profile.CONTINUOUS;

	public CoralReefFeature(Codec<CoralReefConfiguration> codec) {
		super(codec);
	}

	@Override
	public boolean place(FeaturePlaceContext<CoralReefConfiguration> context) {
		WorldGenLevel level = context.level();
		RandomSource random = context.random();
		CoralReefConfiguration config = context.config();
		BlockPos.MutableBlockPos floor = context.origin().mutable();

		if (!SeafloorHelpers.snapToSoftFloor(level, floor) || !SeafloorHelpers.hasWaterAbove(level, floor)) {
			return false;
		}

		int radius = config.radius().sample(random);
		HardCoralStructures.paintSandHalo(level, floor, radius + 2, random, PROFILE);
		paintCoralPavement(level, floor, radius, random);

		int coralCount = config.coralCount().sample(random);
		boolean placed = false;

		for (int i = 0; i < coralCount; i++) {
			// Tighter gaussian than patch reef so heads pack into a continuous mound.
			int dx = (int) Math.round(random.nextGaussian() * radius * 0.32);
			int dz = (int) Math.round(random.nextGaussian() * radius * 0.32);
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
			placePickles(level, floor, radius, random);
		}

		return placed;
	}

	/** Irregular hard-coral pavement so the reef reads as one connected structure. */
	private static void paintCoralPavement(WorldGenLevel level, BlockPos floor, int radius, RandomSource random) {
		BlockPos.MutableBlockPos cursor = new BlockPos.MutableBlockPos();
		int coreR2 = (radius * radius) * 2 / 3;

		for (int dx = -radius; dx <= radius; dx++) {
			for (int dz = -radius; dz <= radius; dz++) {
				int dist2 = dx * dx + dz * dz;
				if (dist2 > radius * radius + random.nextInt(3)) {
					continue;
				}
				// Light pavement — heads carry the reef look, not a solid coral floor.
				float chance = dist2 <= coreR2 ? 0.32f : 0.12f;
				if (random.nextFloat() > chance) {
					continue;
				}

				int localFloorY = HardCoralStructures.findLocalFloorY(
					level, floor.getX() + dx, floor.getY(), floor.getZ() + dz, PROFILE
				);
				if (localFloorY == Integer.MIN_VALUE) {
					continue;
				}
				cursor.set(floor.getX() + dx, localFloorY, floor.getZ() + dz);
				if (!level.getFluidState(cursor.above()).is(Fluids.WATER)) {
					continue;
				}
				BlockState coral = SeafloorHelpers.randomHardCoralBlock(random).defaultBlockState();
				level.setBlock(cursor, coral, 3);
			}
		}
	}

	private static void placePickles(WorldGenLevel level, BlockPos floor, int radius, RandomSource random) {
		int pickles = 2 + random.nextInt(4); // 2–5
		for (int i = 0; i < pickles; i++) {
			int dx = random.nextInt(radius * 2 + 1) - radius;
			int dz = random.nextInt(radius * 2 + 1) - radius;
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
			BlockState under = level.getBlockState(above.below());
			if (!under.is(BlockTags.CORAL_BLOCKS) && !under.is(BlockTags.SAND)) {
				continue;
			}
			int count = 1 + random.nextInt(4);
			level.setBlock(above, Blocks.SEA_PICKLE.defaultBlockState()
				.setValue(BlockStateProperties.PICKLES, count)
				.setValue(BlockStateProperties.WATERLOGGED, true), 3);
		}
	}
}
