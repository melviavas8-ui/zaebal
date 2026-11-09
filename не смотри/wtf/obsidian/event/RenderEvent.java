package wtf.obsidian.event;

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
import net.minecraft.client.render.Camera;
import net.minecraft.client.util.math.MatrixStack;
import org.joml.Matrix4f;

public class RenderEvent extends Event {
   public final MatrixStack matrices;
   public final Matrix4f positionMatrix;
   public final Matrix4f projectionMatrix;
   public final Camera camera;
   public final float tickDelta;

   @Generated
   public MatrixStack getMatrices() {
      return this.matrices;
   }

   @Generated
   public Matrix4f getPositionMatrix() {
      return this.positionMatrix;
   }

   @Generated
   public Matrix4f getProjectionMatrix() {
      return this.projectionMatrix;
   }

   @Generated
   public Camera getCamera() {
      return this.camera;
   }

   @Generated
   public float getTickDelta() {
      return this.tickDelta;
   }

   @Generated
   public RenderEvent(MatrixStack matrices, Matrix4f positionMatrix, Matrix4f projectionMatrix, Camera camera, float tickDelta) {
      this.matrices = matrices;
      this.positionMatrix = positionMatrix;
      this.projectionMatrix = projectionMatrix;
      this.camera = camera;
      this.tickDelta = tickDelta;
   }
}
