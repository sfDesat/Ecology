package com.midas.ecology.worldgen.feature;

import com.mojang.serialization.Codec;
import net.minecraft.core.BlockPos;
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
 * Soft-coral garden clump: gravel hardbottom with coral-block stubs, coral plants, and seafloor fans.
 */
public class SoftCoralClumpFeature extends Feature<SoftCoralClumpConfiguration> {
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

	public SoftCoralClumpFeature(Codec<SoftCoralClumpConfiguration> codec) {
		super(codec);
	}

	@Override
	public boolean place(FeaturePlaceContext<SoftCoralClumpConfiguration> context) {
		WorldGenLevel level = context.level();
		RandomSource random = context.random();
		SoftCoralClumpConfiguration config = context.config();
		BlockPos.MutableBlockPos floor = context.origin().mutable();

		if (!SeafloorFanFeature.isSeafloor(level.getBlockState(floor))) {
			if (SeafloorFanFeature.isSeafloor(level.getBlockState(floor.below()))) {
				floor.move(0, -1, 0);
			} else {
				return false;
			}
		}
		if (!level.getFluidState(floor.above()).is(Fluids.WATER)) {
			return false;
		}

		int radius = config.radius().sample(random);
		paintFloorPatch(level, floor, radius, random);

		boolean placed = false;
		int stubs = 2 + random.nextInt(3); // 2–4
		for (int i = 0; i < stubs; i++) {
			boolean tall = random.nextFloat() < 0.12f;
			if (placeCoralStub(level, floor, radius, random, tall)) {
				placed = true;
			}
		}

		int plants = 3 + random.nextInt(4); // 3–6
		for (int i = 0; i < plants; i++) {
			if (placeCoralPlant(level, floor, radius, random)) {
				placed = true;
			}
		}

		int fanCount = config.fanCount().sample(random);
		BlockPos.MutableBlockPos cursor = new BlockPos.MutableBlockPos();
		for (int i = 0; i < fanCount; i++) {
			int dx = random.nextInt(radius * 2 + 1) - radius;
			int dz = random.nextInt(radius * 2 + 1) - radius;
			if (dx * dx + dz * dz > radius * radius) {
				continue;
			}
			int localFloorY = findLocalFloorY(level, floor.getX() + dx, floor.getY(), floor.getZ() + dz);
			if (localFloorY == Integer.MIN_VALUE) {
				continue;
			}
			cursor.set(floor.getX() + dx, localFloorY, floor.getZ() + dz);
			if (SeafloorFanFeature.placeFan(level, cursor.immutable(), random)) {
				placed = true;
			}
		}

		if (placed && random.nextFloat() < 0.25f) {
			placeSeaPickle(level, floor, radius, random);
		}

		return placed;
	}

	private static boolean placeCoralStub(WorldGenLevel level, BlockPos floor, int radius, RandomSource random, boolean tall) {
		int dx = random.nextInt(radius * 2 + 1) - radius;
		int dz = random.nextInt(radius * 2 + 1) - radius;
		int localFloorY = findLocalFloorY(level, floor.getX() + dx, floor.getY(), floor.getZ() + dz);
		if (localFloorY == Integer.MIN_VALUE) {
			return false;
		}

		Block coralBlock = CORAL_BLOCKS[random.nextInt(CORAL_BLOCKS.length)];
		BlockState block = coralBlock.defaultBlockState();
		int height = tall ? 4 + random.nextInt(3) : 1 + random.nextInt(3); // tall: 4–6, normal: 1–3
		boolean placed = false;
		BlockPos.MutableBlockPos cursor = new BlockPos.MutableBlockPos();

		cursor.set(floor.getX() + dx, localFloorY, floor.getZ() + dz);
		if (SeafloorFanFeature.isSeafloor(level.getBlockState(cursor))) {
			level.setBlock(cursor, block, 3);
			placed = true;
		}

		for (int y = 1; y < height; y++) {
			cursor.set(floor.getX() + dx, localFloorY + y, floor.getZ() + dz);
			if (!level.getFluidState(cursor).is(Fluids.WATER) || !level.getBlockState(cursor).canBeReplaced()) {
				break;
			}
			level.setBlock(cursor, block, 3);
			placed = true;

			if (y <= (tall ? 2 : 1) && random.nextFloat() < (tall ? 0.7f : 0.45f)) {
				var dir = net.minecraft.core.Direction.Plane.HORIZONTAL.getRandomDirection(random);
				BlockPos side = cursor.relative(dir);
				if (level.getFluidState(side).is(Fluids.WATER) && level.getBlockState(side).canBeReplaced()) {
					level.setBlock(side, block, 3);
					if (tall && random.nextFloat() < 0.4f && canPlaceAbove(level, side)) {
						level.setBlock(side.above(), block, 3);
					}
				}
			}
		}

		if (placed) {
			cursor.set(floor.getX() + dx, localFloorY + height, floor.getZ() + dz);
			if (level.getFluidState(cursor).is(Fluids.WATER) && level.getBlockState(cursor).canBeReplaced()) {
				if (random.nextBoolean()) {
					placeWaterlogged(level, cursor, CORAL_PLANTS[random.nextInt(CORAL_PLANTS.length)], random);
				} else {
					placeWaterlogged(level, cursor, SeafloorFanFeature.randomFan(random), random);
				}
			}
		}

		return placed;
	}

	private static boolean canPlaceAbove(WorldGenLevel level, BlockPos pos) {
		BlockPos above = pos.above();
		return level.getFluidState(above).is(Fluids.WATER) && level.getBlockState(above).canBeReplaced();
	}

	private static boolean placeCoralPlant(WorldGenLevel level, BlockPos floor, int radius, RandomSource random) {
		int dx = random.nextInt(radius * 2 + 1) - radius;
		int dz = random.nextInt(radius * 2 + 1) - radius;
		int localFloorY = findLocalFloorY(level, floor.getX() + dx, floor.getY(), floor.getZ() + dz);
		if (localFloorY == Integer.MIN_VALUE) {
			return false;
		}
		BlockPos above = new BlockPos(floor.getX() + dx, localFloorY + 1, floor.getZ() + dz);
		return placeWaterlogged(level, above, CORAL_PLANTS[random.nextInt(CORAL_PLANTS.length)], random);
	}

	private static boolean placeWaterlogged(WorldGenLevel level, BlockPos pos, Block block, RandomSource random) {
		if (!level.getFluidState(pos).is(Fluids.WATER) || !level.getBlockState(pos).canBeReplaced()) {
			return false;
		}
		BlockState state = block.defaultBlockState();
		if (state.hasProperty(BlockStateProperties.WATERLOGGED)) {
			state = state.setValue(BlockStateProperties.WATERLOGGED, true);
		}
		level.setBlock(pos, state, 3);
		return true;
	}

	private static void paintFloorPatch(WorldGenLevel level, BlockPos floor, int radius, RandomSource random) {
		BlockPos.MutableBlockPos cursor = new BlockPos.MutableBlockPos();
		BlockState gravel = Blocks.GRAVEL.defaultBlockState();
		BlockState sand = Blocks.SAND.defaultBlockState();
		for (int dx = -radius; dx <= radius; dx++) {
			for (int dz = -radius; dz <= radius; dz++) {
				if (dx * dx + dz * dz > radius * radius + random.nextInt(2)) {
					continue;
				}
				int localFloorY = findLocalFloorY(level, floor.getX() + dx, floor.getY(), floor.getZ() + dz);
				if (localFloorY == Integer.MIN_VALUE) {
					continue;
				}
				cursor.set(floor.getX() + dx, localFloorY, floor.getZ() + dz);
				BlockState existing = level.getBlockState(cursor);
				if (!SeafloorFanFeature.isSeafloor(existing)) {
					continue;
				}
				if (random.nextFloat() < 0.22f) {
					if (!existing.is(Blocks.SAND)) {
						level.setBlock(cursor, sand, 3);
					}
				} else if (random.nextFloat() < 0.7f && !existing.is(Blocks.GRAVEL)) {
					level.setBlock(cursor, gravel, 3);
				}
			}
		}
	}

	private static void placeSeaPickle(WorldGenLevel level, BlockPos floor, int radius, RandomSource random) {
		int dx = random.nextInt(radius * 2 + 1) - radius;
		int dz = random.nextInt(radius * 2 + 1) - radius;
		int localFloorY = findLocalFloorY(level, floor.getX() + dx, floor.getY(), floor.getZ() + dz);
		if (localFloorY == Integer.MIN_VALUE) {
			return;
		}
		BlockPos above = new BlockPos(floor.getX() + dx, localFloorY + 1, floor.getZ() + dz);
		if (!level.getFluidState(above).is(Fluids.WATER) || !level.getBlockState(above).canBeReplaced()) {
			return;
		}
		BlockState pickle = Blocks.SEA_PICKLE.defaultBlockState()
			.setValue(BlockStateProperties.PICKLES, 1)
			.setValue(BlockStateProperties.WATERLOGGED, true);
		level.setBlock(above, pickle, 3);
	}

	private static int findLocalFloorY(WorldGenLevel level, int x, int originFloorY, int z) {
		BlockPos.MutableBlockPos cursor = new BlockPos.MutableBlockPos();
		for (int y = originFloorY + 4; y >= originFloorY - 4; y--) {
			cursor.set(x, y, z);
			if (SeafloorFanFeature.isSeafloor(level.getBlockState(cursor)) && level.getFluidState(cursor.above()).is(Fluids.WATER)) {
				return y;
			}
		}
		return Integer.MIN_VALUE;
	}
}
