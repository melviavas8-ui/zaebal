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
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL30;

public class ShaderHandsRenderer implements IInstance {
   public static final float EPSILON = 0.001F;
   public static ShaderHandsRenderer instance;
   public Framebuffer beforeBuffer;
   public Framebuffer afterBuffer;
   public Framebuffer maskBuffer;
   private final List<Framebuffer> bloomBuffers = new ArrayList<>();
   public int width = -1;
   public int height = -1;
   public boolean hasBeforeCapture;
   public boolean pendingComposite;
   public int configuredBeforeDepthTex = -1;
   public int configuredAfterDepthTex = -1;

   public static ShaderHandsRenderer getInstance() {
      if (instance == null) {
         instance = new ShaderHandsRenderer();
      }

      return instance;
   }

   public void captureBeforeHands() {
      ShaderHandsModule module = this.getModule();
      if (!this.isEffectEnabled(module)) {
         this.invalidateState();
      } else {
         this.ensureBuffers();
         if (this.beforeBuffer != null) {
            this.copyMainFramebuffer(this.beforeBuffer);
            this.hasBeforeCapture = true;
         }
      }
   }

   public void captureAfterHands() {
      ShaderHandsModule module = this.getModule();
      if (!this.isEffectEnabled(module)) {
         this.invalidateState();
      } else {
         this.ensureBuffers();
         if (this.beforeBuffer != null && this.afterBuffer != null && this.maskBuffer != null) {
            if (this.hasBeforeCapture) {
               this.copyMainFramebuffer(this.afterBuffer);
               this.pendingComposite = true;
            }
         }
      }
   }

   public void renderOverlayIfPending() {
      if (this.pendingComposite) {
         this.ensureBuffers();
         if (this.beforeBuffer != null && this.afterBuffer != null && this.maskBuffer != null) {
            ShaderHandsModule module = this.getModule();
            if (!this.isEffectEnabled(module)) {
               this.invalidateState();
            } else {
               ShaderProgram maskShader = mc.getShaderLoader().getOrCreateProgram(ObsidianComponent_257.shaderHandsMaskDiff);
               if (maskShader == null) {
                  this.invalidateState();
               } else {
                  this.maskBuffer
                     .setClearColor(
                        0.0f,
                        0.0f,
                        0.0f,
                        0.0f
                     );
                  this.maskBuffer.clear();
                  this.maskBuffer.beginWrite(false);
                  RenderSystem.disableDepthTest();
                  RenderSystem.disableBlend();
                  RenderSystem.setShader(ObsidianComponent_257.shaderHandsMaskDiff);
                  RenderSystem.setShaderTexture(0, this.beforeBuffer.getColorAttachment());
                  RenderSystem.setShaderTexture(1, this.afterBuffer.getColorAttachment());
                  int beforeDepth = this.beforeBuffer.getDepthAttachment();
                  int afterDepth = this.afterBuffer.getDepthAttachment();
                  if (beforeDepth != 0 && beforeDepth != this.configuredBeforeDepthTex) {
                     this.configureDepthTexture(beforeDepth);
                     this.configuredBeforeDepthTex = beforeDepth;
                  }

                  if (afterDepth != 0 && afterDepth != this.configuredAfterDepthTex) {
                     this.configureDepthTexture(afterDepth);
                     this.configuredAfterDepthTex = afterDepth;
                  }

                  RenderSystem.setShaderTexture(2, beforeDepth);
                  RenderSystem.setShaderTexture(3, afterDepth);
                  this.drawFullscreenQuad();
                  RenderSystem.enableDepthTest();
                  float glowValue = module.glow.get();
                  float fillValue = module.fill.get();
                  float alphaValue = module.alpha.get();
                  float outlineValue = module.outline.get();
                  boolean hasGlow = (glowValue > 0.0010000000474974513f);
                  boolean hasFill = (fillValue > 0.0010000000474974513f
                        && alphaValue > 0.0010000000474974513f);
                  int color1 = Obsidian.INSTANCE.themeStorage.getThemes().getTheme().getName().equals("Rainbow")
                     ? ObsidianComponent_olorUtil.getThemeColor(0)
                     : Obsidian.INSTANCE.themeStorage.getThemes().getTheme().color[0];
                  if (module.mode.is("Красивый")) {
                     this.renderPrettyMode(module, color1, color1, glowValue, fillValue, alphaValue, outlineValue);
                     this.invalidateState();
                  } else {
                     int blurredMaskTexture = 0;
                     if (hasGlow) {
                        int iterations = Math.max(
                           3,
                           Math.min(
                              8,
                              (4) + Math.round(outlineValue * 0.699999988079071f)
                           )
                        );
                        blurredMaskTexture = this.runKawaseBloom(iterations);
                     }

                     mc.getFramebuffer().beginWrite(true);
                     RenderSystem.enableBlend();
                     RenderSystem.colorMask(
                        true,
                        true,
                        true,
                        false
                     );
                     RenderSystem.disableDepthTest();
                     ShaderProgram glowShader = hasGlow ? mc.getShaderLoader().getOrCreateProgram(ObsidianComponent_257.shaderHandsGlow) : null;
                     if (glowShader != null) {
                        RenderSystem.blendFuncSeparate(SrcFactor.SRC_ALPHA, DstFactor.ONE, SrcFactor.ZERO, DstFactor.ONE);
                        RenderSystem.setShader(ObsidianComponent_257.shaderHandsGlow);
                        RenderSystem.setShaderTexture(0, blurredMaskTexture);
                        RenderSystem.setShaderTexture(1, this.maskBuffer.getColorAttachment());
                        this.setUniform(glowShader, "color", ObsidianComponent_olorUtil.redf(color1), ObsidianComponent_olorUtil.greenf(color1), ObsidianComponent_olorUtil.bluef(color1));
                        this.setUniform(glowShader, "color2", ObsidianComponent_olorUtil.redf(color1), ObsidianComponent_olorUtil.greenf(color1), ObsidianComponent_olorUtil.bluef(color1));
                        this.setUniform(
                           glowShader,
                           "exposure",
                           1.0f + glowValue * 1.7999999523162842f
                        );
                        this.drawFullscreenQuad();
                     }

                     if (hasFill) {
                        ShaderProgram overlayShader = mc.getShaderLoader().getOrCreateProgram(ObsidianComponent_257.shaderHandsOverlay);
                        if (overlayShader == null) {
                           this.restoreCompositeState();
                           this.invalidateState();
                           return;
                        }

                        RenderSystem.blendFuncSeparate(SrcFactor.SRC_ALPHA, DstFactor.ONE_MINUS_SRC_ALPHA, SrcFactor.ZERO, DstFactor.ONE);
                        RenderSystem.setShader(ObsidianComponent_257.shaderHandsOverlay);
                        RenderSystem.setShaderTexture(0, this.maskBuffer.getColorAttachment());
                        this.setUniform(overlayShader, "color", ObsidianComponent_olorUtil.redf(color1), ObsidianComponent_olorUtil.greenf(color1), ObsidianComponent_olorUtil.bluef(color1));
                        this.setUniform(overlayShader, "fill", fillValue);
                        this.setUniform(overlayShader, "alpha", alphaValue);
                        this.drawFullscreenQuad();
                     }

                     this.restoreCompositeState();
                     this.invalidateState();
                  }
               }
            }
         }
      }
   }

   public void invalidateState() {
      this.hasBeforeCapture = false;
      this.pendingComposite = false;
      this.configuredBeforeDepthTex = -1;
      this.configuredAfterDepthTex = -1;
   }
   public int runKawaseBloom(int iterations) {
      this.ensureBloomBuffers(iterations);
      if (this.bloomBuffers.isEmpty()) {
         return this.maskBuffer.getColorAttachment();
      } else {
         int currentTexture = this.maskBuffer.getColorAttachment();
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
               this.setHandsKawaseUniforms(
                  downShader, dst.textureWidth, dst.textureHeight, 1.0f + (float)i
               );
               this.drawFullscreenQuad();
               currentTexture = dst.getColorAttachment();
            }

            for (int i = iterations - (1); i >= (1); i--) {
               Framebuffer dst = this.bloomBuffers.get(i - (1));
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
            return currentTexture;
         }
      }
   }
   public void copyMainFramebuffer(Framebuffer target) {
      int readFbo = GL11.glGetInteger(36010);
      int drawFbo = GL11.glGetInteger(36006);
      GL30.glBindFramebuffer(36008, mc.getFramebuffer().fbo);
      GL30.glBindFramebuffer(36009, target.fbo);
      GL30.glBlitFramebuffer(
         0,
         0,
         this.width,
         this.height,
         0,
         0,
         this.width,
         this.height,
         16640,
         9728
      );
      GL30.glBindFramebuffer(36008, readFbo);
      GL30.glBindFramebuffer(36009, drawFbo);
      mc.getFramebuffer().beginWrite(true);
   }
   public void configureDepthTexture(int depthTex) {
      RenderSystem.bindTexture(depthTex);
      GL11.glTexParameteri(3553, 34892, 0);
      GL11.glTexParameteri(3553, 10241, 9728);
      GL11.glTexParameteri(3553, 10240, 9728);
      RenderSystem.bindTexture(0);
   }
   public void ensureBuffers() {
      int w = mc.getWindow().getFramebufferWidth();
      int h = mc.getWindow().getFramebufferHeight();
      if (w != this.width || h != this.height || this.beforeBuffer == null || this.afterBuffer == null || this.maskBuffer == null) {
         if (this.beforeBuffer != null) {
            this.beforeBuffer.delete();
         }

         if (this.afterBuffer != null) {
            this.afterBuffer.delete();
         }

         if (this.maskBuffer != null) {
            this.maskBuffer.delete();
         }

         for (Framebuffer fb : this.bloomBuffers) {
            fb.delete();
         }

         this.bloomBuffers.clear();
         this.beforeBuffer = new SimpleFramebuffer(w, h, true);
         this.afterBuffer = new SimpleFramebuffer(w, h, true);
         this.maskBuffer = new SimpleFramebuffer(w, h, true);
         this.width = w;
         this.height = h;
         this.configuredBeforeDepthTex = -1;
         this.configuredAfterDepthTex = -1;
      }
   }
   public void ensureBloomBuffers(int iterations) {
      while (this.bloomBuffers.size() > iterations) {
         int last = this.bloomBuffers.size() - 1;
         this.bloomBuffers.get(last).delete();
         this.bloomBuffers.remove(last);
      }

      for (int i = 0; i < iterations; i++) {
         int w = Math.max(2, this.width >> i + (1));
         int h = Math.max(2, this.height >> i + 1);
         if (i >= this.bloomBuffers.size()) {
            Framebuffer fb = new SimpleFramebuffer(w, h, false);
            this.setLinearFiltering(fb);
            this.bloomBuffers.add(fb);
         } else {
            Framebuffer fb = this.bloomBuffers.get(i);
            if (fb.textureWidth != w || fb.textureHeight != h) {
               fb.delete();
               Framebuffer var8 = new SimpleFramebuffer(w, h, false);
               this.setLinearFiltering(var8);
               this.bloomBuffers.set(i, var8);
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
   public ShaderHandsModule getModule() {
      return Obsidian.INSTANCE != null && ModuleManager.INSTANCE != null ? ModuleManager.shaderHands : null;
   }
   public void renderPrettyMode(ShaderHandsModule module, int color1, int color2, float glowValue, float fillValue, float alphaValue, float outlineValue) {
      ShaderProgram shader = mc.getShaderLoader().getOrCreateProgram(ObsidianComponent_257.blockOverlay);
      if (shader != null) {
         mc.getFramebuffer().beginWrite(false);
         RenderSystem.enableBlend();
         RenderSystem.defaultBlendFunc();
         RenderSystem.disableDepthTest();
         RenderSystem.setShader(ObsidianComponent_257.blockOverlay);
         RenderSystem.setShaderTexture(0, this.maskBuffer.getColorAttachment());
         this.setUniform(
            shader,
            "texelSize",
            1.0F / (float)Math.max(1, mc.getWindow().getFramebufferWidth()),
            1.0f
               / (float)Math.max(1, mc.getWindow().getFramebufferHeight())
         );
         this.setUniform(shader, "color", ObsidianComponent_olorUtil.redf(color1), ObsidianComponent_olorUtil.greenf(color1), ObsidianComponent_olorUtil.bluef(color1));
         this.setUniform(shader, "color2", ObsidianComponent_olorUtil.redf(color2), ObsidianComponent_olorUtil.greenf(color2), ObsidianComponent_olorUtil.bluef(color2));
         this.setUniform(
            shader,
            "time",
            (float)(System.currentTimeMillis() % (100000L))
               / 1000.0f
         );
         this.setUniform(shader, "speed", module.waveSpeed.get());
         this.setUniform(shader, "scale", module.waveScale.get());
         this.setUniform(shader, "outline", outlineValue);
         this.setUniform(shader, "glow", glowValue);
         this.setUniform(shader, "fill", fillValue);
         this.setUniform(shader, "alpha", alphaValue);
         this.setUniform(shader, "outlineOnly", 0.0f);
         this.drawFullscreenQuad();
         RenderSystem.enableDepthTest();
         RenderSystem.disableBlend();
         RenderSystem.defaultBlendFunc();
         this.restoreCompositeState();
      }
   }
   public void restoreCompositeState() {
      RenderSystem.colorMask(
         true,
         true,
         true,
         true
      );
      RenderSystem.depthMask(true);
      RenderSystem.enableDepthTest();
      RenderSystem.enableCull();
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
      RenderSystem.setShaderTexture(2, 0);
      RenderSystem.setShaderTexture(3, 0);
      mc.getFramebuffer().beginWrite(true);
   }
   public boolean isEffectEnabled(ShaderHandsModule module) {
      if (module != null && module.isEnable()) {
         boolean hasGlow = (module.glow.get() > 0.0010000000474974513f);
         boolean hasFill = (module.fill.get() > 0.0010000000474974513f
               && module.alpha.get() > 0.0010000000474974513f);
         return (!(!hasGlow && !hasFill));
      } else {
         return false;
      }
   }
   public void setUniform(ShaderProgram shader, String name, float v) {
      GlUniform u = shader.getUniform(name);
      if (u != null) {
         u.set(v);
      }
   }
   public void setUniform(ShaderProgram shader, String name, float x, float y) {
      GlUniform u = shader.getUniform(name);
      if (u != null) {
         u.set(x, y);
      }
   }
   public void setUniform(ShaderProgram shader, String name, float x, float y, float z) {
      GlUniform u = shader.getUniform(name);
      if (u != null) {
         u.set(x, y, z);
      }
   }
   public void setHandsKawaseUniforms(ShaderProgram shader, int texWidth, int texHeight, float offset) {
      this.setUniform(
         shader,
         "uSize",
         (float)Math.max(1, texWidth),
         (float)Math.max(1, texHeight)
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
      float sw = (float)Math.max(mc.getWindow().getScaledWidth(), 1);
      float sh = (float)Math.max(mc.getWindow().getScaledHeight(), 1);
      BufferBuilder b = Tessellator.getInstance().begin(DrawMode.QUADS, VertexFormats.POSITION_TEXTURE_COLOR);
      b.vertex(
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
      b.vertex(0.0f, sh, 0.0f)
         .texture(0.0f, 0.0f)
         .color(
            1.0f,
            1.0f,
            1.0f,
            1.0f
         );
      b.vertex(sw, sh, 0.0f)
         .texture(1.0f, 0.0f)
         .color(
            1.0f,
            1.0f,
            1.0f,
            1.0f
         );
      b.vertex(sw, 0.0f, 0.0f)
         .texture(1.0f, 1.0f)
         .color(
            1.0f,
            1.0f,
            1.0f,
            1.0f
         );
      BufferRenderer.drawWithGlobalProgram(b.end());
   }
}
