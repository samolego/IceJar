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
package fr.neatmonster.nocheatplus.compat;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Collection;

import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.Material;
import org.bukkit.entity.Entity;
import net.minecraft.server.level.ServerPlayer;
import org.bukkit.entity.Projectile;
import org.bukkit.event.player.PlayerTeleportEvent.TeleportCause;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.FireworkMeta;
import org.bukkit.inventory.meta.ItemMeta;

import fr.neatmonster.nocheatplus.utilities.ReflectionUtil;


/**
 * Various bridge methods not enough for an own class.
 * @author mc_dev
 *
 */
public class BridgeMisc {

    private static GameMode getSpectatorGameMode() {
        try {
            return GameMode.SPECTATOR;
        } catch (Throwable t) {}
        return null;
    }

    public static final GameMode GAME_MODE_SPECTATOR = getSpectatorGameMode();

    private static final Method Bukkit_getOnlinePlayers = ReflectionUtil.getMethodNoArgs(Bukkit.class, "getOnlinePlayers");

    /**
     * Correction of position: Used for ordinary setting back. <br>
     * NOTE: Currently it's not distinguished, if we do it as a proactive
     * measure, or due to an actual check violation.
     */
    public static final TeleportCause TELEPORT_CAUSE_CORRECTION_OF_POSITION = TeleportCause.UNKNOWN;

    /**
     * Return a shooter of a projectile if we get an entity, null otherwise.
     */
    public static Player getShooterPlayer(Projectile projectile) {
        Object source;
        try {
            source = projectile.getClass().getMethod("getShooter").invoke(projectile);
        } catch (IllegalArgumentException e) {
            return null;
        } catch (SecurityException e) {
            return null;
        } catch (IllegalAccessException e) {
            return null;
        } catch (InvocationTargetException e) {
            return null;
        } catch (NoSuchMethodException e) {
            return null;
        }
        if (source instanceof Player) {
            return (Player) source;
        } else {
            return null;
        }
    }

    /**
     * Retrieve a player from projectiles or cast to player, if possible.
     * @param damager
     * @return
     */
    public static Player getAttackingPlayer(Entity damager) {
        if (damager instanceof Player) {
            return (Player) damager;
        } else if (damager instanceof Projectile) {
            return getShooterPlayer((Projectile) damager);
        } else {
            return null;
        }
    }

    /**
     * Get online players as an array (convenience for reducing IDE markers :p).
     * @return
     */
    public static Player[] getOnlinePlayers() {
        try {
            Collection<? extends Player> players = Bukkit.getOnlinePlayers();
            return players.isEmpty() ? new Player[0] : players.toArray(new Player[players.size()]);
        }
        catch (NoSuchMethodError e) {}
        if (Bukkit_getOnlinePlayers != null) {
            Object obj = ReflectionUtil.invokeMethodNoArgs(Bukkit_getOnlinePlayers, null);
            if (obj != null && (obj instanceof Player[])) {
                return (Player[]) obj;
            }
        }
        return new Player[0];
    }

    /**
     * Test side conditions for fireworks boost with elytra on, for interaction
     * with the item in hand. Added with Minecraft 1.11.2.
     * 
     * @param player
     * @param materialInHand
     *            The type of the item used with interaction.
     * @return
     */
    public static boolean maybeElytraBoost(final ServerPlayer player, final Material materialInHand) {
        // TODO: Account for MC version (needs configuration override or auto adapt to protocol support).
        // TODO: Non-static due to version checks (...).
        return BridgeMaterial.FIREWORK_ROCKET != null 
                && materialInHand == BridgeMaterial.FIREWORK_ROCKET && Bridge1_9.isGlidingWithElytra(player);
    }

    /**
     * Get the power for a firework(s) item (stack).
     * 
     * @param item
     * @return The power. Should be between 0 and 127 including edges, in case
     *         of valid items, according to javadocs (Spigot MC 1.11.2). In case
     *         the item is null or can't be judged, -1 is returned.
     */
    public static int getFireworksPower(final ItemStack item) {
        if (item == null || item.getType() != BridgeMaterial.FIREWORK_ROCKET) {
            return 0;
        }
        final ItemMeta meta = item.getItemMeta();
        if (!(meta instanceof FireworkMeta)) { // INDIRECT: With elytra, this already exists.
            return 0;
        }
        final FireworkMeta fwMeta = (FireworkMeta) meta;
        return fwMeta.getPower();
    }

}
