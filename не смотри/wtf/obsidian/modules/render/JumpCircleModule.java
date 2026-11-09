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
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import net.minecraft.client.gl.ShaderProgramKeys;
import net.minecraft.client.render.BufferBuilder;
import net.minecraft.client.render.BufferRenderer;
import net.minecraft.client.render.Tessellator;
import net.minecraft.client.render.VertexFormats;
import net.minecraft.client.render.VertexFormat.DrawMode;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.RotationAxis;
import net.minecraft.util.math.Vec3d;
import org.joml.Matrix4f;

public class JumpCircleModule extends Module {
   public static JumpCircleModule INSTANCE = new JumpCircleModule();
   public static final float MAX_LIFETIME_MS = 1850.0F;
   public static final float ROTATION_SPEED = 120.0F;
   public static final float PULSE_SPEED = 7.0F;
   public static final float PULSE_SCALE = 0.06F;
   public static final float PULSE_ALPHA = 0.12F;
   public static final int MAX_CIRCLES = 8;
   private final SliderSetting radius = new SliderSetting(
      "Радиус",
      1.850000023841858f,
      0.5f,
      4.0f,
      0.10000000149011612f
   );
   private final SliderSetting speed = new SliderSetting(
      "Скорость",
      1.2000000476837158f,
      1.0f,
      5.0f,
      0.10000000149011612f
   );
   private final SliderSetting fadeSpeed = new SliderSetting(
      "Скорость исчезновения",
      1.5f,
      1.0f,
      5.0f,
      0.5f
   );
   private final List<ObsidianComponent_367> circles = new ArrayList<>();
   private final Identifier circleTexture = Identifier.of("obsidian", "textures/jumpcircle/circle.png");
   private boolean wasOnGround = true;

   public JumpCircleModule() {
      super("JumpCircle", "Круг при прыжке", Category.RENDER);
      Setting[] var10001 = new Setting[3];
      var10001[0] = this.radius;
      var10001[1] = this.speed;
      var10001[2] = this.fadeSpeed;
      this.addSettings(var10001);
   }

   @Override
   public void onEnable() {
      if (mc.player != null) {
         this.wasOnGround = mc.player.isOnGround();
      }

      super.onEnable();
   }

   @Override
   public void onDisable() {
      this.circles.clear();
      super.onDisable();
   }

   @EventTarget
   public void onUpdate(EventTick event) {
      if (mc.player != null && mc.world != null) {
         boolean isOnGround = mc.player.isOnGround();
         if (this.wasOnGround && !isOnGround) {
            Vec3d pos = new Vec3d(
               mc.player.getX(),
               Math.floor(mc.player.getY()) + 0.001,
               mc.player.getZ()
            );
            this.circles.add(new ObsidianComponent_367(pos, System.currentTimeMillis()));

            while (this.circles.size() > (8)) {
               this.circles.remove(0);
            }
         }

         this.wasOnGround = isOnGround;
         long now = System.currentTimeMillis();
         float lifeTimeMs = this.getLifeTimeMs();
         Iterator<ObsidianComponent_367> iterator = this.circles.iterator();

         while (iterator.hasNext()) {
            ObsidianComponent_367 circle = iterator.next();
            if (now - circle.startTimeMs > (long)lifeTimeMs) {
               iterator.remove();
            }
         }
      }
   }

   @EventTarget
   public void onRender3D(RenderEvent event) {
      if (!this.circles.isEmpty()) {
         long now = System.currentTimeMillis();
         Vec3d cameraPos = event.getCamera().getPos();
         MatrixStack matrices = event.getMatrices();
         RenderSystem.enableBlend();
         RenderSystem.enableDepthTest();
         RenderSystem.depthMask(false);
         RenderSystem.disableCull();
         RenderSystem.blendFunc(770, 1);
         RenderSystem.setShader(ShaderProgramKeys.POSITION_TEX_COLOR);
         RenderSystem.setShaderTexture(0, this.circleTexture);

         for (ObsidianComponent_367 circle : this.circles) {
            float progress = this.getProgress(now, circle);
            if (!(progress >= 1.0f)) {
               float alpha = this.getAlpha(progress);
               if (!(alpha <= 0.009999999776482582f)) {
                  this.renderGlowCircle(matrices, cameraPos, circle, progress, alpha, now);
               }
            }
         }

         RenderSystem.enableCull();
         RenderSystem.depthMask(true);
         RenderSystem.enableDepthTest();
         RenderSystem.defaultBlendFunc();
         RenderSystem.disableBlend();
         RenderSystem.setShaderColor(
            1.0f,
            1.0f,
            1.0f,
            1.0f
         );
      }
   }
   public float getLifeTimeMs() {
      return 1850.0f
         / Math.max(0.25f, this.speed.get());
   }
   public float getProgress(long now, ObsidianComponent_367 circle) {
      return (float)(now - circle.startTimeMs) / this.getLifeTimeMs();
   }
   public float getAlpha(float progress) {
      float fade = MathHelper.clamp(
         progress * this.fadeSpeed.get(),
         0.0f,
         1.0f
      );
      return 1.0f - fade;
   }
   public void renderGlowCircle(MatrixStack matrices, Vec3d cameraPos, ObsidianComponent_367 circle, float progress, float alpha, long now) {
      float lifeTimeSec = (float)(now - circle.startTimeMs) / 1000.0f;
      float easedProgress = easeOutCubic(progress);
      float scale = Math.min(easedProgress * this.radius.get(), this.radius.get());
      float rotation = lifeTimeSec * 120.0f * this.speed.get();
      rotation += (float)Math.sin(
            (double)progress
               * 3.141592653589793
               * 2.0
         )
         * 30.0f;
      float pulse = (float)Math.sin((double)(lifeTimeSec * 7.0f * this.speed.get()));
      float pulseScale = 1.0f
         + pulse * 0.05999999865889549f;
      float pulseAlpha = MathHelper.clamp(
         alpha * (1.0f + pulse * 0.11999999731779099f),
         0.0f,
         1.0f
      );
      float alphaBoost = MathHelper.clamp(
         pulseAlpha * 1.25f, 0.0F, 1.0f
      );
      float finalScale = scale * pulseScale;
      int baseTheme = this.getStableThemeColor();
      int secondaryTheme = this.getStableThemeSecondaryColor();
      int colorA = ObsidianComponent_olorUtil.setAlphaColor(baseTheme, (int)(255.0f * alphaBoost));
      int colorB = ObsidianComponent_olorUtil.setAlphaColor(secondaryTheme, (int)(255.0f * alphaBoost));
      int darkA = ObsidianComponent_olorUtil.setAlphaColor(
         ObsidianComponent_olorUtil.darken(baseTheme, 0.6499999761581421f),
         (int)(
            255.0f
               * MathHelper.clamp(
                  alphaBoost * 0.8999999761581421f,
                  0.0f,
                  1.0f
               )
         )
      );
      int darkB = ObsidianComponent_olorUtil.setAlphaColor(
         ObsidianComponent_olorUtil.darken(secondaryTheme, 0.6499999761581421f),
         (int)(
            255.0f
               * MathHelper.clamp(
                  alphaBoost * 0.8999999761581421f,
                  0.0f,
                  1.0f
               )
         )
      );
      matrices.push();
      matrices.translate(circle.pos.x - cameraPos.x, circle.pos.y - cameraPos.y, circle.pos.z - cameraPos.z);
      matrices.multiply(RotationAxis.POSITIVE_X.rotationDegrees(90.0f));
      matrices.multiply(RotationAxis.POSITIVE_Z.rotationDegrees(rotation));
      Matrix4f matrix = matrices.peek().getPositionMatrix();
      float half = finalScale * 0.5f;
      float thickScale = finalScale * 1.0800000429153442f;
      float thickHalf = thickScale * 0.5f;
      BufferBuilder buffer = Tessellator.getInstance().begin(DrawMode.QUADS, VertexFormats.POSITION_TEXTURE_COLOR);
      this.addTexturedQuad(buffer, matrix, -half, -half, half, half, colorA, colorB);
      this.addTexturedQuad(buffer, matrix, -thickHalf, -thickHalf, thickHalf, thickHalf, darkA, darkB);
      BufferRenderer.drawWithGlobalProgram(buffer.end());
      matrices.pop();
   }
   public void addTexturedQuad(BufferBuilder buffer, Matrix4f matrix, float x1, float y1, float x2, float y2, int colorA, int colorB) {
      int aR = colorA >> (16) & (255);
      int aG = colorA >> 8 & (255);
      int aB = colorA & 255;
      int aA = colorA >> (24) & 255;
      int bR = colorB >> (16) & (255);
      int bG = colorB >> 8 & 255;
      int bB = colorB & 255;
      int bA = colorB >> (24) & 255;
      buffer.vertex(matrix, x1, y1, 0.0f)
         .texture(0.0f, 1.0f)
         .color(aR, aG, aB, aA);
      buffer.vertex(matrix, x1, y2, 0.0f)
         .texture(0.0f, 0.0f)
         .color(bR, bG, bB, bA);
      buffer.vertex(matrix, x2, y2, 0.0f)
         .texture(1.0f, 0.0f)
         .color(bR, bG, bB, bA);
      buffer.vertex(matrix, x2, y1, 0.0f)
         .texture(1.0f, 1.0f)
         .color(aR, aG, aB, aA);
   }
   public int getStableThemeColor() {
      return !Obsidian.INSTANCE.themeStorage.getThemes().getTheme().getName().equals("Rainbow")
         ? Obsidian.INSTANCE.themeStorage.getThemes().getTheme().color[0]
         : ObsidianComponent_olorUtil.getThemeColor();
   }
   public int getStableThemeSecondaryColor() {
      return !Obsidian.INSTANCE.themeStorage.getThemes().getTheme().getName().equals("Rainbow")
         ? Obsidian.INSTANCE.themeStorage.getThemes().getTheme().color[0]
         : ObsidianComponent_olorUtil.getThemeColor(180);
   }
   public static float easeOutCubic(float t) {
      float u = 1.0f - t;
      return 1.0f - u * u * u;
   }
}
