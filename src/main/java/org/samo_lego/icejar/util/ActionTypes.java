package org.samo_lego.icejar.util;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.players.IpBanList;
import net.minecraft.server.players.IpBanListEntry;
import org.samo_lego.icejar.IceJar;
import org.samo_lego.icejar.check.Check;

import java.util.List;

public enum ActionTypes {
    BAN,
    KICK,
    NONE;

    private static final MutableComponent icejarPrefix = new TextComponent("[IceJar] ").withStyle(ChatFormatting.AQUA);

    public void execute(ServerPlayer pl, Check failedCheck) {
        // todo integrate Patbox's banhammer
        switch (this) {
            case KICK -> this.disconnectInStyle(pl);
            case BAN -> this.ban(pl, failedCheck);
            default -> {}
        }
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
        player.connection.disconnect(icejarPrefix.copy().append(new TextComponent(msg).withStyle(ChatFormatting.GREEN)));
    }


    private void disconnectInStyle(ServerPlayer player, String msg) {
        player.connection.disconnect(icejarPrefix.copy().append(new TextComponent(msg).withStyle(ChatFormatting.GREEN)));
    }
}
