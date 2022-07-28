package org.samo_lego.icejar.util;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.players.IpBanList;
import net.minecraft.server.players.IpBanListEntry;
import org.apache.logging.log4j.core.lookup.StrSubstitutor;
import org.samo_lego.icejar.IceJar;
import org.samo_lego.icejar.check.Check;

import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public enum ActionTypes {
    BAN,
    KICK,
    NONE;

    private static final MutableComponent icejarPrefix = Component.literal("[IceJar]\n").withStyle(ChatFormatting.AQUA);

    public void execute(ServerPlayer pl, Check failedCheck) {
        switch (this) {
            case KICK -> this.disconnectInStyle(pl);
            case BAN -> this.ban(pl, failedCheck);
            default -> {}
        }

        if (failedCheck.getOptions().command != null) {
            this.executeCommand(pl, failedCheck);
        }
    }

    public void executeCommand(ServerPlayer player, String command) {
        Map<String, String> valuesMap = new HashMap<>();
        valuesMap.put("player", player.getGameProfile().getName());
        valuesMap.put("uuid", player.getGameProfile().getId().toString());
        valuesMap.put("ip", player.getIpAddress());
        StrSubstitutor sub = new StrSubstitutor(valuesMap);

        player.getServer().getCommands().performPrefixedCommand(player.getServer().createCommandSourceStack(),
                sub.replace(command));

    }

    private void executeCommand(ServerPlayer player, Check failedCheck) {
        Map<String, String> valuesMap = new HashMap<>();
        valuesMap.put("player", player.getGameProfile().getName());
        valuesMap.put("uuid", player.getGameProfile().getId().toString());
        valuesMap.put("ip", player.getIpAddress());
        valuesMap.put("check", failedCheck.getType().toString().toLowerCase(Locale.ROOT));
        StrSubstitutor sub = new StrSubstitutor(valuesMap);

        player.getServer().getCommands().performPrefixedCommand(player.getServer().createCommandSourceStack(),
                sub.replace(failedCheck.getOptions().command));

    }

    private void ban(ServerPlayer player, Check failedCheck) {
        IpBanList ipBanList = player.getServer().getPlayerList().getIpBans();
        final String ip = player.getIpAddress();
        if (ipBanList.isBanned(ip)) {
            List<ServerPlayer> list = player.getServer().getPlayerList().getPlayersWithAddress(ip);
            IpBanListEntry ipBanListEntry = new IpBanListEntry(ip, null, player.getGameProfile().getName(), null, "[IceJar] " + failedCheck.getType());
            ipBanList.add(ipBanListEntry);

            final List<String> kickMessages = IceJar.getInstance().getConfig().kickMessages;
            final String msg = kickMessages.get(player.getRandom().nextInt(kickMessages.size()));
            for (ServerPlayer serverPlayer : list) {
                this.disconnectInStyle(serverPlayer, msg);
            }
        }
    }

    private void disconnectInStyle(ServerPlayer player) {
        final List<String> kickMessages = IceJar.getInstance().getConfig().kickMessages;
        final String msg = kickMessages.get(player.getRandom().nextInt(kickMessages.size()));
        player.connection.disconnect(icejarPrefix.copy().append(Component.literal(msg).withStyle(ChatFormatting.GREEN)));
    }


    private void disconnectInStyle(ServerPlayer player, String msg) {
        player.connection.disconnect(icejarPrefix.copy().append(Component.literal(msg).withStyle(ChatFormatting.GREEN)));
    }
}
