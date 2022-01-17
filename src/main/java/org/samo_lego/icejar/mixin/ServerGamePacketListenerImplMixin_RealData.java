package org.samo_lego.icejar.mixin;

import net.minecraft.network.protocol.game.ServerboundMovePlayerPacket;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.network.ServerGamePacketListenerImpl;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.samo_lego.icejar.check.CheckType;
import org.samo_lego.icejar.mixin.accessor.AServerboundMovePlayerPacket;
import org.samo_lego.icejar.util.AdditionalData;
import org.samo_lego.icejar.util.IceJarPlayer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.List;

@Mixin(ServerGamePacketListenerImpl.class)
public final class ServerGamePacketListenerImplMixin_RealData {
    @Shadow
    public ServerPlayer player;

    /**
     * Checks the real onGround value of the movement packet.
     *
     * @param packet player movement packet.
     * @param ci   callback info.
     */
    @Inject(
            method = "handleMovePlayer",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/server/level/ServerPlayer;isPassenger()Z"
            )
    )
    private void checkOnGround(ServerboundMovePlayerPacket packet, CallbackInfo ci) {
        if (packet.isOnGround()) {
            final IceJarPlayer ijPlayer = (IceJarPlayer) this.player;
            final AdditionalData data = ijPlayer.getAdditionalData();

            data.updateGroundStatus();

            // Get bottom vehicle bounding box.
            Entity bottomEntity = player.getRootVehicle();
            if(bottomEntity == null) {
                bottomEntity = player;
            }
            final AABB bBox = bottomEntity
                    .getBoundingBox()
                    .inflate(0, 0.25005D, 0).move(0, packet.getY(player.getY()) - player.getY() - 0.25005D, 0);

            final Iterable<VoxelShape> collidingBlocks = player.getLevel().getBlockCollisions(bottomEntity, bBox);

            if(collidingBlocks.iterator().hasNext()) {
                // Preferring block collisions over entity ones
                data.setOnGround(true);
            } else {
                // No block collisions found, check for entity collisions
                Entity finalBottomEntity = bottomEntity;
                List<Entity> collidingEntities = player.getLevel().getEntities(bottomEntity, bBox, entity -> !finalBottomEntity.equals(entity));

                boolean noCollisions = collidingEntities.isEmpty();
                data.setOnGround(!noCollisions);

                if (noCollisions) {
                    final double fallDistance = packet.getY(player.getY()) - player.getY();
                    System.out.println("Fall distance: " + player.fallDistance);
                    data.setFallDistance(fallDistance);
                }
            }

            // Player isn't on ground but client packet says it is
            if(!ijPlayer.isNearGround()) {
                data.setOnGround(false);
                ((AServerboundMovePlayerPacket) packet).setOnGround(false);
                // Flag the player
                ijPlayer.flag(CheckType.MOVEMENT_NOFALL);
                System.out.println(player.fallDistance);
            }


        }
    }
}
