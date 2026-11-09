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
import net.minecraft.util.math.MathHelper;

public class ObsidianComponent_142 implements IInstance {
   public static boolean active;
   public static float freeYaw;
   public static float freePitch;

   public ObsidianComponent_142() {
      EventManager.register(this);
   }

   public static boolean isActive() {
      return active;
   }

   @EventTarget
   public void onLook(Event_289 event) {
      if (active) {
         this.rotateTowards(event.getYaw(), event.getPitch());
         event.cancel();
      }
   }

   @EventTarget
   public void onRotation(Event_222 event) {
      if (active) {
         event.setYaw(freeYaw);
         event.setPitch(freePitch);
      } else {
         freeYaw = event.getYaw();
         freePitch = event.getPitch();
      }
   }
   public void rotateTowards(double targetYaw, double targetPitch) {
      freePitch = MathHelper.clamp(
         (float)((double)freePitch + targetPitch * 0.15),
         -90.0f,
         90.0f
      );
      freeYaw = (float)((double)freeYaw + targetYaw * 0.15);
   }

   @Generated
   public static void setActive(boolean active) {
      ObsidianComponent_142.active = active;
   }

   @Generated
   public static float getFreeYaw() {
      return freeYaw;
   }

   @Generated
   public static float getFreePitch() {
      return freePitch;
   }

   @Generated
   public static void setFreeYaw(float freeYaw) {
      ObsidianComponent_142.freeYaw = freeYaw;
   }

   @Generated
   public static void setFreePitch(float freePitch) {
      ObsidianComponent_142.freePitch = freePitch;
   }
}
