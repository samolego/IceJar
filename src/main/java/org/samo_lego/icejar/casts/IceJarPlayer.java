package org.samo_lego.icejar.casts;


import com.mojang.datafixers.util.Pair;
import me.lucko.fabric.api.permissions.v0.Permissions;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.ClickEvent;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.HoverEvent;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ServerboundMovePlayerPacket;
import net.minecraft.network.protocol.game.ServerboundMoveVehiclePacket;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.phys.Vec2;
import net.minecraft.world.phys.Vec3;
import org.samo_lego.icejar.IceJar;
import org.samo_lego.icejar.check.Check;
import org.samo_lego.icejar.check.CheckType;

import java.util.Locale;
import java.util.Map;

public interface IceJarPlayer {
    MutableComponent ID = Component.literal("[IJ] ").withStyle(ChatFormatting.AQUA);
    void flag(final Check check);

    <T extends Check> T getCheck(CheckType type);
    <T extends Check> T getCheck(Class<T> type);

    void ij$setOpenGUI(boolean open);
    boolean ij$hasOpenGui();

    boolean ij$nearGround();

    void ij$setOnGround(boolean ij$onGround);

    void ij$updateGroundStatus();

    void ij$setVehicleMovement(ServerboundMoveVehiclePacket packet);
    Vec3 ij$getLastVehicleMovement();
    Vec3 ij$getVehicleMovement();

    void ij$setMovement(ServerboundMovePlayerPacket packet);
    Vec3 ij$getLast2Movement();
    Vec3 ij$getLastMovement();
    Vec3 ij$getMovement();


    void ij$setRotation(ServerboundMovePlayerPacket packet);
    Vec2 ij$getLast2Rotation();
    Vec2 ij$getLastRotation();
    Vec2 ij$getRotation();

    void ij$setAboveLiquid(boolean aboveLiquid);
    boolean ij$aboveLiquid();

    void ij$copyFrom(IceJarPlayer oldPlayer);
    Map<Class<?>, Check> getCheckMap();

    double ij$getViolationLevel();

    static void broadcast(ServerPlayer player, Check failedCheck) {
        final String reportMessage = IceJar.getInstance().getConfig().violations.reportMessage;
        final CheckType type = failedCheck.getType();
        final MutableComponent additionalInfo = failedCheck.getAdditionalFlagInfo();

        final MutableComponent report = ID.copy()
                .withStyle(s -> s
                        .withHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, Component.translatable("Disable %s check for %s.",
                                Component.literal(type.toString().toLowerCase(Locale.ROOT)).withStyle(ChatFormatting.LIGHT_PURPLE),
                                player.getName().copy().withStyle(ChatFormatting.GREEN))))
                        .withClickEvent(new ClickEvent(ClickEvent.Action.SUGGEST_COMMAND, "/lp user " + player.getGameProfile().getName() + " permission set icejar.checks.bypass." + type.toString().toLowerCase(Locale.ROOT))))
                .append(Component.translatable(reportMessage,
                Component.literal(player.getGameProfile().getName())
                        .withStyle(ChatFormatting.GOLD),
                Component.literal(type.toString().toLowerCase())
                        .withStyle(ChatFormatting.YELLOW)
        )
        .withStyle(ChatFormatting.GRAY)
        .withStyle(s -> s
                .withClickEvent(new ClickEvent(ClickEvent.Action.SUGGEST_COMMAND, "/tp @s " + player.getGameProfile().getId().toString()))
                .withHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, Component.translatable("chat.coordinates.tooltip")))
        ))
        .append(Component.literal(" (hover)")
                        .withStyle(ChatFormatting.ITALIC)
                        .withStyle(ChatFormatting.BLUE)
                        .withStyle(s -> s
                            .withHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT,
                                    Component.translatable("Violation level: %s\nCheck violation level: %s",
                                            Component.literal(String.format("%.2f", ((IceJarPlayer) player).ij$getViolationLevel()))
                                                    .withStyle(ChatFormatting.GOLD),
                                            Component.literal(String.format("%.2f", failedCheck.getViolationLevel()))
                                                    .withStyle(ChatFormatting.YELLOW)
                                    )
                                    .append(additionalInfo.getString().isEmpty() ? "" : "\n")
                                    .append(additionalInfo)
                            )
                        )
        ));

        player.getServer().getPlayerList().getPlayers().forEach(p -> {
            if (Permissions.check(p, type.getReportPermission(), p.hasPermissions(4))) {
                p.sendSystemMessage(report);
            }
        });
    }

    Map<ChunkPos, Pair<Packet<?>, Integer>> ij_getDelayedPackets();
}
