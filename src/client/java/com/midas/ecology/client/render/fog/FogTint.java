package com.midas.ecology.client.render.fog;

import com.midas.ecology.client.config.DistantWaterMode;
import com.midas.ecology.client.config.EcologyClientConfig;
import net.minecraft.client.Minecraft;
import net.minecraft.resources.Identifier;

/**
 * Shared gates for the Fabulous behind-water Fog tint path.
 */
public final class FogTint {
	/** Fragment shader used by {@code post_effect/transparency.json} pass 0. */
	public static final Identifier TRANSPARENCY_FRAGMENT = Identifier.withDefaultNamespace("post/transparency");

	private FogTint() {
	}

	/** True when Fabulous / Improved Transparency is the active graphics path. */
	public static boolean isFabulousTransparency() {
		Minecraft client = Minecraft.getInstance();
		if (client == null) {
			return false;
		}
		if (client.gameRenderer != null) {
			return client.gameRenderer.gameRenderState().useShaderTransparency();
		}
		return client.options != null && Boolean.TRUE.equals(client.options.improvedTransparency().get());
	}

	/**
	 * True when Fog tint should run: configured mode is Fog tint, Iris is not disabling,
	 * and Fabulous is actually on so the transparency composite can decode the water mask.
	 */
	public static boolean isActive() {
		return EcologyClientConfig.get().effectiveMode() == DistantWaterMode.FOG_REMAP
			&& isFabulousTransparency();
	}

	public static boolean isTransparencyFragment(Identifier fragmentShaderId) {
		return TRANSPARENCY_FRAGMENT.equals(fragmentShaderId);
	}
}
