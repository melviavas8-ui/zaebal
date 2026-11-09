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
























import lombok.Generated;
import net.minecraft.client.MinecraftClient;
import net.minecraft.network.packet.s2c.play.WorldTimeUpdateS2CPacket;
import net.minecraft.util.math.MathHelper;

public class ObsidianComponent_430 {
   public float TPS = 20.0f;
   public float adjustTicks = 0.0f;
   public long timestamp;
   public long lastPacketTime;
   public static final int SAMPLE_SIZE = 20;
   public final float[] tpsSamples = new float[20];
   public int sampleIndex = 0;

   @EventTarget
   public void onPacket(PacketEvent e) {
      if (e.getType() == PacketType.RECEIVE && e.getPacket() instanceof WorldTimeUpdateS2CPacket) {
         this.updateTPS();
      }
   }

   public float getTPS() {
      if (this.lastPacketTime == (0L)) {
         return this.TPS;
      } else {
         MinecraftClient mc = MinecraftClient.getInstance();
         return mc != null
               && mc.getNetworkHandler() != null
               && System.currentTimeMillis() - this.lastPacketTime <= (3500L)
            ? this.TPS
            : 20.0f;
      }
   }
   public void updateTPS() {
      long now = System.nanoTime();
      this.lastPacketTime = System.currentTimeMillis();
      if (this.timestamp == (0L)) {
         this.timestamp = now;
      } else {
         long delay = now - this.timestamp;
         this.timestamp = now;
         if (delay > (0L)) {
            float maxTPS = 20.0f;
            float rawTPS = maxTPS * (1000000000.0f / (float)delay);
            float boundedTPS = MathHelper.clamp(rawTPS, 0.0f, maxTPS);
            this.tpsSamples[this.sampleIndex % 20] = boundedTPS;
            this.sampleIndex += 1;
            int sampleCount = Math.min(this.sampleIndex, 20);
            float sum = 0.0f;

            for (int i = 0; i < sampleCount; i++) {
               float sample = this.tpsSamples[i];
               sum += sample;
            }

            this.TPS = (float)this.round((double)(sum / (float)sampleCount));
            this.adjustTicks = this.TPS - maxTPS;
         }
      }
   }

   public double round(double input) {
      return (double)Math.round(input * 10.0)
         / 10.0;
   }

   @Generated
   public float getAdjustTicks() {
      return this.adjustTicks;
   }

   @Generated
   public long getTimestamp() {
      return this.timestamp;
   }

   @Generated
   public long getLastPacketTime() {
      return this.lastPacketTime;
   }

   @Generated
   public float[] getTpsSamples() {
      return this.tpsSamples;
   }

   @Generated
   public int getSampleIndex() {
      return this.sampleIndex;
   }
}
