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

























import net.minecraft.client.gui.screen.ingame.HandledScreen;
import net.minecraft.network.packet.c2s.play.ClickSlotC2SPacket;
import net.minecraft.network.packet.c2s.play.PlayerActionC2SPacket;
import net.minecraft.network.packet.c2s.play.PlayerActionC2SPacket.Action;
import net.minecraft.screen.slot.Slot;
import net.minecraft.screen.slot.SlotActionType;


public class LockSlotModule extends Module {
   public static LockSlotModule INSTANCE = new LockSlotModule();
   public final MultiBooleanSetting slots;

   public LockSlotModule() {
      super("LockSlot", "Блокирует выброс предметов из выбранных слотов", Category.PLAYER);
      BooleanSetting[] var10004 = new BooleanSetting[9];
      var10004[0] = new BooleanSetting("1", false);
      var10004[1] = new BooleanSetting("2", false);
      var10004[2] = new BooleanSetting("3", false);
      var10004[3] = new BooleanSetting("4", false);
      var10004[4] = new BooleanSetting("5", false);
      var10004[5] = new BooleanSetting("6", false);
      var10004[6] = new BooleanSetting("7", false);
      var10004[7] = new BooleanSetting("8", false);
      var10004[8] = new BooleanSetting("9", false);
      this.slots = new MultiBooleanSetting("Слоты", var10004);
      Setting[] var10001 = new Setting[1];
      var10001[0] = this.slots;
      this.addSettings(var10001);
   }

   @EventTarget
   public void onPacket(PacketEvent event) {
      if (mc.player != null && event.getType() == PacketType.SEND) {
         if (!(mc.currentScreen instanceof HandledScreen)) {
            if (event.getPacket() instanceof PlayerActionC2SPacket packet) {
               if (packet.getAction() == Action.DROP_ITEM || packet.getAction() == Action.DROP_ALL_ITEMS) {
                  if (this.isCurrentSlotLockedForDrop()) {
                     event.cancel();
                     this.sendLockedMessage(mc.player.getInventory().selectedSlot);
                  }
               }
            } else {
               if (event.getPacket() instanceof ClickSlotC2SPacket packetx && packetx.getActionType() == SlotActionType.THROW) {
                  int hotbarSlot = this.getHotbarSlotFromClick(packetx.getSlot());
                  if (hotbarSlot >= 0 && this.isHotbarSlotLocked(hotbarSlot)) {
                     event.cancel();
                     this.sendLockedMessage(hotbarSlot);
                  }
               }
            }
         }
      }
   }

   public boolean isCurrentSlotLockedForDrop() {
      if (!this.isEnable() || mc.player == null || mc.player.getMainHandStack().isEmpty()) {
         return false;
      } else {
         return (mc.currentScreen instanceof HandledScreen
            ? false : this.isHotbarSlotLocked(mc.player.getInventory().selectedSlot));
      }
   }
   public boolean isHotbarSlotLocked(int slot) {
      return (slot >= 0 && slot < this.slots.getSettings().size() ? this.slots.getSettings().get(slot).isState() : false);
   }
   public int getHotbarSlotFromClick(int slotId) {
      if (mc.player != null && slotId >= 0 && slotId < mc.player.currentScreenHandler.slots.size()) {
         Slot slot = mc.player.currentScreenHandler.getSlot(slotId);
         SlotAccessor accessor = (SlotAccessor)slot;
         int inventoryIndex = accessor.obsidian$getIndex();
         return accessor.obsidian$getInventory() == mc.player.getInventory() && inventoryIndex >= 0 && inventoryIndex <= (8)
            ? inventoryIndex
            : -1;
      } else {
         return -1;
      }
   }
   public void sendLockedMessage(int slot) {
      ObsidianComponent_hatUtil.sendMessage("Выброс предмета из слота " + (slot + 1) + " заблокирован");
   }
}
