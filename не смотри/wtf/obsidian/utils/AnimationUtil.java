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

























import net.minecraft.util.math.MathHelper;

public class AnimationUtil {
   public float currentValue;
   public float targetValue;
   public float fromValue;
   public float speed;
   public ObsidianComponent_87 easing;
   public long startTime;
   public double duration;
   public boolean isRunning;

   public AnimationUtil(float initialValue, float speed, ObsidianComponent_87 easing) {
      this.currentValue = initialValue;
      this.targetValue = initialValue;
      this.fromValue = initialValue;
      this.speed = speed;
      this.easing = easing != null ? easing : ObsidianComponent_57.LINEAR;
      this.startTime = 0L;
      this.duration = 0.0;
      this.isRunning = false;
   }

   public AnimationUtil(float initialValue, float speed) {
      this(initialValue, speed, ObsidianComponent_57.LINEAR);
   }

   public void update(float target) {
      if (this.targetValue != target || !this.isRunning) {
         this.targetValue = target;
         this.fromValue = this.currentValue;
         this.startTime = System.nanoTime();
         this.duration = 1.0
            / (double)this.speed
            * 2.0;
         this.isRunning = true;
      }

      if (this.isDone()) {
         this.currentValue = this.targetValue;
         this.isRunning = false;
      } else {
         double part = this.calculatePart();
         float easedPart = (float)this.easing.ease(part);
         this.currentValue = MathHelper.lerp(easedPart, this.fromValue, this.targetValue);
      }
   }
   public double calculatePart() {
      if (!this.isRunning) {
         return 1.0;
      } else {
         long now = System.nanoTime();
         double elapsed = (double)(now - this.startTime) / 1000000000.0;
         return MathHelper.clamp(
            elapsed / this.duration,
            0.0,
            1.0
         );
      }
   }

   public float getValue() {
      return this.currentValue;
   }

   public void setValue(float value) {
      this.currentValue = value;
      this.targetValue = value;
      this.fromValue = value;
      this.isRunning = false;
   }

   public float getTarget() {
      return this.targetValue;
   }

   public void setSpeed(float speed) {
      this.speed = speed;
      this.duration = 1.0 / (double)speed;
   }

   public void setEasing(ObsidianComponent_87 easing) {
      this.easing = easing != null ? easing : ObsidianComponent_57.LINEAR;
   }

   public boolean isDone() {
      return (this.calculatePart() >= 1.0);
   }

   public boolean isAlive() {
      return (!this.isDone());
   }
}
