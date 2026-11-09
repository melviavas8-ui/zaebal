package wtf.obsidian.utils;

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
























import com.mojang.blaze3d.systems.RenderSystem;
import lombok.Generated;
import net.minecraft.client.gl.Framebuffer;
import net.minecraft.client.gl.GlUniform;
import net.minecraft.client.gl.ShaderProgram;
import net.minecraft.client.gl.SimpleFramebuffer;
import net.minecraft.client.render.BufferBuilder;
import net.minecraft.client.render.BufferRenderer;
import net.minecraft.client.render.Tessellator;
import net.minecraft.client.render.VertexFormats;
import net.minecraft.client.render.VertexFormat.DrawMode;
import org.lwjgl.opengl.GL30;

public class ProjectionUtil implements IInstance {
   public static ProjectionUtil instance;
   public static Framebuffer buffer1;
   public static Framebuffer buffer2;
   public int lastWidth = -1;
   public int lastHeight = -1;
   public long lastUpdateTime = 0L;
   public boolean requestedThisFrame = true;
   public float blurOffset = 1.0f;
   public final int iterations = 4;

   public static ProjectionUtil getInstance() {
      if (instance == null) {
         instance = new ProjectionUtil();
      }

      return instance;
   }

   public void beginFrame() {
      boolean shouldDraw = this.requestedThisFrame;
      this.requestedThisFrame = false;
      if (shouldDraw) {
         this.draw();
      }
   }

   public void request() {
      this.requestedThisFrame = true;
   }
   public void draw() {
      long currentTime = System.currentTimeMillis();
      if (currentTime - this.lastUpdateTime >= (16L)) {
         this.lastUpdateTime = currentTime;
         int width = mc.getWindow().getFramebufferWidth();
         int height = mc.getWindow().getFramebufferHeight();
         if (buffer1 == null || buffer2 == null || this.lastWidth != width || this.lastHeight != height) {
            if (buffer1 != null) {
               buffer1.delete();
            }

            if (buffer2 != null) {
               buffer2.delete();
            }

            buffer1 = new SimpleFramebuffer(width, height, false);
            buffer2 = new SimpleFramebuffer(width, height, false);
            this.setLinearFiltering(buffer1);
            this.setLinearFiltering(buffer2);
            this.lastWidth = width;
            this.lastHeight = height;
         }

         RenderSystem.enableBlend();
         RenderSystem.defaultBlendFunc();
         ShaderProgram kawaseDown = mc.getShaderLoader().getOrCreateProgram(ObsidianComponent_257.kawaseDown);
         ShaderProgram kawaseUp = mc.getShaderLoader().getOrCreateProgram(ObsidianComponent_257.kawaseUp);
         buffer1.setClearColor(
            0.0f,
            0.0f,
            0.0f,
            0.0f
         );
         buffer1.clear();
         buffer1.beginWrite(true);
         RenderSystem.setShader(ObsidianComponent_257.kawaseDown);
         mc.getFramebuffer().beginRead();
         RenderSystem.setShaderTexture(0, mc.getFramebuffer().getColorAttachment());
         this.setKawaseUniforms(kawaseDown, width, height);
         this.drawQuad((float)mc.getWindow().getScaledWidth(), (float)mc.getWindow().getScaledHeight());
         mc.getFramebuffer().endRead();
         buffer1.endWrite();
         Framebuffer[] var10000 = new Framebuffer[2];
         var10000[0] = buffer1;
         var10000[1] = buffer2;
         Framebuffer[] buffers = var10000;

         for (int i = 1; i < 4; i++) {
            int srcIndex = (i + (1)) % (2);
            int dstIndex = i % (2);
            Framebuffer src = buffers[srcIndex];
            Framebuffer dst = buffers[dstIndex];
            dst.setClearColor(
               0.0f,
               0.0f,
               0.0f,
               0.0f
            );
            dst.clear();
            dst.beginWrite(true);
            RenderSystem.setShader(ObsidianComponent_257.kawaseDown);
            src.beginRead();
            RenderSystem.setShaderTexture(0, src.getColorAttachment());
            this.setKawaseUniforms(kawaseDown, src.textureWidth, src.textureHeight);
            this.drawQuad((float)mc.getWindow().getScaledWidth(), (float)mc.getWindow().getScaledHeight());
            src.endRead();
            dst.endWrite();
         }

         for (int i = 0; i < (4); i++) {
            int srcIndex = i % 2;
            int dstIndex = (i + 1) % 2;
            Framebuffer src = buffers[srcIndex];
            Framebuffer dst = buffers[dstIndex];
            dst.setClearColor(
               0.0f,
               0.0f,
               0.0f,
               0.0f
            );
            dst.clear();
            dst.beginWrite(true);
            RenderSystem.setShader(ObsidianComponent_257.kawaseUp);
            src.beginRead();
            RenderSystem.setShaderTexture(0, src.getColorAttachment());
            this.setKawaseUniforms(kawaseUp, src.textureWidth, src.textureHeight);
            this.drawQuad((float)mc.getWindow().getScaledWidth(), (float)mc.getWindow().getScaledHeight());
            src.endRead();
            dst.endWrite();
         }

         RenderSystem.disableBlend();
         mc.getFramebuffer().beginWrite(true);
         RenderSystem.setShaderTexture(0, 0);
      }
   }
   public void setLinearFiltering(Framebuffer framebuffer) {
      RenderSystem.bindTexture(framebuffer.getColorAttachment());
      GL30.glTexParameteri(3553, 10241, 9729);
      GL30.glTexParameteri(3553, 10240, 9729);
      RenderSystem.bindTexture(0);
   }
   public void setKawaseUniforms(ShaderProgram shader, int texWidth, int texHeight) {
      GlUniform resolutionUniform = shader.getUniform("Resolution");
      GlUniform offsetUniform = shader.getUniform("Offset");
      GlUniform saturationUniform = shader.getUniform("Saturation");
      GlUniform tintIntensityUniform = shader.getUniform("TintIntensity");
      GlUniform tintColorUniform = shader.getUniform("TintColor");
      if (resolutionUniform != null) {
         resolutionUniform.set(
            1.0f / (float)texWidth,
            1.0f / (float)texHeight
         );
      }

      if (offsetUniform != null) {
         offsetUniform.set(this.blurOffset);
      }

      if (saturationUniform != null) {
         saturationUniform.set(1.0f);
      }

      if (tintIntensityUniform != null) {
         tintIntensityUniform.set(0.0f);
      }

      if (tintColorUniform != null) {
         tintColorUniform.set(
            1.0f,
            1.0f,
            1.0f
         );
      }
   }
   public void drawQuad(float width, float height) {
      BufferBuilder builder = Tessellator.getInstance().begin(DrawMode.QUADS, VertexFormats.POSITION_TEXTURE_COLOR);
      builder.vertex(
            0.0f,
            0.0f,
            0.0f
         )
         .texture(0.0f, 1.0f)
         .color(
            1.0f,
            1.0f,
            1.0f,
            1.0f
         );
      builder.vertex(0.0f, height, 0.0f)
         .texture(0.0f, 0.0f)
         .color(
            1.0f,
            1.0f,
            1.0f,
            1.0f
         );
      builder.vertex(width, height, 0.0f)
         .texture(1.0f, 0.0f)
         .color(
            1.0f,
            1.0f,
            1.0f,
            1.0f
         );
      builder.vertex(width, 0.0f, 0.0f)
         .texture(1.0f, 1.0f)
         .color(
            1.0f,
            1.0f,
            1.0f,
            1.0f
         );
      BufferRenderer.drawWithGlobalProgram(builder.end());
   }

   public static int getTexture() {
      getInstance().request();
      return buffer1 != null ? buffer1.getColorAttachment() : 0;
   }

   @Generated
   public static Framebuffer getBuffer1() {
      return buffer1;
   }

   @Generated
   public static Framebuffer getBuffer2() {
      return buffer2;
   }

   @Generated
   public void setBlurOffset(float blurOffset) {
      this.blurOffset = blurOffset;
   }
}
