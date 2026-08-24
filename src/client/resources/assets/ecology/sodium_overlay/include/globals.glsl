layout(std140) uniform u_Globals {
    mat4 u_ProjectionMatrix;
    mat4 u_ModelViewMatrix;

    vec4 u_FogColor;
    vec2 u_EnvironmentFog;
    vec2 u_RenderFog;

    vec2 u_TexelSize;
    vec2 u_TexCoordShrink;

    float u_FadePeriodInv;
    bool u_UseRGSS;

    // Ecology extras (appended by GlobalUniformsMixin; must stay within Sodium's 256-byte UBO).
    // FogUboLayout.SODIUM_EXTRAS_SIZE / FogUboLayout.appendExtras.
    vec4 EcologyWaterFogColor;
    float EcologyCameraUnderwater;
};
