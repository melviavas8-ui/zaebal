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

























import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.ThreadLocalRandom;
import net.minecraft.inventory.Inventory;
import net.minecraft.screen.GenericContainerScreenHandler;
import net.minecraft.screen.HopperScreenHandler;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.screen.slot.Slot;
import net.minecraft.screen.slot.SlotActionType;

public class ChestStealerModule extends Module {
   public static ChestStealerModule INSTANCE = new ChestStealerModule();
   private final SliderSetting stealDelay = new SliderSetting(
      "Задержка",
      100.0f,
      0.0f,
      1000.0f,
      1.0f
   );
   private final BooleanSetting randomize = new BooleanSetting("Рандомизация", false);
   public long lastStealTime = 0L;

   public ChestStealerModule() {
      super("ChestStealer", "Автоматически открывает сундуки и забирает из них предметы", Category.PLAYER);
      Setting[] var10001 = new Setting[2];
      var10001[0] = this.stealDelay;
      var10001[1] = this.randomize;
      this.addSettings(var10001);
   }

   @EventTarget
   public void onUpdate(EventTick event) {
      if (mc.player != null && mc.interactionManager != null) {
         ScreenHandler openContainer = mc.player.currentScreenHandler;
         if (openContainer != null && openContainer != mc.player.playerScreenHandler) {
            if (openContainer instanceof GenericContainerScreenHandler || openContainer instanceof HopperScreenHandler) {
               long currentTime = System.currentTimeMillis();
               long delay = (long)this.stealDelay.get();
               if (currentTime - this.lastStealTime >= delay) {
                  List<Slot> slots = openContainer.slots;
                  this.findValidItem(slots, openContainer)
                     .ifPresent(
                        slot -> {
                           if (mc.player.currentScreenHandler == openContainer) {
                              mc.interactionManager
                                 .clickSlot(openContainer.syncId, slot.id, 0, SlotActionType.QUICK_MOVE, mc.player);
                              this.lastStealTime = currentTime;
                           }
                        }
                     );
               }
            }
         }
      }
   }
   public Optional<Slot> findValidItem(List<Slot> slots, ScreenHandler handler) {
      int containerSlotCount = this.getContainerSlotCount(handler);
      if (containerSlotCount > 0 && containerSlotCount <= slots.size()) {
         List<Slot> containerSlots = slots.subList(0, containerSlotCount);
         List<Slot> validSlots = new ArrayList<>();

         for (Slot slot : containerSlots) {
            if (slot.hasStack() && !slot.getStack().isEmpty() && !mc.player.getItemCooldownManager().isCoolingDown(slot.getStack())) {
               validSlots.add(slot);
            }
         }

         if (validSlots.isEmpty()) {
            return Optional.empty();
         } else if (this.randomize.isState()) {
            int randomIndex = ThreadLocalRandom.current().nextInt(validSlots.size());
            return Optional.of(validSlots.get(randomIndex));
         } else {
            return Optional.of(validSlots.get(0));
         }
      } else {
         return Optional.empty();
      }
   }
   public int getContainerSlotCount(ScreenHandler handler) {
      if (handler instanceof GenericContainerScreenHandler container) {
         Inventory inventory = container.getInventory();
         return inventory.size();
      } else {
         return handler instanceof HopperScreenHandler ? 5 : 0;
      }
   }

   @Override
   public void onDisable() {
      this.lastStealTime = 0L;
      super.onDisable();
   }
}
