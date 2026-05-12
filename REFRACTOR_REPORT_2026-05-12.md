# Refactor Report (2026-05-12)

## Scope
This pass focused on stabilizing and cleaning the dungeon reward pipeline and shop purchase flow without changing intended gameplay loops.

## Refactors Completed

### 1) Dungeon gear generation cleanup and scaling
**File:** `src/main/java/com/revilo/gatesofavarice/dungeon/DungeonGearRoller.java`

- Added overloaded API:
  - `rollAndBind(ItemStack, RandomSource)` (backward-compatible entry point)
  - `rollAndBind(ItemStack, RandomSource, int playerLevel, long timeInDungeonTicks)` (new contextual roll path)
- Reworked internals so dungeon gear can now roll:
  - flat `Armor Points` bonus on armor
  - flat `Weapon Damage` bonus on weapons
- Added deterministic modifier IDs for these stat bonuses so rebinding/rerolling does not stack duplicate modifiers.
- Extracted scale tuning constants (`LEVEL_WEIGHT`, `TIME_WEIGHT`, etc.) to make balancing explicit and easier to adjust.
- Consolidated lore + modifier cleanup into `clearOldAffixesAndBonuses` to remove stale generated data safely.

### 2) Dungeon run state cleanup
**File:** `src/main/java/com/revilo/gatesofavarice/dungeon/DungeonRunManager.java`

- Added run start timestamp tracking:
  - `RunState.runStartGameTime`
  - initialized once when entering dungeon run
- Added helper methods to reduce duplication and improve readability:
  - `getRunForPlayer(Player)`
  - `getEffectivePlayerLevel(ServerPlayer)`
  - `getDungeonLevel(RunState)`
- Updated loot grant flow to pass contextual scaling data (player level + elapsed dungeon time) into gear roller.
- Replaced brittle item identity checks for armor with slot-driven equip logic via `ArmorItem#getEquipmentSlot()`.
  - This ensures armor upgrades always overwrite the currently equipped piece in that slot.
- Added `rollAndBindForActiveRun(...)` as a single shared API for all active-run reward sources.

### 3) Shop purchase flow deduplication
**File:** `src/main/java/com/revilo/gatesofavarice/menu/ShopkeeperMenu.java`

- Removed duplicated reward-grant logic from:
  - single purchase path
  - buy-all path
- Introduced shared helper:
  - `grantPurchasedReward(ServerPlayer, ShopOfferDefinition)`
- Routed active dungeon run purchases through `DungeonRunManager.rollAndBindForActiveRun(...)`.
- Removed now-unused `DungeonGearRoller` import from menu class.
- Tightened non-integrated level fallback to `Math.max(1, player.experienceLevel)` for consistency with dungeon run scaling.

## Obsolete/Dead Feature Removal

No gameplay feature was removed in this pass.

Reason: to minimize regression risk, placeholders and unfinished interfaces that appear to be part of future features (e.g. loadout handlers in `DungeonRunManager`) were not deleted yet.

## Validation

- Build validation executed:
  - `./gradlew.bat compileJava`
  - Result: **BUILD SUCCESSFUL**

## Risk Notes

- Dungeon-generated armor/weapon now include flat stat modifiers in addition to existing percent lore affixes.
- Existing non-dungeon item pipelines were intentionally left untouched.
- Placeholder methods still present in parts of the codebase were preserved to avoid accidental feature wiring breakage.

## Recommended Next Cleanup Pass (Safe Order)

1. Implement or remove loadout stubs in `DungeonRunManager` and related UI/menu classes.
2. Split `DungeonRunManager` into focused components:
   - wave lifecycle
   - loot selection/grant
   - participant/session state
3. Add targeted tests for:
   - armor replacement behavior
   - run-time scaling curve boundaries (short vs long runs, low vs high level)
   - shop buy-all reward consistency.
