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

import net.minecraft.server.level.ServerPlayer;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerEditBookEvent;
import org.bukkit.inventory.meta.BookMeta;

import fr.neatmonster.nocheatplus.checks.Check;
import fr.neatmonster.nocheatplus.checks.CheckType;
import fr.neatmonster.nocheatplus.players.DataManager;
import fr.neatmonster.nocheatplus.players.IPlayerData;

public class Gutenberg extends Check implements Listener {

    public static void testAvailability(){
        if (!PlayerEditBookEvent.class.getSimpleName().equals("PlayerEditBookEvent")){
            throw new RuntimeException("This exception should not even get thrown.");
        }
    }

    public Gutenberg() {
        super(CheckType.INVENTORY_GUTENBERG);
    }

    @EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
    public void onEditBook(final ServerPlayerEditBookEvent event) {
        final ServerPlayer player = event.getPlayer();
        if (!isEnabled(player)) {
            return;
        }
        final IPlayerData pData = DataManager.getPlayerData(player);
        final InventoryConfig cc = pData.getGenericInstance(InventoryConfig.class);
        final InventoryData data = pData.getGenericInstance(InventoryData.class);
        final BookMeta newMeta = event.getNewBookMeta();
        final int pages = newMeta.getPageCount();
        if (pages <= cc.gutenbergPageLimit) {
            // Legitimate.
            return;
        }
        // Violation.
        final int vl = pages - cc.gutenbergPageLimit;
        data.gutenbergVL += vl;
        if (executeActions(player, data.gutenbergVL, vl, cc.gutenbergActions).willCancel()) {
            event.setCancelled(true);
        }
    }

}
