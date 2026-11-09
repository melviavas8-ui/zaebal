package wtf.obsidian.modules.movement;

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
import net.minecraft.util.math.Vec3d;
import org.jetbrains.annotations.NotNull;

public class SpeedModule extends Module implements IInstance {
   public static SpeedModule INSTANCE = new SpeedModule();
   private final SliderSetting speed = new SliderSetting(
      "Скорость",
      1.0f,
      0.10000000149011612f,
      2.0f,
      0.009999999776482582f
   );
   private final SliderSetting radius = new SliderSetting(
      "Радиус",
      1.0f,
      0.009999999776482582f,
      3.0f,
      0.10000000149011612f
   );
   private final SliderSetting predict = new SliderSetting(
      "Предикт",
      1.0f,
      0.0f,
      5.0f,
      0.10000000149011612f
   );
   private final BooleanSetting onlyElytra = new BooleanSetting("Только на элитре", false);

   public SpeedModule() {
      super("Speed", "Дополнительное ускорение", Category.MOVEMENT);
      Setting[] var10001 = new Setting[4];
      var10001[0] = this.speed;
      var10001[1] = this.radius;
      var10001[2] = this.predict;
      var10001[3] = this.onlyElytra;
      this.addSettings(var10001);
   }

   @EventTarget
   public void onUpdate(EventTick event) {
      if (mc.player != null && mc.world != null) {
         this.collisionSpeed();
      }
   }
   public void collisionSpeed() {
      KillAuraModule aura = ModuleManager.aura;
      if (aura != null && aura.isEnable()) {
         LivingEntity target = aura.getTarget();
         if (target != null && target != mc.player) {
            if (!this.onlyElytra.isState() || mc.player.isGliding()) {
               Box expandedBox = mc.player.getBoundingBox().expand(this.radius.getValue().doubleValue());
               boolean canSpeed = false;
               if (mc.player.isGliding() || target.getBoundingBox().intersects(expandedBox)) {
                  if (mc.player.isGliding()) {
                     Vec3d predictedPos = ObsidianComponent_215.predict(target, target.getPos(), this.predict.getValue().intValue());
                     double distanceToPredict = mc.player.getEyePos().distanceTo(predictedPos);
                     double distanceToTarget = mc.player.getEyePos().distanceTo(target.getBoundingBox().getCenter());
                     if (distanceToPredict <= 2.5
                        || distanceToTarget <= 2.5) {
                        canSpeed = true;
                     }
                  } else {
                     canSpeed = true;
                  }
               }

               if (canSpeed) {
                  Vec3d newVelocity = this.calculateVelocity(target);
                  mc.player.setVelocity(newVelocity);
               }
            }
         }
      }
   }

   @NotNull
   public Vec3d calculateVelocity(LivingEntity target) {
      Vec3d predictedPos = ObsidianComponent_215.predict(target, target.getPos(), this.predict.getValue().intValue());
      double deltaX = predictedPos.x - mc.player.getX();
      double deltaZ = predictedPos.z - mc.player.getZ();
      float targetYaw = (float)(
         Math.toDegrees(Math.atan2(deltaZ, deltaX)) - 90.0
      );
      double radYaw = Math.toRadians((double)targetYaw);
      double force = 0.072 * this.speed.getValue().doubleValue();
      Vec3d currentVelocity = mc.player.getVelocity();
      return new Vec3d(currentVelocity.x + -Math.sin(radYaw) * force, currentVelocity.y, currentVelocity.z + Math.cos(radYaw) * force);
   }
}
