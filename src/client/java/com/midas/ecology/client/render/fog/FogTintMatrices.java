package com.midas.ecology.client.render.fog;

import com.midas.ecology.EcologyMod;
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
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Captures level perspective {@code Proj * View} inverse for Fabulous transparency
 * depth unfog (post passes replace {@code Projection} with ortho).
 * <p>
 * {@code transparency.fsh} always declares this UBO, so it is captured and bound
 * whenever the transparency pass runs — including Distant water Off / Opaque.
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
	private static final Matrix4f TMP = new Matrix4f();

	private static boolean hasProj;
	private static boolean hasView;
	private static boolean hasInv;
	private static boolean dirty;
	private static boolean inTransparencyPass;
	private static GpuBuffer buffer;
	private static final AtomicBoolean INVERT_WARNED = new AtomicBoolean(false);

	private FogTintMatrices() {
	}

	public static void captureProjection(Matrix4fc projection) {
		PROJ.set(projection);
		hasProj = true;
		dirty = true;
	}

	public static void captureView(Matrix4fc modelView) {
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

	/** Bind InvClipToFog for the Fabulous transparency pass (always declared by Ecology's post shader). */
	public static void bindIfTransparencyPass(RenderPass renderPass) {
		if (!inTransparencyPass) {
			return;
		}
		if (dirty && hasProj && hasView) {
			recomputeAndUpload();
			dirty = false;
		}
		ensureBuffer();
		if (buffer != null) {
			renderPass.setUniform(UNIFORM_NAME, buffer);
		}
	}

	/** Drop the GPU buffer on resource reload / device reset so the next bind recreates it. */
	public static void releaseBuffer() {
		if (buffer != null) {
			buffer.close();
			buffer = null;
		}
		hasInv = false;
		dirty = true;
	}

	private static void recomputeAndUpload() {
		CLIP.set(PROJ).mul(VIEW);
		if (CLIP.invert(TMP) == null) {
			if (!hasInv) {
				INV.identity();
				hasInv = true;
			}
			if (INVERT_WARNED.compareAndSet(false, true)) {
				EcologyMod.LOGGER.warn("[Ecology Fog tint] Proj*View was singular; keeping last InvClipToFog");
			}
		} else {
			INV.set(TMP);
			hasInv = true;
			INVERT_WARNED.set(false);
		}
		uploadInv();
	}

	private static void uploadInv() {
		if (RenderSystem.getDevice() == null) {
			return;
		}
		ensureBuffer();
		if (buffer == null) {
			return;
		}
		try (MemoryStack stack = MemoryStack.stackPush()) {
			ByteBuffer data = Std140Builder.onStack(stack, UBO_SIZE).putMat4f(INV).get();
			RenderSystem.getDevice().createCommandEncoder().writeToBuffer(buffer.slice(), data);
		}
	}

	private static void ensureBuffer() {
		if (RenderSystem.getDevice() == null) {
			return;
		}
		if (buffer != null && buffer.isClosed()) {
			buffer = null;
			hasInv = false;
		}
		if (buffer == null) {
			buffer = RenderSystem.getDevice().createBuffer(() -> "Ecology FogTint InvClipToFog", UBO_USAGE, UBO_SIZE);
			if (!hasInv) {
				INV.identity();
				hasInv = true;
				try (MemoryStack stack = MemoryStack.stackPush()) {
					ByteBuffer data = Std140Builder.onStack(stack, UBO_SIZE).putMat4f(INV).get();
					RenderSystem.getDevice().createCommandEncoder().writeToBuffer(buffer.slice(), data);
				}
			}
		}
	}
}
