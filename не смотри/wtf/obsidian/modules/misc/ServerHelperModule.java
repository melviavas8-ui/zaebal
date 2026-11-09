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

























import java.util.List;
import java.util.Locale;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.item.consume.UseAction;
import net.minecraft.network.packet.c2s.play.ClientCommandC2SPacket;
import net.minecraft.network.packet.c2s.play.CloseHandledScreenC2SPacket;
import net.minecraft.network.packet.c2s.play.PlayerInputC2SPacket;
import net.minecraft.network.packet.c2s.play.UpdateSelectedSlotC2SPacket;
import net.minecraft.network.packet.c2s.play.ClientCommandC2SPacket.Mode;
import net.minecraft.screen.slot.SlotActionType;
import net.minecraft.util.Hand;
import net.minecraft.util.PlayerInput;

public class ServerHelperModule extends Module {
   public static ServerHelperModule INSTANCE = new ServerHelperModule();
   public final ModeSetting mode;
   public final ObsidianComponent_261 featherKey;
   public final ObsidianComponent_261 magmaKey;
   public final ObsidianComponent_261 cryingObsidianKey;
   public final ObsidianComponent_261 clayKey;
   public final ObsidianComponent_261 disorientationKey;
   public final ObsidianComponent_261 trapKey;
   public final ObsidianComponent_261 plastKey;
   public final ObsidianComponent_261 pilKey;
   public final ObsidianComponent_261 snegKey;
   public final ObsidianComponent_261 auraKey;
   public Item pendingItem;
   public ObsidianComponent_272 pendingAction;

   public ServerHelperModule() {
      super("ServerHelper", "Помощник для серверов", Category.MISC);
      String[] var10005 = new String[2];
      var10005[0] = "Lony";
      var10005[1] = "Spooky";
      this.mode = new ModeSetting("Режим", "Lony", var10005);
      this.featherKey = new ObsidianComponent_261("Перышко", -1).visible(() -> this.mode.is("Lony"));
      this.magmaKey = new ObsidianComponent_261("Ливалка", -1).visible(() -> this.mode.is("Lony"));
      this.cryingObsidianKey = new ObsidianComponent_261("Трапка", -1).visible(() -> this.mode.is("Lony"));
      this.clayKey = new ObsidianComponent_261("Ливалка с платформой", -1).visible(() -> this.mode.is("Lony"));
      this.disorientationKey = new ObsidianComponent_261("Дезориентация", -1).visible(() -> this.mode.is("Spooky"));
      this.trapKey = new ObsidianComponent_261("Трапка", -1).visible(() -> this.mode.is("Spooky"));
      this.plastKey = new ObsidianComponent_261("Пласт", -1).visible(() -> this.mode.is("Spooky"));
      this.pilKey = new ObsidianComponent_261("Явная пыль", -1).visible(() -> this.mode.is("Spooky"));
      this.snegKey = new ObsidianComponent_261("Снег заморозки", -1).visible(() -> this.mode.is("Spooky"));
      this.auraKey = new ObsidianComponent_261("Божья аура", -1).visible(() -> this.mode.is("Spooky"));
      Setting[] var10001 = new Setting[11];
      var10001[0] = this.mode;
      var10001[1] = this.featherKey;
      var10001[2] = this.magmaKey;
      var10001[3] = this.cryingObsidianKey;
      var10001[4] = this.clayKey;
      var10001[5] = this.disorientationKey;
      var10001[6] = this.trapKey;
      var10001[7] = this.plastKey;
      var10001[8] = this.pilKey;
      var10001[9] = this.snegKey;
      var10001[10] = this.auraKey;
      this.addSettings(var10001);
   }

   public boolean isSpookyMode() {
      return this.mode.is("Spooky");
   }

   public boolean isLonyMode() {
      return this.mode.is("Lony");
   }

   public List<ObsidianComponent_317> getLonyHelperBinds() {
      return List.of(
         new ObsidianComponent_317("Перышко", Items.FEATHER, this.featherKey),
         new ObsidianComponent_317("Ливалка", Items.MAGMA_CREAM, this.magmaKey),
         new ObsidianComponent_317("Трапка", Items.CRYING_OBSIDIAN, this.cryingObsidianKey),
         new ObsidianComponent_317("Ливалка с платформой", Items.CLAY, this.clayKey)
      );
   }

   public List<ObsidianComponent_317> getSpookyHelperBinds() {
      return List.of(
         new ObsidianComponent_317("Дезориентация", Items.ENDER_EYE, this.disorientationKey),
         new ObsidianComponent_317("Трапка", Items.NETHERITE_SCRAP, this.trapKey),
         new ObsidianComponent_317("Пласт", Items.DRIED_KELP, this.plastKey),
         new ObsidianComponent_317("Явная пыль", Items.SUGAR, this.pilKey),
         new ObsidianComponent_317("Снег заморозки", Items.SNOWBALL, this.snegKey),
         new ObsidianComponent_317("Божья аура", Items.PHANTOM_MEMBRANE, this.auraKey)
      );
   }

   @Override
   public void onEnable() {
      this.pendingItem = null;
      this.pendingAction = null;
      super.onEnable();
   }

   @Override
   public void onDisable() {
      this.pendingItem = null;
      this.pendingAction = null;
      super.onDisable();
   }

   @EventTarget
   public void onBinding(InputEvent event) {
      if (mc.currentScreen == null) {
         if (this.mode.is("Lony")) {
            if (event.getKey() == this.featherKey.getKey()) {
               this.pendingItem = Items.FEATHER;
            } else if (event.getKey() == this.magmaKey.getKey()) {
               this.pendingItem = Items.MAGMA_CREAM;
            } else if (event.getKey() == this.cryingObsidianKey.getKey()) {
               this.pendingItem = Items.CRYING_OBSIDIAN;
            } else if (event.getKey() == this.clayKey.getKey()) {
               this.pendingItem = Items.CLAY_BALL;
            }
         } else {
            if (event.getKey() == this.disorientationKey.getKey()) {
               this.pendingAction = ObsidianComponent_272.DISORIENTATION;
            } else if (event.getKey() == this.trapKey.getKey()) {
               this.pendingAction = ObsidianComponent_272.TRAP;
            } else if (event.getKey() == this.plastKey.getKey()) {
               this.pendingAction = ObsidianComponent_272.PLAST;
            } else if (event.getKey() == this.pilKey.getKey()) {
               this.pendingAction = ObsidianComponent_272.DUST;
            } else if (event.getKey() == this.snegKey.getKey()) {
               this.pendingAction = ObsidianComponent_272.FREEZE_SNOW;
            } else if (event.getKey() == this.auraKey.getKey()) {
               this.pendingAction = ObsidianComponent_272.AURA;
            }
         }
      }
   }

   @EventTarget
   public void onUpdate(EventTick event) {
      if (mc.player == null || mc.world == null) {
         this.pendingItem = null;
         this.pendingAction = null;
      } else if (this.mode.is("Lony")) {
         if (this.pendingItem != null) {
            ObsidianComponent_225.swapAndUseHvH(this.pendingItem);
            this.pendingItem = null;
         }
      } else if (this.pendingAction != null && mc.interactionManager != null) {
         this.useAction(this.pendingAction);
         this.pendingAction = null;
      }
   }
   public void useAction(ObsidianComponent_272 action) {
      if (mc.player.getItemCooldownManager().isCoolingDown(new ItemStack(action.item))) {
         ObsidianComponent_hatUtil.sendMessage("У предмета " + action.cooldownName + " есть кд");
      } else if (this.matchesAction(mc.player.getMainHandStack(), action)) {
         mc.interactionManager.interactItem(mc.player, Hand.MAIN_HAND);
         mc.player.swingHand(Hand.MAIN_HAND);
         ObsidianComponent_hatUtil.sendMessage(action.successText);
      } else if (this.matchesAction(mc.player.getOffHandStack(), action)) {
         mc.interactionManager.interactItem(mc.player, Hand.OFF_HAND);
         mc.player.swingHand(Hand.OFF_HAND);
         ObsidianComponent_hatUtil.sendMessage(action.successText);
      } else {
         int hotbarSlot = this.findMatchingSlot(action, 0, 8);
         if (hotbarSlot != (-1)) {
            int previousSlot = mc.player.getInventory().selectedSlot;
            mc.player.networkHandler.sendPacket(new UpdateSelectedSlotC2SPacket(hotbarSlot));
            mc.interactionManager.interactItem(mc.player, Hand.MAIN_HAND);
            mc.player.swingHand(Hand.MAIN_HAND);
            mc.player.networkHandler.sendPacket(new UpdateSelectedSlotC2SPacket(previousSlot));
            ObsidianComponent_hatUtil.sendMessage(action.successText);
         } else {
            int inventorySlot = this.findMatchingSlot(action, 9, 35);
            if (inventorySlot != -1) {
               this.useFromInventory(inventorySlot);
               ObsidianComponent_hatUtil.sendMessage(action.successText);
            } else {
               ObsidianComponent_hatUtil.sendMessage(action.failText);
            }
         }
      }
   }
   public void useFromInventory(int inventorySlot) {
      int previousSlot = mc.player.getInventory().selectedSlot;
      int hotbarSlot = this.findTemporaryHotbarSlot();
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

      mc.interactionManager.clickSlot(0, inventorySlot, hotbarSlot, SlotActionType.SWAP, mc.player);
      mc.player.networkHandler.sendPacket(new CloseHandledScreenC2SPacket(0));
      mc.player.networkHandler.sendPacket(new UpdateSelectedSlotC2SPacket(hotbarSlot));
      mc.interactionManager.interactItem(mc.player, Hand.MAIN_HAND);
      mc.player.swingHand(Hand.MAIN_HAND);
      mc.player.networkHandler.sendPacket(new UpdateSelectedSlotC2SPacket(previousSlot));
      mc.interactionManager.clickSlot(0, inventorySlot, hotbarSlot, SlotActionType.SWAP, mc.player);
      mc.player.networkHandler.sendPacket(new CloseHandledScreenC2SPacket(0));
      if (wasSprinting) {
         mc.player.networkHandler.sendPacket(new PlayerInputC2SPacket(mc.player.input.playerInput));
      }
   }
   public int findTemporaryHotbarSlot() {
      int fallback = 8;

      for (int slot = 0; slot < 9; slot++) {
         if (slot != mc.player.getInventory().selectedSlot) {
            ItemStack stack = mc.player.getInventory().getStack(slot);
            if (stack.isEmpty()) {
               return slot;
            }

            if (stack.getUseAction() == UseAction.NONE) {
               fallback = slot;
            }
         }
      }

      return fallback;
   }
   public int findMatchingSlot(ObsidianComponent_272 action, int start, int end) {
      for (int slot = start; slot <= end; slot++) {
         if (this.matchesAction(mc.player.getInventory().getStack(slot), action)) {
            return slot;
         }
      }

      return -1;
   }
   public boolean matchesAction(ItemStack stack, ObsidianComponent_272 action) {
      return (stack != null && !stack.isEmpty() && stack.getItem() == action.item
         ? stack.getName().getString().toLowerCase(Locale.ROOT).contains(action.query)
         : false);
   }
}
