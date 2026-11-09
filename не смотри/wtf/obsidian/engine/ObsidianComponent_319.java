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
import net.minecraft.util.hit.HitResult.Type;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.RaycastContext;
import net.minecraft.world.RaycastContext.FluidHandling;
import net.minecraft.world.RaycastContext.ShapeType;

public final class ObsidianComponent_319 implements IInstance {
   public static Vec3d getClosestPoint(Entity entity) {
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

   public static Vec3d getNearestPoint(Entity entity, double distance) {
      Vec3d eyePos = mc.player.getCameraPosVec(1.0f);
      double rangeSquared = distance * distance;
      Box box = entity.getBoundingBox();
      Vec3d center = box.getCenter();
      Vec3d bestPoint = null;
      double bestDistance = Double.POSITIVE_INFINITY;

      for (double x = box.minX; x <= box.maxX; x += 0.1) {
         for (double y = box.minY; y <= box.maxY; y += 0.1) {
            for (double z = box.minZ; z <= box.maxZ; z += 0.1) {
               Vec3d point = new Vec3d(x, y, z);
               if (!(eyePos.squaredDistanceTo(point) > rangeSquared)) {
                  HitResult result = mc.world.raycast(new RaycastContext(eyePos, point, ShapeType.OUTLINE, FluidHandling.NONE, mc.player));
                  if (result.getType() != Type.BLOCK) {
                     double distToCenter = center.squaredDistanceTo(point);
                     if (distToCenter < bestDistance) {
                        bestDistance = distToCenter;
                        bestPoint = point;
                     }
                  }
               }
            }
         }
      }

      return bestPoint != null ? bestPoint : center;
   }

   @Generated
   public ObsidianComponent_319() {
      throw new UnsupportedOperationException("This is a utility class and cannot be instantiated");
   }
}
