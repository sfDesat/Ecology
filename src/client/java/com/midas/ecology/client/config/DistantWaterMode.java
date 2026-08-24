package com.midas.ecology.client.config;

/**
 * Selectable distant-water fix. Modes are mutually exclusive in the shader.
 * <p>
 * Display names are for the config UI; JSON still uses the enum constant names.
 */
public enum DistantWaterMode {
	/** Vanilla water; Ecology distant-water effects off (swim fog/brightness unchanged). */
	OFF("Off (vanilla)"),
	/** Distance + fresnel alpha boost on marked water faces. */
	OPACITY("Opaque water"),
	/**
	 * Unfog air fog behind water, then apply a fixed underwater sight fog toward water fog color.
	 * Surface fog on the water mesh stays white. Needs Fabulous / Improved Transparency.
	 */
	FOG_REMAP("Fog tint");

	private final String label;

	DistantWaterMode(String label) {
		this.label = label;
	}

	public String label() {
		return this.label;
	}

	@Override
	public String toString() {
		return this.label;
	}

	public float shaderValue() {
		return switch (this) {
			case OFF -> 0.0F;
			case OPACITY -> 1.0F;
			case FOG_REMAP -> 2.0F;
		};
	}

	public static DistantWaterMode fromShaderValue(float value) {
		if (value > 1.5F) {
			return FOG_REMAP;
		}
		if (value > 0.5F) {
			return OPACITY;
		}
		return OFF;
	}
}
