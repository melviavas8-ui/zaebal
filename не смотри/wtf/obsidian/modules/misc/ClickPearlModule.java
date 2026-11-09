package wtf.obsidian.modules.misc;

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

























import net.minecraft.item.Items;
import net.minecraft.util.Hand;

public class ClickPearlModule extends Module {
   public static ClickPearlModule INSTANCE = new ClickPearlModule();
   private final ObsidianComponent_261 keyToPearl = new ObsidianComponent_261("Кнопка", -1);
   private final BooleanSetting bypass = new BooleanSetting("Обход", true);
   public boolean use;

   public ClickPearlModule() {
      super("ClickPearl", "Кидает перку по внутреннему бинду", Category.MISC);
      Setting[] var10001 = new Setting[2];
      var10001[0] = this.keyToPearl;
      var10001[1] = this.bypass;
      this.addSettings(var10001);
   }

   @Override
   public void onEnable() {
      this.use = false;
      super.onEnable();
   }

   @EventTarget
   public void onEvent(InputEvent event) {
      if (mc.currentScreen == null) {
         if (event.getKey() == this.keyToPearl.getKey()) {
            this.use = true;
         }
      }
   }

   @EventTarget
   public void onEvent(EventTick event) {
      if (this.use) {
         if (mc.player != null && mc.world != null) {
            int oldSlot = mc.player.getInventory().selectedSlot;
            int pearlSlot = ObsidianComponent_225.find(Items.ENDER_PEARL, 0, 36);
            if (pearlSlot == (-1)) {
               this.use = false;
            } else {
               if (pearlSlot > 9) {
                  mc.player.setSprinting(false);
               }

               if (this.bypass.isState()) {
                  mc.player.getInventory().selectedSlot = pearlSlot;
                  mc.interactionManager.interactItem(mc.player, Hand.MAIN_HAND);
                  mc.player.getInventory().selectedSlot = oldSlot;
               } else {
                  ObsidianComponent_225.swapAndUseHvH(Items.ENDER_PEARL);
               }

               this.use = false;
            }
         } else {
            this.use = false;
         }
      }
   }
}
