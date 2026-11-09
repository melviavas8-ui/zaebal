package wtf.obsidian.modules.movement;

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

























public class AutoJumpModule extends Module {
   public static AutoJumpModule INSTANCE = new AutoJumpModule();

   public AutoJumpModule() {
      super("AutoJump", "Прыгает автоматически при ауре", Category.MOVEMENT);
   }

   @EventTarget
   public void onUpdate(EventTick event) {
      if (mc.player != null && mc.world != null) {
         KillAuraModule aura = ModuleManager.aura;
         if (aura != null && aura.isEnable()) {
            if (aura.getTarget() != null && mc.player.isOnGround()) {
               mc.player.jump();
            }
         }
      }
   }
}
