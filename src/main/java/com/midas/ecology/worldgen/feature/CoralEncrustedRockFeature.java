package com.midas.ecology.worldgen.feature;

import com.midas.ecology.worldgen.seafloor.RockMoundPlacer;
import com.midas.ecology.worldgen.seafloor.SeafloorHelpers;
import com.mojang.serialization.Codec;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.tags.BlockTags;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.minecraft.world.level.levelgen.feature.FeaturePlaceContext;
import net.minecraft.world.level.material.Fluids;

/**
 * Rare warm-ocean rock outcrop with sparse coral encrustation — mostly fans, a few coral blocks.
 */
public class CoralEncrustedRockFeature extends Feature<UnderwaterRockConfiguration> {
	public CoralEncrustedRockFeature(Codec<UnderwaterRockConfiguration> codec) {
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
		if (maxHeight < 3) {
			return false;
		}

		int radius = config.radius().sample(random);
		int height = Math.min(config.height().sample(random), maxHeight);
		BlockState rock = config.state();
		boolean placed = RockMoundPlacer.place(level, floor, radius, height, rock, random);
		if (placed) {
			encrustWithCoral(level, floor, radius, height, random);
		}
		return placed;
	}

	private static void encrustWithCoral(WorldGenLevel level, BlockPos floor, int radius, int height, RandomSource random) {
		BlockPos.MutableBlockPos cursor = new BlockPos.MutableBlockPos();
		int coralBlocks = 0;
		int maxCoralBlocks = 2 + random.nextInt(3);
		int fans = 0;
		int maxFans = 6 + random.nextInt(5);

		for (int dx = -radius - 1; dx <= radius + 1; dx++) {
			for (int dz = -radius - 1; dz <= radius + 1; dz++) {
				for (int y = floor.getY(); y <= floor.getY() + height + 1; y++) {
					cursor.set(floor.getX() + dx, y, floor.getZ() + dz);
					BlockState state = level.getBlockState(cursor);
					if (!SeafloorHelpers.isRock(state)) {
						continue;
					}

					if (coralBlocks < maxCoralBlocks && isExposedToWater(level, cursor) && random.nextFloat() < 0.08f) {
						BlockState coral = SeafloorHelpers.randomHardCoralBlock(random).defaultBlockState();
						level.setBlock(cursor, coral, 3);
						coralBlocks++;
						state = coral;
					}

					if (fans < maxFans && random.nextFloat() < 0.22f) {
						BlockPos above = cursor.above();
						if ((SeafloorHelpers.isRock(state) || state.is(BlockTags.CORAL_BLOCKS))
							&& SeafloorHelpers.tryPlaceFan(level, above, random)) {
							fans++;
						}
					}
				}
			}
		}
	}

	private static boolean isExposedToWater(WorldGenLevel level, BlockPos pos) {
		for (Direction dir : Direction.values()) {
			if (dir == Direction.DOWN) {
				continue;
			}
			if (level.getFluidState(pos.relative(dir)).is(Fluids.WATER)) {
				return true;
			}
		}
		return false;
	}
}
