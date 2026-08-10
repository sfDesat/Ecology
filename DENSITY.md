# Ecology density / terrain offset

Ecology adjusts overworld terrain height in ocean regions so shelf habitat
continentalness bands match actual seafloor depth.

## Owned component: `ecology:ocean_depth_control`

File: [`src/main/resources/data/ecology/worldgen/density_function/ocean_depth_control.json`](src/main/resources/data/ecology/worldgen/density_function/ocean_depth_control.json)

Standard spline on `minecraft:overworld/continents`. Negative offset deepens
the seafloor. Tunable without touching the large vanilla graph.

| Continents (approx) | Offset | Intent |
|---------------------|--------|--------|
| ≤ -0.8 … -0.6 | deep negative | deep basins |
| -0.48 → -0.19 | -0.38 → 0.0 | shallow shelf ramp |
| ≥ -0.19 | 0 | no extra offset on land/shore |

Shelf bands must stay aligned with [`HabitatPocketTable`](src/main/java/com/midas/ecology/worldgen/climate/HabitatPocketTable.java):

```
SHORE      [-0.22, -0.19]     ~0–10 blocks below sea level
INNER      [-0.28, -0.22]     ~8–15
MID        [-0.34, -0.28]     ~15–25
OUTER      [-0.40, -0.34]     ~25–35
SHELF EDGE [-0.48, -0.40]     ~35–45  (SHELF_EDGE = -0.48; vanilla was -0.455)
DEEP BASIN < -0.48            deep pelagic columns
```

Continentalness is more negative farther from land. Ecology’s shelf is a slight
nudge past vanilla (`-0.48` vs `-0.455`) so non-deep oceans run a bit farther
offshore before Deep Basin begins.

## Glue: `minecraft:overworld/offset`

File: [`src/main/resources/data/minecraft/worldgen/density_function/overworld/offset.json`](src/main/resources/data/minecraft/worldgen/density_function/overworld/offset.json)

```
overworld_offset_base + ecology:ocean_depth_control
```

## Vendored fork: `ecology:overworld/overworld_offset_base`

File: [`src/main/resources/data/ecology/worldgen/density_function/overworld/overworld_offset_base.json`](src/main/resources/data/ecology/worldgen/density_function/overworld/overworld_offset_base.json)

This is a **vendored copy** of vanilla’s nested continent × erosion × ridges
offset graph, kept so Ecology can add `ocean_depth_control` without editing
Mojang’s live `offset` internals mid-graph.

### On Minecraft version bumps

1. Diff vanilla `worldgen/density_function/overworld/offset.json` (and any
   nested files it references) against this fork.
2. Re-apply only if vanilla changed the base offset tree.
3. Prefer leaving `ocean_depth_control` alone unless shelf depths need retuning.
4. Avoid hand-editing the huge base JSON unless a vanilla merge requires it.

Java band constants in `HabitatPocketTable` are the source of truth for shelf
slices; keep the spline knot locations in `ocean_depth_control` consistent with
those comments when changing either side.
