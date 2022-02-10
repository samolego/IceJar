package org.samo_lego.icejar;

import net.fabricmc.fabric.api.command.v1.CommandRegistrationCallback;
import net.fabricmc.fabric.api.entity.event.v1.ServerPlayerEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.fabricmc.fabric.api.event.player.AttackBlockCallback;
import net.fabricmc.fabric.api.event.player.AttackEntityCallback;
import net.fabricmc.fabric.api.event.player.UseBlockCallback;
import net.fabricmc.fabric.api.event.player.UseItemCallback;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.server.MinecraftServer;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.samo_lego.icejar.check.combat.CombatCheck;
import org.samo_lego.icejar.check.inventory.ItemUseCheck;
import org.samo_lego.icejar.check.world.block.BlockBreakCheck;
import org.samo_lego.icejar.check.world.block.BlockInteractCheck;
import org.samo_lego.icejar.command.IceJarCommand;
import org.samo_lego.icejar.config.IceConfig;
import org.samo_lego.icejar.util.IceJarPlayer;

import java.io.File;

import static org.samo_lego.icejar.check.CheckCategory.reloadEnabledChecks;

public class IceJar {

	public static final String MOD_ID = "icejar";

	public static final Logger LOGGER = LogManager.getLogger(MOD_ID);
	private static IceJar INSTANCE;
	private final File configFile;
	private final IceConfig config;
	private MinecraftServer server;

	public IceJar() {
		LOGGER.info("Loading IceJar ...");
		this.configFile = new File(FabricLoader.getInstance().getConfigDir() + "/" + MOD_ID + "/config.json");
		if (!this.configFile.exists()) {
			this.configFile.getParentFile().mkdirs();
		}
		this.config = IceConfig.loadConfigFile(this.configFile);


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

	private static void onServerStarted(MinecraftServer server) {
		INSTANCE.server = server;
	}

	public File getConfigFile() {
		return this.configFile;
	}

	public MinecraftServer getServer() {
		return this.server;
	}

	public IceConfig getConfig() {
		return this.config;
	}

	public static IceJar getInstance() {
		return INSTANCE;
	}

	public static void onInitialize() {
		INSTANCE = new IceJar();
		reloadEnabledChecks();
	}
}
