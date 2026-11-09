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

public class Event_102 extends Event {
   public float forward;
   public float strafe;
   public boolean jump;
   public boolean sneak;

   @Generated
   public float getForward() {
      return this.forward;
   }

   @Generated
   public float getStrafe() {
      return this.strafe;
   }

   @Generated
   public boolean isJump() {
      return this.jump;
   }

   @Generated
   public boolean isSneak() {
      return this.sneak;
   }

   @Generated
   public void setForward(float forward) {
      this.forward = forward;
   }

   @Generated
   public void setStrafe(float strafe) {
      this.strafe = strafe;
   }

   @Generated
   public void setJump(boolean jump) {
      this.jump = jump;
   }

   @Generated
   public void setSneak(boolean sneak) {
      this.sneak = sneak;
   }

   @Generated
   public Event_102(float forward, float strafe, boolean jump, boolean sneak) {
      this.forward = forward;
      this.strafe = strafe;
      this.jump = jump;
      this.sneak = sneak;
   }
}
