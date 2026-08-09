package com.midas.ecology.worldgen.feature;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.util.valueproviders.IntProvider;
import net.minecraft.util.valueproviders.IntProviders;
import net.minecraft.world.level.levelgen.feature.configurations.FeatureConfiguration;

/**
 * Low soft-coral / sea-fan clump on the seafloor (fans only, no hard coral heads).
 */
public record SoftCoralClumpConfiguration(IntProvider fanCount, IntProvider radius) implements FeatureConfiguration {
	public static final Codec<SoftCoralClumpConfiguration> CODEC = RecordCodecBuilder.create(instance -> instance.group(
		IntProviders.codec(1, 24).fieldOf("fan_count").forGetter(SoftCoralClumpConfiguration::fanCount),
		IntProviders.codec(1, 6).fieldOf("radius").forGetter(SoftCoralClumpConfiguration::radius)
	).apply(instance, SoftCoralClumpConfiguration::new));
}
