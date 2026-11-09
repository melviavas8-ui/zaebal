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























import net.minecraft.client.option.CloudRenderMode;
import net.minecraft.client.render.Camera;
import net.minecraft.client.render.Fog;
import net.minecraft.client.render.FrameGraphBuilder;
import net.minecraft.client.render.GameRenderer;
import net.minecraft.client.render.RenderTickCounter;
import net.minecraft.client.render.WorldRenderer;
import net.minecraft.client.render.VertexConsumerProvider.Immediate;
import net.minecraft.client.util.ObjectAllocator;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.profiler.Profilers;
import org.joml.Matrix4f;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;








@Mixin({WorldRenderer.class})
public class WorldRendererMixin implements IInstance {
   @Inject(
      method = {"renderParticles"},
      at = {@At("HEAD")},
      cancellable = true
   )
   private void obsidian$renderParticles(FrameGraphBuilder frameGraphBuilder, Camera camera, float tickDelta, Fog fog, CallbackInfo ci) {
      if (ModuleManager.INSTANCE != null) {
         RemovalsModule removals = ModuleManager.removals;
         if (removals != null && removals.isEnabled("Частицы")) {
            ci.cancel();
         }
      }
   }

   @Inject(
      method = {"renderWeather"},
      at = {@At("HEAD")},
      cancellable = true
   )
   private void obsidian$renderWeather(FrameGraphBuilder frameGraphBuilder, Vec3d pos, float tickDelta, Fog fog, CallbackInfo ci) {
      if (ModuleManager.INSTANCE != null) {
         RemovalsModule removals = ModuleManager.removals;
         if (removals != null && removals.isEnabled("Погода")) {
            ci.cancel();
         }
      }
   }

   @Inject(
      method = {"addWeatherParticlesAndSound"},
      at = {@At("HEAD")},
      cancellable = true
   )
   private void obsidian$addWeatherParticlesAndSound(Camera camera, CallbackInfo ci) {
      if (ModuleManager.INSTANCE != null) {
         RemovalsModule removals = ModuleManager.removals;
         if (removals != null && removals.isEnabled("Погода")) {
            ci.cancel();
         }
      }
   }

   @Inject(
      method = {"renderClouds"},
      at = {@At("HEAD")},
      cancellable = true
   )
   private void obsidian$renderClouds(
      FrameGraphBuilder frameGraphBuilder,
      Matrix4f positionMatrix,
      Matrix4f projectionMatrix,
      CloudRenderMode renderMode,
      Vec3d cameraPos,
      float ticks,
      int color,
      float cloudHeight,
      CallbackInfo ci
   ) {
      if (ModuleManager.INSTANCE != null) {
         RemovalsModule removals = ModuleManager.removals;
         if (removals != null && removals.isEnabled("Облака")) {
            ci.cancel();
         }
      }
   }

   @Inject(
      method = {"renderBlockEntities"},
      at = {@At("HEAD")},
      cancellable = true
   )
   private void obsidian$renderBlockEntities(
      MatrixStack matrices, Immediate mainConsumers, Immediate translucentConsumers, Camera camera, float tickDelta, CallbackInfo ci
   ) {
      if (ModuleManager.INSTANCE != null) {
         RemovalsModule removals = ModuleManager.removals;
         if (removals != null && removals.isEnabled("Блок-сущности")) {
            ci.cancel();
         }
      }
   }

   @Inject(
      method = {"render"},
      at = {@At("RETURN")}
   )
   private void render(
      ObjectAllocator allocator,
      RenderTickCounter tickCounter,
      boolean renderBlockOutline,
      Camera camera,
      GameRenderer gameRenderer,
      Matrix4f positionMatrix,
      Matrix4f projectionMatrix,
      CallbackInfo ci
   ) {
      SonarModule sonar = ModuleManager.INSTANCE != null ? ModuleManager.sonar : null;
      boolean has3DListeners = EventManager.hasListeners(RenderEvent.class);
      boolean renderSonar = sonar != null && sonar.isEnable();
      if (has3DListeners || renderSonar) {
         Profilers.get().swap("obsidian_renderWorld");
         MatrixStack matrices = new MatrixStack();
         matrices.multiplyPositionMatrix(positionMatrix);
         if (has3DListeners) {
            new RenderEvent(matrices, positionMatrix, projectionMatrix, camera, tickCounter.getTickDelta(false)).call();
         }

         if (renderSonar) {
            sonar.renderFromMixin(positionMatrix, projectionMatrix, camera.getPos());
         }
      }
   }

   @Inject(
      method = {"drawEntityOutlinesFramebuffer"},
      at = {@At("HEAD")},
      cancellable = true
   )
   private void obsidian$drawEntityOutlinesFramebuffer(CallbackInfo ci) {
      if (ModuleManager.INSTANCE != null) {
         ShaderEspModule shaderEsp = ModuleManager.shaderEsp;
         if (shaderEsp != null && shaderEsp.isEnable()) {
            ci.cancel();
         }
      }
   }

   @Inject(
      method = {"drawBlockOutline"},
      at = {@At("HEAD")},
      cancellable = true
   )
   public void onDrawBlockOutline(CallbackInfo ci) {
      if (ModuleManager.blockOverlay.isEnable()) {
         ci.cancel();
      }
   }
}
