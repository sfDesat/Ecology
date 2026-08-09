package com.midas.ecology.worldgen.feature;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.util.valueproviders.IntProvider;
import net.minecraft.util.valueproviders.IntProviders;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.feature.configurations.FeatureConfiguration;

/**
 * Config for a small underwater rock mound that rises above the seafloor.
 */
public record UnderwaterRockConfiguration(BlockState state, IntProvider radius, IntProvider height) implements FeatureConfiguration {
	public static final Codec<UnderwaterRockConfiguration> CODEC = RecordCodecBuilder.create(instance -> instance.group(
		BlockState.CODEC.fieldOf("state").forGetter(UnderwaterRockConfiguration::state),
		IntProviders.codec(0, 8).fieldOf("radius").forGetter(UnderwaterRockConfiguration::radius),
		IntProviders.codec(1, 24).fieldOf("height").forGetter(UnderwaterRockConfiguration::height)
	).apply(instance, UnderwaterRockConfiguration::new));
}
