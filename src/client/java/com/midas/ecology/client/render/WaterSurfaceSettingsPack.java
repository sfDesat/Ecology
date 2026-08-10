package com.midas.ecology.client.render;

import com.midas.ecology.EcologyMod;
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
import java.util.Optional;
import java.util.function.Consumer;

/**
 * Runtime overlay pack that only contains {@code distant_water_settings.glsl}
 * so config can change without shipping a second copy of {@code core/terrain}.
 */
public final class WaterSurfaceSettingsPack {
	public static final String PACK_ID = "ecology/distant_water_settings";
	/** Minecraft 26.2 client resource pack format. Bump when porting. */
	public static final int PACK_FORMAT_MAJOR = 88;

	private static final Path PACK_ROOT = FabricLoader.getInstance().getConfigDir().resolve("ecology/distant_water_pack");

	private WaterSurfaceSettingsPack() {
	}

	public static Path packRoot() {
		return PACK_ROOT;
	}

	public static Path settingsFile() {
		return PACK_ROOT.resolve("assets/ecology/shaders/include/distant_water_settings.glsl");
	}

	public static RepositorySource repositorySource() {
		return WaterSurfaceSettingsPack::loadPack;
	}

	private static void loadPack(Consumer<Pack> onLoad) {
		EcologyClientConfig.ensureLoaded();
		WaterSurfaceShaderSupport.syncFromConfig(false);
		PackLocationInfo location = new PackLocationInfo(
			PACK_ID,
			Component.literal("Ecology Water Surface Settings"),
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
			EcologyMod.LOGGER.error("[Ecology WaterSurface] Failed to load settings pack from {} (pack.mcmeta invalid?)", PACK_ROOT);
		}
	}

	public static void writeSettings(
		boolean active,
		boolean distance,
		float strength,
		float start,
		float end,
		boolean fresnel,
		float fresnelStrength,
		float fresnelPower,
		boolean highlightTops,
		boolean highlightFresnel,
		boolean highlightAll
	) {
		try {
			Path assets = PACK_ROOT.resolve("assets/ecology/shaders/include");
			Files.createDirectories(assets);
			Files.writeString(PACK_ROOT.resolve("pack.mcmeta"), packMcmeta(), StandardCharsets.UTF_8);
			Files.writeString(
				assets.resolve("distant_water_settings.glsl"),
				buildSettingsGlsl(active, distance, strength, start, end, fresnel, fresnelStrength, fresnelPower, highlightTops, highlightFresnel, highlightAll),
				StandardCharsets.UTF_8
			);
		} catch (IOException e) {
			EcologyMod.LOGGER.error("[Ecology WaterSurface] Failed to write settings pack", e);
			EcologyClientConfig.notifyPlayer("Ecology: failed to write shader settings pack (see log)");
		}
	}

	private static String packMcmeta() {
		return """
			{
			  "pack": {
			    "description": "Ecology water surface settings overlay",
			    "min_format": [%d, 0],
			    "max_format": [%d, 0]
			  }
			}
			""".formatted(PACK_FORMAT_MAJOR, PACK_FORMAT_MAJOR);
	}

	private static String buildSettingsGlsl(
		boolean active,
		boolean distance,
		float strength,
		float start,
		float end,
		boolean fresnel,
		float fresnelStrength,
		float fresnelPower,
		boolean highlightTops,
		boolean highlightFresnel,
		boolean highlightAll
	) {
		return """
			const float EcologyWaterShaderEnabled = %s;
			const float EcologyDistanceOpacityEnabled = %s;
			const float EcologyDistanceOpacityStrength = %s;
			const float EcologyDistanceOpacityStart = %s;
			const float EcologyDistanceOpacityEnd = %s;
			const float EcologyFresnelEnabled = %s;
			const float EcologyFresnelStrength = %s;
			const float EcologyFresnelPower = %s;
			const float EcologyWaterDebugTops = %s;
			const float EcologyWaterDebugFresnel = %s;
			const float EcologyWaterDebugAll = %s;
			""".formatted(
			active ? "1.0" : "0.0",
			distance ? "1.0" : "0.0",
			formatFloat(strength),
			formatFloat(start),
			formatFloat(end),
			fresnel ? "1.0" : "0.0",
			formatFloat(fresnelStrength),
			formatFloat(fresnelPower),
			highlightTops ? "1.0" : "0.0",
			highlightFresnel ? "1.0" : "0.0",
			highlightAll ? "1.0" : "0.0"
		);
	}

	private static String formatFloat(float value) {
		return String.format(java.util.Locale.ROOT, "%.4f", value);
	}
}
