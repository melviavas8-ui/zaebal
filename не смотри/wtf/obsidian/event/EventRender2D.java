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
import net.minecraft.client.gui.DrawContext;

public class EventRender2D extends Event {
   public final DrawContext context;
   public final float partialTicks;

   @Generated
   public DrawContext getContext() {
      return this.context;
   }

   @Generated
   public float getPartialTicks() {
      return this.partialTicks;
   }

   @Generated
   public EventRender2D(DrawContext context, float partialTicks) {
      this.context = context;
      this.partialTicks = partialTicks;
   }
}
