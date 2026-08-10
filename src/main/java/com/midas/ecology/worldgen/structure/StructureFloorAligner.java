package com.midas.ecology.worldgen.structure;

import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.LevelHeightAccessor;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.levelgen.RandomState;
import net.minecraft.world.level.levelgen.structure.Structure;

/**
 * Aligns structures that hardcode a near-sea-level floor (e.g. ocean monuments)
 * to the sampled ocean floor. Call-scoped via {@link #runWithMinY(int, Runnable)}
 * so pending state is always cleared.
 *
 * <p>Vanilla ocean monuments hardcode their bounding-box floor at Y 39. With
 * Ecology deep basins that creates huge pillars; this component samples
 * {@link Heightmap.Types#OCEAN_FLOOR_WG} and supplies a replacement min Y while
 * pieces are constructed.
 */
public final class StructureFloorAligner {
	/** Vanilla {@code MonumentBuilding} min Y. */
	public static final int VANILLA_MONUMENT_MIN_Y = 39;
	/** Vanilla monument bounding-box height (Y span). */
	public static final int MONUMENT_HEIGHT = 23;
	/** Sit this many blocks above the sampled ocean floor. */
	public static final int FLOOR_CLEARANCE = 8;

	private static final ThreadLocal<Integer> PENDING_MIN_Y = new ThreadLocal<>();

	private StructureFloorAligner() {
	}

	/**
	 * Runs {@code action} with a pending min Y, always clearing afterward.
	 */
	public static void runWithMinY(int minY, Runnable action) {
		PENDING_MIN_Y.set(minY);
		try {
			action.run();
		} finally {
			PENDING_MIN_Y.remove();
		}
	}

	/** Replaces vanilla min Y when a seafloor placement is pending on this thread. */
	public static int resolveMinY(int vanillaMinY) {
		Integer pending = PENDING_MIN_Y.get();
		return pending != null ? pending : vanillaMinY;
	}

	public static int monumentTargetMinY(Structure.GenerationContext context) {
		ChunkPos chunkPos = context.chunkPos();
		return monumentTargetMinY(
			context.chunkGenerator(),
			context.heightAccessor(),
			context.randomState(),
			chunkPos.getMiddleBlockX(),
			chunkPos.getMiddleBlockZ()
		);
	}

	public static int monumentTargetMinY(
		ChunkGenerator generator,
		LevelHeightAccessor heightAccessor,
		RandomState randomState,
		int x,
		int z
	) {
		int floorY = generator.getBaseHeight(
			x,
			z,
			Heightmap.Types.OCEAN_FLOOR_WG,
			heightAccessor,
			randomState
		);
		int seaLevel = generator.getSeaLevel();
		int maxMinY = seaLevel - MONUMENT_HEIGHT - 2;
		int minMinY = heightAccessor.getMinY() + 1;
		return Math.max(minMinY, Math.min(floorY + FLOOR_CLEARANCE, maxMinY));
	}
}
