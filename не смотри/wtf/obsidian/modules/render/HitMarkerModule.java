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
import java.util.Optional;
import net.minecraft.client.gl.ShaderProgramKeys;
import net.minecraft.client.render.BufferBuilder;
import net.minecraft.client.render.BufferRenderer;
import net.minecraft.client.render.Tessellator;
import net.minecraft.client.render.VertexFormats;
import net.minecraft.client.render.VertexFormat.DrawMode;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.Entity;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.RotationAxis;
import net.minecraft.util.math.Vec3d;
import org.joml.Matrix4f;

public class HitMarkerModule extends Module {
   public static HitMarkerModule INSTANCE = new HitMarkerModule();
   private final SliderSetting size = new SliderSetting(
      "Размер",
      0.5f,
      0.10000000149011612f,
      2.0f,
      0.05000000074505806f
   );
   private final SliderSetting fadeInTime = new SliderSetting(
      "Время появления",
      100.0f,
      50.0f,
      500.0f,
      10.0f
   );
   private final SliderSetting displayTime = new SliderSetting(
      "Время показа",
      300.0f,
      100.0f,
      1000.0f,
      50.0f
   );
   private final SliderSetting fadeOutTime = new SliderSetting(
      "Время исчезновения",
      200.0f,
      50.0f,
      500.0f,
      10.0f
   );
   private final BooleanSetting glow = new BooleanSetting("Свечение", true);
   private final BooleanSetting scale = new BooleanSetting("Анимация масштаба", true);
   private final ArrayList<ObsidianComponent_359> hitMarkers = new ArrayList<>();

   public HitMarkerModule() {
      super("HitMarker", "Показывает маркер при ударе", Category.RENDER);
      Setting[] var10001 = new Setting[6];
      var10001[0] = this.size;
      var10001[1] = this.fadeInTime;
      var10001[2] = this.displayTime;
      var10001[3] = this.fadeOutTime;
      var10001[4] = this.glow;
      var10001[5] = this.scale;
      this.addSettings(var10001);
   }

   @Override
   public void onDisable() {
      this.hitMarkers.clear();
      super.onDisable();
   }
   public Identifier getTexture() {
      return Identifier.of("obsidian", "textures/cross/cross.png");
   }

   @EventTarget
   public void onAttack(Event_358 event) {
      if (mc.player != null && mc.world != null) {
         Entity target = event.getTarget();
         if (target != null) {
            synchronized (this.hitMarkers) {
               this.hitMarkers
                  .add(
                     new ObsidianComponent_359(
                        this.resolveHitPosition(event.getPlayer(), target),
                        System.currentTimeMillis(),
                        (long)this.fadeInTime.get(),
                        (long)this.displayTime.get(),
                        (long)this.fadeOutTime.get()
                     )
                  );
            }
         }
      }
   }
   public Vec3d resolveHitPosition(Entity attacker, Entity target) {
      Vec3d fallback = new Vec3d(
         target.getX(),
         target.getY() + (double)target.getHeight() / 2.0,
         target.getZ()
      );
      if (attacker == null) {
         return fallback;
      } else {
         Vec3d eyePos = attacker.getCameraPosVec(1.0f);
         Vec3d lookVec = attacker.getRotationVec(1.0f);
         Vec3d targetCenter = target.getBoundingBox().getCenter();
         double distance = Math.max(
            eyePos.distanceTo(targetCenter) + 1.0,
            6.0
         );
         Vec3d reachPos = eyePos.add(lookVec.multiply(distance));
         Optional<Vec3d> hitPos = target.getBoundingBox().raycast(eyePos, reachPos);
         return hitPos.isPresent() ? hitPos.get() : eyePos.add(lookVec.multiply(eyePos.distanceTo(targetCenter)));
      }
   }

   @EventTarget
   public void onRender3D(RenderEvent e) {
      if (mc.player != null && mc.world != null) {
         synchronized (this.hitMarkers) {
            this.hitMarkers.removeIf(ObsidianComponent_359::isDead);
         }

         if (!this.hitMarkers.isEmpty()) {
            MatrixStack matrices = e.getMatrices();
            Vec3d camera = mc.gameRenderer.getCamera().getPos();
            Identifier texture = this.getTexture();
            RenderSystem.enableBlend();
            RenderSystem.disableDepthTest();
            RenderSystem.depthMask(false);
            RenderSystem.disableCull();
            if (this.glow.isState()) {
               RenderSystem.blendFunc(770, 1);
            } else {
               RenderSystem.defaultBlendFunc();
            }

            RenderSystem.setShaderTexture(0, texture);
            RenderSystem.setShader(ShaderProgramKeys.POSITION_TEX_COLOR);
            ArrayList<ObsidianComponent_359> renderList;
            synchronized (this.hitMarkers) {
               renderList = new ArrayList<>(this.hitMarkers);
            }

            int color = ObsidianComponent_olorUtil.getThemeColor();
            int r = color >> 16 & 255;
            int g = color >> (8) & (255);
            int b = color & 255;

            for (ObsidianComponent_359 marker : renderList) {
               float alpha = marker.getAlpha();
               if (!(alpha <= 0.0f)) {
                  double x = marker.position.x - camera.x;
                  double y = marker.position.y - camera.y;
                  double z = marker.position.z - camera.z;
                  matrices.push();
                  matrices.translate((float)x, (float)y, (float)z);
                  matrices.multiply(RotationAxis.POSITIVE_Y.rotationDegrees(-mc.gameRenderer.getCamera().getYaw()));
                  matrices.multiply(RotationAxis.POSITIVE_X.rotationDegrees(mc.gameRenderer.getCamera().getPitch()));
                  float currentSize = this.size.get();
                  if (this.scale.isState()) {
                     float scaleMultiplier = marker.getScaleMultiplier();
                     currentSize *= scaleMultiplier;
                  }

                  Matrix4f matrix = matrices.peek().getPositionMatrix();
                  float half = currentSize / 2.0f;
                  int alphaInt = (int)(alpha * 255.0f);
                  BufferBuilder buffer = Tessellator.getInstance().begin(DrawMode.QUADS, VertexFormats.POSITION_TEXTURE_COLOR);
                  buffer.vertex(matrix, -half, -half, 0.0f)
                     .texture(
                        0.0f, 1.0f
                     )
                     .color(r, g, b, alphaInt);
                  buffer.vertex(matrix, -half, half, 0.0f)
                     .texture(0.0f, 0.0F)
                     .color(r, g, b, alphaInt);
                  buffer.vertex(matrix, half, half, 0.0f)
                     .texture(1.0f, 0.0f)
                     .color(r, g, b, alphaInt);
                  buffer.vertex(matrix, half, -half, 0.0f)
                     .texture(
                        1.0f, 1.0f
                     )
                     .color(r, g, b, alphaInt);
                  BufferRenderer.drawWithGlobalProgram(buffer.end());
                  matrices.pop();
               }
            }

            RenderSystem.enableCull();
            RenderSystem.depthMask(true);
            RenderSystem.enableDepthTest();
            RenderSystem.defaultBlendFunc();
            RenderSystem.disableBlend();
         }
      }
   }
}
