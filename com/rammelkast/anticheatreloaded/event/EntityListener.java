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
import org.bukkit.event.EventPriority;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityDamageEvent.DamageCause;
import org.bukkit.event.entity.EntityRegainHealthEvent;
import org.bukkit.event.entity.EntityRegainHealthEvent.RegainReason;
import org.bukkit.event.entity.EntityShootBowEvent;
import org.bukkit.event.entity.FoodLevelChangeEvent;

import com.rammelkast.anticheatreloaded.AntiCheatReloaded;
import com.rammelkast.anticheatreloaded.check.CheckResult;
import com.rammelkast.anticheatreloaded.check.CheckType;
import com.rammelkast.anticheatreloaded.check.combat.CriticalsCheck;
import com.rammelkast.anticheatreloaded.check.combat.KillAuraCheck;
import com.rammelkast.anticheatreloaded.check.movement.AimbotCheck;

public class EntityListener extends EventListener {

    @EventHandler
    public void onEntityShootBow(EntityShootBowEvent event) {
        if (event.getEntity() instanceof Player) {
            Player player = (Player) event.getEntity();
            if (getCheckManager().willCheck(player, CheckType.FAST_BOW)) {
                CheckResult result = getBackend().checkFastBow(player, event.getForce());
                if (result.failed()) {
                    event.setCancelled(!silentMode());
                    log(result.getMessage(), player, CheckType.FAST_BOW, result.getSubCheck());
                } else {
                    decrease(player);
                }
            }
        }

        AntiCheatReloaded.getManager().addEvent(event.getEventName(), event.getHandlers().getRegisteredListeners());
    }

    @EventHandler
    public void onEntityRegainHealth(EntityRegainHealthEvent event) {
        if (event.getEntity() instanceof Player && event.getRegainReason() == RegainReason.SATIATED) {
            Player player = (Player) event.getEntity();
            if (getCheckManager().willCheck(player, CheckType.FAST_HEAL)) {
                CheckResult result = getBackend().checkFastHeal(player);
                if (result.failed()) {
                    event.setCancelled(!silentMode());
                    log(result.getMessage(), player, CheckType.FAST_HEAL, result.getSubCheck());
                } else {
                    decrease(player);
                    getBackend().logHeal(player);
                }
            }
        }

        AntiCheatReloaded.getManager().addEvent(event.getEventName(), event.getHandlers().getRegisteredListeners());
    }

    @EventHandler
    public void onFoodLevelChange(FoodLevelChangeEvent event) {
        if (event.getEntity() instanceof Player) {
            Player player = (Player) event.getEntity();
            if (player.getFoodLevel() < event.getFoodLevel() && getCheckManager().willCheck(player, CheckType.FAST_EAT)) // Make sure it's them actually gaining a food level
            {
                CheckResult result = getBackend().checkFastEat(player);
                if (result.failed()) {
                    event.setCancelled(!silentMode());
                    log(result.getMessage(), player, CheckType.FAST_EAT, result.getSubCheck());
                } else {
                    decrease(player);
                }
            }
        }

        AntiCheatReloaded.getManager().addEvent(event.getEventName(), event.getHandlers().getRegisteredListeners());
    }

    @EventHandler(priority=EventPriority.HIGHEST, ignoreCancelled = true)
    public void onEntityDamage(EntityDamageEvent event) {
        boolean noHack = true;
        if (event instanceof EntityDamageByEntityEvent) {
            EntityDamageByEntityEvent e = (EntityDamageByEntityEvent) event;
            if (e.getDamager() instanceof Player) {
            	if (getCheckManager().willCheck((Player)e.getDamager(), CheckType.CRITICALS)) {
                	CriticalsCheck.doDamageEvent(e, (Player)e.getDamager());	 
            	}
            	if (getCheckManager().willCheckQuick((Player)e.getDamager(), CheckType.AIMBOT)) {
    				final CheckResult result = AimbotCheck.runCheck((Player)e.getDamager(), e);
    				if (result.failed()) {
    					log(result.getMessage(), (Player)e.getDamager(), CheckType.AIMBOT, result.getSubCheck());
    				}
    			}
            }
			if (e.getDamager() instanceof Player && event.getCause() == DamageCause.ENTITY_ATTACK) {
				Player player = (Player) e.getDamager();
				if (getCheckManager().willCheck(player, CheckType.KILLAURA)) {
					CheckResult result = KillAuraCheck.checkAngle(player, event);
					if (result.failed()) {
						event.setCancelled(!silentMode());
						log(result.getMessage(), player, CheckType.KILLAURA, result.getSubCheck());
						noHack = false;
					}
				}
				if (getCheckManager().willCheck(player, CheckType.KILLAURA)) {
					CheckResult result = KillAuraCheck.checkReach(player, event.getEntity());
					if (result.failed()) {
						event.setCancelled(!silentMode());
						log(result.getMessage(), player, CheckType.KILLAURA, result.getSubCheck());
						noHack = false;
					}
				}
				if (getCheckManager().willCheck(player, CheckType.KILLAURA)) {
					CheckResult result = KillAuraCheck.checkPacketOrder(player, event.getEntity());
					if (result.failed()) {
						event.setCancelled(!silentMode());
						log(result.getMessage(), player, CheckType.KILLAURA, result.getSubCheck());
						noHack = false;
					}
				}
				if (noHack) {
					decrease(player);
				}
			}
        }

        AntiCheatReloaded.getManager().addEvent(event.getEventName(), event.getHandlers().getRegisteredListeners());
    }
}
