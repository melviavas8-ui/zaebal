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























public class ObsidianComponent_262 {
   public final float x;
   public final float y;
   public final float z;
   public final int r;
   public final int g;
   public final int b;
   public final int a;
public ObsidianComponent_262(float x, float y, float z, int color) {
      this.x = x;
      this.y = y;
      this.z = z;
      this.a = color >> 24 & 255;
      this.r = color >> 16 & (255);
      this.g = color >> 8 & (255);
      this.b = color & (255);
   }
}
