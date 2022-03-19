package fr.neatmonster.nocheatplus.compat.blocks.init.vanilla;

import org.bukkit.Material;

import fr.neatmonster.nocheatplus.compat.BridgeMaterial;
import fr.neatmonster.nocheatplus.compat.blocks.BlockPropertiesSetup;
import fr.neatmonster.nocheatplus.compat.blocks.init.BlockInit;
import fr.neatmonster.nocheatplus.config.*;
import fr.neatmonster.nocheatplus.logging.StaticLog;
import fr.neatmonster.nocheatplus.utilities.map.BlockFlags;
import fr.neatmonster.nocheatplus.utilities.map.BlockProperties;
import fr.neatmonster.nocheatplus.utilities.map.MaterialUtil;

public class BlocksMC1_17 implements BlockPropertiesSetup {
    public BlocksMC1_17() {
        BlockInit.assertMaterialExists("DEEPSLATE");
        BlockInit.assertMaterialExists("COBBLED_DEEPSLATE");
        BlockInit.assertMaterialExists("DEEPSLATE_COAL_ORE");
        BlockInit.assertMaterialExists("DEEPSLATE_GOLD_ORE");
        BlockInit.assertMaterialExists("COPPER_BLOCK");
    }

    @Override
    public void setupBlockProperties(WorldConfigProvider<?> worldConfigProvider) {
        BlockProperties.setBlockProps("LIGHT", BlockProperties.indestructibleType);

        for (Material mat : MaterialUtil.ALL_CANDLES) {
            BlockFlags.addFlags(mat, BlockFlags.SOLID_GROUND);
            BlockProperties.setBlockProps(mat, new BlockProperties.BlockProps(BlockProperties.noTool, 0.1f, BlockProperties.secToMs(0.1)));
        }

        BlockProperties.setBlockProps(Material.DEEPSLATE_COAL_ORE, new BlockProperties.BlockProps(BlockProperties.stonePickaxe, 4.5f, BlockProperties.secToMs(22.5, 3.35, 1.7, 1.15, 0.85, 0.75, 0.55)));
        BlockProperties.setBlockProps(Material.DEEPSLATE_GOLD_ORE, new BlockProperties.BlockProps(BlockProperties.ironPickaxe, 4.5f, BlockProperties.secToMs(22.5, 11.25, 5.6, 1.15, 0.85, 0.75, 1.9)));

        // Deepslate ores + Copper ore
        BlockInit.setPropsAs("DEEPSLATE_IRON_ORE", Material.DEEPSLATE_COAL_ORE);
        BlockInit.setPropsAs("DEEPSLATE_COPPER_ORE", Material.DEEPSLATE_COAL_ORE);
        BlockInit.setPropsAs("DEEPSLATE_REDSTONE_ORE", Material.DEEPSLATE_GOLD_ORE);
        BlockInit.setPropsAs("DEEPSLATE_EMERALD_ORE", Material.DEEPSLATE_GOLD_ORE);
        BlockInit.setPropsAs("DEEPSLATE_LAPIS_ORE", Material.DEEPSLATE_COAL_ORE);
        BlockInit.setPropsAs("DEEPSLATE_DIAMOND_ORE", Material.DEEPSLATE_GOLD_ORE);
        BlockInit.setPropsAs("COPPER_ORE", Material.IRON_ORE);

        // Raw metal blocks
        BlockInit.setPropsAs("RAW_IRON_BLOCK", Material.IRON_BLOCK);
        BlockInit.setPropsAs("RAW_COPPER_BLOCK", Material.IRON_BLOCK);
        BlockInit.setPropsAs("RAW_GOLD_BLOCK", Material.DIAMOND_BLOCK);

        //Deepslate blocks
        BlockFlags.addFlags(Material.DEEPSLATE, BlockFlags.FULLY_SOLID_BOUNDS);
        BlockProperties.setBlockProps(Material.DEEPSLATE, new BlockProperties.BlockProps(BlockProperties.woodPickaxe, 3f, BlockProperties.secToMs(15.0, 2.25, 1.125, 0.75, 0.6, 0.5, 0.4)));

        BlockFlags.addFlags(Material.COBBLED_DEEPSLATE, BlockFlags.FULLY_SOLID_BOUNDS);
        BlockProperties.setBlockProps(Material.COBBLED_DEEPSLATE, new BlockProperties.BlockProps(BlockProperties.woodPickaxe, 3.5f, BlockProperties.secToMs(17.5, 2.65, 1.35, 0.9, 0.7, 0.6, 0.45)));

        BlockFlags.addFlags("POLISHED_DEEPSLATE", BlockFlags.FULLY_SOLID_BOUNDS);
        BlockInit.setPropsAs("POLISHED_DEEPSLATE", Material.COBBLED_DEEPSLATE);

        BlockFlags.addFlags("DEEPSLATE_BRICKS", BlockFlags.FULLY_SOLID_BOUNDS);
        BlockInit.setPropsAs("DEEPSLATE_BRICKS", Material.COBBLED_DEEPSLATE);

        BlockFlags.addFlags("CRACKED_DEEPSLATE_BRICKS", BlockFlags.FULLY_SOLID_BOUNDS);
        BlockInit.setPropsAs("CRACKED_DEEPSLATE_BRICKS", Material.COBBLED_DEEPSLATE);

        BlockFlags.addFlags("DEEPSLATE_TILES", BlockFlags.FULLY_SOLID_BOUNDS);
        BlockInit.setPropsAs("DEEPSLATE_TILES", Material.COBBLED_DEEPSLATE);

        BlockFlags.addFlags("CRACKED_DEEPSLATE_TILES", BlockFlags.FULLY_SOLID_BOUNDS);
        BlockInit.setPropsAs("CRACKED_DEEPSLATE_TILES", Material.COBBLED_DEEPSLATE);

        BlockFlags.addFlags("CHISELED_DEEPSLATE", BlockFlags.FULLY_SOLID_BOUNDS);
        BlockInit.setPropsAs("CHISELED_DEEPSLATE", Material.COBBLED_DEEPSLATE);

        //Deepslate walls
        BlockInit.setPropsAs("COBBLED_DEEPSLATE_WALL", Material.COBBLED_DEEPSLATE);
        BlockInit.setPropsAs("POLISHED_DEEPSLATE_WALL", Material.COBBLED_DEEPSLATE);
        BlockInit.setPropsAs("DEEPSLATE_BRICK_WALL", Material.COBBLED_DEEPSLATE);
        BlockInit.setPropsAs("DEEPSLATE_TILE_WALL", Material.COBBLED_DEEPSLATE);

        //Deepslate stairs
        BlockInit.setPropsAs("COBBLED_DEEPSLATE_STAIRS", Material.COBBLED_DEEPSLATE);
        BlockInit.setPropsAs("POLISHED_DEEPSLATE_STAIRS", Material.COBBLED_DEEPSLATE);
        BlockInit.setPropsAs("DEEPSLATE_BRICK_STAIRS", Material.COBBLED_DEEPSLATE);
        BlockInit.setPropsAs("DEEPSLATE_TILE_STAIRS", Material.COBBLED_DEEPSLATE);

        //Deepslate slabs
        BlockInit.setPropsAs("COBBLED_DEEPSLATE_SLAB", Material.COBBLED_DEEPSLATE);
        BlockInit.setPropsAs("POLISHED_DEEPSLATE_SLAB", Material.COBBLED_DEEPSLATE);
        BlockInit.setPropsAs("DEEPSLATE_BRICK_SLAB", Material.COBBLED_DEEPSLATE);
        BlockInit.setPropsAs("DEEPSLATE_TILE_SLAB", Material.COBBLED_DEEPSLATE);

        // Copper Blocks
        for (Material mat : MaterialUtil.COPPER_BLOCKS) {
            BlockInit.setPropsAs(mat, Material.IRON_ORE);
        }

        // Copper stairs
        BlockInit.setPropsAs("CUT_COPPER_STAIRS", Material.COPPER_BLOCK);
        BlockInit.setPropsAs("EXPOSED_CUT_COPPER_STAIRS", Material.COPPER_BLOCK);
        BlockInit.setPropsAs("WEATHERED_CUT_COPPER_STAIRS", Material.COPPER_BLOCK);
        BlockInit.setPropsAs("OXIDIZED_CUT_COPPER_STAIRS", Material.COPPER_BLOCK);
        // Copper slabs
        BlockInit.setPropsAs("CUT_COPPER_SLAB", Material.COPPER_BLOCK);
        BlockInit.setPropsAs("EXPOSED_CUT_COPPER_SLAB", Material.COPPER_BLOCK);
        BlockInit.setPropsAs("WEATHERED_CUT_COPPER_SLAB", Material.COPPER_BLOCK);
        BlockInit.setPropsAs("OXIDIZED_CUT_COPPER_SLAB", Material.COPPER_BLOCK);

        // Waxed Copper stairs
        BlockInit.setPropsAs("WAXED_CUT_COPPER_STAIRS", Material.COPPER_BLOCK);
        BlockInit.setPropsAs("WAXED_EXPOSED_CUT_COPPER_STAIRS", Material.COPPER_BLOCK);
        BlockInit.setPropsAs("WAXED_WEATHERED_CUT_COPPER_STAIRS", Material.COPPER_BLOCK);
        BlockInit.setPropsAs("WAXED_OXIDIZED_CUT_COPPER_STAIRS", Material.COPPER_BLOCK);
        // Waxed Copper slabs
        BlockInit.setPropsAs("WAXED_CUT_COPPER_SLAB", Material.COPPER_BLOCK);
        BlockInit.setPropsAs("WAXED_EXPOSED_CUT_COPPER_SLAB", Material.COPPER_BLOCK);
        BlockInit.setPropsAs("WAXED_WEATHERED_CUT_COPPER_SLAB", Material.COPPER_BLOCK);
        BlockInit.setPropsAs("WAXED_OXIDIZED_CUT_COPPER_SLAB", Material.COPPER_BLOCK);

        // Flower Azalea
        BlockProperties.setBlockProps("AZALEA", BlockProperties.instantType);
        BlockFlags.addFlags("AZALEA", BlockProperties.F_GROUND);
        BlockProperties.setBlockProps("FLOWERING_AZALEA", BlockProperties.instantType);
        BlockFlags.addFlags("FLOWERING_AZALEA", BlockProperties.F_GROUND);
        // This is temporary ground.
        BlockFlags.addFlags("BIG_DRIPLEAF", BlockProperties.F_GROUND);
        BlockProperties.setBlockProps("BIG_DRIPLEAF", new BlockProperties.BlockProps(BlockProperties.woodAxe, 0.1f, BlockProperties.secToMs(0.15, 0.1, 0.0, 0.0, 0.0, 0.0, 0.0)));
        BlockProperties.setBlockProps("SMALL_DRIPLEAF", new BlockProperties.BlockProps(BlockProperties.woodAxe, 0.1f, BlockProperties.secToMs(0.15, 0.1, 0.0, 0.0, 0.0, 0.0, 0.0)));
        BlockProperties.setBlockProps("BIG_DRIPLEAF_STEM", new BlockProperties.BlockProps(BlockProperties.woodAxe, 0.1f, BlockProperties.secToMs(0.15, 0.1, 0.0, 0.0, 0.0, 0.0, 0.0)));
        BlockProperties.setBlockProps("GLOW_LICHEN", BlockProperties.instantType);
        BlockProperties.setBlockProps("CAVE_VINES", BlockProperties.instantType);
        BlockFlags.addFlags("CAVE_VINES", BlockProperties.F_CLIMBABLE);
        BlockFlags.addFlags("CAVE_VINES_PLANT", BlockProperties.F_CLIMBABLE);
        BlockProperties.setBlockProps("CAVE_VINES_PLANT", BlockProperties.instantType);
        BlockProperties.setBlockProps("SPORE_BLOSSOM", BlockProperties.instantType);
        BlockProperties.setBlockProps("HANGING_ROOTS", BlockProperties.instantType);
        BlockProperties.setBlockProps("ROOTED_DIRT", new BlockProperties.BlockProps(BlockProperties.noTool, 0.5f, BlockProperties.secToMs(0.75)));

        //Dripstone
        BlockProperties.setBlockProps("DRIPSTONE_BLOCK", new BlockProperties.BlockProps(BlockProperties.woodPickaxe, 1.5f, BlockProperties.secToMs(7.5, 1.15, 0.6, 0.4, 0.3, 0.25, 0.2)));
        BlockFlags.addFlags("POINTED_DRIPSTONE", BlockFlags.SOLID_GROUND | BlockProperties.F_VARIABLE);
        BlockProperties.setBlockProps("POINTED_DRIPSTONE", new BlockProperties.BlockProps(BlockProperties.woodPickaxe, 1.5f, BlockProperties.secToMs(2.25, 1.15, 0.6, 0.4, 0.3, 0.25, 0.2)));

        //Amethyst buds
        BlockFlags.addFlags("SMALL_AMETHYST_BUD", BlockFlags.SOLID_GROUND);
        BlockFlags.addFlags("MEDIUM_AMETHYST_BUD", BlockFlags.SOLID_GROUND);
        BlockFlags.addFlags("LARGE_AMETHYST_BUD", BlockFlags.SOLID_GROUND);
        BlockFlags.addFlags("AMETHYST_CLUSTER", BlockFlags.SOLID_GROUND);
        BlockProperties.setBlockProps("SMALL_AMETHYST_BUD", new BlockProperties.BlockProps(BlockProperties.woodPickaxe, 1.5f, BlockProperties.secToMs(2.25, 1.15, 0.6, 0.4, 0.3, 0.25, 0.2)));
        BlockProperties.setBlockProps("MEDIUM_AMETHYST_BUD", new BlockProperties.BlockProps(BlockProperties.woodPickaxe, 1.5f, BlockProperties.secToMs(2.25, 1.15, 0.6, 0.4, 0.3, 0.25, 0.2)));
        BlockProperties.setBlockProps("LARGE_AMETHYST_BUD", new BlockProperties.BlockProps(BlockProperties.woodPickaxe, 1.5f, BlockProperties.secToMs(2.25, 1.15, 0.6, 0.4, 0.3, 0.25, 0.2)));
        BlockProperties.setBlockProps("AMETHYST_CLUSTER", new BlockProperties.BlockProps(BlockProperties.woodPickaxe, 1.5f, BlockProperties.secToMs(2.25, 1.15, 0.6, 0.4, 0.3, 0.25, 0.2)));
        //Amethyst blocks
        BlockProperties.setBlockProps("BUDDING_AMETHYST", new BlockProperties.BlockProps(BlockProperties.woodPickaxe, 1.5f, BlockProperties.secToMs(7.5, 1.15, 0.6, 0.4, 0.3, 0.25, 0.2)));
        BlockProperties.setBlockProps("AMETHYST_BLOCK", new BlockProperties.BlockProps(BlockProperties.woodPickaxe, 1.5f, BlockProperties.secToMs(7.5, 1.15, 0.6, 0.4, 0.3, 0.25, 0.2)));

        // Not done
        //Candle cakes
        for (Material mat : MaterialUtil.ALL_CANDLE_CAKE) {
            BlockFlags.addFlags(mat, BlockProperties.F_GROUND);
            BlockProperties.setBlockProps(mat, new BlockProperties.BlockProps(BlockProperties.noTool, 0.5f, BlockProperties.secToMs(0.75)));
        }

        //Other blocks
        BlockProperties.setBlockProps("CALCITE", new BlockProperties.BlockProps(BlockProperties.woodPickaxe, 0.75f, BlockProperties.secToMs(3.75, 0.6, 0.3, 0.2, 0.15, 0.15, 0.1)));
        BlockProperties.setBlockProps("TUFF", new BlockProperties.BlockProps(BlockProperties.woodPickaxe, 1.5f, BlockProperties.secToMs(7.5, 1.15, 0.6, 0.4, 0.3, 0.25, 0.2)));
        BlockProperties.setBlockFlags("POWDER_SNOW", BlockProperties.F_POWDERSNOW | BlockProperties.F_IGN_PASSABLE | BlockProperties.F_GROUND | BlockProperties.F_GROUND_HEIGHT);
        BlockProperties.setBlockProps("POWDER_SNOW", new BlockProperties.BlockProps(BlockProperties.woodPickaxe, 0.25f, BlockProperties.secToMs(0.4)));
        BlockInit.setPropsAs("POWDER_SNOW_CAULDRON", Material.CAULDRON);
        BlockInit.setPropsAs("WATER_CAULDRON", Material.CAULDRON);
        BlockInit.setPropsAs("LAVA_CAULDRON", Material.CAULDRON);
        BlockProperties.setBlockProps("MOSS_BLOCK", new BlockProperties.BlockProps(BlockProperties.woodHoe, 0.1f, BlockProperties.secToMs(0.15, 0.1, 0.0, 0.0, 0.0, 0.0, 0.0)));
        BlockFlags.addFlags("LIGHTNING_ROD", BlockFlags.SOLID_GROUND);
        BlockProperties.setBlockProps("LIGHTNING_ROD", new BlockProperties.BlockProps(BlockProperties.woodPickaxe, 3.0f, BlockProperties.secToMs(15.0, 7.5, 1.15, 0.75, 0.6, 0.5, 1.25)));
        BlockInit.setAs("SMOOTH_BASALT", BridgeMaterial.TERRACOTTA);
        BlockFlags.addFlags("SCULK_SENSOR", BlockFlags.SOLID_GROUND);
        BlockProperties.setBlockProps("SCULK_SENSOR", new BlockProperties.BlockProps(BlockProperties.woodHoe, 1.5f, BlockProperties.secToMs(2.25, 1.15, 0.6, 0.4, 0.3, 0.25, 0.2)));
        
        ConfigFile config = ConfigManager.getConfigFile();
        if (config.getBoolean(ConfPaths.BLOCKBREAK_DEBUG, config.getBoolean(ConfPaths.CHECKS_DEBUG, false)))
        StaticLog.logInfo("Added block-info for Minecraft 1.17 blocks.");
    }
}
