package com.midas.ecology.client.config;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.midas.ecology.EcologyMod;
import com.midas.ecology.client.compat.IrisCompat;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;

import java.io.IOException;
import java.io.Writer;
import java.nio.file.Files;
import java.nio.file.Path;

public final class EcologyClientConfig {
	private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
	private static final Path PATH = FabricLoader.getInstance().getConfigDir().resolve("ecology-client.json");

	private static EcologyClientConfig instance = new EcologyClientConfig();
	private static boolean loaded;
	private static String lastSavedJson = "";

	/** Master switch for Ecology water surface opacity (distance + fresnel). */
	public boolean waterShaderEnabled = true;
	/** Distance-based opacity component (can disable to isolate fresnel). */
	public boolean distanceOpacityEnabled = true;
	public float distantWaterOpacityStrength = 1.0F;
	/** Lower = opacity starts closer to the player (fraction of FogRenderDistanceEnd). */
	public float distantWaterOpacityStart = 0.0F;
	/** Where opacity reaches full strength (fraction of FogRenderDistanceEnd). Must be &gt;= start. */
	public float distantWaterOpacityEnd = 0.5F;
	public boolean fresnelEnabled = true;
	/** Added on top of distance opacity (combined clamped to 1). */
	public float fresnelStrength = 1.0F;
	/**
	 * Curve for glancing angle: {@code pow(grazing, power)}.
	 * Lower = opacity spreads to more angles; higher = only near-horizon.
	 */
	public float fresnelPower = 0.75F;
	/**
	 * When true, Ecology water opacity turns off while an Iris shader pack is active.
	 * When false, Ecology keeps applying even with Iris (may conflict).
	 */
	public boolean irisAutoDisable = true;
	public boolean debugLogging = false;
	/** Paint tagged water tops yellow→blue by distance so marking + fog distance are obvious. */
	public boolean debugHighlightMarkedTops = false;
	/** Paint tagged water tops green→yellow→red by view angle (fresnel). */
	public boolean debugHighlightFresnel = false;
	/** Paint ANY partial-alpha terrain cyan — proves Ecology terrain.fsh is running at all. */
	public boolean debugHighlightAllTranslucent = false;

	/** Legacy field from older configs; migrated into {@link #waterShaderEnabled} on load. */
	private Boolean distantWaterOpacityEnabled;

	private EcologyClientConfig() {
	}

	public static EcologyClientConfig get() {
		return instance;
	}

	public static Path path() {
		return PATH;
	}

	/** Load once so pack registration can see disk settings before the first resource load. */
	public static void ensureLoaded() {
		if (!loaded) {
			load();
		}
	}

	public static void load() {
		String diskJson = null;
		if (Files.isRegularFile(PATH)) {
			try {
				diskJson = Files.readString(PATH);
				EcologyClientConfig loadedConfig = GSON.fromJson(diskJson, EcologyClientConfig.class);
				if (loadedConfig != null) {
					if (loadedConfig.distantWaterOpacityEnabled != null) {
						loadedConfig.waterShaderEnabled = loadedConfig.distantWaterOpacityEnabled;
						loadedConfig.distantWaterOpacityEnabled = null;
					}
					instance = loadedConfig;
				}
			} catch (Exception e) {
				EcologyMod.LOGGER.error("Failed to load ecology-client.json, using defaults", e);
				notifyPlayer("Ecology: failed to load config, using defaults (see log)");
			}
		} else if (instance.debugLogging) {
			EcologyMod.LOGGER.info("No ecology-client.json yet; creating defaults at {}", PATH.toAbsolutePath());
		}
		instance.clamp();
		loaded = true;
		if (diskJson != null) {
			lastSavedJson = diskJson;
		}
		// Only rewrite disk when clamp/migration changed content.
		saveIfChanged();
	}

	public static void save() {
		saveIfChanged();
	}

	private static void saveIfChanged() {
		instance.clamp();
		instance.distantWaterOpacityEnabled = null;
		String json = GSON.toJson(instance);
		if (json.equals(lastSavedJson) && Files.isRegularFile(PATH)) {
			return;
		}
		try {
			Files.createDirectories(PATH.getParent());
			try (Writer writer = Files.newBufferedWriter(PATH)) {
				writer.write(json);
			}
			lastSavedJson = json;
			if (instance.debugLogging) {
				EcologyMod.LOGGER.info("Saved Ecology client config to {}", PATH.toAbsolutePath());
			}
		} catch (IOException e) {
			EcologyMod.LOGGER.error("Failed to save ecology-client.json", e);
			notifyPlayer("Ecology: failed to save config (see log)");
		}
	}

	public boolean isWaterShaderActive() {
		if (!this.waterShaderEnabled) {
			return false;
		}
		return !(this.irisAutoDisable && IrisCompat.isShaderPackInUse());
	}

	public float clampedStrength() {
		return clamp01(this.distantWaterOpacityStrength);
	}

	public float clampedStart() {
		return clamp01(this.distantWaterOpacityStart);
	}

	/** Full-opacity distance; always &gt;= {@link #clampedStart()}. */
	public float clampedEnd() {
		return Math.max(clampedStart(), clamp01(this.distantWaterOpacityEnd));
	}

	public float clampedFresnelStrength() {
		return clamp01(this.fresnelStrength);
	}

	public float clampedFresnelPower() {
		return Math.max(0.25F, Math.min(8.0F, this.fresnelPower));
	}

	public static void notifyPlayer(String message) {
		if (!get().debugLogging) {
			return;
		}
		Minecraft client = Minecraft.getInstance();
		if (client != null && client.player != null) {
			client.player.sendSystemMessage(Component.literal(message));
		}
	}

	private void clamp() {
		this.distantWaterOpacityStrength = clamp01(this.distantWaterOpacityStrength);
		this.distantWaterOpacityStart = clamp01(this.distantWaterOpacityStart);
		this.distantWaterOpacityEnd = Math.max(this.distantWaterOpacityStart, clamp01(this.distantWaterOpacityEnd));
		this.fresnelStrength = clamp01(this.fresnelStrength);
		this.fresnelPower = clampedFresnelPower();
	}

	private static float clamp01(float value) {
		return Math.max(0.0F, Math.min(1.0F, value));
	}
}
