package com.midas.ecology.client.render;

import com.midas.ecology.client.config.EcologyClientConfig;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.fog.FogData;
import net.minecraft.core.BlockPos;
import net.minecraft.util.Mth;
import net.minecraft.world.phys.Vec3;

/**
 * Replaces vanilla short underwater fog distances while swimming.
 * Mutates {@link FogData} in {@code FogRenderer.setupFog} so Sodium copies the stretched values.
 */
public final class SwimFogDistances {
	private static boolean wasUnderwater;
	private static float displayedSwimFogEnd;
	private static float fadeFromEnd;
	private static float fadeToEnd;
	private static float fadeElapsed;
	private static long lastSwimFogNanos;

	private SwimFogDistances() {
	}

	public static boolean shouldStretch() {
		return EcologyClientConfig.get().swimFogDistanceEnabled && SwimEffects.shouldApply();
	}

	public static void apply(FogData fog) {
		if (!shouldStretch()) {
			wasUnderwater = false;
			return;
		}
		float targetEnd = Math.max(currentSwimFogEnd(), 1.0F);
		float fogEnd = fadedSwimFogEnd(targetEnd);
		float fogStart = fogEnd * 0.10F;
		if (fogStart > fogEnd - 1.0F) {
			fogStart = Math.max(0.0F, fogEnd - 1.0F);
		}
		fog.environmentalStart = fogStart;
		fog.environmentalEnd = fogEnd;
		fog.skyEnd = fogEnd;
		fog.cloudEnd = fogEnd;
	}

	/**
	 * Ease between biome swim-fog distances while already underwater.
	 * Entering or leaving water snaps — no fade on those transitions.
	 */
	private static float fadedSwimFogEnd(float targetEnd) {
		long now = System.nanoTime();
		float duration = EcologyClientConfig.get().clampedSwimFogFadeSeconds();
		if (!wasUnderwater) {
			displayedSwimFogEnd = targetEnd;
			fadeFromEnd = targetEnd;
			fadeToEnd = targetEnd;
			fadeElapsed = duration;
			lastSwimFogNanos = now;
			wasUnderwater = true;
			return displayedSwimFogEnd;
		}
		float dt = (now - lastSwimFogNanos) / 1_000_000_000.0F;
		lastSwimFogNanos = now;
		dt = Mth.clamp(dt, 0.0F, 0.05F);
		if (duration <= 0.001F) {
			displayedSwimFogEnd = targetEnd;
			fadeFromEnd = targetEnd;
			fadeToEnd = targetEnd;
			fadeElapsed = 0.0F;
			return displayedSwimFogEnd;
		}
		if (Math.abs(targetEnd - fadeToEnd) > 0.5F) {
			fadeFromEnd = displayedSwimFogEnd;
			fadeToEnd = targetEnd;
			fadeElapsed = 0.0F;
		}
		fadeElapsed = Math.min(duration, fadeElapsed + dt);
		float t = Mth.clamp(fadeElapsed / duration, 0.0F, 1.0F);
		t = t * t * (3.0F - 2.0F * t);
		displayedSwimFogEnd = Mth.lerp(t, fadeFromEnd, fadeToEnd);
		return displayedSwimFogEnd;
	}

	private static float currentSwimFogEnd() {
		Minecraft client = Minecraft.getInstance();
		if (client == null || client.level == null || client.gameRenderer == null) {
			return EcologyClientConfig.get().clampedSwimFogFallback();
		}
		Vec3 camera = client.gameRenderer.mainCamera().position();
		return EcologyClientConfig.get().clampedSwimFogEnd(
			client.level.getBiome(BlockPos.containing(camera))
		);
	}
}
