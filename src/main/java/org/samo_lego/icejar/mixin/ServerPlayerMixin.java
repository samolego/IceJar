package org.samo_lego.icejar.mixin;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.game.ServerboundMovePlayerPacket;
import net.minecraft.network.protocol.game.ServerboundMoveVehiclePacket;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.phys.Vec3;
import org.samo_lego.icejar.IceJar;
import org.samo_lego.icejar.check.Check;
import org.samo_lego.icejar.check.CheckType;
import org.samo_lego.icejar.config.IceConfig;
import org.samo_lego.icejar.util.IceJarPlayer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.lang.reflect.InvocationTargetException;
import java.util.HashMap;
import java.util.Map;

import static org.samo_lego.icejar.check.CheckCategory.category2checks;

@Mixin(ServerPlayer.class)
public abstract class ServerPlayerMixin implements IceJarPlayer {

    @Unique
    private Map<Class<?>, Check> playerChecks = new HashMap<>();
    @Unique
    private final ServerPlayer player = (ServerPlayer) (Object) this;
    @Unique
    private boolean guiOpen = false;
    @Unique
    private boolean ij$onGround, wasOnGround, wasLastOnGround, aboveLiquid;
    @Unique
    private Vec3 vehicleMovement, lastVehicleMovement, lastMovement, movement;
    @Unique
    private double violationLevel;


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

                // Save check only if enabled
                IceConfig.CheckConfig cfg = IceJar.getInstance().getConfig().checkConfigs.get(check.getType());
                if (cfg != null ? cfg.enabled : IceJar.getInstance().getConfig().DEFAULT.enabled) {
                    this.playerChecks.put(checkClass, check);
                }
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
            final double prevLvl = check.getViolationLevel();
            final double newLvl = check.increaseViolationLevel();
            final double max = check.getMaxViolationLevel();
            if (newLvl > max && max > 0) {
                check.executeAction();
            } else {
                this.violationLevel += (newLvl - prevLvl) / category2checks.keySet().size();
                final IceConfig config = IceJar.getInstance().getConfig();
                final double maxLevel = config.violations.maxLevel;
                if (this.violationLevel > maxLevel && maxLevel > 0) {
                    // Execute action for global violation
                    config.violations.action.execute(this.player, check);
                    this.violationLevel = 0;
                }
            }

            IceJarPlayer.broadcast(this.player, check);

            check.setLastFlagTime(now);
            check.setCheatAttempts(0);
        }
    }

    @Override
    public void ij$setOpenGUI(boolean open) {
        this.guiOpen = open;
    }

    @Override
    public boolean ij$hasOpenGui() {
        return this.guiOpen;
    }

    @Override
    public boolean ij$nearGround() {
        return this.wasLastOnGround || this.wasOnGround || this.ij$onGround;
    }

    @Override
    public void ij$setOnGround(boolean ij$onGround) {
        this.ij$onGround = ij$onGround;
    }

    @Override
    public void ij$updateGroundStatus() {
        this.wasLastOnGround = this.wasOnGround;
        this.wasOnGround = this.ij$onGround;
    }

    @Override
    public void ij$setVehicleMovement(ServerboundMoveVehiclePacket packet) {
        this.lastVehicleMovement = this.vehicleMovement;
        this.vehicleMovement = new Vec3(packet.getX(), packet.getY(), packet.getZ());
    }

    @Override
    public Vec3 ij$getLastVehicleMovement() {
        return lastVehicleMovement;
    }

    @Override
    public Vec3 ij$getVehicleMovement() {
        return vehicleMovement;
    }

    @Override
    public void ij$setMovement(ServerboundMovePlayerPacket packet) {
        this.lastMovement = this.movement;
        this.movement = new Vec3(packet.getX(this.player.getX()), packet.getY(this.player.getY()), packet.getZ(this.player.getZ()));
    }

    @Override
    public Vec3 ij$getLastMovement() {
        return lastMovement;
    }

    @Override
    public Vec3 ij$getMovement() {
        return movement;
    }

    @Override
    public void ij$setAboveLiquid(boolean aboveLiquid) {
        this.aboveLiquid = aboveLiquid;
    }

    @Override
    public boolean ij$aboveLiquid() {
        return this.aboveLiquid;
    }

    @Override
    public void ij$copyFrom(IceJarPlayer oldPlayer) {
        this.violationLevel = oldPlayer.ij$getViolationLevel();
        this.playerChecks = oldPlayer.getCheckMap();
        this.playerChecks.forEach((checkType, check) -> check.setPlayer(this.player));
    }

    @Override
    public Map<Class<?>, Check> getCheckMap() {
        return this.playerChecks;
    }

    @Override
    public double ij$getViolationLevel() {
        return this.violationLevel;
    }

    @Inject(method = "addAdditionalSaveData", at = @At("TAIL"))
    private void onSave(CompoundTag nbt, CallbackInfo ci) {
        final CompoundTag data = new CompoundTag();
        data.putDouble("violationLevel", this.violationLevel);
        nbt.put("IceJar", data);
    }

    @Inject(method = "readAdditionalSaveData", at = @At("TAIL"))
    private void onLoad(CompoundTag nbt, CallbackInfo ci) {
        if (nbt.contains("IceJar")) {
            final CompoundTag data = nbt.getCompound("IceJar");
            this.violationLevel = data.getDouble("violationLevel");
        }
    }
}
