package com.midas.ecology.client.config;

import com.midas.ecology.EcologyMod;
import com.midas.ecology.client.render.DistantWaterShaderSupport;
import me.shedaniel.clothconfig2.api.ConfigBuilder;
import me.shedaniel.clothconfig2.api.ConfigCategory;
import me.shedaniel.clothconfig2.api.ConfigEntryBuilder;
import me.shedaniel.clothconfig2.api.Requirement;
import me.shedaniel.clothconfig2.gui.entries.BooleanListEntry;
import me.shedaniel.clothconfig2.impl.builders.SubCategoryBuilder;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

import java.util.function.IntConsumer;

public final class EcologyConfigScreen {
	private EcologyConfigScreen() {
	}

	public static Screen create(Screen parent) {
		EcologyClientConfig config = EcologyClientConfig.get();
		ConfigBuilder builder = ConfigBuilder.create()
			.setParentScreen(parent)
			.setTitle(t("title"))
			.setSavingRunnable(() -> {
				EcologyClientConfig.save();
				DistantWaterShaderSupport.applyConfigAndReload();
				EcologyMod.LOGGER.info("Ecology config saved via Cloth: {}", DistantWaterShaderSupport.statusSummary());
			});

		ConfigEntryBuilder entries = builder.entryBuilder();
		addGeneral(builder, entries, config);
		addAboveWater(builder, entries, config);
		addUnderwater(builder, entries, config);
		addDebug(builder, entries, config);
		return builder.build();
	}

	private static void addGeneral(ConfigBuilder builder, ConfigEntryBuilder entries, EcologyClientConfig config) {
		ConfigCategory general = builder.getOrCreateCategory(t("category.general"));
		general.addEntry(entries.startEnumSelector(t("mode"), DistantWaterMode.class, config.mode)
			.setDefaultValue(DistantWaterMode.FOG_REMAP)
			.setEnumNameProvider(mode -> ((DistantWaterMode) mode).displayName())
			.setTooltip(t("mode.tooltip"))
			.setSaveConsumer(value -> config.mode = value)
			.build());
		general.addEntry(entries.startBooleanToggle(t("iris"), config.pauseWithIris)
			.setDefaultValue(true)
			.setTooltip(t("iris.tooltip"))
			.setSaveConsumer(value -> config.pauseWithIris = value)
			.build());
		general.addEntry(entries.startBooleanToggle(t("warn_fabulous"), config.warnIfFabulousOff)
			.setDefaultValue(true)
			.setTooltip(t("warn_fabulous.tooltip"))
			.setSaveConsumer(value -> config.warnIfFabulousOff = value)
			.build());
	}

	private static void addAboveWater(ConfigBuilder builder, ConfigEntryBuilder entries, EcologyClientConfig config) {
		ConfigCategory above = builder.getOrCreateCategory(t("category.above_water"));
		above.addEntry(fogTintGroup(entries, config.lookingAtWater).build());
		above.addEntry(opaqueGroup(entries, config.opaqueWater).build());
	}

	private static SubCategoryBuilder fogTintGroup(ConfigEntryBuilder entries, EcologyClientConfig.LookingAtWater looking) {
		SubCategoryBuilder fog = sub(entries, "looking");
		fog.add(float01(entries, "looking.horizon", looking.horizonFog, 1.0F, value -> looking.horizonFog = value).build());
		fog.add(float01(entries, "looking.fill", looking.fill, 1.0F, value -> looking.fill = value).build());
		fog.add(float01(entries, "looking.darkness", looking.fogDarkness, 0.55F, value -> looking.fogDarkness = value).build());

		BooleanListEntry usePercent = entries.startBooleanToggle(t("looking.use_percent"), looking.usePercent)
			.setDefaultValue(true)
			.setTooltip(t("looking.use_percent.tooltip"))
			.setSaveConsumer(value -> looking.usePercent = value)
			.build();
		fog.add(usePercent);
		fog.add(intField(entries, "looking.start", looking.startPercent, 10, 0, 99, value -> looking.startPercent = value)
			.setTooltip(t("looking.start_percent.tooltip"))
			.setRequirement(Requirement.isTrue(usePercent))
			.build());
		fog.add(intField(entries, "looking.end", looking.endPercent, 70, 1, 100, value -> looking.endPercent = value)
			.setTooltip(t("looking.end_percent.tooltip"))
			.setRequirement(Requirement.isTrue(usePercent))
			.build());
		fog.add(intField(entries, "looking.start", Math.round(looking.startBlocks), 16, 0, 256, value -> looking.startBlocks = value)
			.setTooltip(t("looking.start_blocks.tooltip"))
			.setRequirement(Requirement.isFalse(usePercent))
			.build());
		fog.add(intField(entries, "looking.end", Math.round(looking.endBlocks), 128, 1, 256, value -> looking.endBlocks = value)
			.setTooltip(t("looking.end_blocks.tooltip"))
			.setRequirement(Requirement.isFalse(usePercent))
			.build());
		return fog;
	}

	private static SubCategoryBuilder opaqueGroup(ConfigEntryBuilder entries, EcologyClientConfig.OpaqueWater opaque) {
		SubCategoryBuilder group = sub(entries, "opaque");

		BooleanListEntry distanceOn = entries.startBooleanToggle(t("enabled"), opaque.distance)
			.setDefaultValue(true)
			.setTooltip(t("opaque.distance.tooltip"))
			.setSaveConsumer(value -> opaque.distance = value)
			.build();
		Requirement distanceReq = Requirement.isTrue(distanceOn);
		SubCategoryBuilder distance = sub(entries, "opaque.distance");
		distance.add(distanceOn);
		distance.add(float01(entries, "opaque.strength", opaque.strength, 1.0F, value -> opaque.strength = value)
			.setRequirement(distanceReq)
			.build());
		distance.add(float01(entries, "opaque.start", opaque.start, 0.0F, value -> opaque.start = value)
			.setRequirement(distanceReq)
			.build());
		distance.add(float01(entries, "opaque.end", opaque.end, 0.5F, value -> opaque.end = value)
			.setRequirement(distanceReq)
			.build());
		group.add(distance.build());

		BooleanListEntry angleOn = entries.startBooleanToggle(t("enabled"), opaque.angle)
			.setDefaultValue(true)
			.setTooltip(t("opaque.angle.tooltip"))
			.setSaveConsumer(value -> opaque.angle = value)
			.build();
		Requirement angleReq = Requirement.isTrue(angleOn);
		SubCategoryBuilder angle = sub(entries, "opaque.angle");
		angle.add(angleOn);
		angle.add(float01(entries, "opaque.strength", opaque.angleStrength, 1.0F, value -> opaque.angleStrength = value)
			.setRequirement(angleReq)
			.build());
		angle.add(entries.startFloatField(t("opaque.curve"), opaque.angleCurve)
			.setDefaultValue(0.75F)
			.setMin(0.25F)
			.setMax(8.0F)
			.setTooltip(t("opaque.curve.tooltip"))
			.setSaveConsumer(value -> opaque.angleCurve = value)
			.setRequirement(angleReq)
			.build());
		group.add(angle.build());
		return group;
	}

	private static void addUnderwater(ConfigBuilder builder, ConfigEntryBuilder entries, EcologyClientConfig config) {
		EcologyClientConfig.Swimming swimming = config.swimming;
		ConfigCategory underwater = builder.getOrCreateCategory(t("category.underwater"));

		BooleanListEntry customFog = entries.startBooleanToggle(t("swim.custom_fog"), swimming.customDistance)
			.setDefaultValue(true)
			.setTooltip(t("swim.custom_fog.tooltip"))
			.setSaveConsumer(value -> swimming.customDistance = value)
			.build();
		underwater.addEntry(customFog);
		underwater.addEntry(entries.startFloatField(t("swim.fade"), swimming.fadeSeconds)
			.setDefaultValue(1.0F)
			.setMin(0.0F)
			.setMax(5.0F)
			.setTooltip(t("swim.fade.tooltip"))
			.setSaveConsumer(value -> swimming.fadeSeconds = value)
			.setRequirement(Requirement.isTrue(customFog))
			.build());

		SubCategoryBuilder light = sub(entries, "swim.light");
		light.add(intField(entries, "swim.bright_until", Math.round(swimming.brightUntil), 10, 0, 256, value -> swimming.brightUntil = value).build());
		light.add(intField(entries, "swim.dark_at", Math.round(swimming.darkAt), 64, 1, 256, value -> swimming.darkAt = value).build());
		underwater.addEntry(light.build());

		Requirement fogOn = Requirement.isTrue(customFog);
		SubCategoryBuilder distance = sub(entries, "swim.distance");
		addSwimFog(distance, entries, "ecology.config.swim.cold", "swim.cold.tooltip", swimming.cold, 40, value -> swimming.cold = value, fogOn);
		addSwimFog(distance, entries, "ecology.config.swim.ice_openings", "swim.ice_openings.tooltip", swimming.iceOpenings, 48, value -> swimming.iceOpenings = value, fogOn);
		addSwimFog(distance, entries, "biome.ecology.kelp_forest", "swim.kelp_forest.tooltip", swimming.kelpForest, 36, value -> swimming.kelpForest = value, fogOn);
		addSwimFog(distance, entries, "ecology.config.swim.temperate", "swim.temperate.tooltip", swimming.temperate, 44, value -> swimming.temperate = value, fogOn);
		addSwimFog(distance, entries, "ecology.config.swim.lukewarm", "swim.lukewarm.tooltip", swimming.lukewarm, 50, value -> swimming.lukewarm = value, fogOn);
		addSwimFog(distance, entries, "ecology.config.swim.warm", "swim.warm.tooltip", swimming.warm, 56, value -> swimming.warm = value, fogOn);
		addSwimFog(distance, entries, "biome.ecology.lagoon", "swim.lagoon.tooltip", swimming.lagoon, 60, value -> swimming.lagoon = value, fogOn);
		addSwimFog(distance, entries, "biome.ecology.open_ocean", "swim.open_ocean.tooltip", swimming.openOcean, 60, value -> swimming.openOcean = value, fogOn);
		addSwimFog(distance, entries, "biome.ecology.deep_basin", "swim.deep_basin.tooltip", swimming.deepBasin, 52, value -> swimming.deepBasin = value, fogOn);
		addSwimFog(distance, entries, "ecology.config.swim.other", "swim.other.tooltip", swimming.otherWater, 34, value -> swimming.otherWater = value, fogOn);
		underwater.addEntry(distance.build());
	}

	private static void addDebug(ConfigBuilder builder, ConfigEntryBuilder entries, EcologyClientConfig config) {
		ConfigCategory debug = builder.getOrCreateCategory(t("category.debug"));
		debug.addEntry(entries.startBooleanToggle(t("debug.logging"), config.debug.logging)
			.setDefaultValue(false)
			.setTooltip(t("debug.logging.tooltip"))
			.setSaveConsumer(value -> config.debug.logging = value)
			.build());
		debug.addEntry(entries.startBooleanToggle(t("debug.translucent"), config.debug.highlightTranslucent)
			.setDefaultValue(false)
			.setTooltip(t("debug.translucent.tooltip"))
			.setSaveConsumer(value -> config.debug.highlightTranslucent = value)
			.build());
		debug.addEntry(entries.startBooleanToggle(t("debug.fog_fill"), config.debug.highlightSeeThrough)
			.setDefaultValue(false)
			.setTooltip(t("debug.fog_fill.tooltip"))
			.setSaveConsumer(value -> config.debug.highlightSeeThrough = value)
			.build());
		debug.addEntry(entries.startBooleanToggle(t("debug.marked"), config.debug.highlightMarkedWater)
			.setDefaultValue(false)
			.setTooltip(t("debug.marked.tooltip"))
			.setSaveConsumer(value -> config.debug.highlightMarkedWater = value)
			.build());
		debug.addEntry(entries.startBooleanToggle(t("debug.angle"), config.debug.highlightAngle)
			.setDefaultValue(false)
			.setTooltip(t("debug.angle.tooltip"))
			.setSaveConsumer(value -> config.debug.highlightAngle = value)
			.build());
	}

	private static void addSwimFog(
		SubCategoryBuilder group,
		ConfigEntryBuilder entries,
		String nameKey,
		String tooltipKey,
		float value,
		int defaultValue,
		IntConsumer save,
		Requirement requirement
	) {
		group.add(entries.startIntField(Component.translatable(nameKey), Math.round(value))
			.setDefaultValue(defaultValue)
			.setMin(8)
			.setMax(256)
			.setTooltip(t(tooltipKey))
			.setSaveConsumer(save::accept)
			.setRequirement(requirement)
			.build());
	}

	private static SubCategoryBuilder sub(ConfigEntryBuilder entries, String key) {
		return entries.startSubCategory(t(key))
			.setExpanded(true)
			.setTooltip(t(key + ".tooltip"));
	}

	private static me.shedaniel.clothconfig2.impl.builders.FloatFieldBuilder float01(
		ConfigEntryBuilder entries,
		String key,
		float value,
		float defaultValue,
		java.util.function.Consumer<Float> save
	) {
		return entries.startFloatField(t(key), value)
			.setDefaultValue(defaultValue)
			.setMin(0.0F)
			.setMax(1.0F)
			.setTooltip(t(key + ".tooltip"))
			.setSaveConsumer(save);
	}

	private static me.shedaniel.clothconfig2.impl.builders.IntFieldBuilder intField(
		ConfigEntryBuilder entries,
		String key,
		int value,
		int defaultValue,
		int min,
		int max,
		IntConsumer save
	) {
		return entries.startIntField(t(key), value)
			.setDefaultValue(defaultValue)
			.setMin(min)
			.setMax(max)
			.setTooltip(t(key + ".tooltip"))
			.setSaveConsumer(save::accept);
	}

	private static Component t(String key) {
		return Component.translatable("ecology.config." + key);
	}
}
