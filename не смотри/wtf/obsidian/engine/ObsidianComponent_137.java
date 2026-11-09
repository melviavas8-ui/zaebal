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
























import java.util.ArrayList;
import java.util.List;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.math.Vec3i;
public class ObsidianComponent_137 {
   public final LineGlyphesModule this$0;
   public final List<Vec3i> points;
   public final AnimationUtil alpha;
   public Vec3i lastDirection;
   public int currentStepTicks;
   public int lastStepSet;
   public int stepsLeft;
   public boolean removing;
public ObsidianComponent_137(final LineGlyphesModule param1, Vec3i spawnPos, int maxSteps) {
      this.this$0 = param1;
      this.points = new ArrayList<>();
      this.alpha = new AnimationUtil(
         0.0f, 8.0f, ObsidianComponent_57.QUAD_OUT
      );
      this.points.add(spawnPos);
      this.lastDirection = param1.randomAxisDirection();
      this.stepsLeft = maxSteps;
      this.alpha.setValue(0.0f);
   }

   public void update() {
      this.alpha.update(this.removing ? 0.0f : 1.0f);
      if (!this.removing) {
         if (this.stepsLeft <= 0) {
            this.setWantToRemove();
         } else if (this.currentStepTicks > 0) {
            this.currentStepTicks = this.currentStepTicks
               - (this.this$0.slowSpeed.isState() ? 1 : 2);
            if (this.currentStepTicks < 0) {
               this.currentStepTicks = 0;
            }
         } else {
            Vec3i last = this.points.get(this.points.size() - 1);
            boolean added = false;

            for (int attempt = 0; attempt < (8); attempt++) {
               Vec3i nextDirection = this.this$0.nextOrthogonalDirection(this.lastDirection);
               int step = this.this$0.randInt(1, 4);
               Vec3i next = new Vec3i(
                  last.getX() + nextDirection.getX() * step, last.getY() + nextDirection.getY() * step, last.getZ() + nextDirection.getZ() * step
               );
               if (this.this$0.isSpawnPosFree(next)) {
                  this.lastDirection = nextDirection;
                  this.lastStepSet = step;
                  this.currentStepTicks = step;
                  this.points.add(next);
                  this.stepsLeft -= 1;
                  added = true;
                  break;
               }
            }

            if (!added) {
               this.setWantToRemove();
            }
         }
      }
   }

   public List<Vec3d> getSmoothedPositions(float partialTicks) {
      List<Vec3d> smoothed = new ArrayList<>(this.points.size());
      float moveAdvance = this.this$0.moveAdvanceFromTicks(this.lastStepSet, this.currentStepTicks, partialTicks);

      for (int i = 0; i < this.points.size(); i++) {
         Vec3i point = this.points.get(i);
         double x = (double)point.getX();
         double y = (double)point.getY();
         double z = (double)point.getZ();
         if (this.points.size() >= (2) && i == this.points.size() - (1)) {
            Vec3i previous = this.points.get(this.points.size() - 2);
            x = LineGlyphesModule.lerp((double)previous.getX(), x, (double)moveAdvance);
            y = LineGlyphesModule.lerp((double)previous.getY(), y, (double)moveAdvance);
            z = LineGlyphesModule.lerp((double)previous.getZ(), z, (double)moveAdvance);
         }

         smoothed.add(new Vec3d(x, y, z));
      }

      return smoothed;
   }

   public int getPointCount() {
      return this.points.size();
   }

   public float getAlpha() {
      return MathHelper.clamp(
         this.alpha.getValue(),
         0.0f,
         1.0f
      );
   }

   public boolean isRemoving() {
      return this.removing;
   }

   public void setWantToRemove() {
      this.removing = true;
   }

   public boolean isDead() {
      return (this.removing && this.getAlpha() <= 0.029999999329447746f);
   }
}
