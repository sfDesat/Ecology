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
 * <p>Bands align with {@code ecology:ocean_depth_control}:
 * <pre>
 *   COAST   [-0.235, -0.19]  (coastal shallows temporarily disabled)
 *   INNER   [-0.28, -0.235]  seagrass / lagoon
 *   MID     [-0.40, -0.28]   sand waves, kelp, reefs
 *   OUTER   [SHELF_EDGE, -0.40] soft coral / ice fringe
 *   DEEP    &lt; SHELF_EDGE     deep basin / open ocean
 * </pre>
 *
 * <p>Pockets get a slight majority of weirdness vs parent fill.
 */
public final class HabitatPocketTable {
	/**
	 * Seaward edge of the continental shelf / shoreward edge of deep basin.
	 * Vanilla shallow oceans stop at {@code -0.455}; Ecology nudges the shelf
	 * slightly farther offshore.
	 */
	public static final float SHELF_EDGE = -0.48f;

	/**
	 * Parent/generic ocean fill across the whole shallow shelf.
	 * Includes the former COAST strip while coastal shallows are disabled.
	 */
	public static final Climate.Parameter PARENT_SHELF_CONTINENTALNESS = Climate.Parameter.span(SHELF_EDGE, -0.19f);

	/**
	 * Slight pocket bias: parent ~46% of weirdness, pockets ~54%.
	 * Was a clean 50/50 split at 0.0.
	 */
	public static final Climate.Parameter PARENT_WEIRDNESS = Climate.Parameter.span(-1.0f, -0.08f);
	public static final Climate.Parameter POCKET_WEIRDNESS = Climate.Parameter.span(-0.08f, 1.0f);

	/** Full weirdness — reserved for the continuous coastal strip when re-enabled. */
	public static final Climate.Parameter FULL_WEIRDNESS = Climate.Parameter.span(-1.0f, 1.0f);

	/**
	 * Thin underwater strip next to land / beach.
	 * Coastal shallows placements are temporarily omitted from {@link #PLACEMENTS_BY_PARENT}.
	 */
	public static final Climate.Parameter COAST = Climate.Parameter.span(-0.235f, -0.19f);
	/** Seagrass / sheltered lagoon band just seaward of the surf strip. */
	public static final Climate.Parameter INNER = Climate.Parameter.span(-0.28f, -0.235f);
	/** Mid-shelf sand flats, kelp, and reefs. */
	public static final Climate.Parameter MID = Climate.Parameter.span(-0.40f, -0.28f);
	/** Outer shelf fringe before deep basin. */
	public static final Climate.Parameter OUTER = Climate.Parameter.span(SHELF_EDGE, -0.40f);

	private static final Climate.Parameter FROZEN_INNER_MID = Climate.Parameter.span(-0.40f, -0.235f);
	private static final Climate.Parameter FROZEN_FILL_WEIRDNESS_A = Climate.Parameter.span(-0.08f, 0.14f);
	private static final Climate.Parameter FROZEN_SYMPAGIC_WEIRDNESS = Climate.Parameter.span(0.14f, 0.34f);
	private static final Climate.Parameter FROZEN_ICE_EDGE_WEIRDNESS = Climate.Parameter.span(0.34f, 0.54f);
	private static final Climate.Parameter FROZEN_POLYNYA_WEIRDNESS = Climate.Parameter.span(0.54f, 0.76f);
	private static final Climate.Parameter FROZEN_FILL_WEIRDNESS_B = Climate.Parameter.span(0.76f, 1.0f);

	/** Sheltered lagoon — shoreward half of INNER (not the surf COAST strip). */
	private static final Climate.Parameter WARM_LAGOON = Climate.Parameter.span(-0.255f, -0.235f);
	private static final Climate.Parameter WARM_SEAGRASS = Climate.Parameter.span(-0.28f, -0.255f);
	private static final Climate.Parameter WARM_CORAL = Climate.Parameter.span(-0.36f, -0.28f);
	private static final Climate.Parameter TEMPERATE_SAND = Climate.Parameter.span(-0.40f, -0.34f);
	private static final Climate.Parameter TEMPERATE_REEF = Climate.Parameter.span(-0.34f, -0.28f);

	private static final Climate.Parameter LUKE_SEAGRASS = INNER;
	private static final Climate.Parameter LUKE_PATCH = Climate.Parameter.span(-0.34f, -0.28f);
	private static final Climate.Parameter LUKE_SOFT = OUTER;

	private static final Map<ResourceKey<Biome>, List<Placement>> PLACEMENTS_BY_PARENT = Map.of(
		Biomes.FROZEN_OCEAN,
		List.of(
			placement(Biomes.FROZEN_OCEAN, PARENT_SHELF_CONTINENTALNESS, PARENT_WEIRDNESS),
			// TEMP: coastal shallows disabled — placement(EcologyBiomes.FROZEN_COASTAL_SHALLOWS, COAST, FULL_WEIRDNESS),
			placement(Biomes.FROZEN_OCEAN, FROZEN_INNER_MID, FROZEN_FILL_WEIRDNESS_A),
			placement(Biomes.FROZEN_OCEAN, FROZEN_INNER_MID, FROZEN_FILL_WEIRDNESS_B),
			placement(EcologyBiomes.ICE_EDGE, OUTER, POCKET_WEIRDNESS),
			placement(EcologyBiomes.SYMPAGIC_ZONE, FROZEN_INNER_MID, FROZEN_SYMPAGIC_WEIRDNESS),
			placement(EcologyBiomes.ICE_EDGE, FROZEN_INNER_MID, FROZEN_ICE_EDGE_WEIRDNESS),
			placement(EcologyBiomes.POLYNYA, FROZEN_INNER_MID, FROZEN_POLYNYA_WEIRDNESS)
		),
		Biomes.COLD_OCEAN,
		List.of(
			placement(Biomes.COLD_OCEAN, PARENT_SHELF_CONTINENTALNESS, PARENT_WEIRDNESS),
			// TEMP: coastal shallows disabled — placement(EcologyBiomes.COLD_COASTAL_SHALLOWS, COAST, FULL_WEIRDNESS),
			placement(EcologyBiomes.COLD_EELGRASS, INNER, POCKET_WEIRDNESS),
			placement(EcologyBiomes.KELP_FOREST, MID, POCKET_WEIRDNESS)
		),
		Biomes.OCEAN,
		List.of(
			placement(Biomes.OCEAN, PARENT_SHELF_CONTINENTALNESS, PARENT_WEIRDNESS),
			// TEMP: coastal shallows disabled — placement(EcologyBiomes.TEMPERATE_COASTAL_SHALLOWS, COAST, FULL_WEIRDNESS),
			placement(EcologyBiomes.SEAGRASS_MEADOW, INNER, POCKET_WEIRDNESS),
			placement(EcologyBiomes.SAND_WAVE_FIELD, TEMPERATE_SAND, POCKET_WEIRDNESS),
			placement(EcologyBiomes.TEMPERATE_ROCKY_REEF, TEMPERATE_REEF, POCKET_WEIRDNESS)
		),
		Biomes.LUKEWARM_OCEAN,
		List.of(
			placement(Biomes.LUKEWARM_OCEAN, PARENT_SHELF_CONTINENTALNESS, PARENT_WEIRDNESS),
			// TEMP: coastal shallows disabled — placement(EcologyBiomes.LUKEWARM_COASTAL_SHALLOWS, COAST, FULL_WEIRDNESS),
			placement(EcologyBiomes.SUBTROPICAL_SEAGRASS, LUKE_SEAGRASS, POCKET_WEIRDNESS),
			placement(EcologyBiomes.PATCH_REEF, LUKE_PATCH, POCKET_WEIRDNESS),
			placement(EcologyBiomes.SOFT_CORAL_GARDEN, LUKE_SOFT, POCKET_WEIRDNESS)
		),
		Biomes.WARM_OCEAN,
		List.of(
			placement(Biomes.WARM_OCEAN, PARENT_SHELF_CONTINENTALNESS, PARENT_WEIRDNESS),
			// TEMP: coastal shallows disabled — placement(EcologyBiomes.TROPICAL_COASTAL_SHALLOWS, COAST, FULL_WEIRDNESS),
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
