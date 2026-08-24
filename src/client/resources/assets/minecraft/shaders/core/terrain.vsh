#version 330

#moj_import <minecraft:fog.glsl>
#moj_import <minecraft:globals.glsl>
#moj_import <minecraft:chunksection.glsl>
#moj_import <minecraft:projection.glsl>
#moj_import <minecraft:sample_lightmap.glsl>
#moj_import <ecology:ecology_water_vertex.glsl>

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

    bool waterFace = ecologyIsWaterMarker(Color.a);
    ecologyWaterFace = waterFace ? 1.0 : 0.0;
    ecologyGrazing = waterFace ? ecologyGrazingFromView(pos) : 0.0;

    vec4 light = sample_lightmap(Sampler2, UV2);
    vertexColor = ecologyLitVertexColor(Color, light, waterFace);
    texCoord0 = UV0;
}
