package marc3d.skijamod.render;

import com.mojang.blaze3d.GpuFormat;
import com.mojang.blaze3d.opengl.GlTexture;
import com.mojang.blaze3d.pipeline.RenderTarget;
import com.mojang.blaze3d.systems.RenderPass;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.textures.GpuTexture;
import com.mojang.blaze3d.vulkan.VulkanDevice;
import com.mojang.blaze3d.vulkan.VulkanGpuTexture;
import io.github.humbleui.skija.*;
import lombok.Getter;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL30;
import org.lwjgl.vulkan.VK;
import org.lwjgl.vulkan.VK10;
import org.lwjgl.vulkan.VK11;

import java.util.Optional;
import java.util.function.Consumer;

import static marc3d.skijamod.client.SkijaMod.LOG;
import static marc3d.skijamod.client.SkijaMod.mc;

public final class SkijaSurfaceManager {
    private static final SkijaOpenGlState glState = new SkijaOpenGlState();
    private static SkijaSurfaceManager instance;
    @Getter
    private int width = -1;
    @Getter
    private int height = -1;

    private DirectContext directContext;
    private BackendRenderTarget backendRenderTarget;
    private Surface surface;
    private int fboId = -1;
    private long lastVkImage = 0L;
    @Getter
    private boolean isGpuBackend = false;
    @Getter
    private RenderingBackend backendType = RenderingBackend.NONE;
    private SkijaSurfaceManager() {
    }

    public static SkijaSurfaceManager get() {
        if (instance == null) instance = new SkijaSurfaceManager();
        return instance;
    }

    public void draw(Consumer<Canvas> action, @NonNull RenderTarget main) {
        GpuTexture color = main.getColorTexture();
        if (color == null) return;

        int w = color.getWidth(0);
        int h = color.getHeight(0);
        if (w <= 0 || h <= 0) return;

        boolean isOpenGL = color instanceof GlTexture;

        if (isOpenGL) {
            glState.push();
        }

        try {
            ensureDirectSurface(color, w, h);
            if (surface == null) return;

            if (isGpuBackend && directContext != null) {
                if (isOpenGL) {
                    directContext.resetGLAll();
                } else {
                    directContext.resetAll();
                }
            }

            try {
                var canvas = surface.getCanvas();
                canvas.save();

                float guiScale = 1f;
                try {
                    guiScale = (float) mc.getWindow().getGuiScale();
                } catch (Throwable ignored) {

                }

                if (guiScale > 0.01f) {
                    canvas.scale(guiScale, guiScale);
                }

                try {
                    action.accept(canvas);
                } finally {
                    canvas.restore();
                }
            } catch (Exception e) {
                LOG.error(" Skija draw callback threw", e);
            }

            if (isGpuBackend && directContext != null) {
                if (backendType == RenderingBackend.VULKAN && surface != null) {
                    directContext.flushAndSubmit(surface, true);
                } else {
                    directContext.flushAndSubmit(false);
                }
            }
        } catch (Throwable t) {
            t.printStackTrace();
        } finally {
            if (isOpenGL) {
                glState.pop();
            }
        }
    }

    public void drawDirect(Consumer<Canvas> action) {
        RenderTarget main = mc.gameRenderer.mainRenderTarget();

        if (backendType == RenderingBackend.OPENGL) {
            assert main.getColorTextureView() != null;
            try (RenderPass _ = RenderSystem.getDevice().createCommandEncoder()
                    .createRenderPass(() -> "A", main.getColorTextureView(), Optional.empty())) {
                draw(action, main);
            }
        } else {
            draw(action, main);
        }
    }

    public boolean isVulkanBackend() {
        if (backendType == RenderingBackend.VULKAN) return true;
        try {
            var device = RenderSystem.getDevice();
            if (device.getClass().getName().toLowerCase().contains("vulkan")) {
                return true;
            }
        } catch (Throwable ignored) {
        }
        return false;
    }

    @SuppressWarnings("unused")
    public @Nullable Image snapshot() {
        if (surface == null) return null;
        try {
            return surface.makeImageSnapshot();
        } catch (Throwable t) {
            return null;
        }
    }

    public void close() {
        cleanupGpuResources();
        if (directContext != null) {
            directContext.close();
            directContext = null;
        }
        width = -1;
        height = -1;
        isGpuBackend = false;
        backendType = RenderingBackend.NONE;
        lastVkImage = 0L;
        fboId = -1;
    }

    private void ensureDirectSurface(GpuTexture color, int w, int h) {
        if (color instanceof GlTexture) {
            int currentFbo = GL11.glGetInteger(GL30.GL_FRAMEBUFFER_BINDING);
            if (surface != null
                    && !(backendType == RenderingBackend.OPENGL)
                    && currentFbo == fboId
                    && w == width
                    && h == height) {
                return;
            }
            cleanupGpuResources();
            width = w;
            height = h;
            tryWrapOpenGL(currentFbo, w, h);
            return;
        }

        if (color instanceof VulkanGpuTexture vkTexture) {
            long image = vkTexture.vkImage();
            if (surface != null
                    && backendType == RenderingBackend.VULKAN
                    && image == lastVkImage
                    && w == width
                    && h == height) {
                return;
            }
            cleanupGpuResources();
            width = w;
            height = h;
            lastVkImage = image;
            tryWrapVulkan(vkTexture, w, h);
        }
    }

    private void tryWrapOpenGL(int currentFbo, int w, int h) {
        try {
            fboId = currentFbo;

            backendRenderTarget = BackendRenderTarget.makeGL(
                    w, h,
                    0,
                    8,
                    fboId,
                    FramebufferFormat.GR_GL_RGBA8
            );

            if (directContext == null) {
                directContext = DirectContext.makeGL();
            }

            surface = Surface.wrapBackendRenderTarget(
                    directContext,
                    backendRenderTarget,
                    SurfaceOrigin.BOTTOM_LEFT,
                    ColorType.RGBA_8888,
                    ColorSpace.getSRGB(),
                    new SurfaceProps(PixelGeometry.UNKNOWN)
            );

            isGpuBackend = true;
            backendType = RenderingBackend.OPENGL;
        } catch (Throwable t) {
            LOG.warn(" OpenGL wrap failed: {}", t.getMessage());
            cleanupGpuResources();
        }
    }

    private void tryWrapVulkan(VulkanGpuTexture vkTexture, int w, int h) {
        try {
            VulkanDevice vkDevice = findVulkanDevice();
            if (vkDevice == null) {
                LOG.warn("Could not locate VulkanDevice");
                return;
            }

            long instanceAddr = vkDevice.instance().vkInstance().address();
            long physDevAddr = vkDevice.vkDevice().getPhysicalDevice().address();
            long devAddr = vkDevice.vkDevice().address();
            long queueAddr = vkDevice.graphicsQueue().vkQueue().address();
            int queueFamily = vkDevice.graphicsQueue().queueFamilyIndex();

            long getProcAddr = 0L;
            long getProcAddr2 = 0L;
            try {
                getProcAddr = VK.getFunctionProvider().getFunctionAddress("vkGetInstanceProcAddr");
                getProcAddr2 = vkDevice.vkDevice().getCapabilities().vkGetDeviceProcAddr;
                if (getProcAddr2 == 0L) {
                    getProcAddr2 = VK.getFunctionProvider().getFunctionAddress("vkGetDeviceProcAddr");
                }
            } catch (Throwable ignored) {

            }

            if (directContext == null && getProcAddr != 0L) {
                try {
                    directContext = DirectContext.makeVulkan(
                            instanceAddr, physDevAddr, devAddr, queueAddr, queueFamily,
                            getProcAddr, getProcAddr2, VK11.VK_API_VERSION_1_1
                    );
                } catch (Throwable t) {
                    t.printStackTrace();
                }
            }
            if (directContext == null) return;

            int vkFormat = toVulkanFormat(vkTexture.getFormat());
            int imageUsageFlags = toVulkanImageUsageFlags(vkTexture.usage());

            backendRenderTarget = BackendRenderTarget.makeVulkan(
                    w, h,
                    vkTexture.vkImage(),
                    VK10.VK_IMAGE_TILING_OPTIMAL,
                    VK10.VK_IMAGE_LAYOUT_COLOR_ATTACHMENT_OPTIMAL,
                    vkFormat,
                    imageUsageFlags,
                    1,
                    1
            );

            surface = Surface.wrapBackendRenderTarget(
                    directContext,
                    backendRenderTarget,
                    SurfaceOrigin.TOP_LEFT,
                    ColorType.RGBA_8888,
                    ColorSpace.getSRGB(),
                    new SurfaceProps(PixelGeometry.UNKNOWN)
            );

            isGpuBackend = true;
            backendType = RenderingBackend.VULKAN;
        } catch (Throwable t) {
            LOG.warn(" Vulkan wrap failed: {}", t.getMessage());
            cleanupGpuResources();
        }
    }

    private @Nullable VulkanDevice findVulkanDevice() {
        try {
            var gpuDevice = RenderSystem.getDevice().backend;
            if (RenderSystem.getDevice().backend instanceof VulkanDevice) {
                return (VulkanDevice) gpuDevice;
            }
        }
        catch (Throwable t) {
            t.printStackTrace();
        }
        return null;
    }

    private void cleanupGpuResources() {
        if (surface != null) {
            surface.close();
            surface = null;
        }
        if (backendRenderTarget != null) {
            backendRenderTarget.close();
            backendRenderTarget = null;
        }
        fboId = -1;
        lastVkImage = 0L;
        isGpuBackend = false;
        backendType = RenderingBackend.NONE;
    }

    private static int toVulkanFormat(GpuFormat format) {
        if (format == null) {
            return VK10.VK_FORMAT_R8G8B8A8_UNORM;
        }

        return switch (format) {
            case R8_UNORM -> VK10.VK_FORMAT_R8_UNORM;
            case RG8_UNORM -> VK10.VK_FORMAT_R8G8_UNORM;
            case RGBA8_UNORM -> VK10.VK_FORMAT_R8G8B8A8_UNORM;
            case R16_FLOAT -> VK10.VK_FORMAT_R16_SFLOAT;
            case RG16_FLOAT -> VK10.VK_FORMAT_R16G16_SFLOAT;
            case RGBA16_FLOAT -> VK10.VK_FORMAT_R16G16B16A16_SFLOAT;
            case R32_FLOAT -> VK10.VK_FORMAT_R32_SFLOAT;
            case RG32_FLOAT -> VK10.VK_FORMAT_R32G32_SFLOAT;
            case RGBA32_FLOAT -> VK10.VK_FORMAT_R32G32B32A32_SFLOAT;
            case RGB10A2_UNORM -> VK10.VK_FORMAT_A2B10G10R10_UNORM_PACK32;
            default -> VK10.VK_FORMAT_R8G8B8A8_UNORM;
        };
    }

    private static int toVulkanImageUsageFlags(int usage) {
        int flags = 0;

        if ((usage & GpuTexture.USAGE_COPY_SRC) != 0) {
            flags |= VK10.VK_IMAGE_USAGE_TRANSFER_SRC_BIT;
        }
        if ((usage & GpuTexture.USAGE_COPY_DST) != 0) {
            flags |= VK10.VK_IMAGE_USAGE_TRANSFER_DST_BIT;
        }
        if ((usage & GpuTexture.USAGE_TEXTURE_BINDING) != 0) {
            flags |= VK10.VK_IMAGE_USAGE_SAMPLED_BIT;
        }
        if ((usage & GpuTexture.USAGE_RENDER_ATTACHMENT) != 0) {
            flags |= VK10.VK_IMAGE_USAGE_COLOR_ATTACHMENT_BIT;
        }

        if (flags == 0) {
            flags = VK10.VK_IMAGE_USAGE_TRANSFER_SRC_BIT
                    | VK10.VK_IMAGE_USAGE_TRANSFER_DST_BIT
                    | VK10.VK_IMAGE_USAGE_SAMPLED_BIT
                    | VK10.VK_IMAGE_USAGE_COLOR_ATTACHMENT_BIT;
        }

        return flags;
    }
}