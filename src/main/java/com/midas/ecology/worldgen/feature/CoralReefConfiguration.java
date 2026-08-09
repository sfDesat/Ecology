package com.midas.ecology.worldgen.feature;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.util.valueproviders.IntProvider;
import net.minecraft.util.valueproviders.IntProviders;
import net.minecraft.world.level.levelgen.feature.configurations.FeatureConfiguration;

/**
 * Dense continuous hard-coral reef mound — the warm-ocean showpiece pocket.
 */
public record CoralReefConfiguration(IntProvider coralCount, IntProvider radius) implements FeatureConfiguration {
	public static final Codec<CoralReefConfiguration> CODEC = RecordCodecBuilder.create(instance -> instance.group(
		IntProviders.codec(1, 32).fieldOf("coral_count").forGetter(CoralReefConfiguration::coralCount),
		IntProviders.codec(1, 16).fieldOf("radius").forGetter(CoralReefConfiguration::radius)
	).apply(instance, CoralReefConfiguration::new));
}
