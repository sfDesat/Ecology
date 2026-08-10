package com.midas.ecology.worldgen.seafloor;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.tags.BlockTags;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.material.Fluids;

/**
 * Shared hard-coral heads, caps, fans, and sand halos for patch / continuous reefs.
 * Density knobs live in {@link Profile} so callers keep their placement look.
 */
public final class HardCoralStructures {
	private HardCoralStructures() {
	}

	/**
	 * Patch reef: compact heads on soft floor only.
	 * Continuous reef: taller / denser heads that may sit on existing coral.
	 */
	public record Profile(
		boolean allowCoralFloor,
		int columnHeightExtra,
		boolean boulderAllSides,
		float boulderSideChance,
		int boulderHeightExtra,
		int branchHeightExtra,
		boolean branchArmOnLastTwo,
		float plateSideChance,
		float plateAboveChance,
		float capFanChance,
		int fanMaxBase,
		int fanMaxExtra,
		float fanSkipChance,
		int fanCoralScanExtra,
		int floorSlack,
		boolean sandHaloSkipsCoral
	) {
		/** Isolated patch-reef island densities. */
		public static final Profile PATCH = new Profile(
			false,
			2, // column: 2 + nextInt(3) → 2–4
			false,
			1.0f, // single random side always attempted
			1, // boulder height: 1 + nextInt(2)
			1, // branch: 2 + nextInt(2)
			true,
			0.7f, // plate sides: nextFloat() > 0.7 skip
			0.6f,
			0.6f,
			5,
			4,
			0.65f,
			6,
			5,
			false
		);

		/** Dense continuous tropical reef densities. */
		public static final Profile CONTINUOUS = new Profile(
			true,
			3, // column: 2 + nextInt(4) → 2–5
			true,
			0.55f, // boulder/plate sides: nextFloat() > 0.55 skip
			2, // boulder height: 1 + nextInt(3)
			2, // branch: 2 + nextInt(3)
			false,
			0.55f,
			0.75f,
			0.8f,
			8,
			5,
			0.55f,
			7,
			5,
			true
		);
	}

	public static int findLocalFloorY(WorldGenLevel level, int x, int originFloorY, int z, Profile profile) {
		if (profile.allowCoralFloor()) {
			return SeafloorHelpers.findLocalSeafloorOrCoralY(level, x, originFloorY, z, profile.floorSlack());
		}
		return SeafloorHelpers.findLocalSoftFloorY(level, x, originFloorY, z, profile.floorSlack());
	}

	public static boolean placeRandomHead(WorldGenLevel level, BlockPos floor, RandomSource random, Profile profile) {
		if (!level.getFluidState(floor.above()).is(Fluids.WATER)) {
			return false;
		}

		Block coral = SeafloorHelpers.randomHardCoralBlock(random);
		BlockState state = coral.defaultBlockState();
		return switch (random.nextInt(4)) {
			case 0 -> placeColumn(level, floor, state, random, profile);
			case 1 -> placeBoulder(level, floor, state, random, profile);
			case 2 -> placeBranch(level, floor, state, random, profile);
			default -> placePlate(level, floor, state, random, profile);
		};
	}

	public static void placeFansOnCoral(WorldGenLevel level, BlockPos floor, int radius, RandomSource random, Profile profile) {
		int placedFans = 0;
		int maxFans = profile.fanMaxBase() + random.nextInt(profile.fanMaxExtra());
		BlockPos.MutableBlockPos cursor = new BlockPos.MutableBlockPos();

		for (int dx = -radius; dx <= radius && placedFans < maxFans; dx++) {
			for (int dz = -radius; dz <= radius && placedFans < maxFans; dz++) {
				if (dx * dx + dz * dz > radius * radius) {
					continue;
				}
				if (random.nextFloat() > profile.fanSkipChance()) {
					continue;
				}

				int localFloorY = findLocalFloorY(level, floor.getX() + dx, floor.getY(), floor.getZ() + dz, profile);
				if (localFloorY == Integer.MIN_VALUE) {
					continue;
				}

				int topCoralY = Integer.MIN_VALUE;
				int maxY = localFloorY + profile.fanCoralScanExtra();
				for (int y = localFloorY; y <= maxY; y++) {
					cursor.set(floor.getX() + dx, y, floor.getZ() + dz);
					if (level.getBlockState(cursor).is(BlockTags.CORAL_BLOCKS)) {
						topCoralY = y;
					}
				}
				if (topCoralY == Integer.MIN_VALUE) {
					continue;
				}

				cursor.set(floor.getX() + dx, topCoralY + 1, floor.getZ() + dz);
				if (SeafloorHelpers.tryPlaceFan(level, cursor, random)) {
					placedFans++;
				}
			}
		}
	}

	public static void paintSandHalo(WorldGenLevel level, BlockPos floor, int radius, RandomSource random, Profile profile) {
		BlockPos.MutableBlockPos cursor = new BlockPos.MutableBlockPos();
		BlockState sand = Blocks.SAND.defaultBlockState();
		for (int dx = -radius; dx <= radius; dx++) {
			for (int dz = -radius; dz <= radius; dz++) {
				if (dx * dx + dz * dz > radius * radius + random.nextInt(4)) {
					continue;
				}
				int localFloorY = findLocalFloorY(level, floor.getX() + dx, floor.getY(), floor.getZ() + dz, profile);
				if (localFloorY == Integer.MIN_VALUE) {
					continue;
				}
				cursor.set(floor.getX() + dx, localFloorY, floor.getZ() + dz);
				BlockState existing = level.getBlockState(cursor);
				if (!SeafloorHelpers.isSoftSeafloor(existing) || existing.is(BlockTags.SAND)) {
					continue;
				}
				if (profile.sandHaloSkipsCoral() && existing.is(BlockTags.CORAL_BLOCKS)) {
					continue;
				}
				level.setBlock(cursor, sand, 3);
			}
		}
	}

	private static boolean placeColumn(WorldGenLevel level, BlockPos floor, BlockState state, RandomSource random, Profile profile) {
		int height = 2 + random.nextInt(profile.columnHeightExtra() + 1);
		boolean placed = setFloorCoral(level, floor, state, profile);
		for (int y = 1; y <= height; y++) {
			BlockPos pos = floor.above(y);
			if (!SeafloorHelpers.canPlaceInWater(level, pos)) {
				break;
			}
			level.setBlock(pos, state, 3);
			placed = true;
		}
		if (placed) {
			tryCap(level, floor.above(height), random, profile);
		}
		return placed;
	}

	private static boolean placeBoulder(WorldGenLevel level, BlockPos floor, BlockState state, RandomSource random, Profile profile) {
		boolean placed = setFloorCoral(level, floor, state, profile);
		if (profile.boulderAllSides()) {
			for (Direction dir : Direction.Plane.HORIZONTAL) {
				if (random.nextFloat() > profile.boulderSideChance()) {
					continue;
				}
				placed |= tryBoulderSide(level, floor.relative(dir), state, random, profile);
			}
		} else {
			Direction side = Direction.Plane.HORIZONTAL.getRandomDirection(random);
			placed |= tryBoulderSide(level, floor.relative(side), state, random, profile);
		}
		int height = 1 + random.nextInt(profile.boulderHeightExtra() + 1);
		for (int y = 1; y <= height; y++) {
			BlockPos pos = floor.above(y);
			if (!SeafloorHelpers.canPlaceInWater(level, pos)) {
				break;
			}
			level.setBlock(pos, state, 3);
			placed = true;
		}
		return placed;
	}

	private static boolean tryBoulderSide(WorldGenLevel level, BlockPos sideFloor, BlockState state, RandomSource random, Profile profile) {
		BlockState under = level.getBlockState(sideFloor);
		if (!isAllowedFloor(under, profile) || !level.getFluidState(sideFloor.above()).is(Fluids.WATER)) {
			return false;
		}
		level.setBlock(sideFloor, state, 3);
		if (SeafloorHelpers.canPlaceInWater(level, sideFloor.above()) && random.nextBoolean()) {
			level.setBlock(sideFloor.above(), state, 3);
		}
		return true;
	}

	private static boolean placeBranch(WorldGenLevel level, BlockPos floor, BlockState state, RandomSource random, Profile profile) {
		boolean placed = setFloorCoral(level, floor, state, profile);
		int height = 2 + random.nextInt(profile.branchHeightExtra() + 1);
		for (int y = 1; y <= height; y++) {
			BlockPos pos = floor.above(y);
			if (!SeafloorHelpers.canPlaceInWater(level, pos)) {
				return placed;
			}
			level.setBlock(pos, state, 3);
			placed = true;
			boolean placeArm = profile.branchArmOnLastTwo()
				? (y == height - 1 || y == height)
				: (y >= height - 1);
			if (placeArm) {
				Direction dir = Direction.Plane.HORIZONTAL.getRandomDirection(random);
				BlockPos arm = pos.relative(dir);
				if (SeafloorHelpers.canPlaceInWater(level, arm)) {
					level.setBlock(arm, state, 3);
					if (random.nextBoolean() && SeafloorHelpers.canPlaceInWater(level, arm.above())) {
						level.setBlock(arm.above(), state, 3);
						tryCap(level, arm.above(1), random, profile);
					} else {
						tryCap(level, arm, random, profile);
					}
				}
			}
		}
		tryCap(level, floor.above(height), random, profile);
		return placed;
	}

	private static boolean placePlate(WorldGenLevel level, BlockPos floor, BlockState state, RandomSource random, Profile profile) {
		boolean placed = setFloorCoral(level, floor, state, profile);
		for (Direction dir : Direction.Plane.HORIZONTAL) {
			if (random.nextFloat() > profile.plateSideChance()) {
				continue;
			}
			BlockPos side = floor.relative(dir);
			BlockState under = level.getBlockState(side);
			if (isAllowedFloor(under, profile) && level.getFluidState(side.above()).is(Fluids.WATER)) {
				level.setBlock(side, state, 3);
				placed = true;
			}
		}
		if (SeafloorHelpers.canPlaceInWater(level, floor.above()) && random.nextFloat() < profile.plateAboveChance()) {
			level.setBlock(floor.above(), state, 3);
			placed = true;
			tryCap(level, floor.above(1), random, profile);
		} else {
			tryCap(level, floor.above(), random, profile);
		}
		return placed;
	}

	private static boolean setFloorCoral(WorldGenLevel level, BlockPos floor, BlockState state, Profile profile) {
		BlockState existing = level.getBlockState(floor);
		if (!isAllowedFloor(existing, profile)) {
			return false;
		}
		level.setBlock(floor, state, 3);
		return true;
	}

	private static boolean isAllowedFloor(BlockState state, Profile profile) {
		if (SeafloorHelpers.isSoftSeafloor(state)) {
			return true;
		}
		return profile.allowCoralFloor() && state.is(BlockTags.CORAL_BLOCKS);
	}

	private static void tryCap(WorldGenLevel level, BlockPos pos, RandomSource random, Profile profile) {
		if (random.nextFloat() < profile.capFanChance()) {
			SeafloorHelpers.tryPlaceFan(level, pos, random);
			return;
		}
		if (!SeafloorHelpers.canPlaceInWater(level, pos) || SeafloorHelpers.isCoralFan(level.getBlockState(pos))) {
			return;
		}
		if (!SeafloorHelpers.isFanSupport(level.getBlockState(pos.below()))) {
			return;
		}
		BlockState state = SeafloorHelpers.randomHardCoralPlant(random).defaultBlockState();
		if (state.hasProperty(BlockStateProperties.WATERLOGGED)) {
			state = state.setValue(BlockStateProperties.WATERLOGGED, true);
		}
		level.setBlock(pos, state, 3);
	}
}
