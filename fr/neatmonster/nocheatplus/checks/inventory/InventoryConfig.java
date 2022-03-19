/*
 * This program is free software: you can redistribute it and/or modify
 *   it under the terms of the GNU General Public License as published by
 *   the Free Software Foundation, either version 3 of the License, or
 *   (at your option) any later version.
 *
 *   This program is distributed in the hope that it will be useful,
 *   but WITHOUT ANY WARRANTY; without even the implied warranty of
 *   MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *   GNU General Public License for more details.
 *
 *   You should have received a copy of the GNU General Public License
 *   along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package fr.neatmonster.nocheatplus.checks.inventory;

import java.util.HashSet;
import java.util.Set;

import org.bukkit.Material;

import fr.neatmonster.nocheatplus.actions.ActionList;
import fr.neatmonster.nocheatplus.checks.CheckType;
import fr.neatmonster.nocheatplus.checks.access.ACheckConfig;
import fr.neatmonster.nocheatplus.compat.AlmostBoolean;
import fr.neatmonster.nocheatplus.compat.versions.ServerVersion;
import fr.neatmonster.nocheatplus.components.config.value.OverrideType;
import fr.neatmonster.nocheatplus.config.ConfPaths;
import fr.neatmonster.nocheatplus.config.ConfigFile;
import fr.neatmonster.nocheatplus.permissions.Permissions;
import fr.neatmonster.nocheatplus.worlds.IWorldData;

/**
 * Configurations specific for the "inventory" checks. Every world gets one of
 * these assigned to it, or if a world doesn't get it's own, it will use the
 * "global" version.
 */
public class InventoryConfig extends ACheckConfig {

    public final int        dropLimit;
    public final long       dropTimeFrame;
    public final ActionList dropActions;

    public final boolean    fastClickSpareCreative;
    public final boolean 	fastClickTweaks1_5;
    public final float		fastClickShortTermLimit;
    public final float		fastClickNormalLimit;
    public final int        chestOpenLimit;
    public final Set<String> inventoryExemptions = new HashSet<String>();
    public final float      fastClickImprobableWeight;
    public final ActionList fastClickActions;

    public final long		fastConsumeDuration;
    public final boolean    fastConsumeWhitelist;
    public final Set<Material> fastConsumeItems = new HashSet<Material>();
    public final ActionList fastConsumeActions;

    public final int        gutenbergPageLimit;
    public final ActionList gutenbergActions;

    public final boolean 	instantBowStrict;
    public final long       instantBowDelay;
    public final boolean    instantBowImprobableFeedOnly;
    public final float      instantBowImprobableWeight;
    public final ActionList instantBowActions;

    public final ActionList instantEatActions;

    public final boolean	openClose;
    public final boolean	openCancelOther;
    
    public final boolean    invMoveDisableCreative;
    public final double     invMoveHdistDivisor;
    public final boolean    invMoveImprobableFeedOnly;
    public static float     invMoveImprobableWeight;
    public final ActionList invMoveActionList;

    // Hot fixes.
    public final boolean hotFixFallingBlockEndPortalActive;

    /**
     * Instantiates a new inventory configuration.
     * 
     * @param data
     *            the data
     */
    public InventoryConfig(final IWorldData worldData) {
        super(worldData);
        final ConfigFile data = worldData.getRawConfiguration();
        dropLimit = data.getInt(ConfPaths.INVENTORY_DROP_LIMIT);
        dropTimeFrame = data.getLong(ConfPaths.INVENTORY_DROP_TIMEFRAME);
        dropActions = data.getOptimizedActionList(ConfPaths.INVENTORY_DROP_ACTIONS, Permissions.INVENTORY_DROP);

        fastClickSpareCreative = data.getBoolean(ConfPaths.INVENTORY_FASTCLICK_SPARECREATIVE);
        fastClickTweaks1_5 = data.getBoolean(ConfPaths.INVENTORY_FASTCLICK_TWEAKS1_5);
        fastClickShortTermLimit = (float) data.getDouble(ConfPaths.INVENTORY_FASTCLICK_LIMIT_SHORTTERM);
        fastClickNormalLimit = (float) data.getDouble(ConfPaths.INVENTORY_FASTCLICK_LIMIT_NORMAL);
        chestOpenLimit = data.getInt(ConfPaths.INVENTORY_FASTCLICK_LIMIT_CHEST);
        data.readStringlFromList(ConfPaths.INVENTORY_FASTCLICK_EXCLUDE, inventoryExemptions);
        fastClickImprobableWeight = (float) data.getDouble(ConfPaths.INVENTORY_FASTCLICK_IMPROBABLE_WEIGHT);
        fastClickActions = data.getOptimizedActionList(ConfPaths.INVENTORY_FASTCLICK_ACTIONS, Permissions.INVENTORY_FASTCLICK);

        if (ServerVersion.compareMinecraftVersion("1.9") >= 0) {
            /** Note: Disable check should use
             *    NCPAPIProvider#getNoCheatPlusAPI()#getWorldDataManager()#overrideCheckActivation()
             *  to actually disable from all worlds. 
             *  Using worldData from config will only affect
             *  on world they are staying on join; And on other worlds still remain unchanged.
             */
            NCPAPIProvider.getNoCheatPlusAPI().getWorldDataManager().overrideCheckActivation(
                    CheckType.INVENTORY_FASTCONSUME, AlmostBoolean.NO, 
                    OverrideType.PERMANENT, true);
            // Just in case they disable fastconsume in the config and switch to default (instanteat)
            NCPAPIProvider.getNoCheatPlusAPI().getWorldDataManager().overrideCheckActivation(
                    CheckType.INVENTORY_INSTANTEAT, AlmostBoolean.NO, 
                    OverrideType.PERMANENT, true);
            NCPAPIProvider.getNoCheatPlusAPI().getWorldDataManager().overrideCheckActivation(
                    CheckType.INVENTORY_INSTANTBOW, AlmostBoolean.NO, 
                    OverrideType.PERMANENT, true);
        }
        
        fastConsumeDuration = (long) (1000.0 * data.getDouble(ConfPaths.INVENTORY_FASTCONSUME_DURATION));
        fastConsumeWhitelist = data.getBoolean(ConfPaths.INVENTORY_FASTCONSUME_WHITELIST);
        data.readMaterialFromList(ConfPaths.INVENTORY_FASTCONSUME_ITEMS, fastConsumeItems);
        fastConsumeActions = data.getOptimizedActionList(ConfPaths.INVENTORY_FASTCONSUME_ACTIONS, Permissions.INVENTORY_FASTCONSUME);

        gutenbergPageLimit = data.getInt(ConfPaths.INVENTORY_GUTENBERG_PAGELIMIT);
        gutenbergActions = data.getOptimizedActionList(ConfPaths.INVENTORY_GUTENBERG_ACTIONS, Permissions.INVENTORY_GUTENBERG);

        instantBowStrict = data.getBoolean(ConfPaths.INVENTORY_INSTANTBOW_STRICT);
        instantBowDelay = data.getInt(ConfPaths.INVENTORY_INSTANTBOW_DELAY);
        instantBowImprobableFeedOnly = data.getBoolean(ConfPaths.INVENTORY_INSTANTBOW_IMPROBABLE_FEEDONLY);
        instantBowImprobableWeight = (float) data.getDouble(ConfPaths.INVENTORY_INSTANTBOW_IMPROBABLE_WEIGHT);
        instantBowActions = data.getOptimizedActionList(ConfPaths.INVENTORY_INSTANTBOW_ACTIONS, Permissions.INVENTORY_INSTANTBOW);

        instantEatActions = data.getOptimizedActionList(ConfPaths.INVENTORY_INSTANTEAT_ACTIONS, Permissions.INVENTORY_INSTANTEAT);

        openClose = data.getBoolean(ConfPaths.INVENTORY_OPEN_CLOSE);
        openCancelOther = data.getBoolean(ConfPaths.INVENTORY_OPEN_CANCELOTHER);
        
	    invMoveDisableCreative = data.getBoolean(ConfPaths.INVENTORY_INVENTORYMOVE_DISABLECREATIVE);
	    invMoveHdistDivisor = data.getDouble(ConfPaths.INVENTORY_INVENTORYMOVE_HDISTDIVISOR);
        invMoveImprobableFeedOnly = data.getBoolean(ConfPaths.INVENTORY_INVENTORYMOVE_IMPROBABLE_FEEDONLY);
        invMoveImprobableWeight = (float) data.getDouble(ConfPaths.INVENTORY_INSTANTBOW_IMPROBABLE_WEIGHT);
        invMoveActionList = data.getOptimizedActionList(ConfPaths.INVENTORY_INVENTORYMOVE_ACTIONS, Permissions.INVENTORY_INVENTORYMOVE);

        hotFixFallingBlockEndPortalActive = data.getBoolean(ConfPaths.INVENTORY_HOTFIX_DUPE_FALLINGBLOCKENDPORTAL);
    }

}
