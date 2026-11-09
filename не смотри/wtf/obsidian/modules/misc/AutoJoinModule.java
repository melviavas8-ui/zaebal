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

























import net.minecraft.client.gui.screen.ingame.GenericContainerScreen;
import net.minecraft.item.Items;
import net.minecraft.network.packet.c2s.play.UpdateSelectedSlotC2SPacket;
import net.minecraft.network.packet.s2c.play.GameJoinS2CPacket;
import net.minecraft.network.packet.s2c.play.GameMessageS2CPacket;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.screen.slot.Slot;
import net.minecraft.screen.slot.SlotActionType;
import net.minecraft.util.Hand;

public final class AutoJoinModule extends Module {
   public static AutoJoinModule INSTANCE = new AutoJoinModule();
   public static final long CLICK_DELAY_MS = 30L;
   public static final int NEXT_PAGE_SLOT = 44;
   public static final int MAX_PAGE_SWITCHES = 5;
   private final SliderSetting grief = new SliderSetting(
      "Гриф",
      5.0f,
      1.0f,
      64.0f,
      1.0f
   );
   private final ObsidianComponent_372 clickTimer = new ObsidianComponent_372();
   private final ObsidianComponent_372 compassTimer = new ObsidianComponent_372();
   public boolean joining;
   public int pageSwitches;
   public int targetGrief;

   public AutoJoinModule() {
      super("AutoJoin", "Автоматически заходит на выбранный гриф", Category.MISC);
      Setting[] var10001 = new Setting[1];
      var10001[0] = this.grief;
      this.addSettings(var10001);
   }

   public void startJoinTo(int griefId) {
      this.grief.setValue((float)griefId);
      if (!this.isEnable()) {
         this.toggle();
      } else {
         this.startJoin();
      }
   }

   @Override
   public void onEnable() {
      super.onEnable();
      this.startJoin();
   }

   @Override
   public void onDisable() {
      this.joining = false;
      this.pageSwitches = 0;
      super.onDisable();
   }

   @EventTarget
   public void onUpdate(EventTick event) {
      if (this.joining && mc.player != null && mc.world != null && mc.interactionManager != null) {
         if (!(mc.currentScreen instanceof GenericContainerScreen)) {
            this.openServerSelector(false);
         } else {
            this.handleServerMenu();
         }
      }
   }

   @EventTarget
   public void onPacket(PacketEvent event) {
      if (this.joining && mc.player != null && mc.world != null && event.getType() == PacketType.RECEIVE) {
         if (event.getPacket() instanceof GameJoinS2CPacket) {
            ObsidianComponent_hatUtil.sendMessage("Вход на гриф #" + this.targetGrief + ": успешно");
            this.joining = false;
            this.pageSwitches = 0;
         } else if (event.getPacket() instanceof GameMessageS2CPacket packet) {
            String message = packet.content().getString();
            if (message.contains("Подождите несколько секунд перед повторным подключением")) {
               event.cancel();
            } else if (message.contains("К сожалению сервер переполнен")) {
               event.cancel();
               ObsidianComponent_hatUtil.sendMessage("Вход на гриф #" + this.targetGrief + ": неудачно");
            } else {
               this.openServerSelector(false);
            }
         }
      }
   }
   public void startJoin() {
      this.joining = true;
      this.pageSwitches = 0;
      this.targetGrief = Math.round(this.grief.get());
      this.clickTimer.reset();
      this.compassTimer.reset();
      if (mc.player != null && mc.world != null) {
         this.openServerSelector(true);
      }
   }
   public void openServerSelector(boolean force) {
      if (force || this.compassTimer.finished(30L)) {
         if (mc.player != null && mc.interactionManager != null && mc.getNetworkHandler() != null) {
            int previousSlot = mc.player.getInventory().selectedSlot;
            int slot = this.findCompassSlot();
            if (slot != -1) {
               this.pageSwitches = 0;
               mc.player.getInventory().selectedSlot = slot;
               mc.getNetworkHandler().sendPacket(new UpdateSelectedSlotC2SPacket(slot));
               mc.interactionManager.interactItem(mc.player, Hand.MAIN_HAND);
               mc.player.getInventory().selectedSlot = previousSlot;
               mc.getNetworkHandler().sendPacket(new UpdateSelectedSlotC2SPacket(previousSlot));
               this.compassTimer.reset();
            }
         }
      }
   }
   public int findCompassSlot() {
      if (mc.player == null) {
         return -1;
      } else {
         for (int i = 0; i < 9; i++) {
            if (mc.player.getInventory().getStack(i).getItem() == Items.COMPASS) {
               return i;
            }
         }

         return -1;
      }
   }
   public void handleServerMenu() {
      if (mc.currentScreen instanceof GenericContainerScreen screen) {
         if (this.clickTimer.finished(30L)) {
            String title = screen.getTitle().getString();
            ScreenHandler handler = screen.getScreenHandler();
            if (title.contains("Выбор сервера")) {
               this.clickSlot(handler, 21);
               this.pageSwitches = 0;
               this.clickTimer.reset();
            } else if (!this.clickTargetGriefIfVisible(handler)) {
               if (this.targetGrief > 36 && this.pageSwitches < 5) {
                  Slot nextPageSlot = this.getSlot(handler, 44);
                  if (nextPageSlot != null && nextPageSlot.hasStack()) {
                     this.clickSlot(handler, 44);
                     this.pageSwitches += 1;
                     this.clickTimer.reset();
                  }
               }
            }
         }
      }
   }
   public boolean clickTargetGriefIfVisible(ScreenHandler handler) {
      String targetName = "ГРИФ #" + this.targetGrief + " (1.16.5+)";
      String targetPrefix = "ГРИФ #" + this.targetGrief;

      for (int slot = 0; slot < handler.slots.size(); slot++) {
         Slot containerSlot = handler.getSlot(slot);
         if (containerSlot != null && containerSlot.hasStack()) {
            String itemName = containerSlot.getStack().getName().getString();
            if (itemName.equalsIgnoreCase(targetName) || itemName.toUpperCase().contains(targetPrefix)) {
               this.clickSlot(handler, slot);
               this.pageSwitches = 0;
               this.clickTimer.reset();
               return true;
            }
         }
      }

      return false;
   }
   public void clickSlot(ScreenHandler handler, int slot) {
      if (mc.player != null && mc.interactionManager != null) {
         if (slot >= 0 && slot < handler.slots.size()) {
            mc.interactionManager.clickSlot(handler.syncId, slot, 0, SlotActionType.PICKUP, mc.player);
         }
      }
   }
   public Slot getSlot(ScreenHandler handler, int slot) {
      return slot >= 0 && slot < handler.slots.size() ? handler.getSlot(slot) : null;
   }
}
