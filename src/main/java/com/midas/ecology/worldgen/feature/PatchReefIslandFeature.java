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
import net.minecraft.world.level.block.state.properties.DoubleBlockHalf;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.minecraft.world.level.levelgen.feature.FeaturePlaceContext;
import net.minecraft.world.level.material.Fluids;

/**
 * Isolated patch-reef island: sand halo + custom hard-coral heads, a few fans, sparse seagrass.
 */
public class PatchReefIslandFeature extends Feature<PatchReefIslandConfiguration> {
	private static final Block[] CORAL_BLOCKS = {
		Blocks.TUBE_CORAL_BLOCK,
		Blocks.BRAIN_CORAL_BLOCK,
		Blocks.BUBBLE_CORAL_BLOCK,
		Blocks.FIRE_CORAL_BLOCK,
		Blocks.HORN_CORAL_BLOCK
	};

	private static final Block[] CORAL_PLANTS = {
		Blocks.TUBE_CORAL,
		Blocks.BRAIN_CORAL,
		Blocks.BUBBLE_CORAL,
		Blocks.FIRE_CORAL,
		Blocks.HORN_CORAL
	};

	public PatchReefIslandFeature(Codec<PatchReefIslandConfiguration> codec) {
		super(codec);
	}

	@Override
	public boolean place(FeaturePlaceContext<PatchReefIslandConfiguration> context) {
		WorldGenLevel level = context.level();
		RandomSource random = context.random();
		PatchReefIslandConfiguration config = context.config();
		BlockPos.MutableBlockPos floor = context.origin().mutable();

		if (!isSeafloor(level.getBlockState(floor))) {
			if (isSeafloor(level.getBlockState(floor.below()))) {
				floor.move(0, -1, 0);
			} else {
				return false;
			}
		}
		if (!level.getFluidState(floor.above()).is(Fluids.WATER)) {
			return false;
		}

		int radius = config.radius().sample(random);
		paintSandHalo(level, floor, radius + 3, random);

		int coralCount = config.coralCount().sample(random);
		boolean placed = false;

		for (int i = 0; i < coralCount; i++) {
			int dx = (int) Math.round(random.nextGaussian() * radius * 0.4);
			int dz = (int) Math.round(random.nextGaussian() * radius * 0.4);
			dx = Math.max(-radius, Math.min(radius, dx));
			dz = Math.max(-radius, Math.min(radius, dz));

			int localFloorY = findLocalFloorY(level, floor.getX() + dx, floor.getY(), floor.getZ() + dz);
			if (localFloorY == Integer.MIN_VALUE) {
				continue;
			}

			BlockPos headFloor = new BlockPos(floor.getX() + dx, localFloorY, floor.getZ() + dz);
			if (placeCoralStructure(level, headFloor, random)) {
				placed = true;
			}
		}

		if (placed) {
			placeFansOnCoral(level, floor, radius, random);
			placeSparseSeagrass(level, floor, radius + 2, random);
		}

		return placed;
	}

	/**
	 * Patch-reef hard-coral structures — compact shapes, not vanilla mushroom/tree.
	 */
	private static boolean placeCoralStructure(WorldGenLevel level, BlockPos floor, RandomSource random) {
		if (!level.getFluidState(floor.above()).is(Fluids.WATER)) {
			return false;
		}

		Block coral = CORAL_BLOCKS[random.nextInt(CORAL_BLOCKS.length)];
		BlockState state = coral.defaultBlockState();
		return switch (random.nextInt(4)) {
			case 0 -> placeColumn(level, floor, state, random);
			case 1 -> placeBoulder(level, floor, state, random);
			case 2 -> placeBranch(level, floor, state, random);
			default -> placePlate(level, floor, state, random);
		};
	}

	/** Slim upright head, 2–4 tall. */
	private static boolean placeColumn(WorldGenLevel level, BlockPos floor, BlockState state, RandomSource random) {
		int height = 2 + random.nextInt(3);
		boolean placed = setFloorCoral(level, floor, state);
		for (int y = 1; y <= height; y++) {
			BlockPos pos = floor.above(y);
			if (!canPlaceInWater(level, pos)) {
				break;
			}
			level.setBlock(pos, state, 3);
			placed = true;
		}
		if (placed) {
			tryCap(level, floor.above(height), random);
		}
		return placed;
	}

	/** Low rounded lump with a 2-block footprint. */
	private static boolean placeBoulder(WorldGenLevel level, BlockPos floor, BlockState state, RandomSource random) {
		boolean placed = setFloorCoral(level, floor, state);
		Direction side = Direction.Plane.HORIZONTAL.getRandomDirection(random);
		BlockPos sideFloor = floor.relative(side);
		if (isSeafloor(level.getBlockState(sideFloor)) && level.getFluidState(sideFloor.above()).is(Fluids.WATER)) {
			level.setBlock(sideFloor, state, 3);
			placed = true;
			if (canPlaceInWater(level, sideFloor.above()) && random.nextBoolean()) {
				level.setBlock(sideFloor.above(), state, 3);
			}
		}
		int height = 1 + random.nextInt(2);
		for (int y = 1; y <= height; y++) {
			BlockPos pos = floor.above(y);
			if (!canPlaceInWater(level, pos)) {
				break;
			}
			level.setBlock(pos, state, 3);
			placed = true;
		}
		return placed;
	}

	/** Short trunk with one side arm. */
	private static boolean placeBranch(WorldGenLevel level, BlockPos floor, BlockState state, RandomSource random) {
		boolean placed = setFloorCoral(level, floor, state);
		int height = 2 + random.nextInt(2);
		for (int y = 1; y <= height; y++) {
			BlockPos pos = floor.above(y);
			if (!canPlaceInWater(level, pos)) {
				return placed;
			}
			level.setBlock(pos, state, 3);
			placed = true;
			if (y == height - 1 || y == height) {
				Direction dir = Direction.Plane.HORIZONTAL.getRandomDirection(random);
				BlockPos arm = pos.relative(dir);
				if (canPlaceInWater(level, arm)) {
					level.setBlock(arm, state, 3);
					if (random.nextBoolean() && canPlaceInWater(level, arm.above())) {
						level.setBlock(arm.above(), state, 3);
						tryCap(level, arm.above(1), random);
					} else {
						tryCap(level, arm, random);
					}
				}
			}
		}
		tryCap(level, floor.above(height), random);
		return placed;
	}

	/** Flat hard-coral plate on the sand. */
	private static boolean placePlate(WorldGenLevel level, BlockPos floor, BlockState state, RandomSource random) {
		boolean placed = setFloorCoral(level, floor, state);
		for (Direction dir : Direction.Plane.HORIZONTAL) {
			if (random.nextFloat() > 0.7f) {
				continue;
			}
			BlockPos side = floor.relative(dir);
			if (isSeafloor(level.getBlockState(side)) && level.getFluidState(side.above()).is(Fluids.WATER)) {
				level.setBlock(side, state, 3);
				placed = true;
			}
		}
		if (canPlaceInWater(level, floor.above()) && random.nextFloat() < 0.6f) {
			level.setBlock(floor.above(), state, 3);
			placed = true;
			tryCap(level, floor.above(1), random);
		} else {
			tryCap(level, floor.above(), random);
		}
		return placed;
	}

	private static void placeFansOnCoral(WorldGenLevel level, BlockPos floor, int radius, RandomSource random) {
		int placedFans = 0;
		int maxFans = 5 + random.nextInt(4); // 5–8 fans perched on coral blocks
		BlockPos.MutableBlockPos cursor = new BlockPos.MutableBlockPos();

		for (int dx = -radius; dx <= radius && placedFans < maxFans; dx++) {
			for (int dz = -radius; dz <= radius && placedFans < maxFans; dz++) {
				if (dx * dx + dz * dz > radius * radius) {
					continue;
				}
				if (random.nextFloat() > 0.65f) {
					continue;
				}

				int localFloorY = findLocalFloorY(level, floor.getX() + dx, floor.getY(), floor.getZ() + dz);
				if (localFloorY == Integer.MIN_VALUE) {
					continue;
				}

				int topCoralY = Integer.MIN_VALUE;
				for (int y = localFloorY; y <= localFloorY + 6; y++) {
					cursor.set(floor.getX() + dx, y, floor.getZ() + dz);
					if (level.getBlockState(cursor).is(BlockTags.CORAL_BLOCKS)) {
						topCoralY = y;
					}
				}
				if (topCoralY == Integer.MIN_VALUE) {
					continue;
				}

				cursor.set(floor.getX() + dx, topCoralY + 1, floor.getZ() + dz);
				if (!canPlaceInWater(level, cursor)) {
					continue;
				}

				BlockState fan = SeafloorFanFeature.randomFan(random).defaultBlockState();
				if (fan.hasProperty(BlockStateProperties.WATERLOGGED)) {
					fan = fan.setValue(BlockStateProperties.WATERLOGGED, true);
				}
				level.setBlock(cursor, fan, 3);
				placedFans++;
			}
		}
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
			int localFloorY = findLocalFloorY(level, floor.getX() + dx, floor.getY(), floor.getZ() + dz);
			if (localFloorY == Integer.MIN_VALUE) {
				continue;
			}
			BlockPos above = new BlockPos(floor.getX() + dx, localFloorY + 1, floor.getZ() + dz);
			if (!canPlaceInWater(level, above)) {
				continue;
			}

			// Rare tall seagrass; otherwise short.
			if (random.nextFloat() < 0.15f && canPlaceInWater(level, above.above())) {
				level.setBlock(above, Blocks.TALL_SEAGRASS.defaultBlockState()
					.setValue(BlockStateProperties.DOUBLE_BLOCK_HALF, DoubleBlockHalf.LOWER), 3);
				level.setBlock(above.above(), Blocks.TALL_SEAGRASS.defaultBlockState()
					.setValue(BlockStateProperties.DOUBLE_BLOCK_HALF, DoubleBlockHalf.UPPER), 3);
			} else {
				level.setBlock(above, Blocks.SEAGRASS.defaultBlockState(), 3);
			}
		}
	}

	private static boolean setFloorCoral(WorldGenLevel level, BlockPos floor, BlockState state) {
		if (isSeafloor(level.getBlockState(floor))) {
			level.setBlock(floor, state, 3);
			return true;
		}
		return false;
	}

	private static void tryCap(WorldGenLevel level, BlockPos pos, RandomSource random) {
		if (!canPlaceInWater(level, pos)) {
			return;
		}
		// Prefer a fan on coral tops sometimes; otherwise a coral plant.
		Block cap = random.nextFloat() < 0.6f
			? SeafloorFanFeature.randomFan(random)
			: CORAL_PLANTS[random.nextInt(CORAL_PLANTS.length)];
		BlockState state = cap.defaultBlockState();
		if (state.hasProperty(BlockStateProperties.WATERLOGGED)) {
			state = state.setValue(BlockStateProperties.WATERLOGGED, true);
		}
		level.setBlock(pos, state, 3);
	}

	private static boolean canPlaceInWater(WorldGenLevel level, BlockPos pos) {
		return level.getFluidState(pos).is(Fluids.WATER) && level.getBlockState(pos).canBeReplaced();
	}

	private static void paintSandHalo(WorldGenLevel level, BlockPos floor, int radius, RandomSource random) {
		BlockPos.MutableBlockPos cursor = new BlockPos.MutableBlockPos();
		BlockState sand = Blocks.SAND.defaultBlockState();
		for (int dx = -radius; dx <= radius; dx++) {
			for (int dz = -radius; dz <= radius; dz++) {
				if (dx * dx + dz * dz > radius * radius + random.nextInt(4)) {
					continue;
				}
				int localFloorY = findLocalFloorY(level, floor.getX() + dx, floor.getY(), floor.getZ() + dz);
				if (localFloorY == Integer.MIN_VALUE) {
					continue;
				}
				cursor.set(floor.getX() + dx, localFloorY, floor.getZ() + dz);
				BlockState existing = level.getBlockState(cursor);
				if (isSeafloor(existing) && !existing.is(BlockTags.SAND)) {
					level.setBlock(cursor, sand, 3);
				}
			}
		}
	}

	private static int findLocalFloorY(WorldGenLevel level, int x, int originFloorY, int z) {
		BlockPos.MutableBlockPos cursor = new BlockPos.MutableBlockPos();
		for (int y = originFloorY + 5; y >= originFloorY - 5; y--) {
			cursor.set(x, y, z);
			if (isSeafloor(level.getBlockState(cursor)) && level.getFluidState(cursor.above()).is(Fluids.WATER)) {
				return y;
			}
		}
		return Integer.MIN_VALUE;
	}

	private static boolean isSeafloor(BlockState state) {
		return state.is(BlockTags.SAND)
			|| state.is(Blocks.GRAVEL)
			|| state.is(Blocks.CLAY)
			|| state.is(Blocks.STONE)
			|| state.is(Blocks.COBBLESTONE)
			|| state.is(Blocks.DIRT)
			|| state.is(Blocks.COARSE_DIRT);
	}
}
