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

import java.util.ArrayList;
import java.util.List;

import org.bukkit.Material;
import net.minecraft.server.level.ServerPlayer;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.inventory.InventoryView;
import org.bukkit.inventory.ItemStack;

import fr.neatmonster.nocheatplus.checks.Check;
import fr.neatmonster.nocheatplus.checks.CheckType;
import fr.neatmonster.nocheatplus.actions.ParameterName;
import fr.neatmonster.nocheatplus.checks.ViolationData;
import fr.neatmonster.nocheatplus.checks.combined.Improbable;
import fr.neatmonster.nocheatplus.permissions.Permissions;
import fr.neatmonster.nocheatplus.players.IPlayerData;
import fr.neatmonster.nocheatplus.utilities.InventoryUtil;
import fr.neatmonster.nocheatplus.utilities.TickTask;
import fr.neatmonster.nocheatplus.utilities.StringUtil;


/**
 * FastClick checks for players clicking in inventories too quickly (nerfs autosteal cheats and the like).
 */
public class FastClick extends Check {


    final List<String> tags = new ArrayList<String>();


    /**
     * Instantiates a new fast click check.
     */
    public FastClick() {
        super(CheckType.INVENTORY_FASTCLICK);
    }


   /**
    * Checks a player.
    * @param player
    * @param now Millisec
    * @param view
    * @param slot
    * @param cursor
    * @param clicked
    * @param isShiftClick
    * @param inventoryAction
    * @param data
    * @param cc 
    * @param pData
    * @return true, if successful
    */
    public boolean check(final ServerPlayer player, final long now,
                         final InventoryView view, final int slot, final ItemStack cursor, 
                         final ItemStack clicked, final boolean isShiftClick, 
                         final String inventoryAction, final InventoryData data, final InventoryConfig cc, 
                         final IPlayerData pData) {

        final float amount;
        final Material clickedMat = clicked == null ? Material.AIR : clicked.getType();
        final Material cursorMat;
        final int cursorAmount;
        tags.clear();

        if (cursor != null) {
            cursorMat = cursor.getType();
            cursorAmount = Math.max(1, cursor.getAmount());
        }
        else {
            cursorMat = null;
            cursorAmount = 0;
        }

        if (inventoryAction != null) {
            amount = getAmountWithAction(view, slot, clicked, clickedMat, cursorMat, cursorAmount, 
                                        isShiftClick, inventoryAction, data, cc);
        }
        else if (cursor != null && cc.fastClickTweaks1_5) {
            // Detect shift-click features indirectly.
            amount = detectTweaks1_5(view, slot, clicked, clickedMat, cursorMat, cursorAmount, isShiftClick, data, cc);
        }
        else {
            amount = 1f;
        }

        if (isShiftClick && inventoryAction.equals("MOVE_TO_OTHER_INVENTORY") && (cursorMat != null && cursorMat != Material.AIR) 
            && clickedMat != Material.AIR) {
            return false;
        }
        
        data.fastClickFreq.add(now, amount);

        float shortTerm = data.fastClickFreq.bucketScore(0);
        if (shortTerm > cc.fastClickShortTermLimit) {
            // Check for lag.
            shortTerm /= (float) TickTask.getLag(data.fastClickFreq.bucketDuration(), true);
        }
        shortTerm -= cc.fastClickShortTermLimit;

        float normal = data.fastClickFreq.score(1f);
        if (normal > cc.fastClickNormalLimit) {
            // Check for lag.
            normal /= (float) TickTask.getLag(data.fastClickFreq.bucketDuration() * data.fastClickFreq.numberOfBuckets(), true);
        }
        normal -= cc.fastClickNormalLimit;

        final double violation = Math.max(shortTerm, normal);
        boolean cancel = false;

        if (violation > 0.0) {
            tags.add("clickspeed");
            data.fastClickVL += violation;
            final ViolationData vd = new ViolationData(this, player, data.fastClickVL, violation, cc.fastClickActions);
            if (vd.needsParameters()) vd.setParameter(ParameterName.TAGS, StringUtil.join(tags, "+"));
            cancel = executeActions(vd).willCancel();
        }
        else data.fastClickVL *= 0.99;

        if (pData.isDebugActive(type) && pData.hasPermission(Permissions.ADMINISTRATION_DEBUG, player)) {
            player.sendMessage("FastClick: " + data.fastClickFreq.bucketScore(0) + " | " + data.fastClickFreq.score(1f) + " | cursor=" + cursor
                                + " | clicked=" + clicked + " | action=" + inventoryAction);
        }

        data.fastClickLastClicked = clickedMat;
        data.fastClickLastSlot = slot;
        data.fastClickLastCursor = cursorMat;
        data.fastClickLastCursorAmount = cursorAmount;

        // Feed the improbable.
        if (cc.fastClickImprobableWeight > 0.0f) {
            Improbable.feed(player, cc.fastClickImprobableWeight * amount, now);
        }
        return cancel;
    }
    

   /**
    * Prevent players from instantly interacting with the cotainer's contents.
    * @param player
    * @param data
    * @param cc
    */
    public boolean fastClickChest(final ServerPlayer player, final InventoryData data, final InventoryConfig cc) {

        boolean cancel = false;
        tags.clear();
        if (InventoryUtil.hasOpenedContainerRecently(player, cc.chestOpenLimit)) {
            // Interaction was too quick, violation.
            tags.add("interact_time");
            double violation = (cc.chestOpenLimit / (data.lastClickTime - data.containerOpenTime)) * 100D; // Normalize.
            data.fastClickVL += violation;
            final ViolationData vd = new ViolationData(this, player, data.fastClickVL, violation, cc.fastClickActions);
            if (vd.needsParameters()) vd.setParameter(ParameterName.TAGS, StringUtil.join(tags, "+"));
            cancel = executeActions(vd).willCancel();  
        }
       return cancel;
    }
    

   /**
    * Detect the inventory tweaks that were introduced in MC 1.5
    * @param view
    * @param slot
    * @param clicked
    * @param clickedMat
    * @param cursorAmount
    * @param isShiftClick
    * @param data
    * @param cc
    */
    private float detectTweaks1_5(final InventoryView view, final int slot, final ItemStack clicked, 
                                  final Material clickedMat, final Material cursorMat, 
                                  final int cursorAmount, final boolean isShiftClick, 
                                  final InventoryData data, final InventoryConfig cc) {

        if (cursorMat != data.fastClickLastCursor 
                && (!isShiftClick || clickedMat == Material.AIR || clickedMat != data.fastClickLastClicked) 
                || cursorMat == Material.AIR || cursorAmount != data.fastClickLastCursorAmount) {
            return 1f;
        }
        else if (clickedMat == Material.AIR || clickedMat == cursorMat 
                || isShiftClick && clickedMat == data.fastClickLastClicked ) {
            return Math.min(cc.fastClickNormalLimit , cc.fastClickShortTermLimit) 
                    / (float) (isShiftClick && clickedMat != Material.AIR ? (1.0 + Math.max(cursorAmount, InventoryUtil.getStackCount(view, clicked))) : cursorAmount)  * 0.75f;
        }
        else {
            return 1f;
        }
    }


    private float getAmountWithAction(final InventoryView view, final int slot, final ItemStack clicked, 
                                      final Material clickedMat, final Material cursorMat, 
                                      final int cursorAmount, final boolean isShiftClick, 
                                      final String inventoryAction, 
                                      final InventoryData data, final InventoryConfig cc) {

        // Continuous drop feature with open inventories.
        if (inventoryAction.equals("DROP_ONE_SLOT")
                && slot == data.fastClickLastSlot 
                && clickedMat == data.fastClickLastClicked
                && view.getType() == InventoryType.CRAFTING
                // && InventoryUtil.couldHaveInventoryOpen(player)
                // TODO: Distinguish if the inventory is really open.
                ) {
            return 0.6f;
        }

        // Collect to cursor.
        if (inventoryAction.equals("COLLECT_TO_CURSOR")) {
            final int stackCount = InventoryUtil.getStackCount(view, clicked);
            return stackCount <= 0 ? 1f : 
                Math.min(cc.fastClickNormalLimit , cc.fastClickShortTermLimit) 
                / stackCount * 0.75f;
        }

        // Shift click features.
        if ((inventoryAction.equals("MOVE_TO_OTHER_INVENTORY"))
                && cursorMat != Material.AIR && cc.fastClickTweaks1_5) {
            // Let the legacy method do the side condition checks and counting for now.
            return detectTweaks1_5(view, slot, clicked, clickedMat, 
                    cursorMat, cursorAmount, isShiftClick, data, cc);
        }

        // 
        return 1f;
    }
}
