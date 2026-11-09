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
























import net.minecraft.block.BlockState;
import net.minecraft.client.MinecraftClient;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.math.BlockPos.Mutable;
public class ObsidianComponent_188 {
   public Vec3d position;
   public Vec3d velocity;
   public int color;
   public float size;
   public long lifeTime;
   public long birthTime;
   public float alpha = 1.0f;
   public float smoothFactor;
   public long lastUpdateNs;
   public double gravity;
public ObsidianComponent_188(Vec3d position, Vec3d velocity, int color, float size, long lifeTime, float smooth, double gravity) {
      this.position = position;
      this.velocity = velocity;
      this.color = color;
      this.size = size;
      this.lifeTime = lifeTime;
      this.birthTime = System.currentTimeMillis();
      this.lastUpdateNs = System.nanoTime();
      this.smoothFactor = smooth;
      this.gravity = gravity;
   }

   public boolean isDead() {
      return (System.currentTimeMillis() - this.birthTime >= this.lifeTime);
   }

   public void update(MinecraftClient mc) {
      long nowNs = System.nanoTime();
      double deltaSec = (double)(nowNs - this.lastUpdateNs) / 1000000000.0;
      this.lastUpdateNs = nowNs;
      float progress = Math.min(
         1.0f, (float)(System.currentTimeMillis() - this.birthTime) / (float)this.lifeTime
      );
      double factor = Math.pow(1.0 - (double)progress, (double)this.smoothFactor);
      double vx = this.velocity.x;
      double vy = this.velocity.y;
      double vz = this.velocity.z;
      double newX = this.position.x;
      double newY = this.position.y;
      double newZ = this.position.z;
      newX += vx * factor * deltaSec * 60.0;
      if (!checkCollision(newX, this.position.y, this.position.z, this.size, mc)) {
         vx = -vx * 0.8;
         newX = this.position.x;
      }

      newY += vy * factor * deltaSec * 60.0;
      if (!checkCollision(newX, newY, this.position.z, this.size, mc)) {
         vy = -vy * 1.5;
         newY = this.position.y;
      }

      newZ += vz * factor * deltaSec * 60.0;
      if (!checkCollision(newX, newY, newZ, this.size, mc)) {
         vz = -vz * 0.8;
         newZ = this.position.z;
      }

      this.position = new Vec3d(newX, newY, newZ);
      this.velocity = new Vec3d(
         vx * 0.9999,
         vy * 0.9999 - this.gravity,
         vz * 0.9999
      );
      this.alpha = 1.0f - progress;
   }

   public static boolean checkCollision(double x, double y, double z, float size, MinecraftClient mc) {
      if (mc.world == null) {
         return false;
      } else {
         double half = (double)size * 0.5;
         int minX = MathHelper.floor(x - half);
         int maxX = MathHelper.floor(x + half);
         int minY = MathHelper.floor(y - half);
         int maxY = MathHelper.floor(y + half);
         int minZ = MathHelper.floor(z - half);
         int maxZ = MathHelper.floor(z + half);
         Mutable pos = new Mutable();

         for (int bx = minX; bx <= maxX; bx++) {
            for (int by = minY; by <= maxY; by++) {
               for (int bz = minZ; bz <= maxZ; bz++) {
                  pos.set(bx, by, bz);
                  BlockState state = mc.world.getBlockState(pos);
                  if (!state.isAir() && state.isSolidBlock(mc.world, pos)) {
                     return false;
                  }
               }
            }
         }

         return true;
      }
   }
}
