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
























public class ObsidianComponent_255 {
   public float minX;
   public float minY;
   public float maxX;
   public float maxY;

   public ObsidianComponent_255(float minX, float minY, float maxX, float maxY) {
      this.minX = minX;
      this.minY = minY;
      this.maxX = maxX;
      this.maxY = maxY;
   }

   public float minX() { return this.minX; }
   public float minY() { return this.minY; }
   public float maxX() { return this.maxX; }
   public float maxY() { return this.maxY; }

   public float centerX() {
      return (this.minX + this.maxX) * 0.5f;
   }

   public float centerY() {
      return (this.minY + this.maxY) * 0.5f;
   }

}
