// Water mask protocol for Fog tint (terrain → Fabulous transparency).
// No #version — #moj_import'd into core/terrain and post/transparency.
//
// Fabulous has one translucent color target (water + glass + ice). Pack a water bit into
// alpha: visual opacity in 7 bits, water flag in the LSB. Decode before compositing.

vec4 ecologyEncodeTranslucentMask(vec4 color, bool waterFace) {
    if (color.a <= 0.001 || color.a >= 0.999) {
        return color;
    }
    float q = floor(clamp(color.a, 0.0, 1.0) * 127.0 + 0.5);
    q = min(q, 126.0);
    float encodedA = (q * 2.0 + (waterFace ? 1.0 : 0.0)) / 255.0;
    return vec4(color.rgb, encodedA);
}

bool ecologyDecodeWaterMask(inout vec4 translucent) {
    if (translucent.a <= 0.001 || translucent.a >= 0.999) {
        return false;
    }
    float a8 = floor(translucent.a * 255.0 + 0.5);
    bool isWater = mod(a8, 2.0) > 0.5;
    translucent.a = floor(a8 * 0.5) / 127.0;
    return isWater;
}
