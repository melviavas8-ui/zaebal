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

























import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.Entity;
import net.minecraft.entity.projectile.FireworkRocketEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import org.joml.Matrix4f;
import org.joml.Quaternionf;
import org.joml.Vector3f;
import org.joml.Vector4f;

public class FireworkESPModule extends Module {
   public static FireworkESPModule INSTANCE = new FireworkESPModule();
   private final SliderSetting interval = new SliderSetting(
      "Интервал (мс)",
      100.0f,
      10.0f,
      1000.0f,
      10.0f
   );
   private final SliderSetting lifetime = new SliderSetting(
      "Время жизни (мс)",
      1000.0f,
      100.0f,
      5000.0f,
      100.0f
   );
   private final Matrix4f lastProjectionMatrix = new Matrix4f();
   private final Quaternionf lastCameraRotation = new Quaternionf();
   public Vec3d lastCameraPos = Vec3d.ZERO;
   public float lastTickDelta;
   private final Map<Integer, ObsidianComponent_183> fireworks = new HashMap<>();

   public FireworkESPModule() {
      super("FireworkESP", "Показывает теги и трейлы фейерверков", Category.RENDER);
      Setting[] var10001 = new Setting[2];
      var10001[0] = this.interval;
      var10001[1] = this.lifetime;
      this.addSettings(var10001);
   }

   @Override
   public void onDisable() {
      super.onDisable();
      this.fireworks.clear();
   }

   @EventTarget
   public void onRender3D(RenderEvent event) {
      this.lastProjectionMatrix.set(event.getProjectionMatrix());
      this.lastCameraPos = event.getCamera().getPos();
      this.lastCameraRotation.set(event.getCamera().getRotation());
      this.lastTickDelta = event.getTickDelta();
      if (mc.world != null) {
         long currentTime = System.currentTimeMillis();
         this.fireworks
            .entrySet()
            .removeIf(
               entry -> {
                  Entity entityx = mc.world.getEntityById(entry.getKey());
                  boolean isDead = (!(entityx != null && entityx.isAlive()));
                  entry.getValue()
                     .points
                     .removeIf(
                        p -> ((float)(currentTime - p.timestamp) > this.lifetime.get())
                     );
                  return (isDead && entry.getValue().points.isEmpty());
               }
            );

         for (Entity entity : mc.world.getEntities()) {
            if (entity instanceof FireworkRocketEntity && entity.isAlive()) {
               ObsidianComponent_183 data = this.fireworks.computeIfAbsent(entity.getId(), k -> new ObsidianComponent_183());
               if ((float)(currentTime - data.lastSpawnTime) >= this.interval.get()) {
                  Vec3d pos = new Vec3d(
                     MathHelper.lerp((double)this.lastTickDelta, entity.lastRenderX, entity.getX()),
                     MathHelper.lerp((double)this.lastTickDelta, entity.lastRenderY, entity.getY())
                        + 0.5,
                     MathHelper.lerp((double)this.lastTickDelta, entity.lastRenderZ, entity.getZ())
                  );
                  float ageInSeconds = (float)entity.age / 20.0f;
                  data.points.add(new ObsidianComponent_139(pos, currentTime, ageInSeconds));
                  data.lastSpawnTime = currentTime;
               }
            }
         }
      }
   }

   @EventTarget
   public void onRender2D(EventRender2D event) {
      if (mc.player != null && mc.world != null) {
         MatrixStack matrices = event.getContext().getMatrices();
         ItemStack icon = new ItemStack(Items.FIREWORK_ROCKET);
         FontRenderer font = ObsidianComponent_201.getFont("sf_regular", 14);
         long currentTime = System.currentTimeMillis();

         for (Entry<Integer, ObsidianComponent_183> entry : this.fireworks.entrySet()) {
            ObsidianComponent_183 data = entry.getValue();

            for (ObsidianComponent_139 p : data.points) {
               Vec3d screen = this.worldToScreen(p.pos);
               if (screen != null) {
                  float progress = 1.0f - (float)(currentTime - p.timestamp) / this.lifetime.get();
                  progress = MathHelper.clamp(
                     progress, 0.0f, 1.0f
                  );
                  Object[] var10001 = new Object[1];
                  var10001[0] = p.ageSec;
                  String text = String.format("%.1fs", var10001);
                  this.renderIconRect(event, matrices, font, icon, screen, progress, text);
               }
            }

            Entity entity = mc.world.getEntityById(entry.getKey());
            if (entity instanceof FireworkRocketEntity && entity.isAlive()) {
               Vec3d currentPos = new Vec3d(
                  MathHelper.lerp((double)this.lastTickDelta, entity.lastRenderX, entity.getX()),
                  MathHelper.lerp((double)this.lastTickDelta, entity.lastRenderY, entity.getY())
                     + 0.5,
                  MathHelper.lerp((double)this.lastTickDelta, entity.lastRenderZ, entity.getZ())
               );
               Vec3d screen = this.worldToScreen(currentPos);
               if (screen != null) {
                  Object[] var20 = new Object[1];
                  var20[0] = (float)entity.age / 20.0f;
                  String text = String.format("%.1fs", var20);
                  this.renderIconRect(event, matrices, font, icon, screen, 1.0f, text);
               }
            }
         }
      }
   }
   public void renderIconRect(EventRender2D event, MatrixStack matrices, FontRenderer font, ItemStack icon, Vec3d screen, float progress, String text) {
      float iconScale = 0.6000000238418579f;
      float rectHeight = 12.0f;
      float padding = 2.5f;
      float gap = 2.0f;
      float textYOffset = 3.5f;
      float animScale = 0.3499999940395355f + 0.6499999761581421f * progress;
      int alpha = (int)(200.0f * progress);
      if (alpha > 5) {
         int bgColor = alpha << 24 | 657930;
         int textColor = alpha << 24 | 16777215;
         float textWidth = font != null ? font.getStringWidth(text) : 0.0f;
         float iconWidth = 16.0f * iconScale;
         float totalWidth = padding + iconWidth + gap + textWidth + padding;
         matrices.push();
         matrices.translate(screen.x, screen.y, 0.0);
         matrices.scale(animScale, animScale, 1.0f);
         RenderUtil.drawRoundedRect(
            matrices,
            -totalWidth / 2.0f,
            -rectHeight / 2.0f,
            totalWidth,
            rectHeight,
            0.0f,
            bgColor
         );
         float currentX = -totalWidth / 2.0f + padding;
         matrices.push();
         matrices.translate(
            currentX,
            -(16.0f * iconScale) / 2.0f,
            0.0f
         );
         matrices.scale(iconScale, iconScale, 1.0f);
         event.getContext().drawItem(icon, 0, 0);
         matrices.pop();
         currentX += iconWidth + gap;
         if (font != null) {
            font.drawString(
               matrices,
               text,
               currentX,
               -rectHeight / 2.0f
                  + textYOffset
                  + 0.5f,
               textColor
            );
         }

         matrices.pop();
      }
   }
   public Vec3d worldToScreen(Vec3d worldPos) {
      Vector3f relative = new Vector3f(
         (float)(worldPos.x - this.lastCameraPos.x), (float)(worldPos.y - this.lastCameraPos.y), (float)(worldPos.z - this.lastCameraPos.z)
      );
      relative.rotate(new Quaternionf(this.lastCameraRotation).conjugate());
      Vector4f clip = new Vector4f(relative.x, relative.y, relative.z, 1.0f);
      this.lastProjectionMatrix.transform(clip);
      float w = clip.w;
      if (w <= 9.999999747378752e-06f) {
         return null;
      } else {
         float ndcX = clip.x / w;
         float ndcY = clip.y / w;
         float ndcZ = clip.z / w;
         float screenX = (ndcX * 0.5f + 0.5f)
            * (float)mc.getWindow().getScaledWidth();
         float screenY = (
               1.0f
                  - (ndcY * 0.5f + 0.5f)
            )
            * (float)mc.getWindow().getScaledHeight();
         return !Float.isNaN(screenX) && !Float.isNaN(screenY) && !Float.isInfinite(screenX) && !Float.isInfinite(screenY)
            ? new Vec3d((double)screenX, (double)screenY, (double)ndcZ)
            : null;
      }
   }
}
