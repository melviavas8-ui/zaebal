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
























public enum ObsidianComponent_151 {
   NO(0),
   IGNORE(2),
   YES(1);

   public final int reply;

   private ObsidianComponent_151(int reply) {
      this.reply = reply;
   }
   public static ObsidianComponent_151[] getReplies() {
      ObsidianComponent_151[] var10000 = new ObsidianComponent_151[3];
      var10000[0] = NO;
      var10000[1] = YES;
      var10000[2] = IGNORE;
      return var10000;
   }
}
