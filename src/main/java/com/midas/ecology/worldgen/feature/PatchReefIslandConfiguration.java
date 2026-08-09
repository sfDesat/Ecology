package com.midas.ecology.worldgen.feature;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.util.valueproviders.IntProvider;
import net.minecraft.util.valueproviders.IntProviders;
import net.minecraft.world.level.levelgen.feature.configurations.FeatureConfiguration;

/**
 * Dense hard-coral cluster with a sand halo — one patch-reef "island".
 */
public record PatchReefIslandConfiguration(IntProvider coralCount, IntProvider radius) implements FeatureConfiguration {
	public static final Codec<PatchReefIslandConfiguration> CODEC = RecordCodecBuilder.create(instance -> instance.group(
		IntProviders.codec(1, 16).fieldOf("coral_count").forGetter(PatchReefIslandConfiguration::coralCount),
		IntProviders.codec(1, 12).fieldOf("radius").forGetter(PatchReefIslandConfiguration::radius)
	).apply(instance, PatchReefIslandConfiguration::new));
}
