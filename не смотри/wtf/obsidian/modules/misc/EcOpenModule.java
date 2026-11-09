package wtf.obsidian.modules.misc;

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

























import net.minecraft.block.Blocks;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;

public class EcOpenModule extends Module {
   public static EcOpenModule INSTANCE = new EcOpenModule();
   private final ObsidianComponent_261 openKey = new ObsidianComponent_261("Открыть", -1);
   private final SliderSetting range = new SliderSetting(
      "Дистанция",
      6.0f,
      3.0f,
      6.0f,
      0.10000000149011612f
   );
   public BlockPos targetChest = null;
   public boolean shouldRotate = false;
   public int rotationTicks = 0;
   public float currentYaw;
   public float currentPitch;

   public EcOpenModule() {
      super("EcOpen", "Открывает эндер сундук по бинду", Category.MISC);
      Setting[] var10001 = new Setting[2];
      var10001[0] = this.openKey;
      var10001[1] = this.range;
      this.addSettings(var10001);
   }

   @Override
   public void onEnable() {
      this.reset();
      super.onEnable();
   }

   @Override
   public void onDisable() {
      this.reset();
      super.onDisable();
   }

   @EventTarget
   public void onBinding(InputEvent event) {
      if (mc.currentScreen == null && mc.player != null && mc.world != null) {
         if (event.getKey() == this.openKey.getKey()) {
            this.findEnderChest();
         }
      }
   }

   @EventTarget
   public void onGameUpdate(Event_93 event) {
      if (this.shouldRotate && this.targetChest != null && mc.player != null) {
         if (!mc.world.getBlockState(this.targetChest).isOf(Blocks.ENDER_CHEST)) {
            this.reset();
         } else {
            Vec3d target = Vec3d.ofCenter(this.targetChest);
            float[] rotations = this.calculateRotation(target);
            float deltaYaw = MathHelper.wrapDegrees(rotations[0] - this.currentYaw);
            float deltaPitch = rotations[1] - this.currentPitch;
            this.currentYaw = this.currentYaw + deltaYaw * 0.800000011920929f;
            this.currentPitch = MathHelper.clamp(
               this.currentPitch + deltaPitch * 0.800000011920929f,
               -90.0f,
               90.0f
            );
            RotationManager.update(
               new ObsidianComponent_olorSetting(this.currentYaw, this.currentPitch),
               360.0f,
               360.0f,
               360.0f,
               360.0f,
               1,
               1,
               false
            );
            this.rotationTicks += 1;
         }
      }
   }

   @EventTarget
   public void onUpdate(EventTick event) {
      if (this.shouldRotate && this.targetChest != null && mc.player != null) {
         if (this.rotationTicks >= (2)) {
            Vec3d hitVec = Vec3d.ofCenter(this.targetChest)
               .add(
                  0.0,
                  0.5,
                  0.0
               );
            BlockHitResult hitResult = new BlockHitResult(hitVec, Direction.UP, this.targetChest, false);
            mc.interactionManager.interactBlock(mc.player, Hand.MAIN_HAND, hitResult);
            mc.player.swingHand(Hand.MAIN_HAND);
            this.reset();
         }

         if (this.rotationTicks > (20)) {
            this.reset();
         }
      }
   }
   public void findEnderChest() {
      BlockPos playerPos = mc.player.getBlockPos();
      int r = this.range.getValue().intValue();
      double maxDist = (double)(this.range.getValue().floatValue() * this.range.getValue().floatValue());
      double closestDist = 179769313486231570814527423731704356798070567525844996598917476803157260780028538760589558632766878171540458953514382464234321326889464182768467546703537516986049910576551282076245490090389328944075868508455133942304583236903222948165808559332123348274797826204144723168738177180919299881250404026184124858368.0;
      BlockPos closest = null;

      for (int x = -r; x <= r; x++) {
         for (int y = -r; y <= r; y++) {
            for (int z = -r; z <= r; z++) {
               BlockPos pos = playerPos.add(x, y, z);
               if (mc.world.getBlockState(pos).isOf(Blocks.ENDER_CHEST)) {
                  double dist = mc.player.getEyePos().squaredDistanceTo(Vec3d.ofCenter(pos));
                  if (dist < closestDist && dist <= maxDist) {
                     closestDist = dist;
                     closest = pos;
                  }
               }
            }
         }
      }

      if (closest != null) {
         this.targetChest = closest;
         this.shouldRotate = true;
         this.rotationTicks = 0;
         this.currentYaw = mc.player.getYaw();
         this.currentPitch = mc.player.getPitch();
      }
   }
   public float[] calculateRotation(Vec3d target) {
      Vec3d eye = mc.player.getEyePos();
      double dx = target.x - eye.x;
      double dy = target.y - eye.y;
      double dz = target.z - eye.z;
      double dist = Math.sqrt(dx * dx + dz * dz);
      float yaw = (float)Math.toDegrees(Math.atan2(dz, dx)) - 90.0f;
      float pitch = (float)(-Math.toDegrees(Math.atan2(dy, dist)));
      float[] var10000 = new float[2];
      var10000[0] = yaw;
      var10000[1] = MathHelper.clamp(
         pitch, -90.0f, 90.0f
      );
      return var10000;
   }
   public void reset() {
      this.targetChest = null;
      this.shouldRotate = false;
      this.rotationTicks = 0;
   }
}
