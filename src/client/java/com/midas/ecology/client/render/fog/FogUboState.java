package com.midas.ecology.client.render.fog;

import com.midas.ecology.client.config.EcologyClientConfig;
import net.minecraft.client.Minecraft;
import net.minecraft.util.ARGB;
import net.minecraft.world.attribute.EnvironmentAttributes;
import net.minecraft.world.level.material.FogType;
import org.joml.Vector4f;

/**
 * Per-frame Ecology fields for the Fog UBO (water fog color + camera-underwater).
 * Prepared once from {@code FogRenderer.setupFog} so vanilla UBO write and Sodium
 * {@code u_Globals} extras see the same values.
 */
public final class FogUboState {
	private static final Vector4f WATER_FOG = new Vector4f(0.04F, 0.12F, 0.18F, 1.0F);
	private static float cameraUnderwater;

	private FogUboState() {
	}

	/** Resolve Ecology fog extras from the current camera / config before the UBO is written. */
	public static void prepare(Vector4f activeFogColor) {
		Minecraft client = Minecraft.getInstance();
		boolean underwater = client != null
			&& client.gameRenderer != null
			&& client.gameRenderer.mainCamera().getFluidInCamera() == FogType.WATER;

		cameraUnderwater = underwater ? 1.0F : 0.0F;

		if (!FogTint.isActive() || underwater) {
			WATER_FOG.set(activeFogColor);
			return;
		}

		if (client != null && client.gameRenderer != null) {
			Integer waterFog = client.gameRenderer.mainCamera()
				.attributeProbe()
				.getValue(EnvironmentAttributes.WATER_FOG_COLOR, 1.0F);
			if (waterFog != null) {
				float scale = 1.0F - EcologyClientConfig.get().lookingAtWater.clampedFogDarkness();
				WATER_FOG.set(
					ARGB.redFloat(waterFog) * scale,
					ARGB.greenFloat(waterFog) * scale,
					ARGB.blueFloat(waterFog) * scale,
					1.0F
				);
				return;
			}
		}

		float scale = 1.0F - EcologyClientConfig.get().lookingAtWater.clampedFogDarkness();
		WATER_FOG.set(activeFogColor.x * scale, activeFogColor.y * scale, activeFogColor.z * scale, activeFogColor.w);
	}

	public static Vector4f waterFogColor() {
		return WATER_FOG;
	}

	public static float cameraUnderwater() {
		return cameraUnderwater;
	}
}
