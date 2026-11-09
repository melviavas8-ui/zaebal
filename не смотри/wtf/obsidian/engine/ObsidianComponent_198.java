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
























public class ObsidianComponent_198 {
   private final ObsidianComponent_401 handlers = new ObsidianComponent_401();

   public ObsidianComponent_401 build() {
      return this.handlers;
   }

   public ObsidianComponent_198 disconnected(ObsidianComponent_291 var1) {
      this.handlers.disconnected = var1;
      return this;
   }

   public ObsidianComponent_198 errored(ObsidianComponent_163 var1) {
      this.handlers.errored = var1;
      return this;
   }

   public ObsidianComponent_198 ready(ObsidianComponent_286 var1) {
      this.handlers.ready = var1;
      return this;
   }

   public ObsidianComponent_198 joinRequest(ObsidianComponent_348 var1) {
      this.handlers.joinRequest = var1;
      return this;
   }

   public ObsidianComponent_198 joinGame(ObsidianComponent_407 var1) {
      this.handlers.joinGame = var1;
      return this;
   }

   public ObsidianComponent_198 spectateGame(DiscordCallback var1) {
      this.handlers.spectateGame = var1;
      return this;
   }
}
