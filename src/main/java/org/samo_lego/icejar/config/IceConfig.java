package org.samo_lego.icejar.config;

import org.samo_lego.icejar.check.CheckType;

import java.util.HashMap;

public class IceConfig {
    private static class CheckConfig {

    }

    public HashMap<CheckType, CheckConfig> checkConfigs = new HashMap<>();
}
