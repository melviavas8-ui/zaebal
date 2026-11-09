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























import net.minecraft.entity.Entity;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.Vec3d;

public class ObsidianComponent_314 {
   public Vec3d[] points;
   public BlockHitResult blockHit;
   public Vec3d hitPos;
   public Entity entityHit;
   public Vec3d entityHitPos;

   public ObsidianComponent_314(Vec3d[] points, BlockHitResult blockHit, Vec3d hitPos, Entity entityHit, Vec3d entityHitPos) {
      this.points = points;
      this.blockHit = blockHit;
      this.hitPos = hitPos;
      this.entityHit = entityHit;
      this.entityHitPos = entityHitPos;
   }

   public Vec3d[] points() { return this.points; }
   public BlockHitResult blockHit() { return this.blockHit; }
   public Vec3d hitPos() { return this.hitPos; }
   public Entity entityHit() { return this.entityHit; }
   public Vec3d entityHitPos() { return this.entityHitPos; }

}
