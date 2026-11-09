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
























public class ObsidianComponent_111 {
   public double velocity;
   public double gravity;
   public double drag;

   public ObsidianComponent_111(double velocity, double gravity, double drag) {
      this.velocity = velocity;
      this.gravity = gravity;
      this.drag = drag;
   }

   public double velocity() { return this.velocity; }
   public double gravity() { return this.gravity; }
   public double drag() { return this.drag; }

}
