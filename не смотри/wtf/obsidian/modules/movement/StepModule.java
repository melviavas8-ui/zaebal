package wtf.obsidian.modules.movement;

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

























import net.minecraft.block.BlockState;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.shape.VoxelShape;

public class StepModule extends Module {
   public static StepModule INSTANCE = new StepModule();
   public ModeSetting mode;
   public SliderSetting height;
   public BooleanSetting reverse;
   public SliderSetting reverseHeight;
   public int timer;

   public StepModule() {
      super("Step", "Моментально взбирается на блок", Category.MOVEMENT);
      String[] var10005 = new String[3];
      var10005[0] = "Vanilla";
      var10005[1] = "NCP";
      var10005[2] = "Motion";
      this.mode = new ModeSetting("Режим", "Vanilla", var10005);
      this.height = new SliderSetting(
         "Высота",
         1.0f,
         1.0f,
         10.0f,
         0.5f
      );
      this.reverse = new BooleanSetting("Reverse", false);
      this.reverseHeight = new SliderSetting(
         "Высота Reverse",
         1.0f,
         1.0f,
         10.0f,
         0.5f
      );
      this.timer = 0;
      Setting[] var10001 = new Setting[4];
      var10001[0] = this.mode;
      var10001[1] = this.height;
      var10001[2] = this.reverse;
      var10001[3] = this.reverseHeight;
      this.addSettings(var10001);
   }

   @Override
   public void onEnable() {
      super.onEnable();
      this.timer = 0;
   }

   @EventTarget
   public void onUpdate(EventTick event) {
      if (mc.player != null && mc.world != null) {
         if (this.reverse.isState() && mc.player.isOnGround() && !mc.options.jumpKey.isPressed() && !mc.player.isSneaking() && !this.isBlockAbove()) {
            float fallDistance = this.reverseHeight.get();
            if (this.canFall(fallDistance)) {
               Vec3d vel = mc.player.getVelocity();
               mc.player.setVelocity(vel.x, (double)(-fallDistance), vel.z);
            }
         }

         if (mc.player.horizontalCollision && mc.player.isOnGround() && !mc.options.jumpKey.isPressed()) {
            float stepHeight = this.getStepHeight();
            if (stepHeight > 0.6000000238418579f && stepHeight <= this.height.get()) {
               if (this.mode.is("Vanilla")) {
                  this.handleVanillaStep(stepHeight);
               }

               if (this.mode.is("NCP")) {
                  this.handleNCPStep(stepHeight);
               }

               if (this.mode.is("Motion")) {
                  this.handleMotionStep(stepHeight);
               }
            }
         } else {
            this.timer = 0;
         }
      }
   }
   public void handleVanillaStep(float stepHeight) {
      mc.player.setPosition(mc.player.getX(), mc.player.getY() + (double)stepHeight, mc.player.getZ());
   }
   public void handleNCPStep(float stepHeight) {
      double[] offsets = null;
      double baseY = mc.player.getY();
      if (stepHeight <= 1.0f) {
         double[] var10000 = new double[2];
         var10000[0] = 0.42;
         var10000[1] = 0.753;
         offsets = var10000;
      } else if (stepHeight <= 1.5f) {
         double[] var10 = new double[6];
         var10[0] = 0.42;
         var10[1] = 0.75;
         var10[2] = 1.0;
         var10[3] = 1.16;
         var10[4] = 1.23;
         var10[5] = 1.2;
         offsets = var10;
      } else if (stepHeight <= 2.0f) {
         double[] var11 = new double[8];
         var11[0] = 0.42;
         var11[1] = 0.78;
         var11[2] = 0.63;
         var11[3] = 0.51;
         var11[4] = 0.9;
         var11[5] = 1.21;
         var11[6] = 1.45;
         var11[7] = 1.43;
         offsets = var11;
      } else if (stepHeight <= 2.5f) {
         double[] var12 = new double[10];
         var12[0] = 0.425;
         var12[1] = 0.821;
         var12[2] = 0.699;
         var12[3] = 0.599;
         var12[4] = 1.022;
         var12[5] = 1.372;
         var12[6] = 1.652;
         var12[7] = 1.869;
         var12[8] = 2.019;
         var12[9] = 1.907;
         offsets = var12;
      } else if (stepHeight <= 3.0f) {
         double[] var13 = new double[12];
         var13[0] = 0.42;
         var13[1] = 0.78;
         var13[2] = 0.63;
         var13[3] = 0.51;
         var13[4] = 0.9;
         var13[5] = 1.21;
         var13[6] = 1.45;
         var13[7] = 1.43;
         var13[8] = 1.78;
         var13[9] = 2.1;
         var13[10] = 2.4;
         var13[11] = 2.7;
         offsets = var13;
      }

      if (offsets != null) {
         double[] var5 = offsets;
         int var6 = offsets.length;

         for (int var7 = 0; var7 < var6; var7++) {
            double offset = var5[var7];
            mc.player.setPosition(mc.player.getX(), baseY + offset, mc.player.getZ());
         }
      }
   }
   public void handleMotionStep(float stepHeight) {
      Vec3d velocity = mc.player.getVelocity();
      double motionY = 0.42;
      if (stepHeight <= 1.0f) {
         motionY = 0.42;
      } else if (stepHeight <= 1.5f) {
         motionY = 0.52;
      } else if (stepHeight <= 2.0f) {
         motionY = 0.62;
      } else if (stepHeight <= 2.5f) {
         motionY = 0.72;
      } else if (stepHeight <= 3.0f) {
         motionY = 0.82;
      }

      mc.player.setVelocity(velocity.x, motionY, velocity.z);
   }
   public float getStepHeight() {
      Box box = mc.player.getBoundingBox();
      float maxY = 0.0f;
      double checkDistance = 0.3;
      double playerYaw = Math.toRadians((double)mc.player.getYaw());
      double offsetX = -Math.sin(playerYaw) * checkDistance;
      double offsetZ = Math.cos(playerYaw) * checkDistance;

      for (double y = 0.6;
         y <= (double)this.height.get() + 0.6;
         y += 0.1
      ) {
         Box testBox = box.offset(offsetX, y, offsetZ);

         for (BlockPos pos : BlockPos.iterate(
            (int)Math.floor(testBox.minX),
            (int)Math.floor(testBox.minY),
            (int)Math.floor(testBox.minZ),
            (int)Math.floor(testBox.maxX),
            (int)Math.floor(testBox.maxY),
            (int)Math.floor(testBox.maxZ)
         )) {
            BlockState state = mc.world.getBlockState(pos);
            if (!state.isAir()) {
               VoxelShape shape = state.getCollisionShape(mc.world, pos);
               if (!shape.isEmpty()) {
                  for (Box collisionBox : shape.getBoundingBoxes()) {
                     Box offsetBox = collisionBox.offset(pos);
                     float blockHeight = (float)(offsetBox.maxY - mc.player.getY());
                     if (blockHeight > 0.6000000238418579f && blockHeight <= this.height.get()) {
                        maxY = Math.max(maxY, blockHeight);
                     }
                  }
               }
            }
         }
      }

      return maxY;
   }
   public boolean isBlockAbove() {
      Box box = mc.player
         .getBoundingBox()
         .offset(
            0.0,
            1.0,
            0.0
         );

      for (BlockPos pos : BlockPos.iterate(
         (int)Math.floor(box.minX),
         (int)Math.floor(box.minY),
         (int)Math.floor(box.minZ),
         (int)Math.floor(box.maxX),
         (int)Math.floor(box.maxY),
         (int)Math.floor(box.maxZ)
      )) {
         if (!mc.world.getBlockState(pos).isAir()) {
            return true;
         }
      }

      return false;
   }
   public boolean canFall(float distance) {
      Box box = mc.player.getBoundingBox();

      for (double y = 0.1;
         y <= (double)distance;
         y += 0.1
      ) {
         Box testBox = box.offset(
            0.0,
            -y,
            0.0
         );

         for (BlockPos pos : BlockPos.iterate(
            (int)Math.floor(testBox.minX),
            (int)Math.floor(testBox.minY),
            (int)Math.floor(testBox.minZ),
            (int)Math.floor(testBox.maxX),
            (int)Math.floor(testBox.maxY),
            (int)Math.floor(testBox.maxZ)
         )) {
            if (!mc.world.getBlockState(pos).isAir()) {
               return false;
            }
         }
      }

      return true;
   }
}
