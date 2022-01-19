package org.samo_lego.icejar.config;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.annotations.SerializedName;
import org.samo_lego.config2brigadier.IBrigadierConfigurator;
import org.samo_lego.icejar.IceJar;
import org.samo_lego.icejar.check.Check;
import org.samo_lego.icejar.check.CheckType;
import org.samo_lego.icejar.util.ActionTypes;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.samo_lego.icejar.IceJar.MOD_ID;
import static org.samo_lego.icejar.check.CheckCategory.reloadEnabledChecks;
import static org.samo_lego.icejar.check.CheckType.MOVEMENT_NOFALL;
import static org.slf4j.LoggerFactory.getLogger;

public class IceConfig implements IBrigadierConfigurator {


    private static final Gson GSON = new GsonBuilder()
                                        .setPrettyPrinting()
                                        .setLenient()
                                        .disableHtmlEscaping()
                                        .create();

    @SerializedName("default_check_configuration")
    public static final CheckConfig DEFAULT = new CheckConfig();

    public Combat combat = new Combat();

    public static class Combat {
        @SerializedName("max_survival_distance")
        public double maxSurvivalDistance = 5.2D;
    }
    public Movement movement = new Movement();
    public static class Movement {
        public double maxHorizontalDistance = 130.1D;
        public long timerThreshold = 250L;
        public double vehicleYThreshold = 0D;
    }

    public static class CheckConfig {
        public ActionTypes action;
        @SerializedName("flag_cooldown")
        public long cooldown;
        @SerializedName("attempts_to_flag")
        public int attemptsToFlag;
        @SerializedName("violation_increase")
        public double violationIncrease;
        @SerializedName("max_violation_level")
        public double maxViolationLevel;
        public boolean enabled;

        public CheckConfig() {
            this(500, 1, 1, -1, true);
        }
        public CheckConfig(long cooldown, int attemptsToFlag, double violationIncrease, double maxViolationLevel, boolean enabled) {
            this.cooldown = cooldown;
            this.attemptsToFlag = attemptsToFlag;
            this.violationIncrease = violationIncrease;
            this.maxViolationLevel = maxViolationLevel;
            this.enabled = enabled;
            this.action = ActionTypes.NONE;
        }
    }

    @SerializedName("check_configurations")
    public HashMap<CheckType, CheckConfig> checkConfigs = new HashMap<>(Map.of(
            MOVEMENT_NOFALL, new CheckConfig(500, 5, 1, -1, true)
    ));

    /**
     * Which messages should be used when kicking client on cheat attempts.
     * Messages are chosen randomly.
     */
    @SerializedName("kick_messages")
    public List<String> kickMessages = new ArrayList<>(Arrays.asList(
            "Only who dares wins!",
            "Bad Liar ...",
            "No risk it, no biscuit!",
            "Playing God? How about no?",
            "Who flies high falls low.",
            "If you cheat, you only cheat yourself.",
            "Hax bad.",
            "You better check your client. It seems to be lying.",
            "If you have great power, you should\n use it with even greater responsibility."
    ));

    public boolean trainMode = true;
    public boolean debug = true;

    /**
     * Loads config file.
     *
     * @param file file to load the language file from.
     * @return config object
     */
    public static IceConfig loadConfigFile(File file) {
        IceConfig config = null;
        if (file.exists()) {
            try (BufferedReader fileReader = new BufferedReader(
                    new InputStreamReader(new FileInputStream(file), StandardCharsets.UTF_8)
            )) {
                config = GSON.fromJson(fileReader, IceConfig.class);
            } catch (IOException e) {
                throw new RuntimeException(MOD_ID + " Problem occurred when trying to load config: ", e);
            }
        }
        if(config == null)
            config = new IceConfig();

        config.saveConfigFile(file);

        return config;
    }

    public static CheckConfig getCheckOptions(CheckType type) {
        return IceJar.getInstance().getConfig().checkConfigs.getOrDefault(type, DEFAULT);
    }

    public static CheckConfig getCheckOptions(Check check) {
        return getCheckOptions(check.getType());
    }

    /**
     * Saves the config to the given file.
     *
     * @param file file to save config to
     */
    public void saveConfigFile(File file) {
        try (Writer writer = new OutputStreamWriter(new FileOutputStream(file), StandardCharsets.UTF_8)) {
            GSON.toJson(this, writer);
        } catch (IOException e) {
            getLogger(MOD_ID).error("Problem occurred when saving config: " + e.getMessage());
        }
    }

    /**
     * Changes values of current object with reflection,
     * in order to keep the same object.
     * (that still allows in-game editing)
     */
    public void reload(File file) {
        IceConfig newConfig = loadConfigFile(file);
        this.checkConfigs = newConfig.checkConfigs;  // reloading by hand
        this.reload(newConfig);
        this.save();
        reloadEnabledChecks();
    }


    @Override
    public void save() {
        this.saveConfigFile(IceJar.getInstance().getConfigFile());
    }
}
