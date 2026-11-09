package wtf.obsidian.modules.player;

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

























import java.util.Comparator;
import java.util.HashSet;
import java.util.Set;
import net.minecraft.block.BlockState;
import net.minecraft.registry.Registries;
import net.minecraft.util.Hand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Vec3d;

public class NukerModule extends Module {
   public static NukerModule INSTANCE = new NukerModule();
   private final SliderSetting radius = new SliderSetting(
      "Дистанция",
      3.0f,
      1.0f,
      5.0f,
      1.0f
   );
   private final BooleanSetting breakAll = new BooleanSetting("Ломать все блоки", false);
   private final BooleanSetting swing = new BooleanSetting("Анимация руки", true);
   private final Set<String> targetBlocks = new HashSet<>();
   public BlockPos currentTargetBlock;

   public NukerModule() {
      super("Nuker", "Автоматически ломает блоки в радиусе", Category.PLAYER);
      Setting[] var10001 = new Setting[3];
      var10001[0] = this.radius;
      var10001[1] = this.breakAll;
      var10001[2] = this.swing;
      this.addSettings(var10001);
   }

   @EventTarget
   public void onUpdate(EventTick event) {
      if (mc.player == null || mc.world == null || mc.interactionManager == null) {
         this.resetBreaking();
      } else if (!this.breakAll.isState() && this.targetBlocks.isEmpty()) {
         this.resetBreaking();
      } else {
         if (!this.isCurrentTargetValid()) {
            this.currentTargetBlock = this.findNewTarget();
         }

         if (this.currentTargetBlock != null) {
            this.breakCurrentTarget();
         }
      }
   }
   public boolean isCurrentTargetValid() {
      return (this.currentTargetBlock != null && this.isInRange(this.currentTargetBlock) && this.shouldBreak(this.currentTargetBlock));
   }
   public BlockPos findNewTarget() {
      int range = Math.round(this.radius.get());
      BlockPos playerPos = mc.player.getBlockPos();
      return BlockPos.stream(playerPos.add(-range, 0, -range), playerPos.add(range, range, range))
         .<BlockPos>map(BlockPos::toImmutable)
         .filter(this::isInRange)
         .filter(this::shouldBreak)
         .min(Comparator.comparingDouble(pos -> mc.player.squaredDistanceTo(Vec3d.ofCenter(pos))))
         .orElse(null);
   }
   public boolean isInRange(BlockPos pos) {
      double maxDistance = (double)this.radius.get();
      return (mc.player.squaredDistanceTo(Vec3d.ofCenter(pos)) <= maxDistance * maxDistance);
   }
   public boolean shouldBreak(BlockPos pos) {
      BlockState state = mc.world.getBlockState(pos);
      if (state == null || state.isAir() || state.getHardness(mc.world, pos) < 0.0f) {
         return false;
      } else if (this.breakAll.isState()) {
         return true;
      } else {
         String blockName = Registries.BLOCK.getId(state.getBlock()).getPath().toLowerCase();
         return this.targetBlocks.contains(blockName);
      }
   }
   public void breakCurrentTarget() {
      if (this.currentTargetBlock != null && mc.player != null && mc.interactionManager != null) {
         mc.interactionManager.attackBlock(this.currentTargetBlock, Direction.UP);
         mc.interactionManager.updateBlockBreakingProgress(this.currentTargetBlock, Direction.UP);
         if (this.swing.isState()) {
            mc.player.swingHand(Hand.MAIN_HAND);
         }

         if (mc.world.getBlockState(this.currentTargetBlock).isAir()) {
            this.resetBreaking();
         }
      }
   }
   public void resetBreaking() {
      this.currentTargetBlock = null;
      if (mc.interactionManager != null) {
         mc.interactionManager.cancelBlockBreaking();
      }
   }

   public void addBlock(String blockName) {
      this.targetBlocks.add(normalizeBlockName(blockName));
   }

   public void removeBlock(String blockName) {
      this.targetBlocks.remove(normalizeBlockName(blockName));
   }

   public void clearBlocks() {
      this.targetBlocks.clear();
      this.resetBreaking();
   }

   public boolean isTargetBlock(String blockName) {
      return this.targetBlocks.contains(normalizeBlockName(blockName));
   }

   public Set<String> getTargetBlocks() {
      return new HashSet<>(this.targetBlocks);
   }

   public static String normalizeBlockName(String blockName) {
      if (blockName == null) {
         return "";
      } else {
         String normalized = blockName.toLowerCase().trim();
         int namespaceSeparator = normalized.indexOf(58);
         return namespaceSeparator >= 0 ? normalized.substring(namespaceSeparator + (1)) : normalized;
      }
   }

   @Override
   public void onDisable() {
      this.resetBreaking();
      super.onDisable();
   }
}
