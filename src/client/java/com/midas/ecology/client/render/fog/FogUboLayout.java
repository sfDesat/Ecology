package com.midas.ecology.client.render.fog;

import com.mojang.blaze3d.buffers.Std140Builder;
import com.mojang.blaze3d.buffers.Std140SizeCalculator;
import org.joml.Vector4fc;

import java.nio.ByteBuffer;

/**
 * Single source of truth for the Ecology-extended Fog UBO.
 * Must stay in sync with {@code assets/minecraft/shaders/include/fog.glsl}.
 * <p>
 * Layout:
 * <pre>
 * vec4  FogColor
 * float FogEnvironmentalStart/End
 * float FogRenderDistanceStart/End
 * float FogSkyEnd
 * float FogCloudsEnd
 * vec4  EcologyWaterFogColor
 * float EcologyCameraUnderwater
 * </pre>
 */
public final class FogUboLayout {
	public static final int SIZE = new Std140SizeCalculator()
		.putVec4()
		.putFloat()
		.putFloat()
		.putFloat()
		.putFloat()
		.putFloat()
		.putFloat()
		.putVec4()
		.putFloat()
		.get();

	private FogUboLayout() {
	}

	/**
	 * Writes the full Fog UBO at {@code offset}. Advances the buffer position past the payload
	 * so callers that {@link ByteBuffer#flip()} (empty-fog init) get the correct size.
	 */
	public static void write(
		ByteBuffer buffer,
		int offset,
		Vector4fc fogColor,
		float environmentalStart,
		float environmentalEnd,
		float renderDistanceStart,
		float renderDistanceEnd,
		float skyEnd,
		float cloudEnd,
		Vector4fc waterFogColor,
		float cameraUnderwater
	) {
		buffer.position(offset);
		Std140Builder.intoBuffer(buffer)
			.putVec4(fogColor)
			.putFloat(environmentalStart)
			.putFloat(environmentalEnd)
			.putFloat(renderDistanceStart)
			.putFloat(renderDistanceEnd)
			.putFloat(skyEnd)
			.putFloat(cloudEnd)
			.putVec4(waterFogColor)
			.putFloat(cameraUnderwater);
	}
}
