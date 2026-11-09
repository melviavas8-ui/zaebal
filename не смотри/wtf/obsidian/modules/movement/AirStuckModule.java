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

























import net.minecraft.entity.EquipmentSlot;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.network.packet.c2s.play.CloseHandledScreenC2SPacket;
import net.minecraft.network.packet.c2s.play.PlayerMoveC2SPacket;
import net.minecraft.network.packet.c2s.play.PlayerMoveC2SPacket.Full;
import net.minecraft.network.packet.c2s.play.PlayerMoveC2SPacket.LookAndOnGround;
import net.minecraft.network.packet.c2s.play.PlayerMoveC2SPacket.OnGroundOnly;
import net.minecraft.network.packet.c2s.play.PlayerMoveC2SPacket.PositionAndOnGround;
import net.minecraft.screen.slot.SlotActionType;
import net.minecraft.util.math.Vec3d;

public class AirStuckModule extends Module {
   public static AirStuckModule INSTANCE = new AirStuckModule();
   public final ModeSetting mode;
   public final BooleanSetting cancelPackets;
   public final BooleanSetting swapElytra;
   public Vec3d freezePosition;
   public boolean frozen;

   public AirStuckModule() {
      super("AirStuck", "Зависает в воздухе", Category.MOVEMENT);
      String[] var10005 = new String[2];
      var10005[0] = "Обычный";
      var10005[1] = "LonyGrief";
      this.mode = new ModeSetting("Мод", "Обычный", var10005);
      this.cancelPackets = new BooleanSetting("Отменять пакеты", true);
      this.swapElytra = new BooleanSetting("Свапать элитру", true);
      this.freezePosition = Vec3d.ZERO;
      this.frozen = false;
      Setting[] var10001 = new Setting[3];
      var10001[0] = this.mode;
      var10001[1] = this.cancelPackets;
      var10001[2] = this.swapElytra;
      this.addSettings(var10001);
   }

   @Override
   public void onEnable() {
      this.frozen = false;
      if (mc.player != null && this.swapElytra.isState()) {
         this.swapChestEquipment();
      }

      if (mc.player != null && this.mode.is("Обычный")) {
         this.freezePosition = mc.player.getPos();
         this.frozen = true;
      }

      super.onEnable();
   }

   @Override
   public void onDisable() {
      this.frozen = false;
      super.onDisable();
   }
   public void swapChestEquipment() {
      ItemStack chestStack = mc.player.getEquippedStack(EquipmentSlot.CHEST);
      if (chestStack.isOf(Items.ELYTRA)) {
         int chestplateSlot = ObsidianComponent_225.findBestChestplateSlot();
         if (chestplateSlot != -1) {
            this.doSwap(chestplateSlot);
         }
      }
   }
   public void doSwap(int slot) {
      if (slot >= 0 && slot < 9) {
         mc.interactionManager
            .clickSlot(0, 6, slot, SlotActionType.SWAP, mc.player);
      } else {
         mc.interactionManager.clickSlot(0, slot, 0, SlotActionType.SWAP, mc.player);
         mc.interactionManager
            .clickSlot(0, 6, 0, SlotActionType.SWAP, mc.player);
         mc.interactionManager.clickSlot(0, slot, 0, SlotActionType.SWAP, mc.player);
      }

      mc.player.networkHandler.sendPacket(new CloseHandledScreenC2SPacket(0));
   }

   @EventTarget
   public void onMove(Event_321 e) {
      if (mc.player != null) {
         if (this.mode.is("LonyGrief")
            && !this.frozen
            && mc.player.fallDistance > 0.0f
            && mc.player.getVelocity().y < 0.0) {
            this.freezePosition = mc.player.getPos();
            this.frozen = true;
         }

         if (this.frozen) {
            e.setMovePos(Vec3d.ZERO);
            mc.player.setPosition(this.freezePosition.x, this.freezePosition.y, this.freezePosition.z);
            mc.player
               .setVelocity(
                  0.0,
                  0.0,
                  0.0
               );
         }
      }
   }

   @EventTarget
   public void onPacket(PacketEvent e) {
      if (this.frozen && e.getType() == PacketType.SEND) {
         if (e.getPacket() instanceof PlayerMoveC2SPacket packet) {
            if (this.cancelPackets.isState()) {
               e.cancel();
            } else {
               e.cancel();
               ObsidianComponent_239.sendSilentPacket(this.createFrozenPacket(packet));
            }
         }
      }
   }
   public PlayerMoveC2SPacket createFrozenPacket(PlayerMoveC2SPacket packet) {
      boolean onGround = packet.isOnGround();
      boolean horizontalCollision = packet.horizontalCollision();
      if (packet.changesPosition() && packet.changesLook()) {
         return new Full(
            this.freezePosition.x,
            this.freezePosition.y,
            this.freezePosition.z,
            packet.getYaw(mc.player.getYaw()),
            packet.getPitch(mc.player.getPitch()),
            onGround,
            horizontalCollision
         );
      } else if (packet.changesPosition()) {
         return new PositionAndOnGround(this.freezePosition.x, this.freezePosition.y, this.freezePosition.z, onGround, horizontalCollision);
      } else {
         return (PlayerMoveC2SPacket)(packet.changesLook()
            ? new LookAndOnGround(packet.getYaw(mc.player.getYaw()), packet.getPitch(mc.player.getPitch()), onGround, horizontalCollision)
            : new OnGroundOnly(onGround, horizontalCollision));
      }
   }
}
