#version 330

#moj_import <minecraft:fog.glsl>
#moj_import <minecraft:globals.glsl>
#moj_import <minecraft:chunksection.glsl>
#moj_import <minecraft:projection.glsl>
#moj_import <minecraft:sample_lightmap.glsl>

in vec3 Position;
in vec4 Color;
in vec2 UV0;
in ivec2 UV2;

uniform sampler2D Sampler2;

out float sphericalVertexDistance;
out float cylindricalVertexDistance;
out vec4 vertexColor;
out vec2 texCoord0;
out float ecologyWaterFace;
out float ecologyGrazing;

void main() {
    vec3 pos = Position + (ChunkPosition - CameraBlockPos) + CameraOffset;
    gl_Position = ProjMat * ModelViewMat * vec4(pos, 1.0);

    sphericalVertexDistance = fog_spherical_distance(pos);
    cylindricalVertexDistance = fog_cylindrical_distance(pos);

    // FluidRenderer tags water faces with alpha 253/255 (~0.992156).
    bool waterFace = Color.a > 0.980 && Color.a < 0.999;
    ecologyWaterFace = waterFace ? 1.0 : 0.0;

    // Raw grazing vs world-up (0 = looking down, 1 = horizon). Used by opaque-water fresnel.
    ecologyGrazing = 0.0;
    if (waterFace) {
        float posLen = length(pos);
        if (posLen > 1e-4) {
            float cosTheta = clamp((-pos / posLen).y, 0.0, 1.0);
            ecologyGrazing = 1.0 - cosTheta;
        }
    }

    vec4 light = sample_lightmap(Sampler2, UV2);
    if (waterFace) {
        // Restore full vertex alpha so the marker does not permanently darken water.
        vertexColor = vec4(Color.rgb * light.rgb, light.a);
    } else {
        vertexColor = Color * light;
    }
    texCoord0 = UV0;
}
