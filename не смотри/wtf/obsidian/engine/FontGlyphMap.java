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
import net.minecraft.util.math.Vec3d;

public class FontGlyphMap {
   public Entity entity;
   public Vec3d hitPos;

   public FontGlyphMap(Entity entity, Vec3d hitPos) {
      this.entity = entity;
      this.hitPos = hitPos;
   }

   public Entity entity() { return this.entity; }
   public Vec3d hitPos() { return this.hitPos; }

}
