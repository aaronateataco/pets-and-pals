# Phase 0 Research — PetsMod Redesign

*Written 2026-07-10, before any Phase 1 code. Sources: local repo (branch 26.2), the three
addon repos on GitHub, the Xaero's Minimap jar from the local Prism instance, the
freshly-modded Modrinth page, and the JourneyMap API repo.*

## 1. Local dev environment (ground truth)

- **Repo**: `pets-mod` 0.7.10 on branch `26.2`, MC **26.2**, loader 0.18.4, Fabric API
  0.152.1+26.2, YACL 3.9.4, Mod Menu 20.0.0-beta.2. Entity namespace is **`pets-mod`**
  (hyphenated) — matters for Xaero icon definition paths.
- **Prism Launcher** is a host flatpak (`org.prismlauncher.PrismLauncher`); this dev shell is a
  container, so the instance lives at
  `/run/host/root/home/aaron/.var/app/org.prismlauncher.PrismLauncher/data/PrismLauncher/instances/`.
- **Exactly one instance exists: "Fresh & Smooth 2.9.2" on MC 26.1.2** (192 mods), running
  pets-mod **0.7.9-26.1**, pets-earth 1.0.1, pets-natural 1.1.2, Xaero's Minimap
  26.1.5 + Xaero World Map + XaeroPlus, EMF 3.2.4 + ETF 7.1, Fresh Animations.
- ⚠️ **Version mismatch**: the repo builds for 26.2 but the only test instance is 26.1.2.
  In-game testing of phase work needs either `./gradlew runClient` (Loom dev client) or a
  26.2 Prism instance. **JourneyMap is not installed at all.**

## 2. Addon repos — what a real API must replace

All three depend on `"pets-mod": "*"` in fabric.mod.json. Two integration styles exist:

### Style A — client "more species" addons (cobblepets, pets-earth)
Both mixin into petsmod's client classes, mostly targeting **compiler-generated lambda
names**, which is maximally fragile (any edit to `Central`/`PetsConfigScreen` renumbers the
lambdas and silently breaks addons):

| Target | Methods hooked | Purpose |
|---|---|---|
| `Central` | `lambda$createPetSpeciesCommand$1` (cancellable) | claim `/petspecies <name>` for addon species |
| `Central` | `summonPet`, `despawnPet`, `refreshPetNames` (HEAD) | pre-construct *every* addon pet instance (~48 for pets-earth), summon/despawn the active one |
| `Central` | `lambda$createPetNameCommand$1`, `updateSuggestions` (real method) | per-species name storage, command tab-complete |
| `PetsConfigScreen` | `lambda$getModConfigScreenFactory$4/$6/$7/$8` (cancellable) | inject species into the species enum dropdown, name get/set, skin selector |

Shared helpers they call: `Utils.setActivePet / summonPet / despawnEntity / checkName`,
`Central.CONFIG`, `Central.summonPet()/despawnPet()`. Names are stored as **one config field
per species** (`EARTH_CONFIG.fleckedSheepName`, …) and dispatched through giant
string-switch chains keyed on `CONFIG.activePet`.

- **pets-earth**: 48 species, all reusing petsmod's existing pet classes
  (`MinecraftEarthSheep extends` petsmod mobs, plus `ClientChicken`, `ClientWolf`, etc.
  constructed with pets-earth's own registered `EntityType`s). Pure retexture addon.
- **cobblepets**: same mixin pattern (split across `Gen1/Gen4/Gen5CentralMixin` for size),
  but renders with **GeckoLib** (`GeoEntityRenderer`/`GeoModel`) for Pokémon models — the
  API's renderer registration must not assume vanilla `EntityRenderer` subclasses only.

### Style B — "pets as world mobs" (pets-natural)
No mixins into petsmod at all. It consumes the **registered `EntityType`s in
`PetsInitializer`** (`DUCK`, `PENGUIN`, koi, stingray, dumbo octopus…) and makes them real
server-side world mobs: `BiomeModifications.addSpawn`, `SpawnPlacements.register`, spawn-egg
items. **Implication: `PetsInitializer`'s public static EntityType fields are already
cross-mod API surface — renaming/re-namespacing them is a breaking change for addons.**

### API design implications (Phase 4)
1. Replace the lambda mixin surface with a registry: `PetsModApi.registerSpecies(id,
   PetSpecies)` where `PetSpecies` carries factory, display name, skin/variant list,
   category (cute/intense), and an optional renderer hookup on the client — enums for
   species (current design) can't be extended cleanly and are why addons resort to
   cancellable lambda mixins.
2. Per-pet data (name, skins, tame date, variants) must move from
   one-config-field-per-species to a keyed map, or addon species can never be first-class.
3. Keep `PetsInitializer`'s entity types registered and public for pets-natural-style
   consumers; additive changes only.
4. Compat shims: old `Central.summonPet`/`despawnPet`/`refreshPetNames` entry points should
   keep existing (even if they delegate to the new system) so current addon jars degrade
   gracefully rather than crashing.

## 3. Xaero's Minimap (Phase 2) — confirmed from the 26.1.5 jar

- **Radar icons are data-driven and moddable without any Java dependency.** The jar ships a
  fully commented example at
  `assets/xaerominimap/entity/icon/definition/example_mod/example_entity.json`. Convention:
  `assets/xaerominimap/entity/icon/definition/<entity_namespace>/<entity_path>.json` —
  petsmod can ship these **inside its own jar** (works like a built-in resource pack), no
  resource pack download needed. Vanilla examples included (axolotl, cat, horse, …) to copy
  `modelRootPath` configs from.
- Icon types per variant: `model` / `model:N` (auto-rendered from the entity model, with
  `modelConfigs` controlling scale/rotation/parts), `normal_sprite:x.png` /
  `outlined_sprite:x.png` (64×64 pngs under `assets/xaerominimap/entity/icon/sprite/`),
  `item:<id>`, `dot`.
- Variant keys default to the entity's **main texture path** (`BuiltInRadarIconDefinitions.
  getVariant`) — pet skin variants get distinct radar icons for free. Debug option
  `debugEntityVariantIds` prints variant IDs to chat.
- Since pets are petsmod-registered `EntityType`s, the model-based default renderer will
  already *attempt* icons; shipped definition JSONs make them correct.
- **No public plugin API** (no `api` package in the jar). Per-entity radar *hiding* has no
  supported hook: the `xaerominimap:no_entity_radar` effects disable the whole radar for the
  *player*, not one entity. Options for the "hide pets from radar" toggle, best-first:
  1. Ship radar icon definitions with `"default": "dot"`… still shows a dot — not hiding.
  2. Conditional mixin (only when `xaerominimap` is loaded) filtering `AbstractPet`
     instances out of `xaero.hud.minimap.radar.state.RadarStateUpdater`'s entity collection.
  3. Document the built-in user path: Xaero's entity radar category GUI supports per-entity-
     type exclude lists (`EntityRadarCategory` + `ObjectCategoryExcludeList`, user-editable).
- XaeroPlus is also installed; it layers on Xaero's and shouldn't need separate work.

## 4. JourneyMap (Phase 2)

- Has a real plugin API (`@JourneyMapPlugin`, journeymap-api on GitHub, MultiLoader) but it
  covers **waypoints/overlays/markers only — no entity radar icon or entity-hide API**.
  Latest API release targets MC 1.21.1; no 26.x artifact confirmed.
- Not installed in the user's instance.
- **Decision: Xaero's integration first and fully; JourneyMap is best-effort/deferred until
  a 26.x JourneyMap exists to test against.** Its radar groups mobs by category and pets
  will just appear as generic mobs there; revisit when testable.

## 5. freshly-modded / resource-pack conventions (Phase 4 fallback design)

- **freshly-modded (F.M.R.P)** is a Fresh-Animations-style pack for *modded* entities
  (Guard Villagers, **Friends and Foes**, It Takes A Pillage, Frostiful, Savage & Ravage,
  Villager Recruits/Workers, More Mobs Variants, Ravage & Cabbage, Pet Armor). It requires
  **EMF + ETF** (both already in the user's instance) and keys its models/textures off the
  **source mod's entity type**.
- **Design implication for the generic "any mod's entity as pet" wrapper**: if the wrapper
  spawns a client-side entity of the *actual source `EntityType`* (with petsmod driving its
  AI/goals), then EMF/ETF packs (freshly-modded, Fresh Animations) and the source mod's own
  renderer/animations apply automatically — zero per-mob work. Registering petsmod-clone
  EntityTypes instead would break every such pack. Prefer wrapping the real type.
- Existing petsmod texture pack convention (`CREATING A TEXTUREPACK.md`): custom-entity
  textures at `assets/pets-mod/textures/entity/<entity>/<skin>.png`; vanilla-copy pets use
  vanilla texture paths, so vanilla-targeting packs already reskin them. Extend this same
  path convention for user-created pet folders/packs rather than inventing a new format.

## 6. Root-cause notes carried into Phase 1

- `AbstractPet.wander()` sets `setDeltaMovement()` manually from hardcoded distance
  thresholds; no `PathNavigation`/`Goal` use — the confirmed root cause of floating/stair/
  teleport/animation-desync bugs.
- `AbstractPet` overrides `getAddEntityPacket`/`onSyncedDataUpdated` to stay client-only —
  intentional; Phase 5 networking is a new subsystem, not a patch to this.
- `Central` (~2250 lines) owns spawn/despawn/commands; `updateSuggestions` is one of the few
  real (non-lambda) client extension points addons touch.

## 7. Decisions confirmed with the user (2026-07-10)

1. **Phase 6 version targets** (user's words: "basically any major versions from 1.16.5
   onwards … all versions after 1.21.11 definitely needed"):
   - **Must-have**: every version after 1.21.11 — i.e. 1.21.11+, 26.1.x, 26.2 (and future
     26.x as they land).
   - **Stretch**: one build per major version back to 1.16.5 — 1.16.5, 1.18.x, 1.19.x,
     1.20.x, 1.21.x. Port newest-first; the pre-1.21 line has large rendering/mappings
     differences, so treat each as its own build+test pass per TASKS.md.
   - User is happy for additional Prism instances to be created per version for testing.
2. **Day-to-day test loop** (user said "pick"): **Loom `./gradlew runClient`** for fast
   iteration during a phase; at each phase's end, drop the built jar into a matching Prism
   instance (creating a 26.2 instance with Xaero's/EMF/ETF as needed) for the real-modpack
   manual test before sign-off.
