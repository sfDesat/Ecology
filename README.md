# Ecology

Fabric mod for Minecraft **26.2** focused on **ocean worldgen**: habitat pocket biomes on shallow shelves, Open/Deep pelagic stacking in basins, custom seafloor features, and continent-driven shelf depths.

Mob ecology and new creatures are planned later; this repo currently ships the ocean climate and terrain foundation.

## Setup

- JDK **25**
- Import the project in your IDE and run the `genSources` / `runClient` Gradle tasks

## Dev dependencies

Mod Menu and Cloth Config are included as **local runtime** deps for testing config screens later. They are not hard requirements of the mod.

## Client: distant water

Deep oceans show a pale **basin outline** when looking from above: water with nothing behind it shows air fog/sky, while the seafloor does not. See [`WATER_FOG_OUTLINE.md`](WATER_FOG_OUTLINE.md).

Ecology offers two **mutually exclusive** fixes (`core/terrain` + Fog UBO extension + Fabulous `post/transparency` for Fog tint):

| Mode | UI name | Behavior |
|------|---------|----------|
| `OFF` | Off (vanilla) | Vanilla water |
| `OPACITY` | Opaque water | Raise marked water-face alpha with distance + optional fresnel |
| `FOG_REMAP` | Fog tint | Replace pale fog/sky *behind water only* with water fog (Fabulous); white fog on top of water unchanged |

### Config UI

- **Mod Menu → Ecology → Config** (requires Mod Menu + Cloth Config; both are on the `runClient` classpath)
- JSON fallback: `config/ecology-client.json`

| Key | Default | Meaning |
|-----|---------|---------|
| `distantWaterMode` | `FOG_REMAP` | `OFF` / `OPACITY` / `FOG_REMAP` |
| `fogRemapBiasStrength` | `1.0` | Fog tint: strength of behind-water fog/sky replacement at distance |
| `fogTintDarkness` | `0.55` | Fog tint: darkens water fog used *behind* water (`0` = biome color, `1` = black) |
| `distanceOpacityEnabled` | `true` | OPACITY: distance-based opacity (off = fresnel-only) |
| `distantWaterOpacityStrength` | `1.0` | OPACITY: opacity at End distance (`1.0` = fully opaque) |
| `distantWaterOpacityStart` | `0.0` | OPACITY: fraction of render-distance fog where boost begins |
| `distantWaterOpacityEnd` | `0.5` | OPACITY: fraction where opacity reaches full strength (must be ≥ Start) |
| `fresnelEnabled` | `true` | OPACITY: glancing-angle opacity |
| `fresnelStrength` | `1.0` | OPACITY: how much angle opacity to add (combined with distance, capped at 1) |
| `fresnelPower` | `0.75` | OPACITY: `pow(grazing, power)` — lower spreads more, higher = horizon-only |
| `irisAutoDisable` | `true` | Turn off Ecology distant-water effects while an Iris pack is active |
| `debugLogging` | `false` | Log diagnostics; print status to chat when config applies |
| `debugHighlightFresnel` | `false` | Paint marked water green→red by view angle |

- Legacy configs with `waterShaderEnabled: true` (no mode) migrate to `OPACITY`
- Effects apply only when the camera is **above water** (underwater fog stays vanilla)
- Runtime settings overlay: `config/ecology/distant_water_pack/` (written only when settings change)
- **Sodium** may ignore vanilla `core/terrain` overrides; Iris packs auto-disable unless `irisAutoDisable` is off
- Check the log for `[Ecology WaterSurface]` lines if the effect is missing (enable `debugLogging`)

## Design notes

- [`WATER_FOG_OUTLINE.md`](WATER_FOG_OUTLINE.md) — pale basin outline problem and recommended fix
- [`BIOMES.md`](BIOMES.md) — habitat list and depth targets
- [`DENSITY.md`](DENSITY.md) — ocean depth spline and vendored offset fork
