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























public class ObsidianComponent_418 {
   public float ox;
   public float oy;
   public float x;
   public float y;
   public float vx;
   public float vy;
   public float phase;
   public float speed;
   public float fade;
public ObsidianComponent_418(float ox, float oy) {
      this.ox = ox;
      this.oy = oy;
      this.x = ox;
      this.y = oy;
      this.phase = (float)(
         Math.random()
            * 3.141592653589793
            * 2.0
      );
      this.speed = 0.5f
         + (float)(Math.random() * 0.699999988079071);
      this.vx = 0.0f;
      this.vy = 0.0f;
   }
}
