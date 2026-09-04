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

public class SkijaModClient implements ClientModInitializer {
    public static final String MODID = "skijamod";
    public static final String SKIJA_VERSION = "0.119.6";
    public static final Minecraft mc = Minecraft.getInstance();
    public static final FabricLoader fl = FabricLoader.getInstance();
    public static final File FOLDER = new File(fl.getGameDir().toFile(), MODID);
    public static final Logger LOG = LogUtils.getLogger();

    @Override
    public void onInitializeClient() {
        try {
            Path dir = new File(FOLDER, "libs").toPath();
            Files.createDirectories(dir);
            String file;
            Util.OS os = Util.getPlatform();

            //TODO: maybe automatically remove old jar files

            switch (os) {
                case WINDOWS -> file = "skija-windows-x64-" + SKIJA_VERSION + ".jar";
                case LINUX -> file = "skija-linux-x64-" + SKIJA_VERSION + ".jar";
                case OSX -> file = "skija-macos-x64-" + SKIJA_VERSION + ".jar";
                //TODO: android?
                default -> throw new RuntimeException("Unsupported OS: " + os.telemetryName());
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
