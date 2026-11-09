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

























import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.item.consume.UseAction;
import net.minecraft.network.packet.c2s.play.CloseHandledScreenC2SPacket;
import net.minecraft.network.packet.c2s.play.UpdateSelectedSlotC2SPacket;
import net.minecraft.screen.slot.SlotActionType;
import net.minecraft.util.Hand;

public class AutoEatModule extends Module {
   public static final AutoEatModule INSTANCE = new AutoEatModule();
   public static final String BARITONE_API_CLASS = "baritone.api.BaritoneAPI";
   private final SliderSetting hungerBars = new SliderSetting(
      "Плашки голода",
      6.0f,
      1.0f,
      10.0f,
      1.0f
   );
   public boolean eating;
   public boolean sprintPaused;
   public boolean swappedFromInventory;
   public int originalSlot = -1;
   public int swappedInventorySlot = -1;

   public AutoEatModule() {
      super("AutoEat", "Автоматически ест при низком голоде", Category.PLAYER);
      Setting[] var10001 = new Setting[1];
      var10001[0] = this.hungerBars;
      this.addSettings(var10001);
   }

   public static boolean shouldSuppressCombat() {
      return (INSTANCE != null && INSTANCE.isEnable() && INSTANCE.eating);
   }

   @Override
   public void onDisable() {
      this.stopEating();
      super.onDisable();
   }

   @EventTarget
   public void onUpdate(EventTick event) {
      if (mc.player == null || mc.world == null || mc.interactionManager == null) {
         this.stopEating();
      } else if (mc.currentScreen != null) {
         this.stopEating();
      } else if (!mc.player.getAbilities().creativeMode && !mc.player.isSpectator()) {
         if (!this.eating) {
            if (!this.shouldStartEating()) {
               return;
            }

            this.eating = true;
            this.originalSlot = mc.player.getInventory().selectedSlot;
         }

         this.tickEating();
      } else {
         this.stopEating();
      }
   }
   public void tickEating() {
      ClientPlayerEntity player = mc.player;
      if (player == null) {
         this.stopEating();
      } else {
         this.pauseBaritone();
         if (!this.sprintPaused) {
            SprintModule.pushPause(0L);
            this.sprintPaused = true;
         }

         mc.options.attackKey.setPressed(false);
         if (!this.needsFood()) {
            if (!player.isUsingItem()) {
               this.stopEating();
            }
         } else if (!this.ensureFoodReady()) {
            this.stopEating();
         } else {
            Hand eatingHand = this.getEatingHand(player);
            if (eatingHand == null) {
               this.stopEating();
            } else {
               mc.options.useKey.setPressed(true);
               if (!player.isUsingItem() || player.getActiveHand() != eatingHand) {
                  mc.interactionManager.interactItem(player, eatingHand);
               }
            }
         }
      }
   }
   public boolean shouldStartEating() {
      return (!(!this.needsFood()
            || mc.player.isUsingItem()
            || !this.isValidFood(mc.player.getOffHandStack()) && this.findFoodSlot() == (-1)));
   }
   public boolean needsFood() {
      return (mc.player != null
            && mc.player.getHungerManager().getFoodLevel() < 20
            && mc.player.getHungerManager().getFoodLevel() <= this.getFoodThreshold());
   }
   public int getFoodThreshold() {
      return Math.round(this.hungerBars.get()) * 2;
   }
   public boolean ensureFoodReady() {
      ClientPlayerEntity player = mc.player;
      if (player == null) {
         return false;
      } else if (this.isValidFood(player.getOffHandStack())) {
         return true;
      } else if (this.isValidFood(player.getMainHandStack())) {
         return true;
      } else {
         int foodSlot = this.findFoodSlot();
         if (foodSlot == -1) {
            return false;
         } else if (foodSlot < 9) {
            this.swappedFromInventory = false;
            this.swappedInventorySlot = -1;
            this.selectHotbarSlot(foodSlot);
            return this.isValidFood(player.getMainHandStack());
         } else {
            this.selectHotbarSlot(this.originalSlot == -1 ? player.getInventory().selectedSlot : this.originalSlot);
            this.swapInventorySlotWithHotbar(foodSlot, player.getInventory().selectedSlot);
            this.swappedFromInventory = true;
            this.swappedInventorySlot = foodSlot;
            return this.isValidFood(player.getMainHandStack());
         }
      }
   }
   public Hand getEatingHand(ClientPlayerEntity player) {
      if (player == null) {
         return null;
      } else if (this.isValidFood(player.getOffHandStack())) {
         return Hand.OFF_HAND;
      } else {
         return this.isValidFood(player.getMainHandStack()) ? Hand.MAIN_HAND : null;
      }
   }
   public int findFoodSlot() {
      ClientPlayerEntity player = mc.player;
      if (player == null) {
         return -1;
      } else {
         int selected = player.getInventory().selectedSlot;
         if (this.isValidFood(player.getInventory().getStack(selected))) {
            return selected;
         } else {
            for (int slot = 0; slot < 9; slot++) {
               if (slot != selected && this.isValidFood(player.getInventory().getStack(slot))) {
                  return slot;
               }
            }

            for (int slotx = 9; slotx < (36); slotx++) {
               if (this.isValidFood(player.getInventory().getStack(slotx))) {
                  return slotx;
               }
            }

            return -1;
         }
      }
   }
   public boolean isValidFood(ItemStack stack) {
      if (stack == null || stack.isEmpty()) {
         return false;
      } else if (!stack.isOf(Items.GOLDEN_APPLE) && !stack.isOf(Items.ENCHANTED_GOLDEN_APPLE) && !stack.isOf(Items.CHORUS_FRUIT)) {
         return (stack.getUseAction() == UseAction.EAT);
      } else {
         return false;
      }
   }
   public void selectHotbarSlot(int slot) {
      if (mc.player != null && slot >= 0 && slot <= 8 && mc.player.getInventory().selectedSlot != slot) {
         mc.player.getInventory().selectedSlot = slot;
         if (mc.getNetworkHandler() != null) {
            mc.getNetworkHandler().sendPacket(new UpdateSelectedSlotC2SPacket(slot));
         }
      }
   }
   public void swapInventorySlotWithHotbar(int inventorySlot, int hotbarSlot) {
      if (mc.player != null
         && mc.interactionManager != null
         && inventorySlot >= (9)
         && inventorySlot <= (35)
         && hotbarSlot >= 0
         && hotbarSlot <= 8) {
         mc.interactionManager.clickSlot(0, inventorySlot, hotbarSlot, SlotActionType.SWAP, mc.player);
         if (mc.getNetworkHandler() != null) {
            mc.getNetworkHandler().sendPacket(new CloseHandledScreenC2SPacket(0));
         }
      }
   }
   public void stopEating() {
      if (mc.options != null) {
         mc.options.useKey.setPressed(false);
      }

      if (this.sprintPaused) {
         SprintModule.popPause();
         this.sprintPaused = false;
      }

      this.restoreHeldItem();
      this.eating = false;
   }
   public void restoreHeldItem() {
      if (mc.player != null && mc.interactionManager != null) {
         if (this.swappedFromInventory && this.swappedInventorySlot != (-1)) {
            int hotbarSlot = this.originalSlot == (-1) ? mc.player.getInventory().selectedSlot : this.originalSlot;
            this.selectHotbarSlot(hotbarSlot);
            this.swapInventorySlotWithHotbar(this.swappedInventorySlot, hotbarSlot);
         }

         if (this.originalSlot != (-1)) {
            this.selectHotbarSlot(this.originalSlot);
         }

         this.resetSwapState();
      } else {
         this.resetSwapState();
      }
   }
   public void resetSwapState() {
      this.swappedFromInventory = false;
      this.swappedInventorySlot = -1;
      this.originalSlot = -1;
   }
   public void pauseBaritone() {
      try {
         Object baritone = getPrimaryBaritone();
         if (baritone == null) {
            this.cancelVanillaBreaking();
            return;
         }

         Object pathing = invoke(baritone, "getPathingBehavior");
         if (pathing == null || !Boolean.TRUE.equals(invoke(pathing, "hasPath"))) {
            this.cancelVanillaBreaking();
            return;
         }

         Object input = invoke(baritone, "getInputOverrideHandler");
         if (input != null) {
            input.getClass().getMethod("clearAllKeys").invoke(input);
            Object blockBreakHelper = input.getClass().getMethod("getBlockBreakHelper").invoke(input);
            if (blockBreakHelper != null) {
               blockBreakHelper.getClass().getMethod("stopBreakingBlock").invoke(blockBreakHelper);
            }
         }

         pathing.getClass().getMethod("requestPause").invoke(pathing);
         this.cancelVanillaBreaking();
      } catch (Throwable var5) {
         this.cancelVanillaBreaking();
      }
   }
   public void cancelVanillaBreaking() {
      try {
         if (mc.interactionManager != null) {
            mc.interactionManager.cancelBlockBreaking();
         }
      } catch (Throwable var2) {
      }
   }
   public static Object getPrimaryBaritone() throws ReflectiveOperationException {
      Class<?> apiClass = Class.forName("baritone.api.BaritoneAPI");
      Object provider = apiClass.getMethod("getProvider").invoke(null);
      return provider == null ? null : provider.getClass().getMethod("getPrimaryBaritone").invoke(provider);
   }
   public static Object invoke(Object target, String methodName) throws ReflectiveOperationException {
      return target.getClass().getMethod(methodName).invoke(target);
   }
}
