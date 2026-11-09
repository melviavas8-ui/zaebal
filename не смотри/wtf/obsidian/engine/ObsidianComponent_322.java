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
























import net.minecraft.util.math.MathHelper;
public class ObsidianComponent_322 {
   public final float x;
   public final float y;
   public final float z;
   public final long startTime;
   public final int maxLife;
   public final int baseColor;
public ObsidianComponent_322(float x, float y, float z, int maxLife, int baseColor) {
      this.x = x;
      this.y = y;
      this.z = z;
      this.startTime = System.currentTimeMillis();
      this.maxLife = maxLife;
      this.baseColor = baseColor;
   }

   public boolean shouldRemove() {
      return (System.currentTimeMillis() - this.startTime >= (long)this.maxLife);
   }

   public float getTimeProgress() {
      return MathHelper.clamp(
         (float)(System.currentTimeMillis() - this.startTime) / (float)this.maxLife,
         0.0f,
         1.0f
      );
   }

   public int getColor(float timePC) {
      int a = (int)(
         (float)(this.baseColor >> (24) & 255)
            * (1.0f - timePC)
      );
      a = Math.max(0, Math.min(255, a));
      return a << 24 | this.baseColor & (16777215);
   }
}
