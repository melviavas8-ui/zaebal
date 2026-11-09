package wtf.obsidian.modules.player;

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

























import java.util.Comparator;
import net.minecraft.block.Blocks;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.projectile.thrown.EnderPearlEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.network.packet.c2s.play.PlayerMoveC2SPacket.LookAndOnGround;
import net.minecraft.util.hit.HitResult.Type;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec2f;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.RaycastContext;
import net.minecraft.world.RaycastContext.FluidHandling;
import net.minecraft.world.RaycastContext.ShapeType;

public class TargetPearlModule extends Module {
   public static final double MAX_TRACK_DISTANCE = 256.0;
   public static final double MIN_LANDING_DISTANCE = 11.0;
   public static final long LOCAL_THROW_COOLDOWN_MS = 2500L;
   public static final float DIRECT_MIN_PITCH = -25.0F;
   public static final float DIRECT_MAX_PITCH = 35.0F;
   public static final float PITCH_STEP = 0.25F;
   public static final TargetPearlModule INSTANCE = new TargetPearlModule();
   public final ModeSetting mode;
   public final ObsidianComponent_261 bind;
   public final BooleanSetting onlyTarget;
   public final BooleanSetting ignoreFriends;
   public final ObsidianComponent_372 timer;
   public EnderPearlEntity targetPearl;
   public int lastHandledPearlId;
   public long nextThrowAt;
   public boolean isThrowing;
   public Vec2f serverRotation;

   public TargetPearlModule() {
      super("TargetPearl", "Автоматически бросает жемчуг в цель", Category.PLAYER);
      String[] var10005 = new String[2];
      var10005[0] = "По бинду";
      var10005[1] = "Автоматический";
      this.mode = new ModeSetting("Тип", "Автоматический", var10005);
      this.bind = new ObsidianComponent_261("Бинд", -1).visible(() -> this.mode.is("По бинду"));
      this.onlyTarget = new BooleanSetting("Только за противником", false);
      this.ignoreFriends = new BooleanSetting("Игнорировать друзей", true);
      this.timer = new ObsidianComponent_372();
      this.lastHandledPearlId = -1;
      Setting[] var10001 = new Setting[4];
      var10001[0] = this.mode;
      var10001[1] = this.bind;
      var10001[2] = this.onlyTarget;
      var10001[3] = this.ignoreFriends;
      this.addSettings(var10001);
   }

   @EventTarget
   public void onBinding(InputEvent event) {
      if (mc.player != null && mc.world != null && mc.currentScreen == null) {
         if (this.mode.is("По бинду") && event.getKey() == this.bind.getKey()) {
            if (this.canThrowNow()) {
               this.aimAndThrowPearl();
            }
         }
      }
   }

   @EventTarget
   public void onUpdate(EventTick event) {
      if (mc.player != null && mc.world != null) {
         if (this.lastHandledPearlId != -1
            && (!(mc.world.getEntityById(this.lastHandledPearlId) instanceof EnderPearlEntity pearl) || !pearl.isAlive())) {
            this.lastHandledPearlId = -1;
         }

         if (this.mode.is("Автоматический") && this.canThrowNow()) {
            this.aimAndThrowPearl();
         }
      } else {
         this.resetThrowState();
      }
   }

   @EventTarget
   public void onMoveInput(Event_102 event) {
      if (this.isEnable() && this.isThrowing && this.serverRotation != null) {
         float forward = event.getForward();
         float strafe = event.getStrafe();
         if (forward != 0.0f || strafe != 0.0f) {
            double targetAngle = MathHelper.wrapDegrees(Math.toDegrees(direction(this.serverRotation.x, forward, strafe)));
            float bestForward = 0.0f;
            float bestStrafe = 0.0f;
            float smallestDifference = 340282346638528859811704183484516925440.0f;

            for (float testForward = -1.0f;
               testForward <= 1.0f;
               testForward += 1.0f
            ) {
               for (float testStrafe = -1.0f;
                  testStrafe <= 1.0f;
                  testStrafe += 1.0f
               ) {
                  if (testForward != 0.0f
                     || testStrafe != 0.0f) {
                     double testAngle = MathHelper.wrapDegrees(Math.toDegrees(direction(this.serverRotation.x, testForward, testStrafe)));
                     float difference = Math.abs(MathHelper.wrapDegrees((float)(targetAngle - testAngle)));
                     if (difference < smallestDifference) {
                        smallestDifference = difference;
                        bestForward = testForward;
                        bestStrafe = testStrafe;
                     }
                  }
               }
            }

            event.setForward(bestForward);
            event.setStrafe(bestStrafe);
         }
      }
   }

   @Override
   public void onDisable() {
      this.resetThrowState();
      this.lastHandledPearlId = -1;
      this.nextThrowAt = 0L;
      this.timer.reset();
      super.onDisable();
   }
   public boolean canThrowNow() {
      if (System.currentTimeMillis() < this.nextThrowAt) {
         return false;
      } else {
         return (!mc.player.getItemCooldownManager().isCoolingDown(new ItemStack(Items.ENDER_PEARL))
               && this.timer.finished(1000L));
      }
   }
   public void aimAndThrowPearl() {
      Vec3d landingPosition = this.getTargetPearlLandingPosition();
      if (landingPosition == null) {
         this.resetThrowState();
      } else {
         float[] rotations = this.calculateYawPitch(landingPosition);
         if (rotations != null && !Float.isNaN(rotations[0]) && !Float.isNaN(rotations[1])) {
            Vec3d trajectoryLanding = this.checkTrajectory(rotations[0], rotations[1]);
            double allowedError = Math.max(
               3.0,
               mc.player.getPos().distanceTo(landingPosition) * 0.12
            );
            if (trajectoryLanding == null || landingPosition.distanceTo(trajectoryLanding) > allowedError) {
               this.resetThrowState();
            } else if (!this.hasPearl()) {
               this.resetThrowState();
            } else {
               float previousYaw = mc.player.getYaw();
               float previousPitch = mc.player.getPitch();
               this.isThrowing = true;
               this.serverRotation = new Vec2f(rotations[0], rotations[1]);

               try {
                  mc.player.setYaw(rotations[0]);
                  mc.player.setPitch(rotations[1]);
                  mc.player
                     .networkHandler
                     .sendPacket(
                        new LookAndOnGround(
                           rotations[0],
                           rotations[1],
                           mc.player.isOnGround(),
                           mc.player.horizontalCollision
                        )
                     );
                  ObsidianComponent_225.swapAndUseHvH(Items.ENDER_PEARL);
                  this.timer.reset();
                  this.nextThrowAt = System.currentTimeMillis() + (2500L);
                  if (this.targetPearl != null) {
                     this.lastHandledPearlId = this.targetPearl.getId();
                  }
               } finally {
                  mc.player.setYaw(previousYaw);
                  mc.player.setPitch(previousPitch);
                  this.resetThrowState();
               }
            }
         } else {
            this.resetThrowState();
         }
      }
   }
   public Vec3d getTargetPearlLandingPosition() {
      this.targetPearl = this.getTargetPearl();
      if (this.targetPearl != null && this.targetPearl.isAlive()) {
         Vec3d landingPos = this.predictPearlLanding(this.targetPearl);
         return landingPos != null && this.isWithinRange(landingPos) ? landingPos : null;
      } else {
         return null;
      }
   }
   public EnderPearlEntity getTargetPearl() {
      Box searchBox = mc.player.getBoundingBox().expand(256.0);
      LivingEntity auraTarget = ModuleManager.INSTANCE != null ? ModuleManager.aura.getTarget() : null;
      return mc.world
         .getOtherEntities(
            mc.player,
            searchBox,
            entity -> {
               if (entity instanceof EnderPearlEntity pearl
                  && pearl.isAlive()
                  && pearl.getOwner() != mc.player
                  && pearl.getId() != this.lastHandledPearlId
                  && !this.isIgnoredFriend(pearl.getOwner())
                  && (!this.onlyTarget.isState() || auraTarget != null && pearl.getOwner() == auraTarget)) {
                  return true;
               }

               return false;
            }
         )
         .stream()
         .map(entity -> (EnderPearlEntity)entity)
         .filter(
            pearl -> (this.getHorizontalDistanceTo(pearl)
                     <= 256.0)
         )
         .min(Comparator.comparingDouble(this::getHorizontalDistanceTo))
         .orElse(null);
   }
   public boolean isIgnoredFriend(Entity owner) {
      if (this.ignoreFriends.isState() && owner instanceof PlayerEntity player) {
         return (Obsidian.INSTANCE != null
               && Obsidian.INSTANCE.friendStorage != null
               && Obsidian.INSTANCE.friendStorage.isFriend(player.getName().getString()));
      } else {
         return false;
      }
   }
   public double getHorizontalDistanceTo(EnderPearlEntity pearl) {
      Vec3d playerPos = mc.player.getPos();
      Vec3d pearlPos = pearl.getPos();
      double dx = pearlPos.x - playerPos.x;
      double dz = pearlPos.z - playerPos.z;
      return Math.sqrt(dx * dx + dz * dz);
   }
   public Vec3d predictPearlLanding(EnderPearlEntity pearl) {
      Vec3d position = pearl.getPos();
      Vec3d velocity = pearl.getVelocity();
      Vec3d lastPosition = position;

      for (int i = 0; i < 200; i++) {
         lastPosition = position;
         position = position.add(velocity);
         if (this.hitsBlock(lastPosition, position) || position.y <= (double)mc.world.getBottomY()) {
            return new Vec3d(
               (double)MathHelper.floor(lastPosition.x) + 0.5,
               (double)MathHelper.floor(lastPosition.y),
               (double)MathHelper.floor(lastPosition.z) + 0.5
            );
         }

         velocity = this.updatePearlMotion(velocity, position);
      }

      return new Vec3d(
         (double)MathHelper.floor(lastPosition.x) + 0.5,
         (double)MathHelper.floor(lastPosition.y),
         (double)MathHelper.floor(lastPosition.z) + 0.5
      );
   }
   public Vec3d updatePearlMotion(Vec3d motion, Vec3d position) {
      BlockPos blockPos = BlockPos.ofFloored(position);
      return mc.world.getBlockState(blockPos).isOf(Blocks.WATER)
         ? motion.multiply(0.8)
            .add(
               0.0,
               Double.longBitsToDouble(-4639067904161800520L),
               0.0
            )
         : motion.multiply(0.99)
            .add(
               0.0,
               Double.longBitsToDouble(-4639067904161800520L),
               0.0
            );
   }
   public boolean isWithinRange(Vec3d landingPos) {
      double distanceToLanding = mc.player.getPos().distanceTo(landingPos);
      return (distanceToLanding >= 11.0
            && distanceToLanding <= 256.0);
   }
   public float[] calculateYawPitch(Vec3d targetPosition) {
      Vec3d playerPosition = mc.player.getPos();
      double dx = targetPosition.x - playerPosition.x;
      double dy = targetPosition.y - mc.player.getEyeY();
      double dz = targetPosition.z - playerPosition.z;
      float yaw = (float)Math.toDegrees(Math.atan2(dz, dx)) - 90.0f;
      double horizontalDistance = Math.sqrt(dx * dx + dz * dz);
      double allowedError = Math.max(
         1.5, mc.player.getPos().distanceTo(targetPosition) * 0.08
      );
      ObsidianComponent_258 directCandidate = this.findBestCandidate(
         targetPosition,
         yaw,
         -25.0f,
         35.0f,
         allowedError,
         true
      );
      if (directCandidate != null) {
         float[] var19 = new float[2];
         var19[0] = yaw;
         var19[1] = MathHelper.clamp(
            directCandidate.pitch,
            -90.0f,
            90.0f
         );
         return var19;
      } else {
         ObsidianComponent_258 fallbackCandidate = this.findBestCandidate(
            targetPosition,
            yaw,
            -85.0f,
            85.0f,
            allowedError,
            false
         );
         if (fallbackCandidate == null) {
            double fallbackPitch = -Math.toDegrees(Math.atan2(dy, horizontalDistance))
               + 5.0;
            float[] var18 = new float[2];
            var18[0] = yaw;
            var18[1] = MathHelper.clamp(
               (float)fallbackPitch,
               -90.0f,
               90.0f
            );
            return var18;
         } else {
            float[] var10000 = new float[2];
            var10000[0] = yaw;
            var10000[1] = MathHelper.clamp(
               fallbackCandidate.pitch,
               -90.0f,
               90.0f
            );
            return var10000;
         }
      }
   }
   public ObsidianComponent_258 findBestCandidate(Vec3d targetPosition, float yaw, float minPitch, float maxPitch, double allowedError, boolean preferDirect) {
      Vec3d playerPosition = mc.player.getPos();
      double velocity = 1.5;
      ObsidianComponent_258 bestCandidate = null;

      for (float pitch = minPitch; pitch <= maxPitch; pitch += 0.25f) {
         float pitchRad = (float)Math.toRadians((double)pitch);
         double vx = (double)(-MathHelper.sin((float)Math.toRadians((double)yaw)) * MathHelper.cos(pitchRad)) * velocity;
         double vy = (double)(-MathHelper.sin(pitchRad)) * velocity;
         double vz = (double)(MathHelper.cos((float)Math.toRadians((double)yaw)) * MathHelper.cos(pitchRad)) * velocity;
         Vec3d pos = new Vec3d(playerPosition.x, mc.player.getEyeY(), playerPosition.z);
         Vec3d motion = new Vec3d(vx, vy, vz);
         int ticks = 0;

         for (int i = 0; i < 200; i++) {
            Vec3d previous = pos;
            pos = pos.add(motion);
            motion = this.updatePearlMotion(motion, pos);
            ticks++;
            if (this.hitsEntity(previous, pos)) {
               break;
            }

            if (this.hitsBlock(previous, pos) || !(pos.y > (double)mc.world.getBottomY())) {
               double distanceToTarget = pos.distanceTo(targetPosition);
               ObsidianComponent_258 candidate = new ObsidianComponent_258(pitch, distanceToTarget, ticks, pos);
               if (this.isBetterCandidate(candidate, bestCandidate, allowedError, preferDirect)) {
                  bestCandidate = candidate;
               }
               break;
            }
         }
      }

      return bestCandidate != null && !(bestCandidate.distanceToTarget > allowedError) ? bestCandidate : null;
   }
   public boolean isBetterCandidate(ObsidianComponent_258 candidate, ObsidianComponent_258 currentBest, double allowedError, boolean preferDirect) {
      if (currentBest == null) {
         return true;
      } else {
         boolean candidateAccurate = (candidate.distanceToTarget <= allowedError);
         boolean bestAccurate = (currentBest.distanceToTarget <= allowedError);
         if (candidateAccurate != bestAccurate) {
            return candidateAccurate;
         } else {
            if (preferDirect && candidateAccurate && bestAccurate) {
               float candidatePitchAbs = Math.abs(candidate.pitch);
               float bestPitchAbs = Math.abs(currentBest.pitch);
               if (Math.abs(candidatePitchAbs - bestPitchAbs) > 0.009999999776482582f) {
                  return (candidatePitchAbs < bestPitchAbs);
               }

               if (candidate.ticks != currentBest.ticks) {
                  return (candidate.ticks < currentBest.ticks);
               }
            }

            if (Math.abs(candidate.distanceToTarget - currentBest.distanceToTarget)
               > 0.01) {
               return (candidate.distanceToTarget < currentBest.distanceToTarget);
            } else if (candidate.ticks != currentBest.ticks) {
               return (candidate.ticks < currentBest.ticks);
            } else if (!preferDirect) {
               return (Math.abs(candidate.pitch) < Math.abs(currentBest.pitch));
            } else {
               return false;
            }
         }
      }
   }
   public Vec3d checkTrajectory(float yaw, float pitch) {
      float yawRad = (float)Math.toRadians((double)yaw);
      float pitchRad = (float)Math.toRadians((double)pitch);
      double velocity = 1.5;
      double x = mc.player.getX() - (double)(MathHelper.cos(yawRad) * 0.1599999964237213f);
      double y = mc.player.getY()
         + (double)mc.player.getEyeHeight(mc.player.getPose())
         - 0.1;
      double z = mc.player.getZ() - (double)(MathHelper.sin(yawRad) * 0.1599999964237213f);
      double motionX = (double)(-MathHelper.sin(yawRad) * MathHelper.cos(pitchRad)) * velocity;
      double motionY = (double)(-MathHelper.sin(pitchRad)) * velocity;
      double motionZ = (double)(MathHelper.cos(yawRad) * MathHelper.cos(pitchRad)) * velocity;
      Vec3d position = new Vec3d(x, y, z);
      Vec3d motion = new Vec3d(motionX, motionY, motionZ);

      for (int i = 0; i <= 200; i++) {
         Vec3d previous = position;
         position = position.add(motion);
         motion = this.updatePearlMotion(motion, position);
         if (this.hitsEntity(previous, position)) {
            return null;
         }

         if (this.hitsBlock(previous, position) || position.y <= (double)mc.world.getBottomY()) {
            return new Vec3d(
               (double)MathHelper.floor(position.x) + 0.5,
               (double)MathHelper.floor(position.y),
               (double)MathHelper.floor(position.z) + 0.5
            );
         }
      }

      return null;
   }
   public boolean hitsBlock(Vec3d from, Vec3d to) {
      return (mc.world.raycast(new RaycastContext(from, to, ShapeType.COLLIDER, FluidHandling.NONE, mc.player)).getType() == Type.BLOCK);
   }
   public boolean hitsEntity(Vec3d from, Vec3d to) {
      Box searchBox = new Box(from, to).expand(0.3);

      for (Entity entity : mc.world.getOtherEntities(mc.player, searchBox, entityx -> {
         if (!entityx.isAlive() || entityx.isSpectator() || entityx.noClip) {
            return false;
         } else if (entityx == this.targetPearl) {
            return false;
         } else {
            return (!(entityx instanceof EnderPearlEntity));
         }
      })) {
         if (entity.getBoundingBox()
            .expand(0.25)
            .raycast(from, to)
            .isPresent()) {
            return true;
         }
      }

      return false;
   }
   public boolean hasPearl() {
      return (!(!mc.player.getMainHandStack().isOf(Items.ENDER_PEARL)
            && !mc.player.getOffHandStack().isOf(Items.ENDER_PEARL)
            && ObsidianComponent_225.find(Items.ENDER_PEARL, 0, 8) == (-1)
            && ObsidianComponent_225.find(Items.ENDER_PEARL, 9, 45) == (-1)));
   }
   public void resetThrowState() {
      this.isThrowing = false;
      this.targetPearl = null;
      this.serverRotation = null;
   }
   public static double direction(float rotationYaw, float moveForward, float moveStrafing) {
      if (moveForward < 0.0f) {
         rotationYaw += 180.0f;
      }

      float forward = 1.0f;
      if (moveForward < 0.0f) {
         forward = -0.5f;
      } else if (moveForward > 0.0f) {
         forward = 0.5f;
      }

      if (moveStrafing > 0.0f) {
         rotationYaw -= 90.0f * forward;
      }

      if (moveStrafing < 0.0f) {
         rotationYaw += 90.0f * forward;
      }

      return Math.toRadians((double)rotationYaw);
   }
}
