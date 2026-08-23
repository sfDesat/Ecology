#version 330

#moj_import <minecraft:fog.glsl>
#moj_import <ecology:distant_water_settings.glsl>
#moj_import <ecology:water_mask.glsl>

uniform sampler2D MainSampler;
uniform sampler2D MainDepthSampler;
uniform sampler2D TranslucentSampler;
uniform sampler2D TranslucentDepthSampler;
uniform sampler2D ItemEntitySampler;
uniform sampler2D ItemEntityDepthSampler;
uniform sampler2D ParticlesSampler;
uniform sampler2D ParticlesDepthSampler;
uniform sampler2D WeatherSampler;
uniform sampler2D WeatherDepthSampler;
uniform sampler2D CloudsSampler;
uniform sampler2D CloudsDepthSampler;

layout(std140) uniform EcologyDepthFog {
    mat4 InvClipToFog;
};

in vec2 texCoord;

vec4 color_layers[6] = vec4[](vec4(0.0), vec4(0.0), vec4(0.0), vec4(0.0), vec4(0.0), vec4(0.0));
float depth_layers[6] = float[](0, 0, 0, 0, 0, 0);
int active_layers = 0;

out vec4 fragColor;

void try_insert(vec4 color, float depth) {
    if (color.a == 0.0) {
        return;
    }

    color_layers[active_layers] = color;
    depth_layers[active_layers] = depth;

    int jj = active_layers++;
    int ii = jj - 1;
    while (jj > 0 && depth_layers[jj] < depth_layers[ii]) {
        float depthTemp = depth_layers[ii];
        depth_layers[ii] = depth_layers[jj];
        depth_layers[jj] = depthTemp;

        vec4 colorTemp = color_layers[ii];
        color_layers[ii] = color_layers[jj];
        color_layers[jj] = colorTemp;

        jj = ii--;
    }
}

vec3 blend(vec3 dst, vec4 src) {
    return (dst * (1.0 - src.a)) + src.rgb;
}

// Camera-relative fog space (same basis as terrain.vsh pos). Reverse-Z: far/clear ≈ 0.
vec3 ecologyFogPosFromDepth(float depth) {
    vec4 clip = vec4(texCoord * 2.0 - 1.0, depth, 1.0);
    vec4 fogPos4 = InvClipToFog * clip;
    return fogPos4.xyz / max(fogPos4.w, 1e-6);
}

float ecologyAirFogAmount(vec3 fogPos) {
    return total_fog_value(
        fog_spherical_distance(fogPos),
        fog_cylindrical_distance(fogPos),
        FogEnvironmentalStart,
        FogEnvironmentalEnd,
        FogRenderDistanceStart,
        FogRenderDistanceEnd
    );
}

float ecologyUnderwaterSightFog(float viewDist) {
    float rd = max(FogRenderDistanceEnd, 1.0);
    float sightStart = EcologyUnderwaterSightEndUsePercent > 0.5
        ? rd * clamp(EcologyUnderwaterSightStartPercent, 0.0, 1.0)
        : EcologyUnderwaterSightStart;
    float sightEndBlocks = EcologyUnderwaterSightEndUsePercent > 0.5
        ? rd * clamp(EcologyUnderwaterSightEndPercent, 0.0, 1.0)
        : EcologyUnderwaterSightEnd;
    // Always at least start + 1 block.
    float sightEnd = max(sightEndBlocks, sightStart + 1.0);
    return linear_fog_value(viewDist, sightStart, sightEnd);
}

vec3 ecologyUnfogAir(vec3 mainRgb, float airFogAmt, float airness, float cover) {
    float f = clamp(airFogAmt * FogColor.a, 0.0, 0.90);
    if (airness > 0.55) {
        // Chunk fade / full air fog destroyed albedo — don't restore sky-white.
        return mix(mainRgb, EcologyWaterFogColor.rgb, cover * max(airFogAmt, airness));
    }
    vec3 albedo = (mainRgb - FogColor.rgb * f) / max(1.0 - f, 1e-3);
    bool unstable = any(lessThan(albedo, vec3(-0.02))) || any(greaterThan(albedo, vec3(1.15)));
    if (unstable) {
        return mix(mainRgb, EcologyWaterFogColor.rgb, cover * airFogAmt);
    }
    albedo = clamp(albedo, 0.0, 1.0);
    float stillAir = 1.0 - smoothstep(0.025, 0.20, length(albedo - FogColor.rgb));
    vec3 restored = mix(albedo, EcologyWaterFogColor.rgb, stillAir);
    return mix(mainRgb, restored, cover);
}

void main() {
    vec3 mainRgb = texture(MainSampler, texCoord).rgb;
    float mainDepth = texture(MainDepthSampler, texCoord).r;
    vec4 translucent = texture(TranslucentSampler, texCoord);
    float translucentDepth = texture(TranslucentDepthSampler, texCoord).r;

    bool ecologyActive = EcologyWaterShaderEnabled > 0.5;
    bool fogRemapMode = ecologyActive && EcologyDistantWaterMode > 1.5;
    bool cameraUnderwater = EcologyCameraUnderwater > 0.5;
    bool emptyBehind = mainDepth <= 1.0e-5;
    float cover = clamp(EcologyFogRemapBiasStrength, 0.0, 1.0);

    bool isWater = false;
    if (fogRemapMode) {
        isWater = ecologyDecodeWaterMask(translucent);
    }

    if (fogRemapMode && isWater && !cameraUnderwater) {
        float airFogAmt;
        float sightFog;
        float airness;
        if (emptyBehind) {
            airFogAmt = 1.0;
            airness = 1.0;
            sightFog = 1.0;
            mainRgb = mix(mainRgb, EcologyWaterFogColor.rgb, cover);
        } else {
            vec3 fogPos = ecologyFogPosFromDepth(mainDepth);
            airFogAmt = ecologyAirFogAmount(fogPos);
            sightFog = ecologyUnderwaterSightFog(fog_cylindrical_distance(fogPos));
            airness = 1.0 - smoothstep(0.025, 0.22, length(mainRgb - FogColor.rgb));
            mainRgb = ecologyUnfogAir(mainRgb, airFogAmt, airness, cover);
            mainRgb = mix(mainRgb, EcologyWaterFogColor.rgb, cover * sightFog);
        }

        // Debug only while fog tint is the active system (Off must be a full no-op).
        if (EcologyWaterDebugFogRemap > 0.5
            && EcologyWaterDebugTops < 0.5 && EcologyWaterDebugFresnel < 0.5) {
            float pinkAmt = emptyBehind ? max(cover, 0.85) : cover * max(sightFog, airness * 0.35);
            mainRgb = mix(mainRgb, vec3(1.0, 0.15, 0.95), clamp(pinkAmt, 0.0, 1.0));
        }

        // Air fog on the water surface, as if the face were an opaque land block.
        vec3 surfacePos = ecologyFogPosFromDepth(translucentDepth);
        float surfaceFog = ecologyAirFogAmount(surfacePos) * FogColor.a * clamp(EcologySurfaceAirFog, 0.0, 1.0);
        translucent.a = mix(translucent.a, 1.0, surfaceFog);
        translucent.rgb = mix(translucent.rgb, FogColor.rgb, surfaceFog);
    }

    color_layers[0] = vec4(mainRgb, 1.0);
    depth_layers[0] = mainDepth;
    active_layers = 1;

    try_insert(translucent, translucentDepth);
    try_insert(texture(ItemEntitySampler, texCoord), texture(ItemEntityDepthSampler, texCoord).r);
    try_insert(texture(ParticlesSampler, texCoord), texture(ParticlesDepthSampler, texCoord).r);
    try_insert(texture(WeatherSampler, texCoord), texture(WeatherDepthSampler, texCoord).r);
    try_insert(texture(CloudsSampler, texCoord), texture(CloudsDepthSampler, texCoord).r);

    vec3 texelAccum = color_layers[0].rgb;
    for (int ii = 1; ii < active_layers; ++ii) {
        texelAccum = blend(texelAccum, color_layers[ii]);
    }

    fragColor = vec4(texelAccum.rgb, 1.0);
}
