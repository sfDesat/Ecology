package com.midas.ecology.worldgen.feature;

import com.mojang.serialization.Codec;
import net.minecraft.core.BlockPos;
import net.minecraft.tags.BlockTags;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.minecraft.world.level.levelgen.feature.FeaturePlaceContext;
import net.minecraft.world.level.material.Fluids;

/**
 * Places a tapered 3D rock mound on the ocean floor, replacing water above the substrate.
 * Height is clamped so the stack stays fully underwater. Each column snaps to the local
 * seafloor and embeds a few blocks down so wide pillars do not float over dips.
 */
public class UnderwaterRockFeature extends Feature<UnderwaterRockConfiguration> {
	/** Keep at least this many water blocks above the rock top. */
	private static final int SURFACE_CLEARANCE = 2;
	/** How many seafloor blocks the base replaces, so mounds sit into the ground. */
	private static final int EMBED_DEPTH = 2;
	/** How far above/below the origin floor a column may snap when terrain is uneven. */
	private static final int LOCAL_FLOOR_SLACK = 4;

	public UnderwaterRockFeature(Codec<UnderwaterRockConfiguration> codec) {
		super(codec);
	}

	@Override
	public boolean place(FeaturePlaceContext<UnderwaterRockConfiguration> context) {
		WorldGenLevel level = context.level();
		RandomSource random = context.random();
		UnderwaterRockConfiguration config = context.config();
		BlockPos origin = context.origin();

		// Snap to the top of the seafloor (placement heightmap may sit on the solid floor block).
		BlockPos.MutableBlockPos floor = origin.mutable();
		if (!isSoftSeafloor(level.getBlockState(floor))) {
			if (isSoftSeafloor(level.getBlockState(floor.below()))) {
				floor.move(0, -1, 0);
			} else {
				return false;
			}
		}

		// Need water above so this is actually underwater.
		if (!level.getFluidState(floor.above()).is(Fluids.WATER)) {
			return false;
		}

		int waterDepth = countWaterAbove(level, floor);
		int maxHeight = waterDepth - SURFACE_CLEARANCE;
		if (maxHeight < 1) {
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

				// Place embedDepth into the floor, then height blocks above it.
				int baseY = localFloorY - EMBED_DEPTH + 1;
				int topY = localFloorY + height;

				for (int y = baseY; y <= topY; y++) {
					int aboveFloor = y - localFloorY;
					// Full radius through the embed and first above-floor layer; taper after that.
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

		return placed;
	}

	/**
	 * Finds the soft seafloor under a column near the origin floor height.
	 * Returns {@link Integer#MIN_VALUE} when none is nearby (skip that column to avoid floating rock).
	 */
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

	/**
	 * Counts consecutive water blocks directly above the seafloor solid.
	 */
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
			|| state.is(net.minecraft.world.level.block.Blocks.GRAVEL)
			|| state.is(net.minecraft.world.level.block.Blocks.CLAY)
			|| state.is(net.minecraft.world.level.block.Blocks.STONE)
			|| state.is(net.minecraft.world.level.block.Blocks.COBBLESTONE)
			|| state.is(net.minecraft.world.level.block.Blocks.DIRT)
			|| state.is(net.minecraft.world.level.block.Blocks.COARSE_DIRT)
			|| state.is(net.minecraft.world.level.block.Blocks.MOSS_BLOCK);
	}

	private static boolean canReplace(BlockState state, boolean water) {
		return water || state.isAir() || state.canBeReplaced();
	}
}
