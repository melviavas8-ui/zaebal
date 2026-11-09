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
























import net.minecraft.item.ItemStack;
import org.jetbrains.annotations.NotNull;

public record InventorySlotHelper(int slot, boolean found, ItemStack stack) implements IInstance {
   public static final InventorySlotHelper NOT_FOUND_RESULT = new InventorySlotHelper(-1, false, ItemStack.EMPTY);

   public static InventorySlotHelper notFound() {
      return NOT_FOUND_RESULT;
   }

   @NotNull
   public static InventorySlotHelper inOffhand(ItemStack stack) {
      return new InventorySlotHelper(40, true, stack);
   }

   public boolean isHolding() {
      if (mc.player == null) {
         return false;
      } else {
         return this.isOffhand() || mc.player.getInventory().selectedSlot == this.slot;
      }
   }

   public boolean isOffhand() {
      return this.slot == 40;
   }

   public boolean isInHotBar() {
      return this.slot >= 0 && this.slot < 9;
   }

   public void switchTo() {
      if (this.found && this.isInHotBar()) {
         ItemNameUtil.switchTo(this.slot);
      }
   }

   public void switchToSilent() {
      if (this.found && this.isInHotBar()) {
         ItemNameUtil.switchToSilent(this.slot);
      }
   }
}
