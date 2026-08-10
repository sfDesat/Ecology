package com.midas.ecology.worldgen.density;

/**
 * Density-function identifiers and documentation live primarily in datapack JSON
 * and {@code DENSITY.md}. This package is reserved for future Java helpers that
 * reference Ecology-owned density functions (e.g. {@code ecology:ocean_depth_control}).
 */
public final class EcologyDensity {
	private EcologyDensity() {
	}

	public static final String OCEAN_DEPTH_CONTROL_PATH = "ocean_depth_control";
	public static final String OVERWORLD_OFFSET_BASE_PATH = "overworld/overworld_offset_base";
}
