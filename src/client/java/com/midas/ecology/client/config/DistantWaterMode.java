package com.midas.ecology.client.config;

import com.google.gson.annotations.SerializedName;
import net.minecraft.network.chat.Component;

/**
 * Selectable distant-water fix. Modes are mutually exclusive in the shader.
 * JSON uses the {@link SerializedName} values ({@code off}, {@code opaque}, {@code fog_tint}).
 */
public enum DistantWaterMode {
	@SerializedName("off")
	OFF,
	@SerializedName("opaque")
	OPACITY,
	@SerializedName("fog_tint")
	FOG_REMAP;

	public Component displayName() {
		return Component.translatable("ecology.config.mode." + this.jsonName());
	}

	public String jsonName() {
		return switch (this) {
			case OFF -> "off";
			case OPACITY -> "opaque";
			case FOG_REMAP -> "fog_tint";
		};
	}

	@Override
	public String toString() {
		return this.jsonName();
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
