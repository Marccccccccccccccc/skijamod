package marc3d.skijamod.mixin.gui;

import com.mojang.blaze3d.buffers.GpuBufferSlice;
import com.mojang.blaze3d.pipeline.RenderTarget;
import com.mojang.blaze3d.systems.RenderPass;
import com.mojang.blaze3d.systems.RenderSystem;
import marc3d.skijamod.render.SkijaRenderer;
import net.minecraft.client.gui.render.GuiRenderer;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.List;
import java.util.Optional;
import java.util.OptionalDouble;
import java.util.function.Supplier;

@Mixin(GuiRenderer.class)
public abstract class MixinGuiRenderer {

    @Shadow
    @Final
    private List<GuiRenderer.Draw> draws;

    @Shadow
    private void executeDraw(GuiRenderer.Draw draw, RenderPass renderPass) {
    }

    @Inject(method = "executeDrawRange", at = @At("HEAD"), cancellable = true)
    private void skijamod$executeDrawRange(
            Supplier<String> label,
            RenderTarget mainRenderTarget,
            GpuBufferSlice dynamicTransforms,
            int startIndex,
            int endIndex,
            CallbackInfo ci
    ) {

        if (startIndex >= endIndex) return;

        boolean hasSkija = false;
        for (int i = startIndex; i < endIndex; i++) {
            if (SkijaRenderer.isSkijaTextureSetup(this.draws.get(i).textureSetup())) {
                hasSkija = true;
                break;
            }
        }
        if (!hasSkija) {
            return;
        }

        ci.cancel();

        int segmentStart = startIndex;
        for (int i = startIndex; i < endIndex; i++) {
            GuiRenderer.Draw draw = this.draws.get(i);
            if (SkijaRenderer.isSkijaTextureSetup(draw.textureSetup())) {
                if (segmentStart < i) {
                    skijamod$runPass(label, mainRenderTarget, dynamicTransforms, segmentStart, i);
                }
                SkijaRenderer.onExecuteDraw(draw);
                segmentStart = i + 1;
            }
        }
        if (segmentStart < endIndex) {
            skijamod$runPass(label, mainRenderTarget, dynamicTransforms, segmentStart, endIndex);
        }
    }

    @Unique
    private void skijamod$runPass(
            Supplier<String> label,
            RenderTarget mainRenderTarget,
            GpuBufferSlice dynamicTransforms,
            int startIndex,
            int endIndex) {
        try (RenderPass renderPass = RenderSystem.getDevice().createCommandEncoder().createRenderPass(
                label,
                mainRenderTarget.getColorTextureView(),
                Optional.empty(),
                mainRenderTarget.useDepth ? mainRenderTarget.getDepthTextureView() : null,
                OptionalDouble.empty())
        ) {
            RenderSystem.bindDefaultUniforms(renderPass);
            renderPass.setUniform("DynamicTransforms", dynamicTransforms);
            for (int i = startIndex; i < endIndex; i++) {
                this.executeDraw(this.draws.get(i), renderPass);
            }
        }
    }

    @Inject(method = "render", at = @At("RETURN"))
    private void onRenderReturn(CallbackInfo ci) {
        SkijaRenderer.onRenderEnd();
    }
}
