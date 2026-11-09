package wtf.obsidian.mixin;

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























import net.minecraft.block.AbstractBlock;
import net.minecraft.block.BlockState;
import net.minecraft.block.ShapeContext;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.shape.VoxelShape;
import net.minecraft.util.shape.VoxelShapes;
import net.minecraft.world.BlockView;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;



@Mixin({AbstractBlock.class})
public class AbstractBlockMixin {
   @Inject(
      method = {"getOutlineShape"},
      at = {@At("HEAD")},
      cancellable = true
   )
   public void getOutlineShape(BlockState state, BlockView world, BlockPos pos, ShapeContext context, CallbackInfoReturnable<VoxelShape> cir) {
      Event event = new Event_181(pos);
      event.call();
      if (event.isCancelled()) {
         cir.setReturnValue(VoxelShapes.empty());
      }
   }

   @Inject(
      method = {"getCollisionShape"},
      at = {@At("HEAD")},
      cancellable = true
   )
   public void getCollisionShape(BlockState state, BlockView world, BlockPos pos, ShapeContext context, CallbackInfoReturnable<VoxelShape> cir) {
      Event event = new Event_181(pos);
      event.call();
      if (event.isCancelled()) {
         cir.setReturnValue(VoxelShapes.empty());
      }
   }

   @Inject(
      method = {"getRaycastShape"},
      at = {@At("HEAD")},
      cancellable = true
   )
   public void getRaycastShape(BlockState state, BlockView world, BlockPos pos, CallbackInfoReturnable<VoxelShape> cir) {
      Event event = new Event_181(pos);
      event.call();
      if (event.isCancelled()) {
         cir.setReturnValue(VoxelShapes.empty());
      }
   }
}
