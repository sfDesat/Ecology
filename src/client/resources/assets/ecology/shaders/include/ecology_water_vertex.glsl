// Water-face marker + grazing. No #version — #moj_import'd into terrain / Sodium overlay VS.
// Fluid mixins tag water with alpha 253/255 (~0.992156) when Distant water is not Off.

bool ecologyIsWaterMarker(float alpha) {
    return alpha > 0.980 && alpha < 0.999;
}

float ecologyGrazingFromView(vec3 viewPos) {
    float posLen = length(viewPos);
    if (posLen <= 1e-4) {
        return 0.0;
    }
    float cosTheta = clamp((-viewPos / posLen).y, 0.0, 1.0);
    return 1.0 - cosTheta;
}

vec4 ecologyLitVertexColor(vec4 color, vec4 light, bool waterFace) {
    if (waterFace) {
        // Restore full vertex alpha so the marker does not permanently darken water.
        return vec4(color.rgb * light.rgb, light.a);
    }
    return color * light;
}
