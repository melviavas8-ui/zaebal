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

























import net.minecraft.item.ArmorItem;
import net.minecraft.item.ItemStack;
import net.minecraft.screen.slot.SlotActionType;

public class AutoArmorModule extends Module {
   public static AutoArmorModule INSTANCE = new AutoArmorModule();
   private final SliderSetting delay = new SliderSetting(
      "Задержка",
      25.0f,
      1.0f,
      1000.0f,
      1.0f
   );
   public long lastEquipTime = 0L;

   public AutoArmorModule() {
      super("AutoArmor", "Автоматически одевает броню", Category.PLAYER);
      Setting[] var10001 = new Setting[1];
      var10001[0] = this.delay;
      this.addSettings(var10001);
   }

   @EventTarget
   public void onEvent(EventTick event) {
      if (mc.player != null && mc.world != null) {
         if (!this.isMoving()) {
            long currentTime = System.currentTimeMillis();
            if (!((float)(currentTime - this.lastEquipTime) < this.delay.get())) {
               for (int i = 0; i < (4); i++) {
                  ItemStack currentArmor = mc.player.getInventory().getArmorStack(i);
                  if (currentArmor.isEmpty()) {
                     for (int j = 0; j < (36); j++) {
                        ItemStack stack = mc.player.getInventory().getStack(j);
                        if (!stack.isEmpty() && stack.getItem() instanceof ArmorItem armorItem && this.getArmorSlotIndex(armorItem) == i) {
                           int slotToEquip = j;
                           if (j < (9)) {
                              slotToEquip = j + (36);
                           }

                           mc.interactionManager
                              .clickSlot(
                                 0, slotToEquip, 0, SlotActionType.QUICK_MOVE, mc.player
                              );
                           this.lastEquipTime = currentTime;
                           return;
                        }
                     }
                  }
               }
            }
         }
      }
   }
   public boolean isMoving() {
      return (!(mc.player.input.movementForward == 0.0f
            && mc.player.input.movementSideways == 0.0f));
   }
   public int getArmorSlotIndex(ArmorItem armor) {
      String itemName = armor.toString().toLowerCase();
      if (itemName.contains("helmet") || itemName.contains("skull")) {
         return 3;
      } else if (itemName.contains("chestplate") || itemName.contains("tunic")) {
         return 2;
      } else if (itemName.contains("leggings") || itemName.contains("pants")) {
         return 1;
      } else {
         return !itemName.contains("boots") && !itemName.contains("shoes") ? 0 : 0;
      }
   }
}
