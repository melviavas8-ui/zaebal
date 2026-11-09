package wtf.obsidian.commands;

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
























import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import net.minecraft.block.BlockState;
import net.minecraft.command.CommandSource;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction.Axis;
import net.minecraft.util.shape.VoxelShape;

public class TpCommand extends Command {
   public TpCommand() {
      super("tp");
   }

   @Override
   public void execute(LiteralArgumentBuilder<CommandSource> builder) {
      builder.then(this.arg("Y", IntegerArgumentType.integer()).executes(context -> {
         int y = (Integer)context.getArgument("Y", Integer.class);
         mc.player.setPosition(mc.player.getX(), mc.player.getY() + (double)y, mc.player.getZ());
         return 1;
      }));
      builder.then(this.literal("up").executes(context -> {
         this.clipToSafeBlock(true);
         return 1;
      }));
      builder.then(this.literal("down").executes(context -> {
         this.clipToSafeBlock(false);
         return 1;
      }));
   }
   public void clipToSafeBlock(boolean up) {
      if (mc.player != null && mc.world != null) {
         int startY = mc.player.getBlockY();
         int minY = mc.world.getBottomY();
         int maxY = mc.world.getTopYInclusive() - 2;
         int step = up ? 1 : -1;
         int from = up ? startY + (1) : startY - (1);
         int to = up ? maxY : minY;

         for (int y = from; up ? y <= to : y >= to; y += step) {
            if (this.isSafeStandPosition(y)) {
               VoxelShape shape = mc.world
                  .getBlockState(new BlockPos(mc.player.getBlockX(), y - (1), mc.player.getBlockZ()))
                  .getCollisionShape(mc.world, new BlockPos(mc.player.getBlockX(), y - 1, mc.player.getBlockZ()));
               double offsetY = shape.isEmpty()
                  ? 0.0
                  : shape.getMax(Axis.Y);
               mc.player.setPosition(mc.player.getX(), (double)y + offsetY, mc.player.getZ());
               return;
            }
         }
      }
   }
   public boolean isSafeStandPosition(int y) {
      BlockPos floorPos = new BlockPos(mc.player.getBlockX(), y - (1), mc.player.getBlockZ());
      BlockPos feetPos = floorPos.up();
      BlockPos headPos = feetPos.up();
      BlockState floorState = mc.world.getBlockState(floorPos);
      if (floorState.getCollisionShape(mc.world, floorPos).isEmpty()) {
         return false;
      } else {
         return (mc.world.getBlockState(feetPos).getCollisionShape(mc.world, feetPos).isEmpty()
               && mc.world.getBlockState(headPos).getCollisionShape(mc.world, headPos).isEmpty());
      }
   }
}
