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
package fr.neatmonster.nocheatplus.checks.net.protocollib;

import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

import org.bukkit.GameMode;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.data.Ageable;
import net.minecraft.server.level.ServerPlayer;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.inventory.InventoryOpenEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerItemConsumeEvent;
import org.bukkit.event.player.PlayerItemHeldEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.bukkit.plugin.Plugin;

import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.events.ListenerPriority;
import com.comphenix.protocol.events.PacketEvent;
import com.comphenix.protocol.wrappers.EnumWrappers.PlayerDigType;

import fr.neatmonster.nocheatplus.NCPAPIProvider;
import fr.neatmonster.nocheatplus.checks.moving.MovingData;
import fr.neatmonster.nocheatplus.compat.Bridge1_9;
import fr.neatmonster.nocheatplus.compat.BridgeEnchant;
import fr.neatmonster.nocheatplus.components.NoCheatPlusAPI;
import fr.neatmonster.nocheatplus.components.registry.order.RegistrationOrder.RegisterMethodWithOrder;
import fr.neatmonster.nocheatplus.event.mini.MiniListener;
import fr.neatmonster.nocheatplus.logging.StaticLog;
import fr.neatmonster.nocheatplus.players.DataManager;
import fr.neatmonster.nocheatplus.players.IPlayerData;
import fr.neatmonster.nocheatplus.utilities.InventoryUtil;

public class NoSlow extends BaseAdapter {
    private final static String dftag = "system.nocheatplus.noslow";
    private final static MiniListener<?>[] miniListeners = new MiniListener<?>[] {
        new MiniListener<PlayerItemConsumeEvent>() {
            @EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
            @RegisterMethodWithOrder(tag = dftag)
            @Override
            public void onEvent(final ServerPlayerItemConsumeEvent event) {
                onItemConsume(event);
            }
        },
        new MiniListener<PlayerInteractEvent>() {
            @EventHandler(priority = EventPriority.LOWEST)
            @RegisterMethodWithOrder(tag = dftag)
            @Override
            public void onEvent(final ServerPlayerInteractEvent event) {
                onItemInteract(event);
            }
        },
        new MiniListener<InventoryOpenEvent>() {
            @EventHandler(priority = EventPriority.LOWEST)
            @RegisterMethodWithOrder(tag = dftag)
            @Override
            public void onEvent(final InventoryOpenEvent event) {
                onInventoryOpen(event);
            }
        },
        new MiniListener<PlayerItemHeldEvent>() {
            @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
            @RegisterMethodWithOrder(tag = dftag)
            @Override
            public void onEvent(final ServerPlayerItemHeldEvent event) {
                onChangeSlot(event);
            }
        },
        new MiniListener<BlockPlaceEvent>() {
            @EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
            @RegisterMethodWithOrder(tag = dftag)
            @Override
            public void onEvent(final BlockPlaceEvent event) {
                onPlace(event);
            }
        }
    };

    private static int timeBetweenRL = 70;
    private static PacketType[] initPacketTypes() {
        final List<PacketType> types = new LinkedList<PacketType>(Arrays.asList(
                PacketType.Play.Client.BLOCK_DIG
                ));
        return types.toArray(new PacketType[types.size()]);
    }

    public NoSlow(Plugin plugin) {
        super(plugin, ListenerPriority.LOW, initPacketTypes());
        final NoCheatPlusAPI api = NCPAPIProvider.getNoCheatPlusAPI();
        for (final MiniListener<?> listener : miniListeners) {
            api.addComponent(listener, false);
        }
    }

    @Override
    public void onPacketReceiving(final PacketEvent event) {
        if (event.isPlayerTemporary()) return;
        handleDiggingPacket(event);
    }

    private static void onItemConsume(final ServerPlayerItemConsumeEvent e){
        final ServerPlayer p = e.getPlayer();
        
        final IPlayerData pData = DataManager.getPlayerData(p);
        final MovingData data = pData.getGenericInstance(MovingData.class);
        data.isUsingItem = false;        
    }

    private static void onInventoryOpen(final InventoryOpenEvent e){
        if (e.isCancelled()) return;
        final ServerPlayer p = (Player) e.getPlayer();
        
        final IPlayerData pData = DataManager.getPlayerData(p);
        final MovingData data = pData.getGenericInstance(MovingData.class);
        data.isUsingItem = false;        
    }

    private static void onItemInteract(final ServerPlayerInteractEvent e){
        // TODO: Add trident (Check for rain and verify if the player is exposed to it at all, might not be worth doing it...)
        if (e.getAction() != Action.RIGHT_CLICK_AIR && e.getAction() != Action.RIGHT_CLICK_BLOCK) return;
        final ServerPlayer p = e.getPlayer();
        final IPlayerData pData = DataManager.getPlayerData(p);
        final MovingData data = pData.getGenericInstance(MovingData.class);
        // Reset
        data.offHandUse = false;
        Block b = e.getClickedBlock();
        if (p.getGameMode() == GameMode.CREATIVE) {
            data.isUsingItem = false;
            return;
        }
        if (b != null && (
                b.getType().toString().endsWith("DOOR")
             || b.getType().toString().endsWith("GATE")
             || b.getType().toString().endsWith("BUTTON")
             || b.getType().toString().endsWith("LEVER")
             || (b.getType().name().startsWith("SWEET_BERRY_BUSH") && ((Ageable) b.getBlockData()).getAge() > 1)
        )) {
            data.isUsingItem = false;
            return;
        }
        if (e.hasItem()) {
            ItemStack item = e.getItem();
            Material m = item.getType();
            if (Bridge1_9.hasElytra() && p.hasCooldown(m)) return;
            if (InventoryUtil.isConsumable(item)) {
                // pre1.9 splash potion
                if (!Bridge1_9.hasElytra() && item.getDurability() > 16384) return;
                if (m == Material.POTION || m == Material.MILK_BUCKET || m.toString().endsWith("_APPLE") || m.name().startsWith("HONEY_BOTTLE")) {
                    data.isUsingItem = true;
                    data.offHandUse = Bridge1_9.hasGetItemInOffHand() && e.getHand() == EquipmentSlot.OFF_HAND;
                    return;
                }
                if (item.getType().isEdible() && p.getFoodLevel() < 20) {
                    data.isUsingItem = true;
                    data.offHandUse = Bridge1_9.hasGetItemInOffHand() && e.getHand() == EquipmentSlot.OFF_HAND;
                    return;
                }
            }
            if (m.toString().equals("BOW") && hasArrow(p.getInventory())) {
                data.isUsingItem = true;
                data.offHandUse = Bridge1_9.hasGetItemInOffHand() && e.getHand() == EquipmentSlot.OFF_HAND;
                return;
            }
            if (m.name().equals("SHIELD")) {
                data.offHandUse = e.getHand() == EquipmentSlot.OFF_HAND;
                return;
            }
            if (m.toString().equals("CROSSBOW")) {
                if (item.getItemMeta().serialize().get("charged").equals(false) && hasArrow(p.getInventory())) {
                    data.isUsingItem = true;
                    data.offHandUse = e.getHand() == EquipmentSlot.OFF_HAND;
                }
            }
        } else data.isUsingItem = false;        
    }

    private static void onChangeSlot(final ServerPlayerItemHeldEvent e) {
        final ServerPlayer p = e.getPlayer();
        final IPlayerData pData = DataManager.getPlayerData(p);
        final MovingData data = pData.getGenericInstance(MovingData.class);
        if (data.slotChange) {
            p.getInventory().setHeldItemSlot(data.oldItemSlot);
            data.slotChange = false;
        }
        data.isUsingItem = false;
    }

    private static void onPlace(final BlockPlaceEvent e) {
        final ServerPlayer p = e.getPlayer();
        final IPlayerData pData = DataManager.getPlayerData(p);
        final MovingData data = pData.getGenericInstance(MovingData.class);
        if (InventoryUtil.isConsumable(e.getItemInHand())) data.isUsingItem = false;
    }

    private static boolean hasArrow(PlayerInventory i) {
        if (Bridge1_9.hasElytra()) {
            Material m = i.getItemInOffHand().getType();
            return i.contains(Material.ARROW) || m.toString().endsWith("ARROW") || i.contains(Material.TIPPED_ARROW) || i.contains(Material.SPECTRAL_ARROW);
        }
        return i.contains(Material.ARROW);
    }

    private void handleDiggingPacket(PacketEvent event)
    {
        if(event.getPacketType() != PacketType.Play.Client.BLOCK_DIG) return;

        Player p = event.getPlayer();       
        
        if (p == null) {
            counters.add(ProtocolLibComponent.idNullPlayer, 1);
            return;
        }
        final IPlayerData pData = DataManager.getPlayerDataSafe(p);
        if (pData == null) {
            StaticLog.logWarning("Failed to fetch player data with " + event.getPacketType() + " for: " + p.toString());
            return;
        }
        final MovingData data = pData.getGenericInstance(MovingData.class);
        PlayerDigType digtype = event.getPacket().getPlayerDigTypes().read(0);
        // DROP_ALL_ITEMS when dead?
        if (digtype == PlayerDigType.DROP_ALL_ITEMS || digtype == PlayerDigType.DROP_ITEM) data.isUsingItem = false;
        
        //Advanced check
        if(digtype == PlayerDigType.RELEASE_USE_ITEM) {
            data.isUsingItem = false;
            long now = System.currentTimeMillis();
            if (data.releaseItemTime != 0) {
                if (now < data.releaseItemTime) {
                    data.releaseItemTime = now;
                    return;
                }
                if (data.releaseItemTime + timeBetweenRL > now) {
                    data.isHackingRI = true;
                }
            }
            data.releaseItemTime = now;
        }
    }

    /**
     * Set Minimum time between RELEASE_USE_ITEM packet is sent.
     * If time lower this value, A check will flag
     * Should be set from 51-100. Larger number, more protection more false-positive
     * 
     * @param milliseconds
     */ 
    public static void setuseRLThreshold(int time) {
        timeBetweenRL = time;
    }   
}