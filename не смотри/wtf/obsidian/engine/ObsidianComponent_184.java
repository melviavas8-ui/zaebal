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
























import java.util.Objects;
import lombok.Generated;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.projectile.ProjectileUtil;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.hit.EntityHitResult;
import net.minecraft.util.hit.HitResult;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.RaycastContext;
import net.minecraft.world.RaycastContext.FluidHandling;
import net.minecraft.world.RaycastContext.ShapeType;
import org.joml.Vector3f;

public final class ObsidianComponent_184 implements IInstance {
   public static HitResult rayTrace(double rayTraceDistance, float yaw, float pitch, Entity entity) {
      Vec3d startVec = mc.player.getEyePos();
      Vec3d directionVec = getVectorForRotation(pitch, yaw);
      Vec3d endVec = startVec.add(directionVec.x * rayTraceDistance, directionVec.y * rayTraceDistance, directionVec.z * rayTraceDistance);
      return mc.world.raycast(new RaycastContext(startVec, endVec, ShapeType.OUTLINE, FluidHandling.NONE, entity));
   }

   public static BlockHitResult raycast(Vec3d start, Vec3d end, ShapeType shapeType) {
      return raycast(start, end, shapeType, mc.player);
   }

   public static BlockHitResult raycast(Vec3d start, Vec3d end, ShapeType shapeType, Entity entity) {
      return mc.world.raycast(new RaycastContext(start, end, shapeType, FluidHandling.NONE, entity));
   }

   public static boolean rayTrace(Vec3d clientVec, double range, Box box) {
      Vec3d cameraVec = Objects.requireNonNull(mc.player).getEyePos();
      return (!(!box.contains(cameraVec) && !box.raycast(cameraVec, cameraVec.add(clientVec.multiply(range))).isPresent()));
   }

   public static boolean isViewEntity(LivingEntity target, float yaw, float pitch, float distance, boolean ignoreWalls) {
      Entity entity = mc.getCameraEntity();
      if (entity != null && mc.world != null) {
         double reachDistanceSquared = (double)(distance * distance);
         Vec3d startVec = entity.getEyePos();
         Vector3f directionVec = calculateViewVector(yaw, pitch);
         directionVec.mul(distance, distance, distance);
         Vec3d endVec = startVec.add((double)directionVec.x, (double)directionVec.y, (double)directionVec.z);
         Box aabb = target.getBoundingBox();
         EntityHitResult result = ProjectileUtil.raycast(
            entity,
            startVec,
            endVec,
            aabb,
            entityIn -> (!entityIn.isSpectator() && entityIn.isAlive() && entityIn == target),
            reachDistanceSquared
         );
         return (result != null);
      } else {
         return false;
      }
   }

   public static Vector3f calculateViewVector(float yaw, float pitch) {
      float pitchRad = pitch * 0.01745329238474369f;
      float yawRad = -yaw * 0.01745329238474369f;
      float cosYaw = MathHelper.cos(yawRad);
      float sinYaw = MathHelper.sin(yawRad);
      float cosPitch = MathHelper.cos(pitchRad);
      float sinPitch = MathHelper.sin(pitchRad);
      return new Vector3f(sinYaw * cosPitch, -sinPitch, cosYaw * cosPitch);
   }

   public static Vec3d getVectorForRotation(float pitch, float yaw) {
      float yawRadians = -yaw * 0.01745329238474369f - 3.1415927410125732f;
      float pitchRadians = -pitch * 0.01745329238474369f;
      float cosYaw = MathHelper.cos(yawRadians);
      float sinYaw = MathHelper.sin(yawRadians);
      float cosPitch = -MathHelper.cos(pitchRadians);
      float sinPitch = MathHelper.sin(pitchRadians);
      return new Vec3d((double)(sinYaw * cosPitch), (double)sinPitch, (double)(cosYaw * cosPitch));
   }

   public static boolean rayTraceSingleEntity(float yaw, float pitch, double distance, Entity entity) {
      Vec3d eyeVec = mc.player.getEyePos();
      Vec3d lookVec = mc.player.getRotationVector(pitch, yaw);
      Vec3d extendedVec = eyeVec.add(lookVec.multiply(distance));
      Box AABB = entity.getBoundingBox();
      return (!(!AABB.contains(eyeVec) && !AABB.raycast(eyeVec, extendedVec).isPresent()));
   }

   @Generated
   public ObsidianComponent_184() {
      throw new UnsupportedOperationException("This is a utility class and cannot be instantiated");
   }
}
