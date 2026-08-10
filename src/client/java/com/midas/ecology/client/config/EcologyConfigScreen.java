package com.midas.ecology.client.config;

import com.midas.ecology.EcologyMod;
import com.midas.ecology.client.render.WaterSurfaceShaderSupport;
import me.shedaniel.clothconfig2.api.ConfigBuilder;
import me.shedaniel.clothconfig2.api.ConfigCategory;
import me.shedaniel.clothconfig2.api.ConfigEntryBuilder;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

public final class EcologyConfigScreen {
	private EcologyConfigScreen() {
	}

	public static Screen create(Screen parent) {
		EcologyClientConfig config = EcologyClientConfig.get();
		ConfigBuilder builder = ConfigBuilder.create()
			.setParentScreen(parent)
			.setTitle(Component.literal("Ecology Client Config"))
			.setSavingRunnable(() -> {
				EcologyClientConfig.save();
				WaterSurfaceShaderSupport.applyConfigAndReload();
				EcologyClientConfig.notifyPlayer("Ecology config saved. " + WaterSurfaceShaderSupport.statusSummary());
				EcologyMod.LOGGER.info("Ecology config saved via Cloth: {}", WaterSurfaceShaderSupport.statusSummary());
			});

		ConfigEntryBuilder entries = builder.entryBuilder();
		ConfigCategory water = builder.getOrCreateCategory(Component.literal("Water Surface Opacity"));

		water.addEntry(entries.startBooleanToggle(Component.literal("Enable water shader (master)"), config.waterShaderEnabled)
			.setDefaultValue(true)
			.setTooltip(Component.literal("Master switch for Ecology water surface opacity (distance + fresnel). Off = vanilla water."))
			.setSaveConsumer(value -> config.waterShaderEnabled = value)
			.build());

		water.addEntry(entries.startBooleanToggle(Component.literal("Distance opacity"), config.distanceOpacityEnabled)
			.setDefaultValue(true)
			.setTooltip(Component.literal("Distance-based opacity. Turn off to test fresnel alone."))
			.setSaveConsumer(value -> config.distanceOpacityEnabled = value)
			.build());

		water.addEntry(entries.startFloatField(Component.literal("Distance strength (0-1)"), config.distantWaterOpacityStrength)
			.setDefaultValue(1.0F)
			.setMin(0.0F)
			.setMax(1.0F)
			.setTooltip(Component.literal("At End distance: 1.0 = fully opaque from distance alone."))
			.setSaveConsumer(value -> config.distantWaterOpacityStrength = value)
			.build());

		water.addEntry(entries.startFloatField(Component.literal("Start distance (0-1)"), config.distantWaterOpacityStart)
			.setDefaultValue(0.0F)
			.setMin(0.0F)
			.setMax(1.0F)
			.setTooltip(Component.literal("Where distance opacity begins, as a fraction of render-distance fog end."))
			.setSaveConsumer(value -> config.distantWaterOpacityStart = value)
			.build());

		water.addEntry(entries.startFloatField(Component.literal("End distance (0-1)"), config.distantWaterOpacityEnd)
			.setDefaultValue(0.5F)
			.setMin(0.0F)
			.setMax(1.0F)
			.setTooltip(Component.literal("Where distance opacity reaches full strength. Must be >= Start."))
			.setSaveConsumer(value -> config.distantWaterOpacityEnd = value)
			.build());

		water.addEntry(entries.startBooleanToggle(Component.literal("Fresnel (angle opacity)"), config.fresnelEnabled)
			.setDefaultValue(true)
			.setTooltip(Component.literal("Adds glancing-angle opacity. Combined with distance, capped at 1. Can run alone if Distance opacity is off."))
			.setSaveConsumer(value -> config.fresnelEnabled = value)
			.build());

		water.addEntry(entries.startFloatField(Component.literal("Fresnel strength (0-1)"), config.fresnelStrength)
			.setDefaultValue(1.0F)
			.setMin(0.0F)
			.setMax(1.0F)
			.setTooltip(Component.literal("How much glancing-angle opacity to ADD. Total with distance is clamped to 1."))
			.setSaveConsumer(value -> config.fresnelStrength = value)
			.build());

		water.addEntry(entries.startFloatField(Component.literal("Fresnel power / angle curve (0.25-8)"), config.fresnelPower)
			.setDefaultValue(0.75F)
			.setMin(0.25F)
			.setMax(8.0F)
			.setTooltip(Component.literal("pow(grazing, power). Lower = more angles get opaque. Higher = only near-horizon."))
			.setSaveConsumer(value -> config.fresnelPower = value)
			.build());

		water.addEntry(entries.startBooleanToggle(Component.literal("Auto-disable with Iris shader packs"), config.irisAutoDisable)
			.setDefaultValue(true)
			.setTooltip(Component.literal("When on, Ecology water opacity turns off while an Iris pack is active. Turn off to keep Ecology running with Iris (may conflict)."))
			.setSaveConsumer(value -> config.irisAutoDisable = value)
			.build());

		water.addEntry(entries.startBooleanToggle(Component.literal("Debug logging / chat status"), config.debugLogging)
			.setDefaultValue(false)
			.setTooltip(Component.literal("Logs distant-water status and prints a short summary to chat when config applies."))
			.setSaveConsumer(value -> config.debugLogging = value)
			.build());

		water.addEntry(entries.startBooleanToggle(Component.literal("DEBUG: highlight marked water tops (yellow→blue)"), config.debugHighlightMarkedTops)
			.setDefaultValue(false)
			.setTooltip(Component.literal("Yellow = near / not opaque yet. Blue = full opacity (End distance). Underwater tops stay faint."))
			.setSaveConsumer(value -> config.debugHighlightMarkedTops = value)
			.build());

		water.addEntry(entries.startBooleanToggle(Component.literal("DEBUG: highlight fresnel / view angle (green→red)"), config.debugHighlightFresnel)
			.setDefaultValue(false)
			.setTooltip(Component.literal("Green = looking straight down. Yellow = mid. Red = horizon. Uses the fresnel power curve."))
			.setSaveConsumer(value -> config.debugHighlightFresnel = value)
			.build());

		water.addEntry(entries.startBooleanToggle(Component.literal("DEBUG: highlight all translucent terrain (cyan)"), config.debugHighlightAllTranslucent)
			.setDefaultValue(false)
			.setTooltip(Component.literal("Paints ice/glass/water cyan. If NOTHING changes, Ecology core/terrain.fsh is not running (e.g. Sodium)."))
			.setSaveConsumer(value -> config.debugHighlightAllTranslucent = value)
			.build());

		return builder.build();
	}
}
