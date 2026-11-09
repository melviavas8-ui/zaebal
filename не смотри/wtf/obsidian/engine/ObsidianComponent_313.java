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
























import net.minecraft.client.render.OverlayTexture;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.entity.EntityRendererFactory.Context;
import net.minecraft.client.render.entity.feature.FeatureRenderer;
import net.minecraft.client.render.entity.feature.FeatureRendererContext;
import net.minecraft.client.render.entity.model.AllayEntityModel;
import net.minecraft.client.render.entity.model.EntityModelLayers;
import net.minecraft.client.render.entity.model.PlayerEntityModel;
import net.minecraft.client.render.entity.state.AllayEntityRenderState;
import net.minecraft.client.render.entity.state.PlayerEntityRenderState;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.RotationAxis;

public class ObsidianComponent_313 extends FeatureRenderer<PlayerEntityRenderState, PlayerEntityModel> {
   private static final Identifier ALLAY_TEXTURE = Identifier.ofVanilla("textures/entity/allay/allay.png");
   public final AllayEntityModel model;
   private final AllayEntityRenderState allayState = new AllayEntityRenderState();

   public ObsidianComponent_313(FeatureRendererContext<PlayerEntityRenderState, PlayerEntityModel> context, Context rendererContext) {
      super(context);
      this.model = new AllayEntityModel(rendererContext.getPart(EntityModelLayers.ALLAY));
   }

   public void render(
      MatrixStack matrices, VertexConsumerProvider vertexConsumers, int light, PlayerEntityRenderState playerState, float yawDegrees, float pitch
   ) {
      SatelliteModule sattelite = ModuleManager.sattelite;
      if (sattelite != null && sattelite.shouldRender(playerState)) {
         matrices.push();
         float baseX = sattelite.isLeftShoulder()
            ? 0.4000000059604645f
            : -0.4000000059604645f;
         float baseY = playerState.isInSneakingPose
            ? -1.2999999523162842f
            : -1.5f;
         float idleBob = 0.0f;
         float idleYaw = 0.0f;
         float idleRoll = 0.0f;
         float idlePitch = 0.0f;
         float animationAge = playerState.age;
         if (sattelite.idleAnimation.isState()) {
            float speed = sattelite.idleSpeed.get();
            float strength = sattelite.idleStrength.get();
            float time = playerState.age
               * (0.699999988079071f + speed * 0.6499999761581421f);
            idleBob = MathHelper.sin(time * 0.41999998688697815f)
               * 0.05999999865889549f
               * strength;
            idleYaw = MathHelper.sin(time * 0.1599999964237213f)
               * 9.0f
               * strength;
            idleRoll = MathHelper.cos(time * 0.23999999463558197f)
               * 7.0f
               * strength;
            idlePitch = MathHelper.sin(time * 0.3100000023841858f)
               * 5.0f
               * strength;
            animationAge = playerState.age
               * (0.8500000238418579f + speed * 0.44999998807907104f);
         }

         matrices.translate(baseX + sattelite.offsetX.get(), baseY + sattelite.offsetY.get() + idleBob, sattelite.offsetZ.get());
         float scale = sattelite.scale.get();
         matrices.scale(scale, scale, scale);
         matrices.multiply(RotationAxis.POSITIVE_X.rotationDegrees(sattelite.rotateX.get() + idlePitch));
         matrices.multiply(RotationAxis.POSITIVE_Y.rotationDegrees(sattelite.rotateY.get() + idleYaw));
         matrices.multiply(RotationAxis.POSITIVE_Z.rotationDegrees(sattelite.rotateZ.get() + idleRoll));
         this.allayState.age = animationAge;
         this.allayState.limbFrequency = playerState.limbFrequency;
         this.allayState.limbAmplitudeMultiplier = playerState.limbAmplitudeMultiplier;
         this.allayState.yawDegrees = yawDegrees;
         this.allayState.pitch = pitch;
         this.allayState.invisible = playerState.invisible;
         this.allayState.invisibleToPlayer = playerState.invisibleToPlayer;
         this.allayState.hasOutline = playerState.hasOutline;
         this.allayState.shaking = playerState.shaking;
         this.allayState.baby = false;
         this.allayState.touchingWater = playerState.touchingWater;
         this.allayState.bodyYaw = playerState.bodyYaw;
         this.allayState.baseScale = 1.0f;
         this.allayState.ageScale = 1.0f;
         this.allayState.pose = playerState.pose;
         this.allayState.deathTime = 0.0f;
         this.allayState.hurt = playerState.hurt;
         this.allayState.dancing = false;
         this.allayState.spinning = false;
         this.allayState.spinningAnimationTicks = 0.0f;
         this.allayState.itemHoldAnimationTicks = 0.0f;
         this.model.setAngles(this.allayState);
         VertexConsumer vertexConsumer = vertexConsumers.getBuffer(this.model.getLayer(ALLAY_TEXTURE));
         this.model.render(matrices, vertexConsumer, light, OverlayTexture.DEFAULT_UV);
         matrices.pop();
      }
   }
}
