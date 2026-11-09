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























public final class ObsidianComponent_194 {
   public final float preTranslateY;
   public final float preTranslateZ;
   public final float anchorY;
   public final float anchorZ;
   public final float pitchRotation;
   public final float rollRotation;
   public final float openMultiplier;
   public final float scaleMultiplier;
   public final float motionSpreadBoost;
   public final float flapAmplitude;
   public final float sideOffset;
   public final float sideYOffset;
   public final float sideZOffset;
   public final float sideRoll;
   public final float sidePitch;
   public final float flapSpeed;

   public ObsidianComponent_194(
      float preTranslateY,
      float preTranslateZ,
      float anchorY,
      float anchorZ,
      float pitchRotation,
      float rollRotation,
      float openMultiplier,
      float scaleMultiplier,
      float motionSpreadBoost,
      float flapAmplitude,
      float sideOffset,
      float sideYOffset,
      float sideZOffset,
      float sideRoll,
      float sidePitch,
      float flapSpeed
   ) {
      this.preTranslateY = preTranslateY;
      this.preTranslateZ = preTranslateZ;
      this.anchorY = anchorY;
      this.anchorZ = anchorZ;
      this.pitchRotation = pitchRotation;
      this.rollRotation = rollRotation;
      this.openMultiplier = openMultiplier;
      this.scaleMultiplier = scaleMultiplier;
      this.motionSpreadBoost = motionSpreadBoost;
      this.flapAmplitude = flapAmplitude;
      this.sideOffset = sideOffset;
      this.sideYOffset = sideYOffset;
      this.sideZOffset = sideZOffset;
      this.sideRoll = sideRoll;
      this.sidePitch = sidePitch;
      this.flapSpeed = flapSpeed;
   }
}
