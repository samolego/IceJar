package org.samo_lego.icejar;

import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.event.player.AttackEntityCallback;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.samo_lego.icejar.check.combat.Critical;

public class IceJar implements ModInitializer {

	public static final String MOD_ID = "icejar";

	public static final Logger LOGGER = LogManager.getLogger(MOD_ID);

	@Override
	public void onInitialize() {
		LOGGER.info("Loading IceJar ...");

		//AttackBlockCallback.EVENT.register();
		AttackEntityCallback.EVENT.register(Critical::performCheck);
	}
}
