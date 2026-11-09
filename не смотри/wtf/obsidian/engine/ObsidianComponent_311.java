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
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.hit.HitResult.Type;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.RaycastContext.ShapeType;

public final class ObsidianComponent_311 implements IInstance {
   public static Vec3d rotationPoint = Vec3d.ZERO;
   public static Vec3d rotationMotion = Vec3d.ZERO;

   public static Vec3d getRotationPoint() {
      return rotationPoint;
   }

   public static Vec3d getNearestPoint(Entity entity) {
      Box box = entity.getBoundingBox();
      double step = 0.1;
      Vec3d bestVec = null;
      double closestDistance = 179769313486231570814527423731704356798070567525844996598917476803157260780028538760589558632766878171540458953514382464234321326889464182768467546703537516986049910576551282076245490090389328944075868508455133942304583236903222948165808559332123348274797826204144723168738177180919299881250404026184124858368.0;

      for (double x = box.minX; x <= box.maxX; x += step) {
         for (double y = box.minY; y <= box.maxY; y += step) {
            for (double z = box.minZ; z <= box.maxZ; z += step) {
               Vec3d sample = new Vec3d(x, y, z);
               double dist = mc.player.getEyePos().distanceTo(sample);
               if (dist < closestDistance) {
                  closestDistance = dist;
                  bestVec = sample;
               }
            }
         }
      }

      return bestVec;
   }

   public static Vec3d getPoint(Entity target) {
      Box box = target.getBoundingBox();
      double width = box.maxX - box.minX;
      double height = box.maxY - box.minY;
      double depth = box.maxZ - box.minZ;
      double baseX = box.minX + width / 2.0;
      double baseY = box.minY + height * 0.7;
      double baseZ = box.minZ + depth / 2.0;
      double time = (double)System.currentTimeMillis() / 50.0;
      int id = target.getId();
      double offsetX = Math.sin(time + (double)id) * width * 0.45;
      double offsetY = Math.cos(time * 0.8 + (double)id)
         * height
         * 0.1;
      double offsetZ = Math.cos(time * 1.2 + (double)id)
         * depth
         * 0.45;
      return new Vec3d(baseX + offsetX, baseY + offsetY, baseZ + offsetZ);
   }

   public static Vec3d getPoint2(Entity target) {
      Box box = target.getBoundingBox();
      double width = box.maxX - box.minX;
      double height = box.maxY - box.minY;
      double depth = box.maxZ - box.minZ;
      double baseX = box.minX + width / 2.0;
      double baseY = box.minY + height * 0.65;
      double baseZ = box.minZ + depth / 2.0;
      double time = (double)System.currentTimeMillis() / 65.0;
      int id = target.getId();
      double offsetX = Math.sin(time + (double)id) * width * 0.7;
      double offsetY = Math.cos(time * 0.8 + (double)id)
         * height
         * 0.4;
      double offsetZ = Math.cos(time * 1.2 + (double)id)
         * depth
         * 0.7;
      return new Vec3d(baseX + offsetX, baseY + offsetY, baseZ + offsetZ);
   }

   public static Vec3d getNearestVisiblePoint(Entity target, Vec3d preferredPoint, double range) {
      if (preferredPoint != null && mc.player != null && mc.world != null) {
         if (isPointVisible(target, preferredPoint, range)) {
            return preferredPoint;
         } else {
            Box box = target.getBoundingBox();
            double step = 0.12;
            Vec3d bestPoint = null;
            double bestDistance = 179769313486231570814527423731704356798070567525844996598917476803157260780028538760589558632766878171540458953514382464234321326889464182768467546703537516986049910576551282076245490090389328944075868508455133942304583236903222948165808559332123348274797826204144723168738177180919299881250404026184124858368.0;

            for (double x = box.minX; x <= box.maxX; x += step) {
               for (double y = box.minY; y <= box.maxY; y += step) {
                  for (double z = box.minZ; z <= box.maxZ; z += step) {
                     Vec3d sample = new Vec3d(x, y, z);
                     if (isPointVisible(target, sample, range)) {
                        double distanceToCurrent = sample.squaredDistanceTo(preferredPoint);
                        if (distanceToCurrent < bestDistance) {
                           bestDistance = distanceToCurrent;
                           bestPoint = sample;
                        }
                     }
                  }
               }
            }

            return bestPoint != null ? bestPoint : preferredPoint;
         }
      } else {
         return preferredPoint;
      }
   }
   public static boolean isPointVisible(Entity target, Vec3d point, double range) {
      Vec3d eyePos = mc.player.getEyePos();
      double distance = eyePos.distanceTo(point);
      if (distance > range) {
         return false;
      } else {
         Vec3d direction = point.subtract(eyePos).normalize();
         if (!ObsidianComponent_184.rayTrace(
            direction, distance + 0.2, target.getBoundingBox()
         )) {
            return false;
         } else {
            BlockHitResult blockHit = ObsidianComponent_184.raycast(eyePos, point, ShapeType.COLLIDER, mc.player);
            return (!(blockHit.getType() != Type.MISS
                  && !(
                     eyePos.squaredDistanceTo(blockHit.getPos())
                        >= eyePos.squaredDistanceTo(point) - 0.0001
                  )));
         }
      }
   }

   public static Vec3d getMultipoint(Entity target, double distance) {
      float minMotionXZ = 0.004999999888241291f;
      float maxMotionXZ = 0.014999999664723873f;
      float minMotionY = 0.001500000013038516f;
      float maxMotionY = 0.014999999664723873f;
      double lenghtX = target.getBoundingBox().getLengthX();
      double lenghtY = target.getBoundingBox().getLengthY();
      double lenghtZ = target.getBoundingBox().getLengthZ();
      if (rotationMotion.equals(Vec3d.ZERO)) {
         rotationMotion = new Vec3d(
            MathLerpUtil.randomBest(
               Double.longBitsToDouble(-4641950208052166656L),
               0.019999999552965164
            ),
            MathLerpUtil.randomBest(
               Double.longBitsToDouble(-4641950208052166656L),
               0.019999999552965164
            ),
            MathLerpUtil.randomBest(
               Double.longBitsToDouble(-4641950208052166656L),
               0.019999999552965164
            )
         );
      }

      if (rotationPoint.equals(Vec3d.ZERO)) {
         rotationPoint = new Vec3d(
            0.0,
            lenghtY * 0.5,
            0.0
         );
      }

      rotationPoint = rotationPoint.add(rotationMotion);
      double safeX = (lenghtX - 0.1) / 2.0;
      double safeZ = (lenghtZ - 0.1)
         / 2.0;
      if (rotationPoint.x >= safeX) {
         rotationMotion = new Vec3d(-MathLerpUtil.randomBest((double)minMotionXZ, (double)maxMotionXZ), rotationMotion.getY(), rotationMotion.getZ());
      } else if (rotationPoint.x <= -safeX) {
         rotationMotion = new Vec3d(MathLerpUtil.randomBest((double)minMotionXZ, (double)maxMotionXZ), rotationMotion.getY(), rotationMotion.getZ());
      }

      if (rotationPoint.y >= lenghtY * 0.75) {
         rotationMotion = new Vec3d(rotationMotion.getX(), -MathLerpUtil.randomBest((double)minMotionY, (double)maxMotionY), rotationMotion.getZ());
      } else if (rotationPoint.y <= lenghtY * 0.3) {
         rotationMotion = new Vec3d(rotationMotion.getX(), MathLerpUtil.randomBest((double)minMotionY, (double)maxMotionY), rotationMotion.getZ());
      }

      if (rotationPoint.z >= safeZ) {
         rotationMotion = new Vec3d(rotationMotion.getX(), rotationMotion.getY(), -MathLerpUtil.randomBest((double)minMotionXZ, (double)maxMotionXZ));
      } else if (rotationPoint.z <= -safeZ) {
         rotationMotion = new Vec3d(rotationMotion.getX(), rotationMotion.getY(), MathLerpUtil.randomBest((double)minMotionXZ, (double)maxMotionXZ));
      }

      rotationPoint.add(
         MathLerpUtil.randomBest(
            Double.longBitsToDouble(-4636005456307814400L),
            0.05000000074505806
         ),
         0.0,
         MathLerpUtil.randomBest(
            Double.longBitsToDouble(-4636005456307814400L),
            0.05000000074505806
         )
      );
      if (!ObsidianComponent_184.rayTrace(mc.player.getRotationVector(), distance, target.getBoundingBox())) {
         float halfBox = (float)(lenghtX / 2.0)
            * 0.800000011920929f;

         for (float x1 = -halfBox; x1 <= halfBox; x1 += 0.10000000149011612f) {
            for (float z1 = -halfBox; z1 <= halfBox; z1 += 0.10000000149011612f) {
               for (float y1 = (float)(lenghtY * 0.9);
                  (double)y1 >= lenghtY * 0.3;
                  y1 -= 0.10000000149011612f
               ) {
                  Vec3d v1 = new Vec3d(target.getX() + (double)x1, target.getY() + (double)y1, target.getZ() + (double)z1);
                  ObsidianComponent_olorSetting rotation = ObsidianComponent_374.fromVec3d(v1);
                  if (ObsidianComponent_184.rayTrace(rotation.toVector(), distance, target.getBoundingBox())) {
                     rotationPoint = new Vec3d((double)x1, (double)y1, (double)z1);
                     return target.getPos().add(rotationPoint);
                  }
               }
            }
         }
      }

      return target.getPos().add(rotationPoint);
   }

   public static Vec3d getHumanizedMultipoint(Entity target, double distance) {
      if (target != null && mc.player != null) {
         Box box = target.getBoundingBox();
         double width = box.maxX - box.minX;
         double height = box.maxY - box.minY;
         double depth = box.maxZ - box.minZ;
         long now = System.currentTimeMillis();
         double time = (double)(now % (100000L))
            / 350.0;
         int id = target.getId();
         double baseX = box.minX + width * 0.5;
         double baseY = box.minY
            + height * 0.55
            + Math.sin(
                  time * 0.65
                     + (double)id * 1.1
               )
               * height
               * 0.18;
         double baseZ = box.minZ + depth * 0.5;
         double offsetX = Math.sin(time + (double)id * 1.5)
            * width
            * 0.25;
         double offsetZ = Math.cos(
               time * 0.9
                  + (double)id * 2.1
            )
            * depth
            * 0.25;
         Vec3d targetPoint = new Vec3d(baseX + offsetX, baseY, baseZ + offsetZ);
         return !isPointVisible(target, targetPoint, distance)
            ? new Vec3d(baseX, box.minY + height * 0.65, baseZ)
            : targetPoint;
      } else {
         return Vec3d.ZERO;
      }
   }

   @Generated
   public ObsidianComponent_311() {
      throw new UnsupportedOperationException("This is a utility class and cannot be instantiated");
   }
}
