package com.midas.ecology.worldgen.seafloor;

import net.minecraft.core.BlockPos;
import net.minecraft.tags.BlockTags;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.material.Fluids;

/**
 * Shared seafloor placement primitives for Ecology underwater features.
 * Feature classes keep their own shapes and densities; they call these helpers
 * for floor snapping, water checks, and shared block sets.
 */
public final class SeafloorHelpers {
	public static final int DEFAULT_LOCAL_FLOOR_SLACK = 4;

	public static final Block[] HARD_CORAL_BLOCKS = {
		Blocks.TUBE_CORAL_BLOCK,
		Blocks.BRAIN_CORAL_BLOCK,
		Blocks.BUBBLE_CORAL_BLOCK,
		Blocks.FIRE_CORAL_BLOCK,
		Blocks.HORN_CORAL_BLOCK
	};

	public static final Block[] HARD_CORAL_PLANTS = {
		Blocks.TUBE_CORAL,
		Blocks.BRAIN_CORAL,
		Blocks.BUBBLE_CORAL,
		Blocks.FIRE_CORAL,
		Blocks.HORN_CORAL
	};

	public static final Block[] CORAL_FANS = {
		Blocks.TUBE_CORAL_FAN,
		Blocks.BRAIN_CORAL_FAN,
		Blocks.BUBBLE_CORAL_FAN,
		Blocks.FIRE_CORAL_FAN,
		Blocks.HORN_CORAL_FAN
	};

	public static final Block[] DEAD_CORAL_FANS = {
		Blocks.DEAD_TUBE_CORAL_FAN,
		Blocks.DEAD_BRAIN_CORAL_FAN,
		Blocks.DEAD_BUBBLE_CORAL_FAN,
		Blocks.DEAD_FIRE_CORAL_FAN,
		Blocks.DEAD_HORN_CORAL_FAN
	};

	private SeafloorHelpers() {
	}

	/**
	 * Snaps {@code origin} to the solid seafloor block (origin or one below).
	 * Returns {@code false} when neither is a soft seafloor.
	 */
	public static boolean snapToSoftFloor(WorldGenLevel level, BlockPos.MutableBlockPos floor) {
		if (isSoftSeafloor(level.getBlockState(floor))) {
			return true;
		}
		if (isSoftSeafloor(level.getBlockState(floor.below()))) {
			floor.move(0, -1, 0);
			return true;
		}
		return false;
	}

	/**
	 * Like {@link #snapToSoftFloor} but also accepts coral blocks as floor.
	 */
	public static boolean snapToSeafloorOrCoral(WorldGenLevel level, BlockPos.MutableBlockPos floor) {
		if (isSeafloorOrCoral(level.getBlockState(floor))) {
			return true;
		}
		if (isSeafloorOrCoral(level.getBlockState(floor.below()))) {
			floor.move(0, -1, 0);
			return true;
		}
		return false;
	}

	public static boolean hasWaterAbove(WorldGenLevel level, BlockPos floor) {
		return level.getFluidState(floor.above()).is(Fluids.WATER);
	}

	/**
	 * Finds soft seafloor under a column near {@code originFloorY}.
	 * Returns {@link Integer#MIN_VALUE} when none is nearby.
	 */
	public static int findLocalSoftFloorY(WorldGenLevel level, int x, int originFloorY, int z) {
		return findLocalSoftFloorY(level, x, originFloorY, z, DEFAULT_LOCAL_FLOOR_SLACK);
	}

	public static int findLocalSoftFloorY(WorldGenLevel level, int x, int originFloorY, int z, int slack) {
		BlockPos.MutableBlockPos cursor = new BlockPos.MutableBlockPos();
		for (int y = originFloorY + slack; y >= originFloorY - slack; y--) {
			cursor.set(x, y, z);
			if (isSoftSeafloor(level.getBlockState(cursor)) && level.getFluidState(cursor.above()).is(Fluids.WATER)) {
				return y;
			}
		}
		return Integer.MIN_VALUE;
	}

	/**
	 * Soft seafloor or coral block under water — used by continuous reefs.
	 */
	public static int findLocalSeafloorOrCoralY(WorldGenLevel level, int x, int originFloorY, int z) {
		return findLocalSeafloorOrCoralY(level, x, originFloorY, z, DEFAULT_LOCAL_FLOOR_SLACK);
	}

	public static int findLocalSeafloorOrCoralY(WorldGenLevel level, int x, int originFloorY, int z, int slack) {
		BlockPos.MutableBlockPos cursor = new BlockPos.MutableBlockPos();
		for (int y = originFloorY + slack; y >= originFloorY - slack; y--) {
			cursor.set(x, y, z);
			BlockState state = level.getBlockState(cursor);
			if (isSeafloorOrCoral(state) && level.getFluidState(cursor.above()).is(Fluids.WATER)) {
				return y;
			}
		}
		return Integer.MIN_VALUE;
	}

	public static int countWaterAbove(WorldGenLevel level, BlockPos floor) {
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

	public static boolean isSoftSeafloor(BlockState state) {
		return state.is(BlockTags.SAND)
			|| state.is(Blocks.GRAVEL)
			|| state.is(Blocks.CLAY)
			|| state.is(Blocks.STONE)
			|| state.is(Blocks.COBBLESTONE)
			|| state.is(Blocks.DIRT)
			|| state.is(Blocks.COARSE_DIRT)
			|| state.is(Blocks.MOSS_BLOCK)
			|| state.is(Blocks.DEEPSLATE)
			|| state.is(Blocks.BASALT)
			|| state.is(Blocks.SMOOTH_BASALT)
			|| state.is(Blocks.BLACKSTONE);
	}

	public static boolean isSeafloorOrCoral(BlockState state) {
		return isSoftSeafloor(state) || state.is(BlockTags.CORAL_BLOCKS);
	}

	public static boolean isRock(BlockState state) {
		return state.is(Blocks.STONE)
			|| state.is(Blocks.COBBLESTONE)
			|| state.is(Blocks.ANDESITE)
			|| state.is(Blocks.DIORITE)
			|| state.is(Blocks.GRANITE)
			|| state.is(Blocks.BASALT)
			|| state.is(Blocks.SMOOTH_BASALT)
			|| state.is(Blocks.BLACKSTONE)
			|| state.is(Blocks.DEEPSLATE);
	}

	public static boolean canReplaceWaterOrAir(BlockState state, boolean water) {
		return water || state.isAir() || state.canBeReplaced();
	}

	public static boolean canPlaceInWater(WorldGenLevel level, BlockPos pos) {
		return level.getFluidState(pos).is(Fluids.WATER) && level.getBlockState(pos).canBeReplaced();
	}

	public static boolean isCoralFan(BlockState state) {
		for (Block fan : CORAL_FANS) {
			if (state.is(fan)) {
				return true;
			}
		}
		for (Block fan : DEAD_CORAL_FANS) {
			if (state.is(fan)) {
				return true;
			}
		}
		return false;
	}

	public static boolean isHardCoralPlant(BlockState state) {
		for (Block plant : HARD_CORAL_PLANTS) {
			if (state.is(plant)) {
				return true;
			}
		}
		return false;
	}

	/** Solid support a standing fan may sit on — never another fan or soft plant. */
	public static boolean isFanSupport(BlockState state) {
		return isSoftSeafloor(state) || state.is(BlockTags.CORAL_BLOCKS) || isRock(state);
	}

	/**
	 * Fans are {@link BlockState#canBeReplaced()}, so {@link #canPlaceInWater} alone
	 * would stack them. Require clear water above a solid support.
	 */
	public static boolean canPlaceFanAt(WorldGenLevel level, BlockPos fanPos) {
		BlockState existing = level.getBlockState(fanPos);
		if (isCoralFan(existing) || isHardCoralPlant(existing)) {
			return false;
		}
		if (!canPlaceInWater(level, fanPos)) {
			return false;
		}
		return isFanSupport(level.getBlockState(fanPos.below()));
	}

	/** Places a waterlogged standing coral fan, or returns {@code false} if invalid. */
	public static boolean tryPlaceFan(WorldGenLevel level, BlockPos fanPos, RandomSource random) {
		if (!canPlaceFanAt(level, fanPos)) {
			return false;
		}
		BlockState state = randomCoralFan(random).defaultBlockState();
		if (state.hasProperty(BlockStateProperties.WATERLOGGED)) {
			state = state.setValue(BlockStateProperties.WATERLOGGED, true);
		}
		level.setBlock(fanPos, state, 3);
		return true;
	}

	/** Places a waterlogged standing dead (gray) coral fan. */
	public static boolean tryPlaceDeadFan(WorldGenLevel level, BlockPos fanPos, RandomSource random) {
		if (!canPlaceFanAt(level, fanPos)) {
			return false;
		}
		BlockState state = randomDeadCoralFan(random).defaultBlockState();
		if (state.hasProperty(BlockStateProperties.WATERLOGGED)) {
			state = state.setValue(BlockStateProperties.WATERLOGGED, true);
		}
		level.setBlock(fanPos, state, 3);
		return true;
	}

	public static Block randomHardCoralBlock(RandomSource random) {
		return HARD_CORAL_BLOCKS[random.nextInt(HARD_CORAL_BLOCKS.length)];
	}

	public static Block randomHardCoralPlant(RandomSource random) {
		return HARD_CORAL_PLANTS[random.nextInt(HARD_CORAL_PLANTS.length)];
	}

	public static Block randomCoralFan(RandomSource random) {
		return CORAL_FANS[random.nextInt(CORAL_FANS.length)];
	}

	public static Block randomDeadCoralFan(RandomSource random) {
		return DEAD_CORAL_FANS[random.nextInt(DEAD_CORAL_FANS.length)];
	}
}
