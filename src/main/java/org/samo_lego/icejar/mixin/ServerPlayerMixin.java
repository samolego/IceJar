package org.samo_lego.icejar.mixin;

import net.minecraft.Util;
import net.minecraft.network.chat.ChatType;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.network.protocol.game.ServerboundMovePlayerPacket;
import net.minecraft.network.protocol.game.ServerboundMoveVehiclePacket;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.phys.Vec3;
import org.samo_lego.icejar.check.Check;
import org.samo_lego.icejar.check.CheckType;
import org.samo_lego.icejar.util.IceJarPlayer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;

import java.lang.reflect.InvocationTargetException;
import java.util.HashMap;
import java.util.Map;

@Mixin(ServerPlayer.class)
public abstract class ServerPlayerMixin implements IceJarPlayer {

    @Unique
    private Map<Class<?>, Check> playerChecks = new HashMap<>();
    @Unique
    private final ServerPlayer player = (ServerPlayer) (Object) this;
    @Unique
    private boolean guiOpen = false;
    @Unique
    private boolean ij$onGround;
    @Unique
    private boolean wasOnGround;
    @Unique
    private boolean wasLastOnGround;
    @Unique
    private Vec3 vehicleMovement;
    @Unique
    private Vec3 lastVehicleMovement;
    @Unique
    private Vec3 lastMovement;
    @Unique
    private Vec3 movement;
    @Unique
    private boolean aboveLiquid;

    @Override
    public <T extends Check> T getCheck(CheckType checkType) {
        return this.getCheck(checkType.getCheckClass());
    }

    @SuppressWarnings("unchecked")
    @Override
    public <T extends Check> T getCheck(Class<T> checkClass) {
        T check = (T) this.playerChecks.get(checkClass);
        if (check == null) {
            // Create new check from type
            try {
                check = checkClass.getConstructor(ServerPlayer.class).newInstance(this.player);
                this.playerChecks.put(checkClass, check);
            } catch (InstantiationException | IllegalAccessException | InvocationTargetException | NoSuchMethodException e) {
                e.printStackTrace();
            }
        }

        return check;
    }

    @Override
    public void flag(final Check check) {
        // See if cooldown is still active on check
        final long now  = System.currentTimeMillis();
        final long timeDelta = now - check.getLastFlagTime();

        if (check.getCooldown() < timeDelta) {
            final double newLvl = check.increaseViolationLevel();
            final double max = check.getMaxViolationLevel();
            if (newLvl > max && max > 0) {
                check.executeAction();
            }
            this.player.getServer().getPlayerList().broadcastMessage(new TextComponent(this.player.getGameProfile().getName() + " was flagged for " + check.getType()),  ChatType.SYSTEM, Util.NIL_UUID);
            check.setLastFlagTime(now);
            check.setCheatAttempts(0);
        }
    }

    @Override
    public void ij$setOpenGUI(boolean open) {
        this.guiOpen = true;
    }

    @Override
    public boolean ij$hasOpenGui() {
        return this.guiOpen;
    }

    @Override
    public boolean isNearGround() {
        return this.wasLastOnGround || this.wasOnGround || this.ij$onGround;
    }

    @Override
    public void ij$setOnGround(boolean ij$onGround) {
        this.ij$onGround = ij$onGround;
    }

    @Override
    public void updateGroundStatus() {
        this.wasLastOnGround = this.wasOnGround;
        this.wasOnGround = this.ij$onGround;
    }

    @Override
    public void setVehicleMovement(ServerboundMoveVehiclePacket packet) {
        this.lastVehicleMovement = this.vehicleMovement;
        this.vehicleMovement = new Vec3(packet.getX(), packet.getY(), packet.getZ());
    }

    @Override
    public Vec3 getLastVehicleMovement() {
        return lastVehicleMovement;
    }

    @Override
    public Vec3 getVehicleMovement() {
        return vehicleMovement;
    }

    @Override
    public void setMovement(ServerboundMovePlayerPacket packet) {
        this.lastMovement = this.movement;
        this.movement = new Vec3(packet.getX(this.player.getX()), packet.getY(this.player.getY()), packet.getZ(this.player.getZ()));
    }

    @Override
    public Vec3 getLastMovement() {
        return lastMovement;
    }

    @Override
    public Vec3 getMovement() {
        return movement;
    }

    @Override
    public void setAboveLiquid(boolean aboveLiquid) {
        this.aboveLiquid = aboveLiquid;
    }

    @Override
    public boolean aboveLiquid() {
        return this.aboveLiquid;
    }

    @Override
    public void copyFrom(IceJarPlayer oldPlayer) {
        this.playerChecks = oldPlayer.getCheckMap();
    }

    @Override
    public Map<Class<?>, Check> getCheckMap() {
        return this.playerChecks;
    }
}
