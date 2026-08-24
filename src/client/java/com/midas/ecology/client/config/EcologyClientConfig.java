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
	 * Fog tint: how hard behind-water fill / empty fill applies (0-1). JSON name kept.
	 */
	public float fogRemapBiasStrength = 1.0F;
	/**
	 * Fog tint: blocks from camera where behind-water sight fog begins.
	 */
	public float sightFogStart = 16.0F;
	/**
	 * Fog tint: blocks from camera where behind-water sight fog is full (when not using render-distance %).
	 */
	public float sightFogEnd = 128.0F;
	/**
	 * Fog tint: when true, sight start and end are percents of fog render distance
	 * instead of the block distances.
	 */
	public boolean sightFogUseRenderDistancePercent = true;
	/**
	 * Fog tint: percent of fog render distance (1-100) used as sight end when
	 * {@link #sightFogUseRenderDistancePercent} is true.
	 */
	public int sightFogEndPercent = 70;
	/**
	 * Fog tint: percent of fog render distance (0-100) used as sight start when
	 * {@link #sightFogUseRenderDistancePercent} is true.
	 */
	public int sightFogStartPercent = 10;
	/**
	 * Fog tint: darkens biome water fog color. 0 = as-is, 1 = black.
	 */
	public float fogTintDarkness = 0.55F;
	/**
	 * Fog tint: how hard air fog sits on the water surface (0-1).
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
	 * When true, Ecology replaces vanilla short underwater fog with the biome swim distances below.
	 * When false, vanilla underwater fog distance is kept (brightness settings still apply).
	 */
	public boolean swimFogDistanceEnabled = true;
	/**
	 * Fallback swim view distance for rivers, lakes, and unknown biomes.
	 * Does not scale with render distance.
	 */
	public float underwaterFogEnd = 34.0F;
	/** Kelp Forest. */
	public float swimFogKelpCanopy = 36.0F;
	/** Frozen / cold shelf: Frozen Ocean, Sympagic Zone, Cold Ocean, Cold Eelgrass (+ coastal shallows). */
	public float swimFogColdPolarShelf = 40.0F;
	/** Temperate shelf: Ocean, Seagrass Meadow, Rocky Reef, Sand Wave Field (+ coastal shallows). */
	public float swimFogTemperateShelf = 44.0F;
	/** Ice Edge and Polynya. */
	public float swimFogIceOpenings = 48.0F;
	/** Lukewarm Ocean, Subtropical Seagrass, Patch Reef, Soft Coral Garden (+ coastal shallows). */
	public float swimFogSubtropical = 50.0F;
	/** Deep Basin (and leftover vanilla deep oceans). */
	public float swimFogDeepBasin = 52.0F;
	/** Warm Ocean, Coral Reef, Tropical Seagrass (+ coastal shallows). */
	public float swimFogTropicalClear = 56.0F;
	/** Lagoon. */
	public float swimFogLagoon = 60.0F;
	/** Open Ocean pelagic layer. */
	public float swimFogOpenOcean = 60.0F;
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
	/** Fog tint: pink = behind-water sight fog / empty-behind mask. */
	public boolean debugHighlightFogRemap = false;
	/**
	 * Fog tint: chat once when Fog tint is selected but Improved Transparency (Fabulous) is off.
	 */
	public boolean warnMissingImprovedTransparency = true;

	/** Legacy master switch; migrated into {@link #distantWaterMode} on load. */
	private Boolean waterShaderEnabled;
	/** Legacy field from older configs; migrated into {@link #waterShaderEnabled} then mode. */
	private Boolean distantWaterOpacityEnabled;
	/** Legacy Fog tint fractions; ignored after sightFog* migration. */
	@SuppressWarnings("unused")
	private Float fogTintDistanceStart;
	@SuppressWarnings("unused")
	private Float fogTintDistanceEnd;
	/** Legacy behind-water sight fields; migrated into {@code sightFog*}. */
	private Float underwaterSightStart;
	private Float underwaterSightEnd;
	private Boolean underwaterSightEndUseRenderDistancePercent;
	private Integer underwaterSightEndPercent;
	private Integer underwaterSightStartPercent;

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
		instance.underwaterSightStart = null;
		instance.underwaterSightEnd = null;
		instance.underwaterSightEndUseRenderDistancePercent = null;
		instance.underwaterSightEndPercent = null;
		instance.underwaterSightStartPercent = null;
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
		boolean hasSight = diskJson != null && (diskJson.contains("\"sightFogEnd\"") || diskJson.contains("\"underwaterSightEnd\""));
		if (!hasSight && this.sightFogEnd <= 0.0F) {
			this.sightFogStart = 16.0F;
			this.sightFogEnd = 128.0F;
		}
		if (diskJson != null && !diskJson.contains("\"sightFogStart\"") && this.underwaterSightStart != null) {
			this.sightFogStart = this.underwaterSightStart;
		}
		if (diskJson != null && !diskJson.contains("\"sightFogEnd\"") && this.underwaterSightEnd != null) {
			this.sightFogEnd = this.underwaterSightEnd;
		}
		if (diskJson != null && !diskJson.contains("\"sightFogUseRenderDistancePercent\"")
			&& this.underwaterSightEndUseRenderDistancePercent != null) {
			this.sightFogUseRenderDistancePercent = this.underwaterSightEndUseRenderDistancePercent;
		}
		if (diskJson != null && !diskJson.contains("\"sightFogEndPercent\"") && this.underwaterSightEndPercent != null) {
			this.sightFogEndPercent = this.underwaterSightEndPercent;
		}
		if (diskJson != null && !diskJson.contains("\"sightFogStartPercent\"") && this.underwaterSightStartPercent != null) {
			this.sightFogStartPercent = this.underwaterSightStartPercent;
		}
		migrateSwimFogRegionTable();
	}

	/** Push saved region tables forward when they still match an older default set. */
	private void migrateSwimFogRegionTable() {
		boolean firstTable = Math.round(this.swimFogKelpCanopy) == 16
			&& Math.round(this.swimFogColdPolarShelf) == 20
			&& Math.round(this.swimFogTemperateShelf) == 24
			&& Math.round(this.swimFogIceOpenings) == 28
			&& Math.round(this.swimFogSubtropical) == 30
			&& Math.round(this.swimFogDeepBasin) == 32
			&& Math.round(this.swimFogTropicalClear) == 36
			&& Math.round(this.swimFogLagoon) == 40;
		boolean secondTable = Math.round(this.swimFogKelpCanopy) == 26
			&& Math.round(this.swimFogColdPolarShelf) == 30
			&& Math.round(this.swimFogTemperateShelf) == 34
			&& Math.round(this.swimFogIceOpenings) == 38
			&& Math.round(this.swimFogSubtropical) == 40
			&& Math.round(this.swimFogDeepBasin) == 42
			&& Math.round(this.swimFogTropicalClear) == 46
			&& Math.round(this.swimFogLagoon) == 50;
		if (firstTable || secondTable) {
			this.swimFogKelpCanopy = 36.0F;
			this.swimFogColdPolarShelf = 40.0F;
			this.swimFogTemperateShelf = 44.0F;
			this.swimFogIceOpenings = 48.0F;
			this.swimFogSubtropical = 50.0F;
			this.swimFogDeepBasin = 52.0F;
			this.swimFogTropicalClear = 56.0F;
			this.swimFogLagoon = 60.0F;
		}
		if (Math.round(this.swimFogOpenOcean) == 50) {
			this.swimFogOpenOcean = 60.0F;
		}
		if (Math.round(this.underwaterFogEnd) == 24) {
			this.underwaterFogEnd = 34.0F;
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

	/** UI alias for {@link #clampedFogRemapBiasStrength()} (water fill strength). */
	public float clampedFogTintFillStrength() {
		return clampedFogRemapBiasStrength();
	}

	public float clampedSightFogStart() {
		return Math.max(0.0F, Math.min(256.0F, Math.round(this.sightFogStart)));
	}

	/** Block-mode sight end; always &gt;= start + 1. */
	public float clampedSightFogEnd() {
		float start = clampedSightFogStart();
		return Math.max(start + 1.0F, Math.min(256.0F, Math.round(this.sightFogEnd)));
	}

	public int clampedSightFogEndPercent() {
		return Math.max(1, Math.min(100, this.sightFogEndPercent));
	}

	public int clampedSightFogStartPercent() {
		int end = clampedSightFogEndPercent();
		return Math.max(0, Math.min(end - 1, this.sightFogStartPercent));
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

	/** Alias for {@link #clampedUnderwaterFogEnd()} — swim fallback, not Fog tint. */
	public float clampedSwimFogFallback() {
		return clampedUnderwaterFogEnd();
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
		this.sightFogStart = Math.max(0.0F, Math.min(256.0F, Math.round(this.sightFogStart)));
		this.sightFogEnd = Math.max(this.sightFogStart + 1.0F, Math.min(256.0F, Math.round(this.sightFogEnd)));
		this.sightFogEndPercent = Math.max(1, Math.min(100, this.sightFogEndPercent));
		this.sightFogStartPercent = Math.max(0, Math.min(this.sightFogEndPercent - 1, this.sightFogStartPercent));
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
