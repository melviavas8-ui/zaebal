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























public class ObsidianComponent_124 {
   public final float radiusX;
   public final float radiusZ;
   public final float yOffset;
   public final float startDeg;
   public final float arcDeg;
   public final float tiltX;
   public final float tiltZ;
   public final float speedDeg;
   public final float alphaMul;
   public final int startColor;
   public final int endColor;
   public final float baseYaw;

   public ObsidianComponent_124(
      float radiusX,
      float radiusZ,
      float yOffset,
      float startDeg,
      float arcDeg,
      float tiltX,
      float tiltZ,
      float speedDeg,
      float alphaMul,
      int startColor,
      int endColor
   ) {
      this.radiusX = radiusX;
      this.radiusZ = radiusZ;
      this.yOffset = yOffset;
      this.startDeg = startDeg;
      this.arcDeg = arcDeg;
      this.tiltX = tiltX;
      this.tiltZ = tiltZ;
      this.speedDeg = speedDeg;
      this.alphaMul = alphaMul;
      this.startColor = startColor;
      this.endColor = endColor;
      this.baseYaw = startDeg * 0.35F;
   }
}
