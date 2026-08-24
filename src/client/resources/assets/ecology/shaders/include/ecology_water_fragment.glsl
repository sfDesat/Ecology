// Opaque-water alpha + debug paints for marked water. No #version.
// Import after distant_water_settings.glsl and a fog helper that provides linear_fog_value.
// Fog tint (empty-behind) lives in post/transparency.fsh — terrain cannot see framebuffer depth.

vec4 ecologyApplyOpaqueWaterAndDebug(
    vec4 color,
    bool waterFace,
    float cylindricalDistance,
    float grazing,
    float fogRange
) {
    bool ecologyActive = EcologyWaterShaderEnabled > 0.5;
    bool cameraUnderwater = EcologyCameraUnderwater > 0.5;
    bool opacityMode = ecologyActive && EcologyDistantWaterMode > 0.5 && EcologyDistantWaterMode < 1.5;
    bool wantDebug = ecologyActive && (EcologyWaterDebugTops > 0.5
        || EcologyWaterDebugFresnel > 0.5
        || EcologyWaterDebugAll > 0.5);

    float opacityStart = fogRange * EcologyDistanceOpacityStart;
    float opacityEnd = max(fogRange * EcologyDistanceOpacityEnd, opacityStart + 0.001);
    float distFactor = linear_fog_value(cylindricalDistance, opacityStart, opacityEnd);
    float fresnelCurve = pow(clamp(grazing, 0.0, 1.0), max(EcologyFresnelPower, 0.25));

    if (waterFace && (opacityMode || wantDebug)) {
        if (opacityMode && !cameraUnderwater && color.a > 0.001) {
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

    if (ecologyActive && EcologyWaterDebugAll > 0.5 && color.a > 0.001 && color.a < 0.999) {
        color = vec4(0.0, 1.0, 1.0, max(color.a, 0.85));
    }
    return color;
}

vec4 ecologyEncodeFogTintMask(vec4 fragColor, bool waterFace) {
    bool fogRemapMode = EcologyWaterShaderEnabled > 0.5 && EcologyDistantWaterMode > 1.5;
    if (fogRemapMode) {
        return ecologyEncodeTranslucentMask(fragColor, waterFace);
    }
    return fragColor;
}
