package com.revilo.gatewayexpansion.gateway.pool;

import com.revilo.gatewayexpansion.GatewayExpansion;
import com.revilo.gatewayexpansion.integration.ModCompat;
import com.revilo.gatewayexpansion.item.data.CrystalTheme;
import java.util.List;
import net.minecraft.world.entity.EntityType;

public final class EnemyPoolRegistry {

    private static final String[] BOSS_MOD_IDS = {"bosses_of_mass_destruction", "bossesrise", "bosses_rise"};
    private static final String[] CREEPER_IDS = {"creeperoverhaul"};
    private static final String[] DEEPER_DARKER_IDS = {"deeperdarker"};
    private static final String[] LUMINOUS_IDS = {"luminous_monsters"};
    private static final String[] VARIANTS_IDS = {"variantsandventures", "variants_and_ventures"};
    private static final String[] VILLAGERS_IDS = {"villagersandpillagers"};

    private EnemyPoolRegistry() {
    }

    public static EnemyPoolSet create(CrystalTheme theme, int level) {
        EnemyPoolSet pools = new EnemyPoolSet();
        addVanilla(theme, level, pools);
        addBossCompat(theme, level, pools);
        addCreeperCompat(theme, pools);
        addDeeperDarkerCompat(theme, pools);
        addLuminousCompat(theme, pools);
        addVariantsCompat(theme, pools);
        addVillagersCompat(theme, pools);
        GatewayExpansion.LOGGER.debug("Enemy pools for {} -> melee={}, ranged={}, elite={}, boss={}", theme, pools.pool(EnemyPoolRole.MELEE).size(), pools.pool(EnemyPoolRole.RANGED).size(), pools.pool(EnemyPoolRole.ELITE).size(), pools.pool(EnemyPoolRole.BOSS).size());
        return pools;
    }

    private static void addVanilla(CrystalTheme theme, int level, EnemyPoolSet pools) {
        switch (theme) {
            case UNDEAD -> {
                pools.pool(EnemyPoolRole.MELEE).add(EntityType.ZOMBIE, 8, "vanilla").add(EntityType.HUSK, 4, "vanilla").add(EntityType.DROWNED, level >= 18 ? 5 : 2, "vanilla").add(EntityType.ZOMBIE_VILLAGER, level >= 20 ? 4 : 1, "vanilla");
                pools.pool(EnemyPoolRole.RANGED).add(EntityType.SKELETON, 8, "vanilla").add(EntityType.STRAY, 4, "vanilla").add(EntityType.BOGGED, 4, "vanilla").add(EntityType.PHANTOM, level >= 24 ? 3 : 0, "vanilla");
                pools.pool(EnemyPoolRole.TANK).add(EntityType.ZOMBIE, 5, "vanilla").add(EntityType.WITHER_SKELETON, 2, "vanilla").add(EntityType.ZOGLIN, level >= 65 ? 2 : 0, "vanilla");
                pools.pool(EnemyPoolRole.FAST).add(EntityType.HUSK, 4, "vanilla").add(EntityType.DROWNED, 3, "vanilla").add(EntityType.PHANTOM, level >= 24 ? 4 : 0, "vanilla");
                pools.pool(EnemyPoolRole.ELITE).add(EntityType.WITHER_SKELETON, 5, "vanilla").add(EntityType.WITCH, 4, "vanilla").add(EntityType.STRAY, 3, "vanilla").add(EntityType.BREEZE, level >= 55 ? 2 : 0, "vanilla");
                pools.pool(EnemyPoolRole.SUPPORT).add(EntityType.WITCH, 8, "vanilla").add(EntityType.BOGGED, 3, "vanilla").add(EntityType.PHANTOM, level >= 40 ? 2 : 0, "vanilla");
                pools.pool(EnemyPoolRole.THEME).add(EntityType.ZOMBIE, 6, "vanilla").add(EntityType.SKELETON, 6, "vanilla").add(EntityType.ZOMBIE_VILLAGER, level >= 20 ? 4 : 0, "vanilla");
            }
            case BEAST -> {
                pools.pool(EnemyPoolRole.MELEE).add(EntityType.SPIDER, 7, "vanilla").add(EntityType.WOLF, 5, "vanilla").add(EntityType.POLAR_BEAR, 3, "vanilla").add(EntityType.SLIME, level >= 18 ? 4 : 1, "vanilla");
                pools.pool(EnemyPoolRole.RANGED).add(EntityType.BEE, 6, "vanilla").add(EntityType.BREEZE, level >= 45 ? 3 : 0, "vanilla");
                pools.pool(EnemyPoolRole.TANK).add(EntityType.POLAR_BEAR, 6, "vanilla").add(EntityType.RAVAGER, 3, "vanilla").add(EntityType.SLIME, level >= 30 ? 4 : 0, "vanilla");
                pools.pool(EnemyPoolRole.FAST).add(EntityType.CAVE_SPIDER, 7, "vanilla").add(EntityType.WOLF, 6, "vanilla").add(EntityType.ENDERMITE, level >= 36 ? 3 : 0, "vanilla");
                pools.pool(EnemyPoolRole.ELITE).add(EntityType.RAVAGER, 6, "vanilla").add(EntityType.POLAR_BEAR, 4, "vanilla").add(EntityType.BREEZE, level >= 45 ? 2 : 0, "vanilla");
                pools.pool(EnemyPoolRole.SUPPORT).add(EntityType.BEE, 6, "vanilla").add(EntityType.CAVE_SPIDER, 4, "vanilla").add(EntityType.SLIME, level >= 18 ? 3 : 0, "vanilla");
                pools.pool(EnemyPoolRole.THEME).add(EntityType.SPIDER, 7, "vanilla").add(EntityType.WOLF, 4, "vanilla").add(EntityType.SLIME, level >= 18 ? 3 : 0, "vanilla");
            }
            case ARCANE -> {
                pools.pool(EnemyPoolRole.MELEE).add(EntityType.ENDERMAN, 6, "vanilla").add(EntityType.VEX, 6, "vanilla").add(EntityType.SHULKER, level >= 60 ? 2 : 0, "vanilla");
                pools.pool(EnemyPoolRole.RANGED).add(EntityType.PILLAGER, 8, "vanilla").add(EntityType.BLAZE, 5, "vanilla").add(EntityType.WITCH, 4, "vanilla").add(EntityType.BREEZE, level >= 25 ? 4 : 0, "vanilla");
                pools.pool(EnemyPoolRole.TANK).add(EntityType.ENDERMAN, 5, "vanilla").add(EntityType.SHULKER, level >= 60 ? 3 : 0, "vanilla");
                pools.pool(EnemyPoolRole.FAST).add(EntityType.VEX, 8, "vanilla").add(EntityType.ENDERMITE, 4, "vanilla").add(EntityType.BREEZE, level >= 25 ? 4 : 0, "vanilla");
                pools.pool(EnemyPoolRole.ELITE).add(EntityType.EVOKER, 6, "vanilla").add(EntityType.ENDERMAN, 5, "vanilla").add(EntityType.ILLUSIONER, 3, "vanilla").add(EntityType.SHULKER, level >= 60 ? 2 : 0, "vanilla");
                pools.pool(EnemyPoolRole.SUPPORT).add(EntityType.WITCH, 6, "vanilla").add(EntityType.EVOKER, 4, "vanilla").add(EntityType.BREEZE, level >= 30 ? 2 : 0, "vanilla");
                pools.pool(EnemyPoolRole.THEME).add(EntityType.VEX, 7, "vanilla").add(EntityType.ENDERMAN, 6, "vanilla").add(EntityType.BREEZE, level >= 25 ? 3 : 0, "vanilla");
            }
            case NETHER -> {
                pools.pool(EnemyPoolRole.MELEE).add(EntityType.ZOMBIFIED_PIGLIN, 7, "vanilla").add(EntityType.HOGLIN, 6, "vanilla").add(EntityType.ZOGLIN, level >= 50 ? 3 : 0, "vanilla");
                pools.pool(EnemyPoolRole.RANGED).add(EntityType.BLAZE, 8, "vanilla").add(EntityType.GHAST, 3, "vanilla").add(EntityType.PIGLIN, level >= 20 ? 4 : 0, "vanilla");
                pools.pool(EnemyPoolRole.TANK).add(EntityType.HOGLIN, 6, "vanilla").add(EntityType.MAGMA_CUBE, 5, "vanilla").add(EntityType.ZOGLIN, level >= 50 ? 4 : 0, "vanilla");
                pools.pool(EnemyPoolRole.FAST).add(EntityType.MAGMA_CUBE, 7, "vanilla").add(EntityType.PIGLIN, level >= 20 ? 3 : 0, "vanilla");
                pools.pool(EnemyPoolRole.ELITE).add(EntityType.PIGLIN_BRUTE, 6, "vanilla").add(EntityType.WITHER_SKELETON, 5, "vanilla").add(EntityType.BLAZE, 4, "vanilla").add(EntityType.ZOGLIN, level >= 50 ? 2 : 0, "vanilla");
                pools.pool(EnemyPoolRole.SUPPORT).add(EntityType.BLAZE, 7, "vanilla").add(EntityType.PIGLIN, 4, "vanilla").add(EntityType.MAGMA_CUBE, level >= 30 ? 3 : 0, "vanilla");
                pools.pool(EnemyPoolRole.THEME).add(EntityType.BLAZE, 6, "vanilla").add(EntityType.HOGLIN, 5, "vanilla").add(EntityType.ZOGLIN, level >= 50 ? 2 : 0, "vanilla");
            }
        }
    }

    private static void addBossCompat(CrystalTheme theme, int level, EnemyPoolSet pools) {
        ModCompat.debugDetected("Boss mod", BOSS_MOD_IDS);
        if (level < 35 || !ModCompat.isAnyLoaded(BOSS_MOD_IDS)) {
            return;
        }

        EntityType<?> obsidilith = ModCompat.findFirstEntity("bosses_of_mass_destruction:obsidilith");
        EntityType<?> gauntlet = ModCompat.findFirstEntity("bosses_of_mass_destruction:gauntlet");
        EntityType<?> voidBlossom = ModCompat.findFirstEntity("bosses_of_mass_destruction:void_blossom");
        EntityType<?> lich = ModCompat.findFirstEntity("bosses_of_mass_destruction:lich");
        if (theme == CrystalTheme.NETHER) {
            pools.pool(EnemyPoolRole.BOSS).add(gauntlet, 4, "bosses_of_mass_destruction");
        }
        if (theme == CrystalTheme.ARCANE) {
            pools.pool(EnemyPoolRole.BOSS).add(voidBlossom, 3, "bosses_of_mass_destruction").add(lich, 3, "bosses_of_mass_destruction");
        }
        if (theme == CrystalTheme.UNDEAD || theme == CrystalTheme.ARCANE) {
            pools.pool(EnemyPoolRole.BOSS).add(lich, 4, "bosses_of_mass_destruction");
        }
        pools.pool(EnemyPoolRole.BOSS).add(obsidilith, theme == CrystalTheme.NETHER ? 5 : 2, "bosses_of_mass_destruction");

        for (EntityType<?> type : ModCompat.hostileEntitiesForNamespaces(BOSS_MOD_IDS)) {
            pools.pool(EnemyPoolRole.BOSS).add(type, 1, "boss namespace scan");
        }
    }

    private static void addCreeperCompat(CrystalTheme theme, EnemyPoolSet pools) {
        ModCompat.debugDetected("Creeper Overhaul", CREEPER_IDS);
        if (!ModCompat.isAnyLoaded(CREEPER_IDS)) {
            return;
        }
        List<EntityType<?>> creepers = ModCompat.findEntitiesMatching(CREEPER_IDS, "creeper");
        if (creepers.isEmpty()) {
            return;
        }
        if (theme == CrystalTheme.NETHER) {
            pools.pool(EnemyPoolRole.THEME).addAll(creepers, 2, "creeperoverhaul");
            pools.pool(EnemyPoolRole.FAST).addAll(creepers, 1, "creeperoverhaul");
        } else if (theme == CrystalTheme.ARCANE) {
            pools.pool(EnemyPoolRole.THEME).addAll(creepers, 2, "creeperoverhaul");
            pools.pool(EnemyPoolRole.MELEE).addAll(creepers, 1, "creeperoverhaul");
        }
    }

    private static void addDeeperDarkerCompat(CrystalTheme theme, EnemyPoolSet pools) {
        ModCompat.debugDetected("Deeper and Darker", DEEPER_DARKER_IDS);
        if (!ModCompat.isAnyLoaded(DEEPER_DARKER_IDS)) {
            return;
        }
        List<EntityType<?>> darkMobs = ModCompat.hostileEntitiesForNamespaces(DEEPER_DARKER_IDS);
        if (theme == CrystalTheme.ARCANE || theme == CrystalTheme.UNDEAD) {
            pools.pool(EnemyPoolRole.THEME).addAll(darkMobs, 3, "deeperdarker");
            pools.pool(EnemyPoolRole.ELITE).addAll(ModCompat.findEntitiesMatching(DEEPER_DARKER_IDS, "stalker", "shattered", "sculk"), 2, "deeperdarker");
            pools.pool(EnemyPoolRole.SUPPORT).addAll(ModCompat.findEntitiesMatching(DEEPER_DARKER_IDS, "shriek", "worm"), 2, "deeperdarker");
        }
    }

    private static void addLuminousCompat(CrystalTheme theme, EnemyPoolSet pools) {
        ModCompat.debugDetected("Luminous Monsters", LUMINOUS_IDS);
        if (!ModCompat.isAnyLoaded(LUMINOUS_IDS)) {
            return;
        }
        List<EntityType<?>> luminousMobs = ModCompat.hostileEntitiesForNamespaces(LUMINOUS_IDS);
        if (theme == CrystalTheme.ARCANE) {
            pools.pool(EnemyPoolRole.THEME).addAll(luminousMobs, 3, "luminous_monsters");
            pools.pool(EnemyPoolRole.RANGED).addAll(ModCompat.findEntitiesMatching(LUMINOUS_IDS, "mage", "caster", "seer"), 2, "luminous_monsters");
            pools.pool(EnemyPoolRole.SUPPORT).addAll(ModCompat.findEntitiesMatching(LUMINOUS_IDS, "summon", "wisp", "shade"), 2, "luminous_monsters");
        }
    }

    private static void addVariantsCompat(CrystalTheme theme, EnemyPoolSet pools) {
        ModCompat.debugDetected("Variants and Ventures", VARIANTS_IDS);
        if (!ModCompat.isAnyLoaded(VARIANTS_IDS)) {
            return;
        }
        List<EntityType<?>> variants = ModCompat.hostileEntitiesForNamespaces(VARIANTS_IDS);
        pools.pool(EnemyPoolRole.MELEE).addAll(ModCompat.findEntitiesMatching(VARIANTS_IDS, "zombie", "spider", "piglin"), 2, "variants_and_ventures");
        pools.pool(EnemyPoolRole.RANGED).addAll(ModCompat.findEntitiesMatching(VARIANTS_IDS, "skeleton", "archer", "pillager"), 2, "variants_and_ventures");
        pools.pool(EnemyPoolRole.THEME).addAll(variants, theme == CrystalTheme.ARCANE ? 2 : 1, "variants_and_ventures");
    }

    private static void addVillagersCompat(CrystalTheme theme, EnemyPoolSet pools) {
        ModCompat.debugDetected("Villagers and Pillagers", VILLAGERS_IDS);
        if (!ModCompat.isAnyLoaded(VILLAGERS_IDS)) {
            return;
        }
        List<EntityType<?>> illagers = ModCompat.findEntitiesMatching(VILLAGERS_IDS, "pillager", "illager", "vindicator", "evoker", "ravager");
        if (theme == CrystalTheme.ARCANE) {
            pools.pool(EnemyPoolRole.RANGED).addAll(illagers, 2, "villagersandpillagers");
            pools.pool(EnemyPoolRole.ELITE).addAll(illagers, 2, "villagersandpillagers");
            pools.pool(EnemyPoolRole.SUPPORT).addAll(illagers, 1, "villagersandpillagers");
        } else if (theme == CrystalTheme.BEAST) {
            pools.pool(EnemyPoolRole.ELITE).addAll(ModCompat.findEntitiesMatching(VILLAGERS_IDS, "ravager"), 3, "villagersandpillagers");
        }
    }
}
