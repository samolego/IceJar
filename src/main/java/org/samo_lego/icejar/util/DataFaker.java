package org.samo_lego.icejar.util;

import net.minecraft.network.protocol.Packet;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;
import org.samo_lego.icejar.IceJar;

public class DataFaker {
    private static final float RADIOUS = 32.0f;

    /**
     * Sends a packet to everyone in radious of {@link #RADIOUS}.
     * @param except the player to not send the packet to.
     * @param player the player to send the packet from.
     * @param packet the packet to send.
     */
    public static void broadcast(@Nullable final Entity except, final ServerPlayer player, final Packet<?> packet) {
        broadcast(except, player.getPosition(1), player.getLevel().dimension(), packet);
    }

    public static void broadcast(@Nullable final Entity except, final Vec3 pos, final ResourceKey<Level> dimension, final Packet<?> packet) {
        Player targetPlayer = except instanceof Player ? (Player) except : null;
        IceJar.getInstance().getServer().getPlayerList().broadcast(targetPlayer,
                pos.x(),
                pos.y(),
                pos.z(),
                RADIOUS,
                dimension,
                packet);
    }

    /**
     * Sends sound to world around player.
     * @param sound the sound to play.
     * @param player the player to play the sound from.
     */
    public static void sendSound(final SoundEvent sound, final ServerPlayer player) {
        sendSound(sound, player.getPosition(1), player.getLevel(), player.getSoundSource());
    }

    public static void sendSound(final SoundEvent sound, final Vec3 pos, final Level world, final SoundSource source) {
        world.playSound(null,
                pos.x(),
                pos.y(),
                pos.z(),
                sound,
                source,
                1.0f,
                1.0f);
    }
}
