package wtf.obsidian.utils;

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
























import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import lombok.Generated;
import net.minecraft.block.Block;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.AttributeModifiersComponent;
import net.minecraft.component.type.AttributeModifiersComponent.Entry;
import net.minecraft.entity.DamageUtil;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.AxeItem;
import net.minecraft.item.BedItem;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.item.PickaxeItem;
import net.minecraft.item.ShovelItem;
import net.minecraft.item.SwordItem;
import net.minecraft.network.packet.c2s.play.UpdateSelectedSlotC2SPacket;
import net.minecraft.registry.Registries;
import net.minecraft.util.Hand;
import org.jetbrains.annotations.NotNull;

public final class ItemNameUtil implements IInstance {
   public static int cachedSlot = -1;

   public static int getItemCount(Item item) {
      if (mc.player == null) {
         return 0;
      } else {
         int counter = 0;

         for (int i = 0; i < mc.player.getInventory().size(); i++) {
            ItemStack stack = mc.player.getInventory().getStack(i);
            if (stack.isOf(item)) {
               counter += stack.getCount();
            }
         }

         return counter;
      }
   }

   public static InventorySlotHelper getAxe() {
      return findBest(itemStack -> itemStack.getItem() instanceof AxeItem, false);
   }

   public static InventorySlotHelper getAxeHotBar() {
      return findBest(itemStack -> itemStack.getItem() instanceof AxeItem, true);
   }

   public static InventorySlotHelper getPickAxe() {
      return findBest(itemStack -> itemStack.getItem() instanceof PickaxeItem, false);
   }

   public static InventorySlotHelper getPickAxeHotbar() {
      return getPickAxeHotBar();
   }

   public static InventorySlotHelper getPickAxeHotBar() {
      return findBest(itemStack -> itemStack.getItem() instanceof PickaxeItem, true);
   }

   public static InventorySlotHelper getSword() {
      return findBest(itemStack -> itemStack.getItem() instanceof SwordItem, false);
   }

   public static InventorySlotHelper getSwordHotBar() {
      return findBest(itemStack -> itemStack.getItem() instanceof SwordItem, true);
   }

   public static InventorySlotHelper getSkull() {
      return findInHotBar(
         stack -> (!(!stack.isOf(Items.SKELETON_SKULL)
                  && !stack.isOf(Items.WITHER_SKELETON_SKULL)
                  && !stack.isOf(Items.CREEPER_HEAD)
                  && !stack.isOf(Items.PLAYER_HEAD)
                  && !stack.isOf(Items.ZOMBIE_HEAD)))
      );
   }

   public static int getElytra() {
      if (mc.player == null) {
         return -1;
      } else {
         for (ItemStack stack : mc.player.getInventory().armor) {
            if (stack.isOf(Items.ELYTRA) && stack.getDamage() < stack.getMaxDamage() - 1) {
               return -2;
            }
         }

         for (int i = 0; i < 36; i++) {
            ItemStack stackx = mc.player.getInventory().getStack(i);
            if (stackx.isOf(Items.ELYTRA) && stackx.getDamage() < stackx.getMaxDamage() - (1)) {
               return i < (9) ? i + 36 : i;
            }
         }

         return -1;
      }
   }

   public static InventorySlotHelper findInHotBar(ObsidianComponent_260 searcher) {
      if (mc.player != null) {
         if (searcher.isValid(mc.player.getOffHandStack())) {
            return InventorySlotHelper.inOffhand(mc.player.getOffHandStack());
         }

         for (int i = 0; i < (9); i++) {
            ItemStack stack = mc.player.getInventory().getStack(i);
            if (searcher.isValid(stack)) {
               return new InventorySlotHelper(i, true, stack);
            }
         }
      }

      return InventorySlotHelper.notFound();
   }

   public static InventorySlotHelper findItemInHotBar(List<Item> items) {
      return findInHotBar(stack -> items.contains(stack.getItem()));
   }

   public static InventorySlotHelper findItemInHotBar(Item... items) {
      return findItemInHotBar(Arrays.asList(items));
   }

   public static InventorySlotHelper findInInventory(ObsidianComponent_260 searcher) {
      if (mc.player != null) {
         for (int i = 35; i >= 0; i--) {
            ItemStack stack = mc.player.getInventory().getStack(i);
            if (searcher.isValid(stack)) {
               return new InventorySlotHelper(i, true, stack);
            }
         }
      }

      return InventorySlotHelper.notFound();
   }

   public static InventorySlotHelper findItemInInventory(List<Item> items) {
      return findInInventory(stack -> items.contains(stack.getItem()));
   }

   public static InventorySlotHelper findItemInInventory(Item... items) {
      return findItemInInventory(Arrays.asList(items));
   }

   public static InventorySlotHelper findBlockInHotBar(@NotNull List<Block> blocks) {
      return findItemInHotBar(blocks.stream().<Item>map(Block::asItem).toList());
   }

   public static InventorySlotHelper findBlockInHotBar(Block... blocks) {
      return findItemInHotBar(Arrays.stream(blocks).<Item>map(Block::asItem).toList());
   }

   public static InventorySlotHelper findBlockInInventory(@NotNull List<Block> blocks) {
      return findItemInInventory(blocks.stream().<Item>map(Block::asItem).toList());
   }

   public static InventorySlotHelper findBlockInInventory(Block... blocks) {
      return findItemInInventory(Arrays.stream(blocks).<Item>map(Block::asItem).toList());
   }

   public static void saveSlot() {
      if (mc.player != null) {
         cachedSlot = mc.player.getInventory().selectedSlot;
      }
   }

   public static void returnSlot() {
      if (cachedSlot != (-1)) {
         switchTo(cachedSlot);
      }

      cachedSlot = -1;
   }

   public static void saveAndSwitchTo(int slot) {
      saveSlot();
      switchTo(slot);
   }

   public static void switchTo(int slot) {
      if (mc.player != null && mc.getNetworkHandler() != null && slot >= 0 && slot <= 8) {
         if (mc.player.getInventory().selectedSlot != slot) {
            mc.player.getInventory().selectedSlot = slot;
            mc.getNetworkHandler().sendPacket(new UpdateSelectedSlotC2SPacket(slot));
         }
      }
   }

   public static void switchToSilent(int slot) {
      if (mc.player != null && mc.getNetworkHandler() != null && slot >= 0 && slot <= (8)) {
         mc.getNetworkHandler().sendPacket(new UpdateSelectedSlotC2SPacket(slot));
      }
   }

   public static InventorySlotHelper getAntiWeaknessItem() {
      if (mc.player == null) {
         return InventorySlotHelper.notFound();
      } else {
         Item mainHand = mc.player.getMainHandStack().getItem();
         return !(mainHand instanceof SwordItem) && !(mainHand instanceof PickaxeItem) && !(mainHand instanceof AxeItem) && !(mainHand instanceof ShovelItem)
            ? findInHotBar(
               stack -> (!(!(stack.getItem() instanceof SwordItem)
                        && !(stack.getItem() instanceof PickaxeItem)
                        && !(stack.getItem() instanceof AxeItem)
                        && !(stack.getItem() instanceof ShovelItem)))
            )
            : new InventorySlotHelper(mc.player.getInventory().selectedSlot, true, mc.player.getMainHandStack());
      }
   }

   public static float getHitDamage(@NotNull ItemStack weapon, PlayerEntity entity) {
      if (mc.player != null && mc.world != null) {
         float baseDamage = getBaseAttackDamage(weapon);
         if (mc.player.fallDistance > 0.0f) {
            baseDamage += baseDamage / 2.0f;
         }

         if (mc.player.hasStatusEffect(StatusEffects.STRENGTH)) {
            int strength = Objects.requireNonNull(mc.player.getStatusEffect(StatusEffects.STRENGTH)).getAmplifier()
               + 1;
            baseDamage += 3.0f * (float)strength;
         }

         return DamageUtil.getDamageLeft(
            entity,
            baseDamage,
            mc.world.getDamageSources().generic(),
            (float)entity.getArmor(),
            (float)entity.getAttributeValue(EntityAttributes.ARMOR_TOUGHNESS)
         );
      } else {
         return 0.0f;
      }
   }

   public static InventorySlotHelper findBedInHotBar() {
      return findInHotBar(stack -> stack.getItem() instanceof BedItem);
   }

   public static InventorySlotHelper findBed() {
      return findInInventory(stack -> stack.getItem() instanceof BedItem);
   }

   public static Item getItem(String name) {
      if (name == null) {
         return Items.AIR;
      } else {
         String normalized = name.toLowerCase();

         for (Block block : Registries.BLOCK) {
            if (block.getTranslationKey().replace("block.minecraft.", "").equals(normalized)) {
               return Item.fromBlock(block);
            }
         }

         for (Item item : Registries.ITEM) {
            if (item.getTranslationKey().replace("item.minecraft.", "").equals(normalized)) {
               return item;
            }
         }

         return Items.DIRT;
      }
   }

   public static int getBedsCount() {
      if (mc.player == null) {
         return 0;
      } else {
         int counter = 0;

         for (int i = 0; i < mc.player.getInventory().size(); i++) {
            ItemStack stack = mc.player.getInventory().getStack(i);
            if (stack.getItem() instanceof BedItem) {
               counter += stack.getCount();
            }
         }

         return counter;
      }
   }
   public static InventorySlotHelper findBest(ObsidianComponent_260 searcher, boolean hotbarOnly) {
      if (mc.player == null) {
         return InventorySlotHelper.notFound();
      } else {
         int bestSlot = -1;
         float bestDamage = 0.0f;
         int end = hotbarOnly ? 8 : 35;

         for (int i = 0; i <= end; i++) {
            ItemStack stack = mc.player.getInventory().getStack(i);
            if (searcher.isValid(stack)) {
               float damage = getBaseAttackDamage(stack);
               if (damage > bestDamage) {
                  bestDamage = damage;
                  bestSlot = i;
               }
            }
         }

         return bestSlot == (-1)
            ? InventorySlotHelper.notFound()
            : new InventorySlotHelper(bestSlot, true, mc.player.getInventory().getStack(bestSlot));
      }
   }
   public static float getBaseAttackDamage(ItemStack stack) {
      AttributeModifiersComponent component = (AttributeModifiersComponent)stack.getOrDefault(
         DataComponentTypes.ATTRIBUTE_MODIFIERS, AttributeModifiersComponent.DEFAULT
      );
      double damage = 1.0;

      for (Entry entry : component.modifiers()) {
         if (entry.attribute().equals(EntityAttributes.ATTACK_DAMAGE)) {
            damage += entry.modifier().value();
         }
      }

      return (float)damage;
   }

   public static boolean isHolding(Item item) {
      return (!(mc.player == null || !mc.player.getMainHandStack().isOf(item) && !mc.player.getOffHandStack().isOf(item)));
   }

   public static Hand getHand(Item item) {
      if (mc.player == null) {
         return null;
      } else if (mc.player.getOffHandStack().isOf(item)) {
         return Hand.OFF_HAND;
      } else {
         return mc.player.getMainHandStack().isOf(item) ? Hand.MAIN_HAND : null;
      }
   }

   @Generated
   public ItemNameUtil() {
      throw new UnsupportedOperationException("This is a utility class and cannot be instantiated");
   }
}
