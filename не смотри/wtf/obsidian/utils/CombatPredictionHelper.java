package wtf.obsidian.utils;

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

























import net.minecraft.entity.LivingEntity;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Vec2f;
import net.minecraft.util.math.Vec3d;

public abstract class CombatPredictionHelper implements IInstance {
   public Vec2f rotate = Vec2f.ZERO;

   public abstract void updateRotations(LivingEntity var1);

   public static Vec2f correctRotation(float yaw, float pitch) {
      if ((yaw != -90.0f || pitch != 90.0f)
         && yaw != -180.0f) {
         float gcd = ObsidianComponent_390.getGCD();
         yaw -= yaw % gcd;
         pitch -= pitch % gcd;
         return new Vec2f(yaw, pitch);
      } else {
         return new Vec2f(mc.player.getYaw(), mc.player.getPitch());
      }
   }

   protected boolean shouldUseElytraPredict(LivingEntity target) {
      return false;
   }

   protected int getElytraPredictTicks() {
      return 0;
   }

   protected Vec3d getPredictedPoint(LivingEntity target, Vec3d point) {
      return !this.shouldUseElytraPredict(target) ? point : ObsidianComponent_215.bypasselytrahacking(target);
   }

   protected Box getPredictedBox(LivingEntity target) {
      Box box = target.getBoundingBox();
      if (!this.shouldUseElytraPredict(target)) {
         return box;
      } else {
         Vec3d currentCenter = box.getCenter();
         Vec3d predictedCenter = this.getPredictedPoint(target, currentCenter);
         return box.offset(predictedCenter.subtract(currentCenter));
      }
   }
}
