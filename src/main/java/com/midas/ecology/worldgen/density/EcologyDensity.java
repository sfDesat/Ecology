package com.midas.ecology.worldgen.density;

/**
 * Density-function path constants for Ecology-owned and overridden overworld density.
 * Datapack JSON is the source of truth; see DENSITY.md.
 */
public final class EcologyDensity {
	private EcologyDensity() {
	}

	public static final String OCEAN_DEPTH_CONTROL_PATH = "ocean_depth_control";
	public static final String OCEAN_FACTOR_CONTROL_PATH = "ocean_factor_control";
	public static final String OVERWORLD_OFFSET_BASE_PATH = "overworld/overworld_offset_base";
	public static final String OVERWORLD_FACTOR_BASE_PATH = "overworld/overworld_factor_base";
}
