package wtf.obsidian.modules.render;

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

























import com.mojang.blaze3d.platform.GlStateManager.DstFactor;
import com.mojang.blaze3d.platform.GlStateManager.SrcFactor;
import com.mojang.blaze3d.systems.RenderSystem;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.client.gl.Framebuffer;
import net.minecraft.client.gl.GlUniform;
import net.minecraft.client.gl.ShaderProgram;
import net.minecraft.client.gl.SimpleFramebuffer;
import net.minecraft.client.render.BufferBuilder;
import net.minecraft.client.render.BufferRenderer;
import net.minecraft.client.render.Tessellator;
import net.minecraft.client.render.VertexFormats;
import net.minecraft.client.render.VertexFormat.DrawMode;
import net.minecraft.entity.Entity;
import net.minecraft.entity.ItemEntity;
import net.minecraft.entity.decoration.EndCrystalEntity;
import net.minecraft.entity.player.PlayerEntity;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL30;


public class ShaderEspModule extends Module {
   public static ShaderEspModule INSTANCE = new ShaderEspModule();
   public static final float EPSILON = 0.001F;
   public static final long OUTLINE_RETRY_DELAY_MS = 3000L;
   public static final double MAX_RANGE = 256.0;
   public static final float FILL_ALPHA = 0.7F;
   public static final int FILL_MIN_ITERATIONS = 2;
   public static final float GLOW_VALUE = 0.55F;
   public static final float WIDTH_VALUE = 0.9F;
   public final MultiBooleanSetting targets;
   public final BooleanSetting fill;
   public final List<Framebuffer> bloomBuffers;
   public Framebuffer depthCopyBuffer;
   public int bloomWidth;
   public int bloomHeight;
   public boolean outlineReady;
   public boolean hasOutlineTargetsCached;
   public long nextOutlineRetryAt;

   public ShaderEspModule() {
      super("ShaderESP", "Красивая обводка энтити", Category.RENDER);
      BooleanSetting[] var10004 = new BooleanSetting[4];
      var10004[0] = new BooleanSetting("Игроки", true);
      var10004[1] = new BooleanSetting("Кристаллы", true);
      var10004[2] = new BooleanSetting("Предметы", false);
      var10004[3] = new BooleanSetting("Себя", false);
      this.targets = new MultiBooleanSetting("Цели", var10004);
      this.fill = new BooleanSetting("Заливка", false);
      this.bloomBuffers = new ArrayList<>();
      this.bloomWidth = -1;
      this.bloomHeight = -1;
      Setting[] var10001 = new Setting[2];
      var10001[0] = this.targets;
      var10001[1] = this.fill;
      this.addSettings(var10001);
   }

   @Override
   public void onEnable() {
      super.onEnable();
      this.outlineReady = false;
      this.nextOutlineRetryAt = 0L;
      this.tryEnsureOutlineProcessor();
   }

   @Override
   public void onDisable() {
      for (Framebuffer fb : this.bloomBuffers) {
         fb.delete();
      }

      this.bloomBuffers.clear();
      if (this.depthCopyBuffer != null) {
         this.depthCopyBuffer.delete();
         this.depthCopyBuffer = null;
      }

      this.bloomWidth = -1;
      this.bloomHeight = -1;
      this.outlineReady = false;
      this.hasOutlineTargetsCached = false;
      this.nextOutlineRetryAt = 0L;
      super.onDisable();
   }

   @EventTarget
   public void onUpdate(EventTick event) {
      if (this.isEnable()) {
         if (mc.world != null && mc.worldRenderer != null) {
            this.hasOutlineTargetsCached = this.hasOutlineTargets();
            if (!this.hasOutlineTargetsCached) {
               this.outlineReady = false;
            } else {
               if (!this.outlineReady && System.currentTimeMillis() >= this.nextOutlineRetryAt) {
                  this.tryEnsureOutlineProcessor();
               }
            }
         } else {
            this.outlineReady = false;
            this.hasOutlineTargetsCached = false;
         }
      }
   }

   @EventTarget(
      priority = 200
   )
   public void onRender2D(EventRender2D event) {
      if (this.isEnable() && mc.world != null && mc.player != null && mc.worldRenderer != null) {
         boolean hasGlow = true;
         boolean hasFill = this.fill.isState();
         if (hasGlow || hasFill) {
            if (this.hasOutlineTargetsCached) {
               if (this.tryEnsureOutlineProcessor()) {
                  Framebuffer outlineBuffer = this.getOutlineSourceFramebuffer();
                  if (outlineBuffer != null && outlineBuffer.getColorAttachment() != 0) {
                     Framebuffer mainBuffer = mc.getFramebuffer();
                     this.ensureDepthCopyBuffer(mainBuffer.textureWidth, mainBuffer.textureHeight);
                     int iterations = Math.max(
                        1,
                        Math.min(
                           8,
                           (int)Math.ceil(1.125)
                        )
                     );
                     int fillTexture = 0;
                     if (hasFill) {
                        int fillIterations = Math.max(
                           2, Math.min(6, iterations + 1)
                        );
                        fillTexture = this.runKawaseBloom(outlineBuffer.getColorAttachment(), fillIterations);
                     }

                     int blurredTexture = hasGlow ? this.runKawaseBloom(outlineBuffer.getColorAttachment(), iterations) : fillTexture;
                     int color = this.getOutlineColor();
                     mainBuffer.beginWrite(false);
                     RenderSystem.enableBlend();
                     RenderSystem.disableDepthTest();
                     RenderSystem.colorMask(
                        true,
                        true,
                        true,
                        false
                     );
                     if (hasFill) {
                        ShaderProgram fillShader = mc.getShaderLoader().getOrCreateProgram(ObsidianComponent_257.shaderEspFill);
                        if (fillShader != null) {
                           RenderSystem.blendFuncSeparate(SrcFactor.SRC_ALPHA, DstFactor.ONE_MINUS_SRC_ALPHA, SrcFactor.ZERO, DstFactor.ONE);
                           RenderSystem.setShader(ObsidianComponent_257.shaderEspFill);
                           RenderSystem.setShaderTexture(0, outlineBuffer.getColorAttachment());
                           RenderSystem.setShaderTexture(1, fillTexture == 0 ? blurredTexture : fillTexture);
                           this.setUniform(fillShader, "color", ObsidianComponent_olorUtil.redf(color), ObsidianComponent_olorUtil.greenf(color), ObsidianComponent_olorUtil.bluef(color));
                           this.setUniform(fillShader, "alpha", 0.699999988079071f);
                           this.setUniform(
                              fillShader,
                              "time",
                              (float)(System.currentTimeMillis() % (100000L))
                                 / 1000.0f
                           );
                           this.drawFullscreenQuad();
                        }
                     }

                     if (hasGlow) {
                        ShaderProgram glowShader = mc.getShaderLoader().getOrCreateProgram(ObsidianComponent_257.shaderEspGlow);
                        if (glowShader != null) {
                           RenderSystem.blendFuncSeparate(SrcFactor.SRC_ALPHA, DstFactor.ONE_MINUS_SRC_ALPHA, SrcFactor.ZERO, DstFactor.ONE);
                           RenderSystem.setShader(ObsidianComponent_257.shaderEspGlow);
                           RenderSystem.setShaderTexture(0, blurredTexture);
                           RenderSystem.setShaderTexture(1, outlineBuffer.getColorAttachment());
                           this.setUniform(glowShader, "color", ObsidianComponent_olorUtil.redf(color), ObsidianComponent_olorUtil.greenf(color), ObsidianComponent_olorUtil.bluef(color));
                           this.setUniform(glowShader, "color2", ObsidianComponent_olorUtil.redf(color), ObsidianComponent_olorUtil.greenf(color), ObsidianComponent_olorUtil.bluef(color));
                           this.setUniform(glowShader, "exposure", 0.0507499985396862f);
                           this.setUniform(
                              glowShader,
                              "time",
                              (float)(System.currentTimeMillis() % (100000L))
                                 / 1000.0f
                           );
                           this.setUniform(glowShader, "animate", 1.0f);
                           this.drawFullscreenQuadWithDepthTest(mainBuffer, outlineBuffer);
                        }
                     }

                     RenderSystem.colorMask(
                        true,
                        true,
                        true,
                        true
                     );
                     RenderSystem.disableDepthTest();
                     RenderSystem.disableBlend();
                     RenderSystem.defaultBlendFunc();
                     RenderSystem.setShaderColor(
                        1.0f,
                        1.0f,
                        1.0f,
                        1.0f
                     );
                     RenderSystem.setShaderTexture(0, 0);
                     RenderSystem.setShaderTexture(1, 0);
                     mainBuffer.beginWrite(true);
                  }
               }
            }
         }
      }
   }
   public void drawFullscreenQuadWithDepthTest(Framebuffer mainBuffer, Framebuffer outlineBuffer) {
      if (this.depthCopyBuffer == null) {
         this.drawFullscreenQuad();
      } else {
         GL30.glBindFramebuffer(36008, mainBuffer.fbo);
         GL30.glBindFramebuffer(36009, this.depthCopyBuffer.fbo);
         GL30.glBlitFramebuffer(
            0,
            0,
            mainBuffer.textureWidth,
            mainBuffer.textureHeight,
            0,
            0,
            this.depthCopyBuffer.textureWidth,
            this.depthCopyBuffer.textureHeight,
            256,
            9728
         );
         GL30.glBindFramebuffer(36008, outlineBuffer.fbo);
         GL30.glBindFramebuffer(36009, mainBuffer.fbo);
         GL30.glBlitFramebuffer(
            0,
            0,
            outlineBuffer.textureWidth,
            outlineBuffer.textureHeight,
            0,
            0,
            mainBuffer.textureWidth,
            mainBuffer.textureHeight,
            256,
            9728
         );
         mainBuffer.beginWrite(false);
         RenderSystem.enableDepthTest();
         RenderSystem.depthFunc(515);
         RenderSystem.depthMask(false);
         this.drawFullscreenQuad();
         RenderSystem.depthMask(true);
         RenderSystem.disableDepthTest();
         GL30.glBindFramebuffer(36008, this.depthCopyBuffer.fbo);
         GL30.glBindFramebuffer(36009, mainBuffer.fbo);
         GL30.glBlitFramebuffer(
            0,
            0,
            this.depthCopyBuffer.textureWidth,
            this.depthCopyBuffer.textureHeight,
            0,
            0,
            mainBuffer.textureWidth,
            mainBuffer.textureHeight,
            256,
            9728
         );
         mainBuffer.beginWrite(false);
      }
   }
   public void ensureDepthCopyBuffer(int width, int height) {
      if (this.depthCopyBuffer != null && (this.depthCopyBuffer.textureWidth != width || this.depthCopyBuffer.textureHeight != height)) {
         this.depthCopyBuffer.delete();
         this.depthCopyBuffer = null;
      }

      if (this.depthCopyBuffer == null) {
         this.depthCopyBuffer = new SimpleFramebuffer(width, height, true);
      }
   }
   public boolean tryEnsureOutlineProcessor() {
      if (mc.world != null && mc.worldRenderer != null) {
         Framebuffer outlines = this.getOutlineSourceFramebuffer();
         if (outlines != null && outlines.getColorAttachment() != 0) {
            this.outlineReady = true;
            return true;
         } else {
            if (this.outlineReady) {
               this.outlineReady = false;
            }

            if (System.currentTimeMillis() < this.nextOutlineRetryAt) {
               return false;
            } else {
               try {
                  mc.worldRenderer.loadEntityOutlinePostProcessor();
                  outlines = this.getOutlineSourceFramebuffer();
                  this.outlineReady = (outlines != null && outlines.getColorAttachment() != 0);
                  if (!this.outlineReady) {
                     this.nextOutlineRetryAt = System.currentTimeMillis() + (3000L);
                  }

                  return this.outlineReady;
               } catch (Throwable var3) {
                  this.outlineReady = false;
                  this.nextOutlineRetryAt = System.currentTimeMillis() + (3000L);
                  return false;
               }
            }
         }
      } else {
         this.outlineReady = false;
         return false;
      }
   }
   public Framebuffer getOutlineSourceFramebuffer() {
      if (mc.worldRenderer instanceof WorldRendererAccessor accessor) {
         Framebuffer raw = accessor.obsidian$getEntityOutlineFramebufferRaw();
         if (raw != null && raw.getColorAttachment() != 0) {
            return raw;
         }
      }

      return mc.worldRenderer.getEntityOutlinesFramebuffer();
   }

   public boolean shouldOutline(Entity entity) {
      if (!this.isEnable() || entity == null || mc.player == null || mc.world == null) {
         return false;
      } else if (!entity.isAlive()) {
         return false;
      } else if (entity.isRemoved()) {
         return false;
      } else if (entity == mc.player && !this.targets.is("Себя")) {
         return false;
      } else if (entity.squaredDistanceTo(mc.player) > 65536.0) {
         return false;
      } else if (entity instanceof PlayerEntity) {
         return this.targets.is("Игроки");
      } else if (entity instanceof EndCrystalEntity) {
         return this.targets.is("Кристаллы");
      } else {
         return (entity instanceof ItemEntity && this.targets.is("Предметы"));
      }
   }
   public boolean hasOutlineTargets() {
      if (mc.world != null && mc.player != null) {
         for (Entity entity : mc.world.getEntities()) {
            if (this.shouldOutline(entity)) {
               return true;
            }
         }

         return false;
      } else {
         return false;
      }
   }

   public int getOutlineColor() {
      return ObsidianComponent_olorUtil.setAlphaColor(ObsidianComponent_olorUtil.getThemeColor(), 255) & 16777215;
   }
   public int runKawaseBloom(int sourceTexture, int iterations) {
      this.ensureBloomBuffers(iterations);
      if (this.bloomBuffers.isEmpty()) {
         return sourceTexture;
      } else {
         int currentTexture = sourceTexture;
         ShaderProgram downShader = mc.getShaderLoader().getOrCreateProgram(ObsidianComponent_257.shaderHandsKawaseDown);
         ShaderProgram upShader = mc.getShaderLoader().getOrCreateProgram(ObsidianComponent_257.shaderHandsKawaseUp);
         if (downShader != null && upShader != null) {
            for (int i = 0; i < iterations; i++) {
               Framebuffer dst = this.bloomBuffers.get(i);
               dst.setClearColor(
                  0.0f,
                  0.0f,
                  0.0f,
                  0.0f
               );
               dst.clear();
               dst.beginWrite(true);
               RenderSystem.setShader(ObsidianComponent_257.shaderHandsKawaseDown);
               RenderSystem.setShaderTexture(0, currentTexture);
               this.setHandsKawaseUniforms(downShader, dst.textureWidth, dst.textureHeight, 1.0f + (float)i);
               this.drawFullscreenQuad();
               currentTexture = dst.getColorAttachment();
            }

            for (int i = iterations - 1; i >= (1); i--) {
               Framebuffer dst = this.bloomBuffers.get(i - 1);
               dst.setClearColor(
                  0.0f,
                  0.0f,
                  0.0f,
                  0.0f
               );
               dst.clear();
               dst.beginWrite(true);
               RenderSystem.setShader(ObsidianComponent_257.shaderHandsKawaseUp);
               RenderSystem.setShaderTexture(0, currentTexture);
               this.setHandsKawaseUniforms(upShader, dst.textureWidth, dst.textureHeight, 1.0f + (float)i);
               this.setUniform(
                  upShader,
                  "color",
                  1.0f,
                  1.0f,
                  1.0f
               );
               this.drawFullscreenQuad();
               currentTexture = dst.getColorAttachment();
            }

            mc.getFramebuffer().beginWrite(true);
            return currentTexture;
         } else {
            return sourceTexture;
         }
      }
   }
   public void ensureBloomBuffers(int iterations) {
      int w = mc.getWindow().getFramebufferWidth();
      int h = mc.getWindow().getFramebufferHeight();
      if (this.bloomWidth != w || this.bloomHeight != h) {
         for (Framebuffer fb : this.bloomBuffers) {
            fb.delete();
         }

         this.bloomBuffers.clear();
         this.bloomWidth = w;
         this.bloomHeight = h;
      }

      while (this.bloomBuffers.size() > iterations) {
         int last = this.bloomBuffers.size() - 1;
         this.bloomBuffers.get(last).delete();
         this.bloomBuffers.remove(last);
      }

      for (int i = 0; i < iterations; i++) {
         int tw = Math.max(2, w >> i + 1);
         int th = Math.max(2, h >> i + (1));
         if (i >= this.bloomBuffers.size()) {
            Framebuffer fb = new SimpleFramebuffer(tw, th, false);
            this.setLinearFiltering(fb);
            this.bloomBuffers.add(fb);
         } else {
            Framebuffer fb = this.bloomBuffers.get(i);
            if (fb.textureWidth != tw || fb.textureHeight != th) {
               fb.delete();
               Framebuffer var12 = new SimpleFramebuffer(tw, th, false);
               this.setLinearFiltering(var12);
               this.bloomBuffers.set(i, var12);
            }
         }
      }
   }
   public void setLinearFiltering(Framebuffer fb) {
      RenderSystem.bindTexture(fb.getColorAttachment());
      GL11.glTexParameteri(3553, 10241, 9729);
      GL11.glTexParameteri(3553, 10240, 9729);
      RenderSystem.bindTexture(0);
   }
   public void setUniform(ShaderProgram shader, String name, float value) {
      GlUniform uniform = shader.getUniform(name);
      if (uniform != null) {
         uniform.set(value);
      }
   }
   public void setUniform(ShaderProgram shader, String name, float x, float y) {
      GlUniform uniform = shader.getUniform(name);
      if (uniform != null) {
         uniform.set(x, y);
      }
   }
   public void setUniform(ShaderProgram shader, String name, float x, float y, float z) {
      GlUniform uniform = shader.getUniform(name);
      if (uniform != null) {
         uniform.set(x, y, z);
      }
   }
   public void setHandsKawaseUniforms(ShaderProgram shader, int texWidth, int texHeight, float offset) {
      this.setUniform(
         shader, "uSize", (float)Math.max(1, texWidth), (float)Math.max(1, texHeight)
      );
      this.setUniform(shader, "uOffset", offset, offset);
      this.setUniform(
         shader,
         "uHalfPixel",
         0.5f / (float)Math.max(1, texWidth),
         0.5f / (float)Math.max(1, texHeight)
      );
   }
   public void drawFullscreenQuad() {
      float width = (float)Math.max(mc.getWindow().getScaledWidth(), 1);
      float height = (float)Math.max(mc.getWindow().getScaledHeight(), 1);
      BufferBuilder buffer = Tessellator.getInstance().begin(DrawMode.QUADS, VertexFormats.POSITION_TEXTURE_COLOR);
      buffer.vertex(
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
      buffer.vertex(0.0f, height, 0.0f)
         .texture(0.0f, 0.0f)
         .color(
            1.0f,
            1.0f,
            1.0f,
            1.0f
         );
      buffer.vertex(width, height, 0.0f)
         .texture(1.0f, 0.0f)
         .color(
            1.0f,
            1.0f,
            1.0f,
            1.0f
         );
      buffer.vertex(width, 0.0f, 0.0f)
         .texture(1.0f, 1.0f)
         .color(
            1.0f,
            1.0f,
            1.0f,
            1.0f
         );
      BufferRenderer.drawWithGlobalProgram(buffer.end());
   }
}
