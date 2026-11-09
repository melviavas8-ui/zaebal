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
























import net.minecraft.client.render.BufferBuilder;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.LivingEntity;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.RotationAxis;
import net.minecraft.util.math.Vec3d;
import org.joml.Matrix4f;
public class ObsidianComponent_238 implements IInstance {
   public double x;
   public double y;
   public double z;
   public double worldX;
   public double worldY;
   public double worldZ;
   public long time;
   public LivingEntity entity;
   public boolean fading;
   public long fadeStartTime;
   public float vx;
   public float vy;
   public float vz;
   public float rotX;
   public float rotY;
   public float rotZ;
   public float rotSpeedX;
   public float rotSpeedY;
   public float rotSpeedZ;

   public ObsidianComponent_238(LivingEntity entity, double x, double y, double z) {
      this.entity = entity;
      this.x = x;
      this.y = y;
      this.z = z;
      this.time = System.currentTimeMillis();
      this.rotX = (float)(Math.random() * 360.0);
      this.rotY = (float)(Math.random() * 360.0);
      this.rotZ = (float)(Math.random() * 360.0);
      this.rotSpeedX = 1.399999976158142f
         + (float)Math.random() * 3.4000000953674316f;
      this.rotSpeedY = 1.399999976158142f
         + (float)Math.random() * 3.4000000953674316f;
      this.rotSpeedZ = 1.399999976158142f
         + (float)Math.random() * 3.4000000953674316f;
      this.vx = (float)(
         (Math.random() - 0.5)
            * 0.0022
      );
      this.vy = 0.03099999949336052f + (float)Math.random() * 0.019999999552965164f;
      this.vz = (float)(
         (Math.random() - 0.5)
            * 0.0022
      );
   }

   public void update(float dt, long now, LivingEntity currentTarget) {
      float step = dt * 60.0f;
      this.rotX = this.rotX + this.rotSpeedX * step;
      this.rotY = this.rotY + this.rotSpeedY * step;
      this.rotZ = this.rotZ + this.rotSpeedZ * step;
      if (!this.fading) {
         this.x = this.x + (double)(this.vx * step);
         this.y = this.y + (double)(this.vy * step);
         this.z = this.z + (double)(this.vz * step);
         this.vx = this.vx * 0.9919999837875366f;
         this.vz = this.vz * 0.9919999837875366f;
         this.vy = this.vy * 0.9890000224113464f;
         if (this.entity != null) {
            double shoulderHeight = Math.max(
               2.2,
               (double)this.entity.getHeight() * 1.85
            );
            if (this.y >= shoulderHeight) {
               this.y = shoulderHeight;
               this.beginFade(now);
               return;
            }
         }

         boolean targetLost = (!(currentTarget != null && this.entity != null && this.entity.isAlive() && this.entity == currentTarget));
         if (targetLost || now - this.time >= (560L)) {
            this.beginFade(now);
         }
      }
   }

   public boolean shouldRemove(long now) {
      return (this.fading && now - this.fadeStartTime >= (320L));
   }

   public int getRenderColor(int baseColor, int redColor, float hurtPC, long now) {
      float alpha = this.getAlpha(now);
      if (alpha <= 0.0010000000474974513f) {
         return 0;
      } else {
         int color = ObsidianComponent_olorUtil.replAlpha(baseColor, (int)(alpha * 255.0f));
         int hurt = ObsidianComponent_olorUtil.replAlpha(redColor, (int)(alpha * 255.0f));
         return TargetESPModule.INSTANCE.overCol(color, hurt, hurtPC);
      }
   }

   public boolean appendCubeFaces(BufferBuilder faceBuilder, MatrixStack ms, Vec3d cam, float partialTicks, int color) {
      float alpha = (float)(color >> (24) & 255)
         / 255.0f;
      if (alpha <= 0.0010000000474974513f) {
         return false;
      } else {
         Vec3d renderPos = this.getRenderPos(partialTicks);
         if (renderPos == null) {
            return false;
         } else {
            float fadeScale = this.fading
               ? MathHelper.lerp(
                  MathHelper.clamp(
                     (float)(System.currentTimeMillis() - this.fadeStartTime) / 320.0f,
                     0.0f,
                     1.0f
                  ),
                  1.0f,
                  0.44999998807907104f
               )
               : 1.0f;
            float scale = 0.11999999731779099f * fadeScale;
            ms.push();
            ms.translate(renderPos.x - cam.x, renderPos.y - cam.y, renderPos.z - cam.z);
            ms.multiply(RotationAxis.POSITIVE_X.rotationDegrees(this.rotX));
            ms.multiply(RotationAxis.POSITIVE_Y.rotationDegrees(this.rotY));
            ms.multiply(RotationAxis.POSITIVE_Z.rotationDegrees(this.rotZ));
            ms.scale(scale, scale, scale);
            Matrix4f m = ms.peek().getPositionMatrix();
            this.appendFaces(faceBuilder, m, color);
            ms.pop();
            return true;
         }
      }
   }

   public boolean appendCubeLines(BufferBuilder lineBuilder, MatrixStack ms, Vec3d cam, float partialTicks, int color) {
      float alpha = (float)(color >> (24) & (255))
         / 255.0f;
      if (alpha <= 0.0010000000474974513f) {
         return false;
      } else {
         Vec3d renderPos = this.getRenderPos(partialTicks);
         if (renderPos == null) {
            return false;
         } else {
            float fadeScale = this.fading
               ? MathHelper.lerp(
                  MathHelper.clamp(
                     (float)(System.currentTimeMillis() - this.fadeStartTime) / 320.0f,
                     0.0f,
                     1.0f
                  ),
                  1.0f,
                  0.44999998807907104f
               )
               : 1.0f;
            float scale = 0.11999999731779099f * fadeScale;
            ms.push();
            ms.translate(renderPos.x - cam.x, renderPos.y - cam.y, renderPos.z - cam.z);
            ms.multiply(RotationAxis.POSITIVE_X.rotationDegrees(this.rotX));
            ms.multiply(RotationAxis.POSITIVE_Y.rotationDegrees(this.rotY));
            ms.multiply(RotationAxis.POSITIVE_Z.rotationDegrees(this.rotZ));
            ms.scale(scale, scale, scale);
            Matrix4f m = ms.peek().getPositionMatrix();
            this.appendEdges(
               lineBuilder,
               m,
               ObsidianComponent_olorUtil.replAlpha(
                  color,
                  Math.max(
                     1,
                     (int)(
                        (float)(color >> (24) & 255)
                           * 0.699999988079071f
                     )
                  )
               )
            );
            ms.pop();
            return true;
         }
      }
   }

   public boolean appendBloom(BufferBuilder builder, MatrixStack ms, Vec3d camPos, float camYaw, float camPitch, float partialTicks, int colorInt, long now) {
      float alpha = this.getAlpha(now);
      if (alpha <= 0.0010000000474974513f) {
         return false;
      } else {
         Vec3d renderPos = this.getRenderPos(partialTicks);
         if (renderPos == null) {
            return false;
         } else {
            float fadeScale = this.fading
               ? MathHelper.lerp(
                  MathHelper.clamp(
                     (float)(now - this.fadeStartTime) / 320.0f,
                     0.0f,
                     1.0f
                  ),
                  1.0f,
                  0.55F
               )
               : 1.0f;
            float glowScale = 0.949999988079071f * fadeScale;
            int ai = (int)(
               alpha * 0.15000000596046448f * 255.0f
            );
            if (ai <= 0) {
               return false;
            } else {
               int r = colorInt >> (16) & (255);
               int g = colorInt >> 8 & 255;
               int b = colorInt & (255);
               ms.push();
               ms.translate(renderPos.x - camPos.x, renderPos.y - camPos.y, renderPos.z - camPos.z);
               ms.multiply(RotationAxis.POSITIVE_Y.rotationDegrees(-camYaw));
               ms.multiply(RotationAxis.POSITIVE_X.rotationDegrees(camPitch));
               ms.scale(glowScale, glowScale, glowScale);
               Matrix4f m = ms.peek().getPositionMatrix();
               builder.vertex(
                     m,
                     -0.5f,
                     0.5f,
                     0.0f
                  )
                  .texture(0.0f, 1.0f)
                  .color(r, g, b, ai);
               builder.vertex(
                     m,
                     0.5f,
                     0.5f,
                     0.0f
                  )
                  .texture(1.0f, 1.0f)
                  .color(r, g, b, ai);
               builder.vertex(
                     m,
                     0.5f,
                     -0.5f,
                     0.0f
                  )
                  .texture(1.0f, 0.0f)
                  .color(r, g, b, ai);
               builder.vertex(
                     m,
                     -0.5f,
                     -0.5f,
                     0.0f
                  )
                  .texture(0.0f, 0.0f)
                  .color(r, g, b, ai);
               ms.pop();
               return true;
            }
         }
      }
   }
   public void beginFade(long now) {
      if (!this.fading) {
         Vec3d renderPos = this.getRenderPos(1.0f);
         if (renderPos != null) {
            this.worldX = renderPos.x;
            this.worldY = renderPos.y;
            this.worldZ = renderPos.z;
         }

         this.fadeStartTime = now;
         this.fading = true;
         this.entity = null;
      }
   }
   public float getAlpha(long now) {
      if (!this.fading) {
         float fadeIn = MathHelper.clamp(
            (float)(now - this.time) / 140.0f,
            0.0f,
            1.0f
         );
         float preFade = 1.0f
            - MathHelper.clamp(
               (float)(now - this.time - (440L))
                  / 120.0f,
               0.0f,
               0.3499999940395355f
            );
         return fadeIn * preFade;
      } else {
         return 1.0f
            - MathHelper.clamp(
               (float)(now - this.fadeStartTime) / 320.0f,
               0.0f,
               1.0f
            );
      }
   }
   public Vec3d getRenderPos(float partialTicks) {
      return !this.fading && this.entity != null
         ? new Vec3d(
            MathHelper.lerp((double)partialTicks, this.entity.lastRenderX, this.entity.getX()) + this.x,
            MathHelper.lerp((double)partialTicks, this.entity.lastRenderY, this.entity.getY()) + this.y,
            MathHelper.lerp((double)partialTicks, this.entity.lastRenderZ, this.entity.getZ()) + this.z
         )
         : new Vec3d(this.worldX, this.worldY, this.worldZ);
   }
   public void appendFaces(BufferBuilder fb, Matrix4f m, int color) {
      float min = -0.5f;
      float max = 0.5f;
      int fillColor = ObsidianComponent_olorUtil.replAlpha(
         color,
         Math.max(
            1,
            (int)(
               (float)(color >> 24 & 255)
                  * 0.1599999964237213f
            )
         )
      );
      this.addFace(fb, m, min, min, min, max, max, max, fillColor);
   }
   public void appendEdges(BufferBuilder buf, Matrix4f m, int color) {
      byte[][] var4 = TargetESPModule.CUBE_EDGES;
      int var5 = var4.length;

      for (int var6 = 0; var6 < var5; var6++) {
         byte[] edge = var4[var6];
         buf.vertex(
               m,
               (float)edge[0] * 0.5f,
               (float)edge[1] * 0.5f,
               (float)edge[2] * 0.5f
            )
            .color(color);
         buf.vertex(
               m,
               (float)edge[3] * 0.5f,
               (float)edge[4] * 0.5f,
               (float)edge[5] * 0.5f
            )
            .color(color);
      }
   }
   public void addFace(BufferBuilder buf, Matrix4f m, float x1, float y1, float z1, float x2, float y2, float z2, int color) {
      buf.vertex(m, x1, y1, z1).color(color);
      buf.vertex(m, x2, y1, z1).color(color);
      buf.vertex(m, x2, y1, z2).color(color);
      buf.vertex(m, x1, y1, z2).color(color);
      buf.vertex(m, x1, y2, z1).color(color);
      buf.vertex(m, x1, y2, z2).color(color);
      buf.vertex(m, x2, y2, z2).color(color);
      buf.vertex(m, x2, y2, z1).color(color);
      buf.vertex(m, x1, y1, z1).color(color);
      buf.vertex(m, x1, y2, z1).color(color);
      buf.vertex(m, x2, y2, z1).color(color);
      buf.vertex(m, x2, y1, z1).color(color);
      buf.vertex(m, x1, y1, z2).color(color);
      buf.vertex(m, x2, y1, z2).color(color);
      buf.vertex(m, x2, y2, z2).color(color);
      buf.vertex(m, x1, y2, z2).color(color);
      buf.vertex(m, x1, y1, z1).color(color);
      buf.vertex(m, x1, y1, z2).color(color);
      buf.vertex(m, x1, y2, z2).color(color);
      buf.vertex(m, x1, y2, z1).color(color);
      buf.vertex(m, x2, y1, z1).color(color);
      buf.vertex(m, x2, y2, z1).color(color);
      buf.vertex(m, x2, y2, z2).color(color);
      buf.vertex(m, x2, y1, z2).color(color);
   }
}
