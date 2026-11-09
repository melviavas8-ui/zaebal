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

public class ObsidianComponent_386 extends Structure {
   public String userId;
   public String username;
   @Deprecated
   public String discriminator;
   public String avatar;

   @Override
   protected List<String> getFieldOrder() {
      String[] var10000 = new String[4];
      var10000[0] = "userId";
      var10000[1] = "username";
      var10000[2] = "discriminator";
      var10000[3] = "avatar";
      return Arrays.asList(var10000);
   }
}
