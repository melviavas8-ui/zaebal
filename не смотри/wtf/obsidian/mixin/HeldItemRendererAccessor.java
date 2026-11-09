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























import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.AbstractClientPlayerEntity;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.item.HeldItemRenderer;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.LivingEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ModelTransformationMode;
import net.minecraft.util.Arm;
import net.minecraft.util.Hand;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;
import org.spongepowered.asm.mixin.gen.Invoker;

@Mixin({HeldItemRenderer.class})
public interface HeldItemRendererAccessor {
   @Accessor("mainHand")
   ItemStack getMainHand();

   @Accessor("offHand")
   ItemStack getOffHand();

   @Accessor("equipProgressMainHand")
   float getEquipProgressMainHand();

   @Accessor("prevEquipProgressMainHand")
   float getPrevEquipProgressMainHand();

   @Accessor("equipProgressOffHand")
   float getEquipProgressOffHand();

   @Accessor("prevEquipProgressOffHand")
   float getPrevEquipProgressOffHand();

   @Accessor("client")
   MinecraftClient getClient();

   @Invoker("renderFirstPersonItem")
   void invokeRenderFirstPersonItem(
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

   @Invoker("applyEquipOffset")
   void invokeApplyEquipOffset(MatrixStack var1, Arm var2, float var3);

   @Invoker("swingArm")
   void invokeSwingArm(float var1, float var2, MatrixStack var3, int var4, Arm var5);

   @Invoker("renderArmHoldingItem")
   void invokeRenderArmHoldingItem(MatrixStack var1, VertexConsumerProvider var2, int var3, float var4, float var5, Arm var6);

   @Invoker("renderMapInBothHands")
   void invokeRenderMapInBothHands(MatrixStack var1, VertexConsumerProvider var2, int var3, float var4, float var5, float var6);

   @Invoker("renderMapInOneHand")
   void invokeRenderMapInOneHand(MatrixStack var1, VertexConsumerProvider var2, int var3, float var4, Arm var5, float var6, ItemStack var7);

   @Invoker("applySwingOffset")
   void invokeApplySwingOffset(MatrixStack var1, Arm var2, float var3);

   @Invoker("renderItem")
   void invokeRenderItem(LivingEntity var1, ItemStack var2, ModelTransformationMode var3, boolean var4, MatrixStack var5, VertexConsumerProvider var6, int var7);
}
