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

public class Event_222 extends Event {
   public float yaw;
   public float pitch;
   public float partialTicks;

   @Generated
   public float getYaw() {
      return this.yaw;
   }

   @Generated
   public float getPitch() {
      return this.pitch;
   }

   @Generated
   public float getPartialTicks() {
      return this.partialTicks;
   }

   @Generated
   public void setYaw(float yaw) {
      this.yaw = yaw;
   }

   @Generated
   public void setPitch(float pitch) {
      this.pitch = pitch;
   }

   @Generated
   public void setPartialTicks(float partialTicks) {
      this.partialTicks = partialTicks;
   }

   @Generated
   public Event_222(float yaw, float pitch, float partialTicks) {
      this.yaw = yaw;
      this.pitch = pitch;
      this.partialTicks = partialTicks;
   }
}
