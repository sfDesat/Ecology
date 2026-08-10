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

	// cold_ocean
	public static final ResourceKey<Biome> KELP_FOREST = key("kelp_forest");
	public static final ResourceKey<Biome> COLD_EELGRASS = key("cold_eelgrass");

	// ocean
	public static final ResourceKey<Biome> SEAGRASS_MEADOW = key("seagrass_meadow");
	public static final ResourceKey<Biome> SAND_WAVE_FIELD = key("sand_wave_field");
	public static final ResourceKey<Biome> TEMPERATE_ROCKY_REEF = key("temperate_rocky_reef");

	// lukewarm_ocean
	public static final ResourceKey<Biome> SUBTROPICAL_SEAGRASS = key("subtropical_seagrass");
	public static final ResourceKey<Biome> PATCH_REEF = key("patch_reef");
	public static final ResourceKey<Biome> SOFT_CORAL_GARDEN = key("soft_coral_garden");

	// warm_ocean
	public static final ResourceKey<Biome> CORAL_REEF = key("coral_reef");
	public static final ResourceKey<Biome> LAGOON = key("lagoon");
	public static final ResourceKey<Biome> TROPICAL_SEAGRASS = key("tropical_seagrass");

	// pelagic layers (3D stack)
	public static final ResourceKey<Biome> OPEN_OCEAN = key("open_ocean");
	/** Deep basin floor / lower column — also the horizontal marker remapped by Y. */
	public static final ResourceKey<Biome> DEEP_BASIN = key("deep_basin");

	/** Habitat pockets only (excludes pelagic layers). */
	public static final List<ResourceKey<Biome>> POCKETS = List.of(
		ICE_EDGE,
		POLYNYA,
		SYMPAGIC_ZONE,
		KELP_FOREST,
		COLD_EELGRASS,
		SEAGRASS_MEADOW,
		SAND_WAVE_FIELD,
		TEMPERATE_ROCKY_REEF,
		SUBTROPICAL_SEAGRASS,
		PATCH_REEF,
		SOFT_CORAL_GARDEN,
		CORAL_REEF,
		LAGOON,
		TROPICAL_SEAGRASS
	);

	public static final List<ResourceKey<Biome>> ALL = List.of(
		ICE_EDGE,
		POLYNYA,
		SYMPAGIC_ZONE,
		KELP_FOREST,
		COLD_EELGRASS,
		SEAGRASS_MEADOW,
		SAND_WAVE_FIELD,
		TEMPERATE_ROCKY_REEF,
		SUBTROPICAL_SEAGRASS,
		PATCH_REEF,
		SOFT_CORAL_GARDEN,
		CORAL_REEF,
		LAGOON,
		TROPICAL_SEAGRASS,
		OPEN_OCEAN,
		DEEP_BASIN
	);

	private static ResourceKey<Biome> key(String path) {
		return ResourceKey.create(Registries.BIOME, EcologyMod.id(path));
	}
}
