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

























import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.gl.Framebuffer;
import net.minecraft.client.gl.GlUniform;
import net.minecraft.client.gl.ShaderProgram;
import net.minecraft.client.gl.SimpleFramebuffer;
import net.minecraft.client.render.BufferBuilder;
import net.minecraft.client.render.BufferRenderer;
import net.minecraft.client.render.Tessellator;
import net.minecraft.client.render.VertexFormats;
import net.minecraft.client.render.VertexFormat.DrawMode;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import org.joml.Matrix4f;
import org.lwjgl.opengl.GL11;

public class SonarModule extends Module {
   public static SonarModule INSTANCE = new SonarModule();
   private final SliderSetting duration = new SliderSetting(
      "Длительность",
      5.599999904632568f,
      0.800000011920929f,
      10.0f,
      0.10000000149011612f
   );
   private final SliderSetting alpha = new SliderSetting(
      "Яркость",
      1.0f,
      0.10000000149011612f,
      1.0f,
      0.009999999776482582f
   );
   private final SliderSetting widthMul = new SliderSetting(
      "Ширина",
      1.0f,
      0.3499999940395355f,
      2.200000047683716f,
      0.05000000074505806f
   );
   private final SliderSetting sharpness = new SliderSetting(
      "Резкость",
      24.0f,
      4.0f,
      80.0f,
      1.0f
   );
   public Framebuffer depthCopyBuffer;
   public int lastFbWidth = -1;
   public int lastFbHeight = -1;
   public long currentStart;
   public Vec3d center = Vec3d.ZERO;

   public SonarModule() {
      super("Sonar", "Сканирует новые чанки", Category.RENDER);
      Setting[] var10001 = new Setting[4];
      var10001[0] = this.duration;
      var10001[1] = this.alpha;
      var10001[2] = this.widthMul;
      var10001[3] = this.sharpness;
      this.addSettings(var10001);
   }

   @Override
   public void onEnable() {
      if (mc.player != null) {
         this.ping(mc.player.getPos());
      }

      super.onEnable();
   }

   @Override
   public void onDisable() {
      this.currentStart = 0L;
      this.deleteDepthCopyFramebuffer();
      super.onDisable();
   }

   @EventTarget
   public void onChunkReload(Event_114 event) {
      if (mc.player != null) {
         this.ping(mc.player.getPos());
      }
   }

   public void renderFromMixin(Matrix4f positionMatrix, Matrix4f projectionMatrix, Vec3d camPos) {
      if (mc.player != null && mc.world != null && this.currentStart > (0L)) {
         float durationMs = this.duration.get() * 1000.0f;
         float elapsed = (float)(System.currentTimeMillis() - this.currentStart);
         if (elapsed >= durationMs) {
            this.currentStart = 0L;
         } else {
            Framebuffer framebuffer = mc.getFramebuffer();
            this.ensureDepthCopyFramebuffer(framebuffer.textureWidth, framebuffer.textureHeight);
            if (this.depthCopyBuffer != null) {
               this.depthCopyBuffer.copyDepthFrom(framebuffer);
               Matrix4f invView = new Matrix4f(positionMatrix).invert();
               Matrix4f invProj = new Matrix4f(projectionMatrix).invert();
               float far = mc.gameRenderer.getFarPlaneDistance();
               float t = MathHelper.clamp(
                  elapsed / durationMs,
                  0.0f,
                  1.0f
               );
               float r1 = this.lerp(1.0f, far, (float)ObsidianComponent_57.QUINT_OUT.ease((double)t));
               float r2 = this.lerp(1.0f, far, (float)ObsidianComponent_57.QUART_IN_OUT.ease((double)t));
               float baseRadius = MathHelper.lerp(0.8500000238418579f, r1, r2);
               float alphaPc = 1.0f - t;
               float alphaWave = (
                     alphaPc > 0.5f
                        ? 1.0f - alphaPc
                        : alphaPc
                  )
                  * 2.0f;
               alphaWave = Math.min(alphaWave * 1.75f, 1.0f);
               float baseAlpha = MathHelper.clamp(
                  this.alpha.get() * alphaWave,
                  0.0f,
                  1.0f
               );
               int c1 = ObsidianComponent_olorUtil.getThemeColor(0);
               int c2 = ObsidianComponent_olorUtil.getThemeColor(90);
               int c3 = ObsidianComponent_olorUtil.getThemeColor(180);
               int c4 = ObsidianComponent_olorUtil.getThemeColor(270);
               float baseWidth = MathHelper.clamp(
                  6.0f
                     + baseRadius * 0.18000000715255737f * this.widthMul.get(),
                  4.0f,
                  Math.max(10.0f, far * 0.41999998688697815f)
               );
               float baseSharp = this.sharpness.get();
               this.renderPass(
                  invView,
                  invProj,
                  camPos,
                  framebuffer,
                  baseRadius,
                  baseWidth,
                  baseSharp,
                  this.applyAlpha(c1, baseAlpha),
                  this.applyAlpha(c2, baseAlpha),
                  this.applyAlpha(c3, baseAlpha),
                  this.applyAlpha(c4, baseAlpha)
               );
               RenderSystem.defaultBlendFunc();
            }
         }
      }
   }
   public void renderPass(
      Matrix4f invView,
      Matrix4f invProj,
      Vec3d camPos,
      Framebuffer framebuffer,
      float radius,
      float width,
      float sharp,
      int outerColor,
      int midColor,
      int innerColor,
      int scanlineColor
   ) {
      if (!(radius <= 0.0010000000474974513f) && !(width <= 0.0010000000474974513f)) {
         ShaderProgram shader = mc.getShaderLoader().getOrCreateProgram(ObsidianComponent_257.scanEffect);
         GlUniform invViewUniform = shader.getUniform("invViewMat");
         GlUniform invProjUniform = shader.getUniform("invProjMat");
         GlUniform posUniform = shader.getUniform("pos");
         GlUniform centerUniform = shader.getUniform("center");
         GlUniform radiusUniform = shader.getUniform("radius");
         GlUniform widthUniform = shader.getUniform("width");
         GlUniform sharpnessUniform = shader.getUniform("sharpness");
         GlUniform outerColorUniform = shader.getUniform("outerColor");
         GlUniform midColorUniform = shader.getUniform("midColor");
         GlUniform innerColorUniform = shader.getUniform("innerColor");
         GlUniform scanlineColorUniform = shader.getUniform("scanlineColor");
         GlUniform debugModeUniform = shader.getUniform("DebugMode");
         if (invViewUniform != null) {
            invViewUniform.set(invView);
         }

         if (invProjUniform != null) {
            invProjUniform.set(invProj);
         }

         if (posUniform != null) {
            posUniform.set((float)camPos.x, (float)camPos.y, (float)camPos.z);
         }

         if (centerUniform != null) {
            centerUniform.set((float)this.center.x, (float)this.center.y, (float)this.center.z);
         }

         if (radiusUniform != null) {
            radiusUniform.set(radius);
         }

         if (widthUniform != null) {
            widthUniform.set(width);
         }

         if (sharpnessUniform != null) {
            sharpnessUniform.set(sharp);
         }

         if (outerColorUniform != null) {
            this.setColor(outerColorUniform, outerColor);
         }

         if (midColorUniform != null) {
            this.setColor(midColorUniform, midColor);
         }

         if (innerColorUniform != null) {
            this.setColor(innerColorUniform, innerColor);
         }

         if (scanlineColorUniform != null) {
            this.setColor(scanlineColorUniform, scanlineColor);
         }

         if (debugModeUniform != null) {
            debugModeUniform.set(0);
         }

         RenderSystem.enableBlend();
         RenderSystem.blendFunc(770, 1);
         RenderSystem.setShaderColor(
            1.0f,
            1.0f,
            1.0f,
            1.0f
         );
         RenderSystem.disableDepthTest();
         RenderSystem.disableCull();
         RenderSystem.depthMask(false);
         int depthTex = this.depthCopyBuffer.getDepthAttachment();
         if (depthTex == 0) {
            depthTex = mc.getFramebuffer().getDepthAttachment();
         }

         RenderSystem.bindTexture(depthTex);
         GL11.glTexParameteri(3553, 34892, 0);
         GL11.glTexParameteri(3553, 10241, 9728);
         GL11.glTexParameteri(3553, 10240, 9728);
         framebuffer.beginWrite(false);
         RenderSystem.setShaderTexture(0, depthTex);
         RenderSystem.setShader(ObsidianComponent_257.scanEffect);
         this.drawFullscreenQuad();
         RenderSystem.depthMask(true);
         RenderSystem.enableCull();
         RenderSystem.enableDepthTest();
         RenderSystem.disableBlend();
      }
   }
   public void drawFullscreenQuad() {
      BufferBuilder buffer = Tessellator.getInstance().begin(DrawMode.QUADS, VertexFormats.POSITION_TEXTURE);
      buffer.vertex(
            -1.0f,
            -1.0f,
            0.0f
         )
         .texture(0.0f, 0.0f);
      buffer.vertex(
            -1.0f,
            1.0f,
            0.0f
         )
         .texture(0.0f, 1.0f);
      buffer.vertex(
            1.0f,
            1.0f,
            0.0f
         )
         .texture(1.0f, 1.0f);
      buffer.vertex(
            1.0f,
            -1.0f,
            0.0f
         )
         .texture(1.0f, 0.0f);
      BufferRenderer.drawWithGlobalProgram(buffer.end());
   }
   public void ensureDepthCopyFramebuffer(int width, int height) {
      if (this.depthCopyBuffer == null || this.lastFbWidth != width || this.lastFbHeight != height) {
         this.deleteDepthCopyFramebuffer();
         this.depthCopyBuffer = new SimpleFramebuffer(width, height, true);
         this.lastFbWidth = width;
         this.lastFbHeight = height;
      }
   }
   public void deleteDepthCopyFramebuffer() {
      if (this.depthCopyBuffer != null) {
         this.depthCopyBuffer.delete();
         this.depthCopyBuffer = null;
      }

      this.lastFbWidth = -1;
      this.lastFbHeight = -1;
   }
   public void ping(Vec3d pos) {
      this.currentStart = System.currentTimeMillis();
      this.center = pos;
   }
   public void setColor(GlUniform uniform, int color) {
      int a = color >> 24 & (255);
      int r = color >> 16 & (255);
      int g = color >> (8) & 255;
      int b = color & 255;
      if (a == 0) {
         a = 255;
      }

      uniform.set(
         (float)r / 255.0f,
         (float)g / 255.0f,
         (float)b / 255.0f,
         (float)a / 255.0f
      );
   }
   public int applyAlpha(int color, float alphaMul) {
      int a = color >> 24 & (255);
      if (a == 0) {
         a = 255;
      }

      a = (int)(
         (float)a
            * MathHelper.clamp(
               alphaMul, 0.0f, 1.0f
            )
      );
      return color & (16777215) | a << (24);
   }
   public float lerp(float a, float b, float t) {
      return a + (b - a) * t;
   }
}
