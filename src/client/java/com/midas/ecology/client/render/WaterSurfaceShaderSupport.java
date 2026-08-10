package com.midas.ecology.client.render;

import com.midas.ecology.EcologyMod;
import com.midas.ecology.client.compat.IrisCompat;
import com.midas.ecology.client.config.DistantWaterMode;
import com.midas.ecology.client.config.EcologyClientConfig;
import net.minecraft.client.Minecraft;
import net.minecraft.resources.Identifier;
import net.minecraft.server.packs.repository.RepositorySource;
import net.minecraft.server.packs.resources.Resource;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;

/**
 * Syncs distant-water settings into {@link WaterSurfaceSettingsPack} and
 * reloads resources only when the effective signature changes (e.g. Iris pack toggle).
 */
public final class WaterSurfaceShaderSupport {
	private static final Identifier SETTINGS_ID = Identifier.fromNamespaceAndPath("ecology", "shaders/include/distant_water_settings.glsl");
	private static final Identifier TERRAIN_FSH = Identifier.fromNamespaceAndPath("minecraft", "shaders/core/terrain.fsh");
	private static final Identifier TRANSPARENCY_FSH = Identifier.fromNamespaceAndPath("minecraft", "shaders/post/transparency.fsh");

	private static final AtomicReference<String> LAST_SIGNATURE = new AtomicReference<>("");
	private static final AtomicBoolean RELOAD_SCHEDULED = new AtomicBoolean(false);
	private static final AtomicBoolean FABULOUS_WARN_SENT = new AtomicBoolean(false);
	private static int tickCounter;
	private static boolean loggedResourceCheck;

	private WaterSurfaceShaderSupport() {
	}

	/** Write settings without forcing a resource reload (startup / pack registration). */
	public static void applyConfigQuiet() {
		EcologyClientConfig.ensureLoaded();
		syncFromConfig(false);
		logDiagnostics("applyConfigQuiet");
	}

	/** Rewrite settings after the config screen saves; reload only if values actually changed. */
	public static void applyConfigAndReload() {
		EcologyClientConfig.ensureLoaded();
		boolean changed = syncFromConfig(false);
		logDiagnostics("applyConfigAndReload");
		EcologyClientConfig.notifyPlayer("Ecology distant water: " + statusSummary());
		FABULOUS_WARN_SENT.set(false);
		maybeWarnMissingFabulous();
		if (changed) {
			scheduleReload("configSave");
		}
	}

	public static void clientTick() {
		if (++tickCounter < 40) {
			return;
		}
		tickCounter = 0;
		syncFromConfig(true);
		maybeWarnMissingFabulous();
		if (!loggedResourceCheck && Minecraft.getInstance() != null && Minecraft.getInstance().getResourceManager() != null) {
			loggedResourceCheck = true;
			logDiagnostics("firstResourceCheck");
		}
	}

	/**
	 * Fog tint needs Fabulous / Improved Transparency. Chat once while the mismatch lasts
	 * (resets when Fabulous is on, mode changes, or the warn toggle is off).
	 */
	public static void maybeWarnMissingFabulous() {
		EcologyClientConfig config = EcologyClientConfig.get();
		Minecraft client = Minecraft.getInstance();
		boolean fogTintSelected = config.distantWaterMode == DistantWaterMode.FOG_REMAP;
		boolean fabulous = client != null
			&& client.gameRenderer != null
			&& client.gameRenderer.gameRenderState().useShaderTransparency();
		boolean shouldWarn = config.warnMissingImprovedTransparency
			&& fogTintSelected
			&& !fabulous
			&& client != null
			&& client.player != null;

		if (!shouldWarn) {
			FABULOUS_WARN_SENT.set(false);
			return;
		}
		if (!FABULOUS_WARN_SENT.compareAndSet(false, true)) {
			return;
		}
		EcologyClientConfig.notifyPlayerAlways(
			"Ecology Fog tint needs Improved Transparency (Fabulous graphics). Enable it in Video Settings, or switch distant water to Opaque / Off. You can disable this reminder in Ecology → Fog tint."
		);
	}

	/**
	 * @param reloadIfChanged when true, schedule a resource reload if the signature
	 *                        changed from a previously applied non-empty value
	 * @return true if settings were rewritten
	 */
	public static boolean syncFromConfig(boolean reloadIfChanged) {
		EcologyClientConfig config = EcologyClientConfig.get();
		DistantWaterMode mode = config.effectiveMode();
		boolean distance = config.distanceOpacityEnabled;
		float strength = config.clampedStrength();
		float start = config.clampedStart();
		float end = config.clampedEnd();
		boolean fresnel = config.fresnelEnabled;
		float fresnelStrength = config.clampedFresnelStrength();
		float fresnelPower = config.clampedFresnelPower();
		float fogRemapBias = config.clampedFogRemapBiasStrength();
		float underwaterSightStart = config.clampedUnderwaterSightStart();
		float underwaterSightEnd = config.clampedUnderwaterSightEnd();
		boolean sightEndUsePercent = config.underwaterSightEndUseRenderDistancePercent;
		float sightEndPercent = config.clampedUnderwaterSightEndPercent() / 100.0F;
		boolean highlightTops = config.debugHighlightMarkedTops;
		boolean highlightFresnel = config.debugHighlightFresnel;
		boolean highlightAll = config.debugHighlightAllTranslucent;
		boolean highlightFogRemap = config.debugHighlightFogRemap;
		String signature = mode + "|" + distance + "|" + strength + "|" + start + "|" + end
			+ "|" + fresnel + "|" + fresnelStrength + "|" + fresnelPower
			+ "|" + fogRemapBias + "|" + underwaterSightStart + "|" + underwaterSightEnd
			+ "|" + sightEndUsePercent + "|" + sightEndPercent
			+ "|" + config.irisAutoDisable
			+ "|" + highlightTops + "|" + highlightFresnel + "|" + highlightAll
			+ "|" + highlightFogRemap;

		String previous = LAST_SIGNATURE.get();
		if (signature.equals(previous)) {
			return false;
		}

		WaterSurfaceSettingsPack.writeSettings(
			mode, distance, strength, start, end,
			fresnel, fresnelStrength, fresnelPower, fogRemapBias,
			underwaterSightStart, underwaterSightEnd,
			sightEndUsePercent, sightEndPercent,
			highlightTops, highlightFresnel, highlightAll,
			highlightFogRemap
		);
		LAST_SIGNATURE.set(signature);

		boolean shouldReload = reloadIfChanged && previous != null && !previous.isEmpty();
		if (shouldReload) {
			scheduleReload("signatureChange mode=" + mode);
			if (config.debugLogging && (highlightTops || highlightFresnel || highlightAll || highlightFogRemap)) {
				EcologyClientConfig.notifyPlayer(
					"Ecology debug ON — yellow→blue=distance, green→red=fresnel, pink=underwater sight fog (Fabulous), cyan=any translucent."
				);
			}
		}
		return true;
	}

	private static void scheduleReload(String reason) {
		if (!RELOAD_SCHEDULED.compareAndSet(false, true)) {
			return;
		}
		Minecraft client = Minecraft.getInstance();
		if (client == null) {
			RELOAD_SCHEDULED.set(false);
			return;
		}
		client.execute(() -> client.reloadResourcePacks().whenComplete((ignored, error) -> {
			RELOAD_SCHEDULED.set(false);
			loggedResourceCheck = false;
			if (error != null) {
				EcologyMod.LOGGER.error("[Ecology WaterSurface] Resource reload failed ({})", reason, error);
				return;
			}
			EcologyMod.LOGGER.info("[Ecology WaterSurface] Reloaded resources ({})", reason);
			logDiagnostics("afterReload");
		}));
	}

	public static String statusSummary() {
		EcologyClientConfig config = EcologyClientConfig.get();
		return "mode=" + config.effectiveMode()
			+ " configured=" + config.distantWaterMode
			+ " distance=" + config.distanceOpacityEnabled
			+ " irisAutoDisable=" + config.irisAutoDisable
			+ " irisPack=" + IrisCompat.isShaderPackInUse()
			+ " strength=" + config.clampedStrength()
			+ " start=" + config.clampedStart()
			+ " end=" + config.clampedEnd()
			+ " fresnel=" + config.fresnelEnabled
			+ " fresnelStr=" + config.clampedFresnelStrength()
			+ " fresnelPow=" + config.clampedFresnelPower()
			+ " fogBias=" + config.clampedFogRemapBiasStrength()
			+ " sightStart=" + config.clampedUnderwaterSightStart()
			+ " sightEnd=" + config.clampedUnderwaterSightEnd()
			+ " sightEndPct=" + (config.underwaterSightEndUseRenderDistancePercent
				? config.clampedUnderwaterSightEndPercent() + "%"
				: "off")
			+ " fogDark=" + config.clampedFogTintDarkness()
			+ " dbgTops=" + config.debugHighlightMarkedTops
			+ " dbgFresnel=" + config.debugHighlightFresnel
			+ " dbgFogRemap=" + config.debugHighlightFogRemap
			+ " dbgAll=" + config.debugHighlightAllTranslucent
			+ " packDir=" + Files.isDirectory(WaterSurfaceSettingsPack.packRoot());
	}

	public static void logDiagnostics(String reason) {
		EcologyClientConfig config = EcologyClientConfig.get();
		if (!config.debugLogging) {
			return;
		}
		EcologyMod.LOGGER.info("[Ecology WaterSurface] {} -> {}", reason, statusSummary());
		EcologyMod.LOGGER.info("[Ecology WaterSurface] config file: {}", EcologyClientConfig.path().toAbsolutePath());
		EcologyMod.LOGGER.info("[Ecology WaterSurface] settings pack root: {}", WaterSurfaceSettingsPack.packRoot().toAbsolutePath());
		try {
			if (Files.isRegularFile(WaterSurfaceSettingsPack.settingsFile())) {
				EcologyMod.LOGGER.info("[Ecology WaterSurface] generated settings:\n{}", Files.readString(WaterSurfaceSettingsPack.settingsFile()));
			} else {
				EcologyMod.LOGGER.warn("[Ecology WaterSurface] missing generated settings file");
			}
		} catch (IOException e) {
			EcologyMod.LOGGER.warn("[Ecology WaterSurface] could not read generated settings", e);
		}

		Minecraft client = Minecraft.getInstance();
		if (client == null || client.getResourceManager() == null) {
			EcologyMod.LOGGER.info("[Ecology WaterSurface] resource manager not ready yet");
			return;
		}
		Optional<Resource> settings = client.getResourceManager().getResource(SETTINGS_ID);
		Optional<Resource> terrain = client.getResourceManager().getResource(TERRAIN_FSH);
		Optional<Resource> transparency = client.getResourceManager().getResource(TRANSPARENCY_FSH);
		boolean fabulous = client.gameRenderer != null && client.gameRenderer.gameRenderState().useShaderTransparency();
		EcologyMod.LOGGER.info("[Ecology WaterSurface] resource present settings.glsl={} terrain.fsh={} transparency.fsh={} fabulous={} settingsSource={}",
			settings.isPresent(),
			terrain.isPresent(),
			transparency.isPresent(),
			fabulous,
			settings.map(resource -> {
				try {
					return resource.sourcePackId();
				} catch (Exception e) {
					return "?";
				}
			}).orElse("missing"));
		terrain.ifPresent(resource -> {
			try (var in = resource.open()) {
				String text = new String(in.readAllBytes(), StandardCharsets.UTF_8);
				boolean ours = text.contains("EcologyDistantWaterMode") || text.contains("ecologyWaterFace");
				EcologyMod.LOGGER.info("[Ecology WaterSurface] terrain.fsh from pack '{}' looks like Ecology override={}", resource.sourcePackId(), ours);
				if (!ours) {
					EcologyMod.LOGGER.warn("[Ecology WaterSurface] terrain.fsh is NOT Ecology's — another pack/Sodium may be winning");
				}
			} catch (Exception e) {
				EcologyMod.LOGGER.warn("[Ecology WaterSurface] could not read terrain.fsh contents", e);
			}
		});
		transparency.ifPresent(resource -> {
			try (var in = resource.open()) {
				String text = new String(in.readAllBytes(), StandardCharsets.UTF_8);
				boolean ours = text.contains("EcologyUnderwaterSightEnd") || text.contains("ecologyDecodeWaterMask");
				EcologyMod.LOGGER.info("[Ecology WaterSurface] transparency.fsh from pack '{}' looks like Ecology override={}", resource.sourcePackId(), ours);
				if (!ours) {
					EcologyMod.LOGGER.warn("[Ecology WaterSurface] transparency.fsh is NOT Ecology's — fog tint will not run");
				}
			} catch (Exception e) {
				EcologyMod.LOGGER.warn("[Ecology WaterSurface] could not read transparency.fsh contents", e);
			}
		});
		if (!fabulous && config.distantWaterMode == DistantWaterMode.FOG_REMAP) {
			EcologyMod.LOGGER.warn("[Ecology WaterSurface] Fog tint needs Improved Transparency (Fabulous). Current graphics will not run the transparency composite.");
		}
		if (client.getResourcePackRepository() != null) {
			boolean selected = client.getResourcePackRepository().getSelectedIds().contains(WaterSurfaceSettingsPack.PACK_ID);
			boolean available = client.getResourcePackRepository().getAvailableIds().contains(WaterSurfaceSettingsPack.PACK_ID);
			EcologyMod.LOGGER.info("[Ecology WaterSurface] pack available={} selected={}", available, selected);
			if (!available) {
				EcologyMod.LOGGER.warn("[Ecology WaterSurface] settings pack is NOT in the repository — MinecraftMixin pack source may have failed");
			} else if (!selected) {
				EcologyMod.LOGGER.warn("[Ecology WaterSurface] settings pack exists but is not selected");
			}
		}
	}

	public static RepositorySource repositorySource() {
		return WaterSurfaceSettingsPack.repositorySource();
	}
}
