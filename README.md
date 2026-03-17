# Codex Skills + LevelUP Integration

## Progression Backbone
Skills now uses LevelUP (`com.revilo.levelup`) as the progression source of truth for skill point rewards and level-gated upgrades.

Hook location:
- `src/main/java/net/revilodev/codex/skills/logic/LevelUpSkillIntegrationEvents.java`
- Event used: `LevelUpLevelChangedEvent.LevelUp`
- Behavior: grants `+1` skill point per level step (`newLevel - oldLevel`), then marks Skills sync dirty.

## XP Award Routing
Skills activities award LevelUP XP on the server via `LevelUpApi.awardXp(...)` in:
- `src/main/java/net/revilodev/codex/skills/logic/SkillLogic.java`

Source IDs used:
- Combat kills: `LevelUpSources.MOB_KILL`
- Utility objectives (block utility actions): `LevelUpSources.OBJECTIVE_COMPLETE`
- Survival mitigation activity: custom source `codex:survival_prevented`

## Level Gates
Skill upgrades are gated using `LevelUpApi.meetsLevelRequirement(player, requiredLevel)`:
- Server-side enforcement: `SkillLogic.tryUpgrade(...)`
- Client-side button state mirrors gate checks for consistent UX.

Current requirement rule:
- Next skill rank requires matching LevelUP level (`requiredLevel = currentSkillLevel + 1`).
