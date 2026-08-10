# Distant water: pale basin outline

Reference note for the visual bug seen when looking at deep oceans from above, and options for fixing it without ruining close-up water.

## Problem

When the **camera is above water**, far ocean often looks wrong:

1. At render distance, many **water faces have no solid blocks behind them** (deep columns, basin sides, open water past the seafloor silhouette).
2. Through that translucent water you see **atmospheric fog and/or sky** in the framebuffer — the pale band at horizon / chunk fade distance.
3. Where the **seafloor is present**, you see real terrain (darker, textured).
4. The contrast between “water over pale fog/sky” and “water over seafloor” draws a sharp **outline of the sea basin**.

Deeper oceans make this worse: more vertical water → more empty-behind water → stronger silhouette.

```text
Camera (in air)
    |  looking down / toward horizon
    v
[ water over seafloor ]  -->  darker terrain
[ water over empty    ]  -->  pale fog / sky showing through
         ^
         contrast = visible basin outline
```

### What this is not

- **Not** mainly “the seafloor itself is fogged white.” The seafloor is the dark side of the contrast.
- **Not** an underwater-fog bug. Submerged water fog already looks fine; leave that path alone.
- **Not** a request to change fog or sky **above the waterline** (land, horizon air, sky dome should stay vanilla atmospheric fog).
- **Not** a request to recolor atmospheric fog sitting **on top of** the water surface — that should stay normal white/air fog.

### Two different “fogs”

| Fog | Where | Desired |
|-----|--------|---------|
| **On top of water** | Distance fog applied to the water mesh / air above the surface | Stay **white** (air `FogColor`) |
| **Behind water** | Pale fog/sky already in the framebuffer, seen *through* translucent water with nothing solid behind | Tint / replace with **water fog color** |

## Constraints for a better fix

- Fix pale fog/sky **only where it shows through water**
- Do **not** change atmospheric fog on the water surface (keep it white)
- Do **not** change fog / sky for air and land above the waterline
- Prefer **not** making close-up water look solid (opaque mode’s failure mode)
- Prefer a system **separate** from the opacity/fresnel feature, with config to choose which approach to use

## Approaches considered

| Approach | Idea | Pros | Cons |
|----------|------|------|------|
| **A. Opacity / fresnel** | Raise water alpha with distance and angle | Hides behind; simple | Close-up / grazing water looks solid |
| **B. Remap fog *on* water faces** | Change `apply_fog` on water to water fog color | Easy | Wrong target — darkens fog *on top* of water; behind sky still bleeds through alpha |
| **C. Remap fog *behind* water** | Composite water as if behind were water fog, then soft-cover destination at distance | Matches the real bug; surface fog stays white | Needs soft alpha cover at distance (unlike opaque mode, destination is water-fog colored, not solid water texture) |
| **D. Post-process fog ∩ water** | Detect water + fog-like pixels | Intuitive | Hard in vanilla; Sodium/Iris fragile |

## Recommended solution

**Primary: C — remap fog/sky behind water (Fog tint mode), separate from opaque water.**

When the camera is in air and the fragment is marked water:

1. Leave `FogColor` / `apply_fog` as **air fog** (white on the water surface).
2. At distance, composite the fragment as `water.rgb * water.a + waterFog.rgb * (1 - water.a)`, then soft-raise alpha so the real pale framebuffer behind is replaced by that composite.
3. Close-up (`distFactor` low) unchanged — still clear water over whatever is behind.
4. Land / sky / above-waterline fog untouched. Underwater camera path untouched.

Config modes:

- `OFF` — vanilla water
- `OPACITY` — distance + fresnel alpha (hides behind by making water solid-looking)
- `FOG_REMAP` / Fog tint — behind-water composite toward biome water fog (surface fog stays white)

### Debug

“Highlight fog-tint fog (pink)” should paint the **behind** component pink (see-through / empty-behind), **not** the whole ocean surface and **not** the white fog on top of water.

### Success check

- Deep Open Ocean / Deep Basin shelf: pale empty-behind outline gone or soft with Fog tint; white fog on the water surface still looks like air fog
- Close-up water still looks like water
- Sky and fog above the waterline unchanged
- Underwater unchanged
- User can pick opaque water **or** fog tint

## Related code (as of this note)

- Mode + settings: `DistantWaterMode`, `EcologyClientConfig`, `DistantWaterSettingsPack`, Mod Menu screen
- Water-face marker: `FluidRendererMixin` (alpha 253), `assets/minecraft/shaders/core/terrain.{vsh,fsh}`
- Fog tint module: `com.midas.ecology.client.render.fog` (`FogTint`, `FogTintMatrices`)
- Water fog color + underwater flag: `fog.glsl` (`EcologyWaterFogColor`, `EcologyCameraUnderwater`), `FogRendererMixin`
- Water vs glass mask: `ecology:shaders/include/water_mask.glsl` (7-bit alpha + LSB), used by terrain + transparency
- Biome water fog colors: `minecraft:visual/water_fog_color` in ocean biome JSONs
