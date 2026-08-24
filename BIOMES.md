# Ecology Oceans

Shallow oceans are generic climate bases with deliberately plain fauna. Rare habitat pockets spawn inside them. Open ocean and deep ocean stack vertically in deep-basin columns. Depths are rough guidance — blocks below sea level (Y 63), not hard clamps.

Named animals in biome sections are **aspirational** (future Ecology mobs). Current vanilla stand-ins and counts live in `OCEAN_SPAWNS.md`.

## Overview

**Frozen Ocean** — shipped

- Ice Edge — shipped
- Polynya — shipped
- Sympagic Zone — shipped

**Cold Ocean** — shipped

- Kelp Forest — shipped (urchin-barren split deferred)
- Cold Eelgrass — shipped

**Ocean** (temperate) — shipped

- Seagrass Meadow — shipped
- Temperate Rocky Reef — shipped
- Sand Wave Field — shipped

**Lukewarm Ocean** — shipped

- Subtropical Seagrass — shipped
- Patch Reef — shipped
- Soft Coral Garden — shipped

**Warm Ocean** — shipped

- Coral Reef — shipped
- Lagoon — shipped (sand-first shallows, not a dense meadow)
- Tropical Seagrass — shipped

**Open Ocean** — shipped

**Deep Basin** — shipped (gravel/rock floor, dead fans)

## Biomes



### Frozen



#### Frozen Ocean

Polar base ocean. Temperature: frozen (0.0). Depth: about 0–20 below sea level (Y 63–43).

The coldest shallow ocean — a plain polar shelf meant to feel empty until you find an ice habitat pocket. Pack ice at ~85–90% via `ColumnIceSystem` (applied once per chunk, not biome-filtered — avoids square cutoffs), with occasional clustered water holes, dense ecology icebergs, dark cold water fog (#061828), gravel and rock under ice, ice-keel scrape gravel pits. Plants absent.

Animals (aspirational): Arctic amphipods, brittle stars, ringed seals.

#### Ice Edge

Habitat pocket inside Frozen Ocean, plus an outer-shelf continental fringe next to Deep Basin / Open Ocean. Temperature: frozen (0.0). Depth: about 0–10 below sea level (Y 63–53) for inland weirdness blobs; outer fringe sits on the deep shelf band (~35–45 below sea) with the same surface EDGE ice.

The productive margin where pack ice meets open water — feeding grounds and seal haul-outs. Spawns as weirdness blobs on the frozen habitat shelf and as an intermittent pocket-weirdness fringe on the outer shelf (`[-0.48, -0.40]` continentalness) along the deep-basin boundary. Light ice cover with rare larger ice plates, plus sparse icebergs. Sparse gravel floor and rock mounds, richer salmon than the plain pack.

Animals (aspirational): Krill swarms, ivory gulls, harp seals, beluga whales, orcas.

#### Polynya

Habitat pocket inside Frozen Ocean. Temperature: frozen (0.0). Depth: about 0–15 below sea level (Y 63–48).

A persistent opening in the ice where whales and diving birds gather to breathe and feed. Almost open water with only rare ice floes, colder-clear water columns (#0c2848 fog), gravel or rock floor. Moderate weirdness niche (not shelf-dominating).

Animals (aspirational): Copepods, black guillemots, bowhead whales, narwhals, orcas.

#### Sympagic Zone

Habitat pocket inside Frozen Ocean. Temperature: frozen (0.0). Depth: about 0–5 below sea level (Y 63–58); hugs the ice underside.

The thin living layer under sea ice, driven by ice algae and amphipods that graze it. Same ~85–90% pack ice as Frozen Ocean (`ColumnIceSystem`), darker under-ice fog (#051420), gravel/rock floor with ice-keel scrapes, sparse sea fans and sea pickles as the under-ice epifauna stand-in — no seagrass carpet. Spawns as weirdness blobs on the frozen shelf.

Animals (aspirational): Ice algae, Arctic amphipods, bearded seals at breathing holes.

### Cold



#### Cold Ocean

Cold temperate base ocean. Temperature: cold (~0.2). Depth: about 10–30 below sea level (Y 53–33).

A nutrient-rich green-grey shelf. Light kelp may appear on the base; dense canopy belongs to the Kelp Forest pocket. Rocky and gravel bottom, murky productive water, occasional thin kelp. Water fog cold blue (#0a2840).

Animals (aspirational): Sand shrimp, sea cucumbers, Atlantic or Pacific cod.

#### Kelp Forest

Habitat pocket inside Cold Ocean. Temperature: cold (~0.2). Depth: seafloor about 15–35 below sea level (Y 48–28); canopy can reach near the surface.

Tall giant-kelp canopy over rock and gravel. Dense vertical kelp trunks, green light filtering down, rocky holdfasts. (Urchin-barren depth split is deferred — one canopy biome for now.)

Animals (aspirational): Kelp bass, purple sea urchins, sea otters, leopard sharks.

#### Cold Eelgrass

Habitat pocket inside Cold Ocean. Temperature: cold (~0.2). Depth: about 0–10 below sea level (Y 63–53).

Shallow cold meadow of eelgrass on sand and mud, quieter than the kelp forest. Short, patchy blades on a muddy-sandy floor — distinct from temperate meadow density.

Animals (aspirational): Pacific herring, moon snails, Dungeness crabs, great blue herons at the surface.

### Temperate



#### Ocean

Temperate base ocean (primary focus). Temperature: temperate (0.5). Depth: about 20–45 below sea level (Y 43–18).

The common temperate shelf — productive but deliberately plain so meadows, reefs, and sand waves read as special. Mixed sand, gravel, and clay patches; three-dimensional cobble mounds; sparse seagrass only; no kelp. Water fog slightly green-dark (#0a3038).

Animals (aspirational): Sand dollars, lugworms, flounder or plaice.

#### Seagrass Meadow

Habitat pocket inside Ocean. Temperature: temperate (0.5). Depth: about 0–15 below sea level (Y 63–48).

Dense temperate seagrass beds — nursery habitat and filter-feeder ground. Thick seagrass clumps with sandy blowout patches, soft sand and clay floor, clearer sheltered feel than the plain shelf.

Animals (aspirational): Temperate seahorses, pipefish, bay scallops, gray seals.

#### Temperate Rocky Reef

Habitat pocket inside Ocean. Temperature: temperate (0.5). Depth: about 15–30 below sea level (Y 48–33).

Hard-bottom reef of rock piles, cobble, and gravel — structure for fish and crustaceans. Cobble mounds, stone spires, gravel pockets, sparse seagrass between rocks, sparse fans and sea pickles in crevices.

Animals (aspirational): Cunner, moon jellyfish, Atlantic wolffish, lobsters, diving cormorants.

#### Sand Wave Field

Habitat pocket inside Ocean. Temperature: temperate (0.5). Depth: about 30–45 below sea level (Y 33–18).

Deeper mid-shelf sand ripples and wave-sorted beds with little plant cover. Broad sand disks and low sand mounds / ripples, almost barren of seagrass.

Animals (aspirational): Sand eels (sandlance), thornback rays.

### Lukewarm



#### Lukewarm Ocean

Subtropical base ocean. Temperature: lukewarm. Depth: about 15–40 below sea level (Y 48–23).

Sandy and muddy subtropical flats. Clearer warmer water. Light seagrass on the base; coral only in rare pockets. Pale sand and mud, sparse seagrass, open water.

Animals (aspirational): Hermit crabs, sea pens, bonefish.

#### Subtropical Seagrass

Habitat pocket inside Lukewarm Ocean. Temperature: lukewarm. Depth: about 0–10 below sea level (Y 63–53).

Shallow subtropical grass beds — turtle grazing and seahorse shelter. Medium-density seagrass on pale sand with clearings, warm clear shallows.

Animals (aspirational): Seahorses, pinfish, green sea turtles, dugong.

#### Patch Reef

Habitat pocket inside Lukewarm Ocean. Temperature: lukewarm. Depth: about 10–20 below sea level (Y 53–43).

Scattered hard-coral patches on sand — not a continuous barrier reef. Isolated coral heads, coral rubble, sandy corridors between them. Thinner reef fish than the warm Coral Reef showpiece.

Animals (aspirational): Moray eels in crevices, parrotfish, nurse sharks.

#### Soft Coral Garden

Habitat pocket inside Lukewarm Ocean. Temperature: lukewarm. Depth: about 25–45 below sea level (Y 38–18).

Deeper soft-coral and sea-fan gardens on the outer lukewarm shelf. Soft branching corals and fans over sand and rock (dense clumps plus sparser fans), quieter light than the surface.

Animals (aspirational): Butterflyfish, sea fans, spiny lobsters.

### Warm



#### Warm Ocean

Tropical base ocean. Temperature: warm. Depth: about 15–40 below sea level (Y 48–23).

Clear turquoise carbonate shelf. Kept sparse so reefs and lagoons feel special. No kelp. Bright sand, clear water, almost no plants on the generic floor. Rare coral-encrusted rock outcrops break up the plain shelf.

Animals (aspirational): Sea cucumbers, bristle worms, stingrays resting on sand.

#### Coral Reef

Habitat pocket inside Warm Ocean. Temperature: warm. Depth: about 10–20 below sea level (Y 53–43).

Classic hard coral reef — the tropical showpiece pocket. Dense coral structure plus lighter mound variants, bright fish traffic, clear turquoise water.

Animals (aspirational): Clownfish, angelfish, octopus, blacktip reef sharks.

#### Lagoon

Habitat pocket inside Warm Ocean. Temperature: warm. Depth: about 0–8 below sea level (Y 63–55).

Sheltered very shallow tropical water behind reef or shore — calm, bright, **sand-first**. Glass-clear shallows, open sand bottom, sparse seagrass and rare sea pickles (not a dense meadow). Quiet surface.

Animals (aspirational): Upside-down jellyfish on the bottom, needlefish at the surface, manatees or dugongs.

#### Tropical Seagrass

Habitat pocket inside Warm Ocean. Temperature: warm. Depth: about 0–12 below sea level (Y 63–51).

Warm seagrass flats — medium–dense grass on pale tropical sand, clear water. Turtles graze here and on subtropical seagrass / lagoon.

Animals (aspirational): Blue tangs, queen conchs.

### Pelagic



#### Open Ocean

Upper pelagic layer. Temperature: temperate (0.5). Depth: surface down to about Y 15 (roughly 0–48 below sea level).

The open blue upper water column — no seafloor habitat of its own, just pelagic space. Featureless blue water (`water_color` `#428ad4`), bright near the surface, fading with depth. Spawns in every climate column above Deep Basin, so mixed temperate pelagic fish (including salmon megaschools) are intentional.

Animals (aspirational): Flying fish, sardine and mackerel schools, bioluminescent plankton at night, tuna, common dolphins, occasional whale sharks, transient orcas.

#### Deep Basin

Lower pelagic / deep basin floor (`ecology:deep_basin`). Temperature: temperate (0.5). Depth: from about Y 12–15 down to the seafloor (48+ below sea level into deep basins).

The dark lower water and deep basin floor under open ocean — one shared deep biome instead of climate-split vanilla deeps. Also used as the horizontal MultiNoise marker for deep columns **and the warm outer shelf** before vertical remapping (vanilla has no `deep_warm_ocean`; see Notes). Dark navy fog (#061a28), near-black water (#080c18), dim gravel and rock floor, sparse **dead (gray) coral fans**, full cave / canyon carving.

Animals (aspirational): Grenadiers (rattails), deep-sea hagfish, lanternfish, anglerfish, sixgill sharks, elephant seals as deep-diving visitors.

## Notes

Sea level is Y 63. Listed depths are targets, not hard clamps.

Vanilla `deep_*` oceans are disabled and replaced by `ecology:deep_basin`, with Open Ocean stacked above it in deep-basin columns (and on the outer warm-ocean shelf).

**Warm outer shelf → Deep Basin:** Vanilla has no `deep_warm_ocean`. Warm climate still needs deep columns where continentalness is oceanic. Ecology maps outer warm pocket weirdness (`WARM_OUTER_FILL`) to `ecology:deep_basin` so `PelagicColumnResolver` can stack Open Ocean above it. This is intentional, not a temporary bug.

Cold Rocky Reef was removed. Urchin-barren zoning inside Kelp Forest is **deferred**.

**Continental shelf** (see `HabitatPocketTable` + `DENSITY.md`): MultiNoise **continentalness** is more negative farther from land. Land/shore ≥ about `-0.19`. Ecology shallow oceans / habitat pockets occupy **`[-0.48, -0.19]`** (vanilla shallow stopped at `-0.455` — a modest offshore nudge). Deep Basin is more oceanic than `-0.48`. Within the shelf, nearshore pockets sit closer to `-0.19`; outer-shelf pockets (sand waves, soft coral, kelp mid-band, warm outer fill) use the more negative end. Seafloor depth is driven by `ecology:ocean_depth_control` on the same continent coordinate so bands stay aligned.

**Water color** (`effects.water_color`, not fog) runs cold blue → warm turquoise in tight steps: Sympagic `#343eac` → Frozen `#384cbc` → Ice Edge `#3c56c4` → Polynya `#3e5ecc` → Cold `#406cd0` → Temperate / Open Ocean `#428ad4` → Soft Coral `#4496d8` → Lukewarm `#46a4dc` → Warm `#48b4e0` → Coral `#4ac0e4` → Tropical Seagrass `#4cc8e6` → Lagoon `#50d2ea`. Deep Basin water `#080c18`, water fog `#061a28`.

**Carvers** (caves / ravines). Minecraft can bleed carving a short way across biome borders, so a meadow next to kelp or reef can still show a cut.

| Biome | cave | cave_extra | canyon (ravine) |
|--------|:----:|:----------:|:---------------:|
| Deep Basin | yes | yes | yes |
| Kelp Forest | yes | yes | yes |
| Frozen Ocean | yes | — | yes |
| Temperate Rocky Reef | yes | — | yes |
| Coral Reef | yes | — | yes |
| Soft Coral Garden | yes | — | — |
| Patch Reef | yes | — | — |
| Ocean / Cold Ocean / all seagrass & soft-sediment pockets | — | — | — |

Carving bleeds a short way across biome borders. Soft seagrass shelves keep parents (Ocean, Cold Ocean) carver-free so meadows stay uncut; rocky / reef / deep biomes keep structure carvers.

Legacy `minecraft:deep_*` JSON still lists full carvers but those columns remap to Deep Basin / Open Ocean.

**Amethyst geodes** stay on rocky / hard-bottom and deep biomes. Soft-sediment biomes (seagrasses, sand waves, lagoon, warm/lukewarm bases, soft coral garden) omit them.
