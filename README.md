# Ecology

Fabric mod for Minecraft **26.2** focused on **ocean worldgen**: habitat pocket biomes on shallow shelves, Open/Deep pelagic stacking in basins, custom seafloor features, and continent-driven shelf depths.

Mob ecology and new creatures are planned later; this repo currently ships the ocean climate and terrain foundation.

## Setup

- JDK **25**
- Import the project in your IDE and run the `genSources` / `runClient` Gradle tasks

## Dev dependencies

Mod Menu and Cloth Config are included as **local runtime** deps for testing config screens later. They are not hard requirements of the mod.

## Client: water surface opacity

Ecology softens the white air-fog seafloor outline seen through open ocean from above by raising water **top-face** opacity with distance and optional fresnel (`core/terrain` override).

### Config UI

- **Mod Menu → Ecology → Config** (requires Mod Menu + Cloth Config; both are on the `runClient` classpath)
- JSON fallback: `config/ecology-client.json`

| Key | Default | Meaning |
|-----|---------|---------|
| `waterShaderEnabled` | `true` | Master switch for Ecology water opacity |
| `distanceOpacityEnabled` | `true` | Distance-based opacity (off = fresnel-only) |
| `distantWaterOpacityStrength` | `1.0` | Opacity at End distance (`1.0` = fully opaque) |
| `distantWaterOpacityStart` | `0.0` | Fraction of render-distance fog where boost begins |
| `distantWaterOpacityEnd` | `0.5` | Fraction where opacity reaches full strength (must be ≥ Start) |
| `fresnelEnabled` | `true` | Add glancing-angle opacity on top of distance opacity |
| `fresnelStrength` | `1.0` | How much angle opacity to add (combined with distance, capped at 1) |
| `fresnelPower` | `0.75` | Angle curve `pow(grazing, power)` — lower spreads more, higher = horizon-only |
| `irisAutoDisable` | `true` | Turn off Ecology water opacity while an Iris pack is active |
| `debugLogging` | `false` | Log diagnostics; print status to chat when config applies |
| `debugHighlightFresnel` | `false` | Paint water tops green→red by view angle |

- Effect applies only when the camera is **above water** (looking up from underwater stays transparent)
- Runtime settings overlay: `config/ecology/distant_water_pack/` (written only when settings change)
- **Sodium** may ignore vanilla `core/terrain` overrides; Iris packs auto-disable Ecology opacity unless `irisAutoDisable` is off
- Check the log for `[Ecology WaterSurface]` lines if the effect is missing (enable `debugLogging`)

## Design notes

- [`BIOMES.md`](BIOMES.md) — habitat list and depth targets
- [`DENSITY.md`](DENSITY.md) — ocean depth spline and vendored offset fork
