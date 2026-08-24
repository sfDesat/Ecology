package com.midas.ecology.client.compat;

import net.fabricmc.loader.api.FabricLoader;

/**
 * Soft Sodium detection for overlay shaders and diagnostics.
 */
public final class SodiumCompat {
	private static final boolean LOADED = FabricLoader.getInstance().isModLoaded("sodium");

	private SodiumCompat() {
	}

	public static boolean isLoaded() {
		return LOADED;
	}
}
