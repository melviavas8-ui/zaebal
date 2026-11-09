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
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayerEntity;

public class SprintModule extends Module {
   public static SprintModule INSTANCE = new SprintModule();
   private static final MinecraftClient CLIENT = MinecraftClient.getInstance();
   private final BooleanSetting keepInWater = new BooleanSetting("Сохранять в воде", false);
   public static boolean sprinting;
   public static long time = 0L;
   public static int pauseDepth = 0;
   public static boolean restoreAfterPause = false;
   public ClientPlayerEntity lastPlayer;

   public SprintModule() {
      super("Sprint", "Автоматический бег", Category.MOVEMENT);
      Setting[] var10001 = new Setting[1];
      var10001[0] = this.keepInWater;
      this.addSettings(var10001);
   }

   @Override
   public void onEnable() {
      resetPauseState();
      sprinting = true;
      super.onEnable();
   }

   @Override
   public void onDisable() {
      resetPauseState();
      sprinting = false;
      this.lastPlayer = null;
      if (mc.options != null) {
         mc.options.sprintKey.setPressed(false);
      }

      if (mc.player != null) {
         mc.player.setSprinting(false);
      }

      super.onDisable();
   }

   @EventTarget
   public void onEvent(EventTick ignored) {
      if (mc.player == null) {
         this.lastPlayer = null;
         resetPauseState();
         if (mc.options != null) {
            mc.options.sprintKey.setPressed(false);
         }
      } else {
         if (this.lastPlayer != mc.player) {
            this.lastPlayer = mc.player;
            resetPauseState();
            sprinting = true;
         }

         boolean legacyProtocol = ProtocolUtil.isTargetProtocolBelowOneNineteen();
         boolean inWater = (!(!mc.player.isTouchingWater() && !mc.player.isSubmergedInWater()));
         boolean shouldSprint = (pauseDepth == 0
               && System.currentTimeMillis() >= time
               && sprinting
               && ObsidianComponent_268.isMoving()
               && mc.player.input.movementForward > 0.0f
               && (!legacyProtocol || !mc.player.horizontalCollision && !mc.player.collidedSoftly)
               && !mc.player.isGliding());
         if (this.keepInWater.isState() && inWater && mc.player.isSprinting()) {
            shouldSprint = true;
         }

         mc.options.sprintKey.setPressed(shouldSprint);
         mc.player.setSprinting(shouldSprint);
      }
   }

   public boolean shouldKeepSprintInWater() {
      return (this.isEnable() && this.keepInWater.isState());
   }

   public static void pushPause(long delayMs) {
      restoreAfterPause = restoreAfterPause | shouldRestoreAfterPause();
      pauseDepth += 1;
      time = Math.max(time, System.currentTimeMillis() + Math.max(0L, delayMs));
      sprinting = false;
      if (CLIENT.options != null) {
         CLIENT.options.sprintKey.setPressed(false);
      }

      if (CLIENT.player != null) {
         CLIENT.player.setSprinting(false);
      }
   }

   public static void popPause() {
      if (pauseDepth > 0) {
         pauseDepth -= 1;
      }

      if (pauseDepth <= 0) {
         time = 0L;
         sprinting = restoreAfterPause;
         restoreAfterPause = false;
      }
   }
   public static boolean shouldRestoreAfterPause() {
      if (CLIENT.player != null && CLIENT.player.isSprinting()) {
         return true;
      } else {
         return (ModuleManager.sprint != null && ModuleManager.sprint.isEnable() && sprinting);
      }
   }
   public static void resetPauseState() {
      pauseDepth = 0;
      restoreAfterPause = false;
      time = 0L;
   }

   @Generated
   public static boolean isSprinting() {
      return sprinting;
   }

   @Generated
   public static void setSprinting(boolean sprinting) {
      SprintModule.sprinting = sprinting;
   }

   @Generated
   public static long getTime() {
      return time;
   }

   @Generated
   public static void setTime(long time) {
      SprintModule.time = time;
   }
}
