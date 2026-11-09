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



























public class NoJumpDelayModule extends Module {
   public static NoJumpDelayModule INSTANCE = new NoJumpDelayModule();

   public NoJumpDelayModule() {
      super("NoJumpDelay", "Убирает задержку на прыжок", Category.PLAYER);
   }

   @EventTarget
   public void onEvent(EventTick event) {
      if (mc.player != null && mc.world != null) {
         ((ILivingEntity)mc.player).setJumpingCooldown(0);
      }
   }
}
