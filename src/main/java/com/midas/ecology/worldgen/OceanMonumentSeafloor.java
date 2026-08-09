package com.midas.ecology.worldgen;

import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.LevelHeightAccessor;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.levelgen.RandomState;
import net.minecraft.world.level.levelgen.structure.Structure;

/**
 * Vanilla ocean monuments hardcode their bounding-box floor at Y 39 (near sea
 * level) and stretch prismarine pillars down to the seafloor. With Ecology's
 * deep basins that creates huge pillars and leaves the monument body in
 * {@link EcologyBiomes#OPEN_OCEAN}.
 *
 * <p>Height is sampled once at the structure chunk's center
 * ({@link Heightmap.Types#OCEAN_FLOOR_WG}), then raised by
 * {@link #FLOOR_CLEARANCE} so the monument clears uneven seafloor. That value
 * is applied as the monument min Y while pieces are constructed so the outer
 * shell, rooms, and elder spawns stay aligned.
 */
public final class OceanMonumentSeafloor {
	/** Vanilla {@code MonumentBuilding} min Y. */
	public static final int VANILLA_MIN_Y = 39;
	/** Vanilla monument bounding-box height (Y span). */
	public static final int HEIGHT = 23;
	/** Sit this many blocks above the sampled ocean floor. */
	public static final int FLOOR_CLEARANCE = 8;

	private static final ThreadLocal<Integer> PENDING_MIN_Y = new ThreadLocal<>();

	private OceanMonumentSeafloor() {
	}

	public static void setPendingMinY(int minY) {
		PENDING_MIN_Y.set(minY);
	}

	public static void clearPendingMinY() {
		PENDING_MIN_Y.remove();
	}

	/** Replaces vanilla Y 39 when a seafloor placement is pending on this thread. */
	public static int resolveMinY(int vanillaMinY) {
		Integer pending = PENDING_MIN_Y.get();
		return pending != null ? pending : vanillaMinY;
	}

	public static int targetMinY(Structure.GenerationContext context) {
		ChunkPos chunkPos = context.chunkPos();
		return targetMinY(
			context.chunkGenerator(),
			context.heightAccessor(),
			context.randomState(),
			chunkPos.getMiddleBlockX(),
			chunkPos.getMiddleBlockZ()
		);
	}

	public static int targetMinY(
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
		// Keep the roof a few blocks below sea level if the floor is unusually high.
		int maxMinY = seaLevel - HEIGHT - 2;
		int minMinY = heightAccessor.getMinY() + 1;
		return Math.max(minMinY, Math.min(floorY + FLOOR_CLEARANCE, maxMinY));
	}
}
