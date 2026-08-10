package com.midas.ecology.client.mixin;

import com.midas.ecology.client.render.fog.FogTintMatrices;
import com.mojang.blaze3d.buffers.GpuBufferSlice;
import com.mojang.blaze3d.resource.GraphicsResourceAllocator;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import org.joml.Matrix4fc;
import org.joml.Vector4f;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Captures the level model-view used for terrain fog distances.
 */
@Mixin(LevelRenderer.class)
public class LevelRendererMixin {
	@Inject(method = "render", at = @At("HEAD"))
	private void ecology$captureView(
		GraphicsResourceAllocator resourceAllocator,
		DeltaTracker deltaTracker,
		boolean renderOutline,
		CameraRenderState cameraState,
		Matrix4fc modelViewMatrix,
		GpuBufferSlice terrainFog,
		Vector4f fogColor,
		boolean shouldRenderSky,
		CallbackInfo ci
	) {
		FogTintMatrices.captureView(modelViewMatrix);
	}
}
