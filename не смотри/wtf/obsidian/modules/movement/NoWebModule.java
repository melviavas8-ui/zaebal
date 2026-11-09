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

























import net.minecraft.block.Blocks;
import net.minecraft.network.packet.c2s.play.PlayerActionC2SPacket;
import net.minecraft.network.packet.c2s.play.PlayerActionC2SPacket.Action;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;

public class NoWebModule extends Module {
   public static NoWebModule INSTANCE = new NoWebModule();
   public ModeSetting web;

   public NoWebModule() {
      super("NoWeb", "Убирает замедление от паутины", Category.MOVEMENT);
      String[] var10005 = new String[3];
      var10005[0] = "Коллизия";
      var10005[1] = "Обычный";
      var10005[2] = "Тест";
      this.web = new ModeSetting("Мод", "Коллизия", var10005);
      Setting[] var10001 = new Setting[1];
      var10001[0] = this.web;
      this.addSettings(var10001);
   }

   @EventTarget
   public void onUpdate(EventTick eventUpdate) {
      if (mc.player != null && mc.world != null) {
         if (this.web.is("Коллизия")) {
            BlockPos playerPos = mc.player.getBlockPos();

            for (int x = -1; x <= 1; x++) {
               for (int y = 0; y <= (2); y++) {
                  for (int z = -1; z <= 1; z++) {
                     BlockPos pos = playerPos.add(x, y, z);
                     if (mc.world.getBlockState(pos).getBlock() == Blocks.COBWEB) {
                        mc.player.networkHandler.sendPacket(new PlayerActionC2SPacket(Action.STOP_DESTROY_BLOCK, pos, Direction.UP));
                     }
                  }
               }
            }
         }

         if (this.web.is("Обычный") && (!mc.player.isSneaking() || !mc.player.isOnGround())) {
            boolean headInWeb = false;
            boolean feetInWeb = false;

            for (double x = Double.longBitsToDouble(-4624386169376572703L);
               x <= 0.295;
               x += 0.05
            ) {
               for (double zx = Double.longBitsToDouble(-4624386169376572703L);
                  zx <= 0.295;
                  zx += 0.05
               ) {
                  for (double y = (double)mc.player.getStandingEyeHeight();
                     y >= 0.0;
                     y -= 0.1
                  ) {
                     BlockPos headPos = BlockPos.ofFloored(mc.player.getX() + x, mc.player.getY() + y, mc.player.getZ() + zx);
                     if (mc.world.getBlockState(headPos).getBlock() == Blocks.COBWEB) {
                        headInWeb = true;
                        break;
                     }
                  }
               }
            }

            if (!headInWeb) {
               for (double var20 = Double.longBitsToDouble(-4624386169376572703L);
                  var20 <= 0.295;
                  var20 += 0.05
               ) {
                  for (double zx = Double.longBitsToDouble(-4624386169376572703L);
                     zx <= 0.295;
                     zx += 0.05
                  ) {
                     BlockPos pos = BlockPos.ofFloored(mc.player.getX() + var20, mc.player.getY(), mc.player.getZ() + zx);
                     if (mc.world.getBlockState(pos).getBlock() == Blocks.COBWEB) {
                        feetInWeb = true;
                        break;
                     }
                  }
               }
            }

            BlockPos aboveHeadPos = BlockPos.ofFloored(
               mc.player.getX(),
               mc.player.getY()
                  + (double)mc.player.getStandingEyeHeight()
                  + 0.20000000298023224,
               mc.player.getZ()
            );
            if (!headInWeb && !feetInWeb && mc.world.getBlockState(aboveHeadPos).getBlock() == Blocks.COBWEB) {
               headInWeb = true;
            }

            if (headInWeb || feetInWeb) {
               if (mc.options.jumpKey.isPressed()) {
                  mc.player
                     .setVelocity(
                        0.0,
                        0.8,
                        0.0
                     );
               } else if (mc.options.sneakKey.isPressed()) {
                  mc.player
                     .setVelocity(
                        0.0,
                        Double.longBitsToDouble(-4617991057905706598L),
                        0.0
                     );
               } else {
                  mc.player
                     .setVelocity(
                        0.0,
                        0.0,
                        0.0
                     );
               }

               ObsidianComponent_268.setMotion(0.21);
            }
         }

         if (this.web.is("Тест") && mc.player != null) {
            boolean cobweb = false;
            Box box = mc.player.getBoundingBox();

            for (BlockPos pos : BlockPos.iterate(
               MathHelper.floor(box.minX),
               MathHelper.floor(box.minY),
               MathHelper.floor(box.minZ),
               MathHelper.floor(box.maxX),
               MathHelper.floor(box.maxY),
               MathHelper.floor(box.maxZ)
            )) {
               if (mc.world.getBlockState(pos).isOf(Blocks.COBWEB)) {
                  cobweb = true;
               }
            }

            if (cobweb) {
               Vec3d velocity = mc.player.getVelocity();
               float yaw = mc.player.getYaw();
               double forward = 0.0;
               double strafe = 0.0;
               if (mc.player.input.playerInput.forward()) {
                  forward += 1.0;
               }

               if (mc.player.input.playerInput.backward()) {
                  forward -= 1.0;
               }

               if (mc.player.input.playerInput.left()) {
                  strafe += 1.0;
               }

               if (mc.player.input.playerInput.right()) {
                  strafe -= 1.0;
               }

               if (forward != 0.0
                  || strafe != 0.0) {
                  if (forward != 0.0) {
                     if (strafe > 0.0) {
                        yaw += (float)(
                           forward > 0.0
                              ? -45
                              : 45
                        );
                     } else if (strafe < 0.0) {
                        yaw += (float)(
                           forward > 0.0
                              ? 45
                              : -45
                        );
                     }

                     strafe = 0.0;
                     if (forward > 0.0) {
                        forward = 1.0;
                     } else {
                        forward = Double.longBitsToDouble(-4616189618054758400L);
                     }
                  }

                  double movementYaw = Math.toDegrees(Math.atan2(strafe, forward)) + (double)yaw;
                  yaw = (float)(
                     (
                           movementYaw % 360.0
                              + 360.0
                        )
                        % 360.0
                  );
               }

               float result = 0.6299999952316284f;
               if ((
                     !(yaw >= 313.0f)
                        || !(yaw <= 317.0f)
                  )
                  && (!(yaw >= 223.0f) || !(yaw <= 227.0f))
                  && (!(yaw >= 133.0f) || !(yaw <= 137.0f))
                  && (
                     !(yaw >= 43.0f) || !(yaw <= 47.0f)
                  )) {
                  if ((!(yaw >= 311.0F) || !(yaw <= 319.0f))
                     && (
                        !(yaw >= 221.0f)
                           || !(yaw <= 229.0f)
                     )
                     && (
                        !(yaw >= 131.0f)
                           || !(yaw <= 139.0f)
                     )
                     && (
                        !(yaw >= 41.0f)
                           || !(yaw <= 49.0f)
                     )) {
                     if ((
                           !(yaw >= 310.79998779296875f)
                              || !(yaw <= 320.79998779296875f)
                        )
                        && (
                           !(yaw >= 220.8000030517578f)
                              || !(yaw <= 230.8000030517578f)
                        )
                        && (
                           !(yaw >= 130.8000030517578f)
                              || !(yaw <= 140.8000030517578f)
                        )
                        && (!(yaw >= 40.79999923706055f) || !(yaw <= 50.79999923706055f))) {
                        if ((
                              !(yaw >= 308.70001220703125f)
                                 || !(yaw <= 322.70001220703125f)
                           )
                           && (!(yaw >= 218.6999969482422f) || !(yaw <= 232.6999969482422f))
                           && (
                              !(yaw >= 128.6999969482422f)
                                 || !(yaw <= 142.6999969482422f)
                           )
                           && (!(yaw >= 38.70000076293945f) || !(yaw <= 52.70000076293945f))) {
                           if ((
                                 !(yaw >= 306.5f)
                                    || !(yaw <= 324.5f)
                              )
                              && (
                                 !(yaw >= 216.5f)
                                    || !(yaw <= 234.5f)
                              )
                              && (
                                 !(yaw >= 126.5f)
                                    || !(yaw <= 144.5f)
                              )
                              && (
                                 !(yaw >= 36.5f)
                                    || !(yaw <= 54.5f)
                              )) {
                              if (yaw >= 304.0f
                                    && yaw <= 327.0f
                                 || yaw >= 214.0f
                                    && yaw <= 237.0f
                                 || yaw >= 124.0F && yaw <= 147.0f
                                 || yaw >= 34.0f
                                    && yaw <= 57.0f) {
                                 result = 0.75f;
                              }
                           } else {
                              result = 0.7900000214576721f;
                           }
                        } else {
                           result = 0.8100000023841858f;
                        }
                     } else {
                        result = 0.8299999833106995f;
                     }
                  } else {
                     result = 0.8500000238418579f;
                  }
               } else {
                  result = 0.8799999952316284f;
               }

               if (!mc.options.jumpKey.isPressed()) {
                  if (mc.options.sneakKey.isPressed()) {
                     mc.player
                        .setVelocity(velocity.x, Double.longBitsToDouble(-4608083138725491507L), velocity.z);
                  } else {
                     mc.player.setVelocity(velocity.x, 0.0, velocity.z);
                  }
               } else {
                  mc.player
                     .setVelocity(
                        velocity.x,
                        forward == 0.0
                              && strafe == 0.0
                           ? 1.4
                           : 1.2,
                        velocity.z
                     );
               }

               ObsidianComponent_268.setVelocity((double)result);
            }
         }
      }
   }
}
