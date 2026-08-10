# Ecology

Fabric mod for Minecraft **26.2** focused on **ocean worldgen**: habitat pocket biomes on shallow shelves, Open/Deep pelagic stacking in basins, custom seafloor features, and continent-driven shelf depths.

Mob ecology and new creatures are planned later; this repo currently ships the ocean climate and terrain foundation.

## Setup

- JDK **25**
- Import the project in your IDE and run the `genSources` / `runClient` Gradle tasks

## Dev dependencies

Mod Menu and Cloth Config are included as **local runtime** deps for testing config screens later. They are not hard requirements of the mod.

## Design notes

- [`BIOMES.md`](BIOMES.md) — habitat list and depth targets
- [`DENSITY.md`](DENSITY.md) — ocean depth spline and vendored offset fork
