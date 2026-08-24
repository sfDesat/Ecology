package com.midas.ecology.client.render;

import com.midas.ecology.client.compat.IrisCompat;
import com.midas.ecology.client.config.EcologyClientConfig;
import net.minecraft.client.Minecraft;
import net.minecraft.world.level.material.FogType;

/**
 * Shared gate for swimming comfort (lightmap + swim fog). Independent of distant-water mode.
 */
public final class SwimEffects {
	private SwimEffects() {
	}

	public static boolean shouldApply() {
		EcologyClientConfig config = EcologyClientConfig.get();
		if (config.irisAutoDisable && IrisCompat.isShaderPackInUse()) {
			return false;
		}
		Minecraft client = Minecraft.getInstance();
		return client != null
			&& client.gameRenderer != null
			&& client.gameRenderer.mainCamera().getFluidInCamera() == FogType.WATER;
	}
}
