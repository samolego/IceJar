/*
 * AntiCheatReloaded for Bukkit and Spigot.
 * Copyright (c) 2012-2015 AntiCheat Team
 * Copyright (c) 2016-2022 Rammelkast
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */
package com.rammelkast.anticheatreloaded.event;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;

import com.rammelkast.anticheatreloaded.AntiCheatReloaded;
import com.rammelkast.anticheatreloaded.check.CheckResult;
import com.rammelkast.anticheatreloaded.check.CheckType;
import com.rammelkast.anticheatreloaded.check.player.IllegalInteract;

public class BlockListener extends EventListener {

    @EventHandler(ignoreCancelled = true)
    public void onBlockPlace(BlockPlaceEvent event) {
        final Player player = event.getPlayer();
        boolean noHack = true;
        if (getCheckManager().willCheck(player, CheckType.FAST_PLACE)) {
            CheckResult result = getBackend().checkFastPlace(player);
            if (result.failed()) {
                event.setCancelled(!silentMode());
                log(result.getMessage(), player, CheckType.FAST_PLACE, result.getSubCheck());
                noHack = false;
            } 
        }
        
        if (getCheckManager().willCheck(player, CheckType.ILLEGAL_INTERACT)) {
            CheckResult result = IllegalInteract.performCheck(player, event);
            if (result.failed()) {
                event.setCancelled(!silentMode());
                log(result.getMessage(), player, CheckType.ILLEGAL_INTERACT, result.getSubCheck());
                noHack = false;
            } 
        }
        if (noHack) {
        	 decrease(player);
             getBackend().logBlockPlace(player);
        }
        AntiCheatReloaded.getManager().addEvent(event.getEventName(), event.getHandlers().getRegisteredListeners());
    }

    @EventHandler(ignoreCancelled = true)
    public void onBlockBreak(BlockBreakEvent event) {
        final Player player = event.getPlayer();
        boolean noHack = true;
        CheckResult result;
        if (getCheckManager().willCheck(player, CheckType.ILLEGAL_INTERACT)) {
            result = IllegalInteract.performCheck(player, event);
            if (result.failed()) {
                event.setCancelled(!silentMode());
                log(result.getMessage(), player, CheckType.ILLEGAL_INTERACT, result.getSubCheck());
                noHack = false;
            }
        }
        if (noHack) {
            decrease(player);
        }
    }
}
