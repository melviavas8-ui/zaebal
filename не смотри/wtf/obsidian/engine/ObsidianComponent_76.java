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
























import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import java.io.IOException;
import java.io.Reader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;
import net.minecraft.entity.LivingEntity;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec2f;
import net.minecraft.util.math.Vec3d;

public class ObsidianComponent_76 extends CombatPredictionHelper implements IInstance {
   public static final Path DATASET_PATH;
   private final List<ObsidianComponent_104> frames = new ArrayList<>();
   public LivingEntity trackedTarget;
   public LivingEntity trackedRotationTarget;
   public Vec3d currentAimPoint;
   public Vec3d targetAimPoint;
   public long lastModified = Long.MIN_VALUE;
   public long lastLoadAttempt;
   public boolean datasetReady;
   public int playbackIndex;
   public int aimPointTicks;
   public int aimPointRefreshTicks;
   public int smoothProfileTicks;
   public float smoothYawStep;
   public float smoothPitchStep;
   public float smoothYaw;
   public float smoothPitch;
   public float yawSmoothFactor = 1.0f;
   public float pitchSmoothFactor = 1.0f;
   public boolean hasRotationState;

   public void reset() {
      this.trackedTarget = null;
      this.trackedRotationTarget = null;
      this.currentAimPoint = null;
      this.targetAimPoint = null;
      this.playbackIndex = 0;
      this.aimPointTicks = 0;
      this.aimPointRefreshTicks = 0;
      this.smoothProfileTicks = 0;
      this.smoothYawStep = 0.0f;
      this.smoothPitchStep = 0.0f;
      this.smoothYaw = 0.0f;
      this.smoothPitch = 0.0f;
      this.yawSmoothFactor = 1.0f;
      this.pitchSmoothFactor = 1.0f;
      this.hasRotationState = false;
   }

   @Override
   public void updateRotations(LivingEntity target) {
      if (mc.player != null && target != null) {
         boolean focus = this.shouldFocus();
         this.ensureDatasetLoaded();
         Vec3d aimPoint = this.selectAimPoint(target, focus);
         Vec2f rot = ObsidianComponent_374.getRotations(aimPoint);
         if (this.datasetReady && !this.frames.isEmpty()) {
            float currentYaw = mc.player.getYaw();
            float currentPitch = mc.player.getPitch();
            this.syncRotationState(target, currentYaw, currentPitch);
            float remainingYaw = MathHelper.wrapDegrees(rot.x - this.smoothYaw);
            float remainingPitch = rot.y - this.smoothPitch;
            ObsidianComponent_104 frame = this.pickFrame(remainingYaw, remainingPitch, focus);
            this.updateSmoothProfile(frame, remainingYaw, remainingPitch, focus);
            float gcd = Math.max(ObsidianComponent_390.getGCDValue(), 9.999999747378752e-05f);
            float yawStep = this.buildAxisStep(remainingYaw, frame, true, focus);
            float pitchStep = this.buildAxisStep(remainingPitch, frame, false, focus);
            yawStep += this.buildJitter(frame, remainingYaw, true, gcd);
            pitchStep += this.buildJitter(frame, remainingPitch, false, gcd);
            this.smoothYawStep = this.smoothAxisStep(this.smoothYawStep, yawStep, remainingYaw, true, focus);
            this.smoothPitchStep = this.smoothAxisStep(this.smoothPitchStep, pitchStep, remainingPitch, false, focus);
            float quantizedYawStep = this.quantizeDelta(this.smoothYawStep, remainingYaw, gcd, true);
            float quantizedPitchStep = this.quantizeDelta(this.smoothPitchStep, remainingPitch, gcd, false);
            this.smoothYaw = MathHelper.wrapDegrees(this.smoothYaw + quantizedYawStep);
            this.smoothPitch = MathHelper.clamp(
               this.smoothPitch + quantizedPitchStep,
               -89.0f,
               89.0f
            );
            RotationManager.update(
               new ObsidianComponent_olorSetting(this.smoothYaw, this.smoothPitch),
               360.0f,
               360.0f,
               45.0f,
               45.0f,
               0,
               1,
               false
            );
         } else {
            RotationManager.update(
               new ObsidianComponent_olorSetting(
                  rot.x,
                  MathHelper.clamp(
                     rot.y, -89.0f, 89.0f
                  )
               ),
               360.0f,
               360.0f,
               45.0f,
               45.0f,
               0,
               1,
               false
            );
         }
      }
   }
   public boolean shouldFocus() {
      float cooldown = mc.player.getAttackCooldownProgress(1.5f);
      boolean fallingForCrit = (!mc.player.isOnGround()
            && mc.player.getVelocity().y < 0.0
            && mc.player.fallDistance > 0.0f);
      return (!(!(cooldown >= 0.8799999952316284f) && !fallingForCrit));
   }
   public void ensureDatasetLoaded() {
      long now = System.currentTimeMillis();
      if (this.shouldReload(now)) {
         this.lastLoadAttempt = now;
         long modified = this.readLastModified();
         if (!this.datasetReady || modified != this.lastModified) {
            this.frames.clear();
            this.datasetReady = false;
            if (!Files.exists(DATASET_PATH)) {
               this.lastModified = Long.MIN_VALUE;
            } else {
               try (Reader reader = Files.newBufferedReader(DATASET_PATH)) {
                  for (JsonElement element : JsonParser.parseReader(reader).getAsJsonArray()) {
                     if (element.isJsonObject()) {
                        ObsidianComponent_104 frame = this.parseFrame(element.getAsJsonObject());
                        if (frame != null) {
                           this.frames.add(frame);
                        }
                     }
                  }

                  this.datasetReady = (!this.frames.isEmpty());
                  this.lastModified = modified;
                  this.reset();
               } catch (IllegalStateException | IOException var12) {
                  this.datasetReady = false;
                  this.lastModified = Long.MIN_VALUE;
                  this.reset();
               }
            }
         }
      }
   }
   public boolean shouldReload(long now) {
      if (this.datasetReady && !this.frames.isEmpty()) {
         return (now - this.lastLoadAttempt >= (3000L));
      } else {
         return (now - this.lastLoadAttempt >= (1500L));
      }
   }
   public long readLastModified() {
      try {
         return Files.exists(DATASET_PATH)
            ? Files.getLastModifiedTime(DATASET_PATH).toMillis()
            : Long.MIN_VALUE;
      } catch (IOException var2) {
         return Long.MIN_VALUE;
      }
   }
   public ObsidianComponent_104 parseFrame(JsonObject object) {
      float fromYaw = this.getFloat(object, "fromYaw");
      float toYaw = this.getFloat(object, "toYaw");
      float fromPitch = this.getFloat(object, "fromPitch");
      float toPitch = this.getFloat(object, "toPitch");
      float signedYaw = MathHelper.wrapDegrees(toYaw - fromYaw);
      float signedPitch = toPitch - fromPitch;
      float absYaw = Math.abs(signedYaw);
      float absPitch = Math.abs(signedPitch);
      float deltaYaw = Math.max(this.getFloat(object, "deltaYaw"), absYaw);
      float deltaPitch = Math.max(this.getFloat(object, "deltaPitch"), absPitch);
      if (deltaYaw <= 0.0f && deltaPitch <= 0.0f) {
         return null;
      } else {
         ObsidianComponent_104 frame = new ObsidianComponent_104();
         frame.deltaYaw = deltaYaw;
         frame.deltaPitch = deltaPitch;
         frame.signedYaw = signedYaw != 0.0f
            ? signedYaw
            : Math.signum(this.getFloat(object, "jitterYawDir")) * deltaYaw;
         frame.signedPitch = signedPitch != 0.0f
            ? signedPitch
            : Math.signum(this.getFloat(object, "jitterPitchDir")) * deltaPitch;
         frame.rotationSpeed = Math.max(this.getFloat(object, "rotationSpeed"), 0.0f);
         frame.jitterScore = Math.max(this.getFloat(object, "jitterScore"), 0.0f);
         frame.jitterYawSpeed = Math.max(this.getFloat(object, "jitterYawSpeed"), 0.0f);
         frame.jitterPitchSpeed = Math.max(this.getFloat(object, "jitterPitchSpeed"), 0.0f);
         frame.isJittering = this.getBoolean(object, "isJittering");
         frame.attacking = this.getBoolean(object, "attacking");
         frame.combatFrame = this.getBoolean(object, "isCombatFrame");
         frame.instantSnap = this.getBoolean(object, "isInstantSnap");
         frame.timeDeltaMs = Math.max(
            1L,
            object.has("timeDeltaMs") ? object.get("timeDeltaMs").getAsLong() : 50L
         );
         return frame;
      }
   }
   public ObsidianComponent_104 pickFrame(float remainingYaw, float remainingPitch, boolean focus) {
      float pressure = Math.abs(remainingYaw) + Math.abs(remainingPitch) * 0.8199999928474426f;
      int size = this.frames.size();
      int window = Math.min(size, focus ? 78 : 56);
      int bestIndex = this.playbackIndex % size;
      float bestScore = 340282346638528859811704183484516925440.0f;

      for (int i = 0; i < window; i++) {
         int index = (this.playbackIndex + i) % size;
         ObsidianComponent_104 frame = this.frames.get(index);
         float framePressure = frame.deltaYaw + frame.deltaPitch * 0.8199999928474426f;
         float score = Math.abs(framePressure - pressure);
         if (focus) {
            if (!frame.isCombatLike()) {
               score += 3.0f;
            }

            if (frame.instantSnap) {
               score -= 0.5f;
            }
         } else if (frame.isCombatLike()) {
            score += 1.600000023841858f;
         }

         if (pressure < 10.0F && frame.isJittering) {
            score -= Math.min(frame.jitterScore, 2.5999999046325684f)
               * 0.20000000298023224f;
         }

         score += (float)i * 0.03200000151991844f;
         if (score < bestScore) {
            bestScore = score;
            bestIndex = index;
         }
      }

      this.playbackIndex = (bestIndex + (1)) % size;
      return this.frames.get(bestIndex);
   }
   public void updateSmoothProfile(ObsidianComponent_104 frame, float remainingYaw, float remainingPitch, boolean focus) {
      if (this.smoothProfileTicks > 0) {
         this.smoothProfileTicks -= 1;
      } else {
         ThreadLocalRandom random = ThreadLocalRandom.current();
         float pressure = MathHelper.clamp(
            (Math.abs(remainingYaw) + Math.abs(remainingPitch)) / 32.0f,
            0.0f,
            1.0f
         );
         float timePressure = MathHelper.clamp(
            (float)frame.timeDeltaMs / 120.0f,
            0.0f,
            1.0F
         );
         float yawMin = focus ? 0.9399999976158142f : 0.8600000143051147f;
         float yawMax = focus ? 1.1200000047683716f : 1.0399999618530273f;
         float pitchMin = focus ? 0.9200000166893005f : 0.8399999737739563f;
         float pitchMax = focus ? 1.0800000429153442f : 1.0f;
         this.yawSmoothFactor = random.nextFloat(
            yawMin,
            yawMax
               + pressure * 0.07999999821186066f
               + timePressure * 0.03999999910593033f
         );
         this.pitchSmoothFactor = random.nextFloat(
            pitchMin,
            pitchMax
               + pressure * 0.05999999865889549f
               + timePressure * 0.029999999329447746f
         );
         if (frame.isCombatLike()) {
            this.yawSmoothFactor = this.yawSmoothFactor * 1.0199999809265137f;
            this.pitchSmoothFactor = this.pitchSmoothFactor * 1.0149999856948853f;
         }

         this.smoothProfileTicks = random.nextInt(
            focus ? 2 : 3,
            focus ? 6 : 8
         );
      }
   }
   public float buildAxisStep(float remaining, ObsidianComponent_104 frame, boolean yawAxis, boolean focus) {
      float desiredAbs = Math.abs(remaining);
      if (desiredAbs <= 9.999999747378752e-05f) {
         return 0.0f;
      } else {
         float template = yawAxis ? frame.deltaYaw : frame.deltaPitch;
         float speedBoost = 0.30000001192092896f
            + MathHelper.clamp(
               frame.rotationSpeed * (yawAxis ? 3.4000000953674316f : 2.799999952316284f),
               0.0f,
               yawAxis ? 0.20000000298023224f : 0.1599999964237213f
            );
         float pressureBoost = MathHelper.clamp(
            desiredAbs / (yawAxis ? 105.0f : 82.0f),
            0.09000000357627869f,
            yawAxis ? 0.5199999809265137f : 0.46000000834465027f
         );
         float step = Math.max(
            template * Math.max(speedBoost, pressureBoost),
            yawAxis ? 0.029999999329447746f : 0.024000000208616257f
         );
         if (frame.instantSnap) {
            step = Math.max(
               step,
               desiredAbs * (yawAxis ? 0.08500000089406967f : 0.06499999761581421f)
            );
         }

         if (frame.attacking || frame.combatFrame) {
            step *= yawAxis ? 1.0199999809265137f : 1.0149999856948853f;
         }

         step *= yawAxis ? 0.5f : 0.46000000834465027f;
         float finishThreshold = yawAxis
            ? 6.0f
            : 4.0f;
         if (desiredAbs < finishThreshold) {
            float finishBoost = 1.0F + (finishThreshold - desiredAbs) / finishThreshold * 0.18000000715255737f;
            step *= finishBoost;
         }

         float maxStep = yawAxis
            ? Math.max(
               0.47999998927116394f,
               desiredAbs
                  * (
                     frame.instantSnap
                        ? 0.10999999940395355f
                        : 0.06499999761581421f
                  )
            )
            : Math.max(
               0.3400000035762787f,
               desiredAbs
                  * (frame.instantSnap ? 0.09000000357627869f : 0.057999998331069946f)
            );
         step = Math.min(step, maxStep);
         step = Math.min(step, desiredAbs);
         return Math.signum(remaining) * step;
      }
   }
   public float buildJitter(ObsidianComponent_104 frame, float remaining, boolean yawAxis, float gcd) {
      float desiredAbs = Math.abs(remaining);
      if (desiredAbs > (yawAxis ? 6.5f : 4.800000190734863f)) {
         return 0.0f;
      } else {
         float speed = yawAxis ? frame.jitterYawSpeed : frame.jitterPitchSpeed;
         float base = gcd
            * MathHelper.clamp(
               frame.jitterScore * 0.009999999776482582f,
               0.0f,
               yawAxis ? 0.15000000596046448f : 0.10999999940395355f
            );
         base += gcd
            * MathHelper.clamp(
               speed * (yawAxis ? 1.2999999523162842f : 1.0f),
               0.0f,
               yawAxis ? 0.10000000149011612f : 0.07000000029802322f
            );
         if (frame.isJittering) {
            base *= 1.0499999523162842f;
         }

         if (base <= 0.0f) {
            return 0.0f;
         } else {
            float direction = ThreadLocalRandom.current().nextBoolean()
               ? 1.0f
               : -1.0f;
            float jitter = base
               * ThreadLocalRandom.current()
                  .nextFloat(0.30000001192092896f, 0.949999988079071f)
               * direction;
            if (Math.abs(jitter) > desiredAbs && Math.signum(jitter) == Math.signum(remaining)) {
               jitter = remaining;
            }

            return jitter;
         }
      }
   }
   public void syncRotationState(LivingEntity target, float currentYaw, float currentPitch) {
      if (!this.hasRotationState || this.trackedRotationTarget != target) {
         this.trackedRotationTarget = target;
         this.smoothYaw = currentYaw;
         this.smoothPitch = currentPitch;
         this.smoothYawStep = 0.0f;
         this.smoothPitchStep = 0.0f;
         this.yawSmoothFactor = 1.0f;
         this.pitchSmoothFactor = 1.0f;
         this.smoothProfileTicks = 0;
         this.hasRotationState = true;
      }
   }
   public float smoothAxisStep(float currentStep, float desiredStep, float remaining, boolean yawAxis, boolean focus) {
      float desiredAbs = Math.abs(remaining);
      if (desiredAbs <= 9.999999747378752e-05f) {
         return 0.0f;
      } else {
         float baseAlpha = yawAxis
            ? (focus ? 0.09200000017881393f : 0.05999999865889549f)
            : (focus ? 0.0820000022649765f : 0.054999999701976776f);
         float alpha = baseAlpha * (yawAxis ? this.yawSmoothFactor : this.pitchSmoothFactor);
         float smoothed = currentStep
            + (desiredStep - currentStep)
               * MathHelper.clamp(alpha, 0.02500000037252903f, 0.1599999964237213f);
         float minCap = yawAxis ? 0.12999999523162842f : 0.10000000149011612f;
         float capScale = yawAxis
            ? (focus ? 0.0560000017285347f : 0.035999998450279236f)
            : (focus ? 0.04600000008940697f : 0.03200000151991844f);
         float randomFactor = yawAxis ? this.yawSmoothFactor : this.pitchSmoothFactor;
         float maxCap = minCap
            + desiredAbs
               * capScale
               * MathHelper.clamp(randomFactor, 0.8799999952316284f, 1.1799999475479126f);
         float finishThreshold = yawAxis ? 5.5f : 3.799999952316284f;
         if (desiredAbs < finishThreshold) {
            maxCap *= 1.1200000047683716f;
         }

         smoothed = MathHelper.clamp(smoothed, -maxCap, maxCap);
         if (Math.abs(remaining) < Math.abs(smoothed) && Math.signum(remaining) == Math.signum(smoothed)) {
            smoothed = remaining;
         }

         return smoothed;
      }
   }
   public float quantizeDelta(float wantedDelta, float remaining, float gcd, boolean yawAxis) {
      float limited = wantedDelta;
      if (Math.abs(remaining) < Math.abs(wantedDelta) && Math.signum(remaining) == Math.signum(wantedDelta)) {
         limited = remaining;
      }

      float quantized = (float)Math.round(limited / gcd) * gcd;
      if (quantized == 0.0f
         && Math.abs(limited) >= gcd * 0.20000000298023224f) {
         quantized = Math.signum(limited) * gcd;
      }

      if (Math.abs(remaining) < Math.abs(quantized) && Math.signum(remaining) == Math.signum(quantized)) {
         quantized = remaining;
      }

      if (!yawAxis) {
         quantized = MathHelper.clamp(quantized, -89.0f, 89.0f);
      }

      return quantized;
   }
   public Vec3d selectAimPoint(LivingEntity target, boolean focus) {
      if (this.trackedTarget == target && this.currentAimPoint != null && this.targetAimPoint != null) {
         int var10000 = this.aimPointTicks;
         this.aimPointTicks += 1;
         if (var10000 >= this.aimPointRefreshTicks) {
            this.targetAimPoint = this.createAimPoint(target, focus);
            this.aimPointTicks = 0;
            this.aimPointRefreshTicks = this.randomRefreshTicks(focus);
         }

         float lerp = focus ? 0.05999999865889549f : 0.03999999910593033f;
         this.currentAimPoint = new Vec3d(
            MathHelper.lerp((double)lerp, this.currentAimPoint.x, this.targetAimPoint.x),
            MathHelper.lerp((double)lerp, this.currentAimPoint.y, this.targetAimPoint.y),
            MathHelper.lerp((double)lerp, this.currentAimPoint.z, this.targetAimPoint.z)
         );
         return this.currentAimPoint;
      } else {
         this.trackedTarget = target;
         this.targetAimPoint = this.createAimPoint(target, focus);
         this.currentAimPoint = this.targetAimPoint;
         this.aimPointTicks = 0;
         this.aimPointRefreshTicks = this.randomRefreshTicks(focus);
         return this.currentAimPoint;
      }
   }
   public int randomRefreshTicks(boolean focus) {
      return ThreadLocalRandom.current()
         .nextInt(
            focus ? 7 : 10,
            focus ? 13 : 18
         );
   }
   public Vec3d createAimPoint(LivingEntity target, boolean focus) {
      Box box = this.getPredictedBox(target);
      ThreadLocalRandom random = ThreadLocalRandom.current();
      double x = MathHelper.lerp(
         random.nextDouble(
            0.45,
            0.55
         ),
         box.minX,
         box.maxX
      );
      double y = MathHelper.lerp(
         random.nextDouble(
            focus
               ? 0.53
               : 0.49,
            focus
               ? 0.7
               : 0.76
         ),
         box.minY,
         box.maxY
      );
      double z = MathHelper.lerp(
         random.nextDouble(
            0.45,
            0.55
         ),
         box.minZ,
         box.maxZ
      );
      return new Vec3d(x, y, z);
   }
   public float getFloat(JsonObject object, String key) {
      return object.has(key) ? object.get(key).getAsFloat() : 0.0f;
   }
   public boolean getBoolean(JsonObject object, String key) {
      return (object.has(key) && object.get(key).getAsBoolean());
   }

   static {
      String var10000 = System.getProperty("user.home");
      String[] var10001 = new String[2];
      var10001[0] = "Desktop";
      var10001[1] = "data.json";
      DATASET_PATH = Path.of(var10000, var10001);
   }
}
