package wtf.obsidian.utils;

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

























import java.lang.reflect.Method;
import net.minecraft.client.MinecraftClient;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;

public final class BaritoneHelper {
   public static final String PROTECTED_BLOCK_MESSAGE = "Извините, но вы не можете сломать блок здесь";
   public static final long STUCK_TIMEOUT_MS = 7000L;
   public static final double PROGRESS_DISTANCE_SQ = 1.0;
   public static final int RECOVERY_TICKS = 12;
   public static final double PRIVATE_ESCAPE_DISTANCE_SQ = 2500.0;
   public static final long PRIVATE_ESCAPE_TIMEOUT_MS = 25000L;
   public static final double SIDE_OFFSET = 0.95;
   public static final double FORWARD_OFFSET = 0.35;
   public static final String BARITONE_API_CLASS = "baritone.api.BaritoneAPI";
   public static final String INPUT_ENUM_CLASS = "baritone.api.utils.input.Input";
   public static Vec3d anchorPos;
   public static long lastProgressAtMs;
   public static int recoveryTicksRemaining;
   public static boolean strafeRightNext;
   public static boolean privateEscapePending;
   public static boolean privateEscapeActive;
   public static boolean privateEscapeRight;
   public static Vec3d privateEscapeStartPos;
   public static long privateEscapeStartedAtMs;

   public BaritoneHelper() {
   }

   public static void onGameMessage(String message) {
      if (message != null && message.contains("Извините, но вы не можете сломать блок здесь")) {
         privateEscapePending = true;
      }
   }

   public static void tick() {
      MinecraftClient mc = MinecraftClient.getInstance();
      if (mc.player != null && mc.world != null) {
         try {
            Object baritone = getPrimaryBaritone();
            if (baritone == null) {
               resetState();
               return;
            }

            Object pathing = invoke(baritone, "getPathingBehavior");
            Object input = invoke(baritone, "getInputOverrideHandler");
            if (pathing == null || input == null || !Boolean.TRUE.equals(invoke(pathing, "isPathing"))) {
               clearRecovery(input);
               resetTracking();
               return;
            }

            long now = System.currentTimeMillis();
            Vec3d currentPos = mc.player.getPos();
            if (anchorPos == null) {
               anchorPos = currentPos;
               lastProgressAtMs = now;
            }

            if (privateEscapePending && isMiningNow(mc, input)) {
               startPrivateEscape(mc, currentPos);
               privateEscapePending = false;
            }

            if (privateEscapeActive) {
               if (!(
                     currentPos.squaredDistanceTo(privateEscapeStartPos)
                        >= 2500.0
                  )
                  && now - privateEscapeStartedAtMs < (25000L)) {
                  applyPrivateEscapeInput(mc, input);
                  anchorPos = currentPos;
                  lastProgressAtMs = now;
                  return;
               }

               clearAllKeys(input);
               privateEscapeActive = false;
               anchorPos = currentPos;
               lastProgressAtMs = now;
               return;
            }

            if (recoveryTicksRemaining > 0) {
               applyRecoveryInput(mc, input);
               recoveryTicksRemaining--;
               if (recoveryTicksRemaining <= 0) {
                  clearAllKeys(input);
                  anchorPos = mc.player.getPos();
                  lastProgressAtMs = now;
               }

               return;
            }

            if (isMiningNow(mc, input)) {
               anchorPos = currentPos;
               lastProgressAtMs = now;
               return;
            }

            if (!isTryingToMove(input)) {
               anchorPos = currentPos;
               lastProgressAtMs = now;
               return;
            }

            if (currentPos.squaredDistanceTo(anchorPos) >= 1.0) {
               anchorPos = currentPos;
               lastProgressAtMs = now;
               return;
            }

            if (now - lastProgressAtMs < (7000L)) {
               return;
            }

            recoveryTicksRemaining = 12;
            strafeRightNext = chooseRecoverySide(mc, strafeRightNext, true);
            applyRecoveryInput(mc, input);
            anchorPos = currentPos;
            lastProgressAtMs = now;
         } catch (Throwable var7) {
            resetState();
         }
      } else {
         resetState();
      }
   }
   public static Object getPrimaryBaritone() throws ReflectiveOperationException {
      Class<?> apiClass = Class.forName("baritone.api.BaritoneAPI");
      Object provider = apiClass.getMethod("getProvider").invoke(null);
      return provider == null ? null : provider.getClass().getMethod("getPrimaryBaritone").invoke(provider);
   }
   public static boolean isMiningNow(MinecraftClient mc, Object input) throws ReflectiveOperationException {
      return (!((mc.interactionManager == null || !mc.interactionManager.isBreakingBlock()) && !isInputForcedDown(input, "CLICK_LEFT")));
   }
   public static boolean isTryingToMove(Object input) throws ReflectiveOperationException {
      return (!(!isInputForcedDown(input, "MOVE_FORWARD")
            && !isInputForcedDown(input, "MOVE_BACK")
            && !isInputForcedDown(input, "MOVE_LEFT")
            && !isInputForcedDown(input, "MOVE_RIGHT")
            && !isInputForcedDown(input, "JUMP")));
   }
   public static void startPrivateEscape(MinecraftClient mc, Vec3d currentPos) {
      privateEscapeActive = true;
      privateEscapeStartPos = currentPos;
      privateEscapeStartedAtMs = System.currentTimeMillis();
      privateEscapeRight = chooseRecoverySide(mc, privateEscapeRight, false);
   }
   public static void applyRecoveryInput(MinecraftClient mc, Object input) throws ReflectiveOperationException {
      clearAllKeys(input);
      setInputForceState(input, "MOVE_FORWARD", true);
      setInputForceState(input, strafeRightNext ? "MOVE_RIGHT" : "MOVE_LEFT", true);
      if (mc.player != null && mc.player.isOnGround()) {
         setInputForceState(input, "JUMP", true);
      }
   }
   public static void applyPrivateEscapeInput(MinecraftClient mc, Object input) throws ReflectiveOperationException {
      clearAllKeys(input);
      setInputForceState(input, "MOVE_BACK", true);
      setInputForceState(input, privateEscapeRight ? "MOVE_RIGHT" : "MOVE_LEFT", true);
      if (mc.player != null && mc.player.isOnGround()) {
         setInputForceState(input, "JUMP", true);
      }
   }
   public static boolean chooseRecoverySide(MinecraftClient mc, boolean fallbackRight, boolean moveForward) {
      if (mc.player == null) {
         return fallbackRight;
      } else {
         double yawRad = Math.toRadians((double)mc.player.getYaw());
         Vec3d forwardDirection = new Vec3d(
            (double)(-MathHelper.sin((float)yawRad)),
            0.0,
            (double)MathHelper.cos((float)yawRad)
         );
         Vec3d left = new Vec3d(
            forwardDirection.z, 0.0, -forwardDirection.x
         );
         Vec3d right = left.multiply(Double.longBitsToDouble(-4616189618054758400L));
         Vec3d direction = moveForward
            ? forwardDirection
            : forwardDirection.multiply(Double.longBitsToDouble(-4616189618054758400L));
         double leftScore = freeSpaceScore(
            mc,
            left.multiply(0.95)
               .add(direction.multiply(0.35))
         );
         double rightScore = freeSpaceScore(
            mc,
            right.multiply(0.95)
               .add(direction.multiply(0.35))
         );
         if (leftScore == rightScore) {
            return fallbackRight;
         } else {
            return (rightScore > leftScore);
         }
      }
   }
   public static double freeSpaceScore(MinecraftClient mc, Vec3d offset) {
      Box shifted = mc.player.getBoundingBox().offset(offset);
      double score = 0.0;
      if (mc.world.isSpaceEmpty(mc.player, shifted)) {
         score += 1.0;
      }

      if (mc.world
         .isSpaceEmpty(
            mc.player,
            shifted.offset(
               0.0,
               1.0,
               0.0
            )
         )) {
         score += 0.35;
      }

      return score;
   }
   public static void clearRecovery(Object input) {
      if (recoveryTicksRemaining > 0 && input != null) {
         try {
            clearAllKeys(input);
         } catch (ReflectiveOperationException var3) {
         }
      }

      recoveryTicksRemaining = 0;
      if (privateEscapeActive && input != null) {
         try {
            clearAllKeys(input);
         } catch (ReflectiveOperationException var2) {
         }
      }

      privateEscapeActive = false;
      privateEscapePending = false;
   }
   public static void resetTracking() {
      anchorPos = null;
      lastProgressAtMs = 0L;
   }
   public static void resetState() {
      recoveryTicksRemaining = 0;
      anchorPos = null;
      lastProgressAtMs = 0L;
      privateEscapePending = false;
      privateEscapeActive = false;
      privateEscapeStartPos = null;
      privateEscapeStartedAtMs = 0L;
   }
   public static boolean isInputForcedDown(Object inputOverrideHandler, String inputName) throws ReflectiveOperationException {
      Object input = getInputEnum(inputName);
      Class var10000 = inputOverrideHandler.getClass();
      Class[] var10002 = new Class[1];
      var10002[0] = input.getClass();
      Method var4 = var10000.getMethod("isInputForcedDown", var10002);
      Object[] var5 = new Object[1];
      var5[0] = input;
      Object result = var4.invoke(inputOverrideHandler, var5);
      return Boolean.TRUE.equals(result);
   }
   public static void setInputForceState(Object inputOverrideHandler, String inputName, boolean forced) throws ReflectiveOperationException {
      Object input = getInputEnum(inputName);
      Class var10000 = inputOverrideHandler.getClass();
      Class[] var10002 = new Class[2];
      var10002[0] = input.getClass();
      var10002[1] = boolean.class;
      Method var4 = var10000.getMethod("setInputForceState", var10002);
      Object[] var5 = new Object[2];
      var5[0] = input;
      var5[1] = forced;
      var4.invoke(inputOverrideHandler, var5);
   }
   public static void clearAllKeys(Object inputOverrideHandler) throws ReflectiveOperationException {
      inputOverrideHandler.getClass().getMethod("clearAllKeys").invoke(inputOverrideHandler);
   }
   public static Object getInputEnum(String inputName) throws ReflectiveOperationException {
      Class<?> inputEnum = Class.forName("baritone.api.utils.input.Input");
      return Enum.valueOf(inputEnum.asSubclass(Enum.class), inputName);
   }
   public static Object invoke(Object target, String methodName) throws ReflectiveOperationException {
      return target.getClass().getMethod(methodName).invoke(target);
   }
}
