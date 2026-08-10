package com.midas.ecology.client.compat;

import com.midas.ecology.EcologyMod;
import net.fabricmc.loader.api.FabricLoader;

import java.lang.reflect.Method;

/**
 * Soft Iris detection: only disables Ecology distant-water when a shader pack is actually in use.
 */
public final class IrisCompat {
	private static final boolean IRIS_LOADED = FabricLoader.getInstance().isModLoaded("iris");
	private static final Object API_INSTANCE;
	private static final Method IS_SHADER_PACK_IN_USE;

	static {
		Object apiInstance = null;
		Method isShaderPackInUse = null;
		if (IRIS_LOADED) {
			try {
				Class<?> api = Class.forName("net.irisshaders.iris.api.v0.IrisApi");
				apiInstance = api.getMethod("getInstance").invoke(null);
				isShaderPackInUse = api.getMethod("isShaderPackInUse");
			} catch (ReflectiveOperationException e) {
				EcologyMod.LOGGER.info("Iris is present but IrisApi could not be bound; distant water stays available");
				apiInstance = null;
				isShaderPackInUse = null;
			}
		}
		API_INSTANCE = apiInstance;
		IS_SHADER_PACK_IN_USE = isShaderPackInUse;
	}

	private IrisCompat() {
	}

	public static boolean isShaderPackInUse() {
		if (!IRIS_LOADED || API_INSTANCE == null || IS_SHADER_PACK_IN_USE == null) {
			return false;
		}
		try {
			Object result = IS_SHADER_PACK_IN_USE.invoke(API_INSTANCE);
			return result instanceof Boolean b && b;
		} catch (ReflectiveOperationException e) {
			return false;
		}
	}
}
