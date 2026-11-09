package wtf.obsidian.utils;

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
























public class DiscordIPC extends Thread {
   public final ObsidianComponent_342 this$0;

   public DiscordIPC(final ObsidianComponent_342 param1) {
      this.this$0 = param1;
   }

   @Override
   public void run() {
      this.setName("Discord-RPC");

      try {
         while (this.this$0.running) {
            ObsidianComponent_282.INSTANCE.Discord_RunCallbacks();
            Thread.sleep(15000L);
         }
      } catch (Exception var2) {
         this.this$0.stopRPC();
      }

      super.run();
   }
}
