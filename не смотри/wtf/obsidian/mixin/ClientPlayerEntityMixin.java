package wtf.obsidian.mixin;

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























import com.mojang.authlib.GameProfile;
import net.minecraft.client.network.ClientPlayNetworkHandler;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.entity.MovementType;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.network.packet.c2s.play.CloseHandledScreenC2SPacket;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import org.jetbrains.annotations.NotNull;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;












@Mixin({ClientPlayerEntity.class})
public abstract class ClientPlayerEntityMixin extends PlayerEntity implements IInstance {
   @Shadow
   @Final
   public ClientPlayNetworkHandler networkHandler;
   private static final float BODY_FOLLOW_SPEED = 0.4F;

   public ClientPlayerEntityMixin(World world, BlockPos pos, float yaw, GameProfile gameProfile) {
      super(world, pos, yaw, gameProfile);
   }

   @Shadow
   public abstract void closeScreen();

   @Inject(
      method = {"tick"},
      at = {@At("HEAD")}
   )
   private void onTick(CallbackInfo ci) {
      if (EventManager.hasListeners(EventTick.class)) {
         new EventTick().call();
      }
   }

   @Inject(
      method = {"tick"},
      at = {@At("TAIL")}
   )
   private void onTickPost(CallbackInfo ci) {
      if (EventManager.hasListeners(EventPostTick.class)) {
         new EventPostTick().call();
      }

      if (this.shouldSyncRotation()) {
         this.headYaw = this.getYaw();
         this.bodyYaw = this.bodyYaw + MathHelper.wrapDegrees(this.headYaw - this.bodyYaw) * 0.4F;
      }
   }

   @Unique
   private boolean shouldSyncRotation() {
      return ModuleManager.aura.isEnable() && RotationManager.instance.isRotating();
   }

   @Redirect(
      method = {"tickMovement"},
      at = @At(
         value = "INVOKE",
         target = "Lnet/minecraft/client/option/KeyBinding;isPressed()Z",
         ordinal = 1
      ),
      require = 0
   )
   private boolean onSprintKeyPressed(KeyBinding instance) {
      if (!ProtocolUtil.isTargetProtocolBelowOneNineteen() || !this.horizontalCollision && !this.collidedSoftly) {
         EventSprintKey event = new EventSprintKey();
         event.call();
         return event.isCancelled() ? false : instance.isPressed();
      } else {
         return false;
      }
   }

   @Redirect(
      method = {"tickMovement"},
      at = @At(
         value = "INVOKE",
         target = "Lnet/minecraft/client/network/ClientPlayerEntity;isUsingItem()Z"
      ),
      require = 0
   )
   private boolean onSlowDownRedirect(ClientPlayerEntity player) {
      if (player.isUsingItem()) {
         Event_150 event = new Event_150();
         event.call();
         return player.isUsingItem() && player.getVehicle() == null && !event.isCancelled();
      } else {
         return player.isUsingItem() && player.getVehicle() == null;
      }
   }

   @Inject(
      method = {"pushOutOfBlocks"},
      at = {@At("HEAD")},
      cancellable = true
   )
   public void pushOutOfBlocks(double x, double z, CallbackInfo ci) {
      if (ModuleManager.noPush.isEnable() && ModuleManager.noPush.getCollisionList().is("Блоки")) {
         ci.cancel();
      }
   }

   @Inject(
      method = {"move"},
      at = {@At("HEAD")},
      cancellable = true
   )
   private void onMoveHook(MovementType movementType, Vec3d movement, @NotNull CallbackInfo ci) {
      Event_321 event = new Event_321(movement);
      event.call();
      if (event.isCancelled() || !event.getMovePos().equals(movement)) {
         if (event.isCancelled()) {
            ci.cancel();
         } else {
            double d = this.getX();
            double e = this.getZ();
            super.move(movementType, event.getMovePos());
            float f = (float)Math.sqrt(Math.pow(this.getX() - d, 2.0) + Math.pow(this.getZ() - e, 2.0));
            this.updateLimbs(f);
            ci.cancel();
         }
      }
   }

   @Inject(
      method = {"closeHandledScreen"},
      at = {@At("HEAD")},
      cancellable = true
   )
   private void onCloseHandledScreen(CallbackInfo ci) {
      int syncId = this.currentScreenHandler.syncId;
      Event_88 event = new Event_88(syncId);
      event.call();
      if (!event.isCancelled()) {
         this.networkHandler.sendPacket(new CloseHandledScreenC2SPacket(syncId));
      }

      this.closeScreen();
      ci.cancel();
   }

   @Inject(
      method = {"dropSelectedItem"},
      at = {@At("HEAD")},
      cancellable = true
   )
   private void onDropSelectedItem(boolean entireStack, CallbackInfoReturnable<Boolean> cir) {
      if (ModuleManager.lockSlot != null && ModuleManager.lockSlot.isCurrentSlotLockedForDrop()) {
         cir.setReturnValue(false);
      }
   }
}
