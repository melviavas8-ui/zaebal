package wtf.obsidian.modules.combat;

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

























import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.network.packet.c2s.play.CloseHandledScreenC2SPacket;
import net.minecraft.screen.slot.SlotActionType;

public class AutoSwapModule extends Module {
   public static AutoSwapModule INSTANCE = new AutoSwapModule();
   public final ModeSetting firstItem;
   public final ModeSetting secondItem;
   public final ObsidianComponent_261 swapKey;
   public final BooleanSetting bypassgrim;
   public int bypassTicks;
   public boolean sprintPaused;
   public int swapCooldown;
   public int targetSlot;
   public boolean needSwap;

   public AutoSwapModule() {
      super("AutoSwap", "Быстрая смена предметов в офф-хенде", Category.COMBAT);
      String[] var10005 = new String[5];
      var10005[0] = "Руна";
      var10005[1] = "Тотем";
      var10005[2] = "Шар";
      var10005[3] = "Гепл";
      var10005[4] = "Щит";
      this.firstItem = new ModeSetting("Первый предмет", "Руна", var10005);
      var10005 = new String[5];
      var10005[0] = "Руна";
      var10005[1] = "Тотем";
      var10005[2] = "Шар";
      var10005[3] = "Гепл";
      var10005[4] = "Щит";
      this.secondItem = new ModeSetting("Второй предмет", "Тотем", var10005);
      this.swapKey = new ObsidianComponent_261("Кнопка свапа", -98);
      this.bypassgrim = new BooleanSetting("Обходить Grim", true);
      this.targetSlot = -1;
      this.needSwap = false;
      Setting[] var10001 = new Setting[4];
      var10001[0] = this.firstItem;
      var10001[1] = this.secondItem;
      var10001[2] = this.swapKey;
      var10001[3] = this.bypassgrim;
      this.addSettings(var10001);
   }

   @Override
   public void onEnable() {
      this.needSwap = false;
      this.targetSlot = -1;
      this.bypassTicks = 0;
      this.swapCooldown = 0;
      super.onEnable();
   }

   @EventTarget
   public void onBinding(InputEvent event) {
      if (mc.currentScreen == null) {
         if (mc.player != null && mc.world != null) {
            if (event.getKey() == this.swapKey.getKey() && this.swapCooldown == 0) {
               this.needSwap = true;
            }
         }
      }
   }

   @EventTarget
   public void onInput(Event_102 e) {
      if (this.bypassgrim.isState() && this.bypassTicks > 0) {
         if (mc.player == null) {
            return;
         }

         mc.player.setSprinting(false);
         e.setForward(0.0f);
         e.setStrafe(0.0f);
         e.setJump(false);
         e.setSneak(false);
      }
   }

   @EventTarget
   public void onUpdate(EventTick e) {
      if (mc.player != null && mc.world != null) {
         if (this.swapCooldown > 0) {
            this.swapCooldown -= 1;
         }

         if (this.bypassgrim.isState() && this.bypassTicks > 0) {
            mc.player.setSprinting(false);
            this.bypassTicks -= 1;
            if (this.bypassTicks == (1)) {
               this.performSwap();
            }

            if (this.bypassTicks == 0) {
               this.restoreSprint();
            }
         } else {
            if (this.needSwap && this.targetSlot == -1) {
               this.needSwap = false;
               Item offhand = mc.player.getOffHandStack().getItem();
               Item first = this.getItem(this.firstItem.getCurrent());
               Item second = this.getItem(this.secondItem.getCurrent());
               int firstSlot = this.findItemSlot(first);
               int secondSlot = this.findItemSlot(second);
               if (firstSlot == -1 && secondSlot == (-1)) {
                  return;
               }

               int slot;
               if (offhand == first && secondSlot != -1) {
                  slot = secondSlot;
               } else if (firstSlot != (-1)) {
                  slot = firstSlot;
               } else {
                  slot = secondSlot;
               }

               if (slot == -1) {
                  return;
               }

               this.targetSlot = slot;
               if (this.bypassgrim.isState()) {
                  this.disableSprint();
                  this.bypassTicks = 2;
                  this.swapCooldown = 2;
               } else {
                  this.performSwap();
                  this.swapCooldown = 2;
               }
            }
         }
      }
   }
   public void performSwap() {
      if (this.targetSlot != (-1)) {
         this.doSwap(this.targetSlot);
         mc.player.networkHandler.sendPacket(new CloseHandledScreenC2SPacket(0));
         this.targetSlot = -1;
      }
   }
   public void doSwap(int slot) {
      if (slot >= 36 && slot <= (44)) {
         int hotbarSlot = slot - 36;
         mc.interactionManager.clickSlot(0, 45, hotbarSlot, SlotActionType.SWAP, mc.player);
      } else {
         mc.interactionManager.clickSlot(0, slot, 0, SlotActionType.SWAP, mc.player);
         mc.interactionManager
            .clickSlot(0, 45, 0, SlotActionType.SWAP, mc.player);
         mc.interactionManager.clickSlot(0, slot, 0, SlotActionType.SWAP, mc.player);
      }
   }
   public int findItemSlot(Item item) {
      for (int i = 9; i < (45); i++) {
         ItemStack stack = mc.player.playerScreenHandler.getSlot(i).getStack();
         if (stack.getItem() == item) {
            return i;
         }
      }

      return -1;
   }
   public Item getItem(String name) {
      int var3 = -1;
      switch (name.hashCode()) {
         case 1056824:
            if (name.equals("Шар")) {
               var3 = 2;
            }
            break;
         case 1058035:
            if (name.equals("Щит")) {
               var3 = 4;
            }
            break;
         case 32141790:
            if (name.equals("Гепл")) {
               var3 = 3;
            }
            break;
         case 32542454:
            if (name.equals("Руна")) {
               var3 = 0;
            }
            break;
         case 1010520205:
            if (name.equals("Тотем")) {
               var3 = 1;
            }
      }
      return switch (var3) {
         case 0 -> Items.FIREWORK_STAR;
         case 1 -> Items.TOTEM_OF_UNDYING;
         case 2 -> Items.PLAYER_HEAD;
         case 3 -> Items.GOLDEN_APPLE;
         case 4 -> Items.SHIELD;
         default -> Items.AIR;
      };
   }
   public void disableSprint() {
      if (!this.sprintPaused) {
         SprintModule.pushPause(1000L);
         this.sprintPaused = true;
      }
   }
   public void restoreSprint() {
      if (this.sprintPaused) {
         this.sprintPaused = false;
         SprintModule.popPause();
      }
   }

   @Override
   public void onDisable() {
      this.bypassTicks = 0;
      this.swapCooldown = 0;
      this.needSwap = false;
      this.targetSlot = -1;
      this.restoreSprint();
      super.onDisable();
   }
}
