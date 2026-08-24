package com.midas.ecology.client.config;

import com.midas.ecology.EcologyMod;
import com.midas.ecology.client.render.DistantWaterShaderSupport;
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
				DistantWaterShaderSupport.applyConfigAndReload();
				EcologyMod.LOGGER.info("Ecology config saved via Cloth: {}", DistantWaterShaderSupport.statusSummary());
			});

		ConfigEntryBuilder entries = builder.entryBuilder();

		addGeneral(builder, entries, config);
		addLookingAtWater(builder, entries, config);
		addSwimming(builder, entries, config);
		addOpaqueWater(builder, entries, config);

		return builder.build();
	}

	private static void addGeneral(ConfigBuilder builder, ConfigEntryBuilder entries, EcologyClientConfig config) {
		ConfigCategory general = builder.getOrCreateCategory(Component.literal("General"));
		general.addEntry(entries.startTextDescription(Component.literal(
			"Pick how Ecology draws distant oceans. Fog tint is the default. Opaque water is a simpler fallback. Off is vanilla water."
		)).build());
		general.addEntry(entries.startEnumSelector(Component.literal("Distant water system"), DistantWaterMode.class, config.distantWaterMode)
			.setDefaultValue(DistantWaterMode.FOG_REMAP)
			.setEnumNameProvider(mode -> Component.literal(((DistantWaterMode) mode).label()))
			.setTooltip(Component.literal(
				"Fog tint (default): from above water, hide the pale basin outline and fade the seafloor into water fog. Needs Fabulous / Improved Transparency.\n"
					+ "Opaque water: make far water less see-through. No Fabulous needed. Ignored if Fog tint is selected.\n"
					+ "Off: vanilla water; all Ecology distant-water effects off."
			))
			.setSaveConsumer(value -> config.distantWaterMode = value)
			.build());
		general.addEntry(entries.startBooleanToggle(Component.literal("Auto-disable with Iris shader packs"), config.irisAutoDisable)
			.setDefaultValue(true)
			.setTooltip(Component.literal(
				"When on, Ecology fog tint, opaque water, and swimming fog/brightness turn off while an Iris shader pack is actually running. Iris installed with no pack still uses Ecology. Sodium is supported separately via Ecology's Sodium shader overlay."
			))
			.setSaveConsumer(value -> config.irisAutoDisable = value)
			.build());
		general.addEntry(entries.startBooleanToggle(Component.literal("Warn if Improved Transparency is off"), config.warnMissingImprovedTransparency)
			.setDefaultValue(true)
			.setTooltip(Component.literal(
				"Chat reminder when Fog tint is selected but Fabulous / Improved Transparency is off. Fog tint cannot run without it."
			))
			.setSaveConsumer(value -> config.warnMissingImprovedTransparency = value)
			.build());
		general.addEntry(entries.startTextDescription(Component.literal("—— Debug ——")).build());
		general.addEntry(entries.startBooleanToggle(Component.literal("Debug logging / chat status"), config.debugLogging)
			.setDefaultValue(false)
			.setTooltip(Component.literal("Logs distant-water status and prints a short summary to chat when config applies."))
			.setSaveConsumer(value -> config.debugLogging = value)
			.build());
		general.addEntry(entries.startBooleanToggle(Component.literal("Highlight all translucent (cyan)"), config.debugHighlightAllTranslucent)
			.setDefaultValue(false)
			.setTooltip(Component.literal(
				"Paints ice, glass, and water cyan. If nothing changes, Ecology’s terrain shader is not running (vanilla override missing, or Sodium overlay failed)."
			))
			.setSaveConsumer(value -> config.debugHighlightAllTranslucent = value)
			.build());
	}

	private static void addLookingAtWater(ConfigBuilder builder, ConfigEntryBuilder entries, EcologyClientConfig config) {
		ConfigCategory fog = builder.getOrCreateCategory(Component.literal("Looking at water"));
		fog.addEntry(entries.startTextDescription(Component.literal(
			"These settings apply while you are ABOVE water, looking at the ocean. They need Distant water system = Fog tint, and Fabulous / Improved Transparency. They do not affect swimming."
		)).build());
		fog.addEntry(entries.startFloatField(Component.literal("Horizon fog on the surface (0-1)"), config.surfaceAirFog)
			.setDefaultValue(1.0F)
			.setMin(0.0F)
			.setMax(1.0F)
			.setTooltip(Component.literal(
				"How strongly distant water picks up the same white air fog as land. 1 = water and land fade together at the horizon. 0 = water surface stays fogless (old Fog tint look)."
			))
			.setSaveConsumer(value -> config.surfaceAirFog = value)
			.build());
		fog.addEntry(entries.startTextDescription(Component.literal(
			"See-through: how far you can look into the water before the seafloor fades into biome water fog (the pale-basin fix)."
		)).build());
		fog.addEntry(entries.startBooleanToggle(Component.literal("See-through distances use % of render distance"), config.sightFogUseRenderDistancePercent)
			.setDefaultValue(true)
			.setTooltip(Component.literal(
				"On (default): start and end both scale with your render distance. Off: use fixed block distances instead."
			))
			.setSaveConsumer(value -> config.sightFogUseRenderDistancePercent = value)
			.build());
		fog.addEntry(entries.startIntField(Component.literal("See-through start (% of render distance)"), config.sightFogStartPercent)
			.setDefaultValue(10)
			.setMin(0)
			.setMax(99)
			.setTooltip(Component.literal(
				"Used when “% of render distance” is on. Distance where water-fog fill begins. Always kept below end."
			))
			.setSaveConsumer(value -> config.sightFogStartPercent = value)
			.build());
		fog.addEntry(entries.startIntField(Component.literal("See-through end (% of render distance)"), config.sightFogEndPercent)
			.setDefaultValue(70)
			.setMin(1)
			.setMax(100)
			.setTooltip(Component.literal(
				"Used when “% of render distance” is on. Distance where water-fog fill is full. Effective end is always at least start + 1 block."
			))
			.setSaveConsumer(value -> config.sightFogEndPercent = value)
			.build());
		fog.addEntry(entries.startIntField(Component.literal("See-through start (blocks)"), Math.round(config.sightFogStart))
			.setDefaultValue(16)
			.setMin(0)
			.setMax(256)
			.setTooltip(Component.literal(
				"Used when “% of render distance” is off. Distance from the camera where water-fog fill begins. Close water stays clear."
			))
			.setSaveConsumer(value -> config.sightFogStart = value)
			.build());
		fog.addEntry(entries.startIntField(Component.literal("See-through end (blocks)"), Math.round(config.sightFogEnd))
			.setDefaultValue(128)
			.setMin(1)
			.setMax(256)
			.setTooltip(Component.literal(
				"Used when “% of render distance” is off. Distance where water-fog fill is full. Always at least start + 1."
			))
			.setSaveConsumer(value -> config.sightFogEnd = value)
			.build());
		fog.addEntry(entries.startFloatField(Component.literal("Water fill strength (0-1)"), config.fogRemapBiasStrength)
			.setDefaultValue(1.0F)
			.setMin(0.0F)
			.setMax(1.0F)
			.setTooltip(Component.literal(
				"How hard the seafloor fades into water fog, and how hard empty sky behind water is filled. 0 = off, 1 = full."
			))
			.setSaveConsumer(value -> config.fogRemapBiasStrength = value)
			.build());
		fog.addEntry(entries.startFloatField(Component.literal("Water fog darkness (0-1)"), config.fogTintDarkness)
			.setDefaultValue(0.55F)
			.setMin(0.0F)
			.setMax(1.0F)
			.setTooltip(Component.literal(
				"Darkens the biome water fog color used for that fill. 0 = biome color as-is, 1 = black."
			))
			.setSaveConsumer(value -> config.fogTintDarkness = value)
			.build());
		fog.addEntry(entries.startTextDescription(Component.literal("—— Debug ——")).build());
		fog.addEntry(entries.startBooleanToggle(Component.literal("Highlight see-through fog (pink)"), config.debugHighlightFogRemap)
			.setDefaultValue(false)
			.setTooltip(Component.literal(
				"Pink where see-through water fog / empty-behind fill is applying. Needs Fabulous. Save config to reload."
			))
			.setSaveConsumer(value -> config.debugHighlightFogRemap = value)
			.build());
	}

	private static void addSwimming(ConfigBuilder builder, ConfigEntryBuilder entries, EcologyClientConfig config) {
		ConfigCategory underwater = builder.getOrCreateCategory(Component.literal("Swimming"));
		underwater.addEntry(entries.startTextDescription(Component.literal(
			"These settings apply while YOU are underwater. They do not change mob spawning. They turn off when Auto-disable with Iris is on and a pack is active."
		)).build());
		underwater.addEntry(entries.startTextDescription(Component.literal(
			"Brightness: vanilla oceans go cave-dark after about 15 blocks of water above you. Ecology keeps extra light until the depths below."
		)).build());
		underwater.addEntry(entries.startIntField(Component.literal("Stay bright until (blocks of water above you)"), Math.round(config.underwaterLightStart))
			.setDefaultValue(10)
			.setMin(0)
			.setMax(256)
			.setTooltip(Component.literal(
				"How much water can sit above you before extra brightness starts fading. Shallower than this stays closer to surface lighting."
			))
			.setSaveConsumer(value -> config.underwaterLightStart = value)
			.build());
		underwater.addEntry(entries.startIntField(Component.literal("Fully dark at (blocks of water above you)"), Math.round(config.underwaterLightEnd))
			.setDefaultValue(64)
			.setMin(1)
			.setMax(256)
			.setTooltip(Component.literal(
				"How much water above you before extra brightness is gone and vanilla darkness wins. Always at least start + 1."
			))
			.setSaveConsumer(value -> config.underwaterLightEnd = value)
			.build());
		underwater.addEntry(entries.startTextDescription(Component.literal(
			"View distance: how far you can see while swimming, by water type. Vanilla water fog is short and also fades in over time; Ecology replaces that with these block distances. They do not scale with render distance."
		)).build());
		underwater.addEntry(entries.startBooleanToggle(Component.literal("Custom swim fog distance"), config.swimFogDistanceEnabled)
			.setDefaultValue(true)
			.setTooltip(Component.literal(
				"When on, Ecology replaces vanilla short underwater fog with the region distances below (works with Sodium terrain). When off, vanilla underwater fog distance is kept; brightness settings still apply."
			))
			.setSaveConsumer(value -> config.swimFogDistanceEnabled = value)
			.build());
		underwater.addEntry(entries.startFloatField(Component.literal("Region fade (seconds)"), config.swimFogFadeSeconds)
			.setDefaultValue(1.0F)
			.setMin(0.0F)
			.setMax(5.0F)
			.setTooltip(Component.literal(
				"How long swim view distance takes to change when you move into a different water type. 0 = instant. Does not fade when you get in or out of water."
			))
			.setSaveConsumer(value -> config.swimFogFadeSeconds = value)
			.build());
		addSwimFogField(underwater, entries, "Kelp canopy", config.swimFogKelpCanopy, 36,
			"Kelp Forest. Shortest vis — canopy and particles.",
			value -> config.swimFogKelpCanopy = value);
		addSwimFogField(underwater, entries, "Cold / polar shelf", config.swimFogColdPolarShelf, 40,
			"Frozen Ocean, Sympagic Zone, Cold Ocean, Cold Eelgrass. Nutrient-rich green-grey water.",
			value -> config.swimFogColdPolarShelf = value);
		addSwimFogField(underwater, entries, "Temperate shelf", config.swimFogTemperateShelf, 44,
			"Ocean, Seagrass Meadow, Temperate Rocky Reef, Sand Wave Field.",
			value -> config.swimFogTemperateShelf = value);
		addSwimFogField(underwater, entries, "Ice openings", config.swimFogIceOpenings, 48,
			"Ice Edge and Polynya. Cold-clear openings in the pack.",
			value -> config.swimFogIceOpenings = value);
		addSwimFogField(underwater, entries, "Subtropical", config.swimFogSubtropical, 50,
			"Lukewarm Ocean, Subtropical Seagrass, Patch Reef, Soft Coral Garden.",
			value -> config.swimFogSubtropical = value);
		addSwimFogField(underwater, entries, "Deep basin", config.swimFogDeepBasin, 52,
			"Deep Basin. Fairly clean water, but dark — vis dies from light, not silt.",
			value -> config.swimFogDeepBasin = value);
		addSwimFogField(underwater, entries, "Tropical clear", config.swimFogTropicalClear, 56,
			"Warm Ocean, Coral Reef, Tropical Seagrass.",
			value -> config.swimFogTropicalClear = value);
		addSwimFogField(underwater, entries, "Lagoon", config.swimFogLagoon, 60,
			"Lagoon. Glass-clear sand shallows; often clearer than the reef.",
			value -> config.swimFogLagoon = value);
		addSwimFogField(underwater, entries, "Open ocean", config.swimFogOpenOcean, 60,
			"Open Ocean (upper pelagic). Longest swim vis. Drops to Deep basin when you go deeper.",
			value -> config.swimFogOpenOcean = value);
		addSwimFogField(underwater, entries, "Other water (rivers, lakes)", config.underwaterFogEnd, 34,
			"Rivers, lakes, and any biome not listed above.",
			value -> config.underwaterFogEnd = value);
	}

	private static void addSwimFogField(
		ConfigCategory category,
		ConfigEntryBuilder entries,
		String name,
		float value,
		int defaultValue,
		String tooltip,
		java.util.function.IntConsumer save
	) {
		category.addEntry(entries.startIntField(Component.literal(name), Math.round(value))
			.setDefaultValue(defaultValue)
			.setMin(8)
			.setMax(256)
			.setTooltip(Component.literal(tooltip))
			.setSaveConsumer(save::accept)
			.build());
	}

	private static void addOpaqueWater(ConfigBuilder builder, ConfigEntryBuilder entries, EcologyClientConfig config) {
		ConfigCategory opaque = builder.getOrCreateCategory(Component.literal("Opaque water"));
		opaque.addEntry(entries.startTextDescription(Component.literal(
			"Fallback when Distant water system is Opaque water. Makes far water less see-through. Does not need Fabulous. Ignored while Fog tint is selected."
		)).build());
		opaque.addEntry(entries.startBooleanToggle(Component.literal("Distance opacity"), config.distanceOpacityEnabled)
			.setDefaultValue(true)
			.setTooltip(Component.literal("Fade water toward opaque with distance. Turn off to test angle (fresnel) alone."))
			.setSaveConsumer(value -> config.distanceOpacityEnabled = value)
			.build());
		opaque.addEntry(entries.startFloatField(Component.literal("Distance strength (0-1)"), config.distantWaterOpacityStrength)
			.setDefaultValue(1.0F)
			.setMin(0.0F)
			.setMax(1.0F)
			.setTooltip(Component.literal("At End distance: 1.0 = fully opaque from distance alone."))
			.setSaveConsumer(value -> config.distantWaterOpacityStrength = value)
			.build());
		opaque.addEntry(entries.startFloatField(Component.literal("Start distance (0-1 of render distance)"), config.distantWaterOpacityStart)
			.setDefaultValue(0.0F)
			.setMin(0.0F)
			.setMax(1.0F)
			.setTooltip(Component.literal("Where distance opacity begins, as a fraction of render-distance fog end. 0 = starts at the camera."))
			.setSaveConsumer(value -> config.distantWaterOpacityStart = value)
			.build());
		opaque.addEntry(entries.startFloatField(Component.literal("End distance (0-1 of render distance)"), config.distantWaterOpacityEnd)
			.setDefaultValue(0.5F)
			.setMin(0.0F)
			.setMax(1.0F)
			.setTooltip(Component.literal("Where distance opacity reaches full strength. Must be >= Start."))
			.setSaveConsumer(value -> config.distantWaterOpacityEnd = value)
			.build());
		opaque.addEntry(entries.startBooleanToggle(Component.literal("Fresnel (angle opacity)"), config.fresnelEnabled)
			.setDefaultValue(true)
			.setTooltip(Component.literal("Adds extra opacity when looking across the water (glancing angle). Combined with distance, capped at 1."))
			.setSaveConsumer(value -> config.fresnelEnabled = value)
			.build());
		opaque.addEntry(entries.startFloatField(Component.literal("Fresnel strength (0-1)"), config.fresnelStrength)
			.setDefaultValue(1.0F)
			.setMin(0.0F)
			.setMax(1.0F)
			.setTooltip(Component.literal("How much glancing-angle opacity to add. Total with distance is clamped to 1."))
			.setSaveConsumer(value -> config.fresnelStrength = value)
			.build());
		opaque.addEntry(entries.startFloatField(Component.literal("Fresnel power / angle curve (0.25-8)"), config.fresnelPower)
			.setDefaultValue(0.75F)
			.setMin(0.25F)
			.setMax(8.0F)
			.setTooltip(Component.literal("Lower = more view angles get opaque. Higher = only near the horizon."))
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
	}
}
