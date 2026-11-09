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
























public class ObsidianComponent_61 {
   public String lowerName;
   public float width;
   public float anim;

   public ObsidianComponent_61(String lowerName, float width, float anim) {
      this.lowerName = lowerName;
      this.width = width;
      this.anim = anim;
   }

   public String lowerName() { return this.lowerName; }
   public float width() { return this.width; }
   public float anim() { return this.anim; }

}
