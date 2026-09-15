package marc3d.skijamod.render;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.textures.GpuTexture;
import com.mojang.blaze3d.textures.GpuTextureView;
import io.github.humbleui.skija.Canvas;
import lombok.experimental.UtilityClass;
import net.minecraft.client.gui.render.GuiRenderer;
import net.minecraft.client.gui.render.TextureSetup;
import org.jetbrains.annotations.Contract;
import org.jspecify.annotations.NonNull;

import java.util.ArrayDeque;
import java.util.Queue;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Consumer;

import static marc3d.skijamod.client.SkijaMod.LOG;
@SuppressWarnings("unused")
@UtilityClass
public final class SkijaRenderer {

    private static final AtomicInteger UNIQUE_ID_COUNTER = new AtomicInteger();

    private static final Queue<Consumer<Canvas>> renderQueue = new ArrayDeque<>();

    @Contract(" -> new")
    public static @NonNull TextureSetup createSkijaTextureSetup() {
        return new TextureSetup(new DummySkijaTextureView(), null, null, null, null, null);
    }
    @Contract(value = "null -> false", pure = true)
    public static boolean isSkijaTextureSetup(TextureSetup setup) {
        return setup != null && setup.texure0() instanceof DummySkijaTextureView;
    }

    public static void drawDirect(Consumer<Canvas> action) {
        SkijaSurfaceManager.get().drawDirect(action);
    }

    public static void drawImmediate(Consumer<Canvas> action) {
        drawDirect(action);
    }

    public static void registerRenderAction(Consumer<Canvas> action) {
        renderQueue.add(action);
    }

    public static boolean onExecuteDraw(GuiRenderer.@NonNull Draw draw) {
        if (!isSkijaTextureSetup(draw.textureSetup)) {
            return false;
        }

        Consumer<Canvas> action = renderQueue.poll();
        if (action == null) {
            return true;
        }

        if (SkijaSurfaceManager.get().isVulkanBackend()) {
            try {
                RenderSystem.getDevice().createCommandEncoder().submit();
            } catch (Throwable t) {
                LOG.warn("pre-Skija submit failed: {}", t.getMessage());
            }
        }

        try {
            SkijaSurfaceManager.get().drawDirect(action);
        } catch (Throwable t) {
            LOG.warn("Skija draw failed: {}", t.getMessage());
        }
        return true;
    }

    public static void onRenderEnd() {
        renderQueue.clear();
    }

    public static void close() {
        onRenderEnd();
        SkijaSurfaceManager.get().close();
    }

    private static class DummySkijaTextureView extends GpuTextureView {
        private final int id = UNIQUE_ID_COUNTER.incrementAndGet();

        public DummySkijaTextureView() {
            super(null, 0, 1);
        }

        protected DummySkijaTextureView(GpuTexture texture, int baseMipLevel, int mipLevels) {
            super(texture, baseMipLevel, mipLevels);
        }

        @Contract(pure = true)
        @Override
        public void close() {
        }

        @Contract(pure = true)
        @Override
        public boolean isClosed() {
            return false;
        }

        @Contract(pure = true)
        @Override
        public int hashCode() {
            return id;
        }
    }
}
