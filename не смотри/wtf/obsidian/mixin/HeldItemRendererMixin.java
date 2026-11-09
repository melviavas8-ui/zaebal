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























import com.google.common.base.MoreObjects;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.AbstractClientPlayerEntity;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.VertexConsumerProvider.Immediate;
import net.minecraft.client.render.item.HeldItemRenderer;
import net.minecraft.client.render.item.HeldItemRenderer.HandRenderType;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Arm;
import net.minecraft.util.Hand;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.RotationAxis;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyArg;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.At.Shift;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;







@Mixin({HeldItemRenderer.class})
public abstract class HeldItemRendererMixin {
   @Shadow
   private ItemStack mainHand;
   @Shadow
   private float equipProgressMainHand;
   @Shadow
   private float prevEquipProgressMainHand;
   @Shadow
   private float prevEquipProgressOffHand;
   @Shadow
   private float equipProgressOffHand;
   @Shadow
   private ItemStack offHand;

   @Shadow
   protected abstract void renderFirstPersonItem(
      AbstractClientPlayerEntity var1,
      float var2,
      float var3,
      Hand var4,
      float var5,
      ItemStack var6,
      float var7,
      MatrixStack var8,
      VertexConsumerProvider var9,
      int var10
   );

   @Inject(
      method = {"renderItem(FLnet/minecraft/client/util/math/MatrixStack;Lnet/minecraft/client/render/VertexConsumerProvider$Immediate;Lnet/minecraft/client/network/ClientPlayerEntity;I)V"},
      at = {@At("HEAD")}
   )
   private void onRenderItemHead(float tickProgress, MatrixStack matrices, Immediate immediate, ClientPlayerEntity player, int light, CallbackInfo ci) {
      ShaderHandsModule shaderHands = this.getShaderHands();
      if (shaderHands != null && shaderHands.isEnable()) {
         ShaderHandsRenderer.getInstance().captureBeforeHands();
      }
   }

   @Inject(
      method = {"renderItem(FLnet/minecraft/client/util/math/MatrixStack;Lnet/minecraft/client/render/VertexConsumerProvider$Immediate;Lnet/minecraft/client/network/ClientPlayerEntity;I)V"},
      at = {@At("TAIL")}
   )
   private void onRenderItemTail(float tickProgress, MatrixStack matrices, Immediate immediate, ClientPlayerEntity player, int light, CallbackInfo ci) {
      ShaderHandsModule shaderHands = this.getShaderHands();
      if (shaderHands != null && shaderHands.isEnable()) {
         ShaderHandsRenderer.getInstance().captureAfterHands();
      }
   }

   @Redirect(
      method = {"renderItem(FLnet/minecraft/client/util/math/MatrixStack;Lnet/minecraft/client/render/VertexConsumerProvider$Immediate;Lnet/minecraft/client/network/ClientPlayerEntity;I)V"},
      at = @At(
         value = "INVOKE",
         target = "Lnet/minecraft/client/render/item/HeldItemRenderer;renderFirstPersonItem(Lnet/minecraft/client/network/AbstractClientPlayerEntity;FFLnet/minecraft/util/Hand;FLnet/minecraft/item/ItemStack;FLnet/minecraft/client/util/math/MatrixStack;Lnet/minecraft/client/render/VertexConsumerProvider;I)V"
      )
   )
   private void onRenderFirstPersonItemCall(
      HeldItemRenderer instance,
      AbstractClientPlayerEntity player,
      float tickDelta,
      float pitch,
      Hand hand,
      float swingProgress,
      ItemStack stack,
      float equipProgress,
      MatrixStack matrices,
      VertexConsumerProvider vertexConsumers,
      int light
   ) {
      Hand renderHand = hand;
      SwingAnimationsModule tweaks = this.getTweaks();
      if (tweaks != null && tweaks.isEnable() && !tweaks.hmiEnable.isState() && tweaks.swapHands.isState()) {
         renderHand = hand == Hand.MAIN_HAND ? Hand.OFF_HAND : Hand.MAIN_HAND;
      }

      ((HeldItemRendererInvoker)instance)
         .whylol$callRenderFirstPersonItem(player, tickDelta, pitch, renderHand, swingProgress, stack, equipProgress, matrices, vertexConsumers, light);
   }

   @ModifyArg(
      method = {"renderFirstPersonItem"},
      at = @At(
         value = "INVOKE",
         target = "Lnet/minecraft/client/render/item/HeldItemRenderer;renderArmHoldingItem(Lnet/minecraft/client/util/math/MatrixStack;Lnet/minecraft/client/render/VertexConsumerProvider;IFFLnet/minecraft/util/Arm;)V"
      ),
      index = 5
   )
   private Arm swapEmptyHandArm(Arm arm) {
      SwingAnimationsModule tweaks = this.getTweaks();
      if (tweaks != null && tweaks.isEnable() && !tweaks.hmiEnable.isState() && tweaks.swapHands.isState()) {
         return arm == Arm.RIGHT ? Arm.LEFT : Arm.RIGHT;
      } else {
         return arm;
      }
   }

   @Inject(
      method = {"renderFirstPersonItem"},
      at = {@At(
         value = "INVOKE",
         target = "Lnet/minecraft/client/util/math/MatrixStack;push()V",
         shift = Shift.AFTER
      )}
   )
   private void onRenderFirstPersonItem(
      AbstractClientPlayerEntity player,
      float tickDelta,
      float pitch,
      Hand hand,
      float swingProgress,
      ItemStack stack,
      float equipProgress,
      MatrixStack matrices,
      VertexConsumerProvider vertexConsumers,
      int light,
      CallbackInfo ci
   ) {
      ViewModelModule viewModel = this.getViewModel();
      if (viewModel != null && viewModel.isEnable()) {
         if (hand == Hand.MAIN_HAND) {
            matrices.translate(viewModel.mainHandX.get(), viewModel.mainHandY.get(), viewModel.mainHandZ.get());
         } else {
            matrices.translate(viewModel.offHandX.get(), viewModel.offHandY.get(), viewModel.offHandZ.get());
         }
      }
   }

   @Redirect(
      method = {"renderFirstPersonItem"},
      at = @At(
         value = "INVOKE",
         target = "Lnet/minecraft/client/render/item/HeldItemRenderer;swingArm(FFLnet/minecraft/client/util/math/MatrixStack;ILnet/minecraft/util/Arm;)V",
         ordinal = 2
      )
   )
   private void onSwingArm(HeldItemRenderer instance, float swingProgress, float equipProgress, MatrixStack matrices, int armX, Arm arm) {
      SwingAnimationsModule tweaks = this.getTweaks();
      if (tweaks != null && tweaks.isEnable() && !tweaks.hmiEnable.isState() && tweaks.swingEnabled.isState()) {
         KillAuraModule aura = ModuleManager.INSTANCE != null ? ModuleManager.aura : null;
         if (!tweaks.auraTargetOnly.isState() || aura != null && aura.isEnable() && aura.getTarget() != null && aura.getTarget().isAlive()) {
            if (MinecraftClient.getInstance().player != null) {
               Arm expectedSwingArm = MinecraftClient.getInstance().player.getMainArm();
               if (tweaks.swapHands.isState()) {
                  expectedSwingArm = expectedSwingArm == Arm.RIGHT ? Arm.LEFT : Arm.RIGHT;
               }

               if (arm != expectedSwingArm) {
                  this.callSwingArm(instance, swingProgress, equipProgress, matrices, armX, arm);
                  return;
               }
            }

            int i = arm == Arm.RIGHT ? 1 : -1;
            float strength = tweaks.swingStrength.get();
            float sin1 = MathHelper.sin(swingProgress * swingProgress * (float) Math.PI);
            float sin2 = MathHelper.sin(MathHelper.sqrt(swingProgress) * (float) Math.PI);
            String var13 = tweaks.swingType.getCurrent();
            switch (var13) {
               case "Down":
                  matrices.translate((float)i * 0.56F, -0.32F, -0.72F);
                  matrices.multiply(RotationAxis.POSITIVE_Y.rotationDegrees((float)(76 * i)));
                  matrices.multiply(RotationAxis.POSITIVE_Y.rotationDegrees(sin2 * -5.0F * strength));
                  matrices.multiply(RotationAxis.NEGATIVE_X.rotationDegrees(sin2 * -100.0F * strength));
                  matrices.multiply(RotationAxis.POSITIVE_X.rotationDegrees(sin2 * -155.0F * strength));
                  matrices.multiply(RotationAxis.POSITIVE_X.rotationDegrees(-100.0F));
                  break;
               case "Poke": {
                  float anim = (float)Math.sin((double)swingProgress * (Math.PI / 2) * 2.0);
                  float tilt = strength / 3.0F;
                  matrices.translate((float)i * 0.56F, -0.52F, -0.72F);
                  matrices.translate(0.0F, 0.0F, tilt * -anim);
                  matrices.multiply(RotationAxis.POSITIVE_Y.rotationDegrees(75.0F * (float)i));
                  matrices.multiply(RotationAxis.POSITIVE_Z.rotationDegrees((-75.0F * (strength / 4.0F) * anim - 60.0F) * (float)i));
                  matrices.multiply(RotationAxis.POSITIVE_X.rotationDegrees(-75.0F));
                  break;
               }
               case "Static":
                  matrices.translate((float)i * 0.56F, -0.42F, -0.72F);
                  matrices.multiply(RotationAxis.POSITIVE_X.rotationDegrees(sin2 * -60.0F * strength));
                  matrices.translate(0.0, -0.1, 0.0);
                  break;
               case "Feast":
                  matrices.translate((float)i * 0.56F, -0.32F, -0.72F);
                  matrices.multiply(RotationAxis.POSITIVE_Y.rotationDegrees((float)(30 * i)));
                  matrices.multiply(RotationAxis.POSITIVE_Y.rotationDegrees(sin2 * 75.0F * (float)i * strength));
                  matrices.multiply(RotationAxis.POSITIVE_X.rotationDegrees(sin2 * -65.0F * strength));
                  matrices.multiply(RotationAxis.POSITIVE_Y.rotationDegrees((float)(30 * i)));
                  matrices.multiply(RotationAxis.POSITIVE_X.rotationDegrees(-80.0F));
                  matrices.multiply(RotationAxis.POSITIVE_Y.rotationDegrees((float)(35 * i)));
                  break;
               case "Akrien":
                  matrices.translate((float)i * 0.65F, -0.32F, -0.72F);
                  matrices.multiply(RotationAxis.POSITIVE_Y.rotationDegrees((float)(76 * i)));
                  matrices.multiply(RotationAxis.POSITIVE_Y.rotationDegrees(sin2 * -5.0F * strength));
                  matrices.multiply(RotationAxis.NEGATIVE_X.rotationDegrees(sin2 * -100.0F * strength));
                  matrices.multiply(RotationAxis.POSITIVE_X.rotationDegrees(sin2 * -155.0F * strength));
                  matrices.multiply(RotationAxis.POSITIVE_X.rotationDegrees(-100.0F));
                  matrices.multiply(RotationAxis.POSITIVE_Y.rotationDegrees(sin2 * 25.0F * strength));
                  matrices.multiply(RotationAxis.NEGATIVE_X.rotationDegrees(sin2 * -25.0F * strength));
                  matrices.multiply(RotationAxis.NEGATIVE_X.rotationDegrees(sin1 * 15.0F * strength));
                  matrices.translate(sin2 * 0.18F * strength, sin2 * 0.59F * strength, 0.0F);
                  break;
               case "Smooth":
                  this.applySwingOffset(matrices, i, swingProgress, strength);
                  break;
               case "Block":
                  if (swingProgress > 0.0F) {
                     float gx = MathHelper.sin(MathHelper.sqrt(swingProgress) * (float) Math.PI);
                     matrices.translate(0.56F * (float)i, equipProgress * -0.2F - 0.5F, -0.7F);
                     matrices.multiply(RotationAxis.POSITIVE_Y.rotationDegrees((float)(45 * i)));
                     matrices.multiply(RotationAxis.POSITIVE_X.rotationDegrees(gx * -85.0F * strength));
                     matrices.translate(-0.1F * (float)i, 0.28F, 0.2F);
                     matrices.multiply(RotationAxis.POSITIVE_X.rotationDegrees(-85.0F));
                  } else {
                     float n = -0.4F * MathHelper.sin(MathHelper.sqrt(swingProgress) * (float) Math.PI);
                     float m = 0.2F * MathHelper.sin(MathHelper.sqrt(swingProgress) * (float) (Math.PI * 2));
                     float f1 = -0.2F * MathHelper.sin(swingProgress * (float) Math.PI);
                     matrices.translate(n * (float)i * strength, m * strength, f1 * strength);
                     this.applyEquipOffset(matrices, i, equipProgress);
                     this.applySwingOffset(matrices, i, swingProgress, strength);
                  }
                  break;
               case "ToBack":
                  float g = MathHelper.sin(MathHelper.sqrt(swingProgress) * (float) Math.PI);
                  matrices.translate(0.65F * (float)i, -0.45F, -0.9F);
                  matrices.multiply(RotationAxis.POSITIVE_X.rotationDegrees(50.0F));
                  matrices.multiply(RotationAxis.POSITIVE_Y.rotationDegrees((-30.0F * (1.0F - g * strength) - 30.0F) * (float)i));
                  matrices.multiply(RotationAxis.POSITIVE_Z.rotationDegrees(110.0F * (float)i));
                  break;
               case "SelfBack": {
                  float anim = (float)Math.sin((double)swingProgress * (Math.PI / 2) * 2.0);
                  matrices.translate(0.65F * (float)i, -0.3F, -0.8F);
                  matrices.multiply(RotationAxis.POSITIVE_Y.rotationDegrees((float)(90 * i)));
                  matrices.multiply(RotationAxis.POSITIVE_Z.rotationDegrees((float)(-70 * i)));
                  matrices.multiply(RotationAxis.POSITIVE_X.rotationDegrees(-100.0F - 60.0F * strength * anim));
                  break;
               }
               case "Break":
               case "Брик":
                  matrices.translate(0.66F * (float)i, -0.3F, -0.38F);
                  matrices.multiply(RotationAxis.POSITIVE_Y.rotationDegrees((float)(270 * i)));
                  matrices.multiply(RotationAxis.POSITIVE_X.rotationDegrees(sin2 * 10.0F * strength));
                  matrices.scale(0.5F, 0.5F, 0.5F);
                  matrices.translate(-0.1F * (float)i, 0.2F, 0.0F);
                  matrices.multiply(RotationAxis.POSITIVE_Y.rotationDegrees(-10.0F * (float)i));
                  matrices.multiply(RotationAxis.POSITIVE_X.rotationDegrees(90.0F));
                  matrices.multiply(RotationAxis.POSITIVE_Y.rotationDegrees(-105.0F * (float)i));
                  break;
               case "DropDown": {
                  float anim = (float)Math.sin((double)swingProgress * (Math.PI / 2) * 2.0);
                  this.applyEquipOffset(matrices, i, 0.0F);
                  matrices.multiply(RotationAxis.POSITIVE_Y.rotationDegrees(80.0F));
                  matrices.multiply(RotationAxis.POSITIVE_X.rotationDegrees(tweaks.corner.get()));
                  matrices.multiply(RotationAxis.POSITIVE_X.rotationDegrees(-tweaks.slant.get() * anim * strength));
                  break;
               }
               case "Pander":
                  float panderAnim = MathHelper.sin(swingProgress * (float) Math.PI);
                  float panderF = 1.0F - equipProgress;
                  matrices.translate((float)i * 0.56F, -0.52F, -0.72F);
                  matrices.translate((0.3F - panderAnim * 0.15F) * (float)i, 0.2F - panderF * 0.12F, -0.15F - panderAnim * 0.13F);
                  matrices.multiply(RotationAxis.POSITIVE_Y.rotationDegrees((76.0F - 10.0F * panderAnim) * (float)i));
                  matrices.multiply(RotationAxis.POSITIVE_Z.rotationDegrees((-16.0F - 8.0F * panderAnim) * (float)i));
                  matrices.multiply(RotationAxis.POSITIVE_X.rotationDegrees(-83.0F - 26.0F * panderAnim));
                  break;
               case "Slant": {
                  float anim = (float)Math.sin((double)swingProgress * (Math.PI / 2) * 2.0);
                  float rotate = 35.0F * strength;
                  matrices.translate((float)i * 0.56F, -0.52F, -0.72F);
                  matrices.translate(0.0F, 0.0F, -0.3F * anim * strength);
                  matrices.multiply(RotationAxis.POSITIVE_X.rotationDegrees(anim * -rotate));
                  matrices.multiply(RotationAxis.POSITIVE_Z.rotationDegrees(anim * rotate));
                  break;
               }
               default:
                  this.callSwingArm(instance, swingProgress, equipProgress, matrices, armX, arm);
            }
         } else {
            this.callSwingArm(instance, swingProgress, equipProgress, matrices, armX, arm);
         }
      } else {
         this.callSwingArm(instance, swingProgress, equipProgress, matrices, armX, arm);
      }
   }

   @Overwrite
   public void renderItem(float tickDelta, MatrixStack matrices, Immediate vertexConsumers, ClientPlayerEntity player, int light) {
      float f = player.getHandSwingProgress(tickDelta);
      Hand hand = (Hand)MoreObjects.firstNonNull(player.preferredHand, Hand.MAIN_HAND);
      float g = player.getLerpedPitch(tickDelta);
      HandRenderType handRenderType = HeldItemRenderer.getHandRenderType(player);
      float h = MathHelper.lerp(tickDelta, player.lastRenderPitch, player.renderPitch);
      float i = MathHelper.lerp(tickDelta, player.lastRenderYaw, player.renderYaw);
      if (handRenderType.renderMainHand) {
         float j = hand == Hand.MAIN_HAND ? f : 0.0F;
         float k = 1.0F - MathHelper.lerp(tickDelta, this.prevEquipProgressMainHand, this.equipProgressMainHand);
         this.renderFirstPersonItem(player, tickDelta, g, Hand.MAIN_HAND, j, this.mainHand, k, matrices, vertexConsumers, light);
      }

      if (handRenderType.renderOffHand) {
         float j = hand == Hand.OFF_HAND ? f : 0.0F;
         float k = 1.0F - MathHelper.lerp(tickDelta, this.prevEquipProgressOffHand, this.equipProgressOffHand);
         this.renderFirstPersonItem(player, tickDelta, g, Hand.OFF_HAND, j, this.offHand, k, matrices, vertexConsumers, light);
      }

      vertexConsumers.draw();
   }

   @Inject(
      method = {"applyEatOrDrinkTransformation"},
      at = {@At("HEAD")},
      cancellable = true
   )
   private void onApplyEatOrDrinkTransformation(MatrixStack matrices, float tickDelta, Arm arm, ItemStack stack, PlayerEntity player, CallbackInfo ci) {
      SwingAnimationsModule tweaks = this.getTweaks();
      if (tweaks != null && tweaks.isEnable() && !tweaks.hmiEnable.isState() && tweaks.eatAnim.isState() && player.isUsingItem()) {
         this.applyEatOrDrinkTransformationCustom(matrices, tickDelta, arm, stack);
         ci.cancel();
      }
   }

   private void applyEatOrDrinkTransformationCustom(MatrixStack matrices, float tickDelta, Arm arm, ItemStack stack) {
      if (MinecraftClient.getInstance().player != null) {
         float f = (float)MinecraftClient.getInstance().player.getItemUseTimeLeft() - tickDelta + 1.0F;
         float g = f / (float)stack.getMaxUseTime(MinecraftClient.getInstance().player);
         if (g < 0.8F) {
            float h = MathHelper.abs(MathHelper.cos(f / 4.0F * (float) Math.PI) * 0.005F);
            matrices.translate(0.0F, h, 0.0F);
         }

         float h = 1.0F - (float)Math.pow((double)g, 27.0);
         int i = arm == Arm.RIGHT ? 1 : -1;
         float offsetX = 0.0F;
         float offsetY = 0.0F;
         float offsetZ = 0.0F;
         ViewModelModule viewModel = this.getViewModel();
         if (viewModel != null && viewModel.isEnable()) {
            if (arm == Arm.RIGHT) {
               offsetX = viewModel.mainHandX.get();
               offsetY = viewModel.mainHandY.get();
               offsetZ = viewModel.mainHandZ.get();
            } else {
               offsetX = viewModel.offHandX.get();
               offsetY = viewModel.offHandY.get();
               offsetZ = viewModel.offHandZ.get();
            }
         }

         matrices.translate(h * 0.6F * (float)i + offsetX, h * -0.5F + offsetY, offsetZ);
         matrices.multiply(RotationAxis.POSITIVE_Y.rotationDegrees((float)i * h * 90.0F));
         matrices.multiply(RotationAxis.POSITIVE_X.rotationDegrees(h * 10.0F));
         matrices.multiply(RotationAxis.POSITIVE_Z.rotationDegrees((float)i * h * 30.0F));
      }
   }

   private void applyEquipOffset(MatrixStack matrices, int i, float equipProgress) {
      matrices.translate((float)i * 0.56F, -0.52F + equipProgress * -0.6F, -0.72F);
   }

   private void applySwingOffset(MatrixStack matrices, int i, float swingProgress, float strength) {
      float f = MathHelper.sin(swingProgress * swingProgress * (float) Math.PI);
      matrices.translate(0.56F * (float)i, -0.52F, -0.72F);
      matrices.multiply(RotationAxis.POSITIVE_Y.rotationDegrees((float)i * (45.0F + f * -20.0F * strength)));
      float g = MathHelper.sin(MathHelper.sqrt(swingProgress) * (float) Math.PI);
      matrices.multiply(RotationAxis.POSITIVE_Z.rotationDegrees((float)i * g * -20.0F * strength));
      matrices.multiply(RotationAxis.POSITIVE_X.rotationDegrees(g * -80.0F * strength));
      matrices.multiply(RotationAxis.POSITIVE_Y.rotationDegrees((float)i * -45.0F));
   }

   private void callSwingArm(HeldItemRenderer instance, float swingProgress, float equipProgress, MatrixStack matrices, int armX, Arm arm) {
      ((HeldItemRendererInvoker)instance).whylol$callSwingArm(swingProgress, equipProgress, matrices, armX, arm);
   }

   private SwingAnimationsModule getTweaks() {
      return ModuleManager.INSTANCE == null ? null : ModuleManager.swingAnimations;
   }

   private ViewModelModule getViewModel() {
      return ModuleManager.INSTANCE == null ? null : ModuleManager.viewModel;
   }

   private ShaderHandsModule getShaderHands() {
      return ModuleManager.INSTANCE == null ? null : ModuleManager.shaderHands;
   }
}
