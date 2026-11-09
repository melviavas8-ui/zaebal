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

























import lombok.Generated;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec2f;

public class ElytraBoostModule extends Module {
   public static final String[] RANGE_LABELS;
   public static final long DEBUG_MESSAGE_INTERVAL_MS = 800L;
   
   public final SliderSetting[] yawSpeeds = new SliderSetting[9];
   public final SliderSetting[] pitchSpeeds = new SliderSetting[9];
   public final ModeSetting mode;
   public final BooleanSetting debug;
   public long lastDebugMessageAt;

   public ElytraBoostModule() {
      super("ElytraBoost", "Ускоряет на элитрах", Category.MOVEMENT);
      String[] var10005 = new String[5];
      var10005[0] = "Custom";
      var10005[1] = "LonyGrief";
      var10005[2] = "BravoHVH";
      var10005[3] = "ReallyWorld";
      var10005[4] = "SlimeWorld";
      this.mode = new ModeSetting("Сервер", "Custom", var10005);
      this.debug = new BooleanSetting("Дебаг", false).visible(this::isCustomMode);

      for (int i = 0; i < this.yawSpeeds.length; i++) {
         this.yawSpeeds[i] = new SliderSetting(
               "yaw " + RANGE_LABELS[i],
               1.5f,
               1.5f,
               2.5f,
               0.009999999776482582f
            )
            .visible(this::isCustomMode);
      }

      for (int i = 0; i < this.pitchSpeeds.length; i++) {
         this.pitchSpeeds[i] = new SliderSetting(
               "pitch " + RANGE_LABELS[i],
               1.5f,
               1.5f,
               2.5f,
               0.009999999776482582f
            )
            .visible(this::isCustomMode);
      }

      Setting[] var10001 = new Setting[2];
      var10001[0] = this.mode;
      var10001[1] = this.debug;
      this.addSettings(var10001);
      this.addSettings(this.yawSpeeds);
      this.addSettings(this.pitchSpeeds);
   }

   public boolean isCustomMode() {
      return this.mode.is("Custom");
   }

   public Vec2f getBoostV2() {
      float yaw = mc.player != null ? mc.player.getYaw() : 0.0f;
      float pitch = mc.player != null ? mc.player.getPitch() : 0.0f;
      KillAuraModule aura = KillAuraModule.INSTANCE;
      if (aura != null && aura.isEnable() && aura.getTarget() != null) {
         Vec2f rotations = aura.getTargetRotations();
         if (rotations != null) {
            yaw = rotations.x;
            pitch = rotations.y;
         }
      }

      float normalizedYaw = this.convertValToRange(MathHelper.wrapDegrees(yaw));
      float normalizedPitch = this.convertValToRange(Math.abs(pitch));
      int yawIndex = this.getRangeIndex(normalizedYaw, this.yawSpeeds.length);
      int pitchIndex = this.getRangeIndex(normalizedPitch, this.pitchSpeeds.length);
      float yawSpeed = this.yawSpeeds[yawIndex].getValue().floatValue();
      float pitchSpeed = this.pitchSpeeds[pitchIndex].getValue().floatValue();
      if (pitchSpeed > yawSpeed) {
         yawSpeed = pitchSpeed;
      }

      this.logDebug(yawIndex, yawSpeed, pitchIndex, pitchSpeed);
      return new Vec2f(yawSpeed, pitchSpeed);
   }
   public void logDebug(int yawIndex, float yawSpeed, int pitchIndex, float pitchSpeed) {
      if (this.debug.isState()) {
         long now = System.currentTimeMillis();
         if (now - this.lastDebugMessageAt >= (800L)) {
            this.lastDebugMessageAt = now;
            ObsidianComponent_hatUtil.sendMessage("yaw " + RANGE_LABELS[yawIndex] + ": " + yawSpeed + " | pitch " + RANGE_LABELS[pitchIndex] + ": " + pitchSpeed);
         }
      }
   }
   public int getRangeIndex(float value, int length) {
      return Math.min((int)(value / 5.0f), length - (1));
   }
   public float convertValToRange(float value) {
      float result = Math.abs(value);
      if (result > 90.0f) {
         result = 180.0f - result;
      }

      if (result > 45.0f) {
         result = 90.0f - result;
      }

      return result;
   }

   @Generated
   public SliderSetting[] getYawSpeeds() {
      return this.yawSpeeds;
   }

   @Generated
   public SliderSetting[] getPitchSpeeds() {
      return this.pitchSpeeds;
   }

   @Generated
   public ModeSetting getMode() {
      return this.mode;
   }

   @Generated
   public BooleanSetting getDebug() {
      return this.debug;
   }

   @Generated
   public long getLastDebugMessageAt() {
      return this.lastDebugMessageAt;
   }

   @Generated
   public void setLastDebugMessageAt(long lastDebugMessageAt) {
      this.lastDebugMessageAt = lastDebugMessageAt;
   }

   static {
      String[] var10000 = new String[9];
      var10000[0] = "0 - 5";
      var10000[1] = "5 - 10";
      var10000[2] = "10 - 15";
      var10000[3] = "15 - 20";
      var10000[4] = "20 - 25";
      var10000[5] = "25 - 30";
      var10000[6] = "30 - 35";
      var10000[7] = "35 - 40";
      var10000[8] = "40 - 45";
      RANGE_LABELS = var10000;
   }
   public static ElytraBoostModule INSTANCE = new ElytraBoostModule();

}
