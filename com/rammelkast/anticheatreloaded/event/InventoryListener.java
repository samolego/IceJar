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
import org.bukkit.event.inventory.InventoryClickEvent;

import com.rammelkast.anticheatreloaded.AntiCheatReloaded;
import com.rammelkast.anticheatreloaded.check.CheckResult;
import com.rammelkast.anticheatreloaded.check.CheckType;

public class InventoryListener extends EventListener {

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        if (!event.isRightClick() && !event.isShiftClick() && event.getWhoClicked() instanceof Player) {
            Player player = (Player) event.getWhoClicked();
            if (getCheckManager().willCheck(player, CheckType.FAST_INVENTORY)) {
                CheckResult result = getBackend().checkInventoryClicks(player);
                if (result.failed()) {
                    if (!silentMode()) {
                        event.setCancelled(true);
                    }
                    log(result.getMessage(), player, CheckType.FAST_INVENTORY, result.getSubCheck());
                } else {
                    decrease(player);
                }
            }
        }

        AntiCheatReloaded.getManager().addEvent(event.getEventName(), event.getHandlers().getRegisteredListeners());
    }

}
