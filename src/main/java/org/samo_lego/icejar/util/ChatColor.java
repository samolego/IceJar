package org.samo_lego.icejar.util;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.TranslatableComponent;

public class ChatColor {

    /**
     * Returns a text for given boolean value.
     * @param b boolean value.
     * @return text.
     */
    public static MutableComponent styleBoolean(boolean b) {
        return new TranslatableComponent("gui." + (b ? "yes" : "no")).withStyle(b ? ChatFormatting.GREEN : ChatFormatting.RED);
    }
}
