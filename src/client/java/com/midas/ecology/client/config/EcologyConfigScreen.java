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

		ConfigCategory general = builder.getOrCreateCategory(Component.literal("General"));
		general.addEntry(entries.startEnumSelector(Component.literal("Distant water system"), DistantWaterMode.class, config.distantWaterMode)
			.setDefaultValue(DistantWaterMode.FOG_REMAP)
			.setEnumNameProvider(mode -> Component.literal(((DistantWaterMode) mode).label()))
			.setTooltip(Component.literal(
				"Off = vanilla (all Ecology distant-water effects + their debug off). Opaque water = raise water alpha (no Fabulous needed). Fog tint = unfog air fog behind water, then fixed underwater sight fog (needs Fabulous / Improved Transparency)."
			))
			.setSaveConsumer(value -> config.distantWaterMode = value)
			.build());
		general.addEntry(entries.startBooleanToggle(Component.literal("Auto-disable with Iris shader packs"), config.irisAutoDisable)
			.setDefaultValue(true)
			.setTooltip(Component.literal("When on, Ecology distant-water effects turn off while an Iris pack is active."))
			.setSaveConsumer(value -> config.irisAutoDisable = value)
			.build());
		general.addEntry(entries.startBooleanToggle(Component.literal("Debug logging / chat status"), config.debugLogging)
			.setDefaultValue(false)
			.setTooltip(Component.literal("Logs distant-water status and prints a short summary to chat when config applies."))
			.setSaveConsumer(value -> config.debugLogging = value)
			.build());
		general.addEntry(entries.startBooleanToggle(Component.literal("Highlight all translucent (cyan)"), config.debugHighlightAllTranslucent)
			.setDefaultValue(false)
			.setTooltip(Component.literal("Paints ice/glass/water cyan. If NOTHING changes, Ecology core/terrain.fsh is not running (e.g. Sodium)."))
			.setSaveConsumer(value -> config.debugHighlightAllTranslucent = value)
			.build());

		ConfigCategory fog = builder.getOrCreateCategory(Component.literal("Fog tint"));
		fog.addEntry(entries.startTextDescription(Component.literal(
			"Requires Fabulous / Improved Transparency. Strips air fog behind water, then fades the seafloor into water fog over a sight range. End is always at least start + 1 block."
		)).build());
		fog.addEntry(entries.startIntField(Component.literal("Underwater sight start (blocks)"), Math.round(config.underwaterSightStart))
			.setDefaultValue(16)
			.setMin(0)
			.setMax(256)
			.setTooltip(Component.literal("Distance where custom underwater fog begins (behind water, from camera)."))
			.setSaveConsumer(value -> config.underwaterSightStart = value)
			.build());
		fog.addEntry(entries.startBooleanToggle(Component.literal("Sight end uses % of render distance"), config.underwaterSightEndUseRenderDistancePercent)
			.setDefaultValue(false)
			.setTooltip(Component.literal(
				"Off = sight end is a fixed block distance. On = sight end is a percent of fog render distance (scales with your render distance)."
			))
			.setSaveConsumer(value -> config.underwaterSightEndUseRenderDistancePercent = value)
			.build());
		fog.addEntry(entries.startIntField(Component.literal("Underwater sight end (blocks)"), Math.round(config.underwaterSightEnd))
			.setDefaultValue(100)
			.setMin(1)
			.setMax(256)
			.setTooltip(Component.literal("Used when “% of render distance” is off. Effective end is always at least start + 1."))
			.setSaveConsumer(value -> config.underwaterSightEnd = value)
			.build());
		fog.addEntry(entries.startIntField(Component.literal("Underwater sight end (% of render distance)"), config.underwaterSightEndPercent)
			.setDefaultValue(50)
			.setMin(1)
			.setMax(100)
			.setTooltip(Component.literal("Used when “% of render distance” is on. Effective end in blocks is max(start + 1, renderDistance × percent / 100)."))
			.setSaveConsumer(value -> config.underwaterSightEndPercent = value)
			.build());
		fog.addEntry(entries.startFloatField(Component.literal("Underwater fog strength (0-1)"), config.fogRemapBiasStrength)
			.setDefaultValue(1.0F)
			.setMin(0.0F)
			.setMax(1.0F)
			.setTooltip(Component.literal("How hard underwater sight fog and empty-sky fill apply."))
			.setSaveConsumer(value -> config.fogRemapBiasStrength = value)
			.build());
		fog.addEntry(entries.startFloatField(Component.literal("Water fog darkness (0-1)"), config.fogTintDarkness)
			.setDefaultValue(0.55F)
			.setMin(0.0F)
			.setMax(1.0F)
			.setTooltip(Component.literal("Darkens biome water fog color. 0 = as-is, 1 = black. Used for sight fog and empty fill."))
			.setSaveConsumer(value -> config.fogTintDarkness = value)
			.build());
		fog.addEntry(entries.startBooleanToggle(Component.literal("Warn if Improved Transparency is off"), config.warnMissingImprovedTransparency)
			.setDefaultValue(true)
			.setTooltip(Component.literal(
				"When Fog tint is selected but graphics Improved Transparency (Fabulous) is off, send a chat reminder. Fog tint needs that setting."
			))
			.setSaveConsumer(value -> config.warnMissingImprovedTransparency = value)
			.build());
		fog.addEntry(entries.startTextDescription(Component.literal("—— Debug ——")).build());
		fog.addEntry(entries.startBooleanToggle(Component.literal("Highlight underwater fog (pink)"), config.debugHighlightFogRemap)
			.setDefaultValue(false)
			.setTooltip(Component.literal(
				"Pink tracks underwater sight fog / empty-behind fill. Requires Fabulous. Save config to reload."
			))
			.setSaveConsumer(value -> config.debugHighlightFogRemap = value)
			.build());

		ConfigCategory opaque = builder.getOrCreateCategory(Component.literal("Opaque water"));
		opaque.addEntry(entries.startTextDescription(Component.literal(
			"Makes distant water less see-through. Does not need Fabulous. Ignored when Fog tint mode is selected."
		)).build());
		opaque.addEntry(entries.startBooleanToggle(Component.literal("Distance opacity"), config.distanceOpacityEnabled)
			.setDefaultValue(true)
			.setTooltip(Component.literal("Distance-based opacity. Turn off to test fresnel alone."))
			.setSaveConsumer(value -> config.distanceOpacityEnabled = value)
			.build());
		opaque.addEntry(entries.startFloatField(Component.literal("Distance strength (0-1)"), config.distantWaterOpacityStrength)
			.setDefaultValue(1.0F)
			.setMin(0.0F)
			.setMax(1.0F)
			.setTooltip(Component.literal("At End distance: 1.0 = fully opaque from distance alone."))
			.setSaveConsumer(value -> config.distantWaterOpacityStrength = value)
			.build());
		opaque.addEntry(entries.startFloatField(Component.literal("Start distance (0-1)"), config.distantWaterOpacityStart)
			.setDefaultValue(0.0F)
			.setMin(0.0F)
			.setMax(1.0F)
			.setTooltip(Component.literal("Where distance opacity begins, as a fraction of render-distance fog end."))
			.setSaveConsumer(value -> config.distantWaterOpacityStart = value)
			.build());
		opaque.addEntry(entries.startFloatField(Component.literal("End distance (0-1)"), config.distantWaterOpacityEnd)
			.setDefaultValue(0.5F)
			.setMin(0.0F)
			.setMax(1.0F)
			.setTooltip(Component.literal("Where distance opacity reaches full strength. Must be >= Start."))
			.setSaveConsumer(value -> config.distantWaterOpacityEnd = value)
			.build());
		opaque.addEntry(entries.startBooleanToggle(Component.literal("Fresnel (angle opacity)"), config.fresnelEnabled)
			.setDefaultValue(true)
			.setTooltip(Component.literal("Adds glancing-angle opacity. Combined with distance, capped at 1."))
			.setSaveConsumer(value -> config.fresnelEnabled = value)
			.build());
		opaque.addEntry(entries.startFloatField(Component.literal("Fresnel strength (0-1)"), config.fresnelStrength)
			.setDefaultValue(1.0F)
			.setMin(0.0F)
			.setMax(1.0F)
			.setTooltip(Component.literal("How much glancing-angle opacity to ADD. Total with distance is clamped to 1."))
			.setSaveConsumer(value -> config.fresnelStrength = value)
			.build());
		opaque.addEntry(entries.startFloatField(Component.literal("Fresnel power / angle curve (0.25-8)"), config.fresnelPower)
			.setDefaultValue(0.75F)
			.setMin(0.25F)
			.setMax(8.0F)
			.setTooltip(Component.literal("pow(grazing, power). Lower = more angles get opaque. Higher = only near-horizon."))
			.setSaveConsumer(value -> config.fresnelPower = value)
			.build());
		opaque.addEntry(entries.startTextDescription(Component.literal("—— Debug ——")).build());
		opaque.addEntry(entries.startBooleanToggle(Component.literal("Highlight marked water (yellow→blue)"), config.debugHighlightMarkedTops)
			.setDefaultValue(false)
			.setTooltip(Component.literal("Yellow = near, blue = far. Underwater stays faint."))
			.setSaveConsumer(value -> config.debugHighlightMarkedTops = value)
			.build());
		opaque.addEntry(entries.startBooleanToggle(Component.literal("Highlight fresnel / view angle (green→red)"), config.debugHighlightFresnel)
			.setDefaultValue(false)
			.setTooltip(Component.literal("Green = looking down, red = horizon."))
			.setSaveConsumer(value -> config.debugHighlightFresnel = value)
			.build());

		return builder.build();
	}
}
