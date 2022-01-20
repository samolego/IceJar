package org.samo_lego.icejar;

import net.fabricmc.fabric.api.command.v1.CommandRegistrationCallback;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.fabricmc.fabric.api.event.player.AttackEntityCallback;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.server.MinecraftServer;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.samo_lego.icejar.check.combat.CombatCheck;
import org.samo_lego.icejar.command.IceJarCommand;
import org.samo_lego.icejar.config.IceConfig;

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
		this.configFile = new File(FabricLoader.getInstance().getConfigDir() + "/icejar.json");
		this.config = IceConfig.loadConfigFile(this.configFile);

		//AttackBlockCallback.EVENT.register();
		AttackEntityCallback.EVENT.register(CombatCheck::performCheck);
		CommandRegistrationCallback.EVENT.register(IceJarCommand::register);
		ServerLifecycleEvents.SERVER_STARTED.register(IceJar::onServerStarted);
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
