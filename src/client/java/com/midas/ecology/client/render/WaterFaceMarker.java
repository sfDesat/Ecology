package com.midas.ecology.client.render;

import com.midas.ecology.client.config.DistantWaterMode;
import net.minecraft.tags.FluidTags;
import net.minecraft.world.level.material.FluidState;

/**
 * Vertex-color alpha used to mark water faces for distant-water terrain shaders.
 * BLOCK format has no normals; 253/255 is stripped back to full alpha in the vertex shader.
 * <p>
 * Tagging follows {@link DistantWaterShaderSupport#shaderMode()} so Off does not mutate meshes.
 * Refresh {@link #tagging()} on the main thread before chunk rebuilds; meshing threads only read it.
 */
public final class WaterFaceMarker {
	public static final int ALPHA = 253;

	private static volatile boolean tagging;
	private static final ThreadLocal<Boolean> WATER_QUAD = ThreadLocal.withInitial(() -> Boolean.FALSE);

	private WaterFaceMarker() {
	}

	/** Main-thread: cache whether new water quads should be tagged. */
	public static void refresh() {
		tagging = DistantWaterShaderSupport.shaderMode() != DistantWaterMode.OFF;
	}

	public static boolean tagging() {
		return tagging;
	}

	public static void beginQuad(FluidState state) {
		WATER_QUAD.set(tagging && state != null && state.is(FluidTags.WATER));
	}

	public static void endQuad() {
		WATER_QUAD.remove();
	}

	public static boolean currentQuadIsWater() {
		return Boolean.TRUE.equals(WATER_QUAD.get());
	}
}
