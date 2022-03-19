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

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import net.minecraft.server.level.ServerPlayer;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerRespawnEvent;
import org.bukkit.event.player.PlayerTeleportEvent;
import org.bukkit.plugin.Plugin;

import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.PacketType.Protocol;
import com.comphenix.protocol.PacketType.Sender;
import com.comphenix.protocol.ProtocolLibrary;
import com.comphenix.protocol.ProtocolManager;
import com.comphenix.protocol.events.PacketAdapter;

import fr.neatmonster.nocheatplus.NCPAPIProvider;
import fr.neatmonster.nocheatplus.checks.CheckType;
import fr.neatmonster.nocheatplus.checks.net.NetData;
import fr.neatmonster.nocheatplus.compat.versions.ServerVersion;
import fr.neatmonster.nocheatplus.components.NoCheatPlusAPI;
import fr.neatmonster.nocheatplus.components.registry.feature.IDisableListener;
import fr.neatmonster.nocheatplus.components.registry.feature.INotifyReload;
import fr.neatmonster.nocheatplus.components.registry.feature.JoinLeaveListener;
import fr.neatmonster.nocheatplus.config.ConfPaths;
import fr.neatmonster.nocheatplus.config.ConfigManager;
import fr.neatmonster.nocheatplus.logging.StaticLog;
import fr.neatmonster.nocheatplus.logging.Streams;
import fr.neatmonster.nocheatplus.players.DataManager;
import fr.neatmonster.nocheatplus.players.IPlayerData;
import fr.neatmonster.nocheatplus.stats.Counters;
import fr.neatmonster.nocheatplus.utilities.StringUtil;
import fr.neatmonster.nocheatplus.worlds.IWorldDataManager;

/**
 * Quick and dirty ProtocolLib setup.
 * 
 * @author asofold
 *
 */
public class ProtocolLibComponent implements IDisableListener, INotifyReload, JoinLeaveListener, Listener {

    // TODO: Static reference is problematic (needs a static and accessible Counters instance?). 
    public static final int idNullPlayer = NCPAPIProvider.getNoCheatPlusAPI().getGenericInstance(Counters.class).registerKey("packet.nullplayer");
    /** Likely does not happen, TODO: Code review protocol plugin. */
    public static final int idInconsistentIsAsync = NCPAPIProvider.getNoCheatPlusAPI().getGenericInstance(Counters.class).registerKey("packet.inconsistent.isasync");

    /**
     * Auxiliary method for suppressing exceptions.
     * 
     * @param protocol
     * @param sender
     * @param name
     *            PacketType if available, null otherwise.
     * @return
     */
    public static PacketType findPacketTypeByName(Protocol protocol, Sender sender, String name) {
        try {
            return PacketType.findCurrent(protocol, sender, name);
        }
        catch (Throwable t) {
            // uh
            return null;
        }
    }

    // INSTANCE ----

    private static final List<PacketAdapter> registeredPacketAdapters = new LinkedList<PacketAdapter>();

    public ProtocolLibComponent(Plugin plugin) {
        register(plugin);
        /*
         * TODO: Register listeners iff any check is enabled - unregister from
         * EventRegistry with unregister.
         */
    }

    private void register(Plugin plugin) {
        final IWorldDataManager worldMan = NCPAPIProvider.getNoCheatPlusAPI().getWorldDataManager();
        if (!worldMan.isActiveAnywhere(CheckType.NET)) {
            StaticLog.logInfo("No packet level checks activated.");
            return;
        }
        StaticLog.logInfo("Adding packet level hooks for ProtocolLib (MC " + ProtocolLibrary.getProtocolManager().getMinecraftVersion().getVersion() + ")...");
        //Special purpose.
        if (ConfigManager.isTrueForAnyConfig(ConfPaths.NET + ConfPaths.SUB_DEBUG) || ConfigManager.isTrueForAnyConfig(ConfPaths.CHECKS_DEBUG) ) {
            // (Debug logging. Only activates if debug is set for checks or checks.net, not on the fly.)
            register("fr.neatmonster.nocheatplus.checks.net.protocollib.DebugAdapter", plugin);
        }
        // Actual checks.
        if (ServerVersion.compareMinecraftVersion("1.6.4") <= 0) {
            // Don't use this listener.
            NCPAPIProvider.getNoCheatPlusAPI().getLogManager().info(Streams.STATUS, "Disable EntityUseAdapter due to incompatibilities. Use fight.speed instead of net.attackfrequency.");
        }
        else if (worldMan.isActiveAnywhere(CheckType.NET_ATTACKFREQUENCY)) {
            // (Also sets lastKeepAliveTime, if enabled.)
            register("fr.neatmonster.nocheatplus.checks.net.protocollib.UseEntityAdapter", plugin);
        }
        if (worldMan.isActiveAnywhere(CheckType.NET_FLYINGFREQUENCY) || worldMan.isActiveAnywhere(CheckType.NET_MOVING)) {
            // (Also sets lastKeepAliveTime, if enabled.)
            register("fr.neatmonster.nocheatplus.checks.net.protocollib.MovingFlying", plugin);
            register("fr.neatmonster.nocheatplus.checks.net.protocollib.OutgoingPosition", plugin);
        }
        if (worldMan.isActiveAnywhere(CheckType.NET_KEEPALIVEFREQUENCY) || worldMan.isActiveAnywhere(CheckType.FIGHT_GODMODE)) {
            // (Set lastKeepAlive if this or fight.godmode is enabled.)
            register("fr.neatmonster.nocheatplus.checks.net.protocollib.KeepAliveAdapter", plugin);
        }
        if (worldMan.isActiveAnywhere(CheckType.NET_SOUNDDISTANCE)) {
            register("fr.neatmonster.nocheatplus.checks.net.protocollib.SoundDistance", plugin);
        }
        if (worldMan.isActiveAnywhere(CheckType.NET_WRONGTURN)) {
            register("fr.neatmonster.nocheatplus.checks.net.protocollib.WrongTurnAdapter", plugin);
        }
        if (ServerVersion.compareMinecraftVersion("1.9") < 0) {
            if (worldMan.isActiveAnywhere(CheckType.NET_PACKETFREQUENCY)) {
                register("fr.neatmonster.nocheatplus.checks.net.protocollib.CatchAllAdapter", plugin);
            }
        }
        if (ServerVersion.compareMinecraftVersion("1.8") >= 0) {
        	if (ConfigManager.isTrueForAnyConfig(ConfPaths.MOVING_SURVIVALFLY_EXTENDED_NOSLOW)) 
        	    register("fr.neatmonster.nocheatplus.checks.net.protocollib.NoSlow", plugin);
            register("fr.neatmonster.nocheatplus.checks.net.protocollib.Fight", plugin);
        }
        if (!registeredPacketAdapters.isEmpty()) {
            List<String> names = new ArrayList<String>(registeredPacketAdapters.size());
            for (PacketAdapter adapter : registeredPacketAdapters) {
                names.add(adapter.getClass().getSimpleName());
            }
            StaticLog.logInfo("Available (and activated) packet level hooks: " + StringUtil.join(names, " | "));
            NCPAPIProvider.getNoCheatPlusAPI().addFeatureTags("packet-listeners", names);
        } 
        else {
            StaticLog.logInfo("No packet level hooks activated.");
        }
    }

    @SuppressWarnings("unchecked")
    private void register(String name, Plugin plugin) {
        Throwable t = null;
        try {
            Class<?> clazz = Class.forName(name);
            register((Class<? extends PacketAdapter>) clazz, plugin);
            return;
        } catch (ClassNotFoundException e) {
            t = e;
        } catch (ClassCastException e) {
            t = e;
        }
        StaticLog.logWarning("Could not register packet level hook: " + name);
        StaticLog.logWarning(t);
    }

    private void register(Class<? extends PacketAdapter> clazz, Plugin plugin) {
        try {
            // Construct a new instance using reflection.
            PacketAdapter adapter = clazz.getDeclaredConstructor(Plugin.class).newInstance(plugin);
            ProtocolLibrary.getProtocolManager().addPacketListener(adapter);
            registeredPacketAdapters.add(adapter);
        } catch (Throwable t) {
            StaticLog.logWarning("Could not register packet level hook: " + clazz.getSimpleName());
            StaticLog.logWarning(t);
            if (t.getCause() != null) {
                StaticLog.logWarning(t.getCause());
            }
        }
    }

    @Override
    public void onDisable() {
        unregister();
    }

    @Override
    public void onReload() {
        unregister();
        NCPAPIProvider.getNoCheatPlusAPI().getPlayerDataManager().removeGenericInstance(NetData.class); // Currently needed for FlyingFrequency.
        register(Bukkit.getPluginManager().getPlugin("NoCheatPlus")); // TODO: static plugin getter?
    }

    private void unregister() {
        final ProtocolManager protocolManager = ProtocolLibrary.getProtocolManager();
        final NoCheatPlusAPI api = NCPAPIProvider.getNoCheatPlusAPI();
        for (PacketAdapter adapter : registeredPacketAdapters) {
            try {
                protocolManager.removePacketListener(adapter);
                api.removeComponent(adapter); // Bit heavy, but consistent.
            } catch (Throwable t) {
                StaticLog.logWarning("Failed to unregister packet level hook: " + adapter.getClass().getName());
            }// TODO Auto-generated method stub

        }
        registeredPacketAdapters.clear();
    }

    protected static void unregister(PacketAdapter adapter) {
        final ProtocolManager protocolManager = ProtocolLibrary.getProtocolManager();
        final NoCheatPlusAPI api = NCPAPIProvider.getNoCheatPlusAPI();
        try {
            protocolManager.removePacketListener(adapter);
            api.removeComponent(adapter); // Bit heavy, but consistent.
            registeredPacketAdapters.remove(adapter);
            List<String> names = new ArrayList<String>(registeredPacketAdapters.size());
            for (PacketAdapter adaptern : registeredPacketAdapters) {
                names.add(adaptern.getClass().getSimpleName());
            }
            api.setFeatureTags("packet-listeners", names);
            StaticLog.logInfo("Unregistered packet level hook:" + adapter.getClass().getName());
        } catch (Throwable t) {
            StaticLog.logWarning("Failed to unregister packet level hook: " + adapter.getClass().getName());
        }
    }

    @Override
    public void playerJoins(final ServerPlayer player) {
        if (!registeredPacketAdapters.isEmpty()) {
            DataManager.getGenericInstance(player, NetData.class).onJoin(player);
        }
    }

    @Override
    public void playerLeaves(final ServerPlayer player) {
        if (!registeredPacketAdapters.isEmpty()) {
            DataManager.getGenericInstance(player, NetData.class).onLeave(player);
        }
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = false)
    public void onPlayerRespawn(final ServerPlayerRespawnEvent event) {
        if (!registeredPacketAdapters.isEmpty()) {
            final ServerPlayer player = event.getPlayer();
            final NetData data = DataManager.getGenericInstance(player, NetData.class);
            data.onJoin(player);
            final Location loc = event.getRespawnLocation();
            data.teleportQueue.onTeleportEvent(loc.getX(), loc.getY(), loc.getZ(), loc.getYaw(), loc.getPitch());
        }
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.MONITOR)
    public void onPlayerTeleport(final ServerPlayerTeleportEvent event) {
        if (!registeredPacketAdapters.isEmpty()) {
            // TODO: Might move to MovingListener.
            // TODO: Might still add cancelled UNKNOWN events. TEST IT
            final Location to = event.getTo();
            if (to == null) {
                return;
            }
            final ServerPlayer player = event.getPlayer();
            final IPlayerData pData = DataManager.getPlayerData(player);
            final NetData data = pData.getGenericInstance(NetData.class);
            if (pData.isCheckActive(CheckType.NET_FLYINGFREQUENCY, player)) {
                // Register expected location for comparison with outgoing packets.
                data.teleportQueue.onTeleportEvent(to.getX(), to.getY(), to.getZ(), to.getYaw(), to.getPitch());
            }
            data.clearFlyingQueue();
        }
    }

}
