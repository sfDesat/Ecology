package com.midas.ecology.worldgen.climate;

import com.midas.ecology.worldgen.EcologyBiomes;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import net.minecraft.core.Holder;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.biome.BiomeSource;
import net.minecraft.world.level.biome.Biomes;

/**
 * Vertical Open Ocean / Deep Basin layering for deep-water columns.
 * Shelf habitat pockets are unchanged; only deep-basin horizontal markers remap by Y.
 */
public final class PelagicColumnResolver {
	public static final int SEA_LEVEL_QUART = 63 >> 2;
	/** Open Ocean occupies roughly sea level down through ~Y 12–15. */
	public static final int OPEN_OCEAN_MAX_BELOW_SEA_QUART = 11;

	private static final Set<ResourceKey<Biome>> VANILLA_DEEP_OCEANS = Set.of(
		Biomes.DEEP_FROZEN_OCEAN,
		Biomes.DEEP_COLD_OCEAN,
		Biomes.DEEP_OCEAN,
		Biomes.DEEP_LUKEWARM_OCEAN
	);

	private PelagicColumnResolver() {
	}

	public static boolean isVanillaDeepOcean(ResourceKey<Biome> biome) {
		return VANILLA_DEEP_OCEANS.contains(biome);
	}

	/** Horizontal deep-water marker that should receive Open/Deep Basin stacking. */
	public static boolean isDeepBasinColumn(ResourceKey<Biome> biome) {
		return biome == EcologyBiomes.DEEP_BASIN || isVanillaDeepOcean(biome);
	}

	public static ResourceKey<Biome> resolve(ResourceKey<Biome> horizontal, int quartY) {
		if (!isDeepBasinColumn(horizontal)) {
			return horizontal;
		}

		int belowSeaQuarts = SEA_LEVEL_QUART - quartY;
		if (belowSeaQuarts <= OPEN_OCEAN_MAX_BELOW_SEA_QUART) {
			return EcologyBiomes.OPEN_OCEAN;
		}

		return EcologyBiomes.DEEP_BASIN;
	}

	public static ResourceKey<Biome> replaceVanillaDeepOcean(ResourceKey<Biome> biome) {
		if (isVanillaDeepOcean(biome)) {
			return EcologyBiomes.DEEP_BASIN;
		}
		return biome;
	}

	/**
	 * Biomes that must appear in {@link BiomeSource#possibleBiomes()} for 3D remap,
	 * even when MultiNoise never selects them horizontally.
	 */
	public static Set<ResourceKey<Biome>> membershipBiomes() {
		return Set.of(EcologyBiomes.OPEN_OCEAN, EcologyBiomes.DEEP_BASIN);
	}

	/**
	 * Cached key→holder lookup. Pass the biome {@link Registry} from the mixin adapter
	 * so Open Ocean can be resolved without a fake MultiNoise climate point.
	 */
	public static final class HolderCache {
		private final Map<ResourceKey<Biome>, Holder<Biome>> byKey;

		private HolderCache(Map<ResourceKey<Biome>, Holder<Biome>> byKey) {
			this.byKey = byKey;
		}

		public static HolderCache forSource(BiomeSource source, Registry<Biome> biomeRegistry) {
			Map<ResourceKey<Biome>, Holder<Biome>> map = new HashMap<>();

			for (Holder<Biome> holder : source.possibleBiomes()) {
				holder.unwrapKey().ifPresent(key -> map.put(key, holder));
			}

			for (ResourceKey<Biome> key : membershipBiomes()) {
				map.computeIfAbsent(key, k -> biomeRegistry.get(k).orElse(null));
			}
			map.entrySet().removeIf(e -> e.getValue() == null);

			return new HolderCache(map);
		}

		public Holder<Biome> get(ResourceKey<Biome> key) {
			return byKey.get(key);
		}
	}
}
