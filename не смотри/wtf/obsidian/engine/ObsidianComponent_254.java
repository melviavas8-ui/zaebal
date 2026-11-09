package wtf.obsidian.engine;

import wtf.obsidian.*;
import wtf.obsidian.manager.*;
import wtf.obsidian.utils.*;
import wtf.obsidian.engine.*;
import wtf.obsidian.event.*;
import wtf.obsidian.commands.*;
import wtf.obsidian.settings.*;
import wtf.obsidian.hud.*;
import wtf.obsidian.modules.combat.*;
import wtf.obsidian.modules.movement.*;
import wtf.obsidian.modules.render.*;
import wtf.obsidian.modules.player.*;
import wtf.obsidian.modules.misc.*;
import wtf.obsidian.mixin.*;
import wtf.obsidian.Module;
import wtf.obsidian.event.Event;
























import com.mojang.blaze3d.systems.ProjectionType;
import com.mojang.blaze3d.systems.RenderSystem;
import java.awt.Color;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gl.Framebuffer;
import net.minecraft.client.gl.ShaderProgramKeys;
import net.minecraft.client.gl.SimpleFramebuffer;
import net.minecraft.client.render.BufferBuilder;
import net.minecraft.client.render.BufferRenderer;
import net.minecraft.client.render.Tessellator;
import net.minecraft.client.render.VertexFormats;
import net.minecraft.client.render.VertexFormat.DrawMode;
import net.minecraft.client.util.math.MatrixStack;
import org.joml.Matrix4f;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL30;

public class ObsidianComponent_254 {
   public static ObsidianComponent_254 instance;
   private static final MinecraftClient mc = MinecraftClient.getInstance();
   public Framebuffer glowBuffer;
   public int lastWidth;
   public int lastHeight;
   public float glowRadius = 10.0f;
   public float glowIntensity = 1.0f;
   public Color glowColor = Color.WHITE;
   public Matrix4f savedProjection;
   public int savedFbo;
   public static final int RINGS = 6;
   public static final int ANGLES_PER_RING = 12;

   public static ObsidianComponent_254 getInstance() {
      if (instance == null) {
         instance = new ObsidianComponent_254();
      }

      return instance;
   }
   public void checkFramebuffers() {
      int width = mc.getWindow().getFramebufferWidth();
      int height = mc.getWindow().getFramebufferHeight();
      if (this.glowBuffer == null || this.lastWidth != width || this.lastHeight != height) {
         if (this.glowBuffer != null) {
            this.glowBuffer.delete();
         }

         this.glowBuffer = new SimpleFramebuffer(width, height, false);
         this.lastWidth = width;
         this.lastHeight = height;
      }
   }

   public void begin(float radius, Color color) {
      this.begin(radius, 1.0f, color);
   }

   public void begin(float radius, float intensity, Color color) {
      this.checkFramebuffers();
      this.glowRadius = radius;
      this.glowIntensity = intensity;
      this.glowColor = color;
      this.savedProjection = new Matrix4f(RenderSystem.getProjectionMatrix());
      this.savedFbo = GL11.glGetInteger(36006);
      GL30.glBindFramebuffer(36160, this.glowBuffer.fbo);
      GL11.glViewport(0, 0, this.lastWidth, this.lastHeight);
      RenderSystem.clearColor(
         0.0f,
         0.0f,
         0.0f,
         0.0f
      );
      RenderSystem.clear(16384);
      RenderSystem.setProjectionMatrix(this.savedProjection, ProjectionType.ORTHOGRAPHIC);
   }

   public void end(MatrixStack matrices, ObsidianComponent_69 contentCallback) {
      GL30.glBindFramebuffer(36160, this.savedFbo);
      GL11.glViewport(
         0,
         0,
         mc.getWindow().getFramebufferWidth(),
         mc.getWindow().getFramebufferHeight()
      );
      RenderSystem.setProjectionMatrix(this.savedProjection, ProjectionType.ORTHOGRAPHIC);
      this.renderGlow(matrices);
      if (contentCallback != null) {
         contentCallback.render();
      }
   }
   public float gaussian(float x, float sigma) {
      return (float)Math.exp((double)(-(x * x) / (2.0f * sigma * sigma)));
   }
   public void renderGlow(MatrixStack matrices) {
      RenderSystem.enableBlend();
      RenderSystem.blendFunc(770, 1);
      RenderSystem.disableDepthTest();
      int width = mc.getWindow().getScaledWidth();
      int height = mc.getWindow().getScaledHeight();
      RenderSystem.setShaderTexture(0, this.glowBuffer.getColorAttachment());
      RenderSystem.setShader(ShaderProgramKeys.POSITION_TEX_COLOR);
      GL11.glTexParameteri(3553, 10241, 9729);
      GL11.glTexParameteri(3553, 10240, 9729);
      Matrix4f matrix = matrices.peek().getPositionMatrix();
      float r = (float)this.glowColor.getRed() / 255.0f;
      float g = (float)this.glowColor.getGreen() / 255.0f;
      float b = (float)this.glowColor.getBlue() / 255.0f;
      float baseAlpha = (float)this.glowColor.getAlpha() / 255.0f * this.glowIntensity;
      float sigma = this.glowRadius * 0.4000000059604645f;
      float[] ringWeights = new float[6];
      float totalWeight = 0.0f;

      for (int i = 0; i < 6; i++) {
         float distance = this.glowRadius * (float)(i + (1)) / 6.0f;
         ringWeights[i] = this.gaussian(distance, sigma);
         totalWeight += ringWeights[i];
      }

      for (int i = 0; i < 6; i++) {
         ringWeights[i] /= totalWeight;
      }

      BufferBuilder buffer = Tessellator.getInstance().begin(DrawMode.QUADS, VertexFormats.POSITION_TEXTURE_COLOR);

      for (int ring = 0; ring < (6); ring++) {
         float distance = this.glowRadius * (float)(ring + (1)) / 6.0f;
         float alpha = baseAlpha * ringWeights[ring] * 0.699999988079071f;
         if (!(alpha < 0.0010000000474974513f)) {
            alpha = Math.min(alpha, 1.0f);

            for (int angle = 0; angle < (12); angle++) {
               float a1 = (float)(
                     (double)angle
                        * 2.0
                        * 3.141592653589793
                  )
                  / 12.0f;
               float ox = (float)Math.cos((double)a1) * distance;
               float oy = (float)Math.sin((double)a1) * distance;
               buffer.vertex(matrix, ox, oy, 0.0f)
                  .texture(0.0f, 1.0f)
                  .color(r, g, b, alpha);
               buffer.vertex(matrix, ox, (float)height + oy, 0.0f)
                  .texture(0.0f, 0.0F)
                  .color(r, g, b, alpha);
               buffer.vertex(matrix, (float)width + ox, (float)height + oy, 0.0F)
                  .texture(1.0f, 0.0f)
                  .color(r, g, b, alpha);
               buffer.vertex(matrix, (float)width + ox, oy, 0.0f)
                  .texture(1.0f, 1.0f)
                  .color(r, g, b, alpha);
               if (ring > 0) {
                  float a2 = (float)(
                        ((double)angle + 0.5)
                           * 2.0
                           * 3.141592653589793
                     )
                     / 12.0f;
                  float innerDist = distance * 0.6000000238418579f;
                  float ox2 = (float)Math.cos((double)a2) * innerDist;
                  float oy2 = (float)Math.sin((double)a2) * innerDist;
                  float alpha2 = alpha * 0.5f;
                  buffer.vertex(matrix, ox2, oy2, 0.0f)
                     .texture(0.0f, 1.0f)
                     .color(r, g, b, alpha2);
                  buffer.vertex(matrix, ox2, (float)height + oy2, 0.0F)
                     .texture(0.0f, 0.0f)
                     .color(r, g, b, alpha2);
                  buffer.vertex(matrix, (float)width + ox2, (float)height + oy2, 0.0f)
                     .texture(1.0f, 0.0f)
                     .color(r, g, b, alpha2);
                  buffer.vertex(matrix, (float)width + ox2, oy2, 0.0f)
                     .texture(1.0f, 1.0f)
                     .color(r, g, b, alpha2);
               }
            }
         }
      }

      BufferRenderer.drawWithGlobalProgram(buffer.end());
      GL11.glTexParameteri(3553, 10241, 9728);
      GL11.glTexParameteri(3553, 10240, 9728);
      RenderSystem.setShaderTexture(0, 0);
      RenderSystem.defaultBlendFunc();
      RenderSystem.enableDepthTest();
      RenderSystem.disableBlend();
   }

   public static void startGlow(float radius, int color, ObsidianComponent_69 callback, MatrixStack matrices) {
      startGlow(radius, 1.0f, color, callback, matrices);
   }

   public static void startGlow(float radius, float intensity, int color, ObsidianComponent_69 callback, MatrixStack matrices) {
      int a = color >> (24) & (255);
      int r = color >> (16) & (255);
      int g = color >> 8 & 255;
      int b = color & (255);
      if (a == 0) {
         a = 255;
      }

      ObsidianComponent_254 glow = getInstance();
      glow.begin(radius, intensity, new Color(r, g, b, a));
      callback.render();
      glow.end(matrices, callback);
   }

   public void cleanup() {
      if (this.glowBuffer != null) {
         this.glowBuffer.delete();
         this.glowBuffer = null;
      }
   }
}
