package org.samo_lego.icejar.event;

import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.fabricmc.fabric.api.entity.event.v1.ServerPlayerEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerChunkEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.fabricmc.fabric.api.event.player.AttackBlockCallback;
import net.fabricmc.fabric.api.event.player.AttackEntityCallback;
import net.fabricmc.fabric.api.event.player.UseBlockCallback;
import net.fabricmc.fabric.api.event.player.UseItemCallback;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.chunk.LevelChunk;
import org.samo_lego.icejar.IceJar;
import org.samo_lego.icejar.casts.IceJarPlayer;
import org.samo_lego.icejar.check.combat.CombatCheck;
import org.samo_lego.icejar.check.inventory.ItemUseCheck;
import org.samo_lego.icejar.check.world.block.BlockBreakCheck;
import org.samo_lego.icejar.check.world.block.BlockInteractCheck;
import org.samo_lego.icejar.command.IceJarCommand;
import org.samo_lego.icejar.module.NewChunks;

public class EventHandler {
    public EventHandler() {
        ServerChunkEvents.CHUNK_LOAD.register(this::onChunkLoad);

        // Register events
        AttackBlockCallback.EVENT.register(BlockBreakCheck::performCheck);
        UseBlockCallback.EVENT.register(BlockInteractCheck::performCheck);
        AttackEntityCallback.EVENT.register(CombatCheck::performCheck);
        UseItemCallback.EVENT.register(ItemUseCheck::performCheck);

        CommandRegistrationCallback.EVENT.register(IceJarCommand::register);
        ServerLifecycleEvents.SERVER_STARTED.register(IceJar::onServerStarted);

        // Copy data on dimension change etc.
        ServerPlayerEvents.COPY_FROM.register((old, newPl, _alive) -> ((IceJarPlayer) newPl).ij$copyFrom((IceJarPlayer) old));
    }

    private void onChunkLoad(ServerLevel serverLevel, LevelChunk levelChunk) {
        //System.out.println("Chunk loaded! needs saving: " + levelChunk.isUnsaved() + " is new: " + ((IJChunkAccess) levelChunk).ij_isNewChunk());
        //levelChunk.unpackTicks();
        if (NewChunks.NEW_CHUNKS.contains(levelChunk.getPos())) {
            System.out.println("Chunk @ " + levelChunk.getPos() + " is new!");
        }
    }
}
