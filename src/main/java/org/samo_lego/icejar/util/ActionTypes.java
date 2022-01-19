package org.samo_lego.icejar.util;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.server.level.ServerPlayer;
import org.samo_lego.icejar.IceJar;

import java.util.List;

public enum ActionTypes {
    BAN,
    KICK,
    NONE;

    private static final MutableComponent icejarPrefix = new TextComponent("[IceJar] ").withStyle(ChatFormatting.AQUA);
    public void execute(ServerPlayer pl) {
        // todo integrate Patbox's banhammer
        switch (this) {
            case KICK -> {
                final List<String> kickMessages = IceJar.getInstance().getConfig().kickMessages;
                final String msg = kickMessages.get(pl.getRandom().nextInt(kickMessages.size()));
                pl.sendMessage(icejarPrefix.copy().append(new TextComponent(msg).withStyle(ChatFormatting.GREEN)), pl.getUUID());
                //pl.connection.disconnect(icejarPrefix.copy().append(new TextComponent(msg).withStyle(ChatFormatting.GREEN)));
            }
            case BAN -> {
                pl.sendMessage(new TextComponent("You have been banned from this server."), pl.getUUID());
            }
            default -> {}
        }
    }
}
