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

























import net.minecraft.util.math.Vec3d;

public class FlightModule extends Module {
   public static FlightModule INSTANCE = new FlightModule();
   private final SliderSetting speed = new SliderSetting(
      "Скорость",
      2.0f,
      0.10000000149011612f,
      10.0f,
      0.10000000149011612f
   );

   public FlightModule() {
      super("Flight", "Полёт", Category.MOVEMENT);
      Setting[] var10001 = new Setting[1];
      var10001[0] = this.speed;
      this.addSettings(var10001);
   }

   @EventTarget
   public void onUpdate(EventTick event) {
      if (mc.player != null) {
         double spd = (double)this.speed.get();
         float yaw = (float)Math.toRadians((double)mc.player.getYaw());
         double motionX = 0.0;
         double motionY = 0.0;
         double motionZ = 0.0;
         double forward = 0.0;
         double strafe = 0.0;
         if (mc.options.forwardKey.isPressed()) {
            forward += 1.0;
         }

         if (mc.options.backKey.isPressed()) {
            forward -= 1.0;
         }

         if (mc.options.leftKey.isPressed()) {
            strafe += 1.0;
         }

         if (mc.options.rightKey.isPressed()) {
            strafe -= 1.0;
         }

         if (forward != 0.0
            || strafe != 0.0) {
            double angle = Math.atan2(forward, strafe) - 1.5707963267948966;
            motionX = -Math.sin((double)yaw + angle) * spd;
            motionZ = Math.cos((double)yaw + angle) * spd;
         }

         if (mc.options.jumpKey.isPressed()) {
            motionY = spd;
         } else if (mc.options.sneakKey.isPressed()) {
            motionY = -spd;
         }

         mc.player.setVelocity(new Vec3d(motionX, motionY, motionZ));
      }
   }

   @Override
   public void onDisable() {
      super.onDisable();
      if (mc.player != null) {
         mc.player.setVelocity(Vec3d.ZERO);
      }
   }
}
