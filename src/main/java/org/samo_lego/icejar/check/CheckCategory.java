package org.samo_lego.icejar.check;

import org.samo_lego.icejar.IceJar;
import org.samo_lego.icejar.config.IceConfig;

import java.util.HashMap;
import java.util.Set;

public enum CheckCategory {
    COMBAT,
    FIXED_MOVEMENT,
    MOVEMENT,
    PACKETS,
    VEHICLE_MOVEMENT,
    WORLD_BLOCK_BREAK,
    WORLD_BLOCK_INTERACT,
    ENTITY_INTERACT;

    public static final HashMap<CheckCategory, Set<CheckType>> ALL_CHECKS = new HashMap<>();
    public static HashMap<CheckCategory, Set<CheckType>> category2checks = new HashMap<>();

    public static void reloadEnabledChecks() {
        final HashMap<CheckType, IceConfig.CheckConfig> checkConfigs = IceJar.getInstance().getConfig().checkConfigs;
        category2checks = new HashMap<>(ALL_CHECKS);
        for (Set<CheckType> checks : category2checks.values()) {
            // Remove disabled checks
            checks.removeIf(c -> {
                IceConfig.CheckConfig checkConfig = checkConfigs.get(c);
                if (checkConfig != null) {
                    return !checkConfig.enabled;
                }
                return false;
            });
        }
    }
}
