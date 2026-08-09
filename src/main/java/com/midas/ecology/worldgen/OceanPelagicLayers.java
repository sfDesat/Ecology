package com.midas.ecology.worldgen;

import java.util.Set;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.biome.Biomes;

/**
 * Vertical Open Ocean / Deep Ocean layering in deep-basin columns (vanilla
 * {@code deep_*} bands, replaced by {@link EcologyBiomes#DEEP_OCEAN}) and on
 * the outer warm-ocean shelf where the seafloor drops deep. Shelf pockets
 * (reefs, meadows, kelp, etc.) are unchanged.
 */
public final class OceanPelagicLayers {
	/** Sea level in biome quart coordinates ({@code getNoiseBiome} space). */
	public static final int SEA_LEVEL_QUART = 63 >> 2;
	/** Deep Ocean begins below ~Y 12–15 (open above that in pelagic columns). */
	public static final int OPEN_OCEAN_MAX_BELOW_SEA_QUART = 11;

	private static final Set<ResourceKey<Biome>> VANILLA_DEEP_OCEANS = Set.of(
		Biomes.DEEP_FROZEN_OCEAN,
		Biomes.DEEP_COLD_OCEAN,
		Biomes.DEEP_OCEAN,
		Biomes.DEEP_LUKEWARM_OCEAN
	);

	private OceanPelagicLayers() {
	}

	public static boolean isVanillaDeepOcean(ResourceKey<Biome> biome) {
		return VANILLA_DEEP_OCEANS.contains(biome);
	}

	public static boolean isDeepBasin(ResourceKey<Biome> biome) {
		return biome == EcologyBiomes.DEEP_OCEAN || isVanillaDeepOcean(biome);
	}

	/**
	 * Maps a deep-basin horizontal biome to Open Ocean (upper water) or Deep Ocean
	 * (lower water / floor). Shelf biomes pass through unchanged.
	 */
	public static ResourceKey<Biome> resolve(ResourceKey<Biome> horizontal, int quartY) {
		if (!isDeepBasin(horizontal)) {
			return horizontal;
		}

		int belowSeaQuarts = SEA_LEVEL_QUART - quartY;
		if (belowSeaQuarts <= OPEN_OCEAN_MAX_BELOW_SEA_QUART) {
			return EcologyBiomes.OPEN_OCEAN;
		}

		return EcologyBiomes.DEEP_OCEAN;
	}

	public static ResourceKey<Biome> replaceVanillaDeepOcean(ResourceKey<Biome> biome) {
		if (isVanillaDeepOcean(biome)) {
			return EcologyBiomes.DEEP_OCEAN;
		}
		return biome;
	}
}
