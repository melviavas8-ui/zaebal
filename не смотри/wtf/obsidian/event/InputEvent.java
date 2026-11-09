package wtf.obsidian.event;

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

public class InputEvent extends Event {
   public final int key;
   public final ObsidianComponent_125 bindType;

   public boolean isKeyDown(int button) {
      return (this.key == button);
   }

   @Generated
   public InputEvent(int key, ObsidianComponent_125 bindType) {
      this.key = key;
      this.bindType = bindType;
   }

   @Generated
   public int getKey() {
      return this.key;
   }

   @Generated
   public ObsidianComponent_125 getBindType() {
      return this.bindType;
   }
}
