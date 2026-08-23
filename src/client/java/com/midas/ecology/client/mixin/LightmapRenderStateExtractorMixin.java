package com.midas.ecology.client.mixin;

import com.midas.ecology.client.render.UnderwaterLighting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.LightmapRenderStateExtractor;
import net.minecraft.client.renderer.state.LightmapRenderState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * After vanilla fills the lightmap state, add extra underwater ambient so deep Ecology
 * oceans do not go cave-black after ~15 blocks of water overhead.
 */
@Mixin(LightmapRenderStateExtractor.class)
public class LightmapRenderStateExtractorMixin {
	@Inject(
		method = "extract(Lnet/minecraft/client/renderer/state/LightmapRenderState;F)V",
		at = @At("TAIL")
	)
	private void ecology$boostUnderwaterLightmap(LightmapRenderState state, float partialTick, CallbackInfo ci) {
		UnderwaterLighting.boostLightmap(state, Minecraft.getInstance());
	}
}
