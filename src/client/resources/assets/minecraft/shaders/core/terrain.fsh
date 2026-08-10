#version 330

#moj_import <minecraft:fog.glsl>
#moj_import <minecraft:globals.glsl>
#moj_import <minecraft:chunksection.glsl>
#moj_import <ecology:distant_water_settings.glsl>

uniform sampler2D Sampler0;

in float sphericalVertexDistance;
in float cylindricalVertexDistance;
in vec4 vertexColor;
in vec2 texCoord0;
in float ecologyWaterTop;
in float ecologyGrazing;

out vec4 fragColor;

vec4 sampleNearest(sampler2D source, vec2 uv, vec2 pixelSize, vec2 du, vec2 dv, vec2 texelScreenSize) {
    // Convert our UV back up to texel coordinates and find out how far over we are from the center of each pixel
    vec2 uvTexelCoords = uv / pixelSize;
    vec2 texelCenter = round(uvTexelCoords) - 0.5f;
    vec2 texelOffset = uvTexelCoords - texelCenter;

    // Move our offset closer to the texel center based on texel size on screen
    texelOffset = (texelOffset - 0.5f) * pixelSize / texelScreenSize + 0.5f;
    texelOffset = clamp(texelOffset, 0.0f, 1.0f);

    uv = (texelCenter + texelOffset) * pixelSize;
    return textureGrad(source, uv, du, dv);
}

vec4 sampleNearest(sampler2D source, vec2 uv, vec2 pixelSize) {
    vec2 du = dFdx(uv);
    vec2 dv = dFdy(uv);
    vec2 texelScreenSize = sqrt(du * du + dv * dv);
    return sampleNearest(source, uv, pixelSize, du, dv, texelScreenSize);
}

// Rotated Grid Super-Sampling
vec4 sampleRGSS(sampler2D source, vec2 uv, vec2 pixelSize) {
    vec2 du = dFdx(uv);
    vec2 dv = dFdy(uv);

    vec2 texelScreenSize = sqrt(du * du + dv * dv);
    float maxTexelSize = max(texelScreenSize.x, texelScreenSize.y);

    float minPixelSize = min(pixelSize.x, pixelSize.y);

    float transitionStart = minPixelSize * 1.0;
    float transitionEnd = minPixelSize * 2.0;
    float blendFactor = smoothstep(transitionStart, transitionEnd, maxTexelSize);

    float duLength = length(du);
    float dvLength = length(dv);
    float minDerivative = min(duLength, dvLength);
    float maxDerivative = max(duLength, dvLength);

    float effectiveDerivative = sqrt(minDerivative * maxDerivative);

    float mipLevelExact = max(0.0, log2(effectiveDerivative / minPixelSize));

    float mipLevelLow = floor(mipLevelExact);
    float mipLevelHigh = mipLevelLow + 1.0;
    float mipBlend = fract(mipLevelExact);

    const vec2 offsets[4] = vec2[](
    vec2(0.125, 0.375),
    vec2(-0.125, -0.375),
    vec2(0.375, -0.125),
    vec2(-0.375, 0.125)
    );

    vec4 rgssColorLow = vec4(0.0);
    vec4 rgssColorHigh = vec4(0.0);
    for (int i = 0; i < 4; ++i) {
        vec2 sampleUV = uv + offsets[i] * pixelSize;
        rgssColorLow += textureLod(source, sampleUV, mipLevelLow);
        rgssColorHigh += textureLod(source, sampleUV, mipLevelHigh);
    }
    rgssColorLow *= 0.25;
    rgssColorHigh *= 0.25;

    vec4 rgssColor = mix(rgssColorLow, rgssColorHigh, mipBlend);

    vec4 nearestColor = sampleNearest(source, uv, pixelSize, du, dv, texelScreenSize);

    return mix(nearestColor, rgssColor, blendFactor);
}

void main() {
    vec4 color = (UseRgss == 1 ? sampleRGSS(Sampler0, texCoord0, 1.0f / TextureSize) : sampleNearest(Sampler0, texCoord0, 1.0f / TextureSize)) * vertexColor;

    bool waterTop = ecologyWaterTop > 0.5;
    bool wantEffect = EcologyWaterShaderEnabled > 0.5;
    bool wantDebug = EcologyWaterDebugTops > 0.5 || EcologyWaterDebugFresnel > 0.5 || EcologyWaterDebugAll > 0.5;
    bool cameraUnderwater = FogCloudsEnd < 0.0;

    // Skip distance/fresnel work unless this is a marked top or a debug path needs it.
    if (waterTop && (wantEffect || wantDebug)) {
        float fogRange = max(FogRenderDistanceEnd, 1.0);
        float opacityStart = fogRange * EcologyDistanceOpacityStart;
        float opacityEnd = max(fogRange * EcologyDistanceOpacityEnd, opacityStart + 0.001);
        float distFactor = linear_fog_value(cylindricalVertexDistance, opacityStart, opacityEnd);
        float fresnelCurve = pow(clamp(ecologyGrazing, 0.0, 1.0), max(EcologyFresnelPower, 0.25));

        if (wantEffect && !cameraUnderwater && color.a > 0.001) {
            float distOpacity = EcologyDistanceOpacityEnabled > 0.5
                ? distFactor * EcologyDistanceOpacityStrength
                : 0.0;
            float fresnelOpacity = EcologyFresnelEnabled > 0.5
                ? fresnelCurve * EcologyFresnelStrength
                : 0.0;
            float opacityFactor = clamp(distOpacity + fresnelOpacity, 0.0, 1.0);
            color.a = mix(color.a, 1.0, opacityFactor);
        }

        if (EcologyWaterDebugTops > 0.5) {
            vec3 nearCol = vec3(1.0, 1.0, 0.0);
            vec3 farCol = vec3(0.0, 0.25, 1.0);
            color = vec4(mix(nearCol, farCol, distFactor), cameraUnderwater ? 0.35 : 1.0);
        }

        if (EcologyWaterDebugFresnel > 0.5) {
            vec3 downCol = vec3(0.0, 1.0, 0.2);
            vec3 midCol = vec3(1.0, 1.0, 0.0);
            vec3 horizonCol = vec3(1.0, 0.05, 0.05);
            vec3 fresnelCol = fresnelCurve < 0.5
                ? mix(downCol, midCol, fresnelCurve * 2.0)
                : mix(midCol, horizonCol, (fresnelCurve - 0.5) * 2.0);
            color = vec4(fresnelCol, cameraUnderwater ? 0.35 : 1.0);
        }
    }

    if (EcologyWaterDebugAll > 0.5 && color.a > 0.001 && color.a < 0.999) {
        color = vec4(0.0, 1.0, 1.0, max(color.a, 0.85));
    }

    color = mix(FogColor * vec4(1, 1, 1, color.a), color, ChunkVisibility);
#ifdef ALPHA_CUTOUT
    if (color.a < ALPHA_CUTOUT) {
        discard;
    }
#endif
    fragColor = apply_fog(color, sphericalVertexDistance, cylindricalVertexDistance, FogEnvironmentalStart, FogEnvironmentalEnd, FogRenderDistanceStart, FogRenderDistanceEnd, FogColor);
}
