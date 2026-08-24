package com.midas.ecology.client.render;

import com.midas.ecology.EcologyMod;
import com.midas.ecology.client.compat.IrisCompat;
import com.midas.ecology.client.compat.SodiumCompat;
import com.midas.ecology.client.config.DistantWaterMode;
import com.midas.ecology.client.config.EcologyClientConfig;
import com.midas.ecology.client.render.fog.FogTint;
import com.midas.ecology.client.render.fog.FogTintMatrices;
import net.minecraft.client.Minecraft;
import net.minecraft.server.packs.repository.RepositorySource;

import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;

/**
 * Syncs distant-water settings into {@link DistantWaterSettingsPack} and
 * reloads resources only when the effective shader signature changes.
 */
public final class DistantWaterShaderSupport {
	private static final AtomicReference<String> LAST_SIGNATURE = new AtomicReference<>("");
	private static final AtomicBoolean RELOAD_SCHEDULED = new AtomicBoolean(false);
	private static final AtomicBoolean FABULOUS_WARN_SENT = new AtomicBoolean(false);
	private static int tickCounter;
	private static Boolean lastTagging;
	private static volatile boolean overlayDesired;
	private static volatile boolean overlayLive;

	private DistantWaterShaderSupport() {
	}

	/**
	 * Mode written into shader consts and used for tagging / Sodium overlay.
	 * Fog tint without Fabulous is Off in shaders so water alpha is never encoded without a decoder.
	 */
	public static DistantWaterMode shaderMode() {
		DistantWaterMode mode = EcologyClientConfig.get().effectiveMode();
		if (mode == DistantWaterMode.FOG_REMAP && !FogTint.isFabulousTransparency()) {
			return DistantWaterMode.OFF;
		}
		return mode;
	}

	/** Sodium terrain overlay + {@code u_Globals} extras — true only while those shaders are actually loaded. */
	public static boolean sodiumOverlayActive() {
		return overlayLive;
	}

	/** Write settings without forcing a resource reload (startup / pack registration). */
	public static void applyConfigQuiet() {
		EcologyClientConfig.ensureLoaded();
		syncFromConfig(false);
		DistantWaterDiagnostics.log("applyConfigQuiet");
	}

	/** Rewrite settings after the config screen saves; reload only if values actually changed. */
	public static void applyConfigAndReload() {
		EcologyClientConfig.ensureLoaded();
		syncFromConfig(true);
		DistantWaterDiagnostics.log("applyConfigAndReload");
		EcologyClientConfig.notifyPlayer("Ecology distant water: " + DistantWaterDiagnostics.statusSummary());
		FABULOUS_WARN_SENT.set(false);
		maybeWarnMissingFabulous();
	}

	public static void clientTick() {
		if (++tickCounter < 40) {
			return;
		}
		tickCounter = 0;
		syncFromConfig(true);
		maybeWarnMissingFabulous();
		DistantWaterDiagnostics.logFirstResourceCheck();
	}

	/**
	 * Fog tint needs Fabulous / Improved Transparency. Chat once while the mismatch lasts
	 * (resets when Fabulous is on, mode changes, or the warn toggle is off).
	 */
	public static void maybeWarnMissingFabulous() {
		EcologyClientConfig config = EcologyClientConfig.get();
		Minecraft client = Minecraft.getInstance();
		boolean fogTintSelected = config.distantWaterMode == DistantWaterMode.FOG_REMAP;
		boolean fabulous = FogTint.isFabulousTransparency();
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
		DistantWaterMode mode = shaderMode();
		boolean distance = config.distanceOpacityEnabled;
		float strength = config.clampedStrength();
		float start = config.clampedStart();
		float end = config.clampedEnd();
		boolean fresnel = config.fresnelEnabled;
		float fresnelStrength = config.clampedFresnelStrength();
		float fresnelPower = config.clampedFresnelPower();
		float fogTintFill = config.clampedFogTintFillStrength();
		float sightFogStart = config.clampedSightFogStart();
		float sightFogEnd = config.clampedSightFogEnd();
		boolean sightEndUsePercent = config.sightFogUseRenderDistancePercent;
		float sightStartPercent = config.clampedSightFogStartPercent() / 100.0F;
		float sightEndPercent = config.clampedSightFogEndPercent() / 100.0F;
		boolean highlightTops = config.debugHighlightMarkedTops;
		boolean highlightFresnel = config.debugHighlightFresnel;
		boolean highlightAll = config.debugHighlightAllTranslucent;
		boolean highlightFogRemap = config.debugHighlightFogRemap;
		float surfaceAirFog = config.clampedSurfaceAirFog();
		boolean overlay = SodiumCompat.isLoaded() && mode != DistantWaterMode.OFF;
		boolean fabulous = FogTint.isFabulousTransparency();
		String signature = mode + "|" + distance + "|" + strength + "|" + start + "|" + end
			+ "|" + fresnel + "|" + fresnelStrength + "|" + fresnelPower
			+ "|" + fogTintFill + "|" + sightFogStart + "|" + sightFogEnd
			+ "|" + sightEndUsePercent + "|" + sightStartPercent + "|" + sightEndPercent
			+ "|" + surfaceAirFog
			+ "|" + config.irisAutoDisable
			+ "|" + IrisCompat.isShaderPackInUse()
			+ "|" + overlay
			+ "|" + fabulous
			+ "|" + highlightTops + "|" + highlightFresnel + "|" + highlightAll
			+ "|" + highlightFogRemap;

		String previous = LAST_SIGNATURE.get();
		overlayDesired = overlay;
		if (signature.equals(previous)) {
			syncTagging();
			return false;
		}

		DistantWaterSettingsPack.writeSettings(
			mode, overlay, distance, strength, start, end,
			fresnel, fresnelStrength, fresnelPower, fogTintFill,
			sightFogStart, sightFogEnd,
			sightEndUsePercent, sightStartPercent, sightEndPercent,
			surfaceAirFog,
			highlightTops, highlightFresnel, highlightAll,
			highlightFogRemap
		);
		LAST_SIGNATURE.set(signature);
		syncTagging();

		boolean shouldReload = reloadIfChanged && previous != null && !previous.isEmpty();
		if (shouldReload) {
			scheduleReload("signatureChange mode=" + mode);
			if (config.debugLogging && (highlightTops || highlightFresnel || highlightAll || highlightFogRemap)) {
				EcologyClientConfig.notifyPlayer(
					"Ecology debug ON — yellow→blue=distance, green→red=fresnel, pink=behind-water Fog tint (Fabulous), cyan=any translucent."
				);
			}
		} else {
			overlayLive = overlayDesired;
		}
		return true;
	}

	private static void syncTagging() {
		WaterFaceMarker.refresh();
		boolean tagging = WaterFaceMarker.tagging();
		if (lastTagging != null && lastTagging != tagging) {
			rebuildChunks();
		}
		lastTagging = tagging;
	}

	private static void rebuildChunks() {
		Minecraft client = Minecraft.getInstance();
		if (client == null || client.level == null || client.levelRenderer == null || client.gameRenderer == null) {
			return;
		}
		client.levelRenderer.invalidateCompiledGeometry(
			client.level,
			client.options,
			client.gameRenderer.mainCamera(),
			client.getBlockColors()
		);
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
			FogTintMatrices.releaseBuffer();
			DistantWaterDiagnostics.resetResourceCheck();
			if (error != null) {
				EcologyMod.LOGGER.error("[Ecology DistantWater] Resource reload failed ({})", reason, error);
				return;
			}
			EcologyMod.LOGGER.info("[Ecology DistantWater] Reloaded resources ({})", reason);
			overlayLive = overlayDesired;
			DistantWaterDiagnostics.log("afterReload");
		}));
	}

	public static String statusSummary() {
		return DistantWaterDiagnostics.statusSummary();
	}

	public static RepositorySource repositorySource() {
		return DistantWaterSettingsPack.repositorySource();
	}
}
