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

























import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;


public class FastExpModule extends Module {
   public static FastExpModule INSTANCE = new FastExpModule();

   public FastExpModule() {
      super("FastExp", "Позволяет бросать пузырьки опыта без задержки", Category.PLAYER);
   }

   @EventTarget
   public void onUpdate(EventTick event) {
      if (mc.player != null) {
         ItemStack stack = mc.player.getMainHandStack();
         if (stack.isOf(Items.EXPERIENCE_BOTTLE)) {
            ((IMinecraftClientAccessor)mc).setItemUseCooldown(0);
         }
      }
   }
}
