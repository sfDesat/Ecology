package com.midas.ecology.client.render;

import com.midas.ecology.client.config.EcologyClientConfig;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.state.LightmapRenderState;
import net.minecraft.core.BlockPos;
import net.minecraft.tags.FluidTags;
import net.minecraft.util.Mth;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import org.joml.Vector3f;
import org.joml.Vector3fc;

/**
 * Client-only underwater brightness. Does not touch the light engine, mob spawning, or distant water.
 */
public final class UnderwaterLighting {
	private static final int MAX_SCAN = 128;
	private static final BlockPos.MutableBlockPos SCAN_POS = new BlockPos.MutableBlockPos();
	private static final Vector3f BOOSTED_AMBIENT = new Vector3f();

	private static long cachedGameTime = Long.MIN_VALUE;
	private static int cachedX;
	private static int cachedY;
	private static int cachedZ;
	private static int cachedDepth;

	private UnderwaterLighting() {
	}

	public static boolean shouldApply() {
		return SwimEffects.shouldApply();
	}

	/** Water blocks from the camera up to the first non-water block, capped at {@value MAX_SCAN}. */
	public static int waterOverhead(Minecraft client) {
		if (client == null || client.level == null || client.gameRenderer == null) {
			return 0;
		}
		Level level = client.level;
		Vec3 camera = client.gameRenderer.mainCamera().position();
		int x = Mth.floor(camera.x);
		int y = Mth.floor(camera.y);
		int z = Mth.floor(camera.z);
		long gameTime = level.getGameTime();
		if (gameTime == cachedGameTime && x == cachedX && y == cachedY && z == cachedZ) {
			return cachedDepth;
		}
		int maxY = Math.min(level.getMaxY(), y + MAX_SCAN);
		SCAN_POS.set(x, y, z);
		int depth = 0;
		for (int yy = y; yy <= maxY; yy++) {
			SCAN_POS.setY(yy);
			if (!level.getFluidState(SCAN_POS).is(FluidTags.WATER)) {
				break;
			}
			depth++;
		}
		cachedGameTime = gameTime;
		cachedX = x;
		cachedY = y;
		cachedZ = z;
		cachedDepth = depth;
		return depth;
	}

	/**
	 * Mix extra sky-colored ambient into the lightmap so sky-light 0 is not fully black
	 * until {@link EcologyClientConfig.Swimming#clampedDarkAt()} blocks of water overhead.
	 */
	public static void boostLightmap(LightmapRenderState state, Minecraft client) {
		if (!shouldApply() || state == null) {
			return;
		}
		EcologyClientConfig config = EcologyClientConfig.get();
		float start = config.swimming.clampedBrightUntil();
		float end = config.swimming.clampedDarkAt();
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
		BOOSTED_AMBIENT.set(
			ambient.x() + (sky.x() - ambient.x()) * mix,
			ambient.y() + (sky.y() - ambient.y()) * mix,
			ambient.z() + (sky.z() - ambient.z()) * mix
		);
		state.ambientColor = BOOSTED_AMBIENT;
		state.skyFactor = Math.max(state.skyFactor, keep * 0.90F);
		state.brightness = Math.max(state.brightness, keep * 0.35F);
	}

	private static float smoothstep(float edge0, float edge1, float x) {
		if (edge1 <= edge0 + 1.0e-4F) {
			return x >= edge1 ? 1.0F : 0.0F;
		}
		float t = Mth.clamp((x - edge0) / (edge1 - edge0), 0.0F, 1.0F);
		return t * t * (3.0F - 2.0F * t);
	}
}
