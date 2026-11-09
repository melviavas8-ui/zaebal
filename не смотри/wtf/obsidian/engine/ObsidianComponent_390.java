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
























public class ObsidianComponent_390 implements IInstance {
   public static float getFixedRotation(float rot) {
      return getDeltaMouse(rot) * getGCDValue();
   }

   public static float getGCDValue() {
      return (float)((double)getGCD() * 0.15);
   }

   public static float getGCD() {
      double f = 0.5000000149011612;
      return (float)(f * f * f * 8.0);
   }

   public static float getDeltaMouse(float delta) {
      return (float)Math.round(delta / getGCDValue());
   }
}
