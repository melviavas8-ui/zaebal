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

public class ObsidianComponent_141 {
   public long lastMS = System.currentTimeMillis();

   public void reset() {
      this.lastMS = System.currentTimeMillis();
   }

   public boolean isReached(long time) {
      return (System.currentTimeMillis() - this.lastMS > time);
   }

   public void setLastMS(long newValue) {
      this.lastMS = System.currentTimeMillis() + newValue;
   }

   public void setTime(long time) {
      this.lastMS = time;
   }

   public long getTime() {
      return System.currentTimeMillis() - this.lastMS;
   }

   public boolean isRunning() {
      return (System.currentTimeMillis() - this.lastMS <= (0L));
   }

   public boolean hasTimeElapsed() {
      return (this.lastMS < System.currentTimeMillis());
   }

   @Generated
   public long getLastMS() {
      return this.lastMS;
   }
}
