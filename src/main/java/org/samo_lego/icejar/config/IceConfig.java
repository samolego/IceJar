package org.samo_lego.icejar.config;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.annotations.SerializedName;
import org.samo_lego.config2brigadier.IBrigadierConfigurator;
import org.samo_lego.icejar.IceJar;
import org.samo_lego.icejar.check.CheckType;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;

import static org.samo_lego.icejar.IceJar.MOD_ID;
import static org.samo_lego.icejar.check.CheckCategory.reloadEnabledChecks;
import static org.slf4j.LoggerFactory.getLogger;

public class IceConfig implements IBrigadierConfigurator {
    public static final CheckConfig DEFAULT = new CheckConfig();
    private static final Gson GSON = new GsonBuilder()
                                        .setPrettyPrinting()
                                        .setLenient()
                                        .disableHtmlEscaping()
                                        .create();

    public static class CheckConfig {
        public long cooldown = 0;
        public double violationIncrease = 1;
        public boolean enabled = true;
    }

    @SerializedName("WRITE_LIKE_THIS")
    public CheckConfig example = DEFAULT;

    public HashMap<CheckType, CheckConfig> checkConfigs = new HashMap<>();

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
        this.reload(newConfig);
        this.save();
        reloadEnabledChecks();
    }


    @Override
    public void save() {
        this.saveConfigFile(IceJar.getInstance().getConfigFile());
    }
}
