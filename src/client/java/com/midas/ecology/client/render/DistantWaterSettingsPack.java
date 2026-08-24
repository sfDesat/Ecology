package com.midas.ecology.client.render;

import com.midas.ecology.EcologyMod;
import com.midas.ecology.client.config.DistantWaterMode;
import com.midas.ecology.client.config.EcologyClientConfig;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.network.chat.Component;
import net.minecraft.server.packs.PackLocationInfo;
import net.minecraft.server.packs.PackSelectionConfig;
import net.minecraft.server.packs.PackType;
import net.minecraft.server.packs.PathPackResources;
import net.minecraft.server.packs.repository.Pack;
import net.minecraft.server.packs.repository.PackSource;
import net.minecraft.server.packs.repository.RepositorySource;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.Optional;
import java.util.function.Consumer;

/**
 * Runtime overlay pack: {@code distant_water_settings.glsl}, and when Sodium is loaded,
 * Ecology forks of {@code sodium:blocks/block_layer_opaque} + {@code sodium:include/globals}.
 */
public final class DistantWaterSettingsPack {
	public static final String PACK_ID = "ecology/distant_water_settings";
	/** Minecraft 26.2 client resource pack format. Bump when porting. */
	public static final int PACK_FORMAT_MAJOR = 88;

	private static final Path PACK_ROOT = FabricLoader.getInstance().getConfigDir().resolve("ecology/distant_water_pack");

	private static final String[] SODIUM_OVERLAY_FILES = {
		"include/globals.glsl",
		"blocks/block_layer_opaque.vsh",
		"blocks/block_layer_opaque.fsh"
	};

	private DistantWaterSettingsPack() {
	}

	public static Path packRoot() {
		return PACK_ROOT;
	}

	public static Path settingsFile() {
		return PACK_ROOT.resolve("assets/ecology/shaders/include/distant_water_settings.glsl");
	}

	public static Path sodiumBlockLayerFsh() {
		return PACK_ROOT.resolve("assets/sodium/shaders/blocks/block_layer_opaque.fsh");
	}

	public static RepositorySource repositorySource() {
		return DistantWaterSettingsPack::loadPack;
	}

	private static void loadPack(Consumer<Pack> onLoad) {
		EcologyClientConfig.ensureLoaded();
		DistantWaterShaderSupport.syncFromConfig(false);
		PackLocationInfo location = new PackLocationInfo(
			PACK_ID,
			Component.literal("Ecology Distant Water Settings"),
			PackSource.BUILT_IN,
			Optional.empty()
		);
		PackSelectionConfig selection = new PackSelectionConfig(true, Pack.Position.TOP, false);
		Pack pack = Pack.readMetaAndCreate(
			location,
			new PathPackResources.PathResourcesSupplier(PACK_ROOT),
			PackType.CLIENT_RESOURCES,
			selection
		);
		if (pack != null) {
			onLoad.accept(pack);
		} else {
			EcologyMod.LOGGER.error("[Ecology DistantWater] Failed to load settings pack from {} (pack.mcmeta invalid?)", PACK_ROOT);
		}
	}

	private static boolean overlayCopiedThisRuntime;

	public static void writeSettings(
		DistantWaterMode mode,
		boolean sodiumOverlay,
		boolean distance,
		float strength,
		float start,
		float end,
		boolean fresnel,
		float fresnelStrength,
		float fresnelPower,
		float fogTintFill,
		float sightFogStart,
		float sightFogEnd,
		boolean sightEndUsePercent,
		float sightStartPercent,
		float sightEndPercent,
		float surfaceAirFog,
		boolean highlightTops,
		boolean highlightFresnel,
		boolean highlightAll,
		boolean highlightFogRemap
	) {
		try {
			Path assets = PACK_ROOT.resolve("assets/ecology/shaders/include");
			Files.createDirectories(assets);
			Files.writeString(PACK_ROOT.resolve("pack.mcmeta"), packMcmeta(), StandardCharsets.UTF_8);
			Files.writeString(
				assets.resolve("distant_water_settings.glsl"),
				buildSettingsGlsl(
					mode, distance, strength, start, end,
					fresnel, fresnelStrength, fresnelPower, fogTintFill,
					sightFogStart, sightFogEnd,
					sightEndUsePercent, sightStartPercent, sightEndPercent,
					surfaceAirFog,
					highlightTops, highlightFresnel, highlightAll,
					highlightFogRemap
				),
				StandardCharsets.UTF_8
			);
			writeSodiumOverlay(sodiumOverlay);
		} catch (IOException e) {
			EcologyMod.LOGGER.error("[Ecology DistantWater] Failed to write settings pack", e);
			EcologyClientConfig.notifyPlayer(Component.translatable("ecology.config.chat.pack_failed"));
		}
	}

	/**
	 * When distant water is on and Sodium is present, copy Ecology's Sodium shader forks
	 * so they win over Sodium's jar (pack is {@link Pack.Position#TOP}). Off removes them.
	 * Overlay files do not depend on slider consts — copy once per runtime while enabled.
	 */
	private static void writeSodiumOverlay(boolean enable) throws IOException {
		Path sodiumRoot = PACK_ROOT.resolve("assets/sodium/shaders");
		if (!enable) {
			deleteSodiumOverlay(sodiumRoot);
			overlayCopiedThisRuntime = false;
			return;
		}
		if (overlayCopiedThisRuntime) {
			return;
		}
		var container = FabricLoader.getInstance().getModContainer(EcologyMod.MOD_ID)
			.orElseThrow(() -> new IOException("Ecology mod container missing"));
		for (String relative : SODIUM_OVERLAY_FILES) {
			String modPath = "assets/ecology/sodium_overlay/" + relative;
			Path out = sodiumRoot.resolve(relative);
			Files.createDirectories(out.getParent());
			Path in = container.findPath(modPath)
				.orElseThrow(() -> new IOException("Missing Sodium overlay resource: " + modPath));
			Files.copy(in, out, StandardCopyOption.REPLACE_EXISTING);
		}
		overlayCopiedThisRuntime = true;
		EcologyMod.LOGGER.info(
			"[Ecology DistantWater] Wrote Sodium shader overlay ({} files) into {}",
			SODIUM_OVERLAY_FILES.length,
			sodiumRoot
		);
	}

	private static void deleteSodiumOverlay(Path sodiumRoot) throws IOException {
		if (!Files.isDirectory(sodiumRoot)) {
			return;
		}
		for (String relative : SODIUM_OVERLAY_FILES) {
			Files.deleteIfExists(sodiumRoot.resolve(relative));
		}
	}

	private static String packMcmeta() {
		return """
			{
			  "pack": {
			    "description": "Ecology Distant Water Settings overlay",
			    "min_format": [%d, 0],
			    "max_format": [%d, 0]
			  }
			}
			""".formatted(PACK_FORMAT_MAJOR, PACK_FORMAT_MAJOR);
	}

	private static String buildSettingsGlsl(
		DistantWaterMode mode,
		boolean distance,
		float strength,
		float start,
		float end,
		boolean fresnel,
		float fresnelStrength,
		float fresnelPower,
		float fogTintFill,
		float sightFogStart,
		float sightFogEnd,
		boolean sightEndUsePercent,
		float sightStartPercent,
		float sightEndPercent,
		float surfaceAirFog,
		boolean highlightTops,
		boolean highlightFresnel,
		boolean highlightAll,
		boolean highlightFogRemap
	) {
		boolean anyActive = mode != DistantWaterMode.OFF;
		return """
			// EcologyDistantWaterMode: 0=OFF 1=OPACITY 2=FOG_REMAP (Fog tint)
			const float EcologyDistantWaterMode = %s;
			const float EcologyWaterShaderEnabled = %s;
			const float EcologyDistanceOpacityEnabled = %s;
			const float EcologyDistanceOpacityStrength = %s;
			const float EcologyDistanceOpacityStart = %s;
			const float EcologyDistanceOpacityEnd = %s;
			const float EcologyFresnelEnabled = %s;
			const float EcologyFresnelStrength = %s;
			const float EcologyFresnelPower = %s;
			const float EcologyFogTintFillStrength = %s;
			const float EcologySightFogStart = %s;
			const float EcologySightFogEnd = %s;
			const float EcologySightFogEndUsePercent = %s;
			const float EcologySightFogStartPercent = %s;
			const float EcologySightFogEndPercent = %s;
			const float EcologyWaterDebugTops = %s;
			const float EcologyWaterDebugFresnel = %s;
			const float EcologyWaterDebugAll = %s;
			const float EcologyWaterDebugFogRemap = %s;
			const float EcologySurfaceAirFog = %s;
			""".formatted(
			formatFloat(mode.shaderValue()),
			anyActive ? "1.0" : "0.0",
			distance ? "1.0" : "0.0",
			formatFloat(strength),
			formatFloat(start),
			formatFloat(end),
			fresnel ? "1.0" : "0.0",
			formatFloat(fresnelStrength),
			formatFloat(fresnelPower),
			formatFloat(fogTintFill),
			formatFloat(sightFogStart),
			formatFloat(sightFogEnd),
			sightEndUsePercent ? "1.0" : "0.0",
			formatFloat(sightStartPercent),
			formatFloat(sightEndPercent),
			highlightTops ? "1.0" : "0.0",
			highlightFresnel ? "1.0" : "0.0",
			highlightAll ? "1.0" : "0.0",
			highlightFogRemap ? "1.0" : "0.0",
			formatFloat(surfaceAirFog)
		);
	}

	private static String formatFloat(float value) {
		return String.format(java.util.Locale.ROOT, "%.4f", value);
	}
}
