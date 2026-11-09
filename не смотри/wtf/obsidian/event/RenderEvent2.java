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
import net.minecraft.client.render.WorldRenderer;
import net.minecraft.client.util.math.MatrixStack;
import org.joml.Matrix4f;

public class RenderEvent2 extends Event {
   public final WorldRenderer context;
   public final MatrixStack matrix;
   public final Matrix4f projectionMatrix;
   public final Camera camera;
   public final float partialTicks;
   public final long finishTimeNano;

   @Generated
   public WorldRenderer getContext() {
      return this.context;
   }

   @Generated
   public MatrixStack getMatrix() {
      return this.matrix;
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
   public float getPartialTicks() {
      return this.partialTicks;
   }

   @Generated
   public long getFinishTimeNano() {
      return this.finishTimeNano;
   }

   @Generated
   public RenderEvent2(WorldRenderer context, MatrixStack matrix, Matrix4f projectionMatrix, Camera camera, float partialTicks, long finishTimeNano) {
      this.context = context;
      this.matrix = matrix;
      this.projectionMatrix = projectionMatrix;
      this.camera = camera;
      this.partialTicks = partialTicks;
      this.finishTimeNano = finishTimeNano;
   }
}
