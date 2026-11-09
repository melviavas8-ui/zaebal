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
























import java.io.Serializable;
import lombok.Generated;

public class ObsidianComponent_168 implements Serializable {
   public static final long serialVersionUID = 1L;
   public final float yaw;
   public final float pitch;
   public final float deltaYaw;
   public final float deltaPitch;
   public final double distance;
   public final long timestamp;
   public final boolean isCritical;
   public final double targetSpeed;
   public final String targetType;
   public final float smoothness;

   public ObsidianComponent_168(
      float yaw, float pitch, float deltaYaw, float deltaPitch, double distance, boolean isCritical, double targetSpeed, String targetType, float smoothness
   ) {
      this.yaw = yaw;
      this.pitch = pitch;
      this.deltaYaw = deltaYaw;
      this.deltaPitch = deltaPitch;
      this.distance = distance;
      this.timestamp = System.currentTimeMillis();
      this.isCritical = isCritical;
      this.targetSpeed = targetSpeed;
      this.targetType = targetType;
      this.smoothness = smoothness;
   }

   @Generated
   public float getYaw() {
      return this.yaw;
   }

   @Generated
   public float getPitch() {
      return this.pitch;
   }

   @Generated
   public float getDeltaYaw() {
      return this.deltaYaw;
   }

   @Generated
   public float getDeltaPitch() {
      return this.deltaPitch;
   }

   @Generated
   public double getDistance() {
      return this.distance;
   }

   @Generated
   public long getTimestamp() {
      return this.timestamp;
   }

   @Generated
   public boolean isCritical() {
      return this.isCritical;
   }

   @Generated
   public double getTargetSpeed() {
      return this.targetSpeed;
   }

   @Generated
   public String getTargetType() {
      return this.targetType;
   }

   @Generated
   public float getSmoothness() {
      return this.smoothness;
   }

   @Generated
   @Override
   public boolean equals(Object o) {
      if (o == this) {
         return true;
      } else if (!(o instanceof ObsidianComponent_168 other)) {
         return false;
      } else if (!other.canEqual(this)) {
         return false;
      } else if (Float.compare(this.getYaw(), other.getYaw()) != 0) {
         return false;
      } else if (Float.compare(this.getPitch(), other.getPitch()) != 0) {
         return false;
      } else if (Float.compare(this.getDeltaYaw(), other.getDeltaYaw()) != 0) {
         return false;
      } else if (Float.compare(this.getDeltaPitch(), other.getDeltaPitch()) != 0) {
         return false;
      } else if (Double.compare(this.getDistance(), other.getDistance()) != 0) {
         return false;
      } else if (this.getTimestamp() != other.getTimestamp()) {
         return false;
      } else if (this.isCritical() != other.isCritical()) {
         return false;
      } else if (Double.compare(this.getTargetSpeed(), other.getTargetSpeed()) != 0) {
         return false;
      } else if (Float.compare(this.getSmoothness(), other.getSmoothness()) != 0) {
         return false;
      } else {
         Object this$targetType = this.getTargetType();
         Object other$targetType = other.getTargetType();
         return ((this$targetType == null ? other$targetType == null : this$targetType.equals(other$targetType)));
      }
   }

   @Generated
   protected boolean canEqual(Object other) {
      return other instanceof ObsidianComponent_168;
   }

   @Generated
   @Override
   public int hashCode() {
      int PRIME = 59;
      int result = 1;
      result = result * (59) + Float.floatToIntBits(this.getYaw());
      result = result * 59 + Float.floatToIntBits(this.getPitch());
      result = result * (59) + Float.floatToIntBits(this.getDeltaYaw());
      result = result * (59) + Float.floatToIntBits(this.getDeltaPitch());
      long $distance = Double.doubleToLongBits(this.getDistance());
      result = result * 59 + (int)($distance >>> (32) ^ $distance);
      long $timestamp = this.getTimestamp();
      result = result * (59) + (int)($timestamp >>> (32) ^ $timestamp);
      result = result * 59 + (this.isCritical() ? 79 : 97);
      long $targetSpeed = Double.doubleToLongBits(this.getTargetSpeed());
      result = result * (59) + (int)($targetSpeed >>> 32 ^ $targetSpeed);
      result = result * (59) + Float.floatToIntBits(this.getSmoothness());
      Object $targetType = this.getTargetType();
      return result * (59) + ($targetType == null ? 43 : $targetType.hashCode());
   }

   @Generated
   @Override
   public String toString() {
      return "NeuroPattern(yaw="
         + this.getYaw()
         + ", pitch="
         + this.getPitch()
         + ", deltaYaw="
         + this.getDeltaYaw()
         + ", deltaPitch="
         + this.getDeltaPitch()
         + ", distance="
         + this.getDistance()
         + ", timestamp="
         + this.getTimestamp()
         + ", isCritical="
         + this.isCritical()
         + ", targetSpeed="
         + this.getTargetSpeed()
         + ", targetType="
         + this.getTargetType()
         + ", smoothness="
         + this.getSmoothness()
         + ")";
   }
}
