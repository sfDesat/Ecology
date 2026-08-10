package com.midas.ecology.client.mixin;

import com.midas.ecology.client.render.fog.FogTintMatrices;
import net.minecraft.client.renderer.GameRenderer;
import org.joml.Matrix4f;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArg;

/**
 * Saves the level perspective projection before Fabulous transparency switches to ortho.
 */
@Mixin(GameRenderer.class)
public class GameRendererMixin {
	@ModifyArg(
		method = "renderLevel",
		at = @At(
			value = "INVOKE",
			target = "Lnet/minecraft/client/renderer/ProjectionMatrixBuffer;getBuffer(Lorg/joml/Matrix4f;)Lcom/mojang/blaze3d/buffers/GpuBufferSlice;",
			ordinal = 0
		),
		index = 0
	)
	private Matrix4f ecology$captureLevelProjection(Matrix4f projectionMatrix) {
		FogTintMatrices.captureProjection(projectionMatrix);
		return projectionMatrix;
	}
}
