package com.midas.ecology.client.render.fog;

import com.midas.ecology.client.config.DistantWaterMode;
import com.midas.ecology.client.config.EcologyClientConfig;
import net.minecraft.resources.Identifier;

/**
 * Shared gates for the Fabulous behind-water fog-tint path.
 */
public final class FogTint {
	/** Fragment shader used by {@code post_effect/transparency.json} pass 0. */
	public static final Identifier TRANSPARENCY_FRAGMENT = Identifier.withDefaultNamespace("post/transparency");

	private FogTint() {
	}

	/** True when Fog tint is the effective distant-water mode (not Off / Opaque, Iris not disabling). */
	public static boolean isActive() {
		return EcologyClientConfig.get().effectiveMode() == DistantWaterMode.FOG_REMAP;
	}

	public static boolean isTransparencyFragment(Identifier fragmentShaderId) {
		return TRANSPARENCY_FRAGMENT.equals(fragmentShaderId);
	}
}
