package com.midas.ecology.client.config;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.InstanceCreator;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
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
	private static final Gson GSON = new GsonBuilder()
		.setPrettyPrinting()
		.registerTypeAdapter(EcologyClientConfig.class, (InstanceCreator<EcologyClientConfig>) type -> new EcologyClientConfig())
		.registerTypeAdapter(LookingAtWater.class, (InstanceCreator<LookingAtWater>) type -> new LookingAtWater())
		.registerTypeAdapter(Swimming.class, (InstanceCreator<Swimming>) type -> new Swimming())
		.registerTypeAdapter(OpaqueWater.class, (InstanceCreator<OpaqueWater>) type -> new OpaqueWater())
		.registerTypeAdapter(Debug.class, (InstanceCreator<Debug>) type -> new Debug())
		.create();
	private static final Path PATH = FabricLoader.getInstance().getConfigDir().resolve("ecology-client.json");

	private static EcologyClientConfig instance = new EcologyClientConfig();
	private static boolean loaded;
	private static String lastSavedJson = "";

	public DistantWaterMode mode = DistantWaterMode.FOG_REMAP;
	public boolean pauseWithIris = true;
	public boolean warnIfFabulousOff = true;
	public LookingAtWater lookingAtWater = new LookingAtWater();
	public Swimming swimming = new Swimming();
	public OpaqueWater opaqueWater = new OpaqueWater();
	public Debug debug = new Debug();

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
				JsonObject json = JsonParser.parseString(diskJson).getAsJsonObject();
				migrateLegacyJson(json);
				EcologyClientConfig loadedConfig = GSON.fromJson(json, EcologyClientConfig.class);
				if (loadedConfig != null) {
					instance = loadedConfig;
				}
			} catch (Exception e) {
				EcologyMod.LOGGER.error("Failed to load ecology-client.json, using defaults", e);
				notifyPlayer(Component.translatable("ecology.config.chat.load_failed"));
			}
		} else if (instance.debug.logging) {
			EcologyMod.LOGGER.info("No ecology-client.json yet; creating defaults at {}", PATH.toAbsolutePath());
		}
		instance.ensureSections();
		instance.swimming.migrateOldDistanceTables();
		instance.clamp();
		loaded = true;
		if (diskJson != null) {
			lastSavedJson = diskJson;
		}
		saveIfChanged();
	}

	public static void save() {
		saveIfChanged();
	}

	private static void saveIfChanged() {
		instance.ensureSections();
		instance.clamp();
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
			if (instance.debug.logging) {
				EcologyMod.LOGGER.info("Saved Ecology client config to {}", PATH.toAbsolutePath());
			}
		} catch (IOException e) {
			EcologyMod.LOGGER.error("Failed to save ecology-client.json", e);
			notifyPlayer(Component.translatable("ecology.config.chat.save_failed"));
		}
	}

	/**
	 * Flattened ecology-client.json from older builds is rewritten into the nested layout
	 * before Gson binds fields. Unknown keys are dropped on the next save.
	 */
	private static void migrateLegacyJson(JsonObject json) {
		if (!json.has("mode")) {
			if (json.has("distantWaterMode")) {
				json.add("mode", json.get("distantWaterMode"));
			} else {
				boolean legacyOn = true;
				if (json.has("distantWaterOpacityEnabled") && !json.has("waterShaderEnabled")) {
					legacyOn = json.get("distantWaterOpacityEnabled").getAsBoolean();
				} else if (json.has("waterShaderEnabled")) {
					legacyOn = json.get("waterShaderEnabled").getAsBoolean();
				}
				json.addProperty("mode", legacyOn ? "opaque" : "off");
			}
		}
		renameModeValue(json, "mode");

		if (!json.has("pauseWithIris") && json.has("irisAutoDisable")) {
			json.add("pauseWithIris", json.get("irisAutoDisable"));
		}
		if (!json.has("warnIfFabulousOff") && json.has("warnMissingImprovedTransparency")) {
			json.add("warnIfFabulousOff", json.get("warnMissingImprovedTransparency"));
		}

		if (!json.has("lookingAtWater")) {
			JsonObject looking = new JsonObject();
			move(json, "surfaceAirFog", looking, "horizonFog");
			move(json, "sightFogUseRenderDistancePercent", looking, "usePercent");
			move(json, "sightFogStartPercent", looking, "startPercent");
			move(json, "underwaterSightStartPercent", looking, "startPercent");
			move(json, "sightFogEndPercent", looking, "endPercent");
			move(json, "underwaterSightEndPercent", looking, "endPercent");
			move(json, "sightFogStart", looking, "startBlocks");
			move(json, "underwaterSightStart", looking, "startBlocks");
			move(json, "sightFogEnd", looking, "endBlocks");
			move(json, "underwaterSightEnd", looking, "endBlocks");
			move(json, "fogRemapBiasStrength", looking, "fill");
			move(json, "fogTintDarkness", looking, "fogDarkness");
			json.add("lookingAtWater", looking);
		}

		if (!json.has("swimming")) {
			JsonObject swimming = new JsonObject();
			move(json, "swimFogDistanceEnabled", swimming, "customDistance");
			move(json, "swimFogFadeSeconds", swimming, "fadeSeconds");
			move(json, "underwaterLightStart", swimming, "brightUntil");
			move(json, "underwaterLightEnd", swimming, "darkAt");
			move(json, "swimFogKelpCanopy", swimming, "kelpForest");
			move(json, "swimFogColdPolarShelf", swimming, "cold");
			move(json, "swimFogTemperateShelf", swimming, "temperate");
			move(json, "swimFogIceOpenings", swimming, "iceOpenings");
			move(json, "swimFogSubtropical", swimming, "lukewarm");
			move(json, "swimFogDeepBasin", swimming, "deepBasin");
			move(json, "swimFogTropicalClear", swimming, "warm");
			move(json, "swimFogLagoon", swimming, "lagoon");
			move(json, "swimFogOpenOcean", swimming, "openOcean");
			move(json, "underwaterFogEnd", swimming, "otherWater");
			json.add("swimming", swimming);
		}

		if (!json.has("opaqueWater")) {
			JsonObject opaque = new JsonObject();
			move(json, "distanceOpacityEnabled", opaque, "distance");
			move(json, "distantWaterOpacityStrength", opaque, "strength");
			move(json, "distantWaterOpacityStart", opaque, "start");
			move(json, "distantWaterOpacityEnd", opaque, "end");
			move(json, "fresnelEnabled", opaque, "angle");
			move(json, "fresnelStrength", opaque, "angleStrength");
			move(json, "fresnelPower", opaque, "angleCurve");
			json.add("opaqueWater", opaque);
		}

		if (!json.has("debug")) {
			JsonObject debug = new JsonObject();
			move(json, "debugLogging", debug, "logging");
			move(json, "debugHighlightAllTranslucent", debug, "highlightTranslucent");
			move(json, "debugHighlightFogRemap", debug, "highlightSeeThrough");
			move(json, "debugHighlightMarkedTops", debug, "highlightMarkedWater");
			move(json, "debugHighlightFresnel", debug, "highlightAngle");
			json.add("debug", debug);
		}
	}

	private static void renameModeValue(JsonObject json, String key) {
		if (!json.has(key) || !json.get(key).isJsonPrimitive()) {
			return;
		}
		String mapped = switch (json.get(key).getAsString()) {
			case "FOG_REMAP", "fog_tint" -> "fog_tint";
			case "OPACITY", "opaque" -> "opaque";
			case "OFF", "off" -> "off";
			default -> "fog_tint";
		};
		json.addProperty(key, mapped);
	}

	private static void move(JsonObject from, String oldKey, JsonObject to, String newKey) {
		if (from.has(oldKey) && !to.has(newKey)) {
			to.add(newKey, from.get(oldKey));
		}
	}

	public DistantWaterMode effectiveMode() {
		DistantWaterMode resolved = this.mode != null ? this.mode : DistantWaterMode.FOG_REMAP;
		if (resolved == DistantWaterMode.OFF) {
			return DistantWaterMode.OFF;
		}
		if (this.pauseWithIris && IrisCompat.isShaderPackInUse()) {
			return DistantWaterMode.OFF;
		}
		return resolved;
	}

	public static void notifyPlayer(String message) {
		if (!get().debug.logging) {
			return;
		}
		sendChat(Component.literal(message));
	}

	public static void notifyPlayer(Component message) {
		if (!get().debug.logging) {
			return;
		}
		sendChat(message);
	}

	/** Always send a chat line (ignores debug logging). No-op if not in-game. */
	public static void notifyPlayerAlways(String message) {
		sendChat(Component.literal(message));
	}

	public static void notifyPlayerAlways(Component message) {
		sendChat(message);
	}

	private static void sendChat(Component message) {
		Minecraft client = Minecraft.getInstance();
		if (client != null && client.player != null) {
			client.player.sendSystemMessage(message);
		}
	}

	private void ensureSections() {
		if (this.lookingAtWater == null) {
			this.lookingAtWater = new LookingAtWater();
		}
		if (this.swimming == null) {
			this.swimming = new Swimming();
		}
		if (this.opaqueWater == null) {
			this.opaqueWater = new OpaqueWater();
		}
		if (this.debug == null) {
			this.debug = new Debug();
		}
	}

	private void clamp() {
		if (this.mode == null) {
			this.mode = DistantWaterMode.FOG_REMAP;
		}
		this.lookingAtWater.clamp();
		this.swimming.clamp();
		this.opaqueWater.clamp();
	}

	static float clamp01(float value) {
		return Math.max(0.0F, Math.min(1.0F, value));
	}

	static float clampSwimFog(float value) {
		return Math.max(8.0F, Math.min(256.0F, Math.round(value)));
	}

	public static final class LookingAtWater {
		public float horizonFog = 1.0F;
		public boolean usePercent = true;
		public int startPercent = 10;
		public int endPercent = 70;
		public float startBlocks = 16.0F;
		public float endBlocks = 128.0F;
		public float fill = 1.0F;
		public float fogDarkness = 0.55F;

		public float clampedHorizonFog() {
			return clamp01(this.horizonFog);
		}

		public float clampedFill() {
			return clamp01(this.fill);
		}

		public float clampedFogDarkness() {
			return clamp01(this.fogDarkness);
		}

		public float clampedStartBlocks() {
			return Math.max(0.0F, Math.min(256.0F, Math.round(this.startBlocks)));
		}

		public float clampedEndBlocks() {
			float start = clampedStartBlocks();
			return Math.max(start + 1.0F, Math.min(256.0F, Math.round(this.endBlocks)));
		}

		public int clampedEndPercent() {
			return Math.max(1, Math.min(100, this.endPercent));
		}

		public int clampedStartPercent() {
			int end = clampedEndPercent();
			return Math.max(0, Math.min(end - 1, this.startPercent));
		}

		private void clamp() {
			this.horizonFog = clampedHorizonFog();
			this.fill = clampedFill();
			this.fogDarkness = clampedFogDarkness();
			this.startBlocks = clampedStartBlocks();
			this.endBlocks = clampedEndBlocks();
			this.endPercent = clampedEndPercent();
			this.startPercent = clampedStartPercent();
		}
	}

	public static final class Swimming {
		public boolean customDistance = true;
		public float fadeSeconds = 1.0F;
		public float brightUntil = 10.0F;
		public float darkAt = 64.0F;
		public float kelpForest = 36.0F;
		public float cold = 40.0F;
		public float temperate = 44.0F;
		public float iceOpenings = 48.0F;
		public float lukewarm = 50.0F;
		public float deepBasin = 52.0F;
		public float warm = 56.0F;
		public float lagoon = 60.0F;
		public float openOcean = 60.0F;
		public float otherWater = 34.0F;

		public float clampedFadeSeconds() {
			return Math.max(0.0F, Math.min(5.0F, this.fadeSeconds));
		}

		public float clampedBrightUntil() {
			return Math.max(0.0F, Math.min(256.0F, Math.round(this.brightUntil)));
		}

		public float clampedDarkAt() {
			float start = clampedBrightUntil();
			return Math.max(start + 1.0F, Math.min(256.0F, Math.round(this.darkAt)));
		}

		public float clampedOtherWater() {
			return clampSwimFog(this.otherWater);
		}

		/** Swim fog end for the biome at the camera. Unknown water uses {@link #otherWater}. */
		public float endFor(Holder<Biome> biome) {
			if (biome == null) {
				return clampedOtherWater();
			}
			if (biome.is(EcologyBiomes.KELP_FOREST)) {
				return clampSwimFog(this.kelpForest);
			}
			if (biome.is(EcologyBiomes.ICE_EDGE) || biome.is(EcologyBiomes.POLYNYA)) {
				return clampSwimFog(this.iceOpenings);
			}
			if (biome.is(EcologyBiomes.SYMPAGIC_ZONE)
				|| biome.is(EcologyBiomes.COLD_EELGRASS)
				|| biome.is(Biomes.FROZEN_OCEAN)
				|| biome.is(Biomes.COLD_OCEAN)) {
				return clampSwimFog(this.cold);
			}
			if (biome.is(EcologyBiomes.SEAGRASS_MEADOW)
				|| biome.is(EcologyBiomes.TEMPERATE_ROCKY_REEF)
				|| biome.is(EcologyBiomes.SAND_WAVE_FIELD)
				|| biome.is(Biomes.OCEAN)) {
				return clampSwimFog(this.temperate);
			}
			if (biome.is(EcologyBiomes.SUBTROPICAL_SEAGRASS)
				|| biome.is(EcologyBiomes.PATCH_REEF)
				|| biome.is(EcologyBiomes.SOFT_CORAL_GARDEN)
				|| biome.is(Biomes.LUKEWARM_OCEAN)) {
				return clampSwimFog(this.lukewarm);
			}
			if (biome.is(EcologyBiomes.DEEP_BASIN)
				|| biome.is(Biomes.DEEP_FROZEN_OCEAN)
				|| biome.is(Biomes.DEEP_COLD_OCEAN)
				|| biome.is(Biomes.DEEP_OCEAN)
				|| biome.is(Biomes.DEEP_LUKEWARM_OCEAN)) {
				return clampSwimFog(this.deepBasin);
			}
			if (biome.is(EcologyBiomes.CORAL_REEF)
				|| biome.is(EcologyBiomes.TROPICAL_SEAGRASS)
				|| biome.is(Biomes.WARM_OCEAN)) {
				return clampSwimFog(this.warm);
			}
			if (biome.is(EcologyBiomes.LAGOON)) {
				return clampSwimFog(this.lagoon);
			}
			if (biome.is(EcologyBiomes.OPEN_OCEAN)) {
				return clampSwimFog(this.openOcean);
			}
			return clampedOtherWater();
		}

		/** Push saved region tables forward when they still match an older default set. */
		private void migrateOldDistanceTables() {
			boolean firstTable = Math.round(this.kelpForest) == 16
				&& Math.round(this.cold) == 20
				&& Math.round(this.temperate) == 24
				&& Math.round(this.iceOpenings) == 28
				&& Math.round(this.lukewarm) == 30
				&& Math.round(this.deepBasin) == 32
				&& Math.round(this.warm) == 36
				&& Math.round(this.lagoon) == 40;
			boolean secondTable = Math.round(this.kelpForest) == 26
				&& Math.round(this.cold) == 30
				&& Math.round(this.temperate) == 34
				&& Math.round(this.iceOpenings) == 38
				&& Math.round(this.lukewarm) == 40
				&& Math.round(this.deepBasin) == 42
				&& Math.round(this.warm) == 46
				&& Math.round(this.lagoon) == 50;
			if (firstTable || secondTable) {
				this.kelpForest = 36.0F;
				this.cold = 40.0F;
				this.temperate = 44.0F;
				this.iceOpenings = 48.0F;
				this.lukewarm = 50.0F;
				this.deepBasin = 52.0F;
				this.warm = 56.0F;
				this.lagoon = 60.0F;
			}
			if (Math.round(this.openOcean) == 50) {
				this.openOcean = 60.0F;
			}
			if (Math.round(this.otherWater) == 24) {
				this.otherWater = 34.0F;
			}
		}

		private void clamp() {
			this.fadeSeconds = clampedFadeSeconds();
			this.brightUntil = clampedBrightUntil();
			this.darkAt = clampedDarkAt();
			this.kelpForest = clampSwimFog(this.kelpForest);
			this.cold = clampSwimFog(this.cold);
			this.temperate = clampSwimFog(this.temperate);
			this.iceOpenings = clampSwimFog(this.iceOpenings);
			this.lukewarm = clampSwimFog(this.lukewarm);
			this.deepBasin = clampSwimFog(this.deepBasin);
			this.warm = clampSwimFog(this.warm);
			this.lagoon = clampSwimFog(this.lagoon);
			this.openOcean = clampSwimFog(this.openOcean);
			this.otherWater = clampSwimFog(this.otherWater);
		}
	}

	public static final class OpaqueWater {
		public boolean distance = true;
		public float strength = 1.0F;
		public float start = 0.0F;
		public float end = 0.5F;
		public boolean angle = true;
		public float angleStrength = 1.0F;
		public float angleCurve = 0.75F;

		public float clampedStrength() {
			return clamp01(this.strength);
		}

		public float clampedStart() {
			return clamp01(this.start);
		}

		public float clampedEnd() {
			return Math.max(clampedStart(), clamp01(this.end));
		}

		public float clampedAngleStrength() {
			return clamp01(this.angleStrength);
		}

		public float clampedAngleCurve() {
			return Math.max(0.25F, Math.min(8.0F, this.angleCurve));
		}

		private void clamp() {
			this.strength = clampedStrength();
			this.start = clampedStart();
			this.end = clampedEnd();
			this.angleStrength = clampedAngleStrength();
			this.angleCurve = clampedAngleCurve();
		}
	}

	public static final class Debug {
		public boolean logging = false;
		public boolean highlightTranslucent = false;
		public boolean highlightSeeThrough = false;
		public boolean highlightMarkedWater = false;
		public boolean highlightAngle = false;
	}
}
