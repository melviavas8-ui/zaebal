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

























import java.util.Set;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.network.packet.c2s.play.ClientCommandC2SPacket;
import net.minecraft.network.packet.c2s.play.CloseHandledScreenC2SPacket;
import net.minecraft.network.packet.c2s.play.UpdateSelectedSlotC2SPacket;
import net.minecraft.network.packet.c2s.play.ClientCommandC2SPacket.Mode;
import net.minecraft.screen.slot.SlotActionType;
import net.minecraft.util.Formatting;
import net.minecraft.util.Hand;

public class ElytraSwapModule extends Module {
   public static ElytraSwapModule INSTANCE = new ElytraSwapModule();
   private final ObsidianComponent_261 elytraBind = new ObsidianComponent_261("Бинд элитры", -1);
   private final ObsidianComponent_261 fireworkBind = new ObsidianComponent_261("Бинд фейерверка", -1);
   private final BooleanSetting autofly = new BooleanSetting("Авто-взлёт", true);
   private final BooleanSetting bypassgrim = new BooleanSetting("Обходить Grim", true);
   private final BooleanSetting bypassGround = new BooleanSetting("Обходить Граунд", true);
   public boolean swapElytraQueued;
   public boolean useFirework;
   public int bypassTicks;
   public boolean sprintPaused;
   public int swapCooldown;
   public int fireworkReturnSlot = -1;
   public int fireworkReturnTicks = -1;
   public boolean packetSwapActive;
   public int packetSwapStage;
   public int packetSwapSlot;

   public ElytraSwapModule() {
      super("ElytraSwap", "Автоматический свап элитр", Category.MISC);
      Setting[] var10001 = new Setting[5];
      var10001[0] = this.elytraBind;
      var10001[1] = this.fireworkBind;
      var10001[2] = this.autofly;
      var10001[3] = this.bypassgrim;
      var10001[4] = this.bypassGround;
      this.addSettings(var10001);
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
   public void onEvent(EventTick ignored) {
      if (mc.player != null) {
         if (this.swapCooldown > 0) {
            this.swapCooldown -= 1;
         }

         this.handleFireworkReturn();
         this.handlePacketSwap();
         if (this.bypassTicks > 0) {
            mc.player.setSprinting(false);
            this.bypassTicks -= 1;
            if (this.bypassTicks == (1)) {
               this.performSwap();
            }

            if (this.bypassTicks == 0) {
               this.restoreSprint();
            }
         } else {
            if (this.swapElytraQueued) {
               if (this.swapCooldown > 0) {
                  this.swapElytraQueued = false;
                  return;
               }

               if (this.bypassgrim.isState()) {
                  this.disableSprint();
                  this.bypassTicks = 3;
                  this.swapCooldown = 1;
               } else {
                  this.performSwap();
                  this.swapCooldown = 1;
               }

               this.swapElytraQueued = false;
            }

            if (this.useFirework) {
               int slotFirework = ObsidianComponent_225.getItemSlot(Items.FIREWORK_ROCKET);
               if (mc.player.isGliding()) {
                  if (slotFirework != (-1)) {
                     if (this.bypassGround.isState()) {
                        this.executePacketFireworkSwap(slotFirework);
                     } else {
                        ObsidianComponent_225.swapAndUseHvH(Items.FIREWORK_ROCKET);
                     }
                  } else {
                     ObsidianComponent_hatUtil.sendMessage("" + Formatting.RED + Formatting.BOLD + "Нет Фейерверков!");
                  }
               }

               this.useFirework = false;
            }

            if (this.autofly.isState() && this.bypassTicks == 0) {
               ItemStack chestStack = mc.player.getEquippedStack(EquipmentSlot.CHEST);
               if (chestStack.isOf(Items.ELYTRA)
                  && !mc.player.isTouchingWater()
                  && !mc.player.isInLava()
                  && mc.player.isOnGround()
                  && !mc.options.jumpKey.isPressed()) {
                  mc.player.jump();
               } else if (chestStack.isOf(Items.ELYTRA) && this.isElytraUsable(chestStack) && !mc.player.isGliding() && !mc.player.isOnGround()) {
                  mc.player.startGliding();
                  mc.player.networkHandler.sendPacket(new ClientCommandC2SPacket(mc.player, Mode.START_FALL_FLYING));
               }
            }
         }
      }
   }
   public void handlePacketSwap() {
      if (this.packetSwapActive && mc.player != null) {
         if (this.packetSwapStage == 0) {
            int currentSlot = mc.player.getInventory().selectedSlot;
            int nextSlot = (currentSlot + 1) % (9);
            mc.player.networkHandler.sendPacket(new UpdateSelectedSlotC2SPacket(nextSlot));
            this.packetSwapStage = 1;
         } else if (this.packetSwapStage == 1) {
            mc.player.networkHandler.sendPacket(new UpdateSelectedSlotC2SPacket(this.packetSwapSlot));
            this.packetSwapActive = false;
            this.packetSwapStage = 0;
         }
      }
   }
   public void executePacketFireworkSwap(int fireworkSlot) {
      int currentSlot = mc.player.getInventory().selectedSlot;
      this.packetSwapSlot = currentSlot;
      if (fireworkSlot < 9) {
         mc.player.networkHandler.sendPacket(new UpdateSelectedSlotC2SPacket(fireworkSlot));
         mc.interactionManager.interactItem(mc.player, Hand.MAIN_HAND);
         mc.player.networkHandler.sendPacket(new UpdateSelectedSlotC2SPacket(currentSlot));
      } else {
         if (fireworkSlot >= (36)) {
            int var10000 = fireworkSlot - (36);
         }

         mc.interactionManager.clickSlot(0, fireworkSlot, 0, SlotActionType.SWAP, mc.player);
         mc.interactionManager
            .clickSlot(
               0,
               (36) + currentSlot,
               0,
               SlotActionType.SWAP,
               mc.player
            );
         mc.interactionManager.interactItem(mc.player, Hand.MAIN_HAND);
         mc.interactionManager
            .clickSlot(
               0, (36) + currentSlot, 0, SlotActionType.SWAP, mc.player
            );
         mc.interactionManager
            .clickSlot(0, fireworkSlot, 0, SlotActionType.SWAP, mc.player);
         mc.player.networkHandler.sendPacket(new CloseHandledScreenC2SPacket(0));
      }

      this.packetSwapActive = true;
      this.packetSwapStage = 0;
   }
   public void performSwap() {
      int slotElytra = ObsidianComponent_225.findBestElytraSlot();
      int chestSlot = ObsidianComponent_225.findBestChestplateSlot();
      boolean needChestplate = (!(!mc.player.getEquippedStack(EquipmentSlot.CHEST).isOf(Items.ELYTRA)
            && !mc.player.getEquippedStack(EquipmentSlot.CHEST).isEmpty()
            && Set.of(
                  Items.NETHERITE_CHESTPLATE,
                  Items.DIAMOND_CHESTPLATE,
                  Items.IRON_CHESTPLATE,
                  Items.GOLDEN_CHESTPLATE,
                  Items.CHAINMAIL_CHESTPLATE,
                  Items.LEATHER_CHESTPLATE
               )
               .contains(mc.player.getEquippedStack(EquipmentSlot.CHEST).getItem())));
      if (needChestplate) {
         if (chestSlot == (-1)) {
            ObsidianComponent_hatUtil.sendMessage("" + Formatting.RED + Formatting.BOLD + "Нет нагрудника!");
            this.bypassTicks = 0;
            this.restoreSprint();
            return;
         }

         ItemStack chestItem = mc.player.playerScreenHandler.getSlot(chestSlot).getStack();
         this.doSwap(chestSlot);
      } else {
         if (slotElytra == (-1)) {
            ObsidianComponent_hatUtil.sendMessage("" + Formatting.RED + Formatting.BOLD + "Нет элитры!");
            this.bypassTicks = 0;
            this.restoreSprint();
            return;
         }

         ItemStack elytraItem = mc.player.playerScreenHandler.getSlot(slotElytra).getStack();
         this.doSwap(slotElytra);
      }

      mc.player.networkHandler.sendPacket(new CloseHandledScreenC2SPacket(0));
   }
   public void doSwap(int slot) {
      if (slot >= 0 && slot < (9)) {
         mc.interactionManager.clickSlot(0, 6, slot, SlotActionType.SWAP, mc.player);
      } else {
         mc.interactionManager.clickSlot(0, slot, 0, SlotActionType.SWAP, mc.player);
         mc.interactionManager
            .clickSlot(
               0,
               6,
               0,
               SlotActionType.SWAP,
               mc.player
            );
         mc.interactionManager
            .clickSlot(0, slot, 0, SlotActionType.SWAP, mc.player);
      }
   }
   public void handleFireworkReturn() {
      if (this.fireworkReturnTicks >= 0) {
         if (this.fireworkReturnTicks > 0) {
            this.fireworkReturnTicks -= 1;
         } else {
            if (this.fireworkReturnSlot != -1) {
               this.swapSlotToOffhand(this.fireworkReturnSlot);
               mc.player.networkHandler.sendPacket(new CloseHandledScreenC2SPacket(0));
            }

            this.fireworkReturnSlot = -1;
            this.fireworkReturnTicks = -1;
         }
      }
   }
   public int findScreenSlot(Item item) {
      for (int slot = 9; slot < 45; slot++) {
         ItemStack stack = mc.player.playerScreenHandler.getSlot(slot).getStack();
         if (stack.isOf(item)) {
            return slot;
         }
      }

      return -1;
   }
   public void swapSlotToOffhand(int slot) {
      if (slot >= (36) && slot <= (44)) {
         mc.interactionManager
            .clickSlot(0, 45, slot - (36), SlotActionType.SWAP, mc.player);
      } else {
         mc.interactionManager
            .clickSlot(0, slot, 0, SlotActionType.SWAP, mc.player);
         mc.interactionManager
            .clickSlot(0, 45, 0, SlotActionType.SWAP, mc.player);
         mc.interactionManager.clickSlot(0, slot, 0, SlotActionType.SWAP, mc.player);
      }
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
   public boolean isElytraUsable(ItemStack stack) {
      return (stack.getDamage() < stack.getMaxDamage() - (1));
   }

   @EventTarget
   public void onEvent(InputEvent event) {
      if (event.getKey() == this.elytraBind.getKey()) {
         this.swapElytraQueued = true;
      }

      if (event.getKey() == this.fireworkBind.getKey()) {
         this.useFirework = true;
      }
   }

   @Override
   public void onDisable() {
      this.bypassTicks = 0;
      this.swapCooldown = 0;
      this.fireworkReturnSlot = -1;
      this.fireworkReturnTicks = -1;
      this.packetSwapActive = false;
      this.packetSwapStage = 0;
      this.restoreSprint();
      super.onDisable();
   }
}
