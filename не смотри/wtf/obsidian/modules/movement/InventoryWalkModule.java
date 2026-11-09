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

























import java.util.ArrayList;
import java.util.List;
import net.minecraft.client.gui.screen.ChatScreen;
import net.minecraft.client.gui.screen.ingame.HandledScreen;
import net.minecraft.client.gui.screen.ingame.InventoryScreen;
import net.minecraft.client.gui.screen.ingame.SignEditScreen;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.client.util.InputUtil;
import net.minecraft.network.packet.c2s.play.ClickSlotC2SPacket;
import net.minecraft.network.packet.c2s.play.CloseHandledScreenC2SPacket;

public class InventoryWalkModule extends Module {
   public static InventoryWalkModule INSTANCE = new InventoryWalkModule();
   public ModeSetting mode;
   public ModeSetting grimVersion;
   public int tick;
   public final List<ClickSlotC2SPacket> pendingPackets;
   public CloseHandledScreenC2SPacket pendingClosePacket;
   public boolean sprintPaused;
   public boolean waitingToClose;
   public int delayedFlushTicks;
   public boolean flushingPackets;

   public InventoryWalkModule() {
      super("InventoryWalk", "Ходьба с открытым инвентарём", Category.MOVEMENT);
      String[] var10005 = new String[2];
      var10005[0] = "Обычный";
      var10005[1] = "Grim";
      this.mode = new ModeSetting("Обход", "Обычный", var10005);
      var10005 = new String[2];
      var10005[0] = "1.21.4";
      var10005[1] = "1.16.5";
      this.grimVersion = new ModeSetting("Версия свапа", "1.21.4", var10005).visible(() -> this.mode.is("Grim"));
      this.tick = 0;
      this.pendingPackets = new ArrayList<>();
      this.pendingClosePacket = null;
      this.sprintPaused = false;
      this.waitingToClose = false;
      this.delayedFlushTicks = -1;
      this.flushingPackets = false;
      Setting[] var10001 = new Setting[2];
      var10001[0] = this.mode;
      var10001[1] = this.grimVersion;
      this.addSettings(var10001);
   }

   @EventTarget
   public void onUpdate(EventTick event) {
      if (mc.player != null) {
         KeyBinding[] var10000 = new KeyBinding[6];
         var10000[0] = mc.options.forwardKey;
         var10000[1] = mc.options.backKey;
         var10000[2] = mc.options.leftKey;
         var10000[3] = mc.options.rightKey;
         var10000[4] = mc.options.jumpKey;
         var10000[5] = mc.options.sprintKey;
         KeyBinding[] pressedKeys = var10000;
         if (this.mode.is("Grim") && this.grimVersion.is("1.21.4") && this.waitingToClose && !ObsidianComponent_268.isMoving()) {
            this.flushQueuedPackets(true);
            this.waitingToClose = false;
            this.tick = 3;
         }

         if (this.mode.is("Grim") && this.grimVersion.is("1.16.5") && this.delayedFlushTicks >= 0) {
            if (this.delayedFlushTicks == 0) {
               this.flushQueuedPackets(true);
               this.delayedFlushTicks = -1;
               this.tick = 1;
            } else {
               this.delayedFlushTicks -= 1;
            }
         }

         if (this.tick == 0 && !this.pendingPackets.isEmpty() && mc.currentScreen == null && !this.waitingToClose) {
            this.sendPendingPackets();
         }

         if (this.tick != 0) {
            KeyBinding[] var9 = pressedKeys;
            int var11 = pressedKeys.length;

            for (int var13 = 0; var13 < var11; var13++) {
               KeyBinding keyBinding = var9[var13];
               keyBinding.setPressed(false);
            }

            this.tick -= 1;
            if (this.tick == 0 && this.sprintPaused) {
               this.sprintPaused = false;
               SprintModule.popPause();
            }
         } else if (!(mc.currentScreen instanceof ChatScreen) && !(mc.currentScreen instanceof SignEditScreen)) {
            if (!this.mode.is("Grim") || !(mc.currentScreen instanceof HandledScreen) || mc.currentScreen instanceof InventoryScreen) {
               if (this.waitingToClose) {
                  KeyBinding[] var8 = pressedKeys;
                  int var10 = pressedKeys.length;

                  for (int var12 = 0; var12 < var10; var12++) {
                     KeyBinding keyBinding = var8[var12];
                     keyBinding.setPressed(false);
                  }
               } else {
                  KeyBinding[] var3 = pressedKeys;
                  int var4 = pressedKeys.length;

                  for (int var5 = 0; var5 < var4; var5++) {
                     KeyBinding keyBinding = var3[var5];
                     boolean isKeyPressed = InputUtil.isKeyPressed(mc.getWindow().getHandle(), keyBinding.getDefaultKey().getCode());
                     keyBinding.setPressed(isKeyPressed);
                  }
               }
            }
         }
      }
   }

   @EventTarget
   public void onPacket(PacketEvent event) {
      if (event.getType() == PacketType.SEND && !this.flushingPackets) {
         Object packet = event.getPacket();
         if (this.mode.is("Grim") && ObsidianComponent_268.isMoving() && mc.currentScreen instanceof InventoryScreen) {
            if (packet instanceof ClickSlotC2SPacket clickPacket) {
               this.pendingPackets.add(clickPacket);
               event.cancel();
            } else {
               if (packet instanceof CloseHandledScreenC2SPacket closePacket) {
                  this.pendingClosePacket = closePacket;
                  if (this.grimVersion.is("1.16.5")) {
                     this.delayedFlushTicks = 1;
                     this.waitingToClose = false;
                  } else {
                     this.waitingToClose = true;
                  }

                  this.pauseSprint();
                  event.cancel();
               }
            }
         }
      }
   }

   @EventTarget
   public void onCloseInv(Event_88 eventCloseInv) {
      if (this.mode.is("Grim") && this.grimVersion.is("1.16.5") && ObsidianComponent_268.isMoving() && mc.currentScreen instanceof InventoryScreen) {
         this.pendingClosePacket = new CloseHandledScreenC2SPacket(eventCloseInv.windowId);
         this.delayedFlushTicks = 1;
         this.pauseSprint();
         this.tick = 1;
         eventCloseInv.cancel();
      } else {
         if (this.mode.is("Grim") && !this.waitingToClose) {
            this.pauseSprint();
            this.tick = 1;
         }
      }
   }
   public void pauseSprint() {
      if (!this.sprintPaused) {
         SprintModule.pushPause(0L);
         this.sprintPaused = true;
      }
   }
   public void sendPendingPackets() {
      if (mc.player != null && mc.getNetworkHandler() != null) {
         this.flushingPackets = true;
         boolean var5 = false ;

         try {
            var5 = true;

            for (ClickSlotC2SPacket packet : this.pendingPackets) {
               mc.getNetworkHandler().sendPacket(packet);
            }

            var5 = false;
         } finally {
            if (var5) {
               this.flushingPackets = false;
            }
         }

         this.flushingPackets = false;
         this.pendingPackets.clear();
      } else {
         this.pendingPackets.clear();
      }
   }
   public void flushQueuedPackets(boolean includeClose) {
      if (mc.player != null && mc.getNetworkHandler() != null) {
         this.sendPendingPackets();
         if (includeClose && this.pendingClosePacket != null) {
            this.flushingPackets = true;
            boolean var4 = false ;

            try {
               var4 = true;
               mc.getNetworkHandler().sendPacket(this.pendingClosePacket);
               var4 = false;
            } finally {
               if (var4) {
                  this.flushingPackets = false;
               }
            }

            this.flushingPackets = false;
            this.pendingClosePacket = null;
         }
      } else {
         this.pendingPackets.clear();
         this.pendingClosePacket = null;
      }
   }

   public static void stopTick(int ticks) {
      InventoryWalkModule inventoryWalk = ModuleManager.inventoryWalk;
      if (inventoryWalk != null && inventoryWalk.isEnable()) {
         inventoryWalk.tick = Math.max(inventoryWalk.tick, ticks);
      }
   }

   @Override
   public void onDisable() {
      super.onDisable();
      this.flushQueuedPackets(true);
      if (this.sprintPaused) {
         this.sprintPaused = false;
         SprintModule.popPause();
      }

      this.waitingToClose = false;
      this.delayedFlushTicks = -1;
      this.flushingPackets = false;
      this.tick = 0;
   }
}
