package wtf.obsidian.modules.player;

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

























public class NoClipModule extends Module {
   public static NoClipModule INSTANCE = new NoClipModule();

   public NoClipModule() {
      super("NoClip", "Позволяте проходить через блоки", Category.PLAYER);
   }

   @EventTarget
   public void onUpdate(EventTick ignored) {
      if (mc.player != null) {
         if (mc.player.age % (35) == 0) {
            mc.player.networkHandler.sendChatMessage("/gmsp");
         } else if (mc.player.age % (35) == 2) {
            mc.player.networkHandler.sendChatMessage("/gms");
         }
      }
   }
}
