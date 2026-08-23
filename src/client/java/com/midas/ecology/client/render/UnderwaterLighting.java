package com.midas.ecology.client.render;

import com.midas.ecology.client.compat.IrisCompat;
import com.midas.ecology.client.config.EcologyClientConfig;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.state.LightmapRenderState;
import net.minecraft.core.BlockPos;
import net.minecraft.tags.FluidTags;
import net.minecraft.util.Mth;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.material.FogType;
import net.minecraft.world.phys.Vec3;
import org.joml.Vector3f;
import org.joml.Vector3fc;

/**
 * Client-only underwater brightness and fog-distance helpers.
 * Does not touch the light engine or mob spawning.
 */
public final class UnderwaterLighting {
	private static final int MAX_SCAN = 128;
	private static boolean wasUnderwater;
	private static float displayedSwimFogEnd;
	private static float fadeFromEnd;
	private static float fadeToEnd;
	private static float fadeElapsed;
	private static long lastSwimFogNanos;

	private UnderwaterLighting() {
	}

	public static boolean shouldApply() {
		EcologyClientConfig config = EcologyClientConfig.get();
		if (config.irisAutoDisable && IrisCompat.isShaderPackInUse()) {
			return false;
		}
		Minecraft client = Minecraft.getInstance();
		return client != null
			&& client.gameRenderer != null
			&& client.gameRenderer.mainCamera().getFluidInCamera() == FogType.WATER;
	}

	/** Water blocks from the camera up to the first non-water block, capped at {@value MAX_SCAN}. */
	public static int waterOverhead(Minecraft client) {
		if (client == null || client.level == null || client.gameRenderer == null) {
			return 0;
		}
		Level level = client.level;
		Vec3 camera = client.gameRenderer.mainCamera().position();
		int x = Mth.floor(camera.x);
		int z = Mth.floor(camera.z);
		int y = Mth.floor(camera.y);
		int maxY = Math.min(level.getMaxY(), y + MAX_SCAN);
		BlockPos.MutableBlockPos pos = new BlockPos.MutableBlockPos(x, y, z);
		int depth = 0;
		for (int yy = y; yy <= maxY; yy++) {
			pos.setY(yy);
			if (!level.getFluidState(pos).is(FluidTags.WATER)) {
				break;
			}
			depth++;
		}
		return depth;
	}

	/**
	 * Mix extra sky-colored ambient into the lightmap so sky-light 0 is not fully black
	 * until {@link EcologyClientConfig#clampedUnderwaterLightEnd()} blocks of water overhead.
	 */
	public static void boostLightmap(LightmapRenderState state, Minecraft client) {
		if (!shouldApply() || state == null) {
			return;
		}
		EcologyClientConfig config = EcologyClientConfig.get();
		float start = config.clampedUnderwaterLightStart();
		float end = config.clampedUnderwaterLightEnd();
		float depth = waterOverhead(client);
		float fade = smoothstep(start, end, depth);
		float keep = 1.0F - fade;
		if (keep <= 0.001F) {
			return;
		}

		Vector3fc sky = state.skyLightColor;
		Vector3fc ambient = state.ambientColor;
		if (sky == null || ambient == null) {
			return;
		}
		float mix = keep * 0.70F;
		state.ambientColor = new Vector3f(
			ambient.x() + (sky.x() - ambient.x()) * mix,
			ambient.y() + (sky.y() - ambient.y()) * mix,
			ambient.z() + (sky.z() - ambient.z()) * mix
		);
		state.skyFactor = Math.max(state.skyFactor, keep * 0.90F);
		state.brightness = Math.max(state.brightness, keep * 0.35F);
	}

	/**
	 * Replace vanilla water fog distances while swimming.
	 * Vanilla water fog is short and also fades in over time; Ecology sets a fixed
	 * block view distance from config (does not scale with render distance).
	 */
	public static float[] stretchEnvironmentalFog(float environmentalStart, float environmentalEnd) {
		if (!shouldApply()) {
			wasUnderwater = false;
			return new float[] {environmentalStart, environmentalEnd};
		}
		float targetEnd = Math.max(currentSwimFogEnd(), 1.0F);
		float fogEnd = fadedSwimFogEnd(targetEnd);
		float fogStart = fogEnd * 0.10F;
		if (fogStart > fogEnd - 1.0F) {
			fogStart = Math.max(0.0F, fogEnd - 1.0F);
		}
		return new float[] {fogStart, fogEnd};
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
			return EcologyClientConfig.get().clampedUnderwaterFogEnd();
		}
		Vec3 camera = client.gameRenderer.mainCamera().position();
		return EcologyClientConfig.get().clampedSwimFogEnd(
			client.level.getBiome(BlockPos.containing(camera))
		);
	}

	private static float smoothstep(float edge0, float edge1, float x) {
		if (edge1 <= edge0 + 1.0e-4F) {
			return x >= edge1 ? 1.0F : 0.0F;
		}
		float t = Mth.clamp((x - edge0) / (edge1 - edge0), 0.0F, 1.0F);
		return t * t * (3.0F - 2.0F * t);
	}
}
