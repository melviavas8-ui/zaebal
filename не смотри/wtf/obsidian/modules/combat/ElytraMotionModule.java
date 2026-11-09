package wtf.obsidian.modules.combat;

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

public class ElytraMotionModule extends Module {
   public static ElytraMotionModule INSTANCE = new ElytraMotionModule();
   public SliderSetting distance = new SliderSetting(
      "Дистанция до игрока",
      3.0f,
      0.0f,
      6.0f,
      0.10000000149011612f
   );
   public BooleanSetting bypass = new BooleanSetting("Обход", false);

   public ElytraMotionModule() {
      super("ElytraMotion", "Зависает рядом с игроком на эликах", Category.COMBAT);
      Setting[] var10001 = new Setting[2];
      var10001[0] = this.distance;
      var10001[1] = this.bypass;
      this.addSettings(var10001);
   }

   @EventTarget
   public void onMove(Event_321 e) {
      if (this.isEnable()) {
         KillAuraModule aura = ModuleManager.aura;
         if (mc.player != null && mc.world != null && aura.getTarget() != null) {
            if (mc.player.isGliding() && mc.player.distanceTo(aura.getTarget()) < this.distance.getValue().floatValue()) {
               if (this.bypass.isState()) {
                  float yaw = mc.player.getYaw();
                  double rad = Math.toRadians((double)yaw);
                  double forward = 0.01;
                  double down = Double.longBitsToDouble(-4676364914835832019L);
                  double moveX = -Math.sin(rad) * forward;
                  double moveZ = Math.cos(rad) * forward;
                  e.setMovePos(new Vec3d(moveX, down, moveZ));
               } else {
                  e.setMovePos(Vec3d.ZERO);
               }
            }
         }
      }
   }

   @Override
   public void onDisable() {
   }
}
