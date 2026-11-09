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

public class ObsidianComponent_304 implements IInstance {
   private long startTime = System.currentTimeMillis();
   public long millis;

   public ObsidianComponent_304() {
      this.reset();
   }

   public static ObsidianComponent_304 create() {
      return new ObsidianComponent_304();
   }

   public boolean finished(long delay) {
      return (System.currentTimeMillis() - delay >= this.millis);
   }

   public void reset() {
      this.millis = System.currentTimeMillis();
   }

   public long getElapsedTime() {
      return System.currentTimeMillis() - this.millis;
   }

   public double deltaTime() {
      return mc.getCurrentFps() > 0
         ? 1.0 / (double)mc.getCurrentFps()
         : 1.0;
   }

   public boolean every(long ms) {
      boolean passed = (this.getMillis(System.nanoTime() - this.millis) >= ms);
      if (passed) {
         this.reset();
      }

      return passed;
   }

   public boolean passed(long time) {
      return (System.currentTimeMillis() - this.startTime > time);
   }

   public long getMillis(long time) {
      return time / (1000000L);
   }

   public long getTime() {
      return System.currentTimeMillis() - this.startTime;
   }

   public void setTime(long time) {
      this.startTime = time;
   }

   @Generated
   public long getStartTime() {
      return this.startTime;
   }

   @Generated
   public long getMillis() {
      return this.millis;
   }

   @Generated
   public void setStartTime(long startTime) {
      this.startTime = startTime;
   }

   @Generated
   public void setMillis(long millis) {
      this.millis = millis;
   }
}
