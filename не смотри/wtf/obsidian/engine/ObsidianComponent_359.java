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
























import net.minecraft.util.math.Vec3d;
public class ObsidianComponent_359 {
   public Vec3d position;
   public long birthTime;
   public long fadeInTime;
   public long displayTime;
   public long fadeOutTime;
public ObsidianComponent_359(Vec3d position, long birthTime, long fadeInTime, long displayTime, long fadeOutTime) {
      this.position = position;
      this.birthTime = birthTime;
      this.fadeInTime = fadeInTime;
      this.displayTime = displayTime;
      this.fadeOutTime = fadeOutTime;
   }

   public boolean isDead() {
      return (System.currentTimeMillis() - this.birthTime >= this.fadeInTime + this.displayTime + this.fadeOutTime);
   }

   public float getAlpha() {
      long elapsed = System.currentTimeMillis() - this.birthTime;
      if (elapsed < this.fadeInTime) {
         float progress = (float)elapsed / (float)this.fadeInTime;
         return this.easeOutCubic(progress);
      } else if (elapsed < this.fadeInTime + this.displayTime) {
         return 1.0f;
      } else {
         long fadeOutElapsed = elapsed - this.fadeInTime - this.displayTime;
         float progress = Math.min(1.0f, (float)fadeOutElapsed / (float)this.fadeOutTime);
         return 1.0f - this.easeInCubic(progress);
      }
   }

   public float getScaleMultiplier() {
      long elapsed = System.currentTimeMillis() - this.birthTime;
      if (elapsed < this.fadeInTime) {
         float progress = (float)elapsed / (float)this.fadeInTime;
         return 0.5f + 0.5f * this.easeOutBack(progress);
      } else if (elapsed < this.fadeInTime + this.displayTime) {
         return 1.0f;
      } else {
         long fadeOutElapsed = elapsed - this.fadeInTime - this.displayTime;
         float progress = Math.min(1.0f, (float)fadeOutElapsed / (float)this.fadeOutTime);
         return 1.0f
            - 0.30000001192092896f * this.easeInCubic(progress);
      }
   }
   public float easeOutCubic(float x) {
      return 1.0f
         - (float)Math.pow(
            1.0 - (double)x,
            3.0
         );
   }
   public float easeInCubic(float x) {
      return x * x * x;
   }
   public float easeOutBack(float x) {
      float c1 = 1.7015800476074219f;
      float c3 = c1 + 1.0f;
      return 1.0f
         + c3
            * (float)Math.pow(
               (double)x - 1.0,
               3.0
            )
         + c1
            * (float)Math.pow(
               (double)x - 1.0,
               2.0
            );
   }
}
