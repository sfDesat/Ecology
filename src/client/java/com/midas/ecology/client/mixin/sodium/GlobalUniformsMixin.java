package com.midas.ecology.client.mixin.sodium;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.mojang.blaze3d.buffers.Std140Builder;
import com.midas.ecology.client.render.DistantWaterShaderSupport;
import com.midas.ecology.client.render.fog.FogUboLayout;
import com.midas.ecology.client.render.fog.FogUboState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

import java.nio.ByteBuffer;

/**
 * Appends Ecology extras to Sodium's {@code u_Globals} when the Sodium overlay is active.
 * Layout must match Ecology's overlaid {@code sodium:shaders/include/globals.glsl}.
 * Skipped when Distant water is Off so upstream Sodium globals stay valid.
 */
@Mixin(targets = "net.caffeinemc.mods.sodium.client.render.chunk.UniformBufferManager$GlobalUniforms")
public class GlobalUniformsMixin {
	@WrapOperation(
		method = "write",
		at = @At(
			value = "INVOKE",
			target = "Lcom/mojang/blaze3d/buffers/Std140Builder;get()Ljava/nio/ByteBuffer;"
		)
	)
	private ByteBuffer ecology$appendFogExtras(Std140Builder builder, Operation<ByteBuffer> original) {
		if (!DistantWaterShaderSupport.sodiumOverlayActive()) {
			return original.call(builder);
		}
		FogUboLayout.appendExtras(builder, FogUboState.waterFogColor(), FogUboState.cameraUnderwater());
		return original.call(builder);
	}
}
