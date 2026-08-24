package com.midas.ecology.client.mixin;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.sugar.Local;
import com.midas.ecology.client.render.fog.FogTint;
import com.midas.ecology.client.render.fog.FogTintMatrices;
import com.mojang.blaze3d.pipeline.BindGroupLayout;
import com.mojang.blaze3d.pipeline.RenderPipeline;
import net.minecraft.client.renderer.BindGroupLayouts;
import net.minecraft.client.renderer.PostChain;
import net.minecraft.client.renderer.PostChainConfig;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

/**
 * Only the Fabulous {@code transparency} pass declares Fog + Fog tint UBOs.
 * {@code ordinal = 0} is the first {@code withBindGroupLayout} while building that pass.
 */
@Mixin(PostChain.class)
public class PostChainMixin {
	@WrapOperation(
		method = "createPass",
		at = @At(
			value = "INVOKE",
			target = "Lcom/mojang/blaze3d/pipeline/RenderPipeline$Builder;withBindGroupLayout(Lcom/mojang/blaze3d/pipeline/BindGroupLayout;)Lcom/mojang/blaze3d/pipeline/RenderPipeline$Builder;",
			ordinal = 0
		)
	)
	private static RenderPipeline.Builder ecology$bindFogTintForTransparency(
		RenderPipeline.Builder builder,
		BindGroupLayout layout,
		Operation<RenderPipeline.Builder> original,
		@Local(argsOnly = true) PostChainConfig.Pass config
	) {
		if (FogTint.isTransparencyFragment(config.fragmentShaderId())) {
			builder = builder
				.withBindGroupLayout(BindGroupLayouts.FOG)
				.withBindGroupLayout(FogTintMatrices.BIND_GROUP_LAYOUT);
		}
		return original.call(builder, layout);
	}
}
