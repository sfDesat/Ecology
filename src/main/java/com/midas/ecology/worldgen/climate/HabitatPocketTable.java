package com.midas.ecology.worldgen.climate;

import com.midas.ecology.worldgen.EcologyBiomes;
import java.util.List;
import java.util.Map;
import java.util.Set;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.biome.Biomes;
import net.minecraft.world.level.biome.Climate;

/**
 * Habitat pocket placements for shallow-ocean parent biomes.
 * Keyed by parent biome — not by exact continentalness Parameter equality —
 * so vanilla span tweaks do not silently disable Ecology oceans.
 *
 * <p>Band ↔ depth mapping is aligned with {@code ecology:ocean_depth_control}
 * (see DENSITY.md / BIOMES.md):
 * <pre>
 *   SHORE   [-0.22, -0.19]  ~0–10 blocks
 *   INNER   [-0.28, -0.22]  ~8–15
 *   MID     [-0.34, -0.28]  ~15–25
 *   OUTER   [-0.40, -0.34]  ~25–35
 *   DEEP    [SHELF_EDGE, -0.40] ~35–45  (SHELF_EDGE = -0.48)
 * </pre>
 *
 * <p>Frozen oceans use weirdness niches on a wide shelf (blob habitats).
 * Warm outer shelf maps to {@link EcologyBiomes#DEEP_BASIN} so the pelagic
 * column resolver can stack Open over Deep Basin.
 *
 * <p>Continentalness: more negative = farther from land. Shallow shelf is
 * {@code [SHELF_EDGE, -0.19]}; deep basin is more oceanic than {@code SHELF_EDGE}.
 */
public final class HabitatPocketTable {
	/**
	 * Seaward edge of the continental shelf / shoreward edge of deep basin.
	 * Vanilla shallow oceans stop at {@code -0.455}; Ecology nudges the shelf
	 * slightly farther offshore (middle ground vs a larger {@code -0.50} push).
	 */
	public static final float SHELF_EDGE = -0.48f;

	public static final Climate.Parameter SHALLOW_OCEAN_CONTINENTALNESS = Climate.Parameter.span(SHELF_EDGE, -0.19f);

	public static final Climate.Parameter PARENT_WEIRDNESS = Climate.Parameter.span(-1.0f, 0.0f);
	public static final Climate.Parameter POCKET_WEIRDNESS = Climate.Parameter.span(0.0f, 1.0f);

	public static final Climate.Parameter SHORE = Climate.Parameter.span(-0.22f, -0.19f);
	public static final Climate.Parameter SHORE_INNER = Climate.Parameter.span(-0.28f, -0.19f);
	public static final Climate.Parameter MID_TO_DEEP = Climate.Parameter.span(SHELF_EDGE, -0.28f);

	private static final Climate.Parameter FROZEN_HABITAT_SHELF = Climate.Parameter.span(-0.40f, -0.19f);
	/** Outer frozen shelf fringe next to Deep Basin / Open Ocean. */
	private static final Climate.Parameter FROZEN_OUTER_FILL = Climate.Parameter.span(SHELF_EDGE, -0.40f);
	private static final Climate.Parameter FROZEN_FILL_WEIRDNESS_A = Climate.Parameter.span(0.0f, 0.14f);
	private static final Climate.Parameter FROZEN_SYMPAGIC_WEIRDNESS = Climate.Parameter.span(0.14f, 0.34f);
	private static final Climate.Parameter FROZEN_ICE_EDGE_WEIRDNESS = Climate.Parameter.span(0.34f, 0.54f);
	private static final Climate.Parameter FROZEN_POLYNYA_WEIRDNESS = Climate.Parameter.span(0.54f, 0.76f);
	private static final Climate.Parameter FROZEN_FILL_WEIRDNESS_B = Climate.Parameter.span(0.76f, 1.0f);

	private static final Climate.Parameter COLD_EELGRASS_BAND = SHORE_INNER;
	private static final Climate.Parameter COLD_KELP_BAND = MID_TO_DEEP;

	private static final Climate.Parameter TEMPERATE_MEADOW = SHORE_INNER;
	private static final Climate.Parameter TEMPERATE_REEF = Climate.Parameter.span(-0.40f, -0.28f);
	private static final Climate.Parameter TEMPERATE_SAND = Climate.Parameter.span(SHELF_EDGE, -0.40f);

	private static final Climate.Parameter LUKE_SEAGRASS = Climate.Parameter.span(-0.25f, -0.19f);
	private static final Climate.Parameter LUKE_PATCH = Climate.Parameter.span(-0.34f, -0.25f);
	private static final Climate.Parameter LUKE_SOFT = Climate.Parameter.span(SHELF_EDGE, -0.34f);

	private static final Climate.Parameter WARM_LAGOON = SHORE;
	private static final Climate.Parameter WARM_SEAGRASS = Climate.Parameter.span(-0.28f, -0.22f);
	private static final Climate.Parameter WARM_CORAL = Climate.Parameter.span(-0.36f, -0.28f);
	private static final Climate.Parameter WARM_OUTER_FILL = Climate.Parameter.span(SHELF_EDGE, -0.36f);

	private static final Map<ResourceKey<Biome>, List<Placement>> PLACEMENTS_BY_PARENT = Map.of(
		Biomes.FROZEN_OCEAN,
		List.of(
			placement(Biomes.FROZEN_OCEAN, SHALLOW_OCEAN_CONTINENTALNESS, PARENT_WEIRDNESS),
			placement(Biomes.FROZEN_OCEAN, FROZEN_HABITAT_SHELF, FROZEN_FILL_WEIRDNESS_A),
			placement(Biomes.FROZEN_OCEAN, FROZEN_HABITAT_SHELF, FROZEN_FILL_WEIRDNESS_B),
			placement(EcologyBiomes.ICE_EDGE, FROZEN_OUTER_FILL, POCKET_WEIRDNESS),
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
			placement(EcologyBiomes.SEAGRASS_MEADOW, TEMPERATE_MEADOW, POCKET_WEIRDNESS),
			placement(EcologyBiomes.TEMPERATE_ROCKY_REEF, TEMPERATE_REEF, POCKET_WEIRDNESS),
			placement(EcologyBiomes.SAND_WAVE_FIELD, TEMPERATE_SAND, POCKET_WEIRDNESS)
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
			placement(EcologyBiomes.DEEP_BASIN, WARM_OUTER_FILL, POCKET_WEIRDNESS),
			placement(EcologyBiomes.LAGOON, WARM_LAGOON, POCKET_WEIRDNESS),
			placement(EcologyBiomes.TROPICAL_SEAGRASS, WARM_SEAGRASS, POCKET_WEIRDNESS),
			placement(EcologyBiomes.CORAL_REEF, WARM_CORAL, POCKET_WEIRDNESS)
		)
	);

	private static final Set<ResourceKey<Biome>> SHALLOW_OCEAN_PARENTS = PLACEMENTS_BY_PARENT.keySet();

	private HabitatPocketTable() {
	}

	public static boolean isShallowOceanParent(ResourceKey<Biome> biome) {
		return SHALLOW_OCEAN_PARENTS.contains(biome);
	}

	public static List<Placement> placementsFor(ResourceKey<Biome> parent) {
		return PLACEMENTS_BY_PARENT.get(parent);
	}

	/**
	 * Trims vanilla deep-ocean continentalness so it stops at {@link #SHELF_EDGE}
	 * and does not overlap the expanded shallow shelf.
	 */
	public static Climate.Parameter clampDeepContinentalness(Climate.Parameter continentalness) {
		float min = Climate.unquantizeCoord(continentalness.min());
		float max = Climate.unquantizeCoord(continentalness.max());
		if (max <= SHELF_EDGE) {
			return continentalness;
		}
		// Fully shoreward of the shelf edge: keep a single edge sample so deep
		// markers still abut HabitatPocketTable shallow bands without a zero-width span.
		if (min >= SHELF_EDGE) {
			return Climate.Parameter.point(SHELF_EDGE);
		}
		return Climate.Parameter.span(min, SHELF_EDGE);
	}

	private static Placement placement(ResourceKey<Biome> biome, Climate.Parameter continentalness, Climate.Parameter weirdness) {
		return new Placement(biome, continentalness, weirdness);
	}

	public record Placement(ResourceKey<Biome> biome, Climate.Parameter continentalness, Climate.Parameter weirdness) {
	}
}
