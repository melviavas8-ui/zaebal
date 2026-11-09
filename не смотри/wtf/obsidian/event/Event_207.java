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
import net.minecraft.client.util.Window;
import net.minecraft.client.util.math.MatrixStack;
import org.joml.Matrix4f;

public class Event_207 extends Event {
   public final Window scaledResolution;
   public final float partialTicks;
   public final Matrix4f matrix;
   public final MatrixStack matrixStack;

   @Generated
   public Window getScaledResolution() {
      return this.scaledResolution;
   }

   @Generated
   public float getPartialTicks() {
      return this.partialTicks;
   }

   @Generated
   public Matrix4f getMatrix() {
      return this.matrix;
   }

   @Generated
   public MatrixStack getMatrixStack() {
      return this.matrixStack;
   }

   @Generated
   public Event_207(Window scaledResolution, float partialTicks, Matrix4f matrix, MatrixStack matrixStack) {
      this.scaledResolution = scaledResolution;
      this.partialTicks = partialTicks;
      this.matrix = matrix;
      this.matrixStack = matrixStack;
   }
}
