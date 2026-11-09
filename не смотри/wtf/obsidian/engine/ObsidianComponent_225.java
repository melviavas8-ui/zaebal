package wtf.obsidian.engine;

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
























import lombok.Generated;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.EquippableComponent;
import net.minecraft.component.type.ItemEnchantmentsComponent;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.enchantment.Enchantments;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.item.ArmorItem;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.item.consume.UseAction;
import net.minecraft.network.packet.c2s.play.ClientCommandC2SPacket;
import net.minecraft.network.packet.c2s.play.CloseHandledScreenC2SPacket;
import net.minecraft.network.packet.c2s.play.PlayerInputC2SPacket;
import net.minecraft.network.packet.c2s.play.UpdateSelectedSlotC2SPacket;
import net.minecraft.network.packet.c2s.play.ClientCommandC2SPacket.Mode;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.screen.slot.SlotActionType;
import net.minecraft.util.Hand;
import net.minecraft.util.PlayerInput;

public final class ObsidianComponent_225 implements IInstance {
   public static int getItemSlot(Item input) {
      for (ItemStack stack : mc.player.getArmorItems()) {
         if (stack.getItem() == input) {
            return -2;
         }
      }

      int slot = -1;

      for (int i = 0; i < (36); i++) {
         ItemStack s = mc.player.getInventory().getStack(i);
         if (s.getItem() == input) {
            slot = i;
            break;
         }
      }

      if (slot < 9 && slot != -1) {
         slot += 36;
      }

      return slot;
   }

   public static int getEnchantmentLevel(ItemStack stack, RegistryKey<Enchantment> enchantmentKey) {
      ItemEnchantmentsComponent enchantments = (ItemEnchantmentsComponent)stack.getOrDefault(DataComponentTypes.ENCHANTMENTS, ItemEnchantmentsComponent.DEFAULT);

      for (RegistryEntry<Enchantment> enchantment : enchantments.getEnchantments()) {
         if (enchantment.matchesKey(enchantmentKey)) {
            return enchantments.getLevel(enchantment);
         }
      }

      return 0;
   }

   public static int findBestElytraSlot() {
      if (mc.player == null) {
         return -1;
      } else {
         int bestSlot = -1;
         double bestScore = Double.longBitsToDouble(-4616189618054758400L);

         for (int slot = 0; slot < (36); slot++) {
            ItemStack stack = mc.player.getInventory().getStack(slot);
            if (stack.getItem() == Items.ELYTRA) {
               int protection = getEnchantmentLevel(stack, Enchantments.PROTECTION);
               int unbreaking = getEnchantmentLevel(stack, Enchantments.UNBREAKING);
               int mending = getEnchantmentLevel(stack, Enchantments.MENDING);
               int maxDurability = stack.getMaxDamage();
               int currentDamage = stack.getDamage();
               double durabilityRatio = (double)(maxDurability - currentDamage) / (double)maxDurability;
               double score = (double)(
                     protection * (100)
                        + unbreaking * (10)
                        + (mending > 0 ? 1 : 0)
                  )
                  + durabilityRatio * 10.0;
               if (score > bestScore) {
                  bestScore = score;
                  bestSlot = slot;
               }
            }
         }

         return bestSlot;
      }
   }

   public static int findBestChestplateSlot() {
      if (mc.player == null) {
         return -1;
      } else {
         int bestSlot = -1;
         double bestScore = Double.longBitsToDouble(-4616189618054758400L);

         for (int slot = 0; slot < 36; slot++) {
            ItemStack stack = mc.player.getInventory().getStack(slot);
            Item equippable = stack.getItem();
            if (equippable instanceof ArmorItem) {
               ArmorItem armor = (ArmorItem)equippable;
               EquippableComponent equippablex = (EquippableComponent)stack.get(DataComponentTypes.EQUIPPABLE);
               if (equippablex != null && equippablex.slot() == EquipmentSlot.CHEST) {
                  int protection = getEnchantmentLevel(stack, Enchantments.PROTECTION);
                  int unbreaking = getEnchantmentLevel(stack, Enchantments.UNBREAKING);
                  int mending = getEnchantmentLevel(stack, Enchantments.MENDING);
                  int priority = getChestplatePriority(armor);
                  int maxDamage = stack.getMaxDamage();
                  int damage = stack.getDamage();
                  double durabilityRatio = maxDamage == 0
                     ? 1.0
                     : (double)(maxDamage - damage) / (double)maxDamage;
                  double score = (double)priority * 10000.0
                     + (double)protection * 100.0
                     + (double)unbreaking * 10.0
                     + (double)(mending > 0 ? 1 : 0)
                     + durabilityRatio * 10.0;
                  if (score > bestScore) {
                     bestScore = score;
                     bestSlot = slot;
                  }
               }
            }
         }

         return bestSlot;
      }
   }

   public static int getChestplatePriority(Item item) {
      if (item == Items.NETHERITE_CHESTPLATE) {
         return 5;
      } else if (item == Items.DIAMOND_CHESTPLATE) {
         return 4;
      } else if (item == Items.IRON_CHESTPLATE) {
         return 3;
      } else if (item == Items.GOLDEN_CHESTPLATE) {
         return 2;
      } else if (item == Items.CHAINMAIL_CHESTPLATE) {
         return 2;
      } else {
         return item == Items.LEATHER_CHESTPLATE ? 1 : 0;
      }
   }

   public static int find(Item item, int start, int end) {
      if (mc.player != null) {
         for (int i = end; i >= start; i--) {
            if (mc.player.currentScreenHandler.syncId != 0 && mc.player.currentScreenHandler.getSlot(i).getStack().getItem() == item) {
               return i;
            }

            if (mc.player.currentScreenHandler.syncId == 0 && mc.player.getInventory().getStack(i).getItem() == item) {
               return i;
            }
         }
      }

      return -1;
   }

   public static void swapAndUseHvH(Item item) {
      int slot = find(item, 9, 45);
      int slotHotbar = find(item, 0, 8);
      int previousSlot = mc.player.getInventory().selectedSlot;
      boolean isUsingItem = mc.player.isUsingItem();
      if (mc.player.getMainHandStack().getItem() == item) {
         if (!isUsingItem) {
            mc.interactionManager.interactItem(mc.player, Hand.MAIN_HAND);
         }
      } else if (mc.player.getOffHandStack().getItem() == item) {
         mc.interactionManager.interactItem(mc.player, Hand.OFF_HAND);
      } else if (isUsingItem) {
         if (slotHotbar != (-1)) {
            mc.interactionManager
               .clickSlot(
                  0,
                  (36) + slotHotbar,
                  40,
                  SlotActionType.SWAP,
                  mc.player
               );
            mc.player.networkHandler.sendPacket(new CloseHandledScreenC2SPacket(0));
            mc.interactionManager.interactItem(mc.player, Hand.OFF_HAND);
            mc.interactionManager
               .clickSlot(
                  0, (36) + slotHotbar, 40, SlotActionType.SWAP, mc.player
               );
            mc.player.networkHandler.sendPacket(new CloseHandledScreenC2SPacket(0));
         } else if (slot != -1) {
            mc.interactionManager.clickSlot(0, slot, 40, SlotActionType.SWAP, mc.player);
            mc.player.networkHandler.sendPacket(new CloseHandledScreenC2SPacket(0));
            mc.interactionManager.interactItem(mc.player, Hand.OFF_HAND);
            mc.interactionManager.clickSlot(0, slot, 40, SlotActionType.SWAP, mc.player);
            mc.player.networkHandler.sendPacket(new CloseHandledScreenC2SPacket(0));
         }
      } else if (slotHotbar != (-1)) {
         mc.player.networkHandler.sendPacket(new UpdateSelectedSlotC2SPacket(slotHotbar));
         mc.interactionManager.interactItem(mc.player, Hand.MAIN_HAND);
         mc.player.networkHandler.sendPacket(new UpdateSelectedSlotC2SPacket(previousSlot));
      } else {
         if (slot != (-1)) {
            int slotCorrectable = -1;

            for (int slotNone = 0; slotNone < (8); slotNone++) {
               ItemStack stack = mc.player.getInventory().getStack(slotNone);
               if (stack.isEmpty()) {
                  slotCorrectable = slotNone;
                  break;
               }

               UseAction action = stack.getUseAction();
               if (action == UseAction.NONE) {
                  slotCorrectable = slotNone;
               }
            }

            boolean wasSprinting = false;
            if (mc.player.isSprinting()) {
               mc.player
                  .networkHandler
                  .sendPacket(
                     new PlayerInputC2SPacket(
                        new PlayerInput(
                           false,
                           false,
                           false,
                           false,
                           false,
                           false,
                           false
                        )
                     )
                  );
               mc.player.setSprinting(false);
               mc.player.networkHandler.sendPacket(new ClientCommandC2SPacket(mc.player, Mode.STOP_SPRINTING));
               if (!ModuleManager.sprint.isEnable()) {
                  mc.options.sprintKey.setPressed(false);
               }

               wasSprinting = true;
            }

            if (slotCorrectable == -1) {
               mc.interactionManager
                  .clickSlot(0, slot, 8, SlotActionType.SWAP, mc.player);
               mc.player.networkHandler.sendPacket(new CloseHandledScreenC2SPacket(0));
               mc.player.networkHandler.sendPacket(new UpdateSelectedSlotC2SPacket(8));
               mc.interactionManager.interactItem(mc.player, Hand.MAIN_HAND);
               mc.player.networkHandler.sendPacket(new UpdateSelectedSlotC2SPacket(previousSlot));
            } else {
               mc.interactionManager.clickSlot(0, slot, slotCorrectable, SlotActionType.SWAP, mc.player);
               mc.player.networkHandler.sendPacket(new CloseHandledScreenC2SPacket(0));
               mc.player.networkHandler.sendPacket(new UpdateSelectedSlotC2SPacket(slotCorrectable));
               mc.interactionManager.interactItem(mc.player, Hand.MAIN_HAND);
               mc.player.networkHandler.sendPacket(new UpdateSelectedSlotC2SPacket(previousSlot));
               mc.interactionManager.clickSlot(0, slot, slotCorrectable, SlotActionType.SWAP, mc.player);
               mc.player.networkHandler.sendPacket(new CloseHandledScreenC2SPacket(0));
            }

            if (wasSprinting) {
               mc.player.networkHandler.sendPacket(new PlayerInputC2SPacket(mc.player.input.playerInput));
            }
         }
      }
   }

   @Generated
   public ObsidianComponent_225() {
      throw new UnsupportedOperationException("This is a utility class and cannot be instantiated");
   }
}
