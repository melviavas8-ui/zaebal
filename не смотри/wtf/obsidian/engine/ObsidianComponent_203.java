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

public class ObsidianComponent_203 {
   public String name;
   public String command;
   public ObsidianComponent_261 bind;

   @Generated
   public ObsidianComponent_203(String name, String command, ObsidianComponent_261 bind) {
      this.name = name;
      this.command = command;
      this.bind = bind;
   }

   @Generated
   public String getName() {
      return this.name;
   }

   @Generated
   public String getCommand() {
      return this.command;
   }

   @Generated
   public ObsidianComponent_261 getBind() {
      return this.bind;
   }
}
