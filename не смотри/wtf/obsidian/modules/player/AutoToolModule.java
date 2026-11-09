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

























import net.minecraft.block.BlockState;
import net.minecraft.network.packet.c2s.play.UpdateSelectedSlotC2SPacket;
import net.minecraft.util.hit.BlockHitResult;

public class AutoToolModule extends Module {
   public static AutoToolModule INSTANCE = new AutoToolModule();
   private final BooleanSetting packet = new BooleanSetting("Пакетный", false);
   private final BooleanSetting silent = new BooleanSetting("Видно только для других людей", false);
   public int previousSlot = -1;

   public AutoToolModule() {
      super("AutoTool", "При копании берет лучший предмет", Category.PLAYER);
      Setting[] var10001 = new Setting[2];
      var10001[0] = this.packet;
      var10001[1] = this.silent;
      this.addSettings(var10001);
   }

   @EventTarget
   public void onEvent(EventTick event) {
      if (mc.player != null && mc.world != null && mc.interactionManager != null && !mc.player.isCreative()) {
         if (mc.interactionManager.isBreakingBlock()) {
            if (this.previousSlot == (-1)) {
               this.previousSlot = mc.player.getInventory().selectedSlot;
            }

            int toolSlot = this.findOptimalTool();
            if (toolSlot != -1) {
               this.switchToSlot(toolSlot);
            }
         } else if (this.previousSlot != -1) {
            this.switchToSlot(this.previousSlot);
            this.previousSlot = -1;
         }
      } else {
         this.previousSlot = -1;
      }
   }
   public void switchToSlot(int slot) {
      if (slot >= 0 && slot <= (8)) {
         if (mc.player.getInventory().selectedSlot != slot) {
            if (this.silent.isState()) {
               mc.getNetworkHandler().sendPacket(new UpdateSelectedSlotC2SPacket(slot));
            } else if (this.packet.isState()) {
               mc.player.getInventory().selectedSlot = slot;
               mc.getNetworkHandler().sendPacket(new UpdateSelectedSlotC2SPacket(slot));
            } else {
               mc.player.getInventory().selectedSlot = slot;
            }
         }
      }
   }
   public int findOptimalTool() {
      if (mc.crosshairTarget instanceof BlockHitResult blockHitResult) {
         BlockState blockState = mc.world.getBlockState(blockHitResult.getBlockPos());
         return this.findBestToolSlot(blockState);
      } else {
         return -1;
      }
   }
   public int findBestToolSlot(BlockState blockState) {
      int bestSlot = -1;
      float bestSpeed = 1.0f;

      for (int i = 0; i < (9); i++) {
         float speed = mc.player.getInventory().getStack(i).getMiningSpeedMultiplier(blockState);
         if (speed > bestSpeed) {
            bestSpeed = speed;
            bestSlot = i;
         }
      }

      return bestSlot;
   }

   @Override
   public void onDisable() {
      this.previousSlot = -1;
      super.onDisable();
   }
}
