package com.midas.ecology.client.mixin;

import com.midas.ecology.client.render.fog.FogTint;
import com.midas.ecology.client.render.fog.FogTintMatrices;
import com.mojang.blaze3d.pipeline.RenderPipeline;
import net.minecraft.client.renderer.PostPass;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArg;

/**
 * Marks the Fabulous transparency composite pass so {@link FogTintMatrices} can bind its UBO.
 */
@Mixin(PostPass.class)
public class PostPassMixin {
	@Shadow
	@Final
	private RenderPipeline pipeline;

	@ModifyArg(
		method = "addToFrame",
		at = @At(
			value = "INVOKE",
			target = "Lcom/mojang/blaze3d/framegraph/FramePass;executes(Ljava/lang/Runnable;)V"
		),
		index = 0
	)
	private Runnable ecology$wrapTransparencyExecute(Runnable original) {
		if (!FogTint.isTransparencyFragment(this.pipeline.getFragmentShader())) {
			return original;
		}
		return () -> {
			FogTintMatrices.beginTransparencyPass();
			try {
				original.run();
			} finally {
				FogTintMatrices.endTransparencyPass();
			}
		};
	}
}
