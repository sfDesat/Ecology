package com.midas.ecology.worldgen.seafloor;

import net.minecraft.core.BlockPos;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.Fluids;

/**
 * Shared tapered rock-mound placement used by underwater rock features.
 * Callers supply radius/height/state and optional post-pass (e.g. coral encrust).
 */
public final class RockMoundPlacer {
	public static final int SURFACE_CLEARANCE = 2;
	public static final int EMBED_DEPTH = 2;

	private RockMoundPlacer() {
	}

	/**
	 * Places a tapered mound. Returns {@code true} if at least one block was set.
	 */
	public static boolean place(
		WorldGenLevel level,
		BlockPos floor,
		int radius,
		int height,
		BlockState rock,
		RandomSource random
	) {
		boolean placed = false;
		BlockPos.MutableBlockPos cursor = new BlockPos.MutableBlockPos();

		for (int dx = -radius; dx <= radius; dx++) {
			for (int dz = -radius; dz <= radius; dz++) {
				int distSq = dx * dx + dz * dz;
				if (distSq > radius * radius + random.nextInt(2)) {
					continue;
				}

				int localFloorY = SeafloorHelpers.findLocalSoftFloorY(
					level,
					floor.getX() + dx,
					floor.getY(),
					floor.getZ() + dz
				);
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
						if (SeafloorHelpers.isSoftSeafloor(existing)) {
							level.setBlock(cursor, rock, 3);
							placed = true;
						}
					} else if (SeafloorHelpers.canReplaceWaterOrAir(existing, water)) {
						level.setBlock(cursor, rock, 3);
						placed = true;
					}
				}
			}
		}

		return placed;
	}
}
