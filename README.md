# Ecology

Fabric mod for Minecraft **26.2**. Adds ocean habitat pockets, pelagic biome layers, seafloor features, and client water rendering.

New mobs are not in this build. Vanilla ocean fauna is placed by habitat (including larger schools and underwater turtle grazing).

## Requirements

- Minecraft **26.2**, Java **25**, Fabric Loader **0.19.3+**, Fabric API
- License: [CC0-1.0](LICENSE)

**Optional:** Mod Menu + Cloth Config (settings screen), Sodium (shader overlay, temporary for testing). Distant-water **fog tint** needs Fabulous / Improved Transparency. Effects pause while an Iris pack is running (configurable).

## Worldgen

Fourteen **habitat pockets** on shallow shelves, nested in vanilla ocean climate (frozen through warm): Ice Edge, Polynya, Sympagic Zone, Kelp Forest, Cold Eelgrass, Seagrass Meadow, Sand Wave Field, Temperate Rocky Reef, Subtropical Seagrass, Patch Reef, Soft Coral Garden, Coral Reef, Lagoon, Tropical Seagrass.

Basins stack **Open Ocean** over **Deep Basin**. Custom features include reefs, rocks, seafloor fans, icebergs, and patch-reef islands. Shelf depth follows continents.

## Client

Config: `config/ecology-client.json`, or Mod Menu if Cloth Config is installed.

- **Fog tint** / **Opaque** / **Off** for distant water
- Per-biome underwater fog distance and overhead-light falloff

## Setup

Import in an IDE and run `genSources` / `runClient`. Mod Menu, Cloth Config, and Sodium are **local runtime** deps for testing; they are not required to load the published jar.