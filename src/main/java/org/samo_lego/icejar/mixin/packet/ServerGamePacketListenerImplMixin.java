package org.samo_lego.icejar.mixin.packet;

import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.network.protocol.game.ServerboundSignUpdatePacket;
import net.minecraft.network.protocol.game.ServerboundSwingPacket;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.network.ServerGamePacketListenerImpl;
import net.minecraft.server.network.TextFilter;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.SignBlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import org.samo_lego.icejar.check.combat.NoSwing;
import org.samo_lego.icejar.check.world.block.AutoSign;
import org.samo_lego.icejar.util.IceJarPlayer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;

import java.util.List;

import static org.samo_lego.icejar.check.CheckType.COMBAT_NOSWING;
import static org.samo_lego.icejar.check.CheckType.WORLD_BLOCK_AUTOSIGN;
import static org.samo_lego.icejar.mixin.accessor.ASignBlockEntity.FILTERED_TEXT_FIELD_NAMES;
import static org.samo_lego.icejar.mixin.accessor.ASignBlockEntity.RAW_TEXT_FIELD_NAMES;

@Mixin(ServerGamePacketListenerImpl.class)
public abstract class ServerGamePacketListenerImplMixin {

    @Shadow public ServerPlayer player;

    @Shadow public abstract void send(Packet<?> packet);

    @Inject(method = "handleAnimate", at = @At("TAIL"))
    private void onHandSwing(ServerboundSwingPacket packet, CallbackInfo ci) {
        if (COMBAT_NOSWING.isEnabled())
            ((IceJarPlayer) this.player).getCheck(NoSwing.class).onSwing();
    }

    @Inject(method = "updateSignText",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/world/level/block/entity/SignBlockEntity;isEditable()Z"),
            locals = LocalCapture.CAPTURE_FAILHARD,
            cancellable = true)
    private void onSignUpdate(ServerboundSignUpdatePacket packet, List<TextFilter.FilteredText> signText, CallbackInfo ci,
                              ServerLevel level, BlockPos pos, BlockState state, BlockEntity blockEntity) {
        if (WORLD_BLOCK_AUTOSIGN.isEnabled()) {
            if (!((IceJarPlayer) this.player).getCheck(AutoSign.class).allowPlace(packet)) {
                // Seems like Minecraft sends another update packet after this one, so it's cancelled out.
                // todo figure out a way to prevent the second packet / fake it.
                final ClientboundBlockEntityDataPacket fakeData = ClientboundBlockEntityDataPacket.create(blockEntity, be -> {
                    CompoundTag tag = new CompoundTag();

                    for (int i = 0; i < signText.size(); ++i) {
                        final TextFilter.FilteredText text = signText.get(i);

                        if (this.player.isTextFilteringEnabled()) {
                            String textFiltered = text.getFiltered();
                            tag.putString(FILTERED_TEXT_FIELD_NAMES()[i], Component.Serializer.toJson(new TextComponent(textFiltered)));
                        } else {
                            String textRaw = text.getRaw();
                            tag.putString(RAW_TEXT_FIELD_NAMES()[i], Component.Serializer.toJson(new TextComponent(textRaw)));
                        }

                    }

                    tag.putString("Color", ((SignBlockEntity) blockEntity).getColor().getName());
                    tag.putBoolean("GlowingText", ((SignBlockEntity) be).hasGlowingText());

                    return tag;
                });
                this.send(fakeData);
                ci.cancel();
            }
        }
    }
}
