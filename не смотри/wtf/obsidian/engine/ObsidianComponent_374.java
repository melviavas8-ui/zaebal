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
























import lombok.Generated;
import net.minecraft.entity.Entity;
import net.minecraft.util.hit.HitResult;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec2f;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.RaycastContext;
import net.minecraft.world.RaycastContext.FluidHandling;
import net.minecraft.world.RaycastContext.ShapeType;
import org.jetbrains.annotations.NotNull;

public final class ObsidianComponent_374 implements IInstance {
   public static HitResult rayTrace(double dst, float yaw, float pitch) {
      Vec3d vec3d = mc.player.getCameraPosVec(1.0f);
      Vec3d vec3d2 = getRotationVector(pitch, yaw);
      Vec3d vec3d3 = vec3d.add(vec3d2.x * dst, vec3d2.y * dst, vec3d2.z * dst);
      return mc.world.raycast(new RaycastContext(vec3d, vec3d3, ShapeType.OUTLINE, FluidHandling.NONE, mc.player));
   }

   public static Vec3d getBestVector(Entity entity) {
      Vec3d eyePos = mc.player.getEyePos();
      Box box = entity.getBoundingBox();
      double step = 0.1;
      Vec3d bestVec = null;
      double closestDistance = 179769313486231570814527423731704356798070567525844996598917476803157260780028538760589558632766878171540458953514382464234321326889464182768467546703537516986049910576551282076245490090389328944075868508455133942304583236903222948165808559332123348274797826204144723168738177180919299881250404026184124858368.0;

      for (double x = box.minX; x <= box.maxX; x += step) {
         for (double y = box.minY; y <= box.maxY; y += step) {
            for (double z = box.minZ; z <= box.maxZ; z += step) {
               Vec3d sample = new Vec3d(x, y, z);
               double dist = eyePos.distanceTo(sample);
               if (dist < closestDistance) {
                  closestDistance = dist;
                  bestVec = sample;
               }
            }
         }
      }

      return bestVec;
   }

   public static ObsidianComponent_olorSetting fromVec3d(Vec3d vector) {
      return new ObsidianComponent_olorSetting(
         (float)MathHelper.wrapDegrees(
            Math.toDegrees(Math.atan2(vector.z, vector.x)) - 90.0
         ),
         (float)MathHelper.wrapDegrees(Math.toDegrees(-Math.atan2(vector.y, Math.hypot(vector.x, vector.z))))
      );
   }

   @NotNull
   public static Vec3d getRotationVector(float yaw, float pitch) {
      return new Vec3d(
         (double)(
            MathHelper.sin(-pitch * 0.01745329238474369f)
               * MathHelper.cos(yaw * 0.01745329238474369f)
         ),
         (double)(-MathHelper.sin(yaw * 0.01745329238474369f)),
         (double)(
            MathHelper.cos(-pitch * 0.01745329238474369f)
               * MathHelper.cos(yaw * 0.01745329238474369f)
         )
      );
   }

   public static Vec2f getRotations(Entity entity) {
      return getRotations(entity.getX(), entity.getY(), entity.getZ());
   }

   public static Vec2f getRotations(Vec3d vec3d) {
      return getRotations(vec3d.x, vec3d.y, vec3d.z);
   }

   public static Vec2f getRotations(double x, double y, double z) {
      double deltaX = x - mc.player.getX();
      double deltaY = y - mc.player.getEyeY();
      double deltaZ = z - mc.player.getZ();
      double distance = (double)MathHelper.sqrt((float)(deltaX * deltaX + deltaZ * deltaZ));
      float yaw = (float)(
         MathHelper.atan2(deltaZ, deltaX) * 57.29577951308232
            - 90.0
      );
      float pitch = (float)(-MathHelper.atan2(deltaY, distance) * 57.29577951308232);
      return new Vec2f(yaw, pitch);
   }

         public static float[] getRotations(Direction direction) {
      float[] var10000;
      switch (direction) {
         case DOWN:
            var10000 = new float[2];
            var10000[0] = mc.player.getYaw();
            var10000[1] = 90.0f;
            break;
         case UP:
            var10000 = new float[2];
            var10000[0] = mc.player.getYaw();
            var10000[1] = -90.0f;
            break;
         case NORTH:
            var10000 = new float[2];
            var10000[0] = 180.0f;
            var10000[1] = mc.player.getPitch();
            break;
         case SOUTH:
            var10000 = new float[2];
            var10000[0] = 0.0f;
            var10000[1] = mc.player.getPitch();
            break;
         case WEST:
            var10000 = new float[2];
            var10000[0] = 90.0f;
            var10000[1] = mc.player.getPitch();
            break;
         case EAST:
            var10000 = new float[2];
            var10000[0] = -90.0f;
            var10000[1] = mc.player.getPitch();
            break;
         default:
            throw new MatchException(null, null);
      }

      return var10000;
   }

   public static float[] correctRotation(float[] rotations) {
      rotations[0] = rotations[0]
         - rotations[0] % ObsidianComponent_390.getGCDValue();
      rotations[1] = rotations[1]
         - rotations[1] % ObsidianComponent_390.getGCDValue();
      float[] var10000 = new float[2];
      var10000[0] = rotations[0];
      var10000[1] = rotations[1];
      return var10000;
   }

   public static float getFixRotate(float rot) {
      return getDeltaMouse(rot) * ObsidianComponent_390.getGCDValue();
   }

   public static float getDeltaMouse(float delta) {
      return (float)Math.round(delta / ObsidianComponent_390.getGCDValue());
   }

   @Generated
   public ObsidianComponent_374() {
      throw new UnsupportedOperationException("This is a utility class and cannot be instantiated");
   }
}
