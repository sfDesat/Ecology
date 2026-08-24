package com.midas.ecology.client.render;

import com.midas.ecology.EcologyMod;
import com.midas.ecology.client.compat.IrisCompat;
import com.midas.ecology.client.compat.SodiumCompat;
import com.midas.ecology.client.config.DistantWaterMode;
import com.midas.ecology.client.config.EcologyClientConfig;
import com.midas.ecology.client.render.fog.FogTint;
import net.minecraft.client.Minecraft;
import net.minecraft.resources.Identifier;
import net.minecraft.server.packs.resources.Resource;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.Optional;

/**
 * Debug-only distant-water status (chat/log). Not on the render path.
 */
public final class DistantWaterDiagnostics {
	private static final Identifier SETTINGS_ID = Identifier.fromNamespaceAndPath("ecology", "shaders/include/distant_water_settings.glsl");
	private static final Identifier TERRAIN_FSH = Identifier.fromNamespaceAndPath("minecraft", "shaders/core/terrain.fsh");
	private static final Identifier TRANSPARENCY_FSH = Identifier.fromNamespaceAndPath("minecraft", "shaders/post/transparency.fsh");
	private static final Identifier SODIUM_BLOCK_FSH = Identifier.fromNamespaceAndPath("sodium", "shaders/blocks/block_layer_opaque.fsh");

	private static boolean loggedResourceCheck;

	private DistantWaterDiagnostics() {
	}

	public static void resetResourceCheck() {
		loggedResourceCheck = false;
	}

	public static void logFirstResourceCheck() {
		if (loggedResourceCheck) {
			return;
		}
		Minecraft client = Minecraft.getInstance();
		if (client == null || client.getResourceManager() == null) {
			return;
		}
		loggedResourceCheck = true;
		log("firstResourceCheck");
	}

	public static String statusSummary() {
		EcologyClientConfig config = EcologyClientConfig.get();
		EcologyClientConfig.LookingAtWater looking = config.lookingAtWater;
		EcologyClientConfig.Swimming swimming = config.swimming;
		EcologyClientConfig.OpaqueWater opaque = config.opaqueWater;
		return "mode=" + DistantWaterShaderSupport.shaderMode()
			+ " configured=" + config.mode
			+ " effective=" + config.effectiveMode()
			+ " distance=" + opaque.distance
			+ " pauseWithIris=" + config.pauseWithIris
			+ " irisPack=" + IrisCompat.isShaderPackInUse()
			+ " sodium=" + SodiumCompat.isLoaded()
			+ " overlay=" + DistantWaterShaderSupport.sodiumOverlayActive()
			+ " fabulous=" + FogTint.isFabulousTransparency()
			+ " strength=" + opaque.clampedStrength()
			+ " start=" + opaque.clampedStart()
			+ " end=" + opaque.clampedEnd()
			+ " angle=" + opaque.angle
			+ " angleStr=" + opaque.clampedAngleStrength()
			+ " angleCurve=" + opaque.clampedAngleCurve()
			+ " fill=" + looking.clampedFill()
			+ " sightStart=" + looking.clampedStartBlocks()
			+ " sightEnd=" + looking.clampedEndBlocks()
			+ " sightPct=" + (looking.usePercent
				? looking.clampedStartPercent() + "-" + looking.clampedEndPercent() + "%"
				: "off")
			+ " fogDark=" + looking.clampedFogDarkness()
			+ " surfaceFog=" + looking.clampedHorizonFog()
			+ " uwLight=" + swimming.clampedBrightUntil() + "-" + swimming.clampedDarkAt()
			+ " swimFallback=" + swimming.clampedOtherWater()
			+ " swimFog=" + swimming.kelpForest + "/" + swimming.cold
			+ "/" + swimming.temperate + "/" + swimming.iceOpenings
			+ "/" + swimming.lukewarm + "/" + swimming.deepBasin
			+ "/" + swimming.warm + "/" + swimming.lagoon
			+ "/" + swimming.openOcean
			+ " dbgTops=" + config.debug.highlightMarkedWater
			+ " dbgAngle=" + config.debug.highlightAngle
			+ " dbgSeeThrough=" + config.debug.highlightSeeThrough
			+ " dbgAll=" + config.debug.highlightTranslucent
			+ " packDir=" + Files.isDirectory(DistantWaterSettingsPack.packRoot());
	}

	public static void log(String reason) {
		EcologyClientConfig config = EcologyClientConfig.get();
		if (!config.debug.logging) {
			return;
		}
		EcologyMod.LOGGER.info("[Ecology DistantWater] {} -> {}", reason, statusSummary());
		EcologyMod.LOGGER.info("[Ecology DistantWater] config file: {}", EcologyClientConfig.path().toAbsolutePath());
		EcologyMod.LOGGER.info("[Ecology DistantWater] settings pack root: {}", DistantWaterSettingsPack.packRoot().toAbsolutePath());
		try {
			if (Files.isRegularFile(DistantWaterSettingsPack.settingsFile())) {
				EcologyMod.LOGGER.info("[Ecology DistantWater] generated settings:\n{}", Files.readString(DistantWaterSettingsPack.settingsFile()));
			} else {
				EcologyMod.LOGGER.warn("[Ecology DistantWater] missing generated settings file");
			}
		} catch (IOException e) {
			EcologyMod.LOGGER.warn("[Ecology DistantWater] could not read generated settings", e);
		}

		Minecraft client = Minecraft.getInstance();
		if (client == null || client.getResourceManager() == null) {
			EcologyMod.LOGGER.info("[Ecology DistantWater] resource manager not ready yet");
			return;
		}
		Optional<Resource> settings = client.getResourceManager().getResource(SETTINGS_ID);
		Optional<Resource> terrain = client.getResourceManager().getResource(TERRAIN_FSH);
		Optional<Resource> transparency = client.getResourceManager().getResource(TRANSPARENCY_FSH);
		Optional<Resource> sodiumBlock = client.getResourceManager().getResource(SODIUM_BLOCK_FSH);
		boolean fabulous = FogTint.isFabulousTransparency();
		EcologyMod.LOGGER.info("[Ecology DistantWater] resource present settings.glsl={} terrain.fsh={} transparency.fsh={} sodiumBlock.fsh={} fabulous={} sodiumMod={} overlay={} settingsSource={}",
			settings.isPresent(),
			terrain.isPresent(),
			transparency.isPresent(),
			sodiumBlock.isPresent(),
			fabulous,
			SodiumCompat.isLoaded(),
			DistantWaterShaderSupport.sodiumOverlayActive(),
			settings.map(resource -> {
				try {
					return resource.sourcePackId();
				} catch (Exception e) {
					return "?";
				}
			}).orElse("missing"));
		if (!SodiumCompat.isLoaded()) {
			terrain.ifPresent(resource -> {
				try (var in = resource.open()) {
					String text = new String(in.readAllBytes(), StandardCharsets.UTF_8);
					boolean ours = text.contains("EcologyDistantWaterMode") || text.contains("ecologyWaterFace");
					EcologyMod.LOGGER.info("[Ecology DistantWater] terrain.fsh from pack '{}' looks like Ecology override={}", resource.sourcePackId(), ours);
					if (!ours) {
						EcologyMod.LOGGER.warn("[Ecology DistantWater] terrain.fsh is NOT Ecology's — another pack may be winning");
					}
				} catch (Exception e) {
					EcologyMod.LOGGER.warn("[Ecology DistantWater] could not read terrain.fsh contents", e);
				}
			});
		} else if (DistantWaterShaderSupport.sodiumOverlayActive()) {
			sodiumBlock.ifPresentOrElse(resource -> {
				try (var in = resource.open()) {
					String text = new String(in.readAllBytes(), StandardCharsets.UTF_8);
					boolean ours = text.contains("EcologyDistantWaterMode") || text.contains("ecologyWaterFace");
					EcologyMod.LOGGER.info("[Ecology DistantWater] sodium block_layer_opaque.fsh from pack '{}' looks like Ecology override={}", resource.sourcePackId(), ours);
					if (!ours) {
						EcologyMod.LOGGER.warn("[Ecology DistantWater] Sodium terrain shader is NOT Ecology's — overlay pack may have failed");
					}
				} catch (Exception e) {
					EcologyMod.LOGGER.warn("[Ecology DistantWater] could not read Sodium block_layer_opaque.fsh contents", e);
				}
			}, () -> EcologyMod.LOGGER.warn("[Ecology DistantWater] sodium:shaders/blocks/block_layer_opaque.fsh missing from resource manager"));
		} else {
			EcologyMod.LOGGER.info("[Ecology DistantWater] Sodium overlay idle (Distant water Off)");
		}
		transparency.ifPresent(resource -> {
			try (var in = resource.open()) {
				String text = new String(in.readAllBytes(), StandardCharsets.UTF_8);
				boolean ours = text.contains("EcologySightFogEnd") || text.contains("ecologyDecodeWaterMask");
				EcologyMod.LOGGER.info("[Ecology DistantWater] transparency.fsh from pack '{}' looks like Ecology override={}", resource.sourcePackId(), ours);
				if (!ours) {
					EcologyMod.LOGGER.warn("[Ecology DistantWater] transparency.fsh is NOT Ecology's — Fog tint will not run");
				}
			} catch (Exception e) {
				EcologyMod.LOGGER.warn("[Ecology DistantWater] could not read transparency.fsh contents", e);
			}
		});
		if (!fabulous && config.mode == DistantWaterMode.FOG_REMAP) {
			EcologyMod.LOGGER.warn("[Ecology DistantWater] Fog tint needs Improved Transparency (Fabulous). Current graphics will not encode or composite Fog tint.");
		}
		if (client.getResourcePackRepository() != null) {
			boolean selected = client.getResourcePackRepository().getSelectedIds().contains(DistantWaterSettingsPack.PACK_ID);
			boolean available = client.getResourcePackRepository().getAvailableIds().contains(DistantWaterSettingsPack.PACK_ID);
			EcologyMod.LOGGER.info("[Ecology DistantWater] pack available={} selected={}", available, selected);
			if (!available) {
				EcologyMod.LOGGER.warn("[Ecology DistantWater] settings pack is NOT in the repository — MinecraftMixin pack source may have failed");
			} else if (!selected) {
				EcologyMod.LOGGER.warn("[Ecology DistantWater] settings pack exists but is not selected");
			}
		}
	}
}
