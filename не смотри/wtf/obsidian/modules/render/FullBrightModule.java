package wtf.obsidian.modules.render;

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

























import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;

public class FullBrightModule extends Module {
   public static FullBrightModule INSTANCE = new FullBrightModule();

   public FullBrightModule() {
      super("FullBright", "Всегда светло", Category.RENDER);
   }

   @EventTarget
   public void onUpdate(EventTick ignored) {
      if (mc.player != null && mc.world != null) {
         mc.player
            .addStatusEffect(
               new StatusEffectInstance(StatusEffects.NIGHT_VISION, 777, 1)
            );
      }
   }

   @Override
   public void onDisable() {
      if (mc.player != null && mc.world != null) {
         mc.player.removeStatusEffect(StatusEffects.NIGHT_VISION);
         super.onDisable();
      }
   }
}
