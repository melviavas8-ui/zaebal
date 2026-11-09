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
























import com.sun.jna.Structure;
import java.util.Arrays;
import java.util.List;

public class ObsidianComponent_401 extends Structure {
   public ObsidianComponent_291 disconnected;
   public ObsidianComponent_348 joinRequest;
   public DiscordCallback spectateGame;
   public ObsidianComponent_286 ready;
   public ObsidianComponent_163 errored;
   public ObsidianComponent_407 joinGame;

   @Override
   protected List<String> getFieldOrder() {
      String[] var10000 = new String[6];
      var10000[0] = "ready";
      var10000[1] = "disconnected";
      var10000[2] = "errored";
      var10000[3] = "joinGame";
      var10000[4] = "spectateGame";
      var10000[5] = "joinRequest";
      return Arrays.asList(var10000);
   }
}
