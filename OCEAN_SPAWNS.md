# Ecology Ocean Spawns (Vanilla)

Design targets for vanilla water-related mobs across Ecology ocean biomes. Placement follows real-world habitat of each mob’s counterpart (temperature band, depth, and habitat type). Counts are spawn-group size (`minCount`–`maxCount`). Weight is left for implementation later.

Base oceans stay deliberately plain. Habitat pockets get denser or more specialized schools. Structure-only spawns (guardians) are noted but not listed as biome ambient fauna.

## All vanilla water-related mobs

| Mob | Real-world counterpart | Ocean-relevant? |
|-----|------------------------|-----------------|
| Cod | Atlantic / Pacific cod — cold–temperate shelves, kelp, rocky ground | Yes |
| Salmon | Pacific / Atlantic salmon — cold / subarctic coastal & ice-margin waters | Yes |
| Tropical Fish | Reef-associated tropical fish — coral, lagoons, warm seagrass | Yes |
| Pufferfish | Coastal puffers — reefs, lagoons, warm grass flats | Yes |
| Squid | Coastal / shelf squid — open midwater, not ultra-shallow lagoons or under-ice | Yes |
| Glow Squid | Deep bioluminescent cephalopod stand-in | Yes (deep + underground water) |
| Dolphin | Coastal / oceanic dolphins — temperate–tropical shelves & open blue | Yes |
| Turtle | Green sea turtle — subtropical–tropical seagrass & lagoons (nests on beaches) | Yes |
| Polar Bear | Polar bear — solid pack ice they can walk on | Yes (frozen shelf with ice cover) |
| Drowned | Fantasy undead — wreck / deep-dark water niche, not ubiquitous | Yes (limited biomes) |
| Guardian | Monument defender | Structure only (ocean monuments) |
| Elder Guardian | Monument boss | Structure only (ocean monuments) |
| Axolotl | Freshwater cave amphibian | No — lush caves only |
| Frog | Freshwater wetland amphibian | No — swamps / mangrove only |
| Tadpole | Frog juvenile | No — from frogs, not ocean |

---

## Mobs → biomes

### Cod
Cold–temperate demersal fish. Strongest in kelp and rocky reef; present on temperate sand and cold meadows; absent from true tropical / ice-pack interiors.

- Cold Ocean, Kelp Forest (peak), Cold Eelgrass
- Ocean, Seagrass Meadow, Temperate Rocky Reef (peak), Sand Wave Field (light)
- Open Ocean (light — shelf-edge schools only)
- Not lukewarm–warm, not frozen pockets

### Salmon
Cold / subarctic; fattest at productive ice margins. Not a warm-water fish.

- Frozen Ocean (light), Ice Edge (peak), Polynya, Sympagic Zone (light — under-ice edge)
- Cold Ocean, Kelp Forest, Cold Eelgrass

### Tropical Fish
Warm clear water with structure or grass. Dense on reefs and lagoons; lighter on plain warm shelves.

- Lukewarm Ocean (light), Subtropical Seagrass, Patch Reef (peak), Soft Coral Garden
- Warm Ocean (light), Coral Reef (peak), Lagoon, Tropical Seagrass

### Pufferfish
Coastal warm generalists — reefs, soft coral, lagoons, grass flats. Not open pelagic.

- Patch Reef, Soft Coral Garden, Coral Reef
- Lagoon, Tropical Seagrass, Subtropical Seagrass (light)
- Warm Ocean (light)

### Squid
Open midwater over shelves and basins. Skip habitats that are too shallow, enclosed, or sealed under ice.

- Frozen Ocean, Ice Edge, Polynya (light — cold-water squid)
- Cold Ocean, Kelp Forest, Cold Eelgrass (light)
- Ocean, Seagrass Meadow, Temperate Rocky Reef, Sand Wave Field
- Lukewarm Ocean, Subtropical Seagrass (light), Patch Reef, Soft Coral Garden
- Warm Ocean, Coral Reef, Tropical Seagrass (light)
- Open Ocean (peak), Deep Basin
- Not Sympagic Zone, not Lagoon

### Glow Squid
Deep / dark water stand-in.

- Deep Basin only as an open-ocean presence (peak)
- Elsewhere only as `underground_water_creature` in flooded caves (implementation)

### Dolphin
Temperate–tropical coastal and oceanic. Hunt over shelves, meadows, reef edges, and open blue. Avoid polar ice and the abyssal floor biome.

- Ocean, Seagrass Meadow, Temperate Rocky Reef (light — rocky coasts), Sand Wave Field
- Lukewarm Ocean, Subtropical Seagrass, Patch Reef
- Warm Ocean, Coral Reef, Lagoon (light — bottlenose enter lagoons), Tropical Seagrass
- Open Ocean (peak)
- Not frozen / cold biomes, not Soft Coral Garden (too deep / quiet), not Deep Basin

### Turtle
Green turtles graze seagrass and rest in sheltered warm shallows. Nesting stays on beaches (land). Underwater grazing needs Ecology spawn-placement rules (vanilla turtles are sand-only).

- Subtropical Seagrass, Tropical Seagrass, Lagoon (`water_creature` + shore `creature`)
- Not Coral Reef / Patch Reef as primary
- Not open ocean / deep basin

### Polar Bear
Need solid pack ice underfoot — not open-water ice-edge / polynya habitats where they cannot walk.

- Frozen Ocean (peak), Sympagic Zone
- Not Ice Edge, not Polynya

### Drowned
Not a normal coastal fauna. Only Deep Basin as the main niche; two deeper shelves keep a *heavily diluted* rare entry (vanilla land-monster weights so water rolls almost never pick drowned). **Not** in Open Ocean or Soft Coral Garden.

- Deep Basin (main) — singles, weight 5 among land monsters + spawn costs
- Sand Wave Field (rare) — singles, weight 1 + spawn costs
- Kelp Forest (rare) — singles, weight 1 + spawn costs
- Nowhere else

> Implementation note: listing drowned alone in `monster` makes them flood oceans (every water monster roll succeeds). Always dilute with land monsters.

### Guardian / Elder Guardian
- Ocean monuments only (Deep Basin / deep columns) — not natural biome spawn lists

### Axolotl / Frog / Tadpole
- Do not spawn in Ecology oceans

---

## Biomes → mobs (with counts)

Format: `Mob min–max` per spawn attempt group.

### Frozen

#### Frozen Ocean
Polar pack shelf — sparse, ice-adapted only.

- Salmon 1–2
- Squid 1–2
- Polar Bear 1–2
- Glow Squid 2–4 *(underground water only)*

#### Ice Edge
Most productive open-water ice margin — richer fish; no polar bears (too little solid ice to walk).

- Salmon 3–5
- Squid 1–2
- Glow Squid 2–4 *(underground water only)*

#### Polynya
Persistent open water in ice — breathing / feeding hole; no polar bears (open water).

- Salmon 2–4
- Squid 1–2
- Glow Squid 2–4 *(underground water only)*

#### Sympagic Zone
Thin under-ice layer over pack ice — almost no midwater fauna; bears can walk the ice above.

- Salmon 1–2
- Polar Bear 1
- Glow Squid 2–4 *(underground water only)*

### Cold

#### Cold Ocean
Nutrient-rich cold shelf — plain.

- Cod 2–4
- Salmon 1–2
- Squid 1–3
- Glow Squid 2–4 *(underground water only)*

#### Kelp Forest
Classic cold rocky canopy — densest cod; very rare drowned (diluted).

- Cod 4–6
- Salmon 1–3
- Squid 1–2
- Drowned 1 *(rare, diluted)*
- Glow Squid 2–4 *(underground water only)*

#### Cold Eelgrass
Shallow cold nursery meadow — fish, little squid.

- Cod 3–5
- Salmon 1–3
- Squid 1–2
- Glow Squid 2–4 *(underground water only)*

### Temperate

#### Ocean
Plain temperate shelf.

- Cod 2–4
- Squid 1–3
- Dolphin 1–2
- Glow Squid 2–4 *(underground water only)*

#### Seagrass Meadow
Temperate nursery — denser fish, coastal dolphins.

- Cod 3–6
- Squid 1–2
- Dolphin 1–2
- Glow Squid 2–4 *(underground water only)*

#### Temperate Rocky Reef
Hard bottom — demersal fish; light coastal dolphins.

- Cod 4–6
- Squid 1–2
- Dolphin 1
- Glow Squid 2–4 *(underground water only)*

#### Sand Wave Field
Deeper mid-shelf sand — thinner schools; very rare drowned (diluted).

- Cod 2–3
- Squid 1–3
- Dolphin 1–2
- Drowned 1 *(rare, diluted)*
- Glow Squid 2–4 *(underground water only)*

### Lukewarm

#### Lukewarm Ocean
Subtropical flats — no cold-water cod.

- Tropical Fish 3–6
- Squid 1–3
- Dolphin 1–2
- Glow Squid 2–4 *(underground water only)*

#### Subtropical Seagrass
Green-turtle grazing beds.

- Tropical Fish 5–8
- Pufferfish 1–2
- Squid 1–2
- Dolphin 1–2
- Turtle 2–5
- Glow Squid 2–4 *(underground water only)*

#### Patch Reef
Scattered coral heads — reef fish + puffers; dolphins at edges.

- Tropical Fish 8–8
- Pufferfish 1–3
- Squid 1–2
- Dolphin 1–2
- Glow Squid 2–4 *(underground water only)*

#### Soft Coral Garden
Deeper soft-coral / fan habitat — no dolphins; no drowned.

- Tropical Fish 5–8
- Pufferfish 1–2
- Squid 1–3
- Glow Squid 2–4 *(underground water only)*

### Warm

#### Warm Ocean
Plain tropical shelf — light color.

- Tropical Fish 3–6
- Pufferfish 1–2
- Squid 1–3
- Dolphin 1–2
- Glow Squid 2–4 *(underground water only)*

#### Coral Reef
Hard-coral showpiece — densest tropical fish.

- Tropical Fish 8–8
- Pufferfish 1–3
- Squid 1–2
- Dolphin 1–2
- Glow Squid 2–4 *(underground water only)*

#### Lagoon
Sheltered tropical shallows — grass/reef fish, turtles; no squid.

- Tropical Fish 6–8
- Pufferfish 1–3
- Turtle 2–4
- Dolphin 1 *(light)*
- Glow Squid 2–4 *(underground water only)*

#### Tropical Seagrass
Warm grass flats — turtles graze here too.

- Tropical Fish 5–8
- Pufferfish 1–2
- Squid 1–2
- Dolphin 1–2
- Turtle 2–5
- Glow Squid 2–4 *(underground water only)*

### Pelagic

#### Open Ocean
Upper blue water — pelagic squid and dolphins; occasional megaschools (requires Ecology cluster-cap mixin; vanilla caps schools at 8).

- Cod 2–4 *(common)*
- Cod 18–32 *(megaschool)*
- Salmon 14–28 *(megaschool)*
- Squid 2–4
- Dolphin 1–3
- Glow Squid 2–4 *(underground water only)*

#### Deep Basin
Dark lower column / deep floor — drowned niche (diluted like vanilla); glow squid common; no dolphins / reef fish.

- Squid 2–4
- Drowned 1 *(main, diluted + spawn costs)*
- Glow Squid 4–6 *(open water + underground)*
- Guardians / Elder Guardians via monuments only

---

## Notes

- Counts and biome assignments are implemented in the ocean biome JSON `spawners` blocks.
- **Drowned only in:** Deep Basin (main), plus rare singles in Sand Wave Field and Kelp Forest. Always diluted with land monsters + `spawn_costs` — never drowned-only `monster` lists.
- Glow Squid feel common only in Deep Basin open water; elsewhere underground-water only.
- Turtles graze Subtropical Seagrass, Tropical Seagrass, and Lagoon; beach nesting stays vanilla land behavior.
- Cod stays out of lukewarm–warm (outside real range); salmon stays frozen–cold only.
- Open Ocean megaschools: Cod 18–32 / Salmon 14–28 (weights 3 and 2 vs common Cod weight 8). Requires Ecology cluster-cap mixin — vanilla hard-caps schools at 8.
- Turtles need a spawn-placement mixin for underwater grazing; biome JSON alone cannot place them in water (vanilla is sand / on-ground). They are listed under both `creature` (shore sand) and `water_creature` (meadows / lagoon).
- When custom Ecology mobs land later, replace or thin these vanilla stand-ins per `BIOMES.md` animal lists.
