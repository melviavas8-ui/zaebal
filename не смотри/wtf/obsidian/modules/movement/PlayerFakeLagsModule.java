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

























import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import it.unimi.dsi.fastutil.objects.ObjectListIterator;
import lombok.Generated;
import net.minecraft.network.packet.Packet;
import net.minecraft.network.packet.c2s.play.PlayerMoveC2SPacket;

public class PlayerFakeLagsModule extends Module {
   public static PlayerFakeLagsModule INSTANCE = new PlayerFakeLagsModule();
   public final ModeSetting mode;
   public final SliderSetting delay;
   public final BooleanSetting onlyMovement;
   public final ObjectArrayList<Packet<?>> packets;
   public final ObsidianComponent_372 timer;
   public boolean releasing;

   public PlayerFakeLagsModule() {
      super("PlayerFakeLags", "Фейковые лаги", Category.MOVEMENT);
      String[] var10005 = new String[2];
      var10005[0] = "Blink";
      var10005[1] = "Pulse";
      this.mode = new ModeSetting("Режим", "Blink", var10005);
      this.delay = new SliderSetting(
         "Задержка (MS)",
         500.0f,
         50.0f,
         2000.0f,
         50.0f
      );
      this.onlyMovement = new BooleanSetting("Только движение", true);
      this.packets = new ObjectArrayList();
      this.timer = new ObsidianComponent_372();
      this.releasing = false;
      Setting[] var10001 = new Setting[3];
      var10001[0] = this.mode;
      var10001[1] = this.delay;
      var10001[2] = this.onlyMovement;
      this.addSettings(var10001);
   }

   @Override
   public void onEnable() {
      super.onEnable();
      this.packets.clear();
      this.timer.reset();
      this.releasing = false;
   }

   @EventTarget
   public void onEvent(EventTick ignored) {
      if (mc.player != null) {
         if (this.mode.is("Pulse") && this.timer.finished(this.delay.getValue().longValue())) {
            this.releasePackets();
            this.timer.reset();
         }
      }
   }

   @EventTarget
   public void onEvent(PacketEvent event) {
      if (mc.player != null && !this.releasing) {
         if (event.getType() == PacketType.SEND) {
            if (this.onlyMovement.isState()) {
               if (event.getPacket() instanceof PlayerMoveC2SPacket) {
                  event.cancel();
                  this.packets.add(event.getPacket());
               }
            } else {
               event.cancel();
               this.packets.add(event.getPacket());
            }
         }
      }
   }
   public void releasePackets() {
      if (!this.packets.isEmpty()) {
         this.releasing = true;
         ObjectListIterator var1 = this.packets.iterator();

         while (var1.hasNext()) {
            Packet<?> packet = (Packet<?>)var1.next();
            mc.player.networkHandler.sendPacket(packet);
         }

         this.packets.clear();
         this.releasing = false;
      }
   }

   @Override
   public void onDisable() {
      super.onDisable();
      this.releasePackets();
   }

   @Generated
   public ModeSetting getMode() {
      return this.mode;
   }

   @Generated
   public SliderSetting getDelay() {
      return this.delay;
   }

   @Generated
   public BooleanSetting getOnlyMovement() {
      return this.onlyMovement;
   }

   @Generated
   public ObjectArrayList<Packet<?>> getPackets() {
      return this.packets;
   }

   @Generated
   public ObsidianComponent_372 getTimer() {
      return this.timer;
   }

   @Generated
   public boolean isReleasing() {
      return this.releasing;
   }

   @Generated
   public void setReleasing(boolean releasing) {
      this.releasing = releasing;
   }
}
