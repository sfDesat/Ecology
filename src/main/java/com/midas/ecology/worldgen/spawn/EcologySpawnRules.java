package com.midas.ecology.worldgen.spawn;

import com.midas.ecology.worldgen.EcologyBiomes;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.resources.ResourceKey;
import net.minecraft.tags.BiomeTags;
import net.minecraft.tags.FluidTags;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EntityTypes;
import net.minecraft.world.entity.animal.turtle.Turtle;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.ServerLevelAccessor;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.levelgen.Heightmap;

/**
 * Spawn helpers that vanilla biome JSON alone cannot express (cluster caps, turtle water rules).
 */
public final class EcologySpawnRules {
	/** Vanilla schooling fish cap is 8 — too small for open-ocean megaschools. */
	public static final int SCHOOLING_FISH_MAX_CLUSTER = 32;

	private EcologySpawnRules() {
	}

	/** Ocean / deep-ocean biomes where megaschools are intentional (not rivers/lakes). */
	public static boolean isOceanSchoolBiome(Holder<Biome> biome) {
		return biome.is(BiomeTags.IS_OCEAN) || biome.is(BiomeTags.IS_DEEP_OCEAN);
	}

	public static boolean isTurtleGrazingBiome(Holder<Biome> biome) {
		ResourceKey<Biome> key = biome.unwrapKey().orElse(null);
		return key != null
			&& (key.equals(EcologyBiomes.SUBTROPICAL_SEAGRASS)
				|| key.equals(EcologyBiomes.TROPICAL_SEAGRASS)
				|| key.equals(EcologyBiomes.LAGOON));
	}

	/**
	 * Water-column turtle spawn for seagrass / lagoon biomes. Vanilla turtles are
	 * {@code ON_GROUND} + sand-only ({@link Turtle#checkTurtleSpawnRules}), so biome
	 * {@code creature} entries never place them underwater.
	 */
	public static boolean checkGrazingTurtleWaterSpawn(
		EntityType<?> type,
		ServerLevelAccessor level,
		BlockPos pos
	) {
		if (type != EntityTypes.TURTLE) {
			return false;
		}
		if (!isTurtleGrazingBiome(level.getBiome(pos))) {
			return false;
		}
		if (!level.getFluidState(pos).is(FluidTags.WATER)) {
			return false;
		}
		// Prefer the lit upper water column (meadow / lagoon depths), not deep basin.
		int surfaceY = level.getHeight(Heightmap.Types.WORLD_SURFACE, pos.getX(), pos.getZ());
		if (pos.getY() < surfaceY - 16 || pos.getY() > surfaceY) {
			return false;
		}
		BlockPos above = pos.above();
		return level.getFluidState(above).is(FluidTags.WATER) || level.isEmptyBlock(above);
	}

	public static boolean checkGrazingTurtleWaterSpawn(
		EntityType<?> type,
		LevelAccessor level,
		BlockPos pos
	) {
		if (!(level instanceof ServerLevelAccessor server)) {
			return false;
		}
		return checkGrazingTurtleWaterSpawn(type, server, pos);
	}
}
