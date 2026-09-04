package marc3d.skijamod.render;

import org.jetbrains.annotations.ApiStatus;
import org.lwjgl.BufferUtils;
import org.lwjgl.opengl.*;

import java.nio.ByteBuffer;
import java.nio.FloatBuffer;

@ApiStatus.Internal
public class SkijaOpenGlState {
    private final int glVersion;
    private final Properties props = new Properties();

    public SkijaOpenGlState() {
        this.glVersion = 330;
    }

    public void push() {
        Properties p = props;

        GL11.glGetIntegerv(GL13.GL_ACTIVE_TEXTURE, p.lastActiveTexture);

        GL13.glActiveTexture(GL13.GL_TEXTURE0);
        GL11.glGetIntegerv(GL11.GL_TEXTURE_BINDING_2D, p.lastTexture0);

        GL13.glActiveTexture(GL13.GL_TEXTURE1);
        GL11.glGetIntegerv(GL11.GL_TEXTURE_BINDING_2D, p.lastTexture1);

        GL13.glActiveTexture(GL13.GL_TEXTURE0);

        GL11.glGetIntegerv(GL20.GL_CURRENT_PROGRAM, p.lastProgram);
        GL11.glGetIntegerv(GL13.GL_TEXTURE_BINDING_CUBE_MAP, p.lastTextureCube);
        GL11.glGetIntegerv(GL30.GL_FRAMEBUFFER_BINDING, p.lastFramebuffer);
        GL11.glGetIntegerv(GL30.GL_RENDERBUFFER_BINDING, p.lastRenderbuffer);

        if (glVersion >= 330 || GL.getCapabilities().GL_ARB_sampler_objects) {
            GL11.glGetIntegerv(GL33.GL_SAMPLER_BINDING, p.lastSampler);
        }

        GL11.glGetIntegerv(GL15.GL_ARRAY_BUFFER_BINDING, p.lastArrayBuffer);
        GL11.glGetIntegerv(GL15.GL_ELEMENT_ARRAY_BUFFER_BINDING, p.lastElementArrayBuffer);
        GL11.glGetIntegerv(GL31.GL_UNIFORM_BUFFER_BINDING, p.lastUniformBuffer);
        GL11.glGetIntegerv(GL21.GL_PIXEL_UNPACK_BUFFER_BINDING, p.lastPixelUnpackBufferBinding);
        GL11.glGetIntegerv(GL21.GL_PIXEL_PACK_BUFFER_BINDING, p.lastPixelPackBufferBinding);
        GL11.glGetIntegerv(GL30.GL_TRANSFORM_FEEDBACK_BUFFER_BINDING, p.lastTransformFeedbackBufferBinding);

        GL11.glGetIntegerv(GL30.GL_VERTEX_ARRAY_BINDING, p.lastVertexArrayObject);
        p.tempVao = GL30.glGenVertexArrays();
        GL30.glBindVertexArray(p.tempVao);

        GL11.glGetIntegerv(GL11.GL_POLYGON_MODE, p.lastPolygonMode);
        GL11.glGetIntegerv(GL11.GL_VIEWPORT, p.lastViewport);
        GL11.glGetIntegerv(GL11.GL_SCISSOR_BOX, p.lastScissorBox);
        GL11.glGetFloatv(GL11.GL_LINE_WIDTH, p.lastLineWidth);
        GL11.glGetFloatv(GL11.GL_COLOR_CLEAR_VALUE, p.lastClearColor);
        GL11.glGetFloatv(GL11.GL_DEPTH_RANGE, p.lastDepthRange);


        GL11.glGetIntegerv(GL14.GL_BLEND_SRC_RGB, p.lastBlendSrcRgb);
        GL11.glGetIntegerv(GL14.GL_BLEND_DST_RGB, p.lastBlendDstRgb);
        GL11.glGetIntegerv(GL14.GL_BLEND_SRC_ALPHA, p.lastBlendSrcAlpha);
        GL11.glGetIntegerv(GL14.GL_BLEND_DST_ALPHA, p.lastBlendDstAlpha);
        GL11.glGetIntegerv(GL20.GL_BLEND_EQUATION_RGB, p.lastBlendEquationRgb);
        GL11.glGetIntegerv(GL20.GL_BLEND_EQUATION_ALPHA, p.lastBlendEquationAlpha);

        p.lastEnabled[0] = GL11.glIsEnabled(GL11.GL_BLEND);
        p.lastEnabled[1] = GL11.glIsEnabled(GL11.GL_CULL_FACE);
        p.lastEnabled[2] = GL11.glIsEnabled(GL11.GL_DEPTH_TEST);
        p.lastEnabled[3] = GL11.glIsEnabled(GL11.GL_STENCIL_TEST);
        p.lastEnabled[4] = GL11.glIsEnabled(GL11.GL_SCISSOR_TEST);
        p.lastEnabled[5] = GL11.glIsEnabled(GL30.GL_RASTERIZER_DISCARD);
        p.lastEnabled[6] = GL11.glIsEnabled(GL32.GL_DEPTH_CLAMP);
        p.lastEnabled[7] = GL11.glIsEnabled(GL30.GL_FRAMEBUFFER_SRGB);
        p.lastEnabled[8] = GL11.glIsEnabled(GL32.GL_TEXTURE_CUBE_MAP_SEAMLESS);
        p.lastEnabled[9] = GL11.glIsEnabled(GL33.GL_SAMPLE_COVERAGE);
        p.lastEnabled[10] = GL11.glIsEnabled(GL33.GL_SAMPLE_ALPHA_TO_COVERAGE);
        p.lastEnabled[11] = GL11.glIsEnabled(GL33.GL_SAMPLE_ALPHA_TO_ONE);
        p.lastEnabled[12] = GL11.glIsEnabled(GL11.GL_COLOR_LOGIC_OP);
        p.lastEnabled[13] = GL11.glIsEnabled(GL11.GL_POLYGON_OFFSET_FILL);
        p.lastEnabled[14] = GL11.glIsEnabled(GL11.GL_LINE_SMOOTH);

        if (glVersion >= 310) {
            p.lastEnabled[15] = GL11.glIsEnabled(GL31.GL_PRIMITIVE_RESTART);
        }

        p.lastDepthMask = GL11.glGetBoolean(GL11.GL_DEPTH_WRITEMASK);
        p.lastColorMask.rewind();
        GL11.glGetBooleanv(GL11.GL_COLOR_WRITEMASK, p.lastColorMask);
        GL11.glGetIntegerv(GL11.GL_DEPTH_FUNC, p.lastDepthFunc);

        GL11.glGetIntegerv(GL11.GL_STENCIL_FUNC, p.lastStencilFunc);
        GL11.glGetIntegerv(GL11.GL_STENCIL_REF, p.lastStencilRef);
        GL11.glGetIntegerv(GL11.GL_STENCIL_VALUE_MASK, p.lastStencilValueMask);
        GL11.glGetIntegerv(GL11.GL_STENCIL_FAIL, p.lastStencilFail);
        GL11.glGetIntegerv(GL11.GL_STENCIL_PASS_DEPTH_FAIL, p.lastStencilPassDepthFail);
        GL11.glGetIntegerv(GL11.GL_STENCIL_PASS_DEPTH_PASS, p.lastStencilPassDepthPass);
        GL11.glGetIntegerv(GL11.GL_STENCIL_WRITEMASK, p.lastStencilWriteMask);

        GL11.glGetIntegerv(GL20.GL_STENCIL_BACK_FUNC, p.lastStencilBackFunc);
        GL11.glGetIntegerv(GL20.GL_STENCIL_BACK_REF, p.lastStencilBackRef);
        GL11.glGetIntegerv(GL20.GL_STENCIL_BACK_VALUE_MASK, p.lastStencilBackValueMask);
        GL11.glGetIntegerv(GL20.GL_STENCIL_BACK_FAIL, p.lastStencilBackFail);
        GL11.glGetIntegerv(GL20.GL_STENCIL_BACK_PASS_DEPTH_FAIL, p.lastStencilBackPassDepthFail);
        GL11.glGetIntegerv(GL20.GL_STENCIL_BACK_PASS_DEPTH_PASS, p.lastStencilBackPassDepthPass);
        GL11.glGetIntegerv(GL20.GL_STENCIL_BACK_WRITEMASK, p.lastStencilBackWriteMask);

        GL11.glGetFloatv(GL33.GL_SAMPLE_COVERAGE_VALUE, p.lastSampleCoverageValue);
        p.lastSampleCoverageInvert = GL11.glGetBoolean(GL33.GL_SAMPLE_COVERAGE_INVERT);
        GL11.glGetIntegerv(GL11.GL_LOGIC_OP_MODE, p.lastLogicOp);

        if (glVersion >= 320) {
            GL11.glGetIntegerv(GL32.GL_PROVOKING_VERTEX, p.lastProvokingVertex);
        }

        GL11.glGetIntegerv(GL11.GL_CULL_FACE_MODE, p.lastCullFaceMode);
        GL11.glGetIntegerv(GL11.GL_FRONT_FACE, p.lastFrontFace);
        GL11.glGetFloatv(GL11.GL_POLYGON_OFFSET_FACTOR, p.lastPolygonOffsetFactor);
        GL11.glGetFloatv(GL11.GL_POLYGON_OFFSET_UNITS, p.lastPolygonOffsetUnits);

        GL15.glBindBuffer(GL21.GL_PIXEL_UNPACK_BUFFER, 0);
        GL15.glBindBuffer(GL21.GL_PIXEL_PACK_BUFFER, 0);

        GL11.glGetIntegerv(GL11.GL_PACK_SWAP_BYTES, p.lastPackSwapBytes);
        GL11.glGetIntegerv(GL11.GL_PACK_LSB_FIRST, p.lastPackLsbFirst);
        GL11.glGetIntegerv(GL11.GL_PACK_ROW_LENGTH, p.lastPackRowLength);
        GL11.glGetIntegerv(GL11.GL_PACK_SKIP_PIXELS, p.lastPackSkipPixels);
        GL11.glGetIntegerv(GL11.GL_PACK_SKIP_ROWS, p.lastPackSkipRows);
        GL11.glGetIntegerv(GL11.GL_PACK_ALIGNMENT, p.lastPackAlignment);
        GL11.glGetIntegerv(GL12.GL_PACK_IMAGE_HEIGHT, p.lastPackImageHeight);
        GL11.glGetIntegerv(GL12.GL_PACK_SKIP_IMAGES, p.lastPackSkipImages);

        GL11.glGetIntegerv(GL11.GL_UNPACK_SWAP_BYTES, p.lastUnpackSwapBytes);
        GL11.glGetIntegerv(GL11.GL_UNPACK_LSB_FIRST, p.lastUnpackLsbFirst);
        GL11.glGetIntegerv(GL11.GL_UNPACK_ALIGNMENT, p.lastUnpackAlignment);
        GL11.glGetIntegerv(GL11.GL_UNPACK_ROW_LENGTH, p.lastUnpackRowLength);
        GL11.glGetIntegerv(GL11.GL_UNPACK_SKIP_PIXELS, p.lastUnpackSkipPixels);
        GL11.glGetIntegerv(GL11.GL_UNPACK_SKIP_ROWS, p.lastUnpackSkipRows);
        GL11.glGetIntegerv(GL12.GL_UNPACK_IMAGE_HEIGHT, p.lastUnpackImageHeight);
        GL11.glGetIntegerv(GL12.GL_UNPACK_SKIP_IMAGES, p.lastUnpackSkipImages);

        GL11.glPixelStorei(GL11.GL_UNPACK_ALIGNMENT, 1);
        GL11.glPixelStorei(GL11.GL_UNPACK_ROW_LENGTH, 0);
        GL11.glPixelStorei(GL11.GL_UNPACK_SKIP_PIXELS, 0);
        GL11.glPixelStorei(GL11.GL_UNPACK_SKIP_ROWS, 0);
    }

    public void pop() {
        Properties p = props;

        GL20.glUseProgram(p.lastProgram[0]);
        GL30.glBindFramebuffer(GL30.GL_FRAMEBUFFER, p.lastFramebuffer[0]);
        GL30.glBindRenderbuffer(GL30.GL_RENDERBUFFER, p.lastRenderbuffer[0]);

        GL13.glActiveTexture(GL13.GL_TEXTURE1);
        GL11.glBindTexture(GL11.GL_TEXTURE_2D, p.lastTexture1[0]);

        GL13.glActiveTexture(GL13.GL_TEXTURE0);
        GL11.glBindTexture(GL11.GL_TEXTURE_2D, p.lastTexture0[0]);

        GL11.glBindTexture(GL13.GL_TEXTURE_CUBE_MAP, p.lastTextureCube[0]);

        if (glVersion >= 330 || GL.getCapabilities().GL_ARB_sampler_objects) {
            GL33.glBindSampler(0, p.lastSampler[0]);
        }

        GL30.glBindVertexArray(p.lastVertexArrayObject[0]);
        if (p.tempVao != 0) {
            GL30.glDeleteVertexArrays(p.tempVao);
            p.tempVao = 0;
        }

        GL15.glBindBuffer(GL15.GL_ARRAY_BUFFER, p.lastArrayBuffer[0]);
        GL15.glBindBuffer(GL15.GL_ELEMENT_ARRAY_BUFFER, p.lastElementArrayBuffer[0]);
        GL15.glBindBuffer(GL31.GL_UNIFORM_BUFFER, p.lastUniformBuffer[0]);
        GL15.glBindBuffer(GL30.GL_TRANSFORM_FEEDBACK_BUFFER, p.lastTransformFeedbackBufferBinding[0]);

        GL20.glBlendEquationSeparate(p.lastBlendEquationRgb[0], p.lastBlendEquationAlpha[0]);
        GL14.glBlendFuncSeparate(
                p.lastBlendSrcRgb[0], p.lastBlendDstRgb[0],
                p.lastBlendSrcAlpha[0], p.lastBlendDstAlpha[0]
        );

        restoreEnabledState(p, 0, GL11.GL_BLEND);
        restoreEnabledState(p, 1, GL11.GL_CULL_FACE);
        restoreEnabledState(p, 2, GL11.GL_DEPTH_TEST);
        restoreEnabledState(p, 3, GL11.GL_STENCIL_TEST);
        restoreEnabledState(p, 4, GL11.GL_SCISSOR_TEST);
        restoreEnabledState(p, 5, GL30.GL_RASTERIZER_DISCARD);
        restoreEnabledState(p, 6, GL32.GL_DEPTH_CLAMP);
        restoreEnabledState(p, 7, GL30.GL_FRAMEBUFFER_SRGB);
        restoreEnabledState(p, 8, GL32.GL_TEXTURE_CUBE_MAP_SEAMLESS);
        restoreEnabledState(p, 9, GL33.GL_SAMPLE_COVERAGE);
        restoreEnabledState(p, 10, GL33.GL_SAMPLE_ALPHA_TO_COVERAGE);
        restoreEnabledState(p, 11, GL33.GL_SAMPLE_ALPHA_TO_ONE);
        restoreEnabledState(p, 12, GL11.GL_COLOR_LOGIC_OP);
        restoreEnabledState(p, 13, GL11.GL_POLYGON_OFFSET_FILL);
        restoreEnabledState(p, 14, GL11.GL_LINE_SMOOTH);

        if (glVersion >= 310) {
            restoreEnabledState(p, 15, GL31.GL_PRIMITIVE_RESTART);
        }

        GL11.glPolygonMode(GL11.GL_FRONT_AND_BACK, p.lastPolygonMode[0]);
        GL11.glViewport(p.lastViewport[0], p.lastViewport[1], p.lastViewport[2], p.lastViewport[3]);
        GL11.glScissor(p.lastScissorBox[0], p.lastScissorBox[1], p.lastScissorBox[2], p.lastScissorBox[3]);
        GL11.glLineWidth(p.lastLineWidth[0]);

        GL15.glBindBuffer(GL21.GL_PIXEL_PACK_BUFFER, p.lastPixelPackBufferBinding[0]);
        GL11.glPixelStorei(GL11.GL_PACK_SWAP_BYTES, p.lastPackSwapBytes[0]);
        GL11.glPixelStorei(GL11.GL_PACK_LSB_FIRST, p.lastPackLsbFirst[0]);
        GL11.glPixelStorei(GL11.GL_PACK_ROW_LENGTH, p.lastPackRowLength[0]);
        GL11.glPixelStorei(GL11.GL_PACK_SKIP_PIXELS, p.lastPackSkipPixels[0]);
        GL11.glPixelStorei(GL11.GL_PACK_SKIP_ROWS, p.lastPackSkipRows[0]);
        GL11.glPixelStorei(GL11.GL_PACK_ALIGNMENT, p.lastPackAlignment[0]);
        GL11.glPixelStorei(GL12.GL_PACK_IMAGE_HEIGHT, p.lastPackImageHeight[0]);
        GL11.glPixelStorei(GL12.GL_PACK_SKIP_IMAGES, p.lastPackSkipImages[0]);

        GL15.glBindBuffer(GL21.GL_PIXEL_UNPACK_BUFFER, p.lastPixelUnpackBufferBinding[0]);
        GL11.glPixelStorei(GL11.GL_UNPACK_SWAP_BYTES, p.lastUnpackSwapBytes[0]);
        GL11.glPixelStorei(GL11.GL_UNPACK_LSB_FIRST, p.lastUnpackLsbFirst[0]);
        GL11.glPixelStorei(GL11.GL_UNPACK_ALIGNMENT, p.lastUnpackAlignment[0]);
        GL11.glPixelStorei(GL11.GL_UNPACK_ROW_LENGTH, p.lastUnpackRowLength[0]);
        GL11.glPixelStorei(GL11.GL_UNPACK_SKIP_PIXELS, p.lastUnpackSkipPixels[0]);
        GL11.glPixelStorei(GL11.GL_UNPACK_SKIP_ROWS, p.lastUnpackSkipRows[0]);
        GL11.glPixelStorei(GL12.GL_UNPACK_IMAGE_HEIGHT, p.lastUnpackImageHeight[0]);
        GL11.glPixelStorei(GL12.GL_UNPACK_SKIP_IMAGES, p.lastUnpackSkipImages[0]);

        GL11.glDepthMask(p.lastDepthMask);
        GL11.glDepthFunc(p.lastDepthFunc[0]);
        GL11.glDepthRange(p.lastDepthRange.get(0), p.lastDepthRange.get(1));

        GL33.glStencilFuncSeparate(GL11.GL_FRONT, p.lastStencilFunc[0], p.lastStencilRef[0], p.lastStencilValueMask[0]);
        GL33.glStencilOpSeparate(GL11.GL_FRONT, p.lastStencilFail[0], p.lastStencilPassDepthFail[0], p.lastStencilPassDepthPass[0]);
        GL33.glStencilMaskSeparate(GL11.GL_FRONT, p.lastStencilWriteMask[0]);
        GL33.glStencilFuncSeparate(GL11.GL_BACK, p.lastStencilBackFunc[0], p.lastStencilBackRef[0], p.lastStencilBackValueMask[0]);
        GL33.glStencilOpSeparate(GL11.GL_BACK, p.lastStencilBackFail[0], p.lastStencilBackPassDepthFail[0], p.lastStencilBackPassDepthPass[0]);
        GL33.glStencilMaskSeparate(GL11.GL_BACK, p.lastStencilBackWriteMask[0]);

        GL33.glSampleCoverage(p.lastSampleCoverageValue[0], p.lastSampleCoverageInvert);
        GL11.glLogicOp(p.lastLogicOp[0]);

        if (glVersion >= 320) {
            GL32.glProvokingVertex(p.lastProvokingVertex[0]);
        }

        p.lastColorMask.rewind();
        GL11.glColorMask(
                p.lastColorMask.get(0) != 0,
                p.lastColorMask.get(1) != 0,
                p.lastColorMask.get(2) != 0,
                p.lastColorMask.get(3) != 0
        );

        GL11.glClearColor(p.lastClearColor.get(0), p.lastClearColor.get(1), p.lastClearColor.get(2), p.lastClearColor.get(3));


        GL11.glCullFace(p.lastCullFaceMode[0]);
        GL11.glFrontFace(p.lastFrontFace[0]);
        GL11.glPolygonOffset(p.lastPolygonOffsetFactor[0], p.lastPolygonOffsetUnits[0]);

        GL13.glActiveTexture(p.lastActiveTexture[0]);
    }

    private static void restoreEnabledState(Properties p, int index, int cap) {
        if (p.lastEnabled[index]) {
            GL11.glEnable(cap);
        } else {
            GL11.glDisable(cap);
        }
    }

    private static class Properties {
        final int[] lastActiveTexture = new int[1];
        final int[] lastProgram = new int[1];

        final int[] lastTexture0 = new int[1];
        final int[] lastTexture1 = new int[1];

        final int[] lastTextureCube = new int[1];
        final int[] lastFramebuffer = new int[1];
        final int[] lastRenderbuffer = new int[1];
        final int[] lastSampler = new int[1];
        final int[] lastArrayBuffer = new int[1];
        final int[] lastElementArrayBuffer = new int[1];
        final int[] lastUniformBuffer = new int[1];
        final int[] lastTransformFeedbackBufferBinding = new int[1];
        final int[] lastVertexArrayObject = new int[1];
        final int[] lastPolygonMode = new int[1];
        final int[] lastViewport = new int[4];
        final int[] lastScissorBox = new int[4];
        final float[] lastLineWidth = new float[1];
        final int[] lastBlendSrcRgb = new int[1];
        final int[] lastBlendDstRgb = new int[1];
        final int[] lastBlendSrcAlpha = new int[1];
        final int[] lastBlendDstAlpha = new int[1];
        final int[] lastBlendEquationRgb = new int[1];
        final int[] lastBlendEquationAlpha = new int[1];
        final ByteBuffer lastColorMask = BufferUtils.createByteBuffer(4);
        final FloatBuffer lastClearColor = BufferUtils.createFloatBuffer(4);
        final FloatBuffer lastDepthRange = BufferUtils.createFloatBuffer(2);
        final int[] lastCullFaceMode = new int[1];
        final int[] lastFrontFace = new int[1];
        final float[] lastPolygonOffsetFactor = new float[1];
        final float[] lastPolygonOffsetUnits = new float[1];
        final int[] lastDepthFunc = new int[1];
        final int[] lastStencilFunc = new int[1];
        final int[] lastStencilRef = new int[1];
        final int[] lastStencilValueMask = new int[1];
        final int[] lastStencilFail = new int[1];
        final int[] lastStencilPassDepthFail = new int[1];
        final int[] lastStencilPassDepthPass = new int[1];
        final int[] lastStencilWriteMask = new int[1];
        final int[] lastStencilBackFunc = new int[1];
        final int[] lastStencilBackRef = new int[1];
        final int[] lastStencilBackValueMask = new int[1];
        final int[] lastStencilBackFail = new int[1];
        final int[] lastStencilBackPassDepthFail = new int[1];
        final int[] lastStencilBackPassDepthPass = new int[1];
        final int[] lastStencilBackWriteMask = new int[1];
        final float[] lastSampleCoverageValue = new float[1];
        final int[] lastLogicOp = new int[1];
        final int[] lastProvokingVertex = new int[1];
        final int[] lastPixelUnpackBufferBinding = new int[1];
        final int[] lastPixelPackBufferBinding = new int[1];
        final int[] lastPackSwapBytes = new int[1];
        final int[] lastPackLsbFirst = new int[1];
        final int[] lastPackRowLength = new int[1];
        final int[] lastPackSkipPixels = new int[1];
        final int[] lastPackSkipRows = new int[1];
        final int[] lastPackAlignment = new int[1];
        final int[] lastPackImageHeight = new int[1];
        final int[] lastPackSkipImages = new int[1];
        final int[] lastUnpackSwapBytes = new int[1];
        final int[] lastUnpackLsbFirst = new int[1];
        final int[] lastUnpackAlignment = new int[1];
        final int[] lastUnpackRowLength = new int[1];
        final int[] lastUnpackSkipPixels = new int[1];
        final int[] lastUnpackSkipRows = new int[1];
        final int[] lastUnpackImageHeight = new int[1];
        final int[] lastUnpackSkipImages = new int[1];
        int tempVao = 0;
        final boolean[] lastEnabled = new boolean[16];
        boolean lastDepthMask;
        boolean lastSampleCoverageInvert;
    }
}