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
























import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.ThreadLocalRandom;
import lombok.Generated;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;

public class ObsidianComponent_227 implements IInstance {
   public static final long MIN_RECORD_INTERVAL = 50L;
   public static final int MAX_FRAMES = 20000;
   public static final String PATTERNS_DIRECTORY = "data_patterns";
   public static final String LEGACY_PATTERNS_DIRECTORY = "neuro_patterns";
   public static final String PRIMARY_EXTENSION = ".data";
   public static final String LEGACY_EXTENSION = ".neuro";
   public static final float SYNC_SCORE_THRESHOLD = 45.0F;
   public static final float MAX_YAW_CORRECTION = 8.0F;
   public static final float MAX_PITCH_CORRECTION = 6.0F;
   private final List<ObsidianComponent_168> recordedPatterns = new CopyOnWriteArrayList<>();
   public boolean isRecording = false;
   private boolean isUsingNeuro = false;
   public boolean showStats = true;
   public String currentPatternName = null;
   public String lastDebugMessage = "Готов!";
   public int recordedThisSession = 0;
   public long lastRecordTime = 0L;
   public float prevRecordYaw = 0.0f;
   public float prevRecordPitch = 0.0f;
   private boolean hasRecordedBefore = false;
   private final List<ObsidianComponent_356> frames = new CopyOnWriteArrayList<>();
   public int playbackIndex = -1;
   public int ticksSinceSync = 0;
   public float smoothedYawDelta = 0.0f;
   public float smoothedPitchDelta = 0.0f;
   public float smoothedOutputYaw = Float.NaN;
   public float smoothedOutputPitch = Float.NaN;
   public float yawSpeedFactor = 1.0f;
   public float pitchSpeedFactor = 1.0f;
   public int speedProfileTicks = 0;
   public Vec3d currentAimPoint = null;
   public Vec3d targetRandomPoint = null;
   public int aimPointTicks = 0;
   public LivingEntity lastAimTarget = null;
   public boolean lastWasIdle = true;
   public int attackCount = 0;
   public float randomXOffset = 0.0f;
   public float randomYRatio = 0.6600000262260437f;
   public float randomZOffset = 0.0f;

   public ObsidianComponent_227() {
      this.createPatternsDirectory();
   }
   public void createPatternsDirectory() {
      try {
         Path path = Paths.get("data_patterns");
         if (!Files.exists(path)) {
            Files.createDirectories(path);
         }
      } catch (IOException var2) {
         this.lastDebugMessage = "§cОшибка папки";
      }
   }

   public void recordTick(LivingEntity target, float currentYaw, float currentPitch) {
      if (this.isRecording && mc.player != null) {
         long now = System.currentTimeMillis();
         if (now - this.lastRecordTime >= (50L)) {
            float deltaYaw = 0.0f;
            float deltaPitch = 0.0f;
            if (this.hasRecordedBefore) {
               deltaYaw = MathHelper.wrapDegrees(currentYaw - this.prevRecordYaw);
               deltaPitch = currentPitch - this.prevRecordPitch;
            }

            float angleYaw = 0.0f;
            float anglePitch = 0.0f;
            double distance = 0.0;
            boolean hasTarget = (target != null);
            if (hasTarget) {
               ObsidianComponent_411 aimData = this.getAimData(target, currentYaw, currentPitch, null, true);
               angleYaw = aimData.angleYaw;
               anglePitch = aimData.anglePitch;
               distance = aimData.distance;
            }

            ObsidianComponent_356 frame = new ObsidianComponent_356();
            frame.deltaYaw = deltaYaw;
            frame.deltaPitch = deltaPitch;
            frame.angleYaw = angleYaw;
            frame.anglePitch = anglePitch;
            frame.distance = distance;
            frame.hasTarget = hasTarget;
            frame.smoothness = this.calculateSmoothness(deltaYaw, deltaPitch);
            this.frames.add(frame);

            while (this.frames.size() > (20000)) {
               this.frames.remove(0);
            }

            if (hasTarget) {
               boolean crit = (mc.player.fallDistance > 0.0f && !mc.player.isOnGround());
               String type = target instanceof PlayerEntity ? "player" : "mob";
               this.recordedPatterns
                  .add(
                     new ObsidianComponent_168(
                        angleYaw,
                        anglePitch,
                        deltaYaw,
                        deltaPitch,
                        distance,
                        crit,
                        0.0,
                        type,
                        frame.smoothness
                     )
                  );

               while (this.recordedPatterns.size() > (20000)) {
                  this.recordedPatterns.remove(0);
               }
            }

            this.prevRecordYaw = currentYaw;
            this.prevRecordPitch = currentPitch;
            this.hasRecordedBefore = true;
            this.lastRecordTime = now;
            this.recordedThisSession += 1;
            if (this.recordedThisSession % (20) == 0) {
               this.lastDebugMessage = "§aЗапись: §f" + this.frames.size();
            }
         }
      }
   }

   public ObsidianComponent_olorSetting getNeuroRotation(LivingEntity target, float currentYaw, float currentPitch, boolean idle) {
      if (this.isUsingNeuro && target != null && mc.player != null && !this.frames.isEmpty()) {
         if (!idle && this.lastWasIdle) {
            this.rollNewRandomPoint();
            this.attackCount += 1;
         }

         this.lastWasIdle = idle;
         boolean needSync = (!(this.playbackIndex >= 0 && this.playbackIndex < this.frames.size()));
         ObsidianComponent_411 aimData = this.getAimData(target, currentYaw, currentPitch, null, idle);
         boolean airborne = (!(mc.player.isOnGround()
               && mc.player.getVelocity().y == 0.0));
         if (Math.abs(aimData.angleYaw) > 110.0f) {
            needSync = true;
            this.smoothedYawDelta = 0.0f;
            this.smoothedPitchDelta = 0.0f;
            this.smoothedOutputYaw = currentYaw;
            this.smoothedOutputPitch = currentPitch;
         }

         if (!needSync && this.ticksSinceSync >= 5) {
            ObsidianComponent_356 currentFrame = this.frames.get(this.playbackIndex);
            float yawDiff = Math.abs(MathHelper.wrapDegrees(currentFrame.angleYaw - aimData.angleYaw));
            float pitchDiff = Math.abs(currentFrame.anglePitch - aimData.anglePitch);
            float distDiff = (float)Math.abs(currentFrame.distance - aimData.distance);
            if (yawDiff + pitchDiff + distDiff * 0.30000001192092896f
               > 45.0f) {
               needSync = true;
            }
         }

         if (needSync) {
            this.playbackIndex = this.findBest(aimData.angleYaw, aimData.anglePitch, aimData.distance);
            this.ticksSinceSync = 0;
         }

         ObsidianComponent_356 frame = this.frames.get(this.playbackIndex);
         aimData = this.getAimData(target, currentYaw, currentPitch, frame, idle);
         float applyYaw = frame.deltaYaw;
         float applyPitch = frame.deltaPitch;
         this.updateSpeedProfile(idle, airborne, aimData);
         if (Math.abs(frame.angleYaw) > 3.0f
            && Math.abs(aimData.angleYaw) > 3.0f
            && Math.signum(frame.angleYaw) != Math.signum(aimData.angleYaw)) {
            applyYaw = -applyYaw;
         }

         if (Math.abs(frame.anglePitch) > 3.0f
            && Math.abs(aimData.anglePitch) > 3.0f
            && Math.signum(frame.anglePitch) != Math.signum(aimData.anglePitch)) {
            applyPitch = -applyPitch;
         }

         applyYaw = this.adaptRecordedDelta(applyYaw, aimData.angleYaw, frame.smoothness, idle, 8.0f);
         applyPitch = this.adaptRecordedDelta(
            applyPitch, aimData.anglePitch, frame.smoothness, idle, 6.0f
         );
         if (Math.abs(aimData.angleYaw) < 32.0f) {
            applyYaw = MathHelper.lerp(0.5799999833106995f, applyYaw, aimData.angleYaw);
         }

         if (Math.abs(aimData.anglePitch) < 24.0f) {
            applyPitch = MathHelper.lerp(0.5199999809265137f, applyPitch, aimData.anglePitch);
         }

         this.smoothedYawDelta = this.smoothDelta(this.smoothedYawDelta, applyYaw, frame.smoothness);
         this.smoothedPitchDelta = this.smoothDelta(this.smoothedPitchDelta, applyPitch, frame.smoothness);
         float quantizedYaw = this.quantizeToMouseStep(this.smoothedYawDelta, aimData.angleYaw);
         float quantizedPitch = this.quantizeToMouseStep(this.smoothedPitchDelta, aimData.anglePitch);
         quantizedYaw += this.getMicroJitter(true, idle, airborne, aimData);
         quantizedPitch += this.getMicroJitter(false, idle, airborne, aimData);
         float rawYaw = MathHelper.wrapDegrees(currentYaw + quantizedYaw);
         float rawPitch = MathHelper.clamp(
            currentPitch + quantizedPitch, -90.0f, 90.0f
         );
         float finalYaw = this.smoothOutputRotation(rawYaw, currentYaw, frame.smoothness, idle, true);
         float finalPitch = this.smoothOutputRotation(rawPitch, currentPitch, frame.smoothness, idle, false);
         this.playbackIndex += 1;
         this.ticksSinceSync += 1;

         for (int skipped = 0;
            this.playbackIndex < this.frames.size() && !this.frames.get(this.playbackIndex).hasTarget && skipped < 5;
            skipped++
         ) {
            this.playbackIndex += 1;
         }

         if (this.playbackIndex >= this.frames.size()) {
            float newAngleYaw = MathHelper.wrapDegrees(aimData.perfectYaw - finalYaw);
            float newAnglePitch = aimData.perfectPitch - finalPitch;
            this.playbackIndex = this.findBest(newAngleYaw, newAnglePitch, aimData.distance);
            this.ticksSinceSync = 0;
         }

         Object[] var10002 = new Object[4];
         var10002[0] = this.playbackIndex;
         var10002[1] = this.frames.size();
         var10002[2] = quantizedYaw;
         var10002[3] = quantizedPitch;
         this.lastDebugMessage = String.format("§a[%d/%d] dY%.2f dP%.2f", var10002);
         return new ObsidianComponent_olorSetting(finalYaw, finalPitch);
      } else {
         this.resetState();
         return null;
      }
   }
   public void rollNewRandomPoint() {
      ThreadLocalRandom r = ThreadLocalRandom.current();
      this.randomXOffset = r.nextFloat(-0.3799999952316284f, 0.3799999952316284f);
      this.randomYRatio = r.nextFloat(
         0.4000000059604645f, 0.8500000238418579f
      );
      this.randomZOffset = r.nextFloat(
         -0.3799999952316284f, 0.3799999952316284f
      );
   }
   public ObsidianComponent_411 getAimData(LivingEntity target, float currentYaw, float currentPitch, ObsidianComponent_356 frame, boolean relaxed) {
      Vec3d eyePos = mc.player.getEyePos();
      Vec3d point = this.selectAimPoint(target, relaxed);
      double distance = eyePos.distanceTo(point);
      double dx = point.x - eyePos.x;
      double dy = point.y - eyePos.y;
      double dz = point.z - eyePos.z;
      double distXZ = Math.sqrt(dx * dx + dz * dz);
      float perfectYaw = (float)Math.toDegrees(Math.atan2(-dx, dz));
      float perfectPitch = (float)Math.toDegrees(Math.atan2(-dy, distXZ));
      ObsidianComponent_411 aimData = new ObsidianComponent_411();
      aimData.targetPoint = point;
      aimData.distance = distance;
      aimData.perfectYaw = perfectYaw;
      aimData.perfectPitch = perfectPitch;
      aimData.angleYaw = MathHelper.wrapDegrees(perfectYaw - currentYaw);
      aimData.anglePitch = perfectPitch - currentPitch;
      return aimData;
   }
   public float adaptRecordedDelta(float recordedDelta, float currentAngle, float smoothness, boolean idle, float maxCorrection) {
      float correctionWeight = idle
         ? 0.14000000059604645f
         : 0.04500000178813934f;
      float correctionLimit = idle
         ? maxCorrection * 0.6499999761581421f
         : maxCorrection * 0.30000001192092896f;
      float correction = MathHelper.clamp(currentAngle - recordedDelta, -correctionLimit, correctionLimit);
      float result = recordedDelta + correction * correctionWeight;
      if (Math.abs(currentAngle) < Math.abs(result) && Math.signum(currentAngle) == Math.signum(result)) {
         result = currentAngle;
      }

      float preserveFactor = idle
         ? MathHelper.clamp(
            1.0f - smoothness * 0.2199999988079071f,
            0.800000011920929f,
            0.9700000286102295f
         )
         : MathHelper.clamp(
            1.0f - smoothness * 0.10000000149011612f,
            0.9100000262260437f,
            0.9900000095367432f
         );
      result *= preserveFactor;
      return Math.abs(currentAngle) <= ObsidianComponent_390.getGCDValue() ? currentAngle : result;
   }
   public Vec3d selectAimPoint(LivingEntity target, boolean relaxed) {
      if (target != this.lastAimTarget) {
         this.lastAimTarget = target;
         this.currentAimPoint = null;
         this.targetRandomPoint = null;
         this.aimPointTicks = 0;
         this.rollNewRandomPoint();
      }

      Box box = target.getBoundingBox();
      Vec3d eyePos = mc.player.getEyePos();
      Vec3d stablePoint = new Vec3d(
         box.getCenter().x,
         box.minY + box.getLengthY() * 0.72,
         box.getCenter().z
      );
      if (!box.expand(0.12).contains(eyePos)
         && !(eyePos.squaredDistanceTo(stablePoint) <= 2.25)) {
         double xCenter = (box.minX + box.maxX) * 0.5;
         double zCenter = (box.minZ + box.maxZ) * 0.5;
         double halfW = box.getLengthX() * 0.5;
         double halfD = box.getLengthZ() * 0.5;
         double height = box.getLengthY();
         Vec3d desired = new Vec3d(
            xCenter + halfW * (double)this.randomXOffset, box.minY + height * (double)this.randomYRatio, zCenter + halfD * (double)this.randomZOffset
         );
         if (this.targetRandomPoint == null) {
            this.targetRandomPoint = desired;
         } else {
            float driftLerp = relaxed ? 0.12999999523162842f : 0.07000000029802322f;
            this.targetRandomPoint = new Vec3d(
               MathHelper.lerp((double)driftLerp, this.targetRandomPoint.x, desired.x),
               MathHelper.lerp((double)driftLerp, this.targetRandomPoint.y, desired.y),
               MathHelper.lerp((double)driftLerp, this.targetRandomPoint.z, desired.z)
            );
         }

         if (this.currentAimPoint == null) {
            this.currentAimPoint = this.targetRandomPoint;
            this.aimPointTicks = 0;
            return this.currentAimPoint;
         } else {
            float pointLerp = relaxed
               ? 0.10999999940395355f
               : 0.054999999701976776f;
            this.currentAimPoint = new Vec3d(
               MathHelper.lerp((double)pointLerp, this.currentAimPoint.x, this.targetRandomPoint.x),
               MathHelper.lerp((double)pointLerp, this.currentAimPoint.y, this.targetRandomPoint.y),
               MathHelper.lerp((double)pointLerp, this.currentAimPoint.z, this.targetRandomPoint.z)
            );
            this.aimPointTicks += 1;
            return this.currentAimPoint;
         }
      } else {
         this.currentAimPoint = stablePoint;
         this.targetRandomPoint = stablePoint;
         this.aimPointTicks = 0;
         return stablePoint;
      }
   }
   public float smoothDelta(float current, float target, float smoothness) {
      float lerpFactor = MathHelper.clamp(
         0.03500000014901161f
            + (1.0f - smoothness) * 0.11999999731779099f,
         0.03500000014901161f,
         0.15000000596046448f
      );
      return current + (target - current) * lerpFactor;
   }
   public float smoothOutputRotation(float targetRotation, float currentRotation, float smoothness, boolean idle, boolean yawAxis) {
      float previous = yawAxis ? this.smoothedOutputYaw : this.smoothedOutputPitch;
      if (Float.isNaN(previous)) {
         previous = currentRotation;
      }

      float delta = yawAxis ? MathHelper.wrapDegrees(targetRotation - previous) : targetRotation - previous;
      float maxStep = yawAxis
         ? (idle ? 1.6799999475479126f : 0.8600000143051147f)
         : (idle ? 1.2599999904632568f : 0.62F);
      float lerpFactor = idle
         ? MathHelper.clamp(
            0.07999999821186066f
               + (1.0f - smoothness)
                  * 0.09000000357627869f,
            0.07999999821186066f,
            0.17000000178813934f
         )
         : MathHelper.clamp(
            0.03999999910593033f
               + (1.0f - smoothness) * 0.054999999701976776f,
            0.03999999910593033f,
            0.0949999988079071f
         );
      maxStep *= yawAxis ? this.yawSpeedFactor : this.pitchSpeedFactor;
      lerpFactor *= yawAxis ? this.yawSpeedFactor : this.pitchSpeedFactor;
      float smoothed = previous + MathHelper.clamp(delta * lerpFactor, -maxStep, maxStep);
      if (yawAxis) {
         smoothed = MathHelper.wrapDegrees(smoothed);
         this.smoothedOutputYaw = smoothed;
      } else {
         smoothed = MathHelper.clamp(smoothed, -90.0f, 90.0f);
         this.smoothedOutputPitch = smoothed;
      }

      return smoothed;
   }
   public void updateSpeedProfile(boolean idle, boolean airborne, ObsidianComponent_411 aimData) {
      if (this.speedProfileTicks > 0) {
         this.speedProfileTicks -= 1;
      } else {
         ThreadLocalRandom random = ThreadLocalRandom.current();
         float anglePressure = MathHelper.clamp(
            (Math.abs(aimData.angleYaw) + Math.abs(aimData.anglePitch)) / 35.0f,
            0.0F,
            1.0f
         );
         float baseYawMin = idle ? 1.059999942779541f : 0.9599999785423279f;
         float baseYawMax = idle ? 1.340000033378601f : 1.1200000047683716f;
         float basePitchMin = idle ? 1.0f : 0.8999999761581421f;
         float basePitchMax = idle ? 1.2400000095367432f : 1.0499999523162842f;
         this.yawSpeedFactor = random.nextFloat(
            baseYawMin,
            baseYawMax
               + anglePressure * (idle ? 0.10000000149011612f : 0.1599999964237213f)
         );
         this.pitchSpeedFactor = random.nextFloat(
            basePitchMin,
            basePitchMax
               + anglePressure
                  * (idle ? 0.07999999821186066f : 0.11999999731779099f)
         );
         if (!idle && anglePressure > 0.5799999833106995f) {
            this.yawSpeedFactor = Math.max(
               this.yawSpeedFactor,
               1.0800000429153442f + anglePressure * 0.23999999463558197f
            );
            this.pitchSpeedFactor = Math.max(
               this.pitchSpeedFactor,
               1.0f + anglePressure * 0.18000000715255737f
            );
         }

         if (airborne) {
            this.yawSpeedFactor = this.yawSpeedFactor * 0.9700000286102295f;
            this.pitchSpeedFactor = this.pitchSpeedFactor * 0.949999988079071f;
         }

         this.speedProfileTicks = random.nextInt(
            idle ? 3 : 2,
            idle ? 7 : 5
         );
      }
   }
   public float getMicroJitter(boolean yawAxis, boolean idle, boolean airborne, ObsidianComponent_411 aimData) {
      float gcd = ObsidianComponent_390.getGCDValue();
      if (gcd <= 0.0f) {
         return 0.0f;
      } else {
         float pressure = Math.abs(yawAxis ? aimData.angleYaw : aimData.anglePitch);
         if (!idle
            && pressure > (yawAxis ? 10.0f : 7.0f)) {
            return 0.0f;
         } else {
            float amplitude = yawAxis
               ? gcd * 0.017999999225139618f
               : gcd * 0.012000000104308128f;
            if (airborne) {
               amplitude *= 0.3499999940395355f;
            }

            float wave = (float)Math.sin(
               (double)(
                  (
                        (float)mc.player.age
                           + (
                              yawAxis
                                 ? 0.0f
                                 : 7.0f
                           )
                     )
                     * (idle ? 0.41999998688697815f : 0.2800000011920929f)
               )
            );
            return wave * amplitude;
         }
      }
   }
   public float quantizeToMouseStep(float delta, float remainingAngle) {
      float gcd = ObsidianComponent_390.getGCDValue();
      if (gcd <= 0.0f) {
         return delta;
      } else {
         float limited = delta;
         if (Math.abs(remainingAngle) < Math.abs(delta) && Math.signum(remainingAngle) == Math.signum(delta)) {
            limited = remainingAngle;
         }

         float quantized = (float)Math.round(limited / gcd) * gcd;
         if (quantized == 0.0f
            && Math.abs(remainingAngle) >= gcd * 0.3499999940395355f
            && Math.abs(limited) > 0.0010000000474974513f) {
            quantized = Math.signum(limited) * gcd;
         }

         if (Math.abs(remainingAngle) < Math.abs(quantized) && Math.signum(remainingAngle) == Math.signum(quantized)) {
            quantized = remainingAngle;
         }

         return quantized;
      }
   }
   public float calculateSmoothness(float deltaYaw, float deltaPitch) {
      float magnitude = Math.abs(deltaYaw) + Math.abs(deltaPitch);
      float base = 1.0f
         - magnitude / 18.0f;
      float periodic = (float)Math.sin(
            (double)(
               ((float)this.recordedThisSession + (float)mc.player.age * 0.3100000023841858f)
                  * 0.3400000035762787f
            )
         )
         * 0.012000000104308128f;
      float noise = ThreadLocalRandom.current()
         .nextFloat(-0.00800000037997961f, 0.00800000037997961f);
      return MathHelper.clamp(
         base + periodic + noise,
         0.2199999988079071f,
         0.8799999952316284f
      );
   }
   public int findBest(float angleYaw, float anglePitch, double distance) {
      int best = 0;
      float bestScore = 340282346638528859811704183484516925440.0f;

      for (int i = 0; i < this.frames.size(); i++) {
         ObsidianComponent_356 frame = this.frames.get(i);
         if (frame.hasTarget) {
            float yawDiff = Math.abs(MathHelper.wrapDegrees(frame.angleYaw - angleYaw));
            float pitchDiff = Math.abs(frame.anglePitch - anglePitch);
            float distanceDiff = (float)Math.abs(frame.distance - distance);
            float score = yawDiff + pitchDiff + distanceDiff * 0.30000001192092896f;
            if (score < bestScore) {
               bestScore = score;
               best = i;
            }
         }
      }

      return best;
   }

   public boolean savePatterns(String profileName) {
      if (this.frames.isEmpty()) {
         this.lastDebugMessage = "§cНет записей";
         return false;
      } else {
         try {
            int var4;
            try (ObjectOutputStream out = new ObjectOutputStream(new FileOutputStream("data_patterns/" + profileName + ".data"))) {
               ObsidianComponent_216 data = new ObsidianComponent_216();
               data.patterns = new ArrayList<>(this.recordedPatterns);
               data.frames = new ArrayList<>(this.frames);
               out.writeObject(data);
               this.currentPatternName = profileName;
               this.lastDebugMessage = "§aСохранено " + this.frames.size();
               var4 = 1;
            }

            return (var4 != 0);
         } catch (IOException var7) {
            this.lastDebugMessage = "§cОшибка сохранения";
            return false;
         }
      }
   }

   public boolean loadPatterns(String profileName) {
      File file = this.resolveProfileFile(profileName);
      if (!file.exists()) {
         this.lastDebugMessage = "§eНе найдено: " + profileName;
         return false;
      } else {
         try {
            int var10;
            try (ObjectInputStream in = new ObjectInputStream(new FileInputStream(file))) {
               Object obj = in.readObject();
               this.recordedPatterns.clear();
               this.frames.clear();
               if (obj instanceof ObsidianComponent_216 data) {
                  if (data.patterns != null) {
                     this.recordedPatterns.addAll(data.patterns);
                  }

                  if (data.frames != null) {
                     this.frames.addAll(data.frames);
                  }
               } else if (obj instanceof List<?> list) {
                  this.recordedPatterns.addAll((Collection<? extends ObsidianComponent_168>)list);
               }

               if (this.frames.isEmpty() && !this.recordedPatterns.isEmpty()) {
                  this.rebuildFramesFromPatterns();
               }

               this.currentPatternName = profileName;
               this.resetState();
               this.lastDebugMessage = "§aЗагружено " + this.frames.size();
               var10 = !this.frames.isEmpty() ? 1 : 0;
            }

            return (var10 != 0);
         } catch (ClassNotFoundException | IOException var9) {
            this.lastDebugMessage = "§cОшибка загрузки";
            return false;
         }
      }
   }
   public void rebuildFramesFromPatterns() {
      for (ObsidianComponent_168 pattern : this.recordedPatterns) {
         ObsidianComponent_356 frame = new ObsidianComponent_356();
         frame.deltaYaw = pattern.getDeltaYaw();
         frame.deltaPitch = pattern.getDeltaPitch();
         frame.angleYaw = pattern.getYaw();
         frame.anglePitch = pattern.getPitch();
         frame.distance = pattern.getDistance();
         frame.hasTarget = true;
         frame.smoothness = MathHelper.clamp(
            pattern.getSmoothness(), 0.18000000715255737f, 0.8999999761581421f
         );
         this.frames.add(frame);
      }
   }

   public boolean deletePatterns(String profileName) {
      File primaryFile = new File("data_patterns/" + profileName + ".data");
      File legacyFile = new File("neuro_patterns/" + profileName + ".neuro");
      boolean deleted = false;
      if (primaryFile.exists()) {
         deleted = primaryFile.delete();
      }

      if (legacyFile.exists()) {
         deleted = (!(!legacyFile.delete() && !deleted));
      }

      if (deleted) {
         if (profileName.equals(this.currentPatternName)) {
            this.currentPatternName = null;
         }

         this.lastDebugMessage = "§aУдалено";
         return true;
      } else {
         return false;
      }
   }

   public int getPatternCount() {
      return this.recordedPatterns.size();
   }

   public int getFrameCount() {
      return this.frames.size();
   }

   public void startRecording() {
      this.recordedPatterns.clear();
      this.frames.clear();
      this.isRecording = true;
      this.isUsingNeuro = false;
      this.recordedThisSession = 0;
      this.lastRecordTime = 0L;
      this.currentPatternName = null;
      this.hasRecordedBefore = false;
      this.prevRecordYaw = 0.0f;
      this.prevRecordPitch = 0.0f;
      this.resetState();
      this.lastDebugMessage = "§aЗапись";
   }

   public void stopRecording() {
      this.isRecording = false;
      this.lastDebugMessage = "§eСтоп: " + this.frames.size();
   }

   public void clearPatterns() {
      this.recordedPatterns.clear();
      this.frames.clear();
      this.isRecording = false;
      this.isUsingNeuro = false;
      this.recordedThisSession = 0;
      this.currentPatternName = null;
      this.hasRecordedBefore = false;
      this.prevRecordYaw = 0.0f;
      this.prevRecordPitch = 0.0f;
      this.resetState();
      this.lastDebugMessage = "§eОчищено";
   }

   public void resetState() {
      this.playbackIndex = -1;
      this.ticksSinceSync = 0;
      this.smoothedYawDelta = 0.0f;
      this.smoothedPitchDelta = 0.0f;
      this.smoothedOutputYaw = Float.NaN;
      this.smoothedOutputPitch = Float.NaN;
      this.yawSpeedFactor = 1.0f;
      this.pitchSpeedFactor = 1.0f;
      this.speedProfileTicks = 0;
      this.currentAimPoint = null;
      this.targetRandomPoint = null;
      this.aimPointTicks = 0;
      this.lastAimTarget = null;
      this.lastWasIdle = true;
      this.attackCount = 0;
      this.rollNewRandomPoint();
   }

   public String getStatusString() {
      String status = "§8[§bData§8] §f" + this.frames.size();
      if (this.isRecording) {
         status = status + " §a[REC]";
      }

      if (this.isUsingNeuro) {
         status = status + " §b[ON " + this.playbackIndex + "]";
      }

      return status;
   }

   public List<String> getPatternNames() {
      List<String> names = new ArrayList<>();
      this.collectPatternNames(names, new File("data_patterns"), ".data");
      this.collectPatternNames(names, new File("neuro_patterns"), ".neuro");
      return names;
   }
   public void collectPatternNames(List<String> names, File directory, String extension) {
      if (directory.exists() && directory.isDirectory()) {
         File[] files = directory.listFiles((dir, namex) -> namex.endsWith(extension));
         if (files != null) {
            File[] var5 = files;
            int var6 = files.length;

            for (int var7 = 0; var7 < var6; var7++) {
               File file = var5[var7];
               String name = file.getName().replace(extension, "");
               if (!names.contains(name)) {
                  names.add(name);
               }
            }
         }
      }
   }
   public File resolveProfileFile(String profileName) {
      File primaryFile = new File("data_patterns/" + profileName + ".data");
      return primaryFile.exists() ? primaryFile : new File("neuro_patterns/" + profileName + ".neuro");
   }

   @Generated
   public List<ObsidianComponent_168> getRecordedPatterns() {
      return this.recordedPatterns;
   }

   @Generated
   public boolean isRecording() {
      return this.isRecording;
   }

   @Generated
   public void setRecording(boolean isRecording) {
      this.isRecording = isRecording;
   }

   @Generated
   public boolean isUsingNeuro() {
      return this.isUsingNeuro;
   }

   @Generated
   public void setUsingNeuro(boolean isUsingNeuro) {
      this.isUsingNeuro = isUsingNeuro;
   }

   @Generated
   public boolean isShowStats() {
      return this.showStats;
   }

   @Generated
   public void setShowStats(boolean showStats) {
      this.showStats = showStats;
   }

   @Generated
   public String getCurrentPatternName() {
      return this.currentPatternName;
   }

   @Generated
   public void setCurrentPatternName(String currentPatternName) {
      this.currentPatternName = currentPatternName;
   }

   @Generated
   public String getLastDebugMessage() {
      return this.lastDebugMessage;
   }

   @Generated
   public int getRecordedThisSession() {
      return this.recordedThisSession;
   }
}
