package com.midas.ecology.client.compat;

import com.midas.ecology.EcologyMod;
import net.fabricmc.loader.api.FabricLoader;

import java.lang.reflect.Method;

/**
 * Soft Iris detection: only disables Ecology distant-water when a shader pack is actually in use.
 * Oculus (Forge) is detected as present but shares no Fabric API class we can bind.
 */
public final class IrisCompat {
	private static final boolean IRIS_LOADED = FabricLoader.getInstance().isModLoaded("iris")
		|| FabricLoader.getInstance().isModLoaded("oculus");
	private static final Object API_INSTANCE;
	private static final Method IS_SHADER_PACK_IN_USE;

	private static final long CACHE_NANOS = 50_000_000L;
	private static long cachedAtNanos;
	private static boolean cachedInUse;

	static {
		Object apiInstance = null;
		Method isShaderPackInUse = null;
		if (IRIS_LOADED) {
			Bind bind = bindIrisApi();
			apiInstance = bind.instance();
			isShaderPackInUse = bind.method();
			if (apiInstance == null || isShaderPackInUse == null) {
				EcologyMod.LOGGER.warn(
					"Iris/Oculus is present but IrisApi could not be bound; Auto-disable with Iris will not fire"
				);
			} else {
				EcologyMod.LOGGER.info("Bound IrisApi; Auto-disable with Iris can detect an active pack");
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
		long now = System.nanoTime();
		if (now - cachedAtNanos < CACHE_NANOS) {
			return cachedInUse;
		}
		cachedAtNanos = now;
		cachedInUse = queryPackInUse();
		return cachedInUse;
	}

	private static boolean queryPackInUse() {
		try {
			Object result = IS_SHADER_PACK_IN_USE.invoke(API_INSTANCE);
			return result instanceof Boolean b && b;
		} catch (ReflectiveOperationException e) {
			return false;
		}
	}

	private static Bind bindIrisApi() {
		try {
			Class<?> api = Class.forName("net.irisshaders.iris.api.v0.IrisApi");
			Object instance = api.getMethod("getInstance").invoke(null);
			if (instance == null) {
				return new Bind(null, null);
			}
			return new Bind(instance, api.getMethod("isShaderPackInUse"));
		} catch (ReflectiveOperationException ignored) {
			return new Bind(null, null);
		}
	}

	private record Bind(Object instance, Method method) {
	}
}
