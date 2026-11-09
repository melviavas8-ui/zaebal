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
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.item.AxeItem;
import net.minecraft.item.Items;
import net.minecraft.item.ShovelItem;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;


public final class ObsidianComponent_243 implements IInstance {
   public static final int WATER_CRIT_INTENT_TICKS = 8;
   public static final int WATER_CRIT_CONTACT_TICKS = 10;
   public static final double WATER_CRIT_MIN_UPWARD_VELOCITY = 0.05;
   public static int lastWaterContactAge = -2147483648;
   public static int lastWaterCritIntentAge = -2147483648;

   public static float getAICooldown() {
      if (mc.player.getMainHandStack().getItem() == Items.AIR) {
         return 0.8999999761581421f;
      } else {
         return !(mc.player.getMainHandStack().getItem() instanceof AxeItem) && !(mc.player.getMainHandStack().getItem() instanceof ShovelItem)
            ? 0.9300000071525574f
            : 0.949999988079071f;
      }
   }

   public static boolean canAIFall() {
      BlockPos posWater = BlockPos.ofFloored(
         mc.player
            .getPos()
            .add(
               0.0,
               Double.longBitsToDouble(-4622494657425702912L),
               0.0
            )
      );
      if (mc.world.getBlockState(posWater).isOf(Blocks.WATER)) {
         return true;
      } else {
         return (!((
                  getBlock(
                           0.0,
                           3.0,
                           0.0
                        )
                        != Blocks.AIR
                     || getBlock(
                           0.0,
                           2.0,
                           0.0
                        )
                        != Blocks.AIR
                     || getBlock(
                           0.0,
                           1.0,
                           0.0
                        )
                        != Blocks.AIR
               )
               && !(
                  mc.player.fallDistance
                     < (
                        getBlock(
                                 0.0,
                                 2.0,
                                 0.0
                              )
                              != Blocks.AIR
                           ? 0.07999999821186066f
                           : 0.6000000238418579f
                     )
               )
               && !(mc.player.fallDistance > 1.2000000476837158f)));
      }
   }

   public static boolean canCritical(LivingEntity target) {
      updateWaterCritState();
      boolean packetCrits = ModuleManager.packetCriticals.isEnable();
      boolean hasSlowFalling = mc.player.hasStatusEffect(StatusEffects.SLOW_FALLING);
      boolean inCobweb = isInCobweb();
      boolean smartCrit = ModuleManager.aura.smartCrit.isState();
      if (packetCrits && inCobweb) {
         return true;
      } else if (packetCrits && hasSlowFalling) {
         return (mc.player.getVelocity().y < 0.0
               && mc.player.fallDistance > 0.0f);
      } else if (isTryingWaterCrit()) {
         return isWaterCritWindow();
      } else {
         boolean isCritPossible = (!mc.player.isOnGround()
               && mc.player.getVelocity().y < 0.0
               && mc.player.fallDistance > 0.0f);
         if (isNoJumpDelayCeilingCritIntent()) {
            return isNoJumpDelayCeilingCritWindow();
         } else if (isNoJumpDelayJumpCritIntent()) {
            return isNoJumpDelayJumpCritWindow();
         } else if (cannotPerformCrit()) {
            return true;
         } else if (!smartCrit) {
            return isCritPossible;
         } else {
            return (!(!mc.player.isOnGround() && !isCritPossible));
         }
      }
   }
   public static boolean isNoJumpDelayCeilingCritIntent() {
      return (ModuleManager.noJumpDelay.isEnable() && mc.options != null && mc.options.jumpKey.isPressed() && hasLowCeilingForJumpCrit());
   }
   public static boolean isNoJumpDelayJumpCritIntent() {
      return (ModuleManager.noJumpDelay.isEnable() && mc.options != null && mc.options.jumpKey.isPressed());
   }
   public static boolean isNoJumpDelayCeilingCritWindow() {
      return (mc.player != null
            && !mc.player.isOnGround()
            && mc.player.getVelocity().y <= 0.01
            && !mc.player.isTouchingWater()
            && !mc.player.isSubmergedInWater()
            && !mc.player.isInLava()
            && !mc.player.isClimbing()
            && !mc.player.hasVehicle()
            && !mc.player.getAbilities().flying);
   }

   public static boolean isNoJumpDelayJumpCritWindow() {
      return (mc.player != null
            && mc.world != null
            && ModuleManager.noJumpDelay.isEnable()
            && mc.options != null
            && mc.options.jumpKey.isPressed()
            && !mc.player.isOnGround()
            && mc.player.getVelocity().y < 0.0
            && !mc.player.isTouchingWater()
            && !mc.player.isSubmergedInWater()
            && !mc.player.isInLava()
            && !mc.player.isClimbing()
            && !mc.player.hasVehicle()
            && !mc.player.getAbilities().flying
            && !mc.player.hasStatusEffect(StatusEffects.LEVITATION)
            && !mc.player.hasStatusEffect(StatusEffects.SLOW_FALLING)
            && !mc.player.hasStatusEffect(StatusEffects.BLINDNESS)
            && !mc.player.isGliding()
            && !isInCobweb());
   }
   public static boolean hasLowCeilingForJumpCrit() {
      if (mc.player != null && mc.world != null) {
         Box box = mc.player.getBoundingBox().contract(0.03);
         Box headBox = new Box(
            box.minX,
            box.maxY,
            box.minZ,
            box.maxX,
            box.maxY + 0.32,
            box.maxZ
         );

         for (BlockPos pos : BlockPos.iterate(
            MathHelper.floor(headBox.minX),
            MathHelper.floor(headBox.minY),
            MathHelper.floor(headBox.minZ),
            MathHelper.floor(headBox.maxX),
            MathHelper.floor(headBox.maxY),
            MathHelper.floor(headBox.maxZ)
         )) {
            BlockState state = mc.world.getBlockState(pos);
            if (!state.isAir() && !state.getCollisionShape(mc.world, pos).isEmpty()) {
               return true;
            }
         }

         return false;
      } else {
         return false;
      }
   }

   public static boolean canPacketCrit() {
      return (!(!isInCobweb() && !mc.player.hasStatusEffect(StatusEffects.SLOW_FALLING)));
   }
   public static void updateWaterCritState() {
      if (mc.player != null && mc.world != null) {
         boolean nearWaterSurface = isNearWaterSurface();
         if (nearWaterSurface) {
            lastWaterContactAge = mc.player.age;
            if (isWaterCritIntentState()) {
               lastWaterCritIntentAge = mc.player.age;
            }
         }
      } else {
         lastWaterContactAge = -2147483648;
         lastWaterCritIntentAge = -2147483648;
      }
   }
   public static boolean isWaterCritIntentState() {
      if (mc.player != null && mc.options != null) {
         return (mc.options.jumpKey.isPressed()
               && !mc.player.isOnGround()
               && !mc.player.isSubmergedInWater()
               && mc.player.getVelocity().y > 0.05);
      } else {
         return false;
      }
   }
   public static boolean isTryingWaterCrit() {
      if (mc.player != null && mc.options != null && mc.options.jumpKey.isPressed()) {
         return (mc.player.age - lastWaterCritIntentAge <= (8)
               && mc.player.age - lastWaterContactAge <= (10));
      } else {
         return false;
      }
   }
   public static boolean isWaterCritWindow() {
      return (mc.player != null
            && !mc.player.isOnGround()
            && !mc.player.isTouchingWater()
            && !mc.player.isSubmergedInWater()
            && mc.player.fallDistance > 0.0f
            && mc.player.getVelocity().y < 0.0);
   }
   public static boolean isNearWaterSurface() {
      if (mc.player != null && mc.world != null) {
         BlockPos below = BlockPos.ofFloored(
            mc.player
               .getPos()
               .add(
                  0.0,
                  Double.longBitsToDouble(-4622494657425702912L),
                  0.0
               )
         );
         return (!(!mc.player.isTouchingWater() && !mc.player.isSubmergedInWater() && !mc.world.getBlockState(below).isOf(Blocks.WATER)));
      } else {
         return false;
      }
   }
   public static boolean cannotPerformCrit() {
      double effectiveJumpHeight = (double)mc.player.getStepHeight();
      Vec3d jumpVec = new Vec3d(
         0.0,
         effectiveJumpHeight,
         0.0
      );
      Vec3d allowedMovement = ((IEntity)mc.player).invokeAdjustMovementForCollisions(jumpVec);
      boolean cobweb = isInCobweb();
      BlockPos posWater = BlockPos.ofFloored(
         mc.player
            .getPos()
            .add(
               0.0,
               (double)(mc.player.getHeight() / 2.0f),
               0.0
            )
      );
      return (!(!mc.player.isInLava()
            && !mc.player.isClimbing()
            && !mc.world.getBlockState(posWater).isOf(Blocks.WATER)
            && !mc.player.hasStatusEffect(StatusEffects.LEVITATION)
            && !mc.player.hasStatusEffect(StatusEffects.SLOW_FALLING)
            && !mc.player.hasStatusEffect(StatusEffects.BLINDNESS)
            && !cobweb
            && !mc.player.isGliding()
            && !mc.player.hasVehicle()
            && !mc.player.getAbilities().flying
            && !mc.player.isTouchingWater()
            && (
               !(
                     allowedMovement.y
                        < (double)mc.player.getStepHeight() - 0.5
                  )
                  || !mc.player.isOnGround()
            )));
   }

   public static boolean isInCobweb() {
      Box box = mc.player.getBoundingBox();

      for (BlockPos pos : BlockPos.iterate(
         MathHelper.floor(box.minX),
         MathHelper.floor(box.minY),
         MathHelper.floor(box.minZ),
         MathHelper.floor(box.maxX),
         MathHelper.floor(box.maxY),
         MathHelper.floor(box.maxZ)
      )) {
         if (mc.world.getBlockState(pos).isOf(Blocks.COBWEB)) {
            return true;
         }
      }

      return false;
   }

   public static Block getBlock(double x, double y, double z) {
      return mc.world.getBlockState(mc.player.getBlockPos().add((int)x, (int)y, (int)z)).getBlock();
   }

   public static boolean findFall(float fallDistance) {
      Vec3d rotationVec = mc.player.getRotationVector();
      double tempVelocityX = mc.player.getVelocity().x;
      double tempVelocityY = mc.player.getVelocity().y;
      double tempVelocityZ = mc.player.getVelocity().z;
      float n = MathHelper.cos(mc.player.getPitch() * 0.01745329238474369f);
      n = (float)(
         (double)(n * n)
            * Math.min(
               rotationVec.length() / 0.4,
               1.0
            )
      );
      Vec3d vec3d = new Vec3d(tempVelocityX, tempVelocityY, tempVelocityZ)
         .add(
            0.0,
            0.08
               * (
                  Double.longBitsToDouble(-4616189618054758400L)
                     + (double)n * 0.75
               ),
            0.0
         );
      tempVelocityY = vec3d.y * 0.9800000190734863;
      return (tempVelocityY < (double)fallDistance);
   }

   @Generated
   public ObsidianComponent_243() {
      throw new UnsupportedOperationException("This is a utility class and cannot be instantiated");
   }
}
