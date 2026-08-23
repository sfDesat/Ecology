package com.midas.ecology.client.config;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.midas.ecology.EcologyMod;
import com.midas.ecology.client.compat.IrisCompat;
import com.midas.ecology.worldgen.EcologyBiomes;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.client.Minecraft;
import net.minecraft.core.Holder;
import net.minecraft.network.chat.Component;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.biome.Biomes;

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

	/**
	 * Distant-water fix mode. Default {@link DistantWaterMode#FOG_REMAP} for new installs.
	 * Null on disk means legacy config — migrated from {@link #waterShaderEnabled}.
	 */
	public DistantWaterMode distantWaterMode = DistantWaterMode.FOG_REMAP;
	/** Distance-based opacity component (OPACITY mode only). */
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
	 * FOG_REMAP: how hard underwater sight fog / empty fill applies (0-1).
	 */
	public float fogRemapBiasStrength = 1.0F;
	/**
	 * FOG_REMAP: blocks from camera where custom underwater fog begins (behind water).
	 */
	public float underwaterSightStart = 16.0F;
	/**
	 * FOG_REMAP: blocks from camera where underwater fog is full (when not using render-distance %).
	 */
	public float underwaterSightEnd = 128.0F;
	/**
	 * FOG_REMAP: when true, sight start and end are percents of fog render distance
	 * instead of the block distances.
	 */
	public boolean underwaterSightEndUseRenderDistancePercent = true;
	/**
	 * FOG_REMAP: percent of fog render distance (1-100) used as sight end when
	 * {@link #underwaterSightEndUseRenderDistancePercent} is true.
	 */
	public int underwaterSightEndPercent = 70;
	/**
	 * FOG_REMAP: percent of fog render distance (0-100) used as sight start when
	 * {@link #underwaterSightEndUseRenderDistancePercent} is true.
	 */
	public int underwaterSightStartPercent = 10;
	/**
	 * FOG_REMAP: darkens biome water fog color. 0 = as-is, 1 = black.
	 */
	public float fogTintDarkness = 0.55F;
	/**
	 * FOG_REMAP: how hard air fog sits on the water surface (0-1).
	 * Restores land-matching horizon fog after behind-water unfog.
	 */
	public float surfaceAirFog = 1.0F;
	/**
	 * Blocks of water overhead before extra underwater ambient starts fading.
	 */
	public float underwaterLightStart = 10.0F;
	/**
	 * Blocks of water overhead where extra ambient is gone (vanilla-dark).
	 */
	public float underwaterLightEnd = 64.0F;
	/**
	 * Seconds to blend swim view distance when moving between water types.
	 * 0 = instant. Does not fade when entering or leaving water.
	 */
	public float swimFogFadeSeconds = 1.0F;
	/**
	 * Fallback swim view distance for rivers, lakes, and unknown biomes.
	 * Does not scale with render distance.
	 */
	public float underwaterFogEnd = 24.0F;
	/** Kelp Forest. */
	public float swimFogKelpCanopy = 26.0F;
	/** Frozen / cold shelf: Frozen Ocean, Sympagic Zone, Cold Ocean, Cold Eelgrass (+ coastal shallows). */
	public float swimFogColdPolarShelf = 30.0F;
	/** Temperate shelf: Ocean, Seagrass Meadow, Rocky Reef, Sand Wave Field (+ coastal shallows). */
	public float swimFogTemperateShelf = 34.0F;
	/** Ice Edge and Polynya. */
	public float swimFogIceOpenings = 38.0F;
	/** Lukewarm Ocean, Subtropical Seagrass, Patch Reef, Soft Coral Garden (+ coastal shallows). */
	public float swimFogSubtropical = 40.0F;
	/** Deep Basin (and leftover vanilla deep oceans). */
	public float swimFogDeepBasin = 42.0F;
	/** Warm Ocean, Coral Reef, Tropical Seagrass (+ coastal shallows). */
	public float swimFogTropicalClear = 46.0F;
	/** Lagoon. */
	public float swimFogLagoon = 50.0F;
	/** Open Ocean pelagic layer. */
	public float swimFogOpenOcean = 50.0F;
	/**
	 * When true, Ecology distant-water effects turn off while an Iris shader pack is active.
	 * When false, Ecology keeps applying even with Iris (may conflict).
	 */
	public boolean irisAutoDisable = true;
	public boolean debugLogging = false;
	/** Paint marked water faces yellow→blue by distance (opaque-water debug). */
	public boolean debugHighlightMarkedTops = false;
	/** Paint marked water faces green→yellow→red by view angle (fresnel). */
	public boolean debugHighlightFresnel = false;
	/** Paint ANY partial-alpha terrain cyan — proves Ecology terrain.fsh is running at all. */
	public boolean debugHighlightAllTranslucent = false;
	/** Fog tint: pink = underwater sight fog / empty-behind mask. */
	public boolean debugHighlightFogRemap = false;
	/**
	 * Fog tint: chat once when Fog tint is selected but Improved Transparency (Fabulous) is off.
	 */
	public boolean warnMissingImprovedTransparency = true;

	/** Legacy master switch; migrated into {@link #distantWaterMode} on load. */
	private Boolean waterShaderEnabled;
	/** Legacy field from older configs; migrated into {@link #waterShaderEnabled} then mode. */
	private Boolean distantWaterOpacityEnabled;
	/** Legacy FOG_REMAP fractions; ignored after underwaterSight* migration. */
	@SuppressWarnings("unused")
	private Float fogTintDistanceStart;
	@SuppressWarnings("unused")
	private Float fogTintDistanceEnd;

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
					loadedConfig.migrateLegacyFields(diskJson);
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
		instance.waterShaderEnabled = null;
		instance.distantWaterOpacityEnabled = null;
		instance.fogTintDistanceStart = null;
		instance.fogTintDistanceEnd = null;
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

	/**
	 * Legacy configs used {@code waterShaderEnabled} / {@code distantWaterOpacityEnabled} without a mode.
	 * Those map to {@link DistantWaterMode#OPACITY} (or OFF) so existing installs keep opacity until changed.
	 * New installs (no file) keep default {@link DistantWaterMode#FOG_REMAP}.
	 */
	private void migrateLegacyFields(String diskJson) {
		if (this.distantWaterOpacityEnabled != null && this.waterShaderEnabled == null) {
			this.waterShaderEnabled = this.distantWaterOpacityEnabled;
		}
		boolean modeInJson = diskJson != null && diskJson.contains("\"distantWaterMode\"");
		if (!modeInJson) {
			boolean legacyOn = this.waterShaderEnabled == null || this.waterShaderEnabled;
			this.distantWaterMode = legacyOn ? DistantWaterMode.OPACITY : DistantWaterMode.OFF;
		} else if (this.distantWaterMode == null) {
			this.distantWaterMode = DistantWaterMode.FOG_REMAP;
		}
		boolean hasSight = diskJson != null && diskJson.contains("\"underwaterSightEnd\"");
		if (!hasSight && this.underwaterSightEnd <= 0.0F) {
			this.underwaterSightStart = 16.0F;
			this.underwaterSightEnd = 128.0F;
		}
		// First region table was 10 shorter on every ocean type except open ocean / rivers.
		boolean originalRegionTable = Math.round(this.swimFogKelpCanopy) == 16
			&& Math.round(this.swimFogColdPolarShelf) == 20
			&& Math.round(this.swimFogTemperateShelf) == 24
			&& Math.round(this.swimFogIceOpenings) == 28
			&& Math.round(this.swimFogSubtropical) == 30
			&& Math.round(this.swimFogDeepBasin) == 32
			&& Math.round(this.swimFogTropicalClear) == 36
			&& Math.round(this.swimFogLagoon) == 40;
		if (originalRegionTable) {
			this.swimFogKelpCanopy = 26.0F;
			this.swimFogColdPolarShelf = 30.0F;
			this.swimFogTemperateShelf = 34.0F;
			this.swimFogIceOpenings = 38.0F;
			this.swimFogSubtropical = 40.0F;
			this.swimFogDeepBasin = 42.0F;
			this.swimFogTropicalClear = 46.0F;
			this.swimFogLagoon = 50.0F;
		}
	}

	public DistantWaterMode effectiveMode() {
		DistantWaterMode mode = this.distantWaterMode != null ? this.distantWaterMode : DistantWaterMode.FOG_REMAP;
		if (mode == DistantWaterMode.OFF) {
			return DistantWaterMode.OFF;
		}
		if (this.irisAutoDisable && IrisCompat.isShaderPackInUse()) {
			return DistantWaterMode.OFF;
		}
		return mode;
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

	public float clampedFogRemapBiasStrength() {
		return clamp01(this.fogRemapBiasStrength);
	}

	public float clampedUnderwaterSightStart() {
		return Math.max(0.0F, Math.min(256.0F, Math.round(this.underwaterSightStart)));
	}

	/** Block-mode sight end; always &gt;= start + 1. */
	public float clampedUnderwaterSightEnd() {
		float start = clampedUnderwaterSightStart();
		return Math.max(start + 1.0F, Math.min(256.0F, Math.round(this.underwaterSightEnd)));
	}

	public int clampedUnderwaterSightEndPercent() {
		return Math.max(1, Math.min(100, this.underwaterSightEndPercent));
	}

	public int clampedUnderwaterSightStartPercent() {
		int end = clampedUnderwaterSightEndPercent();
		return Math.max(0, Math.min(end - 1, this.underwaterSightStartPercent));
	}

	public float clampedFogTintDarkness() {
		return clamp01(this.fogTintDarkness);
	}

	public float clampedSurfaceAirFog() {
		return clamp01(this.surfaceAirFog);
	}

	public float clampedUnderwaterLightStart() {
		return Math.max(0.0F, Math.min(256.0F, Math.round(this.underwaterLightStart)));
	}

	/** Full-black extra-ambient depth; always >= start + 1. */
	public float clampedUnderwaterLightEnd() {
		float start = clampedUnderwaterLightStart();
		return Math.max(start + 1.0F, Math.min(256.0F, Math.round(this.underwaterLightEnd)));
	}

	public float clampedUnderwaterFogEnd() {
		return clampSwimFog(this.underwaterFogEnd);
	}

	public float clampedSwimFogFadeSeconds() {
		return Math.max(0.0F, Math.min(5.0F, this.swimFogFadeSeconds));
	}

	/** Swim fog end for the biome at the camera. Unknown water uses {@link #underwaterFogEnd}. */
	public float clampedSwimFogEnd(Holder<Biome> biome) {
		if (biome == null) {
			return clampedUnderwaterFogEnd();
		}
		if (biome.is(EcologyBiomes.KELP_FOREST)) {
			return clampSwimFog(this.swimFogKelpCanopy);
		}
		if (biome.is(EcologyBiomes.ICE_EDGE) || biome.is(EcologyBiomes.POLYNYA)) {
			return clampSwimFog(this.swimFogIceOpenings);
		}
		if (biome.is(EcologyBiomes.SYMPAGIC_ZONE)
			|| biome.is(EcologyBiomes.FROZEN_COASTAL_SHALLOWS)
			|| biome.is(EcologyBiomes.COLD_COASTAL_SHALLOWS)
			|| biome.is(EcologyBiomes.COLD_EELGRASS)
			|| biome.is(Biomes.FROZEN_OCEAN)
			|| biome.is(Biomes.COLD_OCEAN)) {
			return clampSwimFog(this.swimFogColdPolarShelf);
		}
		if (biome.is(EcologyBiomes.SEAGRASS_MEADOW)
			|| biome.is(EcologyBiomes.TEMPERATE_ROCKY_REEF)
			|| biome.is(EcologyBiomes.SAND_WAVE_FIELD)
			|| biome.is(EcologyBiomes.TEMPERATE_COASTAL_SHALLOWS)
			|| biome.is(Biomes.OCEAN)) {
			return clampSwimFog(this.swimFogTemperateShelf);
		}
		if (biome.is(EcologyBiomes.SUBTROPICAL_SEAGRASS)
			|| biome.is(EcologyBiomes.PATCH_REEF)
			|| biome.is(EcologyBiomes.SOFT_CORAL_GARDEN)
			|| biome.is(EcologyBiomes.LUKEWARM_COASTAL_SHALLOWS)
			|| biome.is(Biomes.LUKEWARM_OCEAN)) {
			return clampSwimFog(this.swimFogSubtropical);
		}
		if (biome.is(EcologyBiomes.DEEP_BASIN)
			|| biome.is(Biomes.DEEP_FROZEN_OCEAN)
			|| biome.is(Biomes.DEEP_COLD_OCEAN)
			|| biome.is(Biomes.DEEP_OCEAN)
			|| biome.is(Biomes.DEEP_LUKEWARM_OCEAN)) {
			return clampSwimFog(this.swimFogDeepBasin);
		}
		if (biome.is(EcologyBiomes.CORAL_REEF)
			|| biome.is(EcologyBiomes.TROPICAL_SEAGRASS)
			|| biome.is(EcologyBiomes.TROPICAL_COASTAL_SHALLOWS)
			|| biome.is(Biomes.WARM_OCEAN)) {
			return clampSwimFog(this.swimFogTropicalClear);
		}
		if (biome.is(EcologyBiomes.LAGOON)) {
			return clampSwimFog(this.swimFogLagoon);
		}
		if (biome.is(EcologyBiomes.OPEN_OCEAN)) {
			return clampSwimFog(this.swimFogOpenOcean);
		}
		return clampedUnderwaterFogEnd();
	}

	public static void notifyPlayer(String message) {
		if (!get().debugLogging) {
			return;
		}
		sendChat(message);
	}

	/** Always send a chat line (ignores debug logging). No-op if not in-game. */
	public static void notifyPlayerAlways(String message) {
		sendChat(message);
	}

	private static void sendChat(String message) {
		Minecraft client = Minecraft.getInstance();
		if (client != null && client.player != null) {
			client.player.sendSystemMessage(Component.literal(message));
		}
	}

	private void clamp() {
		if (this.distantWaterMode == null) {
			this.distantWaterMode = DistantWaterMode.FOG_REMAP;
		}
		this.distantWaterOpacityStrength = clamp01(this.distantWaterOpacityStrength);
		this.distantWaterOpacityStart = clamp01(this.distantWaterOpacityStart);
		this.distantWaterOpacityEnd = Math.max(this.distantWaterOpacityStart, clamp01(this.distantWaterOpacityEnd));
		this.fresnelStrength = clamp01(this.fresnelStrength);
		this.fresnelPower = clampedFresnelPower();
		this.fogRemapBiasStrength = clamp01(this.fogRemapBiasStrength);
		this.underwaterSightStart = Math.max(0.0F, Math.min(256.0F, Math.round(this.underwaterSightStart)));
		this.underwaterSightEnd = Math.max(this.underwaterSightStart + 1.0F, Math.min(256.0F, Math.round(this.underwaterSightEnd)));
		this.underwaterSightEndPercent = Math.max(1, Math.min(100, this.underwaterSightEndPercent));
		this.underwaterSightStartPercent = Math.max(0, Math.min(this.underwaterSightEndPercent - 1, this.underwaterSightStartPercent));
		this.fogTintDarkness = clamp01(this.fogTintDarkness);
		this.surfaceAirFog = clamp01(this.surfaceAirFog);
		this.underwaterLightStart = Math.max(0.0F, Math.min(256.0F, Math.round(this.underwaterLightStart)));
		this.underwaterLightEnd = Math.max(this.underwaterLightStart + 1.0F, Math.min(256.0F, Math.round(this.underwaterLightEnd)));
		this.swimFogFadeSeconds = clampedSwimFogFadeSeconds();
		this.underwaterFogEnd = clampSwimFog(this.underwaterFogEnd);
		this.swimFogKelpCanopy = clampSwimFog(this.swimFogKelpCanopy);
		this.swimFogColdPolarShelf = clampSwimFog(this.swimFogColdPolarShelf);
		this.swimFogTemperateShelf = clampSwimFog(this.swimFogTemperateShelf);
		this.swimFogIceOpenings = clampSwimFog(this.swimFogIceOpenings);
		this.swimFogSubtropical = clampSwimFog(this.swimFogSubtropical);
		this.swimFogDeepBasin = clampSwimFog(this.swimFogDeepBasin);
		this.swimFogTropicalClear = clampSwimFog(this.swimFogTropicalClear);
		this.swimFogLagoon = clampSwimFog(this.swimFogLagoon);
		this.swimFogOpenOcean = clampSwimFog(this.swimFogOpenOcean);
	}

	private static float clamp01(float value) {
		return Math.max(0.0F, Math.min(1.0F, value));
	}

	private static float clampSwimFog(float value) {
		return Math.max(8.0F, Math.min(256.0F, Math.round(value)));
	}
}
