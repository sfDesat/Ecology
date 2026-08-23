package com.midas.ecology.worldgen;

import com.midas.ecology.EcologyMod;
import java.util.List;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.biome.Biome;

/**
 * Registry keys for Ecology ocean biomes (habitat pockets + pelagic layers).
 * Content is datapack JSON.
 */
public final class EcologyBiomes {
	private EcologyBiomes() {
	}

	// frozen_ocean
	public static final ResourceKey<Biome> ICE_EDGE = key("ice_edge");
	public static final ResourceKey<Biome> POLYNYA = key("polynya");
	public static final ResourceKey<Biome> SYMPAGIC_ZONE = key("sympagic_zone");
	public static final ResourceKey<Biome> FROZEN_COASTAL_SHALLOWS = key("frozen_coastal_shallows");

	// cold_ocean
	public static final ResourceKey<Biome> KELP_FOREST = key("kelp_forest");
	public static final ResourceKey<Biome> COLD_EELGRASS = key("cold_eelgrass");
	public static final ResourceKey<Biome> COLD_COASTAL_SHALLOWS = key("cold_coastal_shallows");

	// ocean
	public static final ResourceKey<Biome> SEAGRASS_MEADOW = key("seagrass_meadow");
	public static final ResourceKey<Biome> SAND_WAVE_FIELD = key("sand_wave_field");
	public static final ResourceKey<Biome> TEMPERATE_ROCKY_REEF = key("temperate_rocky_reef");
	public static final ResourceKey<Biome> TEMPERATE_COASTAL_SHALLOWS = key("temperate_coastal_shallows");

	// lukewarm_ocean
	public static final ResourceKey<Biome> SUBTROPICAL_SEAGRASS = key("subtropical_seagrass");
	public static final ResourceKey<Biome> PATCH_REEF = key("patch_reef");
	public static final ResourceKey<Biome> SOFT_CORAL_GARDEN = key("soft_coral_garden");
	public static final ResourceKey<Biome> LUKEWARM_COASTAL_SHALLOWS = key("lukewarm_coastal_shallows");

	// warm_ocean
	public static final ResourceKey<Biome> CORAL_REEF = key("coral_reef");
	public static final ResourceKey<Biome> LAGOON = key("lagoon");
	public static final ResourceKey<Biome> TROPICAL_SEAGRASS = key("tropical_seagrass");
	public static final ResourceKey<Biome> TROPICAL_COASTAL_SHALLOWS = key("tropical_coastal_shallows");

	// pelagic layers (3D stack)
	public static final ResourceKey<Biome> OPEN_OCEAN = key("open_ocean");
	/** Deep basin floor / lower column — also the horizontal marker remapped by Y. */
	public static final ResourceKey<Biome> DEEP_BASIN = key("deep_basin");

	/** Habitat pockets only (excludes pelagic layers). */
	public static final List<ResourceKey<Biome>> POCKETS = List.of(
		ICE_EDGE,
		POLYNYA,
		SYMPAGIC_ZONE,
		FROZEN_COASTAL_SHALLOWS,
		KELP_FOREST,
		COLD_EELGRASS,
		COLD_COASTAL_SHALLOWS,
		SEAGRASS_MEADOW,
		SAND_WAVE_FIELD,
		TEMPERATE_ROCKY_REEF,
		TEMPERATE_COASTAL_SHALLOWS,
		SUBTROPICAL_SEAGRASS,
		PATCH_REEF,
		SOFT_CORAL_GARDEN,
		LUKEWARM_COASTAL_SHALLOWS,
		CORAL_REEF,
		LAGOON,
		TROPICAL_SEAGRASS,
		TROPICAL_COASTAL_SHALLOWS
	);

	public static final List<ResourceKey<Biome>> ALL = List.of(
		ICE_EDGE,
		POLYNYA,
		SYMPAGIC_ZONE,
		FROZEN_COASTAL_SHALLOWS,
		KELP_FOREST,
		COLD_EELGRASS,
		COLD_COASTAL_SHALLOWS,
		SEAGRASS_MEADOW,
		SAND_WAVE_FIELD,
		TEMPERATE_ROCKY_REEF,
		TEMPERATE_COASTAL_SHALLOWS,
		SUBTROPICAL_SEAGRASS,
		PATCH_REEF,
		SOFT_CORAL_GARDEN,
		LUKEWARM_COASTAL_SHALLOWS,
		CORAL_REEF,
		LAGOON,
		TROPICAL_SEAGRASS,
		TROPICAL_COASTAL_SHALLOWS,
		OPEN_OCEAN,
		DEEP_BASIN
	);

	private static ResourceKey<Biome> key(String path) {
		return ResourceKey.create(Registries.BIOME, EcologyMod.id(path));
	}
}
