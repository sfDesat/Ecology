package com.midas.ecology.client.render.fog;

import com.mojang.blaze3d.buffers.GpuBuffer;
import com.mojang.blaze3d.buffers.Std140Builder;
import com.mojang.blaze3d.buffers.Std140SizeCalculator;
import com.mojang.blaze3d.pipeline.BindGroupLayout;
import com.mojang.blaze3d.shaders.UniformType;
import com.mojang.blaze3d.systems.RenderPass;
import com.mojang.blaze3d.systems.RenderSystem;
import org.joml.Matrix4f;
import org.joml.Matrix4fc;
import org.lwjgl.system.MemoryStack;

import java.nio.ByteBuffer;

/**
 * Captures level perspective {@code Proj * View} inverse for Fabulous transparency
 * depth unfog (post passes replace {@code Projection} with ortho).
 * <p>
 * Matrices are stored cheaply during the level pass; GPU upload happens once on first
 * bind of the transparency post pass while Fog tint is active.
 */
public final class FogTintMatrices {
	public static final String UNIFORM_NAME = "EcologyDepthFog";
	public static final int UBO_SIZE = new Std140SizeCalculator().putMat4f().get();
	public static final BindGroupLayout BIND_GROUP_LAYOUT = BindGroupLayout.builder()
		.withUniform(UNIFORM_NAME, UniformType.UNIFORM_BUFFER)
		.build();

	/** {@link GpuBuffer#USAGE_COPY_DST} | {@link GpuBuffer#USAGE_UNIFORM} */
	private static final int UBO_USAGE = GpuBuffer.USAGE_COPY_DST | GpuBuffer.USAGE_UNIFORM;

	private static final Matrix4f PROJ = new Matrix4f();
	private static final Matrix4f VIEW = new Matrix4f();
	private static final Matrix4f CLIP = new Matrix4f();
	private static final Matrix4f INV = new Matrix4f();

	private static boolean hasProj;
	private static boolean hasView;
	private static boolean dirty;
	private static boolean inTransparencyPass;
	private static GpuBuffer buffer;

	private FogTintMatrices() {
	}

	public static void captureProjection(Matrix4fc projection) {
		if (!FogTint.isActive()) {
			return;
		}
		PROJ.set(projection);
		hasProj = true;
		dirty = true;
	}

	public static void captureView(Matrix4fc modelView) {
		if (!FogTint.isActive()) {
			return;
		}
		VIEW.set(modelView);
		hasView = true;
		dirty = true;
	}

	public static void beginTransparencyPass() {
		inTransparencyPass = true;
	}

	public static void endTransparencyPass() {
		inTransparencyPass = false;
	}

	/** Bind InvClipToFog when the Fabulous transparency pass is drawing and Fog tint is on. */
	public static void bindIfTransparencyPass(RenderPass renderPass) {
		if (!inTransparencyPass || !FogTint.isActive()) {
			return;
		}
		if (!hasProj || !hasView) {
			return;
		}
		if (dirty) {
			recomputeAndUpload();
			dirty = false;
		}
		if (buffer != null) {
			renderPass.setUniform(UNIFORM_NAME, buffer);
		}
	}

	private static void recomputeAndUpload() {
		CLIP.set(PROJ).mul(VIEW);
		if (CLIP.invert(INV) == null) {
			INV.identity();
		}
		if (RenderSystem.getDevice() == null) {
			return;
		}
		ensureBuffer();
		try (MemoryStack stack = MemoryStack.stackPush()) {
			ByteBuffer data = Std140Builder.onStack(stack, UBO_SIZE).putMat4f(INV).get();
			RenderSystem.getDevice().createCommandEncoder().writeToBuffer(buffer.slice(), data);
		}
	}

	private static void ensureBuffer() {
		if (buffer == null) {
			buffer = RenderSystem.getDevice().createBuffer(() -> "Ecology FogTint InvClipToFog", UBO_USAGE, UBO_SIZE);
		}
	}
}
