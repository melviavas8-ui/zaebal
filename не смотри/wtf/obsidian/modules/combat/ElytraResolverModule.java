package wtf.obsidian.modules.combat;

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
import java.util.Comparator;
import java.util.List;
import net.minecraft.item.Items;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.hit.HitResult.Type;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.RaycastContext;
import net.minecraft.world.RaycastContext.FluidHandling;
import net.minecraft.world.RaycastContext.ShapeType;

public class ElytraResolverModule extends Module {
   public static ElytraResolverModule INSTANCE = new ElytraResolverModule();
   private final SliderSetting distance = new SliderSetting(
      "Дистанция отлета",
      6.0f,
      4.0f,
      8.0f,
      0.10000000149011612f
   );
   private final BooleanSetting autoFirework = new BooleanSetting("Авто-Фейерверк", true);
   public static final float MIN_HEIGHT = 4.0F;
   public boolean escaping;
   public Vec3d escapePos;
   public long escapeStartTime;
   public int returnFireworkTicks = -1;
   public Vec3d lastEscapeDirection;

   public ElytraResolverModule() {
      super("ElytraResolver", "Отлет на элитрах", Category.COMBAT);
      Setting[] var10001 = new Setting[2];
      var10001[0] = this.distance;
      var10001[1] = this.autoFirework;
      this.addSettings(var10001);
      INSTANCE = this;
   }

   @Override
   public void onDisable() {
      super.onDisable();
      this.escaping = false;
      this.escapePos = null;
      this.returnFireworkTicks = -1;
      this.lastEscapeDirection = null;
   }

   public void onAuraAttack() {
      if (this.isEnable() && mc.player != null && mc.player.isGliding()) {
         Vec3d bestPos = this.calculateSmartEscape(mc.player.getPos(), this.distance.get());
         if (bestPos != null) {
            this.escapePos = bestPos;
            this.escaping = true;
            this.escapeStartTime = System.currentTimeMillis();
            if (this.autoFirework.isState()) {
               this.useFirework();
            }
         }
      }
   }

   @EventTarget
   public void onUpdate(EventTick event) {
      if (mc.player != null && mc.world != null && mc.player.isGliding()) {
         if (this.returnFireworkTicks > 0) {
            this.returnFireworkTicks -= 1;
         } else if (this.returnFireworkTicks == 0) {
            if (this.autoFirework.isState()) {
               this.useFirework();
            }

            this.returnFireworkTicks = -1;
         }

         if (this.escaping && this.escapePos != null) {
            double dist = mc.player.getPos().distanceTo(this.escapePos);
            if (dist < 2.0
               || System.currentTimeMillis() - this.escapeStartTime > (1000L)) {
               this.escaping = false;
               if (this.autoFirework.isState()) {
                  this.returnFireworkTicks = 2;
               }
            }
         }
      } else {
         this.escaping = false;
         this.returnFireworkTicks = -1;
      }
   }

   public boolean isEscaping() {
      return (this.isEnable() && this.escaping && this.escapePos != null && mc.player != null && mc.player.isGliding());
   }

   public Vec3d getEscapePos() {
      return this.escapePos;
   }
   public Vec3d calculateSmartEscape(Vec3d pPos, float d) {
      Vec3d playerLook = mc.player.getRotationVector();
      Vec3d playerVelocity = mc.player.getVelocity();
      Vec3d[] directions = this.generateSmartDirections(playerLook, playerVelocity);
      List<ObsidianComponent_351> validPoints = new ArrayList<>();
      Vec3d[] var7 = directions;
      int var8 = directions.length;

      for (int var9 = 0; var9 < var8; var9++) {
         Vec3d dir = var7[var9];
         Vec3d target = pPos.add(dir.multiply((double)d));
         if (!(target.y < pPos.y + 4.0)) {
            RaycastContext context = new RaycastContext(pPos, target, ShapeType.COLLIDER, FluidHandling.NONE, mc.player);
            BlockHitResult hit = mc.world.raycast(context);
            double actualDistance = (double)d;
            Vec3d finalPos = target;
            if (hit.getType() != Type.MISS) {
               double hitDist = hit.getPos().distanceTo(pPos);
               if (!(hitDist > 2.0)) {
                  continue;
               }

               actualDistance = hitDist;
               finalPos = hit.getPos().add(dir.multiply(Double.longBitsToDouble(-4616189618054758400L)));
            }

            double score = this.calculateEscapeScore(dir, playerLook, playerVelocity, actualDistance, finalPos);
            validPoints.add(new ObsidianComponent_351(finalPos, actualDistance, score));
         }
      }

      if (validPoints.isEmpty()) {
         return null;
      } else {
         validPoints.sort(Comparator.comparingDouble(p -> -p.score));
         this.lastEscapeDirection = validPoints.get(0).pos.subtract(pPos).normalize();
         return validPoints.get(0).pos;
      }
   }
   public Vec3d[] generateSmartDirections(Vec3d playerLook, Vec3d velocity) {
      Vec3d back = new Vec3d(-playerLook.x, 0.0, -playerLook.z)
         .normalize();
      Vec3d right = new Vec3d(-playerLook.z, 0.0, playerLook.x)
         .normalize();
      Vec3d left = right.multiply(Double.longBitsToDouble(-4616189618054758400L));
      Vec3d up = new Vec3d(
         0.0,
         1.0,
         0.0
      );
      List<Vec3d> dirs = new ArrayList<>();
      dirs.add(back.add(up).normalize());
      dirs.add(back.add(right).add(up).normalize());
      dirs.add(back.add(left).add(up).normalize());
      dirs.add(right.add(up).normalize());
      dirs.add(left.add(up).normalize());
      dirs.add(
         back.add(right.multiply(0.5))
            .add(up.multiply(1.5))
            .normalize()
      );
      dirs.add(
         back.add(left.multiply(0.5))
            .add(up.multiply(1.5))
            .normalize()
      );
      dirs.add(back.add(up.multiply(2.0)).normalize());
      dirs.add(right.add(up.multiply(1.5)).normalize());
      dirs.add(left.add(up.multiply(1.5)).normalize());
      dirs.add(
         back.multiply(0.7)
            .add(right.multiply(0.3))
            .add(up.multiply(1.2))
            .normalize()
      );
      dirs.add(
         back.multiply(0.7)
            .add(left.multiply(0.3))
            .add(up.multiply(1.2))
            .normalize()
      );
      dirs.add(
         back.multiply(0.5)
            .add(up.multiply(1.8))
            .normalize()
      );
      dirs.add(
         right.multiply(0.8)
            .add(up.multiply(1.3))
            .normalize()
      );
      dirs.add(
         left.multiply(0.8)
            .add(up.multiply(1.3))
            .normalize()
      );
      if (velocity.lengthSquared() > 0.01) {
         Vec3d perpendicular = new Vec3d(-velocity.z, 0.0, velocity.x)
            .normalize();
         dirs.add(perpendicular.add(up).normalize());
         dirs.add(perpendicular.multiply(Double.longBitsToDouble(-4616189618054758400L)).add(up).normalize());
         dirs.add(perpendicular.add(up.multiply(1.5)).normalize());
         dirs.add(
            perpendicular.multiply(Double.longBitsToDouble(-4616189618054758400L))
               .add(up.multiply(1.5))
               .normalize()
         );
      }

      return dirs.toArray(new Vec3d[0]);
   }
   public double calculateEscapeScore(Vec3d direction, Vec3d playerLook, Vec3d velocity, double distance, Vec3d finalPos) {
      double score = 0.0;
      double backwardBonus = -direction.dotProduct(
         new Vec3d(playerLook.x, 0.0, playerLook.z).normalize()
      );
      score += backwardBonus * 30.0;
      score += direction.y * 25.0;
      score += distance * 2.0;
      if (velocity.lengthSquared() > 0.01) {
         Vec3d velNorm = velocity.normalize();
         double perpendicular = Math.abs(
            direction.dotProduct(new Vec3d(-velNorm.z, 0.0, velNorm.x))
         );
         score += perpendicular * 15.0;
      }

      if (this.lastEscapeDirection != null) {
         double similarity = direction.dotProduct(this.lastEscapeDirection);
         if (similarity > 0.7) {
            score -= 20.0;
         }
      }

      double groundDistance = finalPos.y - (double)mc.world.getBottomY();
      if (groundDistance < 10.0) {
         score -= (10.0 - groundDistance)
            * 5.0;
      }

      return score;
   }
   public void useFirework() {
      if (mc.player != null) {
         int slotFirework = ObsidianComponent_225.getItemSlot(Items.FIREWORK_ROCKET);
         if (slotFirework != -1) {
            ObsidianComponent_225.swapAndUseHvH(Items.FIREWORK_ROCKET);
         }
      }
   }
}
