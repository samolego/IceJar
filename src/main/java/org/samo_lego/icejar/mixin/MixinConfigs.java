package org.samo_lego.icejar.mixin;

import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import net.fabricmc.loader.api.FabricLoader;
import org.objectweb.asm.tree.ClassNode;
import org.spongepowered.asm.mixin.extensibility.IMixinConfigPlugin;
import org.spongepowered.asm.mixin.extensibility.IMixinInfo;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.HashSet;
import java.util.List;
import java.util.Scanner;
import java.util.Set;

import static org.apache.logging.log4j.LogManager.getLogger;
import static org.samo_lego.icejar.IceJar.MOD_ID;

public class MixinConfigs implements IMixinConfigPlugin {

    private static final Set<String> SKIPPED_MIXINS = new HashSet<>();

    @Override
    public void onLoad(String mixinPackage) {

    }

    @Override
    public String getRefMapperConfig() {
        return null;
    }

    @Override
    public boolean shouldApplyMixin(String targetClassName, String mixinClassName) {
        return !SKIPPED_MIXINS.contains(mixinClassName);
    }

    @Override
    public void acceptTargets(Set<String> myTargets, Set<String> otherTargets) {
    }

    @Override
    public List<String> getMixins() {
        return null;
    }

    @Override
    public void preApply(String targetClassName, ClassNode targetClass, String mixinClassName, IMixinInfo mixinInfo) {
    }

    @Override
    public void postApply(String targetClassName, ClassNode targetClass, String mixinClassName, IMixinInfo mixinInfo) {
    }


    static {
        String path = FabricLoader.getInstance().getConfigDir() + "/" + MOD_ID + "/skipped_mixins.json";
        getLogger(MOD_ID).info("[IceJar] Loading skipped mixins from " + path);
        File file = new File(path);

        if (!file.exists()) {
            file.getParentFile().mkdirs();

            try {
                file.createNewFile();
            } catch (IOException e) {
                e.printStackTrace();
            }
        } else {
            // Read file content
            try (FileInputStream fis = new FileInputStream(file);
                 Scanner scanner = new Scanner(fis)) {
                StringBuilder sb = new StringBuilder();

                while (scanner.hasNextLine()) {
                    sb.append(scanner.nextLine());
                }

                JsonElement jsonElement = JsonParser.parseString(sb.toString());

                if (jsonElement.isJsonArray()) {
                    jsonElement.getAsJsonArray().forEach(jsonElement1 -> {
                        if (jsonElement1.isJsonPrimitive()) {
                            SKIPPED_MIXINS.add(jsonElement1.getAsString());
                        }
                    });
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}
