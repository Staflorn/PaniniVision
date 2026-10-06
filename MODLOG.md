# MODLOG — Panini Projection Mod

## Objective
Build a Fabric mod for Minecraft 26.3 that replaces the default perspective
projection with a Panini Projection using Java 25 toolchain.

## Environment
- Minecraft 26.3 (compliance level 1 — native Mojang names, no obfuscation)
- Fabric Loom 1.18.2
- Fabric Loader 0.19.5, Fabric API 0.161.0+26.3
- Java 25 (Eclipse Temurin 25.0.4.101)
- Gradle 9.7.0

## Decisions

### Build Configuration
- **Groovy DSL** (not Kotlin DSL) — Loom 1.18.2 plugin DSL resolution issues with Kotlin.
- **No separate mappings** — Minecraft 26.3 ships with native Mojang names (compliance level 1).
  The `fabric.loom.disableObfuscation=true` property tells Loom to use the jar as-is.
- **`implementation` instead of `modImplementation`** — `disableObfuscation=true` skips
  `createRemapConfigurations`, so `modImplementation` is not available. Loom's
  `DebofConfiguration` creates resolve configs that extend from standard Gradle configs.
- **`loom.mixin.useLegacyMixinAp=true`** was attempted but conflicts with `disableObfuscation`
  (the `mappingsFinal` config doesn't exist). Instead, an empty refmap `META-INF/panini_projection.refmap.json`
  is provided manually as `{}`.
- **Java 25 toolchain** — set via `JAVA_HOME` to the Eclipse Temurin JDK.

### Projection Implementation
- **Shader-based post-processing** approach (researched from Sodium Extra), using a
  Minecraft `PostChain` with a custom fragment shader. This replaces the matrix-based
  approach which had fundamental issues (asymmetry, UI/hand distortion, FOV >170° problems).
- **Why shader-based**: The matrix approach modifies the projection matrix, which
  distorts everything rendered with that matrix (world, hand, GUI, UI). The shader
  approach renders the world normally, then applies Panini as a post-process effect
  that only affects the world — the hand and GUI render on top, undistorted.
- **PostChain JSON** at `assets/panini_projection/post_effect/panini.json`:
  Two-pass approach — Pass 1 applies the Panini shader to `minecraft:main` → writes
  to intermediate `panini_swap` target; Pass 2 blits `panini_swap` → `minecraft:main`.
- **Fragment shader** at `assets/panini_projection/shaders/post/panini.fsh`:
  Uses the full Panini projection formula (not the simplified matrix approximation).
  Reads FOV/aspect info from the projection matrix via `1/|m00|` (= aspect·tan(FOV/2))
  and `1/|m11|` (= tan(FOV/2)) as source extents. The formula uses the correct
  trigonometric identities with discriminant-based solution of the Panini inverse.
- **Mixin injection**: `@Mixin(GameRenderer.class)` with `@Inject(method="render3dHud", at=@At("HEAD"))`
  — runs AFTER `LevelRenderer.render()` (world rendering) but BEFORE hand projection
  setup and hand rendering. This gives the clean separation: world → Panini shader → hand/GUI.
- **Accessor mixins**: `AccessorPostChain` (`@Accessor("passes")`) and `AccessorPostPass`
  (`@Accessor("customUniforms")`) provide access to PostChain's internal pass list and
  each PostPass's custom uniform buffer map, which is needed to update the PaniniConfig
  uniform at runtime with the current camera/projection state.
  - All 4 mixins applied without errors
  - PostChain + shader compiled without errors
  - Player joined world, config saved to `config\panini_vision\config.json`
  - Clean shutdown, `BUILD SUCCESSFUL`
