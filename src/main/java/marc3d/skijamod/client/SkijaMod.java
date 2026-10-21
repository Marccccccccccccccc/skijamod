package marc3d.skijamod.client;

import com.mojang.logging.LogUtils;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.loader.api.FabricLoader;
import net.fabricmc.loader.impl.launch.FabricLauncherBase;
import net.minecraft.client.Minecraft;
import net.minecraft.util.Util;
import org.slf4j.Logger;

import java.io.File;
import java.io.InputStream;
import java.net.URI;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.stream.Stream;

public class SkijaMod implements ClientModInitializer {
    public static final String MODID = "skijamod";
    public static final String SKIJA_VERSION = "0.143.17";
    public static final Minecraft mc = Minecraft.getInstance();
    public static final FabricLoader fl = FabricLoader.getInstance();
    public static final File FOLDER = new File(fl.getGameDir().toFile(), MODID);
    public static final Logger LOG = LogUtils.getLogger();

    @Override
    public void onInitializeClient() {
        if (fl.isModLoaded("connector")) {
            LOG.info("Connector detected, disabling SkijaMod");
            return;
        }
        LOG.info("Initializing SkijaMod");
        try {
            Path dir = new File(FOLDER, "libs").toPath();
            Files.createDirectories(dir);
            String file;
            Util.OS os = Util.getPlatform();
            String arch = System.getProperty("os.arch");
            boolean isArm = arch != null && (arch.contains("aarch64") || arch.contains("arm64"));

            //TODO: android?

            switch (os) {
                case WINDOWS -> file = "skija-windows-x64-" + SKIJA_VERSION + ".jar";
                case LINUX -> file = "skija-linux-x64-" + SKIJA_VERSION + ".jar";
                case OSX -> file = (isArm ? "skija-macos-arm64-" : "skija-macos-x64-") + SKIJA_VERSION + ".jar";
                default -> throw new RuntimeException("Unsupported OS: " + os.telemetryName());
            }

            try (Stream<Path> paths = Files.list(dir)) {
                paths.filter(Files::isRegularFile)
                        .filter(path -> {
                            String name = path.getFileName().toString();
                            return name.startsWith("skija-") && name.endsWith(".jar") && !name.equals(file);
                        })
                        .forEach(path -> {
                            try {
                                Files.deleteIfExists(path);
                            } catch (Exception e) {
                                LOG.warn("Failed to delete stale Skija jar {}", path, e);
                            }
                        });
            }

            Path path = dir.resolve(file);

            if (!Files.exists(path)) {
                String url = "https://repo1.maven.org/maven2/io/github/humbleui/" + file.replace("-" + SKIJA_VERSION + ".jar", "") + "/" + SKIJA_VERSION + "/" + file;
                LOG.info("Downloading {} from {}", file, url);
                try (InputStream in = new URI(url).toURL().openStream()) {
                    Files.copy(in, path);
                }
            }

            FabricLauncherBase.getLauncher().addToClassPath(path);
        } catch (Exception e) {
            throw new RuntimeException("Skija load failed", e);
        }
    }
}
