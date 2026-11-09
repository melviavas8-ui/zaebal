package wtf.obsidian.hud;

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

public class DraggableHudElement implements IInstance {
   public final ObsidianComponent_280 draggable;
   private boolean unusualRectType = true;

   public boolean isUnusualRectType() {
      return this.unusualRectType;
   }

   public void setUnusualRectType(boolean unusualRectType) {
      this.unusualRectType = unusualRectType;
   }

   public void onUpdate(EventTick eventUpdate) {
   }

   public void onRender(EventRender2D eventRender) {
   }

   @Generated
   public DraggableHudElement(ObsidianComponent_280 draggable) {
      this.draggable = draggable;
   }
}
