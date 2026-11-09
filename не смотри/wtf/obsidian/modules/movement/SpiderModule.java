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

























import net.minecraft.item.BowItem;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.item.TridentItem;
import net.minecraft.network.packet.c2s.play.PlayerInteractItemC2SPacket;
import net.minecraft.network.packet.c2s.play.UpdateSelectedSlotC2SPacket;
import net.minecraft.screen.slot.SlotActionType;
import net.minecraft.util.Hand;

public class SpiderModule extends Module {
   public static SpiderModule INSTANCE = new SpiderModule();
   public final ModeSetting mode;
   public final BooleanSetting legit;
   public int lastSlot;
   public boolean isClimbing;
   public int swapBackSlot;
   public int spookyTicks;
   public int chargeSlot;
   public boolean charging;

   public SpiderModule() {
      super("Spider", "Позволяет взбираться по стенам", Category.MOVEMENT);
      String[] var10005 = new String[2];
      var10005[0] = "Вода";
      var10005[1] = "SpookyTime";
      this.mode = new ModeSetting("Мод", "Вода", var10005);
      this.legit = new BooleanSetting("Легит", false);
      this.lastSlot = -1;
      this.isClimbing = false;
      this.swapBackSlot = -1;
      this.chargeSlot = -1;
      Setting[] var10001 = new Setting[2];
      var10001[0] = this.mode;
      var10001[1] = this.legit;
      this.addSettings(var10001);
   }

   @Override
   public void onDisable() {
      super.onDisable();
      if (mc.player != null) {
         if (this.lastSlot != -1 && this.legit.isState()) {
            mc.player.getInventory().selectedSlot = this.lastSlot;
         }

         this.lastSlot = -1;
         this.swapBackSlot = -1;
         this.isClimbing = false;
         this.spookyTicks = 0;
         this.chargeSlot = -1;
         this.charging = false;
      }
   }

   @EventTarget
   public void onUpdate(EventTick event) {
      if (mc.player != null && mc.world != null) {
         if (!mc.player.horizontalCollision) {
            this.stopClimbing();
         } else {
            this.isClimbing = true;
            RotationManager.update(
               new ObsidianComponent_olorSetting(mc.player.getYaw(), 0.0f),
               360.0f,
               360.0f,
               360.0f,
               360.0f,
               1,
               1,
               false
            );
            if (this.mode.is("SpookyTime")) {
               this.processSpookyTime();
            } else {
               int bucketSlot = this.getBucketSlot(false);
               if (bucketSlot != -1) {
                  this.useBucket(bucketSlot, this.legit.isState());
                  mc.player
                     .setVelocity(
                        mc.player.getVelocity().x,
                        0.36,
                        mc.player.getVelocity().z
                     );
               }
            }
         }
      }
   }
   public void stopClimbing() {
      if (this.lastSlot != -1 && this.legit.isState()) {
         mc.player.getInventory().selectedSlot = this.lastSlot;
         this.lastSlot = -1;
      }

      if (this.swapBackSlot != (-1)) {
         mc.interactionManager
            .clickSlot(
               0,
               this.swapBackSlot,
               0,
               SlotActionType.QUICK_MOVE,
               mc.player
            );
         this.swapBackSlot = -1;
      }

      this.isClimbing = false;
      this.spookyTicks = 0;
      this.chargeSlot = -1;
      this.charging = false;
   }
   public void processSpookyTime() {
      int bucketSlot = this.getBucketSlot(true);
      boolean bucketPulse = (this.spookyTicks % (5) == 0);
      boolean boostPulse = (this.spookyTicks % 4 != (3));
      this.keepChargeHeld();
      if (bucketSlot != -1 && bucketPulse) {
         this.useBucket(bucketSlot, false);
         this.keepChargeHeld();
      }

      double y = boostPulse
         ? 0.18
         : 0.03;
      mc.player.setVelocity(mc.player.getVelocity().x, y, mc.player.getVelocity().z);
      this.spookyTicks += 1;
   }
   public void useBucket(int bucketSlot, boolean legitMode) {
      if (!legitMode) {
         int currentSlot = mc.player.getInventory().selectedSlot;
         boolean isInventorySwap = (bucketSlot >= (9) && bucketSlot <= 35);
         if (isInventorySwap) {
            mc.interactionManager.clickSlot(0, bucketSlot, currentSlot, SlotActionType.SWAP, mc.player);
            mc.interactionManager.interactItem(mc.player, Hand.MAIN_HAND);
            mc.interactionManager.clickSlot(0, bucketSlot, currentSlot, SlotActionType.SWAP, mc.player);
         } else {
            mc.player.networkHandler.sendPacket(new UpdateSelectedSlotC2SPacket(bucketSlot));
            mc.interactionManager.interactItem(mc.player, Hand.MAIN_HAND);
            mc.player.networkHandler.sendPacket(new UpdateSelectedSlotC2SPacket(currentSlot));
         }
      } else {
         boolean isInventorySwap = (bucketSlot >= (9) && bucketSlot <= (35));
         if (isInventorySwap) {
            mc.interactionManager
               .clickSlot(0, bucketSlot, mc.player.getInventory().selectedSlot, SlotActionType.SWAP, mc.player);
            this.swapBackSlot = bucketSlot;
         } else if (mc.player.getInventory().selectedSlot != bucketSlot) {
            if (this.lastSlot == -1) {
               this.lastSlot = mc.player.getInventory().selectedSlot;
            }

            mc.player.getInventory().selectedSlot = bucketSlot;
         }

         mc.interactionManager.interactItem(mc.player, Hand.MAIN_HAND);
      }
   }
   public void keepChargeHeld() {
      if (this.isChargeItem(mc.player.getOffHandStack())) {
         if (!this.charging || this.spookyTicks % (12) == 0) {
            this.sendChargeUsePacket(Hand.OFF_HAND);
         }

         this.charging = true;
      } else {
         if (this.chargeSlot == (-1) || !this.isChargeItem(mc.player.getInventory().getStack(this.chargeSlot))) {
            this.chargeSlot = this.getChargeHotbarSlot();
            this.charging = false;
         }

         if (this.chargeSlot != (-1)) {
            if (mc.player.getInventory().selectedSlot != this.chargeSlot) {
               mc.player.networkHandler.sendPacket(new UpdateSelectedSlotC2SPacket(this.chargeSlot));
               mc.player.getInventory().selectedSlot = this.chargeSlot;
               this.charging = false;
            }

            if (!this.charging || this.spookyTicks % 12 == 0) {
               this.sendChargeUsePacket(Hand.MAIN_HAND);
            }

            this.charging = true;
         }
      }
   }
   public void sendChargeUsePacket(Hand hand) {
      mc.player.networkHandler.sendPacket(new PlayerInteractItemC2SPacket(hand, 0, mc.player.getYaw(), mc.player.getPitch()));
   }
   public int getBucketSlot(boolean allowLava) {
      for (int i = 0; i < (9); i++) {
         ItemStack stack = mc.player.getInventory().getStack(i);
         if (this.isBucket(stack, allowLava)) {
            return i;
         }
      }

      if (!this.legit.isState() || this.mode.is("SpookyTime")) {
         for (int ix = 9; ix < 36; ix++) {
            ItemStack stack = mc.player.getInventory().getStack(ix);
            if (this.isBucket(stack, allowLava)) {
               return ix;
            }
         }
      }

      return -1;
   }
   public int getChargeHotbarSlot() {
      for (int i = 0; i < (9); i++) {
         if (this.isChargeItem(mc.player.getInventory().getStack(i))) {
            return i;
         }
      }

      return -1;
   }
   public boolean isBucket(ItemStack stack, boolean allowLava) {
      return (!(stack.getItem() != Items.WATER_BUCKET && (!allowLava || stack.getItem() != Items.LAVA_BUCKET)));
   }
   public boolean isChargeItem(ItemStack stack) {
      return (!(!(stack.getItem() instanceof BowItem) && !(stack.getItem() instanceof TridentItem)));
   }
}
