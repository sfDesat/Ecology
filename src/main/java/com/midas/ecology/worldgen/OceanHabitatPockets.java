package com.midas.ecology.worldgen;

import java.util.List;
import java.util.Map;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.biome.Biomes;
import net.minecraft.world.level.biome.Climate;

/**
 * Advanced shallow-ocean ParameterPoints: habitat pockets are constrained to
 * non-overlapping continentalness bands that roughly match BIOMES.md seafloor
 * depth targets. Parent bases keep weirdness [-1, 0] across the full shallow
 * span; fill placements claim weirdness (0, 1] on shelf depths with no pocket.
 *
 * <p>Frozen oceans are an exception: ice habitats use a shared wide shelf span
 * and weirdness niches so polynyas / ice edges / sympagic zones form blob-like
 * areas instead of depth-parallel coastal strips.
 *
 * <p>Continentalness within vanilla shallow ocean {@code [-0.455, -0.19]}
 * (more negative → deeper via {@code ecology:ocean_depth_control}):
 * <pre>
 *   SHORE   [-0.22, -0.19]  ~0–10 blocks
 *   INNER   [-0.28, -0.22]  ~8–15
 *   MID     [-0.34, -0.28]  ~15–25
 *   OUTER   [-0.40, -0.34]  ~25–35
 *   DEEP    [-0.455, -0.40] ~35–45
 * </pre>
 */
public final class OceanHabitatPockets {
	/** Vanilla shallow-ocean continentalness from {@code OverworldBiomeBuilder}. */
	public static final Climate.Parameter SHALLOW_OCEAN_CONTINENTALNESS = Climate.Parameter.span(-0.455f, -0.19f);

	public static final Climate.Parameter PARENT_WEIRDNESS = Climate.Parameter.span(-1.0f, 0.0f);
	public static final Climate.Parameter POCKET_WEIRDNESS = Climate.Parameter.span(0.0f, 1.0f);

	// --- Shared shelf bands (aligned with ocean_depth_control) ---

	/** ~0–10 */
	public static final Climate.Parameter SHORE = Climate.Parameter.span(-0.22f, -0.19f);
	/** ~0–15 */
	public static final Climate.Parameter SHORE_INNER = Climate.Parameter.span(-0.28f, -0.19f);
	/** ~15–45 (kelp forest wide band) */
	public static final Climate.Parameter MID_TO_DEEP = Climate.Parameter.span(-0.455f, -0.28f);

	// --- Frozen: weirdness niches on a wide shelf (blob patches, not depth strips) ---

	/**
	 * Wide shallow–mid shelf for ice habitats. Continentalness bands alone paint
	 * coastal-parallel strips; weirdness slices on this span yield area-like pockets.
	 */
	private static final Climate.Parameter FROZEN_HABITAT_SHELF = Climate.Parameter.span(-0.40f, -0.19f);
	/** Outer shelf beyond habitat pockets — always plain frozen ocean. */
	private static final Climate.Parameter FROZEN_OUTER_FILL = Climate.Parameter.span(-0.455f, -0.40f);

	/** Remaining positive weirdness → plain frozen ocean between ice-habitat blobs. */
	private static final Climate.Parameter FROZEN_FILL_WEIRDNESS_A = Climate.Parameter.span(0.0f, 0.14f);
	private static final Climate.Parameter FROZEN_SYMPAGIC_WEIRDNESS = Climate.Parameter.span(0.14f, 0.34f);
	private static final Climate.Parameter FROZEN_ICE_EDGE_WEIRDNESS = Climate.Parameter.span(0.34f, 0.54f);
	/** Mid size — between the last “too big” and earlier smaller niche. */
	private static final Climate.Parameter FROZEN_POLYNYA_WEIRDNESS = Climate.Parameter.span(0.54f, 0.76f);
	private static final Climate.Parameter FROZEN_FILL_WEIRDNESS_B = Climate.Parameter.span(0.76f, 1.0f);

	// Contiguous non-overlapping pocket slices (per parent). Shared edges only.

	/** Cold: Eelgrass ~0–10 */
	private static final Climate.Parameter COLD_EELGRASS_BAND = SHORE_INNER;
	/** Cold: Kelp Forest ~15–35 */
	private static final Climate.Parameter COLD_KELP_BAND = MID_TO_DEEP;

	/** Temperate: Seagrass Meadow ~0–15 */
	private static final Climate.Parameter TEMP_MEADOW = SHORE_INNER;
	/** Temperate: Rocky Reef ~15–30 */
	private static final Climate.Parameter TEMP_REEF = Climate.Parameter.span(-0.40f, -0.28f);
	/** Temperate: Sand Wave Field ~30–45 */
	private static final Climate.Parameter TEMP_SAND = Climate.Parameter.span(-0.455f, -0.40f);

	/** Lukewarm: Subtropical Seagrass ~0–10 */
	private static final Climate.Parameter LUKE_SEAGRASS = Climate.Parameter.span(-0.25f, -0.19f);
	/** Lukewarm: Patch Reef ~10–20 */
	private static final Climate.Parameter LUKE_PATCH = Climate.Parameter.span(-0.34f, -0.25f);
	/** Lukewarm: Soft Coral Garden ~25–45 */
	private static final Climate.Parameter LUKE_SOFT = Climate.Parameter.span(-0.455f, -0.34f);

	/** Warm: Lagoon ~0–8 */
	private static final Climate.Parameter WARM_LAGOON = SHORE;
	/** Warm: Tropical Seagrass ~0–12 */
	private static final Climate.Parameter WARM_SEAGRASS = Climate.Parameter.span(-0.28f, -0.22f);
	/** Warm: Coral Reef ~10–20 */
	private static final Climate.Parameter WARM_CORAL = Climate.Parameter.span(-0.36f, -0.28f);
	/** Warm outer shelf fill (no reef pocket past ~20–25). */
	private static final Climate.Parameter WARM_OUTER_FILL = Climate.Parameter.span(-0.455f, -0.36f);

	private static final Map<ResourceKey<Biome>, List<Placement>> PLACEMENTS_BY_PARENT = Map.of(
		Biomes.FROZEN_OCEAN,
		List.of(
			placement(Biomes.FROZEN_OCEAN, SHALLOW_OCEAN_CONTINENTALNESS, PARENT_WEIRDNESS),
			placement(Biomes.FROZEN_OCEAN, FROZEN_HABITAT_SHELF, FROZEN_FILL_WEIRDNESS_A),
			placement(Biomes.FROZEN_OCEAN, FROZEN_HABITAT_SHELF, FROZEN_FILL_WEIRDNESS_B),
			placement(Biomes.FROZEN_OCEAN, FROZEN_OUTER_FILL, POCKET_WEIRDNESS),
			placement(EcologyBiomes.SYMPAGIC_ZONE, FROZEN_HABITAT_SHELF, FROZEN_SYMPAGIC_WEIRDNESS),
			placement(EcologyBiomes.ICE_EDGE, FROZEN_HABITAT_SHELF, FROZEN_ICE_EDGE_WEIRDNESS),
			placement(EcologyBiomes.POLYNYA, FROZEN_HABITAT_SHELF, FROZEN_POLYNYA_WEIRDNESS)
		),
		Biomes.COLD_OCEAN,
		List.of(
			placement(Biomes.COLD_OCEAN, SHALLOW_OCEAN_CONTINENTALNESS, PARENT_WEIRDNESS),
			placement(EcologyBiomes.COLD_EELGRASS, COLD_EELGRASS_BAND, POCKET_WEIRDNESS),
			placement(EcologyBiomes.KELP_FOREST, COLD_KELP_BAND, POCKET_WEIRDNESS)
		),
		Biomes.OCEAN,
		List.of(
			placement(Biomes.OCEAN, SHALLOW_OCEAN_CONTINENTALNESS, PARENT_WEIRDNESS),
			placement(EcologyBiomes.SEAGRASS_MEADOW, TEMP_MEADOW, POCKET_WEIRDNESS),
			placement(EcologyBiomes.TEMPERATE_ROCKY_REEF, TEMP_REEF, POCKET_WEIRDNESS),
			placement(EcologyBiomes.SAND_WAVE_FIELD, TEMP_SAND, POCKET_WEIRDNESS)
		),
		Biomes.LUKEWARM_OCEAN,
		List.of(
			placement(Biomes.LUKEWARM_OCEAN, SHALLOW_OCEAN_CONTINENTALNESS, PARENT_WEIRDNESS),
			placement(EcologyBiomes.SUBTROPICAL_SEAGRASS, LUKE_SEAGRASS, POCKET_WEIRDNESS),
			placement(EcologyBiomes.PATCH_REEF, LUKE_PATCH, POCKET_WEIRDNESS),
			placement(EcologyBiomes.SOFT_CORAL_GARDEN, LUKE_SOFT, POCKET_WEIRDNESS)
		),
		Biomes.WARM_OCEAN,
		List.of(
			placement(Biomes.WARM_OCEAN, SHALLOW_OCEAN_CONTINENTALNESS, PARENT_WEIRDNESS),
			// Outer warm shelf: deep floor → pelagic stack (Open over Deep), not plain warm_ocean
			placement(EcologyBiomes.DEEP_OCEAN, WARM_OUTER_FILL, POCKET_WEIRDNESS),
			placement(EcologyBiomes.LAGOON, WARM_LAGOON, POCKET_WEIRDNESS),
			placement(EcologyBiomes.TROPICAL_SEAGRASS, WARM_SEAGRASS, POCKET_WEIRDNESS),
			placement(EcologyBiomes.CORAL_REEF, WARM_CORAL, POCKET_WEIRDNESS)
		)
	);

	private OceanHabitatPockets() {
	}

	public static boolean isShallowOceanContinentalness(Climate.Parameter continentalness) {
		return SHALLOW_OCEAN_CONTINENTALNESS.equals(continentalness);
	}

	public static List<Placement> placementsFor(ResourceKey<Biome> parent) {
		return PLACEMENTS_BY_PARENT.get(parent);
	}

	private static Placement placement(ResourceKey<Biome> biome, Climate.Parameter continentalness, Climate.Parameter weirdness) {
		return new Placement(biome, continentalness, weirdness);
	}

	/**
	 * One MultiNoise surface registration (emitted at climate depth 0 and 1).
	 */
	public record Placement(ResourceKey<Biome> biome, Climate.Parameter continentalness, Climate.Parameter weirdness) {
	}
}
