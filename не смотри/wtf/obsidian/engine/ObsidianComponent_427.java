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























public class ObsidianComponent_427 {
   public double x;
   public double y;
   public double z;
   public float vx;
   public float vy;
   public float vz;
   public float rotX;
   public float rotY;
   public float rotZ;
   public float rotSpeedX;
   public float rotSpeedY;
   public float rotSpeedZ;
   public float wobblePhase;
   public float wobbleOffset;
   public float renderAlpha;
   public int life;
   public int maxLife;
public ObsidianComponent_427(
      double x,
      double y,
      double z,
      float vx,
      float vy,
      float vz,
      float rotX,
      float rotY,
      float rotZ,
      float rotSpeedX,
      float rotSpeedY,
      float rotSpeedZ,
      int life,
      float wobblePhase,
      float wobbleOffset
   ) {
      this.x = x;
      this.y = y;
      this.z = z;
      this.vx = vx;
      this.vy = vy;
      this.vz = vz;
      this.rotX = rotX;
      this.rotY = rotY;
      this.rotZ = rotZ;
      this.rotSpeedX = rotSpeedX;
      this.rotSpeedY = rotSpeedY;
      this.rotSpeedZ = rotSpeedZ;
      this.life = this.maxLife = life;
      this.wobblePhase = wobblePhase;
      this.wobbleOffset = wobbleOffset;
   }
}
