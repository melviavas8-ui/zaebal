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

























import net.minecraft.network.packet.s2c.play.EntityVelocityUpdateS2CPacket;
import net.minecraft.network.packet.s2c.play.ExplosionS2CPacket;

public class NoVelocityModule extends Module {
   public static NoVelocityModule INSTANCE = new NoVelocityModule();
   public final ModeSetting mode;
   public final BooleanSetting explosions;
   public boolean needJump;
   public int hurtTicks;

   public NoVelocityModule() {
      super("NoVelocity", "Отключает отдачу от урона", Category.MOVEMENT);
      String[] var10005 = new String[3];
      var10005[0] = "Vanilla";
      var10005[1] = "Grim";
      var10005[2] = "Jump Reset";
      this.mode = new ModeSetting("Мод", "Vanilla", var10005);
      this.explosions = new BooleanSetting("Взрывы", true);
      Setting[] var10001 = new Setting[2];
      var10001[0] = this.mode;
      var10001[1] = this.explosions;
      this.addSettings(var10001);
   }

   @Override
   public void onEnable() {
      super.onEnable();
      this.needJump = false;
      this.hurtTicks = 0;
   }

   @Override
   public void onDisable() {
      super.onDisable();
      this.needJump = false;
      this.hurtTicks = 0;
   }

   @EventTarget
   public void onPacket(PacketEvent event) {
      if (mc.player != null && mc.world != null) {
         if (event.getType() == PacketType.RECEIVE) {
            if (event.getPacket() instanceof EntityVelocityUpdateS2CPacket packet) {
               if (packet.getEntityId() != mc.player.getId()) {
                  return;
               }

               if (this.mode.is("Vanilla")) {
                  event.cancel();
               }

               if (this.mode.is("Grim")) {
                  event.cancel();
                  double velY = packet.getVelocityY() / 8000.0;
                  if (mc.player.isOnGround() && velY > 0.0) {
                     mc.player
                        .setVelocity(
                           mc.player.getVelocity().x,
                           0.0,
                           mc.player.getVelocity().z
                        );
                  } else if (velY > 0.0) {
                     mc.player
                        .setVelocity(
                           mc.player.getVelocity().x,
                           0.0,
                           mc.player.getVelocity().z
                        );
                  }
               }

               if (this.mode.is("Jump Reset")) {
                  double velY = packet.getVelocityY() / 8000.0;
                  if (velY > 0.1) {
                     this.needJump = true;
                     this.hurtTicks = 0;
                  }
               }
            }

            if (this.explosions.isState() && event.getPacket() instanceof ExplosionS2CPacket && (this.mode.is("Vanilla") || this.mode.is("Grim"))) {
               event.cancel();
            }
         }
      }
   }

   @EventTarget
   public void onUpdate(EventTick event) {
      if (mc.player != null) {
         if (this.mode.is("Jump Reset") && this.needJump) {
            this.hurtTicks += 1;
            if (mc.player.isOnGround()) {
               mc.player.jump();
               this.needJump = false;
               this.hurtTicks = 0;
            }

            if (this.hurtTicks > 5) {
               this.needJump = false;
               this.hurtTicks = 0;
            }
         }
      }
   }
}
