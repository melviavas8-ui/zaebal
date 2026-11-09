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

























import java.util.concurrent.ThreadLocalRandom;
import net.minecraft.util.math.Vec3d;

public class GrimGlideModule extends Module {
   public static GrimGlideModule INSTANCE = new GrimGlideModule();
   public long lastTickTime = 0L;
   public int ticksTwo = 0;

   public GrimGlideModule() {
      super("GrimGlide", "Ускорение на элитре без фееров", Category.MOVEMENT);
   }

   @EventTarget
   public void onUpdate(EventTick event) {
      if (mc.player != null && mc.world != null && mc.player.isGliding()) {
         this.ticksTwo += 1;
         Vec3d pos = mc.player.getPos();
         float yaw = mc.player.getYaw();
         double forward = mc.player.age % 2 == 0
            ? 0.087
            : 0.09;
         double dx = -Math.sin(Math.toRadians((double)yaw)) * forward;
         double dz = Math.cos(Math.toRadians((double)yaw)) * forward;
         if (System.currentTimeMillis() - this.lastTickTime >= (40L)) {
            mc.player.setPosition(pos.getX() + dx, pos.getY(), pos.getZ() + dz);
            this.lastTickTime = System.currentTimeMillis();
         }

         if (this.ticksTwo % (40) == 0) {
            mc.player
               .setVelocity(
                  dx
                     * (double)ThreadLocalRandom.current()
                        .nextFloat(1.0010000467300415f, 1.0020999908447266f),
                  mc.player.getVelocity().y + 0.00600000075995922,
                  dz
                     * (double)ThreadLocalRandom.current()
                        .nextFloat(1.0010000467300415f, 1.0020999908447266f)
               );
         }
      }
   }

   @Override
   public void onEnable() {
      super.onEnable();
      this.ticksTwo = 0;
      this.lastTickTime = System.currentTimeMillis();
   }

   @Override
   public void onDisable() {
      this.ticksTwo = 0;
      super.onDisable();
   }
}
