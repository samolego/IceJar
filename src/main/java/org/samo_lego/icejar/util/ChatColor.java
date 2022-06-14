package org.samo_lego.icejar.util;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;

public class ChatColor {

    /**
     * Returns a text for given boolean value.
     * @param b boolean value.
     * @return text.
     */
    public static MutableComponent styleBoolean(boolean b) {
        return Component.translatable("gui." + (b ? "yes" : "no")).withStyle(b ? ChatFormatting.GREEN : ChatFormatting.RED);
    }
}
