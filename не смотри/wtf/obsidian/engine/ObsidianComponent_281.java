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
























import net.minecraft.util.math.Vec3d;
public class ObsidianComponent_281 {
   public final Vec3d direction;
   public final float spread;
   public final float swirlAmount;
   public final float rotationScale;
   public final float timeScale;
   public final float progressOffset;
   public final int color;

   public ObsidianComponent_281(Vec3d direction, float spread, float swirlAmount, float rotationScale, float timeScale, float progressOffset, int color) {
      this.direction = direction;
      this.spread = spread;
      this.swirlAmount = swirlAmount;
      this.rotationScale = rotationScale;
      this.timeScale = timeScale;
      this.progressOffset = progressOffset;
      this.color = color;
   }
}
