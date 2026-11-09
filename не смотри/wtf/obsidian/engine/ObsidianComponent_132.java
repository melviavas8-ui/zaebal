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
























import java.lang.reflect.InvocationTargetException;
import lombok.Generated;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.network.packet.c2s.play.ClientCommandC2SPacket;
import net.minecraft.network.packet.c2s.play.PlayerMoveC2SPacket;
import net.minecraft.network.packet.c2s.play.UpdateSelectedSlotC2SPacket;
import net.minecraft.network.packet.c2s.play.ClientCommandC2SPacket.Mode;
import net.minecraft.network.packet.s2c.play.EntityStatusS2CPacket;

public class ObsidianComponent_132 implements IInstance {
   public int serverSlot;
   public float serverYaw;
   public float serverPitch;
   public float fallDistance;
   public double serverX;
   public double serverY;
   public double serverZ;
   public boolean serverOnGround;
   public boolean serverSprinting;
   public boolean serverSneaking;
   public boolean serverHorizontalCollision;

   public void ServerManager() {
      EventManager.register(this);
   }

   @EventTarget
   public void onTick(Event_204 e) {
      if (mc.player != null && mc.world != null) {
         double y = mc.player.prevY - mc.player.getY();
         if (mc.player.isOnGround()) {
            this.fallDistance = 0.0f;
         } else if (y > 0.0) {
            this.fallDistance += (float)y;
         }
      }
   }

         @EventTarget
   public void onPacketSend(PacketEvent e) {
      if (mc.player != null && mc.world != null) {
         if (e.getPacket() instanceof PlayerMoveC2SPacket packet) {
            if (packet.changesPosition()) {
               this.serverX = packet.getX(mc.player.getX());
               this.serverY = packet.getY(mc.player.getY());
               this.serverZ = packet.getZ(mc.player.getZ());
            }

            if (packet.changesLook()) {
               this.serverYaw = packet.getYaw(mc.player.getYaw());
               this.serverPitch = packet.getPitch(mc.player.getPitch());
            }

            this.serverOnGround = packet.isOnGround();
            this.serverHorizontalCollision = packet.horizontalCollision();
         }

         if (e.getPacket() instanceof UpdateSelectedSlotC2SPacket packet) {
            this.serverSlot = packet.getSelectedSlot();
         }

         if (e.getPacket() instanceof ClientCommandC2SPacket packet) {
            switch (packet.getMode()) {
            case START_SPRINTING:
                  e.setCancelled(this.serverSprinting);
                  if (!e.isCancelled()) {
                     this.serverSprinting = true;
                  }
                  break;
               case STOP_SPRINTING:
                  e.setCancelled((!this.serverSprinting));
                  if (!e.isCancelled()) {
                     this.serverSprinting = false;
                  }
                  break;
               case PRESS_SHIFT_KEY:
                  this.serverSneaking = true;
                  break;
               case RELEASE_SHIFT_KEY:
                  this.serverSneaking = false;
            }
         }
      }
   }

   @EventTarget
   public void onPacketReceive(PacketEvent e) throws InvocationTargetException, IllegalAccessException, InstantiationException {
      if (mc.player != null && mc.world != null) {
         if (e.getPacket() instanceof EntityStatusS2CPacket packet && packet.getStatus() == (35)) {
            if (!(packet.getEntity(mc.world) instanceof PlayerEntity player)) {
               return;
            }

            EventManager.invoke(new Event_196(player));
         }
      }
   }

   @Generated
   public int getServerSlot() {
      return this.serverSlot;
   }

   @Generated
   public float getServerYaw() {
      return this.serverYaw;
   }

   @Generated
   public float getServerPitch() {
      return this.serverPitch;
   }

   @Generated
   public float getFallDistance() {
      return this.fallDistance;
   }

   @Generated
   public double getServerX() {
      return this.serverX;
   }

   @Generated
   public double getServerY() {
      return this.serverY;
   }

   @Generated
   public double getServerZ() {
      return this.serverZ;
   }

   @Generated
   public boolean isServerOnGround() {
      return this.serverOnGround;
   }

   @Generated
   public boolean isServerSprinting() {
      return this.serverSprinting;
   }

   @Generated
   public boolean isServerSneaking() {
      return this.serverSneaking;
   }

   @Generated
   public boolean isServerHorizontalCollision() {
      return this.serverHorizontalCollision;
   }
}
