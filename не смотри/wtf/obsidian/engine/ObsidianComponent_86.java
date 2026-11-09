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
























public class ObsidianComponent_86 {
   public String playerName;
   public int percent;
   public double distance;

   public ObsidianComponent_86(String playerName, int percent, double distance) {
      this.playerName = playerName;
      this.percent = percent;
      this.distance = distance;
   }

   public String playerName() { return this.playerName; }
   public int percent() { return this.percent; }
   public double distance() { return this.distance; }

}
