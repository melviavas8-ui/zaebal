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
import net.minecraft.block.Blocks;
import net.minecraft.block.ShulkerBoxBlock;
import net.minecraft.client.gui.screen.ingame.ShulkerBoxScreen;
import net.minecraft.entity.vehicle.BoatEntity;
import net.minecraft.util.math.BlockPos;

public class HighJumpModule extends Module {
   public static HighJumpModule INSTANCE = new HighJumpModule();
   public final ModeSetting mode;
   public final SliderSetting slimeMultiplier;
   public boolean wasInBoat;
   public double lastVelY;
   public int cooldown;

   public HighJumpModule() {
      super("HighJump", "Высокий прыжок от различных источников", Category.MOVEMENT);
      String[] var10005 = new String[3];
      var10005[0] = "Shulker";
      var10005[1] = "Slime";
      var10005[2] = "Boat";
      this.mode = new ModeSetting("Режим", "Shulker", var10005);
      this.slimeMultiplier = new SliderSetting(
         "Множитель",
         2.0f,
         1.100000023841858f,
         5.0f,
         0.10000000149011612f
      );
      Setting[] var10001 = new Setting[2];
      var10001[0] = this.mode;
      var10001[1] = this.slimeMultiplier;
      this.addSettings(var10001);
   }

   @Override
   public void onEnable() {
      super.onEnable();
      this.wasInBoat = false;
      this.lastVelY = 0.0;
      this.cooldown = 0;
   }

   @Override
   public void onDisable() {
      super.onDisable();
      this.wasInBoat = false;
      this.lastVelY = 0.0;
      this.cooldown = 0;
   }

   @EventTarget
   public void onUpdate(EventTick event) {
      if (mc.player != null && mc.world != null) {
         if (this.cooldown > 0) {
            this.cooldown -= 1;
         }

         if (this.mode.is("Shulker")) {
            this.handleShulker();
         }

         if (this.mode.is("Slime")) {
            this.handleSlime();
         }

         if (this.mode.is("Boat")) {
            this.handleBoat();
         }
      }
   }
   public void handleShulker() {
      if (mc.currentScreen instanceof ShulkerBoxScreen) {
         BlockPos playerPos = mc.player.getBlockPos();
         BlockPos[] var10000 = new BlockPos[6];
         var10000[0] = playerPos.down();
         var10000[1] = playerPos;
         var10000[2] = playerPos.north();
         var10000[3] = playerPos.south();
         var10000[4] = playerPos.east();
         var10000[5] = playerPos.west();
         BlockPos[] checkPositions = var10000;
         boolean onShulker = false;
         BlockPos[] var4 = checkPositions;
         int var5 = checkPositions.length;

         for (int var6 = 0; var6 < var5; var6++) {
            BlockPos pos = var4[var6];
            BlockState state = mc.world.getBlockState(pos);
            if (state.getBlock() instanceof ShulkerBoxBlock) {
               onShulker = true;
               break;
            }
         }

         if (onShulker) {
            mc.player
               .setVelocity(
                  mc.player.getVelocity().x,
                  2.0,
                  mc.player.getVelocity().z
               );
            mc.player.closeHandledScreen();
         }
      }
   }
   public void handleSlime() {
      double velY = mc.player.getVelocity().y;
      BlockPos below = mc.player.getBlockPos().down();
      BlockPos belowTwo = mc.player.getBlockPos().down(2);
      boolean onSlime = (!(!mc.world.getBlockState(below).isOf(Blocks.SLIME_BLOCK) && !mc.world.getBlockState(belowTwo).isOf(Blocks.SLIME_BLOCK)));
      if (this.lastVelY < Double.longBitsToDouble(-4631501856787818086L)
         && velY > 0.1
         && onSlime
         && this.cooldown == 0) {
         double boostedVel = velY * (double)this.slimeMultiplier.get();
         mc.player.setVelocity(mc.player.getVelocity().x, boostedVel, mc.player.getVelocity().z);
         this.cooldown = 5;
      }

      this.lastVelY = velY;
   }
   public void handleBoat() {
      boolean inBoat = mc.player.getVehicle() instanceof BoatEntity;
      if (this.wasInBoat && !inBoat && this.cooldown == 0) {
         mc.player
            .setVelocity(
               mc.player.getVelocity().x,
               1.5,
               mc.player.getVelocity().z
            );
         this.cooldown = 20;
      }

      this.wasInBoat = inBoat;
   }
}
