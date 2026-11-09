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

public final class ObsidianComponent_113 {
   public final String targetType;
   public final String distanceBucket;
   public final String movementState;
   public final String critState;
   public final String healthState;

   @Override
   public String toString() {
      return this.targetType + "_" + this.distanceBucket + "_" + this.movementState + "_" + this.critState + "_" + this.healthState;
   }

   @Generated
   public ObsidianComponent_113(String targetType, String distanceBucket, String movementState, String critState, String healthState) {
      this.targetType = targetType;
      this.distanceBucket = distanceBucket;
      this.movementState = movementState;
      this.critState = critState;
      this.healthState = healthState;
   }

   @Generated
   public String getTargetType() {
      return this.targetType;
   }

   @Generated
   public String getDistanceBucket() {
      return this.distanceBucket;
   }

   @Generated
   public String getMovementState() {
      return this.movementState;
   }

   @Generated
   public String getCritState() {
      return this.critState;
   }

   @Generated
   public String getHealthState() {
      return this.healthState;
   }

   @Generated
   @Override
   public boolean equals(Object o) {
      if (o == this) {
         return true;
      } else if (!(o instanceof ObsidianComponent_113 other)) {
         return false;
      } else {
         Object this$targetType = this.getTargetType();
         Object other$targetType = other.getTargetType();
         if (this$targetType == null ? other$targetType == null : this$targetType.equals(other$targetType)) {
            Object this$distanceBucket = this.getDistanceBucket();
            Object other$distanceBucket = other.getDistanceBucket();
            if (this$distanceBucket == null ? other$distanceBucket == null : this$distanceBucket.equals(other$distanceBucket)) {
               Object this$movementState = this.getMovementState();
               Object other$movementState = other.getMovementState();
               if (this$movementState == null ? other$movementState == null : this$movementState.equals(other$movementState)) {
                  Object this$critState = this.getCritState();
                  Object other$critState = other.getCritState();
                  if (this$critState == null ? other$critState == null : this$critState.equals(other$critState)) {
                     Object this$healthState = this.getHealthState();
                     Object other$healthState = other.getHealthState();
                     return ((this$healthState == null ? other$healthState == null : this$healthState.equals(other$healthState)));
                  } else {
                     return false;
                  }
               } else {
                  return false;
               }
            } else {
               return false;
            }
         } else {
            return false;
         }
      }
   }

   @Generated
   @Override
   public int hashCode() {
      int PRIME = 59;
      int result = 1;
      Object $targetType = this.getTargetType();
      result = result * 59
         + ($targetType == null ? 43 : $targetType.hashCode());
      Object $distanceBucket = this.getDistanceBucket();
      result = result * (59) + ($distanceBucket == null ? 43 : $distanceBucket.hashCode());
      Object $movementState = this.getMovementState();
      result = result * (59) + ($movementState == null ? 43 : $movementState.hashCode());
      Object $critState = this.getCritState();
      result = result * (59) + ($critState == null ? 43 : $critState.hashCode());
      Object $healthState = this.getHealthState();
      return result * (59) + ($healthState == null ? 43 : $healthState.hashCode());
   }
}
