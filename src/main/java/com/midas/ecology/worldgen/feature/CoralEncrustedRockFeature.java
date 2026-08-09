package com.midas.ecology.worldgen.feature;

import com.mojang.serialization.Codec;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.tags.BlockTags;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.minecraft.world.level.levelgen.feature.FeaturePlaceContext;
import net.minecraft.world.level.material.Fluids;

/**
 * Rare warm-ocean rock outcrop with sparse coral encrustation — mostly fans, a few coral blocks.
 */
public class CoralEncrustedRockFeature extends Feature<UnderwaterRockConfiguration> {
	private static final int SURFACE_CLEARANCE = 2;
	private static final int EMBED_DEPTH = 2;
	private static final int LOCAL_FLOOR_SLACK = 4;

	private static final Block[] CORAL_BLOCKS = {
		Blocks.TUBE_CORAL_BLOCK,
		Blocks.BRAIN_CORAL_BLOCK,
		Blocks.BUBBLE_CORAL_BLOCK,
		Blocks.FIRE_CORAL_BLOCK,
		Blocks.HORN_CORAL_BLOCK
	};

	public CoralEncrustedRockFeature(Codec<UnderwaterRockConfiguration> codec) {
		super(codec);
	}

	@Override
	public boolean place(FeaturePlaceContext<UnderwaterRockConfiguration> context) {
		WorldGenLevel level = context.level();
		RandomSource random = context.random();
		UnderwaterRockConfiguration config = context.config();
		BlockPos.MutableBlockPos floor = context.origin().mutable();

		if (!isSoftSeafloor(level.getBlockState(floor))) {
			if (isSoftSeafloor(level.getBlockState(floor.below()))) {
				floor.move(0, -1, 0);
			} else {
				return false;
			}
		}
		if (!level.getFluidState(floor.above()).is(Fluids.WATER)) {
			return false;
		}

		int waterDepth = countWaterAbove(level, floor);
		int maxHeight = waterDepth - SURFACE_CLEARANCE;
		if (maxHeight < 3) {
			return false;
		}

		int radius = config.radius().sample(random);
		int height = Math.min(config.height().sample(random), maxHeight);
		BlockState rock = config.state();
		boolean placed = false;
		BlockPos.MutableBlockPos cursor = new BlockPos.MutableBlockPos();

		for (int dx = -radius; dx <= radius; dx++) {
			for (int dz = -radius; dz <= radius; dz++) {
				int distSq = dx * dx + dz * dz;
				if (distSq > radius * radius + random.nextInt(2)) {
					continue;
				}

				int localFloorY = findLocalFloorY(level, floor.getX() + dx, floor.getY(), floor.getZ() + dz);
				if (localFloorY == Integer.MIN_VALUE) {
					continue;
				}

				int baseY = localFloorY - EMBED_DEPTH + 1;
				int topY = localFloorY + height;

				for (int y = baseY; y <= topY; y++) {
					int aboveFloor = y - localFloorY;
					int layerIndex = Math.max(0, aboveFloor);
					int layerRadius = Math.max(0, radius - (layerIndex * radius) / Math.max(1, height));
					if (distSq > layerRadius * layerRadius + random.nextInt(2)) {
						continue;
					}

					cursor.set(floor.getX() + dx, y, floor.getZ() + dz);
					BlockState existing = level.getBlockState(cursor);
					boolean water = level.getFluidState(cursor).is(Fluids.WATER);
					if (aboveFloor <= 0) {
						if (isSoftSeafloor(existing)) {
							level.setBlock(cursor, rock, 3);
							placed = true;
						}
					} else if (canReplace(existing, water)) {
						level.setBlock(cursor, rock, 3);
						placed = true;
					}
				}
			}
		}

		if (placed) {
			encrustWithCoral(level, floor, radius, height, random);
		}
		return placed;
	}

	/**
	 * Sparse coral blocks on the rock surface; fans are the main decoration.
	 */
	private static void encrustWithCoral(WorldGenLevel level, BlockPos floor, int radius, int height, RandomSource random) {
		BlockPos.MutableBlockPos cursor = new BlockPos.MutableBlockPos();
		int coralBlocks = 0;
		int maxCoralBlocks = 2 + random.nextInt(3); // 2–4
		int fans = 0;
		int maxFans = 6 + random.nextInt(5); // 6–10

		for (int dx = -radius - 1; dx <= radius + 1; dx++) {
			for (int dz = -radius - 1; dz <= radius + 1; dz++) {
				for (int y = floor.getY(); y <= floor.getY() + height + 1; y++) {
					cursor.set(floor.getX() + dx, y, floor.getZ() + dz);
					BlockState state = level.getBlockState(cursor);
					if (!isRock(state)) {
						continue;
					}

					// Occasional coral block replacing a surface rock face.
					if (coralBlocks < maxCoralBlocks && isExposedToWater(level, cursor) && random.nextFloat() < 0.08f) {
						BlockState coral = CORAL_BLOCKS[random.nextInt(CORAL_BLOCKS.length)].defaultBlockState();
						level.setBlock(cursor, coral, 3);
						coralBlocks++;
						state = coral;
					}

					// Fans on top of rock or coral — main look.
					if (fans < maxFans && random.nextFloat() < 0.22f) {
						BlockPos above = cursor.above();
						if (canPlaceInWater(level, above) && (isRock(state) || state.is(BlockTags.CORAL_BLOCKS))) {
							BlockState fan = SeafloorFanFeature.randomFan(random).defaultBlockState();
							if (fan.hasProperty(BlockStateProperties.WATERLOGGED)) {
								fan = fan.setValue(BlockStateProperties.WATERLOGGED, true);
							}
							level.setBlock(above, fan, 3);
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

	private static boolean isRock(BlockState state) {
		return state.is(Blocks.STONE)
			|| state.is(Blocks.COBBLESTONE)
			|| state.is(Blocks.ANDESITE)
			|| state.is(Blocks.DIORITE)
			|| state.is(Blocks.GRANITE);
	}

	private static boolean canPlaceInWater(WorldGenLevel level, BlockPos pos) {
		return level.getFluidState(pos).is(Fluids.WATER) && level.getBlockState(pos).canBeReplaced();
	}

	private static int findLocalFloorY(WorldGenLevel level, int x, int originFloorY, int z) {
		BlockPos.MutableBlockPos cursor = new BlockPos.MutableBlockPos();
		for (int y = originFloorY + LOCAL_FLOOR_SLACK; y >= originFloorY - LOCAL_FLOOR_SLACK; y--) {
			cursor.set(x, y, z);
			if (isSoftSeafloor(level.getBlockState(cursor)) && level.getFluidState(cursor.above()).is(Fluids.WATER)) {
				return y;
			}
		}
		return Integer.MIN_VALUE;
	}

	private static int countWaterAbove(WorldGenLevel level, BlockPos floor) {
		int depth = 0;
		BlockPos.MutableBlockPos cursor = floor.mutable().move(0, 1, 0);
		int maxY = level.getMaxY();
		while (cursor.getY() <= maxY && level.getFluidState(cursor).is(Fluids.WATER)) {
			depth++;
			cursor.move(0, 1, 0);
			if (depth > 64) {
				break;
			}
		}
		return depth;
	}

	private static boolean isSoftSeafloor(BlockState state) {
		return state.is(BlockTags.SAND)
			|| state.is(Blocks.GRAVEL)
			|| state.is(Blocks.CLAY)
			|| state.is(Blocks.STONE)
			|| state.is(Blocks.COBBLESTONE)
			|| state.is(Blocks.DIRT)
			|| state.is(Blocks.COARSE_DIRT)
			|| state.is(Blocks.MOSS_BLOCK);
	}

	private static boolean canReplace(BlockState state, boolean water) {
		return water || state.isAir() || state.canBeReplaced();
	}
}
